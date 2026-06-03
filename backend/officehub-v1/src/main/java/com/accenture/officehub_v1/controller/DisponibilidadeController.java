package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.request.CriarRegraDisponibilidadeRequest;
import com.accenture.officehub_v1.dto.request.ExcecaoDisponibilidadeRequest;
import com.accenture.officehub_v1.dto.response.RegraDisponibilidadeResponse;
import com.accenture.officehub_v1.dto.response.ValidacaoDisponibilidadeResponse;
import com.accenture.officehub_v1.service.DisponibilidadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/salas/{salaId}")
@RequiredArgsConstructor
public class DisponibilidadeController {

    private final DisponibilidadeService disponibilidadeService;

    @PostMapping("/regras-disponibilidade")
    public ResponseEntity<RegraDisponibilidadeResponse> criarRegra(
            @PathVariable UUID salaId,
            @Valid @RequestBody CriarRegraDisponibilidadeRequest request) {
        RegraDisponibilidadeResponse response = disponibilidadeService.criarRegra(salaId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/regras-disponibilidade")
    public ResponseEntity<RegraDisponibilidadeResponse> buscarRegra(@PathVariable UUID salaId) {
        return ResponseEntity.ok(disponibilidadeService.buscarRegraPorSala(salaId));
    }

    @GetMapping("/disponibilidade")
    public ResponseEntity<ValidacaoDisponibilidadeResponse> consultarDisponibilidade(
            @PathVariable UUID salaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(disponibilidadeService.validarReservaPermitida(salaId, data));
    }

    @PostMapping("/excecoes-disponibilidade")
    public ResponseEntity<Void> adicionarExcecao(
            @PathVariable UUID salaId,
            @Valid @RequestBody ExcecaoDisponibilidadeRequest request,
            @RequestHeader("X-Usuario-Id") UUID usuarioId) {
        disponibilidadeService.adicionarExcecao(salaId, request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
