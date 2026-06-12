package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.response.CargoResponse;
import com.accenture.officehub_v1.dto.response.EspecialidadeResponse;
import com.accenture.officehub_v1.entity.Cargo;
import com.accenture.officehub_v1.entity.Especialidade;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.entity.UsuarioEspecialidade;

import java.util.Comparator;
import java.util.List;

public final class UsuarioMapperHelper {

    private UsuarioMapperHelper() {
    }

    public static CargoResponse mapCargo(Usuario usuario) {
        Cargo cargo = usuario.getCargo();
        return cargo != null ? CargoResponse.from(cargo) : null;
    }

    public static List<EspecialidadeResponse> mapEspecialidades(Usuario usuario) {
        return usuario.getUsuarioEspecialidades().stream()
                .map(UsuarioEspecialidade::getEspecialidade)
                .sorted(Comparator.comparing(Especialidade::getNome, String.CASE_INSENSITIVE_ORDER))
                .map(EspecialidadeResponse::from)
                .toList();
    }

    public static String mapCargoNome(Usuario usuario) {
        return usuario.getCargo() != null ? usuario.getCargo().getNome() : null;
    }

    public static void inicializarCargoEEspecialidades(Usuario usuario) {
        if (usuario.getCargo() != null) {
            usuario.getCargo().getNome();
        }
        usuario.getUsuarioEspecialidades()
                .forEach(vinculo -> vinculo.getEspecialidade().getNome());
    }
}