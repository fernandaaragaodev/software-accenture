package com.accenture.officehub.officehub_api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record BatchReservationRequestDto(
        @NotEmpty List<@Valid ReservationRequestDto> reservations
) {}
