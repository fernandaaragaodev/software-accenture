package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.Posicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PosicaoRepository extends JpaRepository<Posicao, UUID> {

    List<Posicao> findBySalaIdAndDeletedAtIsNull(UUID salaId);

    Optional<Posicao> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsBySalaIdAndIdentificadorIgnoreCaseAndDeletedAtIsNull(UUID salaId, String identificador);

    boolean existsBySalaIdAndIdentificadorIgnoreCaseAndDeletedAtIsNullAndIdNot(
            UUID salaId, String identificador, UUID id);

    List<Posicao> findBySalaIdOrderByIdentificadorAsc(UUID salaId);

    long countByDeletedAtIsNull();

    long countByDeletedAtIsNullAndStatusIgnoreCase(String status);
}
