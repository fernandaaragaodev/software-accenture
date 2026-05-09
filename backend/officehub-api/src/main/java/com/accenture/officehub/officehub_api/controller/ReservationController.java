package com.accenture.officehub.officehub_api.controller;

import com.accenture.officehub.officehub_api.dto.BatchReservationRequestDto;
import com.accenture.officehub.officehub_api.dto.ReservationRequestDto;
import com.accenture.officehub.officehub_api.dto.ReservationResponseDto;
import com.accenture.officehub.officehub_api.dto.ReservationGroupResponseDto;
import com.accenture.officehub.officehub_api.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponseDto>> listReservations() {
        return ResponseEntity.ok(reservationService.listReservations());
    }

    @GetMapping("/groups")
    public ResponseEntity<List<ReservationGroupResponseDto>> listReservationGroups() {
        return ResponseEntity.ok(reservationService.listReservationGroups());
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<ReservationGroupResponseDto> getReservationGroup(@PathVariable("groupId") String groupId) {
        return ResponseEntity.ok(reservationService.getReservationGroup(groupId));
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> createReservation(@Valid @RequestBody ReservationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationService.createReservation(request));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<ReservationResponseDto>> createReservationsBatch(
            @Valid @RequestBody BatchReservationRequestDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservationsBatch(request.reservations()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelReservation(
            @PathVariable("id") Long id,
            @RequestParam("requesterName") String requesterName,
            @RequestParam("requesterRole") String requesterRole
    ) {
        reservationService.cancelReservation(id, requesterName, requesterRole);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<Void> cancelReservationGroup(
            @PathVariable("groupId") String groupId,
            @RequestParam("requesterName") String requesterName,
            @RequestParam("requesterRole") String requesterRole
    ) {
        reservationService.cancelReservationGroup(groupId, requesterName, requesterRole);
        return ResponseEntity.noContent().build();
    }
}
