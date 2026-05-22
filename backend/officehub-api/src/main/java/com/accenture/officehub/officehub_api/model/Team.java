package com.accenture.officehub.officehub_api.model;

/**
 * Equipe com piso preferencial (âncora para sugestão de proximidade).
 */
public class Team {
    private Long id;
    private String name;
    /** Valor alinhado ao campo {@link com.accenture.officehub.officehub_api.model.Room#getFloor()}. */
    private String preferredFloor;

    public Team() {
    }

    public Team(Long id, String name, String preferredFloor) {
        this.id = id;
        this.name = name;
        this.preferredFloor = preferredFloor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPreferredFloor() {
        return preferredFloor;
    }

    public void setPreferredFloor(String preferredFloor) {
        this.preferredFloor = preferredFloor;
    }
}
