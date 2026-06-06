package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.response.DashboardStatsResponse;
import com.accenture.officehub_v1.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsResponse> obterDashboard() {
        return ResponseEntity.ok(dashboardService.obterEstatisticas());
    }
}
