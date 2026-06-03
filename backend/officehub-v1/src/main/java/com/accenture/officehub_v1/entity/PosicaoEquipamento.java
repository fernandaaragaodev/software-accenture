package com.accenture.officehub_v1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "posicao_equipamentos",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_posicao_equipamento",
                columnNames = {"posicao_id", "tipo_equipamento_id"}
        )
)
@Check(constraints = "quantidade > 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosicaoEquipamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "posicao_id", nullable = false)
    private Posicao posicao;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_equipamento_id", nullable = false)
    private TipoEquipamento tipoEquipamento;

    @NotNull
    @Min(1)
    @Column(name = "quantidade", nullable = false)
    @Builder.Default
    private Integer quantidade = 1;

    @Column(name = "observacao", columnDefinition = "text")
    private String observacao;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
