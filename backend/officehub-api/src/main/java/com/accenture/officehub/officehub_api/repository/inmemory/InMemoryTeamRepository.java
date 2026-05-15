package com.accenture.officehub.officehub_api.repository.inmemory;

import com.accenture.officehub.officehub_api.model.Team;
import com.accenture.officehub.officehub_api.repository.TeamRepository;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryTeamRepository implements TeamRepository {

    private final List<Team> teams = new CopyOnWriteArrayList<>();

    @Override
    public List<Team> findAll() {
        return teams.stream().sorted(Comparator.comparing(Team::getId)).map(this::copy).toList();
    }

    @Override
    public Optional<Team> findById(Long id) {
        return teams.stream().filter(t -> t.getId().equals(id)).findFirst().map(this::copy);
    }

    @Override
    public Team save(Team team) {
        teams.removeIf(t -> t.getId().equals(team.getId()));
        Team copy = copy(team);
        teams.add(copy);
        return copy(copy);
    }

    @Override
    public void saveAll(List<Team> newTeams) {
        teams.clear();
        newTeams.stream().map(this::copy).forEach(teams::add);
    }

    private Team copy(Team t) {
        return new Team(t.getId(), t.getName(), t.getPreferredFloor());
    }
}
