package com.accenture.officehub_v1.dto.ia.openrouter;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record OpenRouterRequestDto(
        @JsonProperty("model") String model,
        @JsonProperty("messages") List<OpenRouterMessageDto> messages,
        @JsonProperty("temperature") double temperature,
        @JsonProperty("max_tokens") Integer maxTokens,
        @JsonProperty("response_format") Map<String, String> responseFormat
) {
}
