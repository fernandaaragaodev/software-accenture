package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.PessoaReservaRequest;
import com.accenture.officehub_v1.entity.Posicao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AlocacaoPosicaoServiceTest {

    private AlocacaoPosicaoService service;

    @BeforeEach
    void setUp() {
        service = new AlocacaoPosicaoService();
    }

    @Test
    void deveAlocarSequencialmentePorTipoPreferido() {
        List<Posicao> livres = List.of(
                posicao("P-01", "Estação Padrão", 1, 1),
                posicao("P-02", "Estação Executiva", 5, 5),
                posicao("P-03", "Estação Padrão", 2, 2));

        PessoaReservaRequest pessoa1 = pessoa("Estação Padrão", null, null);
        PessoaReservaRequest pessoa2 = pessoa("Estação Padrão", null, null);

        var resultado = service.alocar(
                List.of(pessoa1, pessoa2),
                livres,
                CriterioProximidade.PREFERENCIAL,
                BigDecimal.valueOf(10));

        assertThat(resultado.sucesso()).isTrue();
        assertThat(resultado.alocacoes()).hasSize(2);
        assertThat(resultado.alocacoes().get(0).posicao().getIdentificador()).isEqualTo("P-01");
        assertThat(resultado.alocacoes().get(1).posicao().getIdentificador()).isEqualTo("P-03");
    }

    @Test
    void deveRejeitarQuandoNaoHaTipoCompativel() {
        List<Posicao> livres = List.of(posicao("P-01", "Hot Desk", 1, 1));
        PessoaReservaRequest pessoa = pessoa("Estação Executiva", null, null);

        var resultado = service.alocar(
                List.of(pessoa),
                livres,
                CriterioProximidade.PREFERENCIAL,
                BigDecimal.valueOf(5));

        assertThat(resultado.sucesso()).isFalse();
        assertThat(resultado.motivoFalha()).contains("Estação Executiva");
    }

    @Test
    void deveExigirProximidadeObrigatoriaDentroDoRaio() {
        List<Posicao> livres = List.of(
                posicao("P-01", "Estação Padrão", 0, 0),
                posicao("P-02", "Estação Padrão", 10, 0));

        PessoaReservaRequest pessoa1 = pessoa("Estação Padrão", null, null);
        PessoaReservaRequest pessoa2 = pessoa("Estação Padrão", null, null);

        var resultado = service.alocar(
                List.of(pessoa1, pessoa2),
                livres,
                CriterioProximidade.OBRIGATORIA,
                BigDecimal.valueOf(3));

        assertThat(resultado.sucesso()).isFalse();
        assertThat(resultado.motivoFalha()).contains("proximidade obrigatória");
    }

    @Test
    void deveAlocarComProximidadeObrigatoriaQuandoDentroDoRaio() {
        List<Posicao> livres = List.of(
                posicao("P-01", "Estação Padrão", 0, 0),
                posicao("P-02", "Estação Padrão", 2, 0));

        PessoaReservaRequest pessoa1 = pessoa("Estação Padrão", null, null);
        PessoaReservaRequest pessoa2 = pessoa("Estação Padrão", null, null);

        var resultado = service.alocar(
                List.of(pessoa1, pessoa2),
                livres,
                CriterioProximidade.OBRIGATORIA,
                BigDecimal.valueOf(3));

        assertThat(resultado.sucesso()).isTrue();
        assertThat(resultado.alocacoes()).hasSize(2);
    }

    @Test
    void deveInformarAvisoQuandoProximidadePreferencialNaoAtendida() {
        List<Posicao> livres = List.of(
                posicao("P-01", "Estação Padrão", 0, 0),
                posicao("P-02", "Estação Padrão", 10, 0));

        var resultado = service.alocar(
                List.of(pessoa("Estação Padrão", null, null), pessoa("Estação Padrão", null, null)),
                livres,
                CriterioProximidade.PREFERENCIAL,
                BigDecimal.valueOf(3));

        assertThat(resultado.sucesso()).isTrue();
        assertThat(resultado.avisoProximidade()).contains("raio de proximidade preferencial");
    }

    @Test
    void deveCalcularDistanciaEuclidiana() {
        Posicao a = posicao("A", "Tipo", 0, 0);
        Posicao b = posicao("B", "Tipo", 3, 4);

        assertThat(service.distancia(a, b)).isEqualTo(5.0);
    }

    private PessoaReservaRequest pessoa(String tipo1, String tipo2, String tipo3) {
        return new PessoaReservaRequest(null, "Visitante", tipo1, tipo2, tipo3);
    }

    private Posicao posicao(String identificador, String tipo, double x, double y) {
        Posicao posicao = new Posicao();
        posicao.setId(UUID.randomUUID());
        posicao.setIdentificador(identificador);
        posicao.setTipo(tipo);
        posicao.setCoordX(BigDecimal.valueOf(x));
        posicao.setCoordY(BigDecimal.valueOf(y));
        return posicao;
    }
}
