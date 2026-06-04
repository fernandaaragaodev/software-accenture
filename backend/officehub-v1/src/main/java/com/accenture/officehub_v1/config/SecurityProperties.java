package com.accenture.officehub_v1.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(
        Jwt jwt,
        RefreshToken refreshToken,
        RateLimit rateLimit
) {

    public record Jwt(String secret, int expirationMinutes) {
    }

    public record RefreshToken(int expirationDays) {
    }

    public record RateLimit(int loginPerMinute) {
    }
}
