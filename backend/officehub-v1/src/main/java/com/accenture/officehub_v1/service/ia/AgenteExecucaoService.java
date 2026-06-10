package com.accenture.officehub_v1.service.ia;

import com.accenture.officehub_v1.dto.response.AgenteExecucaoResponse;
import com.accenture.officehub_v1.entity.AgenteExecucao;
import com.accenture.officehub_v1.entity.enums.StatusAgente;
import com.accenture.officehub_v1.repository.AgenteExecucaoRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AgenteExecucaoService {

    private final AgenteExecucaoRepository agenteExecucaoRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AgenteExecucao registrarExecucao(
            String tipoAgente,
            String versaoModelo,
            JsonNode payloadEntrada,
            JsonNode payloadSaida,
            int tempoProcessamentoMs,
            StatusAgente status,
            String erroMensagem) {
        return registrarExecucao(
                tipoAgente,
                versaoModelo,
                payloadEntrada,
                payloadSaida,
                tempoProcessamentoMs,
                status,
                erroMensagem,
                null);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AgenteExecucao registrarExecucao(
            String tipoAgente,
            String versaoModelo,
            JsonNode payloadEntrada,
            JsonNode payloadSaida,
            int tempoProcessamentoMs,
            StatusAgente status,
            String erroMensagem,
            Integer tokensUtilizados) {

        AgenteExecucao execucao = AgenteExecucao.builder()
                .tipoAgente(tipoAgente)
                .versaoModelo(versaoModelo)
                .payloadEntrada(payloadEntrada)
                .payloadSaida(enriquecerPayloadSaida(payloadSaida, tokensUtilizados))
                .tempoProcessamentoMs(tempoProcessamentoMs)
                .status(status)
                .erroMensagem(erroMensagem)
                .tentativas(1)
                .build();

        return agenteExecucaoRepository.save(execucao);
    }

    private JsonNode enriquecerPayloadSaida(JsonNode payloadSaida, Integer tokensUtilizados) {
        if (tokensUtilizados == null || payloadSaida == null || !payloadSaida.isObject()) {
            return payloadSaida;
        }

        ObjectNode enriquecido = payloadSaida.deepCopy();
        enriquecido.put("tokensUtilizados", tokensUtilizados);
        return enriquecido;
    }

    @Transactional
    public void vincularReferencia(UUID execucaoId, UUID referenciaId) {
        agenteExecucaoRepository.findById(execucaoId).ifPresent(execucao -> {
            execucao.setReferenciaId(referenciaId);
            agenteExecucaoRepository.save(execucao);
        });
    }

    public List<AgenteExecucaoResponse> listar(
            String tipoAgente,
            StatusAgente status,
            LocalDate dataInicio,
            LocalDate dataFim) {

        OffsetDateTime inicio = dataInicio != null
                ? dataInicio.atStartOfDay().atOffset(ZoneOffset.UTC)
                : OffsetDateTime.parse("2000-01-01T00:00:00Z");

        OffsetDateTime fim = dataFim != null
                ? dataFim.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC)
                : OffsetDateTime.parse("2100-01-01T00:00:00Z");

        List<AgenteExecucao> execucoes;

        if (tipoAgente != null && status != null) {
            execucoes = agenteExecucaoRepository
                    .findByTipoAgenteAndStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
                            tipoAgente, status, inicio, fim);
        } else if (tipoAgente != null) {
            execucoes = agenteExecucaoRepository
                    .findByTipoAgenteAndCreatedAtBetweenOrderByCreatedAtDesc(
                            tipoAgente, inicio, fim);
        } else if (status != null) {
            execucoes = agenteExecucaoRepository
                    .findByStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
                            status, inicio, fim);
        } else {
            execucoes = agenteExecucaoRepository
                    .findByCreatedAtBetweenOrderByCreatedAtDesc(
                            inicio, fim);
        }

        return execucoes.stream()
                .map(AgenteExecucaoResponse::from)
                .toList();
    }
}
