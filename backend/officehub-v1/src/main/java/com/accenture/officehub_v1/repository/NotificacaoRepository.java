package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.Notificacao;
import com.accenture.officehub_v1.entity.enums.StatusNotificacao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificacaoRepository extends JpaRepository<Notificacao, UUID> {

    List<Notificacao> findByUsuarioIdOrderByCreatedAtDesc(UUID usuarioId);

    List<Notificacao> findByStatus(StatusNotificacao status);
}
