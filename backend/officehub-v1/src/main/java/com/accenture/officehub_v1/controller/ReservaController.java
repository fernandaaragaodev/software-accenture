package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.request.AceitarSugestaoReservaRequest;
import com.accenture.officehub_v1.dto.request.CancelarReservaRequest;
import com.accenture.officehub_v1.dto.request.RejeitarReservaRequest;
import com.accenture.officehub_v1.dto.request.SolicitarReservaRequest;
import com.accenture.officehub_v1.dto.request.SugestaoOutraAlocacaoRequest;
import com.accenture.officehub_v1.dto.response.PageResponse;
import com.accenture.officehub_v1.dto.response.ReservaResumoResponse;
import com.accenture.officehub_v1.dto.response.ReservaResponse;
import com.accenture.officehub_v1.dto.response.SugestaoAlocacaoResponse;
import com.accenture.officehub_v1.security.SecurityUtils;
import com.accenture.officehub_v1.service.ReservaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reservas")
@RequiredArgsConstructor
@Tag(name = "Reservas", description = "Solicitação e gestão de reservas de salas")
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping("/sugerir")
    @Operation(summary = "Sugerir alocação de posições via IA")
    public ResponseEntity<SugestaoAlocacaoResponse> sugerir(@Valid @RequestBody SolicitarReservaRequest request) {
        return ResponseEntity.ok(reservaService.sugerirAlocacao(
                request,
                SecurityUtils.getUsuarioIdAtual(),
                SecurityUtils.getPerfisAtuais()));
    }

    @PostMapping("/sugerir/outra")
    @Operation(summary = "Sugerir outra combinação de posições via IA")
    public ResponseEntity<SugestaoAlocacaoResponse> sugerirOutra(
            @Valid @RequestBody SugestaoOutraAlocacaoRequest request) {
        return ResponseEntity.ok(reservaService.sugerirOutraAlocacao(
                request,
                SecurityUtils.getUsuarioIdAtual(),
                SecurityUtils.getPerfisAtuais()));
    }

    @PostMapping
    @Operation(
            summary = "Aceitar sugestão e criar reserva pendente",
            description = """
                    Cria uma reserva PENDENTE com as posições da sugestão aceita. \
                    A confirmação final deve ser feita em PATCH /reservas/{id}/confirmar.""")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Reserva pendente criada com posições sugeridas",
                    content = @Content(schema = @Schema(implementation = ReservaResponse.class))),
            @ApiResponse(responseCode = "409", description = "Sugestão inválida ou indisponível"),
            @ApiResponse(responseCode = "422", description = "Regra de negócio violada")
    })
    public ResponseEntity<ReservaResponse> solicitar(@Valid @RequestBody AceitarSugestaoReservaRequest request) {
        UUID solicitanteId = SecurityUtils.getUsuarioIdAtual();
        ReservaResponse response = reservaService.solicitarReserva(
                request,
                solicitanteId,
                SecurityUtils.getPerfisAtuais());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<ReservaResumoResponse>> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam(defaultValue = "false") boolean canceladas,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return ResponseEntity.ok(reservaService.listarReservas(
                data,
                canceladas,
                page,
                size,
                SecurityUtils.getUsuarioIdAtual(),
                SecurityUtils.getPerfisAtuais()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(reservaService.buscarPorId(
                id, SecurityUtils.getUsuarioIdAtual(), SecurityUtils.getPerfisAtuais()));
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<ReservaResponse> confirmar(@PathVariable UUID id) {
        return ResponseEntity.ok(reservaService.confirmarReserva(
                id, SecurityUtils.getUsuarioIdAtual(), SecurityUtils.getPerfisAtuais()));
    }

    @PatchMapping("/{id}/rejeitar")
    public ResponseEntity<ReservaResponse> rejeitar(
            @PathVariable UUID id,
            @Valid @RequestBody RejeitarReservaRequest request) {
        return ResponseEntity.ok(reservaService.rejeitarReserva(
                id, request.motivo(), SecurityUtils.getUsuarioIdAtual(), SecurityUtils.getPerfisAtuais()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservaResponse> cancelar(
            @PathVariable UUID id,
            @Valid @RequestBody CancelarReservaRequest request) {
        return ResponseEntity.ok(reservaService.cancelarReserva(
                id,
                request,
                SecurityUtils.getUsuarioIdAtual(),
                SecurityUtils.getPerfisAtuais()));
    }
}
