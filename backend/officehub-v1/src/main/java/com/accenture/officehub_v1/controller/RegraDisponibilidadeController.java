package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.request.AtribuirRegraSalaRequest;
import com.accenture.officehub_v1.dto.request.CriarRegraDisponibilidadeIndependenteRequest;
import com.accenture.officehub_v1.dto.response.RegraDisponibilidadeResponse;
import com.accenture.officehub_v1.service.DisponibilidadeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/regras-disponibilidade")
@RequiredArgsConstructor
public class RegraDisponibilidadeController {

    private final DisponibilidadeService disponibilidadeService;

    @PostMapping
    public ResponseEntity<RegraDisponibilidadeResponse> criar(
            @Valid @RequestBody CriarRegraDisponibilidadeIndependenteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(disponibilidadeService.criarRegraIndependente(request));
    }

    @GetMapping
    public ResponseEntity<List<RegraDisponibilidadeResponse>> listar() {
        return ResponseEntity.ok(disponibilidadeService.listarRegras());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegraDisponibilidadeResponse> buscar(@PathVariable UUID id) {
        return ResponseEntity.ok(disponibilidadeService.buscarRegra(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegraDisponibilidadeResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody CriarRegraDisponibilidadeIndependenteRequest request) {
        return ResponseEntity.ok(disponibilidadeService.atualizarRegra(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable UUID id) {
        disponibilidadeService.excluirRegra(id);
        return ResponseEntity.noContent().build();
    }
}
