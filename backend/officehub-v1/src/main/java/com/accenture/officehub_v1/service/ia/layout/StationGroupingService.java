package com.accenture.officehub_v1.service.ia.layout;

import com.accenture.officehub_v1.config.YoloProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StationGroupingService {

    private static final int SCALE = 4;
    private static final Set<String> CLASSES_CADEIRA = Set.of("cadeira", "chair");
    private static final Set<String> CLASSES_MONITOR = Set.of("monitor");

    private final YoloProperties yoloProperties;

    public List<WorkstationGroup> agrupar(List<DetectedObject> objetos) {
        List<DetectedObject> cadeiras = filtrar(objetos, CLASSES_CADEIRA);
        List<DetectedObject> monitores = filtrar(objetos, CLASSES_MONITOR);
        List<DetectedObject> demais = objetos.stream()
                .filter(o -> !CLASSES_CADEIRA.contains(o.className()) && !CLASSES_MONITOR.contains(o.className()))
                .toList();

        Set<DetectedObject> usados = new HashSet<>();
        List<WorkstationGroup> grupos = new ArrayList<>();

        for (DetectedObject monitor : monitores) {
            DetectedObject cadeira = encontrarMaisProxima(cadeiras, monitor, usados);
            if (cadeira == null) {
                continue;
            }

            List<DetectedObject> membros = new ArrayList<>();
            membros.add(cadeira);
            membros.add(monitor);
            usados.add(cadeira);
            usados.add(monitor);

            for (DetectedObject extra : demais) {
                if (usados.contains(extra)) {
                    continue;
                }
                if (distancia(monitor, extra).compareTo(yoloProperties.distanciaAgrupamentoMetros()) <= 0) {
                    membros.add(extra);
                    usados.add(extra);
                }
            }

            grupos.add(criarGrupo(membros));
        }

        return grupos;
    }

    private List<DetectedObject> filtrar(List<DetectedObject> objetos, Set<String> classes) {
        return objetos.stream()
                .filter(o -> classes.contains(o.className()))
                .toList();
    }

    private DetectedObject encontrarMaisProxima(
            List<DetectedObject> candidatos,
            DetectedObject referencia,
            Set<DetectedObject> usados) {

        return candidatos.stream()
                .filter(c -> !usados.contains(c))
                .filter(c -> distancia(c, referencia).compareTo(yoloProperties.distanciaAgrupamentoMetros()) <= 0)
                .min(Comparator.comparing(c -> distancia(c, referencia)))
                .orElse(null);
    }

    private WorkstationGroup criarGrupo(List<DetectedObject> membros) {
        BigDecimal somaRoomX = BigDecimal.ZERO;
        BigDecimal somaRoomY = BigDecimal.ZERO;
        BigDecimal somaPixelX = BigDecimal.ZERO;
        BigDecimal somaPixelY = BigDecimal.ZERO;

        for (DetectedObject membro : membros) {
            somaRoomX = somaRoomX.add(membro.roomX());
            somaRoomY = somaRoomY.add(membro.roomY());
            somaPixelX = somaPixelX.add(membro.pixelX());
            somaPixelY = somaPixelY.add(membro.pixelY());
        }

        BigDecimal divisor = BigDecimal.valueOf(membros.size());
        return new WorkstationGroup(
                somaRoomX.divide(divisor, SCALE, RoundingMode.HALF_UP),
                somaRoomY.divide(divisor, SCALE, RoundingMode.HALF_UP),
                somaPixelX.divide(divisor, SCALE, RoundingMode.HALF_UP),
                somaPixelY.divide(divisor, SCALE, RoundingMode.HALF_UP),
                List.copyOf(membros));
    }

    private BigDecimal distancia(DetectedObject a, DetectedObject b) {
        BigDecimal dx = a.roomX().subtract(b.roomX());
        BigDecimal dy = a.roomY().subtract(b.roomY());
        double distancia = Math.sqrt(dx.pow(2).add(dy.pow(2)).doubleValue());
        return BigDecimal.valueOf(distancia).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
