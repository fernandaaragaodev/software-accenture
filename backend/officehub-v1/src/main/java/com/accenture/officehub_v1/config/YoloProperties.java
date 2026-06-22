package com.accenture.officehub_v1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@ConfigurationProperties(prefix = "yolo")
public record YoloProperties(
        String baseUrl,
        Integer connectTimeoutMs,
        Integer readTimeoutMs,
        BigDecimal distanciaAgrupamentoMetros,
        BigDecimal distanciaMinimaEstacoesMetros,
        BigDecimal margemPerimetralMetros
) {

    public YoloProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://127.0.0.1:8001";
        }
        if (connectTimeoutMs == null || connectTimeoutMs <= 0) {
            connectTimeoutMs = 5000;
        }
        if (readTimeoutMs == null || readTimeoutMs <= 0) {
            readTimeoutMs = 120000;
        }
        if (distanciaAgrupamentoMetros == null || distanciaAgrupamentoMetros.compareTo(BigDecimal.ZERO) <= 0) {
            distanciaAgrupamentoMetros = new BigDecimal("2.0");
        }
        if (distanciaMinimaEstacoesMetros == null || distanciaMinimaEstacoesMetros.compareTo(BigDecimal.ZERO) <= 0) {
            distanciaMinimaEstacoesMetros = new BigDecimal("1.0");
        }
        if (margemPerimetralMetros == null || margemPerimetralMetros.compareTo(BigDecimal.ZERO) < 0) {
            margemPerimetralMetros = new BigDecimal("0.5");
        }
    }
}
