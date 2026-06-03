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
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Check;

import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "horarios_disponibilidade")
@Check(constraints = "hora_abertura < hora_fechamento")
@Check(constraints = "dia_semana >= 0 AND dia_semana <= 6")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HorarioDisponibilidade {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "regra_disponibilidade_id", nullable = false)
    private RegraDisponibilidade regraDisponibilidade;

    @NotNull
    @Min(0)
    @Max(6)
    @Column(name = "dia_semana", nullable = false)
    private Integer diaSemana;

    @NotNull
    @Column(name = "hora_abertura", nullable = false)
    private LocalTime horaAbertura;

    @NotNull
    @Column(name = "hora_fechamento", nullable = false)
    private LocalTime horaFechamento;
}
