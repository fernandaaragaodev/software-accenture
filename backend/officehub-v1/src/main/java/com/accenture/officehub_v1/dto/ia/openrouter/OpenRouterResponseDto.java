package com.accenture.officehub_v1.dto.ia.openrouter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenRouterResponseDto(
        @JsonProperty("choices") List<OpenRouterChoiceDto> choices,
        @JsonProperty("usage") OpenRouterUsageDto usage
) {
}
