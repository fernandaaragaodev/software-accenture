package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.request.AtualizarCoordenadasPosicaoRequest;
import com.accenture.officehub_v1.dto.request.CriarLayoutRequest;
import com.accenture.officehub_v1.dto.response.LayoutResponse;
import com.accenture.officehub_v1.service.LayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LayoutController {

    private final LayoutService layoutService;

    @PostMapping("/api/v1/layouts")
    public ResponseEntity<LayoutResponse> criar(@Valid @RequestBody CriarLayoutRequest request) {
        LayoutResponse response = layoutService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/salas/{salaId}/layout/ativo")
    public ResponseEntity<LayoutResponse> buscarLayoutAtivo(@PathVariable UUID salaId) {
        return ResponseEntity.ok(layoutService.buscarLayoutAtivo(salaId));
    }

    @PatchMapping("/api/v1/layouts/{id}/aprovar")
    public ResponseEntity<LayoutResponse> aprovar(
            @PathVariable UUID id,
            @RequestHeader("X-Usuario-Id") UUID usuarioId) {
        return ResponseEntity.ok(layoutService.aprovar(id, usuarioId));
    }

    @PatchMapping("/api/v1/layouts/posicoes/{posicaoId}/coordenadas")
    public ResponseEntity<Void> ajustarCoordenadasPosicao(
            @PathVariable UUID posicaoId,
            @Valid @RequestBody AtualizarCoordenadasPosicaoRequest request) {
        layoutService.ajustarCoordenadasPosicao(posicaoId, request);
        return ResponseEntity.noContent().build();
    }
}
