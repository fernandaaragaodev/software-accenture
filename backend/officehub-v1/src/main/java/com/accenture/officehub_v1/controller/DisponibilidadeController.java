package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.request.AtribuirRegraSalaRequest;
import com.accenture.officehub_v1.dto.request.CriarRegraDisponibilidadeRequest;
import com.accenture.officehub_v1.dto.request.ExcecaoDisponibilidadeRequest;
import com.accenture.officehub_v1.dto.response.ConsultaDisponibilidadeResponse;
import com.accenture.officehub_v1.dto.response.RegraDisponibilidadeResponse;
import com.accenture.officehub_v1.security.SecurityUtils;
import com.accenture.officehub_v1.service.DisponibilidadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
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

    @PutMapping("/regra-disponibilidade")
    public ResponseEntity<RegraDisponibilidadeResponse> atribuirRegra(
            @PathVariable UUID salaId,
            @Valid @RequestBody AtribuirRegraSalaRequest request) {
        return ResponseEntity.ok(disponibilidadeService.atribuirRegraSala(salaId, request));
    }

    @DeleteMapping("/regra-disponibilidade")
    public ResponseEntity<Void> desatribuirRegra(@PathVariable UUID salaId) {
        disponibilidadeService.desatribuirRegraSala(salaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/disponibilidade")
    public ResponseEntity<ConsultaDisponibilidadeResponse> consultarDisponibilidade(
            @PathVariable UUID salaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime horaFim) {
        return ResponseEntity.ok(
                disponibilidadeService.consultarDisponibilidade(salaId, data, horaInicio, horaFim));
    }

    @PostMapping("/excecoes-disponibilidade")
    public ResponseEntity<Void> adicionarExcecao(
            @PathVariable UUID salaId,
            @Valid @RequestBody ExcecaoDisponibilidadeRequest request) {
        UUID usuarioId = SecurityUtils.getUsuarioIdAtual();
        disponibilidadeService.adicionarExcecao(salaId, request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
