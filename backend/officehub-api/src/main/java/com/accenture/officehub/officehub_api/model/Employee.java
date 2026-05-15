package com.accenture.officehub.officehub_api.model;

import com.accenture.officehub.officehub_api.enums.ProfessionalProfile;

/**
 * Colaborador em memória (sem banco) — equipe, perfil e visibilidade para recomendações.
 */
public class Employee {
    private Long id;
    private String displayName;
    private Long teamId;
    private ProfessionalProfile professionalProfile;
    private boolean hidePresenceFromTeam;
    /** Horário típico de chegada (exibição / contexto), opcional. */
    private String typicalStartTime;

    public Employee() {
    }

    public Employee(
            Long id,
            String displayName,
            Long teamId,
            ProfessionalProfile professionalProfile,
            boolean hidePresenceFromTeam,
            String typicalStartTime
    ) {
        this.id = id;
        this.displayName = displayName;
        this.teamId = teamId;
        this.professionalProfile = professionalProfile;
        this.hidePresenceFromTeam = hidePresenceFromTeam;
        this.typicalStartTime = typicalStartTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public ProfessionalProfile getProfessionalProfile() {
        return professionalProfile;
    }

    public void setProfessionalProfile(ProfessionalProfile professionalProfile) {
        this.professionalProfile = professionalProfile;
    }

    public boolean isHidePresenceFromTeam() {
        return hidePresenceFromTeam;
    }

    public void setHidePresenceFromTeam(boolean hidePresenceFromTeam) {
        this.hidePresenceFromTeam = hidePresenceFromTeam;
    }

    public String getTypicalStartTime() {
        return typicalStartTime;
    }

    public void setTypicalStartTime(String typicalStartTime) {
        this.typicalStartTime = typicalStartTime;
    }
}
