package com.accenture.officehub_v1.service.ia.motor;

public class OpenRouterIndisponivelException extends RuntimeException {

    public OpenRouterIndisponivelException(String message) {
        super(message);
    }

    public OpenRouterIndisponivelException(String message, Throwable cause) {
        super(message, cause);
    }
}
