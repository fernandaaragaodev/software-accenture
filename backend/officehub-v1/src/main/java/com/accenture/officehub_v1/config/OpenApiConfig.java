package com.accenture.officehub_v1.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI officeHubOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("OfficeHub API")
                        .description("""
                                API do sistema de reservas de salas. \
                                Inclui o Agente de Alocação (IA algorítmica espacial — \
                                versão ALGORITMO_ESPACIAL_V1) integrado ao fluxo de criação de reservas.""")
                        .version("v1")
                        .contact(new Contact().name("OfficeHub").email("suporte@officehub.local")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtido em POST /api/v1/auth/login")));
    }
}
