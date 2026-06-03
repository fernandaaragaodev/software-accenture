package com.accenture.officehub_v1.entity;

import com.accenture.officehub_v1.entity.enums.StatusAgente;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "agente_execucoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgenteExecucao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank
    @Column(name = "tipo_agente", nullable = false)
    private String tipoAgente;

    @Column(name = "referencia_id")
    private UUID referenciaId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", nullable = false, columnDefinition = "status_agente")
    @Builder.Default
    private StatusAgente status = StatusAgente.PROCESSANDO;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_entrada", columnDefinition = "jsonb")
    private JsonNode payloadEntrada;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_saida", columnDefinition = "jsonb")
    private JsonNode payloadSaida;

    @Column(name = "versao_modelo", length = 100)
    private String versaoModelo;

    @Column(name = "tempo_processamento_ms")
    private Integer tempoProcessamentoMs;

    @Column(name = "tentativas")
    @Builder.Default
    private Integer tentativas = 0;

    @Column(name = "erro_mensagem", columnDefinition = "text")
    private String erroMensagem;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}
