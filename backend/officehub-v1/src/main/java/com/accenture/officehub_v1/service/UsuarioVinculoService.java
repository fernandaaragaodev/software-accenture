package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.entity.Cargo;
import com.accenture.officehub_v1.entity.Especialidade;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.entity.UsuarioEspecialidade;
import com.accenture.officehub_v1.entity.UsuarioEspecialidadeId;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.repository.CargoRepository;
import com.accenture.officehub_v1.repository.EspecialidadeRepository;
import com.accenture.officehub_v1.repository.UsuarioEspecialidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioVinculoService {

    private final CargoRepository cargoRepository;
    private final EspecialidadeRepository especialidadeRepository;
    private final UsuarioEspecialidadeRepository usuarioEspecialidadeRepository;

    @Transactional
    public void atribuirCargo(Usuario usuario, UUID cargoId) {
        if (cargoId == null) {
            usuario.setCargo(null);
            return;
        }

        Cargo cargo = cargoRepository.findById(cargoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cargo não encontrado."));
        usuario.setCargo(cargo);
    }

    @Transactional
    public void atribuirEspecialidades(Usuario usuario, List<UUID> especialidadeIds) {
        usuarioEspecialidadeRepository.deleteByUsuarioId(usuario.getId());

        if (especialidadeIds == null || especialidadeIds.isEmpty()) {
            usuario.getUsuarioEspecialidades().clear();
            return;
        }

        Set<UUID> idsUnicos = new LinkedHashSet<>(especialidadeIds);
        usuario.getUsuarioEspecialidades().clear();

        for (UUID especialidadeId : idsUnicos) {
            Especialidade especialidade = especialidadeRepository.findById(especialidadeId)
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Especialidade não encontrada: " + especialidadeId));

            UsuarioEspecialidade vinculo = UsuarioEspecialidade.builder()
                    .id(new UsuarioEspecialidadeId(usuario.getId(), especialidade.getId()))
                    .usuario(usuario)
                    .especialidade(especialidade)
                    .build();

            usuarioEspecialidadeRepository.save(vinculo);
            usuario.getUsuarioEspecialidades().add(vinculo);
        }
    }
}
