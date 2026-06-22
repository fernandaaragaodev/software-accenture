package com.accenture.officehub_v1.service.ia.layout;

import com.accenture.officehub_v1.config.YoloProperties;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StationGroupingServiceTest {

    private final StationGroupingService service = new StationGroupingService(
            new YoloProperties(null, null, null, new BigDecimal("2.0"), null, null));

    @Test
    void deveAgruparCadeiraProximaDeMonitor() {
        DetectedObject cadeira = objeto("cadeira", "1.0", "1.0");
        DetectedObject monitor = objeto("monitor", "1.5", "1.2");
        DetectedObject notebook = objeto("notebook", "1.6", "1.3");

        List<WorkstationGroup> grupos = service.agrupar(List.of(cadeira, monitor, notebook));

        assertThat(grupos).hasSize(1);
        assertThat(grupos.getFirst().objetos()).hasSize(3);
    }

    @Test
    void naoDeveAgruparQuandoDistanciaExcedeLimite() {
        DetectedObject cadeira = objeto("cadeira", "1.0", "1.0");
        DetectedObject monitor = objeto("monitor", "5.0", "5.0");

        List<WorkstationGroup> grupos = service.agrupar(List.of(cadeira, monitor));

        assertThat(grupos).isEmpty();
    }

    private DetectedObject objeto(String classe, String roomX, String roomY) {
        return new DetectedObject(
                classe,
                0.95,
                new BigDecimal(roomX),
                new BigDecimal(roomY),
                new BigDecimal(roomX),
                new BigDecimal(roomY));
    }
}
