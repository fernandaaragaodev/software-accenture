package com.accenture.officehub_v1.service.ia.layout;

import com.accenture.officehub_v1.config.YoloProperties;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpatialValidationService {

    private static final int SCALE = 4;
    private static final BigDecimal TOLERANCIA_COORDENADA = new BigDecimal("0.10");

    private final YoloProperties yoloProperties;

    public void validarEstacoes(
            List<WorkstationGroup> estacoes,
            BigDecimal larguraSala,
            BigDecimal alturaSala,
            int capacidadeMaxima) {

        if (estacoes.isEmpty()) {
            throw new RegraNegocioException(
                    "Nenhuma estação de trabalho foi identificada na planta baixa (cadeira + monitor próximos).");
        }

        if (estacoes.size() > capacidadeMaxima) {
            throw new RegraNegocioException(
                    "A planta identificou " + estacoes.size()
                            + " estações, acima da capacidade máxima da sala (" + capacidadeMaxima + ").");
        }

        BigDecimal margem = yoloProperties.margemPerimetralMetros();
        BigDecimal minX = margem;
        BigDecimal maxX = larguraSala.subtract(margem);
        BigDecimal minY = margem;
        BigDecimal maxY = alturaSala.subtract(margem);

        List<WorkstationGroup> aprovadas = new ArrayList<>();

        for (int i = 0; i < estacoes.size(); i++) {
            WorkstationGroup estacao = estacoes.get(i);

            if (estacao.centerRoomX().compareTo(minX) < 0
                    || estacao.centerRoomX().compareTo(maxX) > 0
                    || estacao.centerRoomY().compareTo(minY) < 0
                    || estacao.centerRoomY().compareTo(maxY) > 0) {
                throw new RegraNegocioException(
                        "A estação P" + String.format("%02d", i + 1)
                                + " está fora dos limites físicos da sala.");
            }

            for (WorkstationGroup existente : aprovadas) {
                if (mesmaCoordenada(estacao, existente)) {
                    throw new RegraNegocioException(
                            "Duas estações foram alocadas na mesma coordenada física.");
                }

                if (distancia(estacao, existente).compareTo(yoloProperties.distanciaMinimaEstacoesMetros()) < 0) {
                    throw new RegraNegocioException(
                            "As estações identificadas não respeitam a distância mínima exigida.");
                }
            }

            aprovadas.add(estacao);
        }
    }

    private boolean mesmaCoordenada(WorkstationGroup a, WorkstationGroup b) {
        return a.centerRoomX().subtract(b.centerRoomX()).abs().compareTo(TOLERANCIA_COORDENADA) <= 0
                && a.centerRoomY().subtract(b.centerRoomY()).abs().compareTo(TOLERANCIA_COORDENADA) <= 0;
    }

    private BigDecimal distancia(WorkstationGroup a, WorkstationGroup b) {
        BigDecimal dx = a.centerRoomX().subtract(b.centerRoomX());
        BigDecimal dy = a.centerRoomY().subtract(b.centerRoomY());
        double distancia = Math.sqrt(dx.pow(2).add(dy.pow(2)).doubleValue());
        return BigDecimal.valueOf(distancia).setScale(SCALE, RoundingMode.HALF_UP);
    }
}
