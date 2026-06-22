from fastapi import FastAPI, File, HTTPException, UploadFile
from fastapi.middleware.cors import CORSMiddleware

from app.config import settings
from app.schemas import DetectResponse
from app.yolo_service import yolo_service

app = FastAPI(title=settings.app_name, version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)


@app.get("/")
def root():
    return {
        "message": "YOLO FastAPI API rodando",
        "docs": "/docs",
        "health": "/health",
        "detect": "/detect",
    }


@app.get("/health")
def health():
    return {
        "status": "ok",
        "model": settings.model_path,
        "confidence_threshold": settings.confidence_threshold,
    }


@app.post("/detect", response_model=DetectResponse)
async def detect(file: UploadFile = File(...)):
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="Envie um arquivo de imagem válido.")

    image_bytes = await file.read()

    if not image_bytes:
        raise HTTPException(status_code=400, detail="Arquivo vazio.")

    detections = yolo_service.detect(image_bytes)
    summary = yolo_service.summarize_by_class(detections)

    return {
        "filename": file.filename or "imagem",
        "count": len(detections),
        "summary": summary,
        "detections": detections,
    }
