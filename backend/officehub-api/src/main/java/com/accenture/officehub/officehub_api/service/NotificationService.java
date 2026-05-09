package com.accenture.officehub.officehub_api.service;

import com.accenture.officehub.officehub_api.dto.NotificationResponseDto;
import com.accenture.officehub.officehub_api.model.Reservation;

import java.util.List;

public interface NotificationService {
    List<NotificationResponseDto> listNotifications();
    void markAsRead(Long notificationId);
    void markAllAsRead();
    void createReservationConfirmedNotification(Reservation reservation);
    void createReservationCancelledNotification(Reservation reservation);
    void createReservationGroupConfirmedNotification(
            String reservationGroupId,
            String room,
            String date,
            String start,
            String end,
            int peopleCount
    );
    void createReservationGroupCancelledNotification(
            String reservationGroupId,
            String room,
            String date,
            String start,
            String end,
            int peopleCount
    );
}
