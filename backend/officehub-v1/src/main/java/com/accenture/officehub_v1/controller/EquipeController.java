package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.request.AdicionarMembroEquipeRequest;
import com.accenture.officehub_v1.dto.request.CriarEquipeRequest;
import com.accenture.officehub_v1.dto.response.EquipeResumoResponse;
import com.accenture.officehub_v1.dto.response.EquipeResponse;
import com.accenture.officehub_v1.security.SecurityUtils;
import com.accenture.officehub_v1.service.EquipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/equipes")
@RequiredArgsConstructor
public class EquipeController {

    private final EquipeService equipeService;

    @PostMapping
    public ResponseEntity<EquipeResponse> criar(@Valid @RequestBody CriarEquipeRequest request) {
        EquipeResponse response = equipeService.criar(
                request,
                SecurityUtils.getUsuarioIdAtual(),
                SecurityUtils.getPerfisAtuais());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<EquipeResumoResponse>> listar() {
        return ResponseEntity.ok(equipeService.listar(
                SecurityUtils.getUsuarioIdAtual(),
                SecurityUtils.getPerfisAtuais()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipeResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(equipeService.buscarPorId(
                id,
                SecurityUtils.getUsuarioIdAtual(),
                SecurityUtils.getPerfisAtuais()));
    }

    @PostMapping("/{id}/membros")
    public ResponseEntity<EquipeResponse> adicionarMembro(
            @PathVariable UUID id,
            @Valid @RequestBody AdicionarMembroEquipeRequest request) {
        return ResponseEntity.ok(equipeService.adicionarMembro(
                id,
                request,
                SecurityUtils.getUsuarioIdAtual(),
                SecurityUtils.getPerfisAtuais()));
    }

    @DeleteMapping("/{id}/membros/{usuarioId}")
    public ResponseEntity<EquipeResponse> removerMembro(
            @PathVariable UUID id,
            @PathVariable UUID usuarioId) {
        return ResponseEntity.ok(equipeService.removerMembro(
                id,
                usuarioId,
                SecurityUtils.getUsuarioIdAtual(),
                SecurityUtils.getPerfisAtuais()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desmembrar(@PathVariable UUID id) {
        equipeService.desmembrar(
                id,
                SecurityUtils.getUsuarioIdAtual(),
                SecurityUtils.getPerfisAtuais());
        return ResponseEntity.noContent().build();
    }
}
