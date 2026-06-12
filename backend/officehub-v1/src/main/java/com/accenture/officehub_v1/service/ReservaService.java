package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.AceitarSugestaoReservaRequest;
import com.accenture.officehub_v1.dto.request.CancelarReservaRequest;
import com.accenture.officehub_v1.dto.request.PessoaReservaRequest;
import com.accenture.officehub_v1.dto.request.SolicitarReservaRequest;
import com.accenture.officehub_v1.dto.request.SugestaoOutraAlocacaoRequest;
import com.accenture.officehub_v1.dto.response.PageResponse;
import com.accenture.officehub_v1.dto.response.ReservaPosicaoAlocadaResponse;
import com.accenture.officehub_v1.dto.response.ReservaResumoResponse;
import com.accenture.officehub_v1.dto.response.ReservaResponse;
import com.accenture.officehub_v1.dto.response.SugestaoAlocacaoResponse;
import com.accenture.officehub_v1.entity.Posicao;
import com.accenture.officehub_v1.entity.Reserva;
import com.accenture.officehub_v1.entity.ReservaPessoa;
import com.accenture.officehub_v1.entity.ReservaPosicao;
import com.accenture.officehub_v1.entity.Sala;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.entity.enums.StatusReserva;
import com.accenture.officehub_v1.exception.AcessoNegadoException;
import com.accenture.officehub_v1.exception.ConflitoAlocacaoException;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.service.ia.AgenteAlocacaoService;
import com.accenture.officehub_v1.service.ia.ResultadoExecucaoAgente;
import com.accenture.officehub_v1.repository.PosicaoEquipamentoRepository;
import com.accenture.officehub_v1.repository.ReservaPessoaRepository;
import com.accenture.officehub_v1.repository.ReservaPosicaoRepository;
import com.accenture.officehub_v1.repository.ReservaRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import com.accenture.officehub_v1.service.alocacao.ItemAlocacao;
import com.accenture.officehub_v1.service.alocacao.ResultadoAlocacao;
import com.accenture.officehub_v1.security.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservaService {

    private static final Set<StatusReserva> STATUS_OCUPAM_POSICAO =
            EnumSet.of(StatusReserva.PENDENTE, StatusReserva.CONFIRMADA);

    private static final Set<StatusReserva> STATUS_ATIVAS_LISTAGEM =
            EnumSet.of(StatusReserva.PENDENTE, StatusReserva.CONFIRMADA, StatusReserva.REJEITADA);

    private final ReservaRepository reservaRepository;
    private final ReservaPessoaRepository reservaPessoaRepository;
    private final ReservaPosicaoRepository reservaPosicaoRepository;
    private final PosicaoEquipamentoRepository posicaoEquipamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalaService salaService;
    private final DisponibilidadeService disponibilidadeService;
    private final PosicaoService posicaoService;
    private final NotificacaoService notificacaoService;
    private final AgenteAlocacaoService agenteAlocacaoService;
    private final LayoutService layoutService;
    private final AuditService auditService;
    private final ReservaAutorizacaoService reservaAutorizacaoService;

    public SugestaoAlocacaoResponse sugerirAlocacao(
            SolicitarReservaRequest request,
            UUID solicitanteId,
            Collection<String> perfis) {
        ContextoAlocacao contexto = prepararContextoAlocacao(request, solicitanteId, perfis);
        ResultadoExecucaoAgente execucaoIa = agenteAlocacaoService.executar(
                contexto.sala(),
                request.dataReserva(),
                request.equipeId(),
                request.pessoas(),
                request.criterioProximidade(),
                contexto.posicoesLivres());
        return montarSugestao(execucaoIa);
    }

    public SugestaoAlocacaoResponse sugerirOutraAlocacao(
            SugestaoOutraAlocacaoRequest request,
            UUID solicitanteId,
            Collection<String> perfis) {
        SolicitarReservaRequest reserva = request.reserva();
        ContextoAlocacao contexto = prepararContextoAlocacao(reserva, solicitanteId, perfis);
        ResultadoExecucaoAgente execucaoIa = agenteAlocacaoService.executar(
                contexto.sala(),
                reserva.dataReserva(),
                reserva.equipeId(),
                reserva.pessoas(),
                reserva.criterioProximidade(),
                contexto.posicoesLivres(),
                request.combinacoesExcluidas());
        return montarSugestao(execucaoIa);
    }

    @Transactional
    public ReservaResponse solicitarReserva(
            AceitarSugestaoReservaRequest request,
            UUID solicitanteId,
            Collection<String> perfis) {
        SolicitarReservaRequest dados = request.reserva();
        ContextoAlocacao contexto = prepararContextoAlocacao(dados, solicitanteId, perfis);

        ResultadoAlocacao resultado = agenteAlocacaoService.recuperarResultadoExecucao(
                request.execucaoId(),
                dados.pessoas(),
                contexto.posicoesLivres());

        if (!resultado.sucesso()) {
            throw new ConflitoAlocacaoException(resultado.motivoFalha());
        }

        Usuario solicitante = usuarioRepository.findByIdAndDeletedAtIsNull(solicitanteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Solicitante da reserva não encontrado."));

        ReservaResponse response = persistirReservaPendente(
                dados, contexto.sala(), solicitante, resultado.alocacoes(), resultado.avisoProximidade());
        agenteAlocacaoService.vincularReferenciaReserva(request.execucaoId(), response.id());
        auditService.registrar(solicitanteId, "CRIAR", "Reserva", response.id());
        return response;
    }

    @Transactional
    public ReservaResponse cancelarReserva(
            UUID id,
            CancelarReservaRequest request,
            UUID canceladoPorId,
            Collection<String> perfis) {
        Reserva reserva = buscarEntidadeAtiva(id);
        validarPermissaoGerenciarReserva(reserva, canceladoPorId, perfis);

        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new RegraNegocioException("Esta reserva já foi cancelada.");
        }

        Usuario canceladoPor = usuarioRepository.findByIdAndDeletedAtIsNull(canceladoPorId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário responsável pelo cancelamento não encontrado."));

        reserva.setStatus(StatusReserva.CANCELADA);
        reserva.setMotivoCancelamento(request.motivo());
        reserva.setCanceladoPor(canceladoPor);
        reserva.setCanceladoEm(OffsetDateTime.now());
        reserva.setDeletedAt(OffsetDateTime.now());

        reserva = reservaRepository.save(reserva);
        notificacaoService.notificarCancelamentoReserva(reserva);
        auditService.registrar(canceladoPorId, "CANCELAR", "Reserva", reserva.getId());
        return toResponse(reserva);
    }

    @Transactional
    public ReservaResponse confirmarReserva(UUID id, UUID usuarioId, Collection<String> perfis) {
        Reserva reserva = buscarEntidadeAtiva(id);
        validarPermissaoGerenciarReserva(reserva, usuarioId, perfis);

        if (reserva.getStatus() != StatusReserva.PENDENTE) {
            throw new RegraNegocioException(
                    "Apenas reservas com status PENDENTE podem ser confirmadas.");
        }

        reserva.setStatus(StatusReserva.CONFIRMADA);
        reserva = reservaRepository.save(reserva);
        notificacaoService.notificarConfirmacaoReserva(reserva);
        return toResponse(reserva);
    }

    @Transactional
    public ReservaResponse rejeitarReserva(UUID id, String motivoRejeicao, UUID usuarioId, Collection<String> perfis) {
        Reserva reserva = buscarEntidadeAtiva(id);
        validarPermissaoGerenciarReserva(reserva, usuarioId, perfis);

        if (reserva.getStatus() != StatusReserva.PENDENTE) {
            throw new RegraNegocioException(
                    "Apenas reservas com status PENDENTE podem ser rejeitadas.");
        }

        reserva.setStatus(StatusReserva.REJEITADA);
        reserva.setMotivoRejeicao(motivoRejeicao);
        reserva = reservaRepository.save(reserva);
        notificacaoService.notificarRejeicaoReserva(reserva);
        return toResponse(reserva);
    }

    public ReservaResponse buscarPorId(UUID id, UUID usuarioId, Collection<String> perfis) {
        Reserva reserva = reservaRepository.findByIdComDetalhes(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva não encontrada."));
        validarPermissaoGerenciarReserva(reserva, usuarioId, perfis);
        return toResponse(reserva);
    }

    public static final int TAMANHO_PAGINA_PADRAO = 15;

    public PageResponse<ReservaResumoResponse> listarReservas(
            LocalDate data,
            boolean canceladas,
            int page,
            int size,
            UUID usuarioId,
            Collection<String> perfis) {
        int pagina = Math.max(0, page);
        int tamanho = size > 0 ? size : TAMANHO_PAGINA_PADRAO;

        if (perfis.contains(Roles.ADMIN_SALA)) {
            return listarReservasAdmin(data, canceladas, pagina, tamanho);
        }

        List<Reserva> reservas;
        if (canceladas) {
            reservas = data != null
                    ? reservaRepository.findCanceladasPorData(StatusReserva.CANCELADA, data)
                    : reservaRepository.findCanceladas(StatusReserva.CANCELADA);
        } else {
            reservas = data != null
                    ? reservaRepository.findAtivasPorData(STATUS_ATIVAS_LISTAGEM, data)
                    : reservaRepository.findAtivas(STATUS_ATIVAS_LISTAGEM);
        }

        List<ReservaResumoResponse> filtradas = reservas.stream()
                .filter(r -> reservaAutorizacaoService.podeGerenciarReserva(usuarioId, perfis, r))
                .map(ReservaResumoResponse::from)
                .toList();

        return PageResponse.fromList(filtradas, pagina, tamanho);
    }

    private PageResponse<ReservaResumoResponse> listarReservasAdmin(
            LocalDate data,
            boolean canceladas,
            int page,
            int size) {
        Pageable pageable = canceladas
                ? PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "canceladoEm"))
                : PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Order.desc("dataReserva"), Sort.Order.asc("horaInicio")));

        if (canceladas) {
            return data != null
                    ? PageResponse.from(
                            reservaRepository.findCanceladasPorDataPaginado(
                                    StatusReserva.CANCELADA, data, pageable),
                            ReservaResumoResponse::from)
                    : PageResponse.from(
                            reservaRepository.findCanceladasPaginado(StatusReserva.CANCELADA, pageable),
                            ReservaResumoResponse::from);
        }

        return data != null
                ? PageResponse.from(
                        reservaRepository.findAtivasPorDataPaginado(STATUS_ATIVAS_LISTAGEM, data, pageable),
                        ReservaResumoResponse::from)
                : PageResponse.from(
                        reservaRepository.findAtivasPaginado(STATUS_ATIVAS_LISTAGEM, pageable),
                        ReservaResumoResponse::from);
    }

    public List<Posicao> buscarPosicoesLivres(UUID salaId, LocalDate data, java.time.LocalTime horaInicio, java.time.LocalTime horaFim) {
        List<Posicao> posicoesAtivas = posicaoService.listarPosicoesAtivasDaSala(salaId);
        List<UUID> posicoesOcupadas = reservaPosicaoRepository.findPosicaoIdsOcupadas(
                salaId, data, horaInicio, horaFim, STATUS_OCUPAM_POSICAO);

        return posicoesAtivas.stream()
                .filter(p -> !posicoesOcupadas.contains(p.getId()))
                .toList();
    }

    public Reserva buscarEntidadeAtiva(UUID id) {
        return reservaRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Reserva não encontrada ou foi cancelada."));
    }

    /**
     * Bloqueia reserva quando sala inativa, data em exceção, antecedência ou dia da semana inválido.
     */
    private Sala validarPreCondicoesReserva(SolicitarReservaRequest request) {
        Sala sala = salaService.buscarEntidadeAtiva(request.salaId());
        salaService.validarSalaAtiva(request.salaId());
        layoutService.validarLayoutAtivoAprovado(request.salaId());
        disponibilidadeService.validarDisponibilidade(
                request.salaId(), request.dataReserva(), request.horaInicio(), request.horaFim());
        return sala;
    }

    private ContextoAlocacao prepararContextoAlocacao(
            SolicitarReservaRequest request,
            UUID solicitanteId,
            Collection<String> perfis) {
        Sala sala = validarPreCondicoesReserva(request);
        validarQuantidadePessoas(request, sala);
        validarPessoasInformadas(request, solicitanteId, perfis);
        validarCriterioProximidade(request.criterioProximidade());

        List<Posicao> posicoesLivres = buscarPosicoesLivres(
                request.salaId(), request.dataReserva(), request.horaInicio(), request.horaFim());

        return new ContextoAlocacao(sala, posicoesLivres);
    }

    private SugestaoAlocacaoResponse montarSugestao(ResultadoExecucaoAgente execucaoIa) {
        ResultadoAlocacao resultado = execucaoIa.resultadoAlocacao();
        if (!resultado.sucesso()) {
            throw new ConflitoAlocacaoException(resultado.motivoFalha());
        }

        List<UUID> posicaoIds = resultado.alocacoes().stream()
                .map(item -> item.posicao().getId())
                .toList();

        Map<UUID, List<String>> equipamentosPorPosicao = posicaoIds.isEmpty()
                ? Map.of()
                : posicaoEquipamentoRepository.findByPosicaoIdInWithTipoEquipamento(posicaoIds).stream()
                        .collect(Collectors.groupingBy(
                                pe -> pe.getPosicao().getId(),
                                Collectors.mapping(pe -> pe.getTipoEquipamento().getNome(), Collectors.toList())));

        Map<UUID, String> nomesPorUsuario = resultado.alocacoes().stream()
                .map(item -> item.pessoa().usuarioId())
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        id -> usuarioRepository.findByIdAndDeletedAtIsNull(id)
                                .map(Usuario::getNome)
                                .orElse("Participante")));

        List<ReservaPosicaoAlocadaResponse> alocacoes = resultado.alocacoes().stream()
                .map(item -> ReservaPosicaoAlocadaResponse.from(
                        item,
                        item.pessoa().usuarioId() != null
                                ? nomesPorUsuario.get(item.pessoa().usuarioId())
                                : item.pessoa().nomeExterno(),
                        equipamentosPorPosicao.getOrDefault(item.posicao().getId(), List.of())))
                .toList();

        return new SugestaoAlocacaoResponse(
                execucaoIa.execucaoId(),
                resultado.avisoProximidade(),
                alocacoes,
                posicaoIds);
    }

    private ReservaResponse persistirReservaPendente(
            SolicitarReservaRequest request,
            Sala sala,
            Usuario solicitante,
            List<ItemAlocacao> alocacoes,
            String avisoProximidade) {

        Reserva reserva = reservaRepository.save(Reserva.builder()
                .sala(sala)
                .solicitante(solicitante)
                .dataReserva(request.dataReserva())
                .horaInicio(request.horaInicio())
                .horaFim(request.horaFim())
                .quantidadePessoas(request.quantidadePessoas())
                .criterioProximidade(request.criterioProximidade())
                .status(StatusReserva.PENDENTE)
                .build());

        persistirPessoasEPosicoes(reserva, alocacoes);
        return toResponse(reserva, avisoProximidade);
    }

    private record ContextoAlocacao(Sala sala, List<Posicao> posicoesLivres) {
    }

    private void persistirPessoasEPosicoes(Reserva reserva, List<ItemAlocacao> alocacoes) {
        for (ItemAlocacao item : alocacoes) {
            if (!PosicaoStatus.isAtiva(item.posicao())) {
                throw new ConflitoAlocacaoException(
                        "A posição " + item.posicao().getIdentificador()
                                + " está bloqueada e não pode ser reservada.");
            }

            ReservaPessoa pessoa = salvarReservaPessoa(reserva, item.pessoa());

            ReservaPosicao reservaPosicao = ReservaPosicao.builder()
                    .reserva(reserva)
                    .reservaPessoa(pessoa)
                    .posicao(item.posicao())
                    .build();

            reservaPosicaoRepository.save(reservaPosicao);
        }
    }

    private ReservaPessoa salvarReservaPessoa(Reserva reserva, PessoaReservaRequest pessoaRequest) {
        Usuario usuario = pessoaRequest.usuarioId() != null
                ? usuarioRepository.findByIdAndDeletedAtIsNull(pessoaRequest.usuarioId()).orElse(null)
                : null;

        ReservaPessoa pessoa = ReservaPessoa.builder()
                .reserva(reserva)
                .usuario(usuario)
                .nomeExterno(pessoaRequest.nomeExterno())
                .tipoPreferido1(pessoaRequest.tipoPreferido1())
                .tipoPreferido2(pessoaRequest.tipoPreferido2())
                .tipoPreferido3(pessoaRequest.tipoPreferido3())
                .build();

        return reservaPessoaRepository.save(pessoa);
    }

    private void validarQuantidadePessoas(SolicitarReservaRequest request, Sala sala) {
        if (request.quantidadePessoas() > sala.getCapacidadeMaxima()) {
            throw new RegraNegocioException(String.format(
                    "A quantidade de pessoas (%d) excede a capacidade máxima da sala (%d).",
                    request.quantidadePessoas(),
                    sala.getCapacidadeMaxima()));
        }
    }

    private void validarPessoasInformadas(
            SolicitarReservaRequest request,
            UUID solicitanteId,
            Collection<String> perfis) {
        if (request.pessoas().size() != request.quantidadePessoas()) {
            throw new RegraNegocioException(
                    "A quantidade de pessoas informada não corresponde ao número de participantes cadastrados.");
        }

        if (perfis.contains(Roles.GESTOR_RESERVAS)) {
            validarReservaGestor(request, solicitanteId);
        }

        for (PessoaReservaRequest pessoa : request.pessoas()) {
            if (pessoa.usuarioId() == null) {
                throw new RegraNegocioException(
                        "Cada participante deve possuir um usuário associado.");
            }

            if (pessoa.nomeExterno() != null && !pessoa.nomeExterno().isBlank()) {
                throw new RegraNegocioException("Nome externo não é permitido para reservas.");
            }

            if (perfis.contains(Roles.USUARIO_FINAL)
                    && !pessoa.usuarioId().equals(solicitanteId)) {
                throw new RegraNegocioException(
                        "Usuário final só pode reservar para si mesmo.");
            }
        }
    }

    private void validarReservaGestor(SolicitarReservaRequest request, UUID solicitanteId) {
        if (request.equipeId() == null) {
            throw new RegraNegocioException(
                    "Selecione a equipe para a qual deseja fazer a reserva.");
        }

        if (!reservaAutorizacaoService.gestorGerenciaEquipe(solicitanteId, request.equipeId())) {
            throw new AcessoNegadoException("Você não gerencia a equipe selecionada.");
        }

        for (PessoaReservaRequest pessoa : request.pessoas()) {
            if (!reservaAutorizacaoService.usuarioPertenceEquipe(
                    request.equipeId(), solicitanteId, pessoa.usuarioId())) {
                throw new RegraNegocioException(
                        "Todos os participantes devem pertencer à equipe selecionada.");
            }
        }
    }

    private void validarCriterioProximidade(String criterio) {
        if (!CriterioProximidade.isObrigatoria(criterio) && !CriterioProximidade.isPreferencial(criterio)) {
            throw new RegraNegocioException(
                    "Critério de proximidade inválido. Use OBRIGATORIA ou PREFERENCIAL.");
        }
    }

    private void validarPermissaoGerenciarReserva(Reserva reserva, UUID usuarioId, Collection<String> perfis) {
        if (!reservaAutorizacaoService.podeGerenciarReserva(usuarioId, perfis, reserva)) {
            throw new AcessoNegadoException(
                    "Você não tem permissão para gerenciar esta reserva.");
        }
    }

    private ReservaResponse toResponse(Reserva reserva) {
        return toResponse(reserva, null);
    }

    private ReservaResponse toResponse(Reserva reserva, String avisoProximidade) {
        List<ReservaPosicao> reservaPosicoes =
                reservaPosicaoRepository.findByReservaIdWithDetails(reserva.getId());

        List<UUID> posicaoIds = reservaPosicoes.stream()
                .map(rp -> rp.getPosicao().getId())
                .toList();

        Map<UUID, List<String>> equipamentosPorPosicao = posicaoIds.isEmpty()
                ? Map.of()
                : posicaoEquipamentoRepository.findByPosicaoIdInWithTipoEquipamento(posicaoIds).stream()
                        .collect(Collectors.groupingBy(
                                pe -> pe.getPosicao().getId(),
                                Collectors.mapping(pe -> pe.getTipoEquipamento().getNome(), Collectors.toList())));

        List<ReservaPosicaoAlocadaResponse> alocacoes = reservaPosicoes.stream()
                .map(rp -> ReservaPosicaoAlocadaResponse.from(
                        rp,
                        equipamentosPorPosicao.getOrDefault(rp.getPosicao().getId(), List.of())))
                .toList();

        return ReservaResponse.from(reserva, alocacoes, avisoProximidade);
    }
}
