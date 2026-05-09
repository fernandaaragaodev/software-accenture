package com.accenture.officehub.officehub_api.service;

import com.accenture.officehub.officehub_api.dto.ReservationRequestDto;
import com.accenture.officehub.officehub_api.dto.ReservationResponseDto;
import com.accenture.officehub.officehub_api.dto.ReservationGroupResponseDto;

import java.util.List;

public interface ReservationService {
    List<ReservationResponseDto> listReservations();
    List<ReservationGroupResponseDto> listReservationGroups();
    ReservationGroupResponseDto getReservationGroup(String groupId);
    ReservationResponseDto createReservation(ReservationRequestDto request);
    List<ReservationResponseDto> createReservationsBatch(List<ReservationRequestDto> requests);
    void cancelReservation(Long reservationId, String cancellerName, String cancellerRole);
    void cancelReservationGroup(String groupId, String cancellerName, String cancellerRole);
    void synchronizeStatuses();
}
