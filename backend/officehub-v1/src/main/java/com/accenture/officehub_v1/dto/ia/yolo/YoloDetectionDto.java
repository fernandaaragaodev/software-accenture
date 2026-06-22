package com.accenture.officehub_v1.dto.ia.yolo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record YoloDetectionDto(
        @JsonProperty("class_name")
        String className,
        @JsonProperty("center_x")
        Double centerX,
        @JsonProperty("center_y")
        Double centerY,
        Double confidence,
        @JsonProperty("bbox_xyxy")
        List<Double> bboxXyxy
) {
}
