from fastapi import FastAPI, UploadFile, File, HTTPException, Form
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from ultralytics import YOLO
from typing import List
import uvicorn
from PIL import Image
import io
import cv2
import requests
from PIL import UnidentifiedImageError
from pydantic import BaseModel
from fastapi.responses import StreamingResponse
import torch
import numpy as np
import asyncio
from concurrent.futures import ThreadPoolExecutor
app = FastAPI(title="Stellarvision AI API")
model = YOLO("model/yolov12n.pt")
model_EDSR="model/EDSR_x2.pb"
CONF_THRESHOLD = 0.7  # 신뢰도 기준값

origins = [
    "http://localhost:5173",
    "https://localhost:5173",  # HTTPS
    "https://i13c106.p.ssafy.io"
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class OpenCvSuperRes:
    def __init__(self, model_path: str = model_EDSR, model_name: str = "edsr", scale: int = 2):
        self.sr = cv2.dnn_superres.DnnSuperResImpl_create()
        self.sr.readModel(model_path)
        self.sr.setModel(model_name, scale)

    def upscale(self, pil_img: Image.Image) -> Image.Image:
        # PIL 이미지를 OpenCV 이미지로 변환 (RGB -> BGR)
        cv_image = cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)
        # OpenCV DNN 슈퍼해상도로 업스케일
        upscaled_cv = self.sr.upsample(cv_image)
        # OpenCV 이미지 → PIL 이미지로 변환 (BGR -> RGB)
        upscaled_pil = Image.fromarray(cv2.cvtColor(upscaled_cv, cv2.COLOR_BGR2RGB))
        return upscaled_pil

opencv_sr = OpenCvSuperRes(model_path=model_EDSR, model_name="edsr", scale=2)
executor = ThreadPoolExecutor(max_workers=2)

async def run_in_threadpool(func, *args):
    loop = asyncio.get_running_loop()
    return await loop.run_in_executor(executor, func, *args)

def upscale_sync(pil_img):
    return opencv_sr.upscale(pil_img)

@app.post("/api/upscale/photo")
async def upscale_photo(file: UploadFile = File(...)):
    try:
        image_bytes = await file.read()
        image = Image.open(io.BytesIO(image_bytes)).convert("RGB")

        upscaled_image = await run_in_threadpool(upscale_sync, image)  # 동기 작업을 비동기로 실행

        img_byte_arr = io.BytesIO()
        upscaled_image.save(img_byte_arr, format="PNG")
        img_byte_arr.seek(0)

        return StreamingResponse(img_byte_arr, media_type="image/png")

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Inference failed: {str(e)}")


@app.post("/api/detect/streaming")
async def detect_stream(file: UploadFile = File(...)):

    if file.content_type not in ["image/jpeg", "image/png"]:
        raise HTTPException(status_code=400, detail="Only JPEG or PNG images are supported.")

    try:
        image_bytes = await file.read()
        image = Image.open(io.BytesIO(image_bytes)).convert("RGB")

        results = model(image,device='cpu')

        pred_list = []
        result = results[0]

        for box in result.boxes:
            cls_id = int(box.cls.cpu().numpy())
            conf = float(box.conf.cpu().numpy())
            if conf < CONF_THRESHOLD:  # 신뢰도 기준치 미달은 무시
                continue

            bbox = box.xyxy[0].cpu().numpy().tolist()  # [xmin, ymin, xmax, ymax]
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

    try:
        resp = requests.get(payload.image_url)
        if resp.status_code != 200:
            raise HTTPException(status_code=400, detail="Failed to fetch image from URL.")

        content_type = resp.headers.get("Content-Type", "")
        if not (content_type.startswith("image/")):
            raise HTTPException(status_code=400, detail="URL does not point to an image.")

        image = Image.open(io.BytesIO(resp.content)).convert("RGB")
        results = model(image, device='cpu')
        pred_list = []
        result = results[0]

        for box in result.boxes:
            cls_id = int(box.cls.cpu().numpy())
            conf = float(box.conf.cpu().numpy())
            if conf < CONF_THRESHOLD:  # 신뢰도 기준치 미달은 무시
                continue

            bbox = box.xyxy[0].cpu().numpy().tolist()  # [xmin, ymin, xmax, ymax]
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
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
