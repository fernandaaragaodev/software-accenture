package com.accenture.officehub.officehub_api.dto;

import com.accenture.officehub.officehub_api.enums.ReservationStatus;

import java.util.List;

public record ReservationGroupResponseDto(
        String groupId,
        boolean isBatch,
        String room,
        String requesterRole,
        String date,
        String start,
        String end,
        ReservationStatus status,
        int peopleCount,
        List<ReservationGroupMemberDto> members
) {}

