from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    app_name: str = "YOLO FastAPI - Plantas OfficeHub"
    model_path: str = "models/best.pt"

    # Filtros da inferência.
    # confidence_threshold baixo demais (ex: 0.001 ou 0) gera muitas caixas falsas/duplicadas.
    confidence_threshold: float = 0.10
    iou_threshold: float = 0.45
    max_detections: int = 300

    class Config:
        env_file = ".env"
        extra = "ignore"


settings = Settings()
