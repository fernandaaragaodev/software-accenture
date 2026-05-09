package com.accenture.officehub.officehub_api.dto;

import com.accenture.officehub.officehub_api.enums.ReservationStatus;

public record ReservationResponseDto(
        Long id,
        String room,
        String user,
        String requesterRole,
        String reservationGroupId,
        String seatCode,
        String seatType,
        java.util.List<String> requestedEquipment,
        String date,
        String start,
        String end,
        ReservationStatus status
) {}
