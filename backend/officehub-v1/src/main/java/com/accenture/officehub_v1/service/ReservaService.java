package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.CancelarReservaRequest;
import com.accenture.officehub_v1.dto.request.PessoaReservaRequest;
import com.accenture.officehub_v1.dto.request.SolicitarReservaRequest;
import com.accenture.officehub_v1.dto.response.ReservaResponse;
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
import com.accenture.officehub_v1.repository.ReservaPessoaRepository;
import com.accenture.officehub_v1.repository.ReservaPosicaoRepository;
import com.accenture.officehub_v1.repository.ReservaRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import com.accenture.officehub_v1.service.alocacao.ItemAlocacao;
import com.accenture.officehub_v1.service.alocacao.ResultadoAlocacao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservaService {

    private static final Set<StatusReserva> STATUS_OCUPAM_POSICAO =
            EnumSet.of(StatusReserva.PENDENTE, StatusReserva.CONFIRMADA);

    private final ReservaRepository reservaRepository;
    private final ReservaPessoaRepository reservaPessoaRepository;
    private final ReservaPosicaoRepository reservaPosicaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final SalaService salaService;
    private final DisponibilidadeService disponibilidadeService;
    private final PosicaoService posicaoService;
    private final NotificacaoService notificacaoService;
    private final AgenteAlocacaoService agenteAlocacaoService;
    private final AuditService auditService;
    private final ReservaAutorizacaoService reservaAutorizacaoService;

    @Transactional
    public ReservaResponse solicitarReserva(SolicitarReservaRequest request, UUID solicitanteId) {
        Sala sala = validarPreCondicoesReserva(request);

        validarQuantidadePessoas(request, sala);
        validarPessoasInformadas(request);
        validarCriterioProximidade(request.criterioProximidade());

        List<Posicao> posicoesLivres = buscarPosicoesLivres(request.salaId(), request.dataReserva());

        ResultadoExecucaoAgente execucaoIa = agenteAlocacaoService.executar(
                request.salaId(),
                request.dataReserva(),
                request.pessoas(),
                request.criterioProximidade(),
                sala.getRaioProximidade(),
                posicoesLivres);

        ResultadoAlocacao resultado = execucaoIa.resultadoAlocacao();

        if (!resultado.sucesso()) {
            throw new ConflitoAlocacaoException(resultado.motivoFalha());
        }

        Usuario solicitante = usuarioRepository.findByIdAndDeletedAtIsNull(solicitanteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Solicitante da reserva não encontrado."));

        ReservaResponse response = persistirReservaConfirmada(
                request, sala, solicitante, resultado.alocacoes(), resultado.avisoProximidade());
        agenteAlocacaoService.vincularReferenciaReserva(execucaoIa.execucaoId(), response.id());
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
        return ReservaResponse.from(reserva);
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
        return ReservaResponse.from(reserva);
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
        return ReservaResponse.from(reserva);
    }

    public ReservaResponse buscarPorId(UUID id, UUID usuarioId, Collection<String> perfis) {
        Reserva reserva = buscarEntidadeAtiva(id);
        validarPermissaoGerenciarReserva(reserva, usuarioId, perfis);
        return ReservaResponse.from(reserva);
    }

    public List<Posicao> buscarPosicoesLivres(UUID salaId, LocalDate data) {
        List<Posicao> posicoesAtivas = posicaoService.listarPosicoesAtivasDaSala(salaId);
        List<UUID> posicoesOcupadas = reservaPosicaoRepository.findPosicaoIdsOcupadas(
                salaId, data, STATUS_OCUPAM_POSICAO);

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
        disponibilidadeService.validarDisponibilidade(request.salaId(), request.dataReserva());
        return sala;
    }

    private ReservaResponse persistirReservaConfirmada(
            SolicitarReservaRequest request,
            Sala sala,
            Usuario solicitante,
            List<ItemAlocacao> alocacoes,
            String avisoProximidade) {

        Reserva reserva = reservaRepository.save(Reserva.builder()
                .sala(sala)
                .solicitante(solicitante)
                .dataReserva(request.dataReserva())
                .quantidadePessoas(request.quantidadePessoas())
                .criterioProximidade(request.criterioProximidade())
                .status(StatusReserva.CONFIRMADA)
                .build());

        persistirPessoasEPosicoes(reserva, alocacoes);
        notificacaoService.notificarConfirmacaoReserva(reserva);
        return ReservaResponse.from(reserva, alocacoes, avisoProximidade);
    }

    private void persistirPessoasEPosicoes(Reserva reserva, List<ItemAlocacao> alocacoes) {
        for (ItemAlocacao item : alocacoes) {
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

    private void validarPessoasInformadas(SolicitarReservaRequest request) {
        if (request.pessoas().size() != request.quantidadePessoas()) {
            throw new RegraNegocioException(
                    "A quantidade de pessoas informada não corresponde ao número de participantes cadastrados.");
        }

        for (PessoaReservaRequest pessoa : request.pessoas()) {
            if (pessoa.usuarioId() == null
                    && (pessoa.nomeExterno() == null || pessoa.nomeExterno().isBlank())) {
                throw new RegraNegocioException(
                        "Cada participante deve possuir usuário interno ou nome externo.");
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
}
