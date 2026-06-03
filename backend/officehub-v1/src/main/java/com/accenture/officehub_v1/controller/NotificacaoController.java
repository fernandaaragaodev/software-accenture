package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.response.NotificacaoResponse;
import com.accenture.officehub_v1.service.NotificacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final NotificacaoService notificacaoService;

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<NotificacaoResponse>> listarPorUsuario(@PathVariable UUID usuarioId) {
        return ResponseEntity.ok(notificacaoService.listarPorUsuario(usuarioId));
    }

    @PostMapping("/processar-fila")
    public ResponseEntity<Void> processarFilaPendente() {
        notificacaoService.processarFilaPendente();
        return ResponseEntity.accepted().build();
    }
}
