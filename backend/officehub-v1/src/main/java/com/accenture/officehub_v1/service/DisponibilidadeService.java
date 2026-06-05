package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.CriarRegraDisponibilidadeRequest;
import com.accenture.officehub_v1.dto.request.ExcecaoDisponibilidadeRequest;
import com.accenture.officehub_v1.dto.request.HorarioDisponibilidadeRequest;
import com.accenture.officehub_v1.dto.response.ConsultaDisponibilidadeResponse;
import com.accenture.officehub_v1.dto.response.PosicaoLayoutDisponibilidadeResponse;
import com.accenture.officehub_v1.dto.response.PosicaoOcupadaResponse;
import com.accenture.officehub_v1.dto.response.RegraDisponibilidadeResponse;
import com.accenture.officehub_v1.dto.response.ValidacaoDisponibilidadeResponse;
import com.accenture.officehub_v1.entity.ExcecaoDisponibilidade;
import com.accenture.officehub_v1.entity.Posicao;
import com.accenture.officehub_v1.entity.HorarioDisponibilidade;
import com.accenture.officehub_v1.entity.RegraDisponibilidade;
import com.accenture.officehub_v1.entity.Sala;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.entity.enums.StatusReserva;
import com.accenture.officehub_v1.entity.enums.StatusSala;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.ExcecaoDisponibilidadeRepository;
import com.accenture.officehub_v1.repository.HorarioDisponibilidadeRepository;
import com.accenture.officehub_v1.repository.RegraDisponibilidadeRepository;
import com.accenture.officehub_v1.repository.ReservaPosicaoRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import com.accenture.officehub_v1.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisponibilidadeService {

    private static final Set<StatusReserva> STATUS_OCUPAM_POSICAO =
            EnumSet.of(StatusReserva.PENDENTE, StatusReserva.CONFIRMADA);

    private final RegraDisponibilidadeRepository regraDisponibilidadeRepository;
    private final HorarioDisponibilidadeRepository horarioDisponibilidadeRepository;
    private final ExcecaoDisponibilidadeRepository excecaoDisponibilidadeRepository;
    private final SalaService salaService;
    private final PosicaoService posicaoService;
    private final ReservaPosicaoRepository reservaPosicaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditService auditService;

    @Transactional
    public RegraDisponibilidadeResponse criarRegra(UUID salaId, CriarRegraDisponibilidadeRequest request) {
        Sala sala = salaService.buscarEntidadeAtiva(salaId);

        if (regraDisponibilidadeRepository.existsBySalaId(salaId)) {
            throw new RegraNegocioException(
                    "Esta sala já possui regra de disponibilidade cadastrada.");
        }

        validarHorarios(request.horarios());

        RegraDisponibilidade regra = RegraDisponibilidade.builder()
                .sala(sala)
                .antecedenciaMinimaDias(request.antecedenciaMinimaDias())
                .build();

        regra = regraDisponibilidadeRepository.save(regra);

        RegraDisponibilidade regraFinal = regra;
        List<HorarioDisponibilidade> horarios = request.horarios().stream()
                .map(h -> HorarioDisponibilidade.builder()
                        .regraDisponibilidade(regraFinal)
                        .diaSemana(h.diaSemana())
                        .horaAbertura(h.horaAbertura())
                        .horaFechamento(h.horaFechamento())
                        .build())
                .map(horarioDisponibilidadeRepository::save)
                .toList();

        auditService.registrar(SecurityUtils.getUsuarioIdAtual(), "CRIAR", "RegraDisponibilidade", regra.getId());
        return RegraDisponibilidadeResponse.from(regra, horarios);
    }

    @Transactional
    public void adicionarExcecao(UUID salaId, ExcecaoDisponibilidadeRequest request, UUID createdById) {
        Sala sala = salaService.buscarEntidadeAtiva(salaId);

        if (excecaoDisponibilidadeRepository.existsBySalaIdAndData(salaId, request.data())) {
            throw new RegraNegocioException(
                    "Já existe uma exceção de disponibilidade cadastrada para esta data.");
        }

        Usuario createdBy = usuarioRepository.findByIdAndDeletedAtIsNull(createdById)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário responsável pelo cadastro não encontrado."));

        ExcecaoDisponibilidade excecao = ExcecaoDisponibilidade.builder()
                .sala(sala)
                .data(request.data())
                .motivo(request.motivo())
                .createdBy(createdBy)
                .build();

        ExcecaoDisponibilidade excecaoSalva = excecaoDisponibilidadeRepository.save(excecao);
        auditService.registrar(createdById, "ALTERAR_DISPONIBILIDADE", "ExcecaoDisponibilidade", excecaoSalva.getId());
    }

    public ValidacaoDisponibilidadeResponse validarReservaPermitida(UUID salaId, LocalDate data) {
        ConsultaDisponibilidadeResponse consulta = consultarDisponibilidade(salaId, data);
        return new ValidacaoDisponibilidadeResponse(
                consulta.salaId(),
                consulta.data(),
                consulta.disponivelParaReserva(),
                consulta.mensagemRegras());
    }

    /**
     * RF-35 / fluxo 7.4 — calendário de disponibilidade com layout e ocupação por data.
     */
    public ConsultaDisponibilidadeResponse consultarDisponibilidade(UUID salaId, LocalDate data) {
        Sala sala = salaService.buscarEntidadeAtiva(salaId);
        ValidacaoDisponibilidadeResponse regras = validarRegrasDisponibilidade(salaId, data);

        List<Posicao> todasPosicoes = posicaoService.listarPosicoesDaSala(salaId);
        List<UUID> posicoesOcupadasIds = reservaPosicaoRepository.findPosicaoIdsOcupadas(
                salaId, data, STATUS_OCUPAM_POSICAO);

        int totalInativas = 0;
        int totalLivres = 0;
        int totalOcupadas = 0;

        List<PosicaoLayoutDisponibilidadeResponse> layout = new java.util.ArrayList<>();
        List<PosicaoOcupadaResponse> ocupadas = new java.util.ArrayList<>();

        for (Posicao posicao : todasPosicoes) {
            String situacao = resolverSituacao(posicao, posicoesOcupadasIds);
            layout.add(PosicaoLayoutDisponibilidadeResponse.from(posicao, situacao));

            switch (situacao) {
                case PosicaoStatus.INATIVA -> totalInativas++;
                case PosicaoStatus.OCUPADA -> {
                    totalOcupadas++;
                    ocupadas.add(PosicaoOcupadaResponse.from(posicao));
                }
                case PosicaoStatus.LIVRE -> totalLivres++;
                default -> {
                }
            }
        }

        Map<String, Long> livresPorTipo = todasPosicoes.stream()
                .filter(p -> PosicaoStatus.LIVRE.equals(resolverSituacao(p, posicoesOcupadasIds)))
                .collect(Collectors.groupingBy(
                        p -> p.getTipo() != null && !p.getTipo().isBlank() ? p.getTipo() : "SEM_TIPO",
                        Collectors.counting()));

        boolean salaAtiva = sala.getStatus() == StatusSala.ATIVA;
        boolean disponivel = salaAtiva && regras.disponivel();

        String mensagem = montarMensagemConsulta(sala, regras);

        return new ConsultaDisponibilidadeResponse(
                salaId,
                data,
                sala.getStatus(),
                disponivel,
                mensagem,
                todasPosicoes.size(),
                totalLivres,
                totalOcupadas,
                totalInativas,
                livresPorTipo,
                ocupadas,
                layout);
    }

    private ValidacaoDisponibilidadeResponse validarRegrasDisponibilidade(UUID salaId, LocalDate data) {
        try {
            validarDisponibilidade(salaId, data);
            return new ValidacaoDisponibilidadeResponse(salaId, data, true,
                    "A sala está disponível para reserva na data informada.");
        } catch (RegraNegocioException ex) {
            return new ValidacaoDisponibilidadeResponse(salaId, data, false, ex.getMessage());
        }
    }

    private String montarMensagemConsulta(Sala sala, ValidacaoDisponibilidadeResponse regras) {
        if (sala.getStatus() != StatusSala.ATIVA) {
            return "A sala não está ativa para reservas.";
        }
        return regras.mensagem();
    }

    private String resolverSituacao(Posicao posicao, List<UUID> posicoesOcupadasIds) {
        if (posicao.getDeletedAt() != null
                || PosicaoStatus.INATIVA.equalsIgnoreCase(posicao.getStatus())) {
            return PosicaoStatus.INATIVA;
        }

        if (posicoesOcupadasIds.contains(posicao.getId())) {
            return PosicaoStatus.OCUPADA;
        }

        return PosicaoStatus.LIVRE;
    }

    public void validarDisponibilidade(UUID salaId, LocalDate data) {
        RegraDisponibilidade regra = regraDisponibilidadeRepository.findBySalaId(salaId)
                .orElseThrow(() -> new RegraNegocioException(
                        "A sala não possui regra de disponibilidade configurada."));

        verificarExcecao(salaId, data);
        verificarAntecedenciaMinima(regra, data);
        verificarDiaSemanaPermitido(regra, data);
    }

    public void verificarAntecedenciaMinima(RegraDisponibilidade regra, LocalDate data) {
        LocalDate dataMinima = LocalDate.now().plusDays(regra.getAntecedenciaMinimaDias());
        if (data.isBefore(dataMinima)) {
            throw new RegraNegocioException(String.format(
                    "A reserva deve ser feita com antecedência mínima de %d dia(s). " +
                            "A data mais próxima permitida é %s.",
                    regra.getAntecedenciaMinimaDias(),
                    dataMinima));
        }
    }

    public void verificarExcecao(UUID salaId, LocalDate data) {
        excecaoDisponibilidadeRepository.findBySalaIdAndData(salaId, data).ifPresent(excecao -> {
            String motivo = excecao.getMotivo() != null && !excecao.getMotivo().isBlank()
                    ? excecao.getMotivo()
                    : "data bloqueada ou feriado";
            throw new RegraNegocioException(
                    "A sala não está disponível para reserva em " + data + ": " + motivo + ".");
        });
    }

    public void verificarDiaSemanaPermitido(RegraDisponibilidade regra, LocalDate data) {
        int diaSemana = converterDiaSemana(data);
        boolean permitido = horarioDisponibilidadeRepository
                .existsByRegraDisponibilidadeIdAndDiaSemana(regra.getId(), diaSemana);

        if (!permitido) {
            throw new RegraNegocioException(
                    "A sala não aceita reservas no dia da semana informado.");
        }
    }

    public RegraDisponibilidadeResponse buscarRegraPorSala(UUID salaId) {
        salaService.buscarEntidadeAtiva(salaId);

        RegraDisponibilidade regra = regraDisponibilidadeRepository.findBySalaId(salaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Regra de disponibilidade não encontrada para esta sala."));

        List<HorarioDisponibilidade> horarios =
                horarioDisponibilidadeRepository.findByRegraDisponibilidadeId(regra.getId());

        return RegraDisponibilidadeResponse.from(regra, horarios);
    }

    /**
     * Converte LocalDate para dia da semana no formato 0=segunda ... 6=domingo.
     */
    int converterDiaSemana(LocalDate data) {
        return data.getDayOfWeek().getValue() - 1;
    }

    private void validarHorarios(List<HorarioDisponibilidadeRequest> horarios) {
        Set<Integer> dias = new HashSet<>();
        for (HorarioDisponibilidadeRequest horario : horarios) {
            if (!horario.horaAbertura().isBefore(horario.horaFechamento())) {
                throw new RegraNegocioException(
                        "A hora de abertura deve ser anterior à hora de fechamento.");
            }
            if (!dias.add(horario.diaSemana())) {
                throw new RegraNegocioException(
                        "Não é permitido cadastrar mais de um horário para o mesmo dia da semana.");
            }
        }
    }
}
