package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.PosicaoEquipamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface PosicaoEquipamentoRepository extends JpaRepository<PosicaoEquipamento, UUID> {

    List<PosicaoEquipamento> findByPosicaoIdOrderByCreatedAtAsc(UUID posicaoId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT pe FROM PosicaoEquipamento pe JOIN FETCH pe.tipoEquipamento te WHERE pe.posicao.id IN :posicaoIds")
    List<PosicaoEquipamento> findByPosicaoIdInWithTipoEquipamento(
            @org.springframework.data.repository.query.Param("posicaoIds") Collection<UUID> posicaoIds);

    boolean existsByPosicaoIdAndTipoEquipamentoId(UUID posicaoId, UUID tipoEquipamentoId);

    @org.springframework.data.jpa.repository.Query(
            "SELECT COALESCE(SUM(pe.quantidade), 0) FROM PosicaoEquipamento pe")
    long somarQuantidadeTotal();
}
