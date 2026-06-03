package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.response.NotificacaoResponse;
import com.accenture.officehub_v1.entity.Notificacao;
import com.accenture.officehub_v1.entity.Reserva;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.entity.enums.StatusNotificacao;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.repository.NotificacaoRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * RF-24 — Registra e envia notificações ao solicitante quando a reserva é confirmada ou rejeitada.
 * O envio real (e-mail/push) é simulado via log; a fila persiste em {@code notificacoes}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificacaoService {

    public static final String TIPO_RESERVA_CONFIRMADA = "RESERVA_CONFIRMADA";
    public static final String TIPO_RESERVA_REJEITADA = "RESERVA_REJEITADA";
    public static final String TIPO_RESERVA_CANCELADA = "RESERVA_CANCELADA";

    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public NotificacaoResponse notificarConfirmacaoReserva(Reserva reserva) {
        String assunto = "Reserva confirmada";
        String mensagem = String.format(
                "Sua reserva na sala \"%s\" para o dia %s foi confirmada com sucesso.",
                reserva.getSala().getNome(),
                reserva.getDataReserva());

        return criarENotificar(reserva, TIPO_RESERVA_CONFIRMADA, assunto, mensagem);
    }

    @Transactional
    public NotificacaoResponse notificarRejeicaoReserva(Reserva reserva) {
        String assunto = "Reserva não aprovada";
        String motivo = reserva.getMotivoRejeicao() != null ? reserva.getMotivoRejeicao() : "motivo não informado";
        String mensagem = String.format(
                "Sua reserva na sala \"%s\" para o dia %s foi rejeitada. Motivo: %s",
                reserva.getSala().getNome(),
                reserva.getDataReserva(),
                motivo);

        return criarENotificar(reserva, TIPO_RESERVA_REJEITADA, assunto, mensagem);
    }

    @Transactional
    public NotificacaoResponse notificarCancelamentoReserva(Reserva reserva) {
        String assunto = "Reserva cancelada";
        String motivo = reserva.getMotivoCancelamento() != null ? reserva.getMotivoCancelamento() : "motivo não informado";
        String mensagem = String.format(
                "Sua reserva na sala \"%s\" para o dia %s foi cancelada. Motivo: %s",
                reserva.getSala().getNome(),
                reserva.getDataReserva(),
                motivo);

        return criarENotificar(reserva, TIPO_RESERVA_CANCELADA, assunto, mensagem);
    }

    public List<NotificacaoResponse> listarPorUsuario(UUID usuarioId) {
        usuarioRepository.findByIdAndDeletedAtIsNull(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado."));

        return notificacaoRepository.findByUsuarioIdOrderByCreatedAtDesc(usuarioId).stream()
                .map(NotificacaoResponse::from)
                .toList();
    }

    @Transactional
    public void processarFilaPendente() {
        List<Notificacao> pendentes = notificacaoRepository.findByStatus(StatusNotificacao.FILA);
        for (Notificacao notificacao : pendentes) {
            enviar(notificacao);
        }
    }

    private NotificacaoResponse criarENotificar(Reserva reserva, String tipo, String assunto, String mensagem) {
        Usuario destinatario = reserva.getSolicitante();

        Notificacao notificacao = Notificacao.builder()
                .usuario(destinatario)
                .reserva(reserva)
                .tipo(tipo)
                .assunto(assunto)
                .mensagem(mensagem)
                .status(StatusNotificacao.FILA)
                .tentativas(0)
                .build();

        notificacao = notificacaoRepository.save(notificacao);
        enviar(notificacao);
        return NotificacaoResponse.from(notificacao);
    }

    private void enviar(Notificacao notificacao) {
        try {
            log.info("Enviando notificação [{}] para {} — Assunto: {}",
                    notificacao.getTipo(),
                    notificacao.getUsuario().getEmail(),
                    notificacao.getAssunto());

            notificacao.setStatus(StatusNotificacao.ENVIADA);
            notificacao.setEnviadoEm(OffsetDateTime.now());
            notificacaoRepository.save(notificacao);
        } catch (Exception ex) {
            log.error("Falha ao enviar notificação {}: {}", notificacao.getId(), ex.getMessage());
            notificacao.setStatus(StatusNotificacao.ERRO);
            notificacao.setTentativas(notificacao.getTentativas() + 1);
            notificacaoRepository.save(notificacao);
        }
    }
}
