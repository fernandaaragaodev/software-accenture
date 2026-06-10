package com.accenture.officehub_v1.dto.ia.openrouter;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OpenRouterMessageDto(
        @JsonProperty("role") String role,
        @JsonProperty("content") String content
) {
}
