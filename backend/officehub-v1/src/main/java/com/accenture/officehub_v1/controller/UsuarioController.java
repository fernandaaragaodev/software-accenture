package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.response.UsuarioResponse;
import com.accenture.officehub_v1.dto.response.UsuarioResumoResponse;
import com.accenture.officehub_v1.security.SecurityUtils;
import com.accenture.officehub_v1.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    @GetMapping("/gestores")
    public ResponseEntity<List<UsuarioResumoResponse>> listarGestores() {
        return ResponseEntity.ok(usuarioService.listarGestores());
    }

    @GetMapping("/disponiveis-equipe")
    public ResponseEntity<List<UsuarioResumoResponse>> listarDisponiveisParaEquipe() {
        return ResponseEntity.ok(usuarioService.listarDisponiveisParaEquipe());
    }

    @GetMapping("/membros-equipe")
    public ResponseEntity<List<UsuarioResumoResponse>> listarMembrosEquipeDoGestor() {
        return ResponseEntity.ok(usuarioService.listarMembrosEquipeDoGestor(
                SecurityUtils.getUsuarioIdAtual(),
                SecurityUtils.getPerfisAtuais()));
    }
}
