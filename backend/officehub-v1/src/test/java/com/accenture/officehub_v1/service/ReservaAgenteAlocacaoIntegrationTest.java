package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.PessoaReservaRequest;
import com.accenture.officehub_v1.dto.request.SolicitarReservaRequest;
import com.accenture.officehub_v1.entity.Layout;
import com.accenture.officehub_v1.entity.Posicao;
import com.accenture.officehub_v1.entity.Sala;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.entity.enums.StatusAgente;
import com.accenture.officehub_v1.entity.enums.StatusReserva;
import com.accenture.officehub_v1.entity.enums.StatusSala;
import com.accenture.officehub_v1.exception.ConflitoAlocacaoException;
import com.accenture.officehub_v1.repository.AgenteExecucaoRepository;
import com.accenture.officehub_v1.repository.LayoutRepository;
import com.accenture.officehub_v1.repository.PosicaoRepository;
import com.accenture.officehub_v1.repository.ReservaRepository;
import com.accenture.officehub_v1.repository.SalaRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import com.accenture.officehub_v1.security.Roles;
import com.accenture.officehub_v1.service.ia.ConstantesAgenteIa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReservaAgenteAlocacaoIntegrationTest {

    @Autowired
    private ReservaService reservaService;

    @Autowired
    private SalaRepository salaRepository;

    @Autowired
    private LayoutRepository layoutRepository;

    @Autowired
    private PosicaoRepository posicaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private AgenteExecucaoRepository agenteExecucaoRepository;

    @MockitoBean
    private DisponibilidadeService disponibilidadeService;

    @MockitoBean
    private NotificacaoService notificacaoService;

    @MockitoBean
    private AuditService auditService;

    private UUID salaId;
    private UUID solicitanteId;
    private LocalDate dataReserva;

    @BeforeEach
    void setUp() {
        agenteExecucaoRepository.deleteAll();
        reservaRepository.deleteAll();
        posicaoRepository.deleteAll();
        layoutRepository.deleteAll();
        salaRepository.deleteAll();
        usuarioRepository.deleteAll();

        doNothing().when(disponibilidadeService).validarDisponibilidade(any(), any());

        Usuario solicitante = usuarioRepository.save(Usuario.builder()
                .nome("Solicitante Teste")
                .email("solicitante@test.com")
                .senhaHash("hash")
                .ativo(true)
                .build());
        solicitanteId = solicitante.getId();

        Sala sala = salaRepository.save(Sala.builder()
                .nome("Sala IA")
                .capacidadeMaxima(10)
                .raioProximidade(BigDecimal.valueOf(3))
                .status(StatusSala.ATIVA)
                .build());
        salaId = sala.getId();

        Layout layout = layoutRepository.save(Layout.builder()
                .sala(sala)
                .versao("1")
                .ativo(true)
                .build());

        dataReserva = LocalDate.of(2026, 6, 10);
    }

    @Test
    void deveCriarReservaComSucessoELogarExecucaoIa() {
        criarPosicao("P-01", "Estação Padrão", 0, 0);
        criarPosicao("P-02", "Estação Padrão", 2, 0);

        var response = reservaService.solicitarReserva(
                request(CriterioProximidade.OBRIGATORIA, 2,
                        pessoa("Estação Padrão"),
                        pessoa("Estação Padrão")),
                solicitanteId,
                List.of(Roles.USUARIO_FINAL));

        assertThat(response.status()).isEqualTo(StatusReserva.CONFIRMADA);
        assertThat(response.alocacoes()).hasSize(2);
        assertThat(reservaRepository.count()).isEqualTo(1);

        var logs = agenteExecucaoRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getTipoAgente()).isEqualTo(ConstantesAgenteIa.TIPO_ALOCACAO);
        assertThat(logs.get(0).getVersaoModelo()).isEqualTo(ConstantesAgenteIa.VERSAO_ALGORITMO_ESPACIAL_V1);
        assertThat(logs.get(0).getStatus()).isEqualTo(StatusAgente.SUCESSO);
        assertThat(logs.get(0).getPayloadEntrada()).isNotNull();
        assertThat(logs.get(0).getPayloadSaida()).isNotNull();
        assertThat(logs.get(0).getReferenciaId()).isEqualTo(response.id());
    }

    @Test
    void deveFalharPorFaltaDePosicoesLivresSemCriarReserva() {
        criarPosicao("P-01", "Estação Padrão", 0, 0);

        assertThatThrownBy(() -> reservaService.solicitarReserva(
                request(CriterioProximidade.PREFERENCIAL, 2,
                        pessoa("Estação Padrão"),
                        pessoa("Estação Padrão")),
                solicitanteId,
                List.of(Roles.USUARIO_FINAL)))
                .isInstanceOf(ConflitoAlocacaoException.class)
                .hasMessageContaining("posições livres suficientes");

        assertThat(reservaRepository.count()).isZero();
        assertLogFalha();
    }

    @Test
    void deveFalharPorTipoIncompativelSemCriarReserva() {
        criarPosicao("P-01", "Hot Desk", 0, 0);
        criarPosicao("P-02", "Hot Desk", 1, 0);

        assertThatThrownBy(() -> reservaService.solicitarReserva(
                request(CriterioProximidade.PREFERENCIAL, 1, pessoa("Estação Executiva")),
                solicitanteId,
                List.of(Roles.USUARIO_FINAL)))
                .isInstanceOf(ConflitoAlocacaoException.class)
                .hasMessageContaining("Estação Executiva");

        assertThat(reservaRepository.count()).isZero();
        assertLogFalha();
    }

    @Test
    void deveFalharPorProximidadeObrigatoriaSemCriarReserva() {
        criarPosicao("P-01", "Estação Padrão", 0, 0);
        criarPosicao("P-02", "Estação Padrão", 10, 0);

        assertThatThrownBy(() -> reservaService.solicitarReserva(
                request(CriterioProximidade.OBRIGATORIA, 2,
                        pessoa("Estação Padrão"),
                        pessoa("Estação Padrão")),
                solicitanteId,
                List.of(Roles.USUARIO_FINAL)))
                .isInstanceOf(ConflitoAlocacaoException.class)
                .hasMessageContaining("proximidade obrigatória");

        assertThat(reservaRepository.count()).isZero();
        assertLogFalha();
    }

    @Test
    void deveTerSucessoComProximidadePreferencialMesmoForaDoRaio() {
        criarPosicao("P-01", "Estação Padrão", 0, 0);
        criarPosicao("P-02", "Estação Padrão", 10, 0);

        var response = reservaService.solicitarReserva(
                request(CriterioProximidade.PREFERENCIAL, 2,
                        pessoa("Estação Padrão"),
                        pessoa("Estação Padrão")),
                solicitanteId,
                List.of(Roles.USUARIO_FINAL));

        assertThat(response.status()).isEqualTo(StatusReserva.CONFIRMADA);
        assertThat(response.avisoProximidade()).contains("raio de proximidade preferencial");

        var logs = agenteExecucaoRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getStatus()).isEqualTo(StatusAgente.SUCESSO);
    }

    private void assertLogFalha() {
        var logs = agenteExecucaoRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getStatus()).isEqualTo(StatusAgente.FALHA);
        assertThat(logs.get(0).getErroMensagem()).isNotBlank();
        assertThat(logs.get(0).getPayloadEntrada()).isNotNull();
        assertThat(logs.get(0).getPayloadSaida()).isNotNull();
        assertThat(logs.get(0).getReferenciaId()).isNull();
    }

    private void criarPosicao(String identificador, String tipo, double x, double y) {
        Sala sala = salaRepository.findById(salaId).orElseThrow();
        Layout layout = layoutRepository.findBySalaIdAndAtivoTrue(salaId).orElseThrow();

        posicaoRepository.save(Posicao.builder()
                .sala(sala)
                .layout(layout)
                .identificador(identificador)
                .tipo(tipo)
                .coordX(BigDecimal.valueOf(x))
                .coordY(BigDecimal.valueOf(y))
                .build());
    }

    private SolicitarReservaRequest request(String criterio, int qtd, PessoaReservaRequest... pessoas) {
        return new SolicitarReservaRequest(
                salaId,
                null,
                dataReserva,
                LocalTime.of(8, 0),
                LocalTime.of(18, 0),
                qtd,
                criterio,
                List.of(pessoas));
    }

    private PessoaReservaRequest pessoa(String tipoPreferido) {
        return new PessoaReservaRequest(solicitanteId, null, tipoPreferido, null, null);
    }
}
