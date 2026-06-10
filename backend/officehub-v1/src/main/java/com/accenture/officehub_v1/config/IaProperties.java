package com.accenture.officehub_v1.config;

import com.accenture.officehub_v1.service.ia.motor.TipoMotorAlocacao;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ia")
public record IaProperties(
        TipoMotorAlocacao motor,
        OpenRouter openrouter
) {

    public IaProperties {
        if (motor == null) {
            motor = TipoMotorAlocacao.OPENROUTER;
        }
    }

    public record OpenRouter(
            String apiKey,
            String baseUrl,
            String model,
            Integer maxTokens,
            String httpReferer,
            String title
    ) {
        public OpenRouter {
            if (baseUrl == null || baseUrl.isBlank()) {
                baseUrl = "https://openrouter.ai/api/v1/chat/completions";
            }
            if (model == null || model.isBlank()) {
                model = "google/gemini-2.5-flash";
            }
            if (maxTokens == null || maxTokens <= 0) {
                maxTokens = 2000;
            }
            if (httpReferer == null || httpReferer.isBlank()) {
                httpReferer = "http://localhost";
            }
            if (title == null || title.isBlank()) {
                title = "OfficeHub";
            }
        }
    }
}
