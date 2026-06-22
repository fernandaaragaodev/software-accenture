from typing import Dict, List

from pydantic import BaseModel


class Detection(BaseModel):
    class_id: int
    class_name: str
    confidence: float
    bbox_xyxy: List[float]
    center_x: float
    center_y: float


class DetectResponse(BaseModel):
    filename: str
    count: int
    summary: Dict[str, int]
    detections: List[Detection]
