package com.accenture.officehub_v1.service.ia.layout;

import com.accenture.officehub_v1.dto.ia.yolo.YoloDetectionDto;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class CoordinateTransformationService {

    private static final int SCALE = 4;

    public List<DetectedObject> transform(
            List<YoloDetectionDto> detections,
            int imageWidth,
            int imageHeight,
            BigDecimal roomWidth,
            BigDecimal roomHeight) {

        if (imageWidth <= 0 || imageHeight <= 0) {
            throw new IllegalArgumentException("As dimensões da imagem devem ser maiores que zero.");
        }
        if (roomWidth == null || roomHeight == null
                || roomWidth.compareTo(BigDecimal.ZERO) <= 0
                || roomHeight.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("As dimensões da sala devem ser maiores que zero.");
        }

        List<DetectedObject> resultado = new ArrayList<>();
        if (detections == null) {
            return resultado;
        }

        BigDecimal larguraImagem = BigDecimal.valueOf(imageWidth);
        BigDecimal alturaImagem = BigDecimal.valueOf(imageHeight);

        for (YoloDetectionDto detection : detections) {
            if (detection.centerX() == null || detection.centerY() == null || detection.className() == null) {
                continue;
            }

            BigDecimal pixelX = scale(detection.centerX());
            BigDecimal pixelY = scale(detection.centerY());
            BigDecimal roomX = pixelX.multiply(roomWidth).divide(larguraImagem, SCALE, RoundingMode.HALF_UP);
            BigDecimal roomY = pixelY.multiply(roomHeight).divide(alturaImagem, SCALE, RoundingMode.HALF_UP);

            resultado.add(new DetectedObject(
                    detection.className().trim().toLowerCase(),
                    detection.confidence(),
                    pixelX,
                    pixelY,
                    roomX,
                    roomY));
        }

        return resultado;
    }

    private BigDecimal scale(double value) {
        return BigDecimal.valueOf(value).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
