package com.accenture.officehub_v1.service.ia.motor;

import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteSaidaDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public final class OpenRouterRespostaParser {

    private OpenRouterRespostaParser() {
    }

    static String extrairJson(String content) {
        if (content == null) {
            return "";
        }

        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence = trimmed.lastIndexOf("```");
            if (firstNewline > 0 && lastFence > firstNewline) {
                trimmed = trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }

        return trimmed;
    }

    static AlocacaoAgenteSaidaDto parsear(ObjectMapper objectMapper, String content)
            throws JsonProcessingException {
        String json = extrairJson(content);
        AlocacaoAgenteSaidaDto saida = objectMapper.readValue(json, AlocacaoAgenteSaidaDto.class);
        return normalizar(saida, objectMapper.readTree(json));
    }

    static AlocacaoAgenteSaidaDto normalizar(AlocacaoAgenteSaidaDto saida, JsonNode json) {
        if (saida == null) {
            return null;
        }

        boolean sucessoExplicito = json != null && json.has("sucesso");
        boolean sucesso = saida.sucesso();

        if (!sucessoExplicito && saida.alocacoes() != null && !saida.alocacoes().isEmpty()) {
            sucesso = true;
        }

        Integer scoreTotal = saida.scoreTotal();
        if (scoreTotal == null && sucesso) {
            scoreTotal = 0;
        }

        return new AlocacaoAgenteSaidaDto(
                sucesso,
                scoreTotal,
                saida.motivoFalha(),
                saida.avisoProximidade(),
                saida.alocacoes() != null ? saida.alocacoes() : List.of(),
                saida.tokensUtilizados());
    }
}
