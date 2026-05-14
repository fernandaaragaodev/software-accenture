package com.accenture.officehub.officehub_api.service.impl;

import com.accenture.officehub.officehub_api.dto.NotificationResponseDto;
import com.accenture.officehub.officehub_api.exception.ForbiddenException;
import com.accenture.officehub.officehub_api.exception.ResourceNotFoundException;
import com.accenture.officehub.officehub_api.model.Notification;
import com.accenture.officehub.officehub_api.model.Reservation;
import com.accenture.officehub.officehub_api.repository.NotificationRepository;
import com.accenture.officehub.officehub_api.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM");

    private final NotificationRepository notificationRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public List<NotificationResponseDto> listNotifications(String viewerName, String viewerRole) {
        List<Notification> all = notificationRepository.findAll();
        if (isAdminRole(viewerRole)) {
            return all.stream().map(this::toDto).toList();
        }
        String viewer = normalizeName(viewerName);
        if (viewer.isEmpty()) {
            return List.of();
        }
        return all.stream()
                .filter(n -> matchesViewer(n, viewer))
                .map(this::toDto)
                .toList();
    }

    @Override
    public void markAsRead(Long notificationId, String viewerName, String viewerRole) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacao com id " + notificationId + " nao encontrada."));
        assertCanAccess(notification, viewerName, viewerRole);
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(String viewerName, String viewerRole) {
        if (isAdminRole(viewerRole)) {
            notificationRepository.findAll().forEach(notification -> {
                if (!notification.isRead()) {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                }
            });
            return;
        }
        String viewer = normalizeName(viewerName);
        if (viewer.isEmpty()) {
            return;
        }
        notificationRepository.findAll().stream()
                .filter(n -> matchesViewer(n, viewer))
                .filter(n -> !n.isRead())
                .forEach(notification -> {
                    notification.setRead(true);
                    notificationRepository.save(notification);
                });
    }

    @Override
    public void createReservationConfirmedNotification(Reservation reservation) {
        String actor = normalizeName(reservation.getRequesterName());
        if (actor.isEmpty()) {
            actor = normalizeName(reservation.getUser());
        }
        String text = String.format(
                "Reserva confirmada: %s para %s · %s (%s-%s) · %s",
                reservation.getRoom(),
                reservation.getUser(),
                reservation.getDate().format(DATE_FORMATTER),
                reservation.getStart(),
                reservation.getEnd(),
                reservation.getSeatCode()
        );
        createNotification("reservation_confirmed", text, "var(--green)", reservation.getId(), reservation.getReservationGroupId(), actor);
    }

    @Override
    public void createReservationCancelledNotification(Reservation reservation, String actorUserName) {
        String actor = normalizeName(actorUserName);
        String text = String.format(
                "Reserva cancelada: %s · %s (%s-%s) · %s",
                reservation.getRoom(),
                reservation.getDate().format(DATE_FORMATTER),
                reservation.getStart(),
                reservation.getEnd(),
                reservation.getSeatCode()
        );
        createNotification("reservation_cancelled", text, "var(--red)", reservation.getId(), reservation.getReservationGroupId(), actor);
    }

    @Override
    public void createReservationGroupConfirmedNotification(
            String reservationGroupId,
            String room,
            String date,
            String start,
            String end,
            int peopleCount,
            String actorUserName
    ) {
        String actor = normalizeName(actorUserName);
        String text = String.format(
                "Reserva em lote confirmada: %s · %s (%s-%s) · %d pessoa(s)",
                room,
                date,
                start,
                end,
                peopleCount
        );
        createNotification("reservation_group_confirmed", text, "var(--green)", null, reservationGroupId, actor);
    }

    @Override
    public void createReservationGroupCancelledNotification(
            String reservationGroupId,
            String room,
            String date,
            String start,
            String end,
            int peopleCount,
            String actorUserName
    ) {
        String actor = normalizeName(actorUserName);
        String text = String.format(
                "Reserva em lote cancelada: %s · %s (%s-%s) · %d pessoa(s)",
                room,
                date,
                start,
                end,
                peopleCount
        );
        createNotification("reservation_group_cancelled", text, "var(--red)", null, reservationGroupId, actor);
    }

    private void createNotification(String type, String text, String color, Long reservationId, String reservationGroupId, String actorUserName) {
        notificationRepository.save(new Notification(
                null,
                type,
                text,
                color,
                false,
                LocalDateTime.now(),
                reservationId,
                reservationGroupId,
                actorUserName.isEmpty() ? null : actorUserName
        ));
    }

    private NotificationResponseDto toDto(Notification notification) {
        return new NotificationResponseDto(
                notification.getId(),
                notification.getType(),
                notification.getText(),
                notification.getColor(),
                notification.isRead(),
                notification.getCreatedAt().toString(),
                notification.getReservationId(),
                notification.getReservationGroupId(),
                notification.getActorUserName()
        );
    }

    private static boolean isAdminRole(String viewerRole) {
        return viewerRole != null && viewerRole.trim().equalsIgnoreCase("admin");
    }

    private static String normalizeName(String name) {
        return name == null ? "" : name.trim();
    }

    private static boolean matchesViewer(Notification n, String viewerNormalized) {
        return n.getActorUserName() != null && n.getActorUserName().trim().equalsIgnoreCase(viewerNormalized);
    }

    private void assertCanAccess(Notification notification, String viewerName, String viewerRole) {
        if (isAdminRole(viewerRole)) {
            return;
        }
        String viewer = normalizeName(viewerName);
        if (viewer.isEmpty() || !matchesViewer(notification, viewer)) {
            throw new ForbiddenException("Sem permissao para esta notificacao.");
        }
    }
}
