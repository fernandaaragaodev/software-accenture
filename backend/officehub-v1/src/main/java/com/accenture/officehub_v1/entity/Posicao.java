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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "posicoes",
        uniqueConstraints = @UniqueConstraint(name = "uq_posicao_sala", columnNames = {"sala_id", "identificador"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Posicao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sala_id", nullable = false)
    private Sala sala;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "layout_id", nullable = false)
    private Layout layout;

    @NotBlank
    @Column(name = "identificador", nullable = false, length = 100)
    private String identificador;

    @Column(name = "tipo", length = 100)
    private String tipo;

    @Column(name = "coord_x")
    private BigDecimal coordX;

    @Column(name = "coord_y")
    private BigDecimal coordY;

    @Column(name = "tipo_cadeira", length = 100)
    private String tipoCadeira;

    @Column(name = "tipo_mesa", length = 100)
    private String tipoMesa;

    @Column(name = "status", length = 50)
    private String status;

    @Column(name = "ajustado_manualmente")
    @Builder.Default
    private Boolean ajustadoManualmente = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;
}
