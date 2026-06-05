package com.accenture.officehub_v1.service.ia;

import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteEntradaDto;
import com.accenture.officehub_v1.dto.ia.AlocacaoAgenteSaidaDto;
import com.accenture.officehub_v1.dto.ia.PessoaAlocacaoEntradaDto;
import com.accenture.officehub_v1.dto.ia.PosicaoAlocadaSaidaDto;
import com.accenture.officehub_v1.dto.ia.PosicaoLivreEntradaDto;
import com.accenture.officehub_v1.dto.request.PessoaReservaRequest;
import com.accenture.officehub_v1.entity.Posicao;
import com.accenture.officehub_v1.entity.enums.StatusAgente;
import com.accenture.officehub_v1.service.AlocacaoPosicaoService;
import com.accenture.officehub_v1.service.alocacao.ItemAlocacao;
import com.accenture.officehub_v1.service.alocacao.ResultadoAlocacao;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class AgenteAlocacaoService {

    private final AlocacaoPosicaoService alocacaoPosicaoService;
    private final AgenteExecucaoService agenteExecucaoService;
    private final ObjectMapper objectMapper;

    public ResultadoExecucaoAgente executar(
            UUID salaId,
            LocalDate dataReserva,
            List<PessoaReservaRequest> pessoas,
            String criterioProximidade,
            BigDecimal raioProximidade,
            List<Posicao> posicoesLivres) {

        AlocacaoAgenteEntradaDto entrada = montarEntrada(
                salaId,
                dataReserva,
                pessoas,
                criterioProximidade,
                raioProximidade,
                posicoesLivres);

        long inicio = System.nanoTime();
        ResultadoAlocacao resultado = alocacaoPosicaoService.alocar(
                pessoas,
                posicoesLivres,
                criterioProximidade,
                raioProximidade);
        int tempoMs = (int) ((System.nanoTime() - inicio) / 1_000_000L);

        AlocacaoAgenteSaidaDto saida = montarSaida(resultado);
        JsonNode payloadEntrada = objectMapper.valueToTree(entrada);
        JsonNode payloadSaida = objectMapper.valueToTree(saida);

        StatusAgente status = resultado.sucesso() ? StatusAgente.SUCESSO : StatusAgente.FALHA;
        String erro = resultado.sucesso() ? null : resultado.motivoFalha();

        var execucao = agenteExecucaoService.registrarExecucao(
                ConstantesAgenteIa.TIPO_ALOCACAO,
                ConstantesAgenteIa.VERSAO_ALGORITMO_ESPACIAL_V1,
                payloadEntrada,
                payloadSaida,
                tempoMs,
                status,
                erro);

        return new ResultadoExecucaoAgente(resultado, execucao.getId());
    }

    public void vincularReferenciaReserva(UUID execucaoId, UUID reservaId) {
        agenteExecucaoService.vincularReferencia(execucaoId, reservaId);
    }

    private AlocacaoAgenteEntradaDto montarEntrada(
            UUID salaId,
            LocalDate dataReserva,
            List<PessoaReservaRequest> pessoas,
            String criterioProximidade,
            BigDecimal raioProximidade,
            List<Posicao> posicoesLivres) {

        List<PessoaAlocacaoEntradaDto> pessoasEntrada = pessoas.stream()
                .map(p -> new PessoaAlocacaoEntradaDto(
                        p.usuarioId(),
                        p.nomeExterno(),
                        tiposCompativeis(p)))
                .toList();

        List<PosicaoLivreEntradaDto> posicoesEntrada = posicoesLivres.stream()
                .map(p -> new PosicaoLivreEntradaDto(
                        p.getId(),
                        p.getTipo(),
                        p.getCoordX(),
                        p.getCoordY()))
                .toList();

        return new AlocacaoAgenteEntradaDto(
                salaId,
                dataReserva,
                criterioProximidade,
                raioProximidade,
                pessoasEntrada,
                posicoesEntrada);
    }

    private AlocacaoAgenteSaidaDto montarSaida(ResultadoAlocacao resultado) {
        if (!resultado.sucesso()) {
            return new AlocacaoAgenteSaidaDto(
                    false,
                    resultado.motivoFalha(),
                    null,
                    List.of());
        }

        List<PosicaoAlocadaSaidaDto> alocacoes = resultado.alocacoes().stream()
                .map(this::mapearAlocacao)
                .toList();

        return new AlocacaoAgenteSaidaDto(
                true,
                null,
                resultado.avisoProximidade(),
                alocacoes);
    }

    private PosicaoAlocadaSaidaDto mapearAlocacao(ItemAlocacao item) {
        return new PosicaoAlocadaSaidaDto(
                item.posicao().getId(),
                item.posicao().getIdentificador(),
                item.posicao().getTipo(),
                item.pessoa().usuarioId(),
                item.pessoa().nomeExterno());
    }

    private List<String> tiposCompativeis(PessoaReservaRequest pessoa) {
        List<String> tipos = new ArrayList<>();
        Stream.of(pessoa.tipoPreferido1(), pessoa.tipoPreferido2(), pessoa.tipoPreferido3())
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .forEach(tipos::add);
        return tipos;
    }
}
