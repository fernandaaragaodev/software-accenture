package com.accenture.officehub_v1.service.ia.layout;

import java.math.BigDecimal;
import java.util.List;

public record WorkstationGroup(
        BigDecimal centerRoomX,
        BigDecimal centerRoomY,
        BigDecimal centerPixelX,
        BigDecimal centerPixelY,
        List<DetectedObject> objetos
) {
}
