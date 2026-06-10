package com.accenture.officehub_v1.dto.ia.openrouter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenRouterChoiceDto(
        @JsonProperty("message") OpenRouterMessageDto message
) {
}
