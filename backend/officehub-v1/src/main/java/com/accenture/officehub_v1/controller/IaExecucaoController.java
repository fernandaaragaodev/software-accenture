package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.response.AgenteExecucaoResponse;
import com.accenture.officehub_v1.entity.enums.StatusAgente;
import com.accenture.officehub_v1.service.ia.AgenteExecucaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/ia/execucoes")
@RequiredArgsConstructor
@Tag(name = "IA — Execuções", description = "Consulta administrativa dos logs do agente de IA algorítmica")
public class IaExecucaoController {

    private final AgenteExecucaoService agenteExecucaoService;

    @GetMapping
    @Operation(
            summary = "Listar execuções da IA",
            description = """
                    Retorna o histórico de execuções dos agentes de IA (ex.: alocação espacial). \
                    Filtros opcionais por tipo de agente, status e intervalo de datas.""")
    public ResponseEntity<List<AgenteExecucaoResponse>> listar(
            @Parameter(description = "Tipo do agente, ex.: ALOCACAO")
            @RequestParam(required = false) String tipoAgente,
            @Parameter(description = "Status da execução: SUCESSO ou FALHA")
            @RequestParam(required = false) StatusAgente status,
            @Parameter(description = "Data inicial do intervalo (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @Parameter(description = "Data final do intervalo (inclusive)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim) {

        return ResponseEntity.ok(agenteExecucaoService.listar(tipoAgente, status, dataInicio, dataFim));
    }
}
