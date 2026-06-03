package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.Posicao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PosicaoRepository extends JpaRepository<Posicao, UUID> {
}
