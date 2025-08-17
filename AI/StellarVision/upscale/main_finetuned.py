from fastapi import FastAPI, UploadFile, File, HTTPException, Form, Query
from fastapi.responses import JSONResponse, StreamingResponse, FileResponse
from starlette.concurrency import run_in_threadpool
from ultralytics import YOLO
from basicsr.archs.rrdbnet_arch import RRDBNet
from realesrgan import RealESRGANer
from PIL import Image
from pydantic import BaseModel
from anyio import Semaphore
import uvicorn
import asyncio
import torch
import numpy as np
import io
import requests

app = FastAPI(title="Stellarvision AI API")

model = YOLO("model/yolov12n_.pt")

# RealESRGAN 모델 설정
MODEL = RRDBNet(num_in_ch=3, num_out_ch=3, num_feat=64, num_block=23, num_grow_ch=32, scale=4)
SCALE = 4

UPSCALER = RealESRGANer(
    scale=SCALE,
    model_path='model/realesrgan-x4plus_finetuned.pth',
    model=MODEL,
    tile=512,        
    tile_pad=32,     
    pre_pad=0,
    half=True        # FP16으로 GPU 메모리 절약
)

# 상수 설정
SUPPORTED_CT = ("image/jpeg", "image/png", "image/webp")
CONF_THRESHOLD = 0.7
MAX_CONCURRENT_THREADS = 2  # 동시 처리 스레드 수 제한
MAX_IMAGE_SIZE = 2000 * 2000  # 최대 이미지 크기 (4MP)

# 스레드 제한을 위한 세마포어
thread_guard = Semaphore(MAX_CONCURRENT_THREADS)

async def safe_run_in_threadpool(func, *args):    
    async with thread_guard:
        return await run_in_threadpool(func, *args)

def _upscale_image(pil_img: Image.Image) -> Image.Image:   
    try:
        # GPU 메모리 정리
        if torch.cuda.is_available():
            torch.cuda.empty_cache()
        
        img_array = np.array(pil_img)
        
        # 큰 이미지는 크기 제한
        h, w = img_array.shape[:2]
        if h * w > MAX_IMAGE_SIZE:
            temp_img = Image.fromarray(img_array)
            temp_img.thumbnail((2000, 2000), Image.Resampling.LANCZOS)
            img_array = np.array(temp_img)
        
        output, _ = UPSCALER.enhance(img_array, outscale=SCALE)
        
    # 처리 후 GPU 메모리 정리
        if torch.cuda.is_available():
            torch.cuda.empty_cache()
            
        return Image.fromarray(output)
        
    except Exception as e:        
        if torch.cuda.is_available():
            torch.cuda.empty_cache()
        raise e

@app.post("/api/upscale/photo")
async def upscale_photo(file: UploadFile = File(...)):
    """이미지 업스케일 API"""
    if file.content_type not in SUPPORTED_CT:
        raise HTTPException(status_code=400, detail="Only jpg/png/webp supported")
    
    try:
        data = await file.read()
        img = Image.open(io.BytesIO(data)).convert("RGB")
        
        upscaled = await asyncio.wait_for(
            safe_run_in_threadpool(_upscale_image, img),
            timeout=180.0
        )
        
        buf = io.BytesIO()
        upscaled.save(buf, format="PNG")
        buf.seek(0)
        
        return StreamingResponse(buf, media_type="image/png")
        
    except asyncio.TimeoutError:
        raise HTTPException(status_code=408, detail="업스케일 처리 시간 초과 (180초)")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"처리 실패: {str(e)}")

@app.post("/api/detect/streaming")
async def detect_stream(file: UploadFile = File(...)):
    """이미지 업로드 객체 탐지 API"""
    if file.content_type not in ["image/jpeg", "image/png"]:
        raise HTTPException(status_code=400, detail="Only JPEG or PNG images are supported.")
    
    try:
        image_bytes = await file.read()
        image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        
        results = model(image, device='cuda')
        pred_list = []
        result = results[0]
        
        for box in result.boxes:
            cls_id = int(box.cls.cpu().numpy())
            conf = float(box.conf.cpu().numpy())
            
            if conf < CONF_THRESHOLD:
                continue
                
            bbox = box.xyxy[0].cpu().numpy().tolist()
            cls_name = model.names[cls_id] if model.names else str(cls_id)
            
            pred_list.append({
                "class": cls_name,
                "confidence": round(conf, 4),
                "bbox": [round(coord, 2) for coord in bbox]
            })
        
        return JSONResponse(content={"predictions": pred_list})
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Inference failed: {str(e)}")

class ImageURLInput(BaseModel):
    photoid: int
    image_url: str

@app.post("/api/detect/photo")
async def detect_photo(payload: ImageURLInput):
    """URL 이미지 객체 탐지 API"""
    try:
        resp = requests.get(payload.image_url, timeout=10)
        if resp.status_code != 200:
            raise HTTPException(status_code=400, detail="Failed to fetch image from URL.")
        
        content_type = resp.headers.get("Content-Type", "")
        if not content_type.startswith("image/"):
            raise HTTPException(status_code=400, detail="URL does not point to an image.")
        
        image = Image.open(io.BytesIO(resp.content)).convert("RGB")
        results = model(image, device='cuda')
        pred_list = []
        result = results[0]
        
        for box in result.boxes:
            cls_id = int(box.cls.cpu().numpy())
            conf = float(box.conf.cpu().numpy())
            
            if conf < CONF_THRESHOLD:
                continue
                
            bbox = box.xyxy[0].cpu().numpy().tolist()
            cls_name = model.names[cls_id] if model.names else str(cls_id)
            
            pred_list.append({
                "class": cls_name,
                "confidence": round(conf, 4),
                "bbox": [round(coord, 2) for coord in bbox]
            })
        
        return JSONResponse(content={
            "photoid": payload.photoid,
            "predictions": pred_list
        })
        
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Inference failed: {str(e)}")

@app.get("/health")
async def health_check():
    """서버 상태 확인 API"""
    return {"status": "healthy", "message": "Stellarvision AI API is running"}

if __name__ == "__main__":
    uvicorn.run(
        app, 
        host="0.0.0.0", 
        port=8001,
        workers=1,              # 단일 워커로 GPU 충돌 방지
        limit_concurrency=5,    # 동시 요청 수 제한
        timeout_keep_alive=30   # 클라이언트와의 연결 유지 시간
    )
