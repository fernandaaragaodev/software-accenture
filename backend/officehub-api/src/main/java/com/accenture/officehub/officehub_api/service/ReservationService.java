package com.accenture.officehub.officehub_api.service;

import com.accenture.officehub.officehub_api.dto.ReservationRequestDto;
import com.accenture.officehub.officehub_api.dto.ReservationResponseDto;

import java.util.List;

public interface ReservationService {
    List<ReservationResponseDto> listReservations();
    ReservationResponseDto createReservation(ReservationRequestDto request);
    void cancelReservation(Long reservationId);
    void synchronizeStatuses();
}
