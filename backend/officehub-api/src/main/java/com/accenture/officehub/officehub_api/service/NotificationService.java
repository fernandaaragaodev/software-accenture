package com.accenture.officehub.officehub_api.service;

import com.accenture.officehub.officehub_api.dto.NotificationResponseDto;
import com.accenture.officehub.officehub_api.model.Reservation;

import java.util.List;

public interface NotificationService {
    List<NotificationResponseDto> listNotifications(String viewerName, String viewerRole);

    void markAsRead(Long notificationId, String viewerName, String viewerRole);

    void markAllAsRead(String viewerName, String viewerRole);

    void createReservationConfirmedNotification(Reservation reservation);

    void createReservationCancelledNotification(Reservation reservation, String actorUserName);

    void createReservationGroupConfirmedNotification(
            String reservationGroupId,
            String room,
            String date,
            String start,
            String end,
            int peopleCount,
            String actorUserName
    );

    void createReservationGroupCancelledNotification(
            String reservationGroupId,
            String room,
            String date,
            String start,
            String end,
            int peopleCount,
            String actorUserName
    );
}
