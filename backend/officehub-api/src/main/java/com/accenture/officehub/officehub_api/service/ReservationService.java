package com.accenture.officehub.officehub_api.service;

import com.accenture.officehub.officehub_api.dto.ReservationRequestDto;
import com.accenture.officehub.officehub_api.dto.ReservationResponseDto;
import com.accenture.officehub.officehub_api.dto.ReservationGroupResponseDto;

import java.util.List;

public interface ReservationService {
    List<ReservationResponseDto> listReservations(String requesterName, String requesterRole);
    List<ReservationGroupResponseDto> listReservationGroups(String requesterName, String requesterRole);
    ReservationGroupResponseDto getReservationGroup(String groupId, String requesterName, String requesterRole);
    ReservationResponseDto createReservation(ReservationRequestDto request);
    List<ReservationResponseDto> createReservationsBatch(List<ReservationRequestDto> requests);
    void cancelReservation(Long reservationId, String cancellerName, String cancellerRole);
    void cancelReservationGroup(String groupId, String cancellerName, String cancellerRole);
    void synchronizeStatuses();
}
