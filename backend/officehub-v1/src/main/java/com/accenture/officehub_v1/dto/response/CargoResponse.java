package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Cargo;
import com.accenture.officehub_v1.entity.Especialidade;

import java.util.UUID;

public record CargoResponse(
        UUID id,
        String nome,
        String descricao
) {
    public static CargoResponse from(Cargo cargo) {
        return new CargoResponse(cargo.getId(), cargo.getNome(), cargo.getDescricao());
    }
}
