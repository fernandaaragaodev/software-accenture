package com.accenture.officehub_v1.service.ia.layout;

import com.accenture.officehub_v1.config.YoloProperties;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpatialValidationServiceTest {

    private final SpatialValidationService service = new SpatialValidationService(
            new YoloProperties(null, null, null, null, new BigDecimal("1.0"), new BigDecimal("0.5")));

    @Test
    void deveAprovarEstacoesValidas() {
        List<WorkstationGroup> estacoes = List.of(
                grupo("2.0", "2.0"),
                grupo("4.0", "2.0"));

        assertThatCode(() -> service.validarEstacoes(
                estacoes,
                new BigDecimal("12.0"),
                new BigDecimal("8.0"),
                10)).doesNotThrowAnyException();
    }

    @Test
    void deveRejeitarEstacoesMuitoProximas() {
        List<WorkstationGroup> estacoes = List.of(
                grupo("2.0", "2.0"),
                grupo("2.5", "2.0"));

        assertThatThrownBy(() -> service.validarEstacoes(
                estacoes,
                new BigDecimal("12.0"),
                new BigDecimal("8.0"),
                10))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("distância mínima");
    }

    private WorkstationGroup grupo(String roomX, String roomY) {
        return new WorkstationGroup(
                new BigDecimal(roomX),
                new BigDecimal(roomY),
                new BigDecimal(roomX),
                new BigDecimal(roomY),
                List.of());
    }
}
