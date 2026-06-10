package com.accenture.officehub_v1.service.ia.motor;

import com.accenture.officehub_v1.config.IaProperties;
import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteSaidaDto;
import com.accenture.officehub_v1.dto.ia.PosicaoAlocadaSaidaDto;
import com.accenture.officehub_v1.dto.ia.openrouter.OpenRouterChoiceDto;
import com.accenture.officehub_v1.dto.ia.openrouter.OpenRouterMessageDto;
import com.accenture.officehub_v1.dto.ia.openrouter.OpenRouterResponseDto;
import com.accenture.officehub_v1.dto.ia.openrouter.OpenRouterUsageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class MotorAlocacaoOpenRouterTest {

    @Mock
    private RestClient openRouterRestClient;

    private MotorAlocacaoOpenRouter motor;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        IaProperties iaProperties = new IaProperties(
                null,
                new IaProperties.OpenRouter("test-key", null, null, null, null));

        motor = new MotorAlocacaoOpenRouter(
                openRouterRestClient,
                iaProperties,
                objectMapper,
                new AlocacaoRespostaValidador());
    }

    @Test
    void deveParsearRespostaJsonDoOpenRouter() throws Exception {
        UUID pessoaId = UUID.fromString("10000000-0000-0000-0000-000000000010");
        UUID posicaoId = UUID.fromString("10000000-0000-0000-0000-000000000015");

        String json = """
                {
                  "sucesso": true,
                  "scoreTotal": 280,
                  "alocacoes": [
                    {
                      "pessoaId": "%s",
                      "posicaoId": "%s"
                    }
                  ]
                }
                """.formatted(pessoaId, posicaoId);

        OpenRouterResponseDto response = new OpenRouterResponseDto(
                List.of(new OpenRouterChoiceDto(new OpenRouterMessageDto("assistant", json))),
                new OpenRouterUsageDto(100, 50, 150));

        AlocacaoAgenteSaidaDto saida = motor.parsearResposta(response);

        assertThat(saida.sucesso()).isTrue();
        assertThat(saida.scoreTotal()).isEqualTo(280);
        assertThat(saida.alocacoes()).containsExactly(new PosicaoAlocadaSaidaDto(pessoaId, posicaoId));
    }

    @Test
    void deveParsearRespostaComMarkdown() throws Exception {
        UUID pessoaId = UUID.fromString("10000000-0000-0000-0000-000000000010");
        UUID posicaoId = UUID.fromString("10000000-0000-0000-0000-000000000015");

        String json = """
                ```json
                {
                  "sucesso": true,
                  "scoreTotal": 120,
                  "alocacoes": [
                    { "pessoaId": "%s", "posicaoId": "%s" }
                  ]
                }
                ```
                """.formatted(pessoaId, posicaoId);

        OpenRouterResponseDto response = new OpenRouterResponseDto(
                List.of(new OpenRouterChoiceDto(new OpenRouterMessageDto("assistant", json))),
                new OpenRouterUsageDto(100, 50, 150));

        AlocacaoAgenteSaidaDto saida = motor.parsearResposta(response);

        assertThat(saida.sucesso()).isTrue();
        assertThat(saida.scoreTotal()).isEqualTo(120);
    }

    @Test
    void deveFalharAoParsearRespostaVazia() {
        OpenRouterResponseDto response = new OpenRouterResponseDto(List.of(), null);

        assertThatThrownBy(() -> motor.parsearResposta(response))
                .isInstanceOf(OpenRouterRespostaInvalidaException.class)
                .hasMessageContaining("resposta vazia");
    }
}
