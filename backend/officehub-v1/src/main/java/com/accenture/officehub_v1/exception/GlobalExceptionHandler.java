package com.accenture.officehub_v1.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleRecursoNaoEncontrado(RecursoNaoEncontradoException ex) {
        return resposta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleRegraNegocio(RegraNegocioException ex) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(ConflitoAlocacaoException.class)
    public ResponseEntity<Map<String, Object>> handleConflitoAlocacao(ConflitoAlocacaoException ex) {
        return resposta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<Map<String, Object>> handleCredenciaisInvalidas(CredenciaisInvalidasException ex) {
        return resposta(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<Map<String, Object>> handleAcessoNegado(AcessoNegadoException ex) {
        return resposta(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacao(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return resposta(HttpStatus.BAD_REQUEST, mensagem);
    }

    private ResponseEntity<Map<String, Object>> resposta(HttpStatus status, String mensagem) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", status.value(),
                "mensagem", mensagem
        ));
    }
}
