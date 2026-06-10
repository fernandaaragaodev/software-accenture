package com.accenture.officehub_v1.service.ia;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PosicaoCompatibilidadeTest {

    @Test
    void deveAceitarQualquerPosicaoSemPreferencias() {
        assertThat(PosicaoCompatibilidade.compativel("INDIVIDUAL", List.of(), List.of())).isTrue();
    }

    @Test
    void deveCompatibilizarPorTipoDaPosicao() {
        assertThat(PosicaoCompatibilidade.compativel(
                "Estação Padrão",
                List.of(),
                List.of("Estação Padrão"))).isTrue();
    }

    @Test
    void deveCompatibilizarPorEquipamentoVinculado() {
        assertThat(PosicaoCompatibilidade.compativel(
                "INDIVIDUAL",
                List.of("Monitor menor", "Mesa digitalizadora"),
                List.of("Monitor menor"))).isTrue();
    }

    @Test
    void deveAceitarQualquerPreferenciaListada() {
        assertThat(PosicaoCompatibilidade.compativel(
                "INDIVIDUAL",
                List.of("Monitor"),
                List.of("Monitor menor", "Mesa digitalizadora", "Monitor"))).isTrue();
    }

    @Test
    void deveRejeitarQuandoNemTipoNemEquipamentoAtendemPreferencia() {
        assertThat(PosicaoCompatibilidade.compativel(
                "INDIVIDUAL",
                List.of("Teclado mecânico"),
                List.of("Monitor menor"))).isFalse();
    }
}
