package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.response.EspecialidadeResponse;
import com.accenture.officehub_v1.service.EspecialidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/especialidades")
@RequiredArgsConstructor
public class EspecialidadeController {

    private final EspecialidadeService especialidadeService;

    @GetMapping
    public ResponseEntity<List<EspecialidadeResponse>> listar() {
        return ResponseEntity.ok(especialidadeService.listar());
    }
}
