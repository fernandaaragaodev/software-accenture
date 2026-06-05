package com.accenture.officehub_v1.service.ia;

import com.accenture.officehub_v1.dto.response.AgenteExecucaoResponse;
import com.accenture.officehub_v1.entity.AgenteExecucao;
import com.accenture.officehub_v1.entity.enums.StatusAgente;
import com.accenture.officehub_v1.repository.AgenteExecucaoRepository;
import com.fasterxml.jackson.databind.JsonNode;
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

        AgenteExecucao execucao = AgenteExecucao.builder()
                .tipoAgente(tipoAgente)
                .versaoModelo(versaoModelo)
                .payloadEntrada(payloadEntrada)
                .payloadSaida(payloadSaida)
                .tempoProcessamentoMs(tempoProcessamentoMs)
                .status(status)
                .erroMensagem(erroMensagem)
                .tentativas(1)
                .build();

        return agenteExecucaoRepository.save(execucao);
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
                : null;
        OffsetDateTime fim = dataFim != null
                ? dataFim.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC).minusNanos(1)
                : null;

        return agenteExecucaoRepository.buscarComFiltros(tipoAgente, status, inicio, fim).stream()
                .map(AgenteExecucaoResponse::from)
                .toList();
    }
}
