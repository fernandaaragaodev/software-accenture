package com.accenture.officehub.officehub_api.dto;

import com.accenture.officehub.officehub_api.enums.RoomStatus;

import java.util.List;

public record AvailableRoomForReservationDto(
        Long id,
        String name,
        Integer capacity,
        Integer desks,
        RoomStatus status,
        List<String> equipment,
        String floor,
        Integer area,
        int matchingAvailablePositions
) {}
