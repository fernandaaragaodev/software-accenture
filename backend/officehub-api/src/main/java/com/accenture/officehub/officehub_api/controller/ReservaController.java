package com.accenture.officehub.officehub_api.controller;

import com.accenture.officehub.officehub_api.dto.ReservaRequest;
import com.accenture.officehub.officehub_api.dto.ReservaResponse;
import com.accenture.officehub.officehub_api.service.ReservaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservaResponse> criar(@RequestBody ReservaRequest request) {
        return ResponseEntity.status(201).body(service.criar(request));
    }

    @GetMapping
    public List<ReservaResponse> listar() {
        return service.listar();
    }

    @DeleteMapping("/{id}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable String id) {
        service.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}