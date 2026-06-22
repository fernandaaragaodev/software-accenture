package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.response.GerarLayoutPorIaResponse;
import com.accenture.officehub_v1.service.ia.layout.AiLayoutGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
public class AiLayoutController {

    private final AiLayoutGenerationService aiLayoutGenerationService;

    @PostMapping(value = "/api/v1/layouts/gerar-por-ia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GerarLayoutPorIaResponse> gerarPorIa(
            @RequestParam("nomeSala") String nomeSala,
            @RequestParam("largura") BigDecimal largura,
            @RequestParam("altura") BigDecimal altura,
            @RequestParam("imagem") MultipartFile imagem) {

        GerarLayoutPorIaResponse response = aiLayoutGenerationService.gerar(nomeSala, largura, altura, imagem);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
