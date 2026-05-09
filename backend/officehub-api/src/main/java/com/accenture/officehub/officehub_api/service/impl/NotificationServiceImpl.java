package com.accenture.officehub.officehub_api.service.impl;

import org.springframework.stereotype.Service;

import com.accenture.officehub.officehub_api.dto.NotificationResponseDto;
import com.accenture.officehub.officehub_api.exception.ResourceNotFoundException;
import com.accenture.officehub.officehub_api.model.Notification;
import com.accenture.officehub.officehub_api.model.Reservation;
import com.accenture.officehub.officehub_api.repository.NotificationRepository;
import com.accenture.officehub.officehub_api.service.NotificationService;

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
    public List<NotificationResponseDto> listNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notificacao com id " + notificationId + " nao encontrada."));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead() {
        notificationRepository.findAll().forEach(notification -> {
            if (!notification.isRead()) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    @Override
    public void createReservationConfirmedNotification(Reservation reservation) {
        String text = String.format(
                "Reserva confirmada: %s para %s · %s (%s-%s) · %s",
                reservation.getRoom(),
                reservation.getUser(),
                reservation.getDate().format(DATE_FORMATTER),
                reservation.getStart(),
                reservation.getEnd(),
                reservation.getSeatCode()
        );
        createNotification("reservation_confirmed", text, "var(--green)", reservation.getId(), reservation.getReservationGroupId());
    }

    @Override
    public void createReservationCancelledNotification(Reservation reservation) {
        String text = String.format(
                "Reserva cancelada: %s · %s (%s-%s) · %s",
                reservation.getRoom(),
                reservation.getDate().format(DATE_FORMATTER),
                reservation.getStart(),
                reservation.getEnd(),
                reservation.getSeatCode()
        );
        createNotification("reservation_cancelled", text, "var(--red)", reservation.getId(), reservation.getReservationGroupId());
    }

    @Override
    public void createReservationGroupConfirmedNotification(
            String reservationGroupId,
            String room,
            String date,
            String start,
            String end,
            int peopleCount
    ) {
        String text = String.format(
                "Reserva em lote confirmada: %s · %s (%s-%s) · %d pessoa(s)",
                room,
                date,
                start,
                end,
                peopleCount
        );
        createNotification("reservation_group_confirmed", text, "var(--green)", null, reservationGroupId);
    }

    @Override
    public void createReservationGroupCancelledNotification(
            String reservationGroupId,
            String room,
            String date,
            String start,
            String end,
            int peopleCount
    ) {
        String text = String.format(
                "Reserva em lote cancelada: %s · %s (%s-%s) · %d pessoa(s)",
                room,
                date,
                start,
                end,
                peopleCount
        );
        createNotification("reservation_group_cancelled", text, "var(--red)", null, reservationGroupId);
    }

    private void createNotification(String type, String text, String color, Long reservationId, String reservationGroupId) {
        notificationRepository.save(new Notification(
                null,
                type,
                text,
                color,
                false,
                LocalDateTime.now(),
                reservationId,
                reservationGroupId
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
                notification.getReservationGroupId()
        );
    }
}
