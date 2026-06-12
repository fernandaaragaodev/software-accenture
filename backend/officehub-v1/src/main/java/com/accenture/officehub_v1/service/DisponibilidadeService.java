package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.AtribuirRegraSalaRequest;
import com.accenture.officehub_v1.dto.request.CriarRegraDisponibilidadeIndependenteRequest;
import com.accenture.officehub_v1.dto.request.CriarRegraDisponibilidadeRequest;
import com.accenture.officehub_v1.dto.request.ExcecaoDisponibilidadeRequest;
import com.accenture.officehub_v1.dto.request.HorarioDisponibilidadeRequest;
import com.accenture.officehub_v1.dto.response.ConsultaDisponibilidadeResponse;
import com.accenture.officehub_v1.dto.response.PosicaoDisponibilidadeItemResponse;
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
import com.accenture.officehub_v1.repository.PosicaoEquipamentoRepository;
import com.accenture.officehub_v1.repository.RegraDisponibilidadeRepository;
import com.accenture.officehub_v1.repository.ReservaPosicaoRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import com.accenture.officehub_v1.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    private final PosicaoEquipamentoRepository posicaoEquipamentoRepository;
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

        String nome = request.nome() != null && !request.nome().isBlank()
                ? request.nome().trim()
                : "Regra — " + sala.getNome();

        RegraDisponibilidade regra = RegraDisponibilidade.builder()
                .nome(nome)
                .sala(sala)
                .antecedenciaMinimaDias(request.antecedenciaMinimaDias())
                .build();

        regra = regraDisponibilidadeRepository.save(regra);

        RegraDisponibilidadeResponse response = salvarRegraComHorarios(regra, request.horarios());
        auditService.registrar(SecurityUtils.getUsuarioIdAtual(), "CRIAR", "RegraDisponibilidade", response.id());
        return response;
    }

    @Transactional
    public RegraDisponibilidadeResponse criarRegraIndependente(
            CriarRegraDisponibilidadeIndependenteRequest request) {
        validarHorarios(request.horarios());

        RegraDisponibilidade regra = RegraDisponibilidade.builder()
                .nome(request.nome().trim())
                .antecedenciaMinimaDias(request.antecedenciaMinimaDias())
                .build();

        RegraDisponibilidadeResponse response = salvarRegraComHorarios(regra, request.horarios());
        auditService.registrar(SecurityUtils.getUsuarioIdAtual(), "CRIAR", "RegraDisponibilidade", response.id());
        return response;
    }

    public List<RegraDisponibilidadeResponse> listarRegras() {
        return regraDisponibilidadeRepository.findAllByOrderByNomeAsc().stream()
                .map(regra -> RegraDisponibilidadeResponse.from(
                        regra,
                        horarioDisponibilidadeRepository.findByRegraDisponibilidadeId(regra.getId())))
                .toList();
    }

    public RegraDisponibilidadeResponse buscarRegra(UUID regraId) {
        RegraDisponibilidade regra = buscarEntidadeRegra(regraId);
        List<HorarioDisponibilidade> horarios =
                horarioDisponibilidadeRepository.findByRegraDisponibilidadeId(regra.getId());
        return RegraDisponibilidadeResponse.from(regra, horarios);
    }

    @Transactional
    public RegraDisponibilidadeResponse atualizarRegra(
            UUID regraId,
            CriarRegraDisponibilidadeIndependenteRequest request) {
        RegraDisponibilidade regra = buscarEntidadeRegra(regraId);
        validarHorarios(request.horarios());

        regra.setNome(request.nome().trim());
        regra.setAntecedenciaMinimaDias(request.antecedenciaMinimaDias());
        regra = regraDisponibilidadeRepository.save(regra);

        horarioDisponibilidadeRepository.deleteByRegraDisponibilidadeId(regra.getId());
        List<HorarioDisponibilidade> horarios = persistirHorarios(regra, request.horarios());

        auditService.registrar(SecurityUtils.getUsuarioIdAtual(), "ATUALIZAR", "RegraDisponibilidade", regraId);
        return RegraDisponibilidadeResponse.from(regra, horarios);
    }

    @Transactional
    public void excluirRegra(UUID regraId) {
        RegraDisponibilidade regra = buscarEntidadeRegra(regraId);

        if (regra.getSala() != null) {
            throw new RegraNegocioException(
                    "Desatribua a regra da sala antes de excluí-la.");
        }

        horarioDisponibilidadeRepository.deleteByRegraDisponibilidadeId(regra.getId());
        regraDisponibilidadeRepository.delete(regra);
        auditService.registrar(SecurityUtils.getUsuarioIdAtual(), "EXCLUIR", "RegraDisponibilidade", regraId);
    }

    @Transactional
    public RegraDisponibilidadeResponse atribuirRegraSala(UUID salaId, AtribuirRegraSalaRequest request) {
        Sala sala = salaService.buscarEntidadeAtiva(salaId);

        if (regraDisponibilidadeRepository.existsBySalaId(salaId)) {
            throw new RegraNegocioException(
                    "Esta sala já possui uma regra de disponibilidade atribuída.");
        }

        RegraDisponibilidade regra = buscarEntidadeRegra(request.regraId());

        if (regra.getSala() != null) {
            throw new RegraNegocioException(
                    "Esta regra já está atribuída a outra sala. Desatribua-a primeiro.");
        }

        regra.setSala(sala);
        regra = regraDisponibilidadeRepository.save(regra);

        List<HorarioDisponibilidade> horarios =
                horarioDisponibilidadeRepository.findByRegraDisponibilidadeId(regra.getId());

        auditService.registrar(SecurityUtils.getUsuarioIdAtual(), "ATRIBUIR", "RegraDisponibilidade", regra.getId());
        return RegraDisponibilidadeResponse.from(regra, horarios);
    }

    @Transactional
    public void desatribuirRegraSala(UUID salaId) {
        RegraDisponibilidade regra = regraDisponibilidadeRepository.findBySalaId(salaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Regra de disponibilidade não encontrada para esta sala."));

        regra.setSala(null);
        regraDisponibilidadeRepository.save(regra);
        auditService.registrar(SecurityUtils.getUsuarioIdAtual(), "DESATRIBUIR", "RegraDisponibilidade", regra.getId());
    }

    private RegraDisponibilidadeResponse salvarRegraComHorarios(
            RegraDisponibilidade regra,
            List<HorarioDisponibilidadeRequest> horariosRequest) {
        regra = regraDisponibilidadeRepository.save(regra);
        List<HorarioDisponibilidade> horarios = persistirHorarios(regra, horariosRequest);
        return RegraDisponibilidadeResponse.from(regra, horarios);
    }

    private List<HorarioDisponibilidade> persistirHorarios(
            RegraDisponibilidade regra,
            List<HorarioDisponibilidadeRequest> horariosRequest) {
        RegraDisponibilidade regraFinal = regra;
        return horariosRequest.stream()
                .map(h -> HorarioDisponibilidade.builder()
                        .regraDisponibilidade(regraFinal)
                        .diaSemana(h.diaSemana())
                        .horaAbertura(h.horaAbertura())
                        .horaFechamento(h.horaFechamento())
                        .build())
                .map(horarioDisponibilidadeRepository::save)
                .toList();
    }

    private RegraDisponibilidade buscarEntidadeRegra(UUID regraId) {
        return regraDisponibilidadeRepository.findById(regraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Regra de disponibilidade não encontrada."));
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
        ConsultaDisponibilidadeResponse consulta = consultarDisponibilidade(
                salaId, data, LocalTime.MIN, LocalTime.MAX);
        return new ValidacaoDisponibilidadeResponse(
                consulta.salaId(),
                consulta.data(),
                consulta.disponivelParaReserva(),
                consulta.mensagemRegras());
    }

    public ConsultaDisponibilidadeResponse consultarDisponibilidade(UUID salaId, LocalDate data) {
        return consultarDisponibilidade(salaId, data, LocalTime.MIN, LocalTime.MAX);
    }

    /**
     * RF-35 / fluxo 7.4 — disponibilidade por data e faixa horária com equipamentos por posição.
     */
    public ConsultaDisponibilidadeResponse consultarDisponibilidade(
            UUID salaId,
            LocalDate data,
            LocalTime horaInicio,
            LocalTime horaFim) {
        if (!horaInicio.isBefore(horaFim)) {
            throw new RegraNegocioException("A hora de início deve ser anterior à hora de fim.");
        }

        Sala sala = salaService.buscarEntidadeAtiva(salaId);
        ValidacaoDisponibilidadeResponse regras = validarRegrasDisponibilidade(salaId, data, horaInicio, horaFim);

        List<Posicao> todasPosicoes = posicaoService.listarPosicoesDaSala(salaId);
        List<UUID> posicoesOcupadasIds = reservaPosicaoRepository.findPosicaoIdsOcupadas(
                salaId, data, horaInicio, horaFim, STATUS_OCUPAM_POSICAO);

        List<UUID> posicaoIds = todasPosicoes.stream().map(Posicao::getId).toList();
        Map<UUID, List<String>> equipamentosPorPosicao = posicaoIds.isEmpty()
                ? Map.of()
                : posicaoEquipamentoRepository.findByPosicaoIdInWithTipoEquipamento(posicaoIds).stream()
                        .collect(Collectors.groupingBy(
                                pe -> pe.getPosicao().getId(),
                                Collectors.mapping(pe -> pe.getTipoEquipamento().getNome(), Collectors.toList())));

        int totalInativas = 0;
        int totalLivres = 0;
        int totalOcupadas = 0;

        List<PosicaoDisponibilidadeItemResponse> posicoes = new java.util.ArrayList<>();
        List<PosicaoOcupadaResponse> ocupadas = new java.util.ArrayList<>();

        for (Posicao posicao : todasPosicoes) {
            String situacao = resolverSituacao(posicao, posicoesOcupadasIds);
            List<String> equipamentos = equipamentosPorPosicao.getOrDefault(posicao.getId(), List.of());
            posicoes.add(new PosicaoDisponibilidadeItemResponse(
                    posicao.getId(),
                    posicao.getIdentificador(),
                    situacao,
                    equipamentos));

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

        Map<String, Long> livresPorTipo = posicoes.stream()
                .filter(p -> PosicaoStatus.LIVRE.equals(p.situacao()))
                .flatMap(p -> {
                    if (p.equipamentos().isEmpty()) {
                        return java.util.stream.Stream.of("SEM_EQUIPAMENTO");
                    }
                    return p.equipamentos().stream();
                })
                .collect(Collectors.groupingBy(nome -> nome, Collectors.counting()));

        boolean salaAtiva = sala.getStatus() == StatusSala.ATIVA;
        boolean disponivel = salaAtiva && regras.disponivel();

        String mensagem = montarMensagemConsulta(sala, regras);

        return new ConsultaDisponibilidadeResponse(
                salaId,
                data,
                horaInicio,
                horaFim,
                sala.getStatus(),
                disponivel,
                mensagem,
                todasPosicoes.size(),
                totalLivres,
                totalOcupadas,
                totalInativas,
                livresPorTipo,
                posicoes,
                ocupadas,
                List.of());
    }

    private ValidacaoDisponibilidadeResponse validarRegrasDisponibilidade(
            UUID salaId,
            LocalDate data,
            LocalTime horaInicio,
            LocalTime horaFim) {
        try {
            validarDisponibilidade(salaId, data, horaInicio, horaFim);
            return new ValidacaoDisponibilidadeResponse(salaId, data, true,
                    "A sala está disponível para reserva no horário informado.");
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
        if (posicao.getDeletedAt() != null || !PosicaoStatus.isAtiva(posicao)) {
            return PosicaoStatus.INATIVA;
        }

        if (posicoesOcupadasIds.contains(posicao.getId())) {
            return PosicaoStatus.OCUPADA;
        }

        return PosicaoStatus.LIVRE;
    }

    public void validarDisponibilidade(UUID salaId, LocalDate data) {
        validarDisponibilidade(salaId, data, null, null);
    }

    public void validarDisponibilidade(UUID salaId, LocalDate data, LocalTime horaInicio, LocalTime horaFim) {
        RegraDisponibilidade regra = regraDisponibilidadeRepository.findBySalaId(salaId)
                .orElseThrow(() -> new RegraNegocioException(
                        "A sala não possui regra de disponibilidade configurada."));

        verificarExcecao(salaId, data);
        verificarAntecedenciaMinima(regra, data);
        verificarDiaSemanaPermitido(regra, data);

        if (horaInicio != null && horaFim != null) {
            verificarHorarioDentroRegra(regra, data, horaInicio, horaFim);
        }
    }

    public void verificarHorarioDentroRegra(
            RegraDisponibilidade regra,
            LocalDate data,
            LocalTime horaInicio,
            LocalTime horaFim) {
        if (!horaInicio.isBefore(horaFim)) {
            throw new RegraNegocioException("A hora de início deve ser anterior à hora de fim.");
        }

        int diaSemana = converterDiaSemana(data);
        HorarioDisponibilidade horario = horarioDisponibilidadeRepository
                .findByRegraDisponibilidadeIdAndDiaSemana(regra.getId(), diaSemana)
                .orElseThrow(() -> new RegraNegocioException(
                        "A sala não aceita reservas no dia da semana informado."));

        if (horaInicio.isBefore(horario.getHoraAbertura())
                || horaFim.isAfter(horario.getHoraFechamento())) {
            throw new RegraNegocioException(String.format(
                    "O horário da reserva deve estar entre %s e %s.",
                    horario.getHoraAbertura(),
                    horario.getHoraFechamento()));
        }
    }

    public Optional<HorarioDisponibilidade> buscarHorarioDia(UUID salaId, LocalDate data) {
        return regraDisponibilidadeRepository.findBySalaId(salaId)
                .flatMap(regra -> horarioDisponibilidadeRepository.findByRegraDisponibilidadeIdAndDiaSemana(
                        regra.getId(), converterDiaSemana(data)));
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

        return montarResponse(regra);
    }

    private RegraDisponibilidadeResponse montarResponse(RegraDisponibilidade regra) {
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
