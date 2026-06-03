package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.request.AtualizarSalaRequest;
import com.accenture.officehub_v1.dto.request.AtualizarStatusSalaRequest;
import com.accenture.officehub_v1.dto.request.CriarSalaRequest;
import com.accenture.officehub_v1.dto.response.SalaResponse;
import com.accenture.officehub_v1.service.SalaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/salas")
@RequiredArgsConstructor
public class SalaController {

    private final SalaService salaService;

    @PostMapping
    public ResponseEntity<SalaResponse> criar(
            @Valid @RequestBody CriarSalaRequest request,
            @RequestHeader("X-Usuario-Id") UUID usuarioId) {
        SalaResponse response = salaService.criar(request, usuarioId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<SalaResponse>> listar() {
        return ResponseEntity.ok(salaService.listarNaoDeletadas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalaResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(salaService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SalaResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarSalaRequest request) {
        return ResponseEntity.ok(salaService.atualizar(id, request));
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<SalaResponse> inativar(@PathVariable UUID id) {
        return ResponseEntity.ok(salaService.inativar(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SalaResponse> atualizarStatus(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarStatusSalaRequest request) {
        return ResponseEntity.ok(salaService.atualizarStatus(id, request));
    }
}
