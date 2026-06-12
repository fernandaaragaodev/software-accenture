package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CargoRepository extends JpaRepository<Cargo, UUID> {

    List<Cargo> findAllByOrderByNomeAsc();
}
