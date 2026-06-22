package com.accenture.officehub_v1.service.ia.layout;

import com.accenture.officehub_v1.config.YoloProperties;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class StationGroupingServiceTest {

    private final StationGroupingService service = new StationGroupingService(
            new YoloProperties(null, null, null, new BigDecimal("2.0"), null, null));

    @Test
    void deveCriarUmaPosicaoPorCadeiraComEquipamentosProximos() {
        DetectedObject cadeira = objeto("cadeira", "1.0", "1.0");
        DetectedObject monitor = objeto("monitor", "1.5", "1.2");
        DetectedObject notebook = objeto("notebook", "1.6", "1.3");

        List<WorkstationGroup> grupos = service.agrupar(List.of(cadeira, monitor, notebook));

        assertThat(grupos).hasSize(1);
        assertThat(grupos.getFirst().objetos()).hasSize(3);
        assertThat(grupos.getFirst().centerRoomX()).isEqualByComparingTo(new BigDecimal("1.0"));
        assertThat(grupos.getFirst().centerRoomY()).isEqualByComparingTo(new BigDecimal("1.0"));
    }

    @Test
    void deveCriarPosicaoParaCadeiraSemMonitor() {
        DetectedObject cadeira = objeto("cadeira", "1.0", "1.0");
        DetectedObject monitor = objeto("monitor", "5.0", "5.0");

        List<WorkstationGroup> grupos = service.agrupar(List.of(cadeira, monitor));

        assertThat(grupos).hasSize(1);
        assertThat(grupos.getFirst().objetos()).containsExactly(cadeira);
    }

    @Test
    void deveCriarUmaPosicaoParaCadaCadeiraDetectada() {
        List<DetectedObject> objetos = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            objetos.add(objeto("cadeira", String.valueOf(i), "1.0"));
        }

        List<WorkstationGroup> grupos = service.agrupar(objetos);

        assertThat(grupos).hasSize(30);
    }

    @Test
    void equipamentosNaoDevemCriarPosicoes() {
        List<DetectedObject> objetos = List.of(
                objeto("monitor", "1.0", "1.0"),
                objeto("notebook", "2.0", "2.0"),
                objeto("impressora", "3.0", "3.0"),
                objeto("projetor", "4.0", "4.0"));

        List<WorkstationGroup> grupos = service.agrupar(objetos);

        assertThat(grupos).isEmpty();
    }

    @Test
    void deveAssociarEquipamentosACadeiraMaisProxima() {
        DetectedObject cadeira1 = objeto("cadeira", "1.0", "1.0");
        DetectedObject cadeira2 = objeto("cadeira", "5.0", "1.0");
        DetectedObject monitor = objeto("monitor", "1.2", "1.1");
        DetectedObject notebook = objeto("notebook", "1.3", "1.2");
        DetectedObject impressora = objeto("impressora", "5.1", "1.1");

        List<WorkstationGroup> grupos = service.agrupar(List.of(
                cadeira1, cadeira2, monitor, notebook, impressora));

        assertThat(grupos).hasSize(2);
        assertThat(grupos.get(0).objetos()).containsExactly(cadeira1, monitor, notebook);
        assertThat(grupos.get(1).objetos()).containsExactly(cadeira2, impressora);
    }

    @Test
    void deveGerarTrintaPosicoesParaPlantaComEquipamentosVariados() {
        List<DetectedObject> objetos = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            objetos.add(objeto("cadeira", String.valueOf(i * 3), "1.0"));
        }
        for (int i = 0; i < 18; i++) {
            objetos.add(objeto("monitor", String.valueOf(i * 3 + 0.2), "1.1"));
        }
        for (int i = 0; i < 4; i++) {
            objetos.add(objeto("notebook", String.valueOf(i * 3 + 0.3), "1.2"));
        }
        objetos.add(objeto("impressora", "90.0", "1.0"));
        objetos.add(objeto("projetor", "93.0", "1.0"));

        List<WorkstationGroup> grupos = service.agrupar(objetos);

        assertThat(grupos).hasSize(30);
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
