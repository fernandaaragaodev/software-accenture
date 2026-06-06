package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.PosicaoEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PosicaoEquipamentoRepository extends JpaRepository<PosicaoEquipamento, UUID> {

    List<PosicaoEquipamento> findByPosicaoIdOrderByCreatedAtAsc(UUID posicaoId);

    boolean existsByPosicaoIdAndTipoEquipamentoId(UUID posicaoId, UUID tipoEquipamentoId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(SUM(pe.quantidade), 0) FROM PosicaoEquipamento pe")
    long somarQuantidadeTotal();
}
