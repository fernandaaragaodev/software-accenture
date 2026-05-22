package com.accenture.officehub.officehub_api.model;

import java.util.ArrayList;
import java.util.List;

public class RoomPosition {
    private String code;
    private String type;
    private List<String> equipment;
    /** Bloqueio manual por administrador; não deriva de reservas. */
    private boolean blocked;

    public RoomPosition() {
        this.equipment = new ArrayList<>();
    }

    public RoomPosition(String code, String type, List<String> equipment) {
        this(code, type, equipment, false);
    }

    public RoomPosition(String code, String type, List<String> equipment, boolean blocked) {
        this.code = code;
        this.type = type;
        this.equipment = equipment == null ? new ArrayList<>() : new ArrayList<>(equipment);
        this.blocked = blocked;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<String> getEquipment() { return equipment; }
    public void setEquipment(List<String> equipment) { this.equipment = equipment == null ? new ArrayList<>() : new ArrayList<>(equipment); }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
