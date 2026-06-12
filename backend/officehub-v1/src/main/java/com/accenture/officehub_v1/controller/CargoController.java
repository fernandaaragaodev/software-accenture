package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.response.CargoResponse;
import com.accenture.officehub_v1.service.CargoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cargos")
@RequiredArgsConstructor
public class CargoController {

    private final CargoService cargoService;

    @GetMapping
    public ResponseEntity<List<CargoResponse>> listar() {
        return ResponseEntity.ok(cargoService.listar());
    }
}
