from fastapi import FastAPI, UploadFile, File, HTTPException, Form,Query
from fastapi.responses import JSONResponse
from ultralytics import YOLO
import uvicorn
from PIL import Image
import io
import requests
from pydantic import BaseModel
from fastapi.responses import StreamingResponse, FileResponse
from starlette.concurrency import run_in_threadpool
from realesrgan_ncnn_py import Realesrgan
app = FastAPI(title="Stellarvision AI API")
model = YOLO("model/yolov12n_.pt")
UPSCALER = Realesrgan(gpuid=0, tta_mode=False, tilesize=0,model=4)
MODEL = "realesrgan-x4plus"
SCALE = 4
SUPPORTED_CT = ("image/jpeg", "image/png", "image/webp")
CONF_THRESHOLD = 0.7  # 신뢰도 기준값
def _upscale_image(pil_img: Image.Image, scale: int, model: str) -> Image.Image:
    return UPSCALER.process_pil(pil_img)
@app.post("/api/upscale/photo")
async def upscale_photo(
        file: UploadFile = File(...),
):
    # 이미지 파일만 허용
    if file.content_type not in ("image/jpeg", "image/png", "image/webp"):
        raise HTTPException(status_code=400, detail="Only jpg/png/webp supported")
    try:
        # 업로드 → PIL 이미지
        data = await file.read()
        img = Image.open(io.BytesIO(data)).convert("RGB")
        # 업스케일 처리
        upscaled = await run_in_threadpool(_upscale_image, img, SCALE, MODEL)
        # 결과를 PNG 바이트로 변환
        buf = io.BytesIO()
        upscaled.save(buf, format="PNG")
        buf.seek(0)
        return StreamingResponse(
            buf,
            media_type="image/png"
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"처리 실패: {str(e)}")
@app.post("/api/detect/streaming")
async def detect_stream(file: UploadFile = File(...)):
    if file.content_type not in ["image/jpeg", "image/png"]:
        raise HTTPException(status_code=400, detail="Only JPEG or PNG images are supported.")
    try:
        image_bytes = await file.read()
        image = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        results = model(image,device='cuda')
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
        results = model(image, device='cuda')
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