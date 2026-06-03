package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.ReservaPessoa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservaPessoaRepository extends JpaRepository<ReservaPessoa, UUID> {
}
