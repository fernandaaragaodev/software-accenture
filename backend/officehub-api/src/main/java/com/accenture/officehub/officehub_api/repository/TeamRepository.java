package com.accenture.officehub.officehub_api.repository;

import com.accenture.officehub.officehub_api.model.Team;

import java.util.List;
import java.util.Optional;

public interface TeamRepository {
    List<Team> findAll();

    Optional<Team> findById(Long id);

    Team save(Team team);

    void saveAll(List<Team> teams);
}
