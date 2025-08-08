from fastapi import FastAPI, UploadFile, File, HTTPException, Form
from fastapi.responses import JSONResponse
from ultralytics import YOLO
from typing import List
import uvicorn
from PIL import Image
import io
import requests
from PIL import UnidentifiedImageError
from pydantic import BaseModel
from aura_sr import AuraSR
from fastapi.responses import StreamingResponse
import torch
app = FastAPI(title="Stellarvision AI API")
model = YOLO("model/yolov12n.pt")
CONF_THRESHOLD = 0.7  # 신뢰도 기준값


device = torch.device("cpu")
aura_sr = AuraSR.from_pretrained()

@app.post("/api/upscale/photo")
async def upscale_photo(file: UploadFile = File(...)):
    try:
        image_bytes = await file.read()
        image = Image.open(io.BytesIO(image_bytes)).convert("RGB")

        upscaled_image = aura_sr.upscale_4x(image)

        # 업스케일링 결과를 메모리 내 바이너리 스트림으로 저장
        img_byte_arr = io.BytesIO()
        upscaled_image.save(img_byte_arr, format="PNG")
        img_byte_arr.seek(0)  # 스트림 위치 리셋

        # 이미지 파일을 StreamingResponse로 반환
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
