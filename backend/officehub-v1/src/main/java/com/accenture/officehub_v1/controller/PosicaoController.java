package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.request.AtualizarCoordenadasPosicaoRequest;
import com.accenture.officehub_v1.dto.request.CriarPosicaoRequest;
import com.accenture.officehub_v1.dto.response.PosicaoResponse;
import com.accenture.officehub_v1.service.PosicaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PosicaoController {

    private final PosicaoService posicaoService;

    @GetMapping("/api/v1/salas/{salaId}/posicoes")
    public ResponseEntity<List<PosicaoResponse>> listarPorSala(@PathVariable UUID salaId) {
        return ResponseEntity.ok(posicaoService.listarPorSala(salaId));
    }

    @PostMapping("/api/v1/posicoes")
    public ResponseEntity<PosicaoResponse> criar(@Valid @RequestBody CriarPosicaoRequest request) {
        PosicaoResponse response = posicaoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/v1/posicoes/{id}")
    public ResponseEntity<PosicaoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(posicaoService.buscarPorId(id));
    }

    @PatchMapping("/api/v1/posicoes/{id}/coordenadas")
    public ResponseEntity<PosicaoResponse> atualizarCoordenadas(
            @PathVariable UUID id,
            @Valid @RequestBody AtualizarCoordenadasPosicaoRequest request) {
        return ResponseEntity.ok(posicaoService.atualizarCoordenadas(id, request));
    }

    @PatchMapping("/api/v1/posicoes/{id}/inativar")
    public ResponseEntity<PosicaoResponse> inativar(@PathVariable UUID id) {
        return ResponseEntity.ok(posicaoService.inativar(id));
    }
}
