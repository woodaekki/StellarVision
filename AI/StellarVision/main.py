from fastapi import FastAPI, UploadFile, File, HTTPException, Form
from fastapi.responses import JSONResponse
from ultralytics import YOLO
from typing import List
import uvicorn
from PIL import Image
import io

app = FastAPI(title="YOLOv12n Constellation Detection API")
model = YOLO("model/yolov12n.pt")
CONF_THRESHOLD = 0.7  # 신뢰도 기준값
@app.post("/api/detect/streaming")
async def predict_image(file: UploadFile = File(...)):

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
@app.post("/api/detect/photo")
async def predict_image(
    photoid: int = Form(...),         # 숫자 파라미터로 photoid 받기
    file: UploadFile = File(...)      # 이미지 파일 받기
):
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

        return JSONResponse(content={
            "photoid": photoid,  # 반환값에 photoid를 포함!
            "predictions": pred_list
        })

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Inference failed: {str(e)}")
if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
