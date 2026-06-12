package com.accenture.officehub_v1.service.ia;

import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteEntradaDto;
import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteSaidaDto;
import com.accenture.officehub_v1.dto.ia.PosicaoAlocadaSaidaDto;
import com.accenture.officehub_v1.dto.request.PessoaReservaRequest;
import com.accenture.officehub_v1.entity.AgenteExecucao;
import com.accenture.officehub_v1.entity.Posicao;
import com.accenture.officehub_v1.entity.Sala;
import com.accenture.officehub_v1.entity.enums.StatusAgente;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.AgenteExecucaoRepository;
import com.accenture.officehub_v1.service.alocacao.ItemAlocacao;
import com.accenture.officehub_v1.service.alocacao.ResultadoAlocacao;
import com.accenture.officehub_v1.service.ia.motor.AlocacaoRespostaValidador;
import com.accenture.officehub_v1.service.ia.motor.MotorAlocacao;
import com.accenture.officehub_v1.service.ia.motor.MotorAlocacaoFactory;
import com.accenture.officehub_v1.service.ia.motor.MotorAlocacaoOpenRouter;
import com.accenture.officehub_v1.service.ia.motor.OpcoesChamadaOpenRouter;
import com.accenture.officehub_v1.service.ia.motor.OpenRouterIndisponivelException;
import com.accenture.officehub_v1.service.ia.motor.ResultadoChamadaOpenRouter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgenteAlocacaoService {

    private static final Logger log = LoggerFactory.getLogger(AgenteAlocacaoService.class);
    private static final int MAX_TENTATIVAS_NOVA_SUGESTAO = 5;
    private static final double TEMPERATURA_BASE_NOVA_SUGESTAO = 0.35;

    private final MotorAlocacaoFactory motorAlocacaoFactory;
    private final AlocacaoRespostaValidador respostaValidador;
    private final AlocacaoEntradaBuilder alocacaoEntradaBuilder;
    private final AgenteExecucaoService agenteExecucaoService;
    private final AgenteExecucaoRepository agenteExecucaoRepository;
    private final ObjectMapper objectMapper;

    public ResultadoExecucaoAgente executar(
            Sala sala,
            LocalDate dataReserva,
            UUID equipeId,
            List<PessoaReservaRequest> pessoas,
            String criterioProximidade,
            List<Posicao> posicoesLivres) {
        return executar(sala, dataReserva, equipeId, pessoas, criterioProximidade, posicoesLivres, List.of());
    }

    public ResultadoExecucaoAgente executar(
            Sala sala,
            LocalDate dataReserva,
            UUID equipeId,
            List<PessoaReservaRequest> pessoas,
            String criterioProximidade,
            List<Posicao> posicoesLivres,
            List<List<UUID>> combinacoesExcluidas) {

        AlocacaoAgenteEntradaDto entrada = alocacaoEntradaBuilder.montar(
                sala,
                dataReserva,
                equipeId,
                pessoas,
                criterioProximidade,
                posicoesLivres,
                combinacoesExcluidas);

        ExecucaoMotorResultado execucao = combinacoesExcluidas != null && !combinacoesExcluidas.isEmpty()
                ? executarNovaSugestaoOpenRouter(entrada, pessoas, posicoesLivres)
                : executarComFallback(entrada, pessoas, posicoesLivres);
        return new ResultadoExecucaoAgente(execucao.resultadoAlocacao(), execucao.execucaoId());
    }

    public ResultadoAlocacao recuperarResultadoExecucao(
            UUID execucaoId,
            List<PessoaReservaRequest> pessoas,
            List<Posicao> posicoesLivres) {
        AgenteExecucao execucao = agenteExecucaoRepository.findById(execucaoId)
                .orElseThrow(() -> new RegraNegocioException("Sugestão de alocação não encontrada ou expirada."));

        if (execucao.getReferenciaId() != null) {
            throw new RegraNegocioException("Esta sugestão já foi utilizada em outra reserva.");
        }

        if (execucao.getStatus() != StatusAgente.SUCESSO) {
            throw new RegraNegocioException("A sugestão de alocação não está disponível para confirmação.");
        }

        AlocacaoAgenteSaidaDto saida = objectMapper.convertValue(
                execucao.getPayloadSaida(), AlocacaoAgenteSaidaDto.class);
        return converterParaResultado(saida, pessoas, posicoesLivres);
    }

    public void vincularReferenciaReserva(UUID execucaoId, UUID reservaId) {
        agenteExecucaoService.vincularReferencia(execucaoId, reservaId);
    }

    private ExecucaoMotorResultado executarNovaSugestaoOpenRouter(
            AlocacaoAgenteEntradaDto entrada,
            List<PessoaReservaRequest> pessoas,
            List<Posicao> posicoesLivres) {

        MotorAlocacaoOpenRouter openRouter = motorAlocacaoFactory.obterMotorOpenRouter();
        boolean alternativasEsgotadas = respostaValidador.alternativasEsgotadas(entrada);
        String ultimoErro = "A IA não conseguiu sugerir uma combinação diferente das já apresentadas.";

        if (alternativasEsgotadas) {
            return finalizarNovaSugestaoOpenRouter(
                    entrada,
                    pessoas,
                    posicoesLivres,
                    openRouter.chamar(
                            entrada,
                            new OpcoesChamadaOpenRouter(
                                    TEMPERATURA_BASE_NOVA_SUGESTAO,
                                    0,
                                    true,
                                    true,
                                    true)));
        }

        for (int tentativa = 0; tentativa < MAX_TENTATIVAS_NOVA_SUGESTAO; tentativa++) {
            OpcoesChamadaOpenRouter opcoes = new OpcoesChamadaOpenRouter(
                    TEMPERATURA_BASE_NOVA_SUGESTAO + tentativa * 0.15,
                    tentativa,
                    true,
                    false,
                    false);

            ResultadoChamadaOpenRouter resultado = openRouter.chamar(entrada, opcoes);

            if (resultado.erroValidacao().isEmpty()) {
                return finalizarNovaSugestaoOpenRouter(entrada, pessoas, posicoesLivres, resultado);
            }

            ultimoErro = resultado.erroValidacao().orElse(ultimoErro);
            boolean repetiuCombinacao = AlocacaoRespostaValidador.ERRO_COMBINACAO_JA_SUGERIDA.equals(ultimoErro)
                    || respostaValidador.combinacaoExcluida(entrada, resultado.saida());

            if (!repetiuCombinacao) {
                log.warn("Nova sugestão OpenRouter rejeitada: {}", ultimoErro);
                break;
            }

            log.info(
                    "Nova sugestão OpenRouter repetiu combinação excluída (tentativa {}/{}). Reenviando à IA.",
                    tentativa + 1,
                    MAX_TENTATIVAS_NOVA_SUGESTAO);
        }

        AlocacaoAgenteSaidaDto saidaFalha = AlocacaoAgenteSaidaDto.falha(ultimoErro);
        var execucaoFalha = registrarExecucao(
                ConstantesAgenteIa.VERSAO_OPENROUTER_GEMINI_FLASH_V1,
                entrada,
                saidaFalha,
                0,
                StatusAgente.FALHA,
                ultimoErro);

        return new ExecucaoMotorResultado(
                converterParaResultado(saidaFalha, pessoas, posicoesLivres),
                execucaoFalha.getId());
    }

    private ExecucaoMotorResultado finalizarNovaSugestaoOpenRouter(
            AlocacaoAgenteEntradaDto entrada,
            List<PessoaReservaRequest> pessoas,
            List<Posicao> posicoesLivres,
            ResultadoChamadaOpenRouter resultado) {

        if (resultado.erroValidacao().isPresent()) {
            String erro = resultado.erroValidacao().get();
            AlocacaoAgenteSaidaDto saidaFalha = AlocacaoAgenteSaidaDto.falha(erro);
            var execucaoFalha = registrarExecucao(
                    ConstantesAgenteIa.VERSAO_OPENROUTER_GEMINI_FLASH_V1,
                    entrada,
                    saidaFalha,
                    resultado.tempoMs(),
                    StatusAgente.FALHA,
                    erro);

            return new ExecucaoMotorResultado(
                    converterParaResultado(saidaFalha, pessoas, posicoesLivres),
                    execucaoFalha.getId());
        }

        var execucao = registrarExecucao(
                ConstantesAgenteIa.VERSAO_OPENROUTER_GEMINI_FLASH_V1,
                entrada,
                resultado.saida(),
                resultado.tempoMs(),
                StatusAgente.SUCESSO,
                null);

        return new ExecucaoMotorResultado(
                converterParaResultado(resultado.saida(), pessoas, posicoesLivres),
                execucao.getId());
    }

    private ExecucaoMotorResultado executarComFallback(
            AlocacaoAgenteEntradaDto entrada,
            List<PessoaReservaRequest> pessoas,
            List<Posicao> posicoesLivres) {

        if (motorAlocacaoFactory.usaOpenRouterComoPrincipal()) {
            return executarOpenRouterComFallback(entrada, pessoas, posicoesLivres);
        }

        return executarMotor(
                motorAlocacaoFactory.obterMotorPrincipal(),
                ConstantesAgenteIa.VERSAO_ALGORITMO_ESPACIAL_V2,
                entrada,
                pessoas,
                posicoesLivres,
                false);
    }

    private ExecucaoMotorResultado executarOpenRouterComFallback(
            AlocacaoAgenteEntradaDto entrada,
            List<PessoaReservaRequest> pessoas,
            List<Posicao> posicoesLivres) {

        MotorAlocacao openRouter = motorAlocacaoFactory.obterMotorPrincipal();

        try {
            long inicio = System.nanoTime();
            AlocacaoAgenteSaidaDto saida = openRouter.executar(entrada);
            int tempoMs = (int) ((System.nanoTime() - inicio) / 1_000_000L);

            if (!saida.sucesso()) {
                log.warn("OpenRouter retornou falha de alocação: {}. Acionando fallback espacial.",
                        saida.motivoFalha());
                registrarExecucao(
                        ConstantesAgenteIa.VERSAO_OPENROUTER_GEMINI_FLASH_V1,
                        entrada,
                        saida,
                        tempoMs,
                        StatusAgente.FALHA,
                        saida.motivoFalha());

                return executarMotor(
                        motorAlocacaoFactory.obterMotorFallback(),
                        ConstantesAgenteIa.VERSAO_ALGORITMO_ESPACIAL_V2,
                        entrada,
                        pessoas,
                        posicoesLivres,
                        true);
            }

            var execucao = registrarExecucao(
                    ConstantesAgenteIa.VERSAO_OPENROUTER_GEMINI_FLASH_V1,
                    entrada,
                    saida,
                    tempoMs,
                    StatusAgente.SUCESSO,
                    null);

            return new ExecucaoMotorResultado(
                    converterParaResultado(saida, pessoas, posicoesLivres),
                    execucao.getId());
        } catch (OpenRouterIndisponivelException ex) {
            log.error("Falha no OpenRouter. Executando fallback espacial. Motivo: {}", ex.getMessage());

            registrarExecucaoFalhaOpenRouter(entrada, ex.getMessage());

            return executarMotor(
                    motorAlocacaoFactory.obterMotorFallback(),
                    ConstantesAgenteIa.VERSAO_ALGORITMO_ESPACIAL_V2,
                    entrada,
                    pessoas,
                    posicoesLivres,
                    true);
        }
    }

    private ExecucaoMotorResultado executarMotor(
            MotorAlocacao motor,
            String versaoModelo,
            AlocacaoAgenteEntradaDto entrada,
            List<PessoaReservaRequest> pessoas,
            List<Posicao> posicoesLivres,
            boolean fallback) {

        long inicio = System.nanoTime();
        AlocacaoAgenteSaidaDto saida = motor.executar(entrada);
        int tempoMs = (int) ((System.nanoTime() - inicio) / 1_000_000L);

        StatusAgente status = saida.sucesso() ? StatusAgente.SUCESSO : StatusAgente.FALHA;
        String erro = saida.sucesso() ? null : saida.motivoFalha();

        if (fallback && saida.sucesso()) {
            log.info("Fallback espacial concluiu alocação com sucesso.");
        }

        var execucao = registrarExecucao(versaoModelo, entrada, saida, tempoMs, status, erro);

        return new ExecucaoMotorResultado(
                converterParaResultado(saida, pessoas, posicoesLivres),
                execucao.getId());
    }

    private void registrarExecucaoFalhaOpenRouter(AlocacaoAgenteEntradaDto entrada, String mensagemErro) {
        AlocacaoAgenteSaidaDto saidaFalha = AlocacaoAgenteSaidaDto.falha(mensagemErro);
        registrarExecucao(
                ConstantesAgenteIa.VERSAO_OPENROUTER_GEMINI_FLASH_V1,
                entrada,
                saidaFalha,
                0,
                StatusAgente.FALHA,
                mensagemErro);
    }

    private AgenteExecucao registrarExecucao(
            String versaoModelo,
            AlocacaoAgenteEntradaDto entrada,
            AlocacaoAgenteSaidaDto saida,
            int tempoMs,
            StatusAgente status,
            String erro) {

        JsonNode payloadEntrada = objectMapper.valueToTree(entrada);
        JsonNode payloadSaida = objectMapper.valueToTree(saida);

        return agenteExecucaoService.registrarExecucao(
                ConstantesAgenteIa.TIPO_ALOCACAO,
                versaoModelo,
                payloadEntrada,
                payloadSaida,
                tempoMs,
                status,
                erro,
                saida.tokensUtilizados());
    }

    private ResultadoAlocacao converterParaResultado(
            AlocacaoAgenteSaidaDto saida,
            List<PessoaReservaRequest> pessoas,
            List<Posicao> posicoesLivres) {

        if (!saida.sucesso()) {
            return ResultadoAlocacao.falha(saida.motivoFalha());
        }

        Map<UUID, PessoaReservaRequest> pessoasPorId = pessoas.stream()
                .filter(p -> p.usuarioId() != null)
                .collect(Collectors.toMap(PessoaReservaRequest::usuarioId, Function.identity()));

        Map<UUID, Posicao> posicoesPorId = posicoesLivres.stream()
                .collect(Collectors.toMap(Posicao::getId, Function.identity()));

        List<ItemAlocacao> itens = new ArrayList<>();

        for (PosicaoAlocadaSaidaDto alocacao : saida.alocacoes()) {
            PessoaReservaRequest pessoa = pessoasPorId.get(alocacao.pessoaId());
            Posicao posicao = posicoesPorId.get(alocacao.posicaoId());

            if (pessoa == null || posicao == null) {
                return ResultadoAlocacao.falha("Alocação retornada com referências inválidas.");
            }

            itens.add(new ItemAlocacao(pessoa, posicao));
        }

        return ResultadoAlocacao.sucesso(itens, saida.avisoProximidade());
    }

    private record ExecucaoMotorResultado(ResultadoAlocacao resultadoAlocacao, UUID execucaoId) {
    }
}
