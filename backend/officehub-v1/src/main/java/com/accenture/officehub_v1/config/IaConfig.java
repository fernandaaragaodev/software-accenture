package com.accenture.officehub_v1.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(IaProperties.class)
public class IaConfig {

    @Bean
    public RestClient openRouterRestClient(IaProperties iaProperties) {
        return RestClient.builder()
                .baseUrl(iaProperties.openrouter().baseUrl())
                .build();
    }
}
