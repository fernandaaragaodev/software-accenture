package com.accenture.officehub_v1.service.ia.layout;

import java.math.BigDecimal;

public record DetectedObject(
        String className,
        Double confidence,
        BigDecimal pixelX,
        BigDecimal pixelY,
        BigDecimal roomX,
        BigDecimal roomY
) {
}
