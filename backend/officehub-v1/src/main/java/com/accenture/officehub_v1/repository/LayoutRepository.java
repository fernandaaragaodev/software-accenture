package com.accenture.officehub_v1.repository;

import com.accenture.officehub_v1.entity.Layout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LayoutRepository extends JpaRepository<Layout, UUID> {

    Optional<Layout> findBySalaIdAndAtivoTrue(UUID salaId);

    List<Layout> findBySalaId(UUID salaId);
}
