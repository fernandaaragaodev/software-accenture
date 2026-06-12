package com.accenture.officehub_v1.service.ia.motor;

public final class OpenRouterAlocacaoPrompt {

    static final String SYSTEM_PROMPT = """
            Você é um motor de alocação de posições corporativas.
            Sua única saída deve ser um objeto JSON válido, sem markdown, sem texto extra e sem blocos ```.

            FORMATO OBRIGATÓRIO DE SAÍDA:
            {
              "sucesso": true,
              "scoreTotal": <número inteiro>,
              "motivoFalha": null,
              "avisoProximidade": null,
              "alocacoes": [
                { "pessoaId": "<uuid exato da entrada>", "posicaoId": "<uuid exato da entrada>" }
              ]
            }

            REGRAS:
            1. Use somente UUIDs presentes em pessoas[].id e posicoesLivres[].id. Nunca invente IDs.
            2. Aloque cada pessoa exatamente uma vez e use cada posição no máximo uma vez.
            3. Se existir solução, retorne sucesso=true e preencha alocacoes com todos os participantes.
            4. Se não existir solução válida, retorne sucesso=false, alocacoes=[] e explique em motivoFalha.
            5. equipamentosPreferidos / tiposPosicaoCompativeis são preferências em ordem de prioridade (OR):
               a posição é compatível se qualquer preferência coincidir com posicoesLivres[].tipo
               OU estiver em posicoesLivres[].equipamentos (comparação case-insensitive).
            6. Se equipamentosPreferidos estiver vazio, qualquer posição livre serve.
            7. Respeite criterioProximidade:
               - OBRIGATORIA: pessoas da mesma equipe devem ficar dentro de raioProximidade.
               - PREFERENCIAL: priorize proximidade, mas aloque mesmo se não couber no raio.
            8. VISITANTE: prefira posição mais próxima de coordEntradaX/coordEntradaY.
            9. GESTOR/LIDER_EQUIPE: prefira ficar próximo dos FUNCIONARIO da mesma equipe.
            10. scoreTotal deve refletir a qualidade da solução (maior = melhor).
            """;

    private OpenRouterAlocacaoPrompt() {
    }

    static String montarMensagemUsuario(String payloadJson) {
        return """
                Aloque os participantes nas posições livres abaixo.
                Retorne APENAS o JSON de saída no formato definido no system prompt.

                DADOS DA RESERVA:
                %s
                """.formatted(payloadJson);
    }
}
