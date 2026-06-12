package com.accenture.officehub_v1.service.ia.motor;

import com.accenture.officehub_v1.config.IaProperties;
import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteEntradaDto;
import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteSaidaDto;
import com.accenture.officehub_v1.dto.ia.openrouter.OpenRouterMessageDto;
import com.accenture.officehub_v1.dto.ia.openrouter.OpenRouterRequestDto;
import com.accenture.officehub_v1.dto.ia.openrouter.OpenRouterResponseDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MotorAlocacaoOpenRouter implements MotorAlocacao {

    private static final Logger log = LoggerFactory.getLogger(MotorAlocacaoOpenRouter.class);

    private final RestClient openRouterRestClient;
    private final IaProperties iaProperties;
    private final ObjectMapper objectMapper;
    private final AlocacaoRespostaValidador respostaValidador;

    @Override
    public AlocacaoAgenteSaidaDto executar(AlocacaoAgenteEntradaDto entrada) {
        ResultadoChamadaOpenRouter resultado = chamar(entrada, OpcoesChamadaOpenRouter.padrao());
        if (resultado.erroValidacao().isPresent()) {
            log.warn("Resposta OpenRouter rejeitada na validação: {}", resultado.erroValidacao().get());
            return AlocacaoAgenteSaidaDto.falha(resultado.erroValidacao().get());
        }
        return resultado.saida();
    }

    public ResultadoChamadaOpenRouter chamar(
            AlocacaoAgenteEntradaDto entrada,
            OpcoesChamadaOpenRouter opcoes) {
        validarConfiguracao();

        try {
            long inicio = System.nanoTime();
            String payloadJson = objectMapper.writeValueAsString(entrada);
            OpenRouterRequestDto request = montarRequest(payloadJson, opcoes);
            OpenRouterResponseDto response = chamarApi(request);
            AlocacaoAgenteSaidaDto saida = parsearResposta(response);
            int tempoMs = (int) ((System.nanoTime() - inicio) / 1_000_000L);

            Optional<String> erroValidacao = respostaValidador.validar(
                    entrada,
                    saida,
                    opcoes.ignorarCombinacoesExcluidas());

            if (erroValidacao.isPresent()) {
                return new ResultadoChamadaOpenRouter(saida, erroValidacao, tempoMs);
            }

            return new ResultadoChamadaOpenRouter(enriquecerComTokens(saida, response), Optional.empty(), tempoMs);
        } catch (RestClientException ex) {
            log.error("Falha na comunicação com OpenRouter: {}", ex.getMessage());
            throw new OpenRouterIndisponivelException("OpenRouter indisponível: " + ex.getMessage(), ex);
        } catch (JsonProcessingException ex) {
            log.error("Falha ao processar JSON do OpenRouter: {}", ex.getMessage());
            throw new OpenRouterIndisponivelException("Resposta inválida do OpenRouter.", ex);
        } catch (OpenRouterRespostaInvalidaException ex) {
            log.error("Resposta inválida do OpenRouter: {}", ex.getMessage());
            throw new OpenRouterIndisponivelException(ex.getMessage(), ex);
        }
    }

    AlocacaoAgenteSaidaDto parsearResposta(OpenRouterResponseDto response) throws JsonProcessingException {
        if (response == null
                || response.choices() == null
                || response.choices().isEmpty()
                || response.choices().get(0).message() == null
                || response.choices().get(0).message().content() == null
                || response.choices().get(0).message().content().isBlank()) {
            throw new OpenRouterRespostaInvalidaException("OpenRouter retornou resposta vazia.");
        }

        String content = response.choices().get(0).message().content().trim();
        log.debug("Resposta bruta OpenRouter: {}", content);
        return OpenRouterRespostaParser.parsear(objectMapper, content);
    }

    private OpenRouterRequestDto montarRequest(String payloadJson, OpcoesChamadaOpenRouter opcoes) {
        return new OpenRouterRequestDto(
                iaProperties.openrouter().model(),
                List.of(
                        new OpenRouterMessageDto("system", OpenRouterAlocacaoPrompt.SYSTEM_PROMPT),
                        new OpenRouterMessageDto(
                                "user",
                                OpenRouterAlocacaoPrompt.montarMensagemUsuario(
                                        payloadJson,
                                        opcoes.novaSugestao(),
                                        opcoes.tentativa(),
                                        opcoes.repeticaoPermitida()))),
                opcoes.temperatura(),
                iaProperties.openrouter().maxTokens(),
                Map.of("type", "json_object"));
    }

    private OpenRouterResponseDto chamarApi(OpenRouterRequestDto request) {
        return openRouterRestClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + iaProperties.openrouter().apiKey())
                .header("HTTP-Referer", iaProperties.openrouter().httpReferer())
                .header("X-Title", iaProperties.openrouter().title())
                .body(request)
                .retrieve()
                .body(OpenRouterResponseDto.class);
    }

    private AlocacaoAgenteSaidaDto enriquecerComTokens(
            AlocacaoAgenteSaidaDto saida,
            OpenRouterResponseDto response) {

        Integer tokens = null;
        if (response.usage() != null) {
            tokens = response.usage().totalTokens();
        }

        return new AlocacaoAgenteSaidaDto(
                saida.sucesso(),
                saida.scoreTotal(),
                saida.motivoFalha(),
                saida.avisoProximidade(),
                saida.alocacoes(),
                tokens);
    }

    private void validarConfiguracao() {
        String apiKey = iaProperties.openrouter().apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new OpenRouterIndisponivelException(
                    "OPENROUTER_API_KEY não configurada. Defina a variável de ambiente.");
        }
    }
}
