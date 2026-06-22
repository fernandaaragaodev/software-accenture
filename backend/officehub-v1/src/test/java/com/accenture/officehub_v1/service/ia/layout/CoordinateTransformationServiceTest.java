package com.accenture.officehub_v1.service.ia.layout;

import com.accenture.officehub_v1.dto.ia.yolo.YoloDetectionDto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoordinateTransformationServiceTest {

    private final CoordinateTransformationService service = new CoordinateTransformationService();

    @Test
    void deveConverterPixelsParaCoordenadasReais() {
        List<YoloDetectionDto> deteccoes = List.of(
                new YoloDetectionDto("monitor", 500.0, 400.0, 0.9, List.of(480.0, 380.0, 520.0, 420.0)));

        List<DetectedObject> objetos = service.transform(
                deteccoes,
                1000,
                800,
                new BigDecimal("10.0"),
                new BigDecimal("8.0"));

        assertThat(objetos).hasSize(1);
        assertThat(objetos.getFirst().pixelX()).isEqualByComparingTo("500.0000");
        assertThat(objetos.getFirst().pixelY()).isEqualByComparingTo("400.0000");
        assertThat(objetos.getFirst().roomX()).isEqualByComparingTo("5.0000");
        assertThat(objetos.getFirst().roomY()).isEqualByComparingTo("4.0000");
    }
}
