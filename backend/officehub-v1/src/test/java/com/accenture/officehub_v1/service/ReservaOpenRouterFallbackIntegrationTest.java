package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteEntradaDto;
import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteSaidaDto;
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
import com.accenture.officehub_v1.service.ia.motor.MotorAlocacaoOpenRouter;
import com.accenture.officehub_v1.service.ia.motor.OpenRouterIndisponivelException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "ia.motor=OPENROUTER")
@Transactional
class ReservaOpenRouterFallbackIntegrationTest {

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

    @MockitoBean
    private MotorAlocacaoOpenRouter motorAlocacaoOpenRouter;

    private UUID salaId;
    private UUID solicitanteId;
    private UUID participante2Id;
    private LocalDate dataReserva;

    @BeforeEach
    void setUp() {
        agenteExecucaoRepository.deleteAll();
        reservaRepository.deleteAll();
        posicaoRepository.deleteAll();
        layoutRepository.deleteAll();
        salaRepository.deleteAll();
        usuarioRepository.deleteAll();

        doNothing().when(disponibilidadeService).validarDisponibilidade(any(), any(), any(), any());

        Usuario solicitante = usuarioRepository.save(Usuario.builder()
                .nome("Solicitante Teste")
                .email("fallback@test.com")
                .senhaHash("hash")
                .ativo(true)
                .build());
        solicitanteId = solicitante.getId();

        Usuario participante2 = usuarioRepository.save(Usuario.builder()
                .nome("Participante 2")
                .email("participante2-fallback@test.com")
                .senhaHash("hash")
                .ativo(true)
                .build());
        participante2Id = participante2.getId();

        Sala sala = salaRepository.save(Sala.builder()
                .nome("Sala Fallback")
                .capacidadeMaxima(10)
                .raioProximidade(BigDecimal.valueOf(5))
                .status(StatusSala.ATIVA)
                .build());
        salaId = sala.getId();

        layoutRepository.save(Layout.builder()
                .sala(sala)
                .versao("1")
                .ativo(true)
                .aprovadoPor(solicitante)
                .aprovadoEm(OffsetDateTime.now())
                .build());

        dataReserva = LocalDate.of(2026, 6, 10);
    }

    @Test
    void deveExecutarFallbackQuandoOpenRouterFalha() {
        criarPosicao("P-01", "Estação Padrão", 0, 0);
        criarPosicao("P-02", "Estação Padrão", 2, 0);

        when(motorAlocacaoOpenRouter.executar(any(AlocacaoAgenteEntradaDto.class)))
                .thenThrow(new OpenRouterIndisponivelException("OpenRouter indisponível"));

        var response = reservaService.solicitarReserva(
                request(CriterioProximidade.OBRIGATORIA, 2,
                        pessoa(solicitanteId, "Estação Padrão"),
                        pessoa(participante2Id, "Estação Padrão")),
                solicitanteId,
                List.of(Roles.INTEGRADOR));

        assertThat(response.status()).isEqualTo(StatusReserva.CONFIRMADA);
        assertThat(response.alocacoes()).hasSize(2);
        assertThat(reservaRepository.count()).isEqualTo(1);

        var logs = agenteExecucaoRepository.findAll();
        assertThat(logs).hasSize(2);
        assertThat(logs.stream().map(l -> l.getVersaoModelo()).toList())
                .contains(
                        ConstantesAgenteIa.VERSAO_OPENROUTER_GEMINI_FLASH_V1,
                        ConstantesAgenteIa.VERSAO_ALGORITMO_ESPACIAL_V2);
        assertThat(logs.stream().filter(l -> l.getStatus() == StatusAgente.SUCESSO).count()).isEqualTo(1);
        assertThat(logs.stream().filter(l -> l.getStatus() == StatusAgente.FALHA).count()).isEqualTo(1);
    }

    @Test
    void deveRetornar409QuandoFallbackTambemFalha() {
        criarPosicao("P-01", "Estação Padrão", 0, 0);

        when(motorAlocacaoOpenRouter.executar(any(AlocacaoAgenteEntradaDto.class)))
                .thenThrow(new OpenRouterIndisponivelException("OpenRouter indisponível"));

        assertThatThrownBy(() -> reservaService.solicitarReserva(
                request(CriterioProximidade.PREFERENCIAL, 2,
                        pessoa(solicitanteId, "Estação Padrão"),
                        pessoa(participante2Id, "Estação Padrão")),
                solicitanteId,
                List.of(Roles.INTEGRADOR)))
                .isInstanceOf(ConflitoAlocacaoException.class);

        assertThat(reservaRepository.count()).isZero();
    }

    @Test
    void deveUsarRespostaOpenRouterQuandoDisponivel() {
        UUID posicaoId = criarPosicao("P-01", "Estação Padrão", 0, 0).getId();

        when(motorAlocacaoOpenRouter.executar(any(AlocacaoAgenteEntradaDto.class)))
                .thenReturn(AlocacaoAgenteSaidaDto.sucesso(
                        300,
                        List.of(new com.accenture.officehub_v1.dto.ia.PosicaoAlocadaSaidaDto(
                                solicitanteId, posicaoId))));

        var response = reservaService.solicitarReserva(
                request(CriterioProximidade.PREFERENCIAL, 1, pessoa("Estação Padrão")),
                solicitanteId,
                List.of(Roles.USUARIO_FINAL));

        assertThat(response.status()).isEqualTo(StatusReserva.CONFIRMADA);
        assertThat(agenteExecucaoRepository.findAll()).hasSize(1);
        assertThat(agenteExecucaoRepository.findAll().get(0).getVersaoModelo())
                .isEqualTo(ConstantesAgenteIa.VERSAO_OPENROUTER_GEMINI_FLASH_V1);
    }

    private Posicao criarPosicao(String identificador, String tipo, double x, double y) {
        Sala sala = salaRepository.findById(salaId).orElseThrow();
        Layout layout = layoutRepository.findBySalaIdAndAtivoTrue(salaId).orElseThrow();

        return posicaoRepository.save(Posicao.builder()
                .sala(sala)
                .layout(layout)
                .identificador(identificador)
                .tipo(tipo)
                .coordX(BigDecimal.valueOf(x))
                .coordY(BigDecimal.valueOf(y))
                .status(PosicaoStatus.ATIVA)
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

    private PessoaReservaRequest pessoa(UUID usuarioId, String tipoPreferido) {
        return new PessoaReservaRequest(usuarioId, null, tipoPreferido, null, null);
    }
}
