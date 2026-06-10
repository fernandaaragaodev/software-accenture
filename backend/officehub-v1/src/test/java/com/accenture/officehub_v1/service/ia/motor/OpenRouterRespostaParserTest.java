package com.accenture.officehub_v1.service.ia.motor;

import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteSaidaDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OpenRouterRespostaParserTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void deveExtrairJsonDeBlocoMarkdown() throws Exception {
        UUID pessoaId = UUID.fromString("10000000-0000-0000-0000-000000000010");
        UUID posicaoId = UUID.fromString("10000000-0000-0000-0000-000000000015");

        String content = """
                ```json
                {
                  "sucesso": true,
                  "scoreTotal": 100,
                  "alocacoes": [
                    { "pessoaId": "%s", "posicaoId": "%s" }
                  ]
                }
                ```
                """.formatted(pessoaId, posicaoId);

        AlocacaoAgenteSaidaDto saida = OpenRouterRespostaParser.parsear(objectMapper, content);

        assertThat(saida.sucesso()).isTrue();
        assertThat(saida.alocacoes()).hasSize(1);
        assertThat(saida.alocacoes().get(0).pessoaId()).isEqualTo(pessoaId);
    }

    @Test
    void deveInferirSucessoQuandoAlocacoesExistemSemCampoSucesso() throws Exception {
        UUID pessoaId = UUID.fromString("10000000-0000-0000-0000-000000000010");
        UUID posicaoId = UUID.fromString("10000000-0000-0000-0000-000000000015");

        String content = """
                {
                  "scoreTotal": 50,
                  "alocacoes": [
                    { "pessoaId": "%s", "posicaoId": "%s" }
                  ]
                }
                """.formatted(pessoaId, posicaoId);

        AlocacaoAgenteSaidaDto saida = OpenRouterRespostaParser.parsear(objectMapper, content);

        assertThat(saida.sucesso()).isTrue();
        assertThat(saida.scoreTotal()).isEqualTo(50);
    }

    @Test
    void deveAceitarAliasesEmIngles() throws Exception {
        UUID pessoaId = UUID.fromString("10000000-0000-0000-0000-000000000010");
        UUID posicaoId = UUID.fromString("10000000-0000-0000-0000-000000000015");

        String content = """
                {
                  "success": true,
                  "score": 80,
                  "allocations": [
                    { "personId": "%s", "positionId": "%s" }
                  ]
                }
                """.formatted(pessoaId, posicaoId);

        AlocacaoAgenteSaidaDto saida = OpenRouterRespostaParser.parsear(objectMapper, content);

        assertThat(saida.sucesso()).isTrue();
        assertThat(saida.scoreTotal()).isEqualTo(80);
        assertThat(saida.alocacoes().get(0).pessoaId()).isEqualTo(pessoaId);
        assertThat(saida.alocacoes().get(0).posicaoId()).isEqualTo(posicaoId);
    }
}
