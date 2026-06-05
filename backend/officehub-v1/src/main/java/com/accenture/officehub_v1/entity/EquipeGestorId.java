package com.accenture.officehub_v1.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class EquipeGestorId implements Serializable {

    @Column(name = "equipe_id")
    private UUID equipeId;

    @Column(name = "usuario_id")
    private UUID usuarioId;
}
