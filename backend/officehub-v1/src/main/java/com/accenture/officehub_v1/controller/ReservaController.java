package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.request.CancelarReservaRequest;
import com.accenture.officehub_v1.dto.request.RejeitarReservaRequest;
import com.accenture.officehub_v1.dto.request.SolicitarReservaRequest;
import com.accenture.officehub_v1.dto.response.ReservaResponse;
import com.accenture.officehub_v1.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<ReservaResponse> solicitar(
            @Valid @RequestBody SolicitarReservaRequest request,
            @RequestHeader("X-Usuario-Id") UUID solicitanteId) {
        ReservaResponse response = reservaService.solicitarReserva(request, solicitanteId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(reservaService.buscarPorId(id));
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<ReservaResponse> confirmar(@PathVariable UUID id) {
        return ResponseEntity.ok(reservaService.confirmarReserva(id));
    }

    @PatchMapping("/{id}/rejeitar")
    public ResponseEntity<ReservaResponse> rejeitar(
            @PathVariable UUID id,
            @Valid @RequestBody RejeitarReservaRequest request) {
        return ResponseEntity.ok(reservaService.rejeitarReserva(id, request.motivo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservaResponse> cancelar(
            @PathVariable UUID id,
            @Valid @RequestBody CancelarReservaRequest request,
            @RequestHeader("X-Usuario-Id") UUID canceladoPorId) {
        return ResponseEntity.ok(reservaService.cancelarReserva(id, request, canceladoPorId));
    }
}
