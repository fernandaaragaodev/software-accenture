package com.accenture.officehub_v1.service.ia.layout;

import com.accenture.officehub_v1.config.YoloProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StationGroupingService {

    private static final int SCALE = 4;
    private static final Set<String> CLASSES_CADEIRA = Set.of("cadeira", "chair");

    private final YoloProperties yoloProperties;

    public List<WorkstationGroup> agrupar(List<DetectedObject> objetos) {
        List<DetectedObject> cadeiras = filtrar(objetos, CLASSES_CADEIRA);
        List<DetectedObject> equipamentos = objetos.stream()
                .filter(o -> !CLASSES_CADEIRA.contains(o.className()))
                .toList();

        Map<DetectedObject, List<DetectedObject>> membrosPorCadeira = new LinkedHashMap<>();
        for (DetectedObject cadeira : cadeiras) {
            List<DetectedObject> membros = new ArrayList<>();
            membros.add(cadeira);
            membrosPorCadeira.put(cadeira, membros);
        }

        for (DetectedObject equipamento : equipamentos) {
            DetectedObject cadeira = encontrarCadeiraMaisProxima(cadeiras, equipamento);
            if (cadeira == null) {
                continue;
            }
            membrosPorCadeira.get(cadeira).add(equipamento);
        }

        List<WorkstationGroup> grupos = new ArrayList<>();
        for (DetectedObject cadeira : cadeiras) {
            grupos.add(criarGrupoCentradoNaCadeira(membrosPorCadeira.get(cadeira), cadeira));
        }

        return grupos;
    }

    private List<DetectedObject> filtrar(List<DetectedObject> objetos, Set<String> classes) {
        return objetos.stream()
                .filter(o -> classes.contains(o.className()))
                .toList();
    }

    private DetectedObject encontrarCadeiraMaisProxima(
            List<DetectedObject> cadeiras,
            DetectedObject equipamento) {

        return cadeiras.stream()
                .filter(c -> distancia(c, equipamento).compareTo(yoloProperties.distanciaAgrupamentoMetros()) <= 0)
                .min(Comparator.comparing(c -> distancia(c, equipamento)))
                .orElse(null);
    }

    private WorkstationGroup criarGrupoCentradoNaCadeira(List<DetectedObject> membros, DetectedObject cadeira) {
        return new WorkstationGroup(
                cadeira.roomX(),
                cadeira.roomY(),
                cadeira.pixelX(),
                cadeira.pixelY(),
                List.copyOf(membros));
    }

    private BigDecimal distancia(DetectedObject a, DetectedObject b) {
        BigDecimal dx = a.roomX().subtract(b.roomX());
        BigDecimal dy = a.roomY().subtract(b.roomY());
        double distancia = Math.sqrt(dx.pow(2).add(dy.pow(2)).doubleValue());
        return BigDecimal.valueOf(distancia).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
