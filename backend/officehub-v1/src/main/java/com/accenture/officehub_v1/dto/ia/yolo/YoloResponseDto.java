package com.accenture.officehub_v1.dto.ia.yolo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record YoloResponseDto(
        String filename,
        Integer count,
        List<YoloDetectionDto> detections
) {
}
