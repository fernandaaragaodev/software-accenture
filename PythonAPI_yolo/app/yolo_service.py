from collections import Counter
from io import BytesIO
from typing import Any, Dict, List

from PIL import Image
from ultralytics import YOLO

from app.config import settings


class YoloService:
    def __init__(self) -> None:
        self.model = YOLO(settings.model_path)

    def detect(self, image_bytes: bytes) -> List[Dict[str, Any]]:
        image = Image.open(BytesIO(image_bytes)).convert("RGB")

        # Usa filtros reais do YOLO:
        # - conf remove previsões fracas;
        # - iou controla o NMS, removendo caixas sobrepostas;
        # - max_det limita a quantidade máxima de detecções finais.
        results = self.model.predict(
            source=image,
            imgsz=1280,
            conf=settings.confidence_threshold,
            iou=settings.iou_threshold,
            max_det=settings.max_detections,
            agnostic_nms=False,
            verbose=False,
        )

        detections: List[Dict[str, Any]] = []

        for result in results:
            for box in result.boxes:
                confidence = float(box.conf[0].item())

                # Segurança extra: garante que nada abaixo do filtro entre na resposta.
                if confidence < settings.confidence_threshold:
                    continue

                class_id = int(box.cls[0].item())
                class_name = self.model.names[class_id]
                x1, y1, x2, y2 = [float(v) for v in box.xyxy[0].tolist()]

                detections.append(
                    {
                        "class_id": class_id,
                        "class_name": class_name,
                        "confidence": confidence,
                        "bbox_xyxy": [x1, y1, x2, y2],
                        "center_x": (x1 + x2) / 2,
                        "center_y": (y1 + y2) / 2,
                    }
                )

        return detections

    @staticmethod
    def summarize_by_class(detections: List[Dict[str, Any]]) -> Dict[str, int]:
        return dict(Counter(detection["class_name"] for detection in detections))


yolo_service = YoloService()
