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
            11. Se combinacoesExcluidas estiver preenchido, trata-se de uma NOVA SUGESTÃO:
                escolha uma combinação de posicaoId DIFERENTE de todas as listadas em combinacoesExcluidas
                (mesmos UUIDs, independente da ordem). Varie a escolha mesmo entre posições equivalentes.
            12. Só repita uma combinação de combinacoesExcluidas se instrucaoRepeticaoPermitida=true
                e não houver outra combinação válida disponível.
            """;

    private OpenRouterAlocacaoPrompt() {
    }

    static String montarMensagemUsuario(String payloadJson) {
        return montarMensagemUsuario(payloadJson, false, 0, false);
    }

    static String montarMensagemUsuario(
            String payloadJson,
            boolean novaSugestao,
            int tentativa,
            boolean repeticaoPermitida) {

        if (!novaSugestao) {
            return """
                    Aloque os participantes nas posições livres abaixo.
                    Retorne APENAS o JSON de saída no formato definido no system prompt.

                    DADOS DA RESERVA:
                    %s
                    """.formatted(payloadJson);
        }

        StringBuilder mensagem = new StringBuilder("""
                O usuário pediu OUTRA SUGESTÃO de alocação.
                Retorne APENAS o JSON de saída no formato definido no system prompt.

                IMPORTANTE:
                - combinacoesExcluidas contém sugestões já apresentadas; NÃO repita nenhuma delas.
                - Priorize posições diferentes, mesmo quando forem equivalentes em equipamento ou tipo.
                - Explore alternativas antes de voltar a uma combinação anterior.
                """);

        if (tentativa > 0) {
            mensagem.append("""
                    - Tentativa %d: a resposta anterior repetiu uma combinação excluída.
                      Escolha obrigatoriamente outra combinação válida.
                    """.formatted(tentativa + 1));
        }

        if (repeticaoPermitida) {
            mensagem.append("""
                    - instrucaoRepeticaoPermitida=true: as alternativas distintas já foram esgotadas.
                      Retorne a melhor combinação válida restante, mesmo que já tenha sido sugerida antes.
                    """);
        }

        mensagem.append("""

                DADOS DA RESERVA:
                %s
                """.formatted(payloadJson));

        return mensagem.toString();
    }
}
