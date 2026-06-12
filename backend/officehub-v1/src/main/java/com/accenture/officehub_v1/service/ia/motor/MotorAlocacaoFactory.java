package com.accenture.officehub_v1.service.ia.motor;

import com.accenture.officehub_v1.config.IaProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MotorAlocacaoFactory {

    private final IaProperties iaProperties;
    private final MotorAlocacaoOpenRouter motorAlocacaoOpenRouter;
    private final MotorAlocacaoEspacial motorAlocacaoEspacial;

    public MotorAlocacao obterMotorPrincipal() {
        return iaProperties.motor() == TipoMotorAlocacao.ESPACIAL
                ? motorAlocacaoEspacial
                : motorAlocacaoOpenRouter;
    }

    public MotorAlocacao obterMotorFallback() {
        return motorAlocacaoEspacial;
    }

    public boolean usaOpenRouterComoPrincipal() {
        return iaProperties.motor() == TipoMotorAlocacao.OPENROUTER;
    }

    public MotorAlocacaoOpenRouter obterMotorOpenRouter() {
        return motorAlocacaoOpenRouter;
    }
}
