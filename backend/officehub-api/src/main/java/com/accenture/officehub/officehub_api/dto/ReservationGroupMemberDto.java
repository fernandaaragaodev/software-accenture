package com.accenture.officehub.officehub_api.dto;

import com.accenture.officehub.officehub_api.enums.ReservationStatus;

import java.util.List;

public record ReservationGroupMemberDto(
        Long reservationId,
        String user,
        String seatCode,
        String seatType,
        List<String> requestedEquipment,
        ReservationStatus status
) {}

