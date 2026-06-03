package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.request.TipoEquipamentoRequest;
import com.accenture.officehub_v1.dto.response.TipoEquipamentoResponse;
import com.accenture.officehub_v1.service.TipoEquipamentoService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tipos-equipamento")
@RequiredArgsConstructor
public class TipoEquipamentoController {

    private final TipoEquipamentoService tipoEquipamentoService;

    @PostMapping
    public ResponseEntity<TipoEquipamentoResponse> criar(@Valid @RequestBody TipoEquipamentoRequest request) {
        TipoEquipamentoResponse response = tipoEquipamentoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TipoEquipamentoResponse>> listar() {
        return ResponseEntity.ok(tipoEquipamentoService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoEquipamentoResponse> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(tipoEquipamentoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoEquipamentoResponse> atualizar(
            @PathVariable UUID id,
            @Valid @RequestBody TipoEquipamentoRequest request) {
        return ResponseEntity.ok(tipoEquipamentoService.atualizar(id, request));
    }

    @PatchMapping("/{id}/inativar")
    public ResponseEntity<TipoEquipamentoResponse> inativar(@PathVariable UUID id) {
        return ResponseEntity.ok(tipoEquipamentoService.inativar(id));
    }
}
