package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.CancelarReservaRequest;
import com.accenture.officehub_v1.dto.request.PessoaReservaRequest;
import com.accenture.officehub_v1.dto.request.SolicitarReservaRequest;
import com.accenture.officehub_v1.dto.response.ReservaResponse;
import com.accenture.officehub_v1.entity.Posicao;
import com.accenture.officehub_v1.entity.Reserva;
import com.accenture.officehub_v1.entity.ReservaPessoa;
import com.accenture.officehub_v1.entity.Sala;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.entity.enums.StatusReserva;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.ReservaPessoaRepository;
import com.accenture.officehub_v1.repository.ReservaPosicaoRepository;
import com.accenture.officehub_v1.repository.ReservaRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
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

    /**
     * Versão inicial: valida regras de negócio e persiste a reserva como PENDENTE.
     * A alocação definitiva de posições pelo Agente de IA (RF-19) será integrada posteriormente.
     */
    @Transactional
    public ReservaResponse solicitarReserva(SolicitarReservaRequest request, UUID solicitanteId) {
        Sala sala = salaService.buscarEntidadeAtiva(request.salaId());
        salaService.validarSalaAtiva(request.salaId());
        disponibilidadeService.validarDisponibilidade(request.salaId(), request.dataReserva());

        validarQuantidadePessoas(request);
        validarPessoasInformadas(request);

        List<Posicao> posicoesLivres = buscarPosicoesLivres(request.salaId(), request.dataReserva());
        validarPosicoesSuficientes(posicoesLivres.size(), request.quantidadePessoas());

        Usuario solicitante = usuarioRepository.findByIdAndDeletedAtIsNull(solicitanteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Solicitante da reserva não encontrado."));

        Reserva reserva = Reserva.builder()
                .sala(sala)
                .solicitante(solicitante)
                .dataReserva(request.dataReserva())
                .quantidadePessoas(request.quantidadePessoas())
                .criterioProximidade(request.criterioProximidade())
                .status(StatusReserva.PENDENTE)
                .build();

        reserva = reservaRepository.save(reserva);

        Reserva reservaFinal = reserva;
        for (PessoaReservaRequest pessoaRequest : request.pessoas()) {
            Usuario usuario = pessoaRequest.usuarioId() != null
                    ? usuarioRepository.findByIdAndDeletedAtIsNull(pessoaRequest.usuarioId()).orElse(null)
                    : null;

            ReservaPessoa pessoa = ReservaPessoa.builder()
                    .reserva(reservaFinal)
                    .usuario(usuario)
                    .nomeExterno(pessoaRequest.nomeExterno())
                    .tipoPreferido1(pessoaRequest.tipoPreferido1())
                    .tipoPreferido2(pessoaRequest.tipoPreferido2())
                    .tipoPreferido3(pessoaRequest.tipoPreferido3())
                    .build();

            reservaPessoaRepository.save(pessoa);
        }

        return ReservaResponse.from(reserva);
    }

    @Transactional
    public ReservaResponse cancelarReserva(UUID id, CancelarReservaRequest request, UUID canceladoPorId) {
        Reserva reserva = buscarEntidadeAtiva(id);

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
        return ReservaResponse.from(reserva);
    }

    @Transactional
    public ReservaResponse confirmarReserva(UUID id) {
        Reserva reserva = buscarEntidadeAtiva(id);

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
    public ReservaResponse rejeitarReserva(UUID id, String motivoRejeicao) {
        Reserva reserva = buscarEntidadeAtiva(id);

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

    public ReservaResponse buscarPorId(UUID id) {
        return ReservaResponse.from(buscarEntidadeAtiva(id));
    }

    public List<Posicao> buscarPosicoesLivres(UUID salaId, java.time.LocalDate data) {
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

    private void validarQuantidadePessoas(SolicitarReservaRequest request) {
        Sala sala = salaService.buscarEntidadeAtiva(request.salaId());

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

    private void validarPosicoesSuficientes(int posicoesLivres, int quantidadePessoas) {
        if (posicoesLivres < quantidadePessoas) {
            throw new RegraNegocioException(String.format(
                    "Não há posições livres suficientes na data solicitada. " +
                            "Disponíveis: %d, solicitadas: %d.",
                    posicoesLivres, quantidadePessoas));
        }
    }
}
