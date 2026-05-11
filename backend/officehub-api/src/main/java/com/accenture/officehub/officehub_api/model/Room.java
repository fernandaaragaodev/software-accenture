package com.accenture.officehub.officehub_api.model;

import com.accenture.officehub.officehub_api.enums.RoomStatus;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private Long id;
    private String name;
    private Integer capacity;
    private RoomStatus status;
    private List<String> equipment;
    private String floor;
    private Integer area;
    private List<RoomPosition> positions;
    /** Bloqueio manual por administrador; não deriva de reservas. */
    private boolean blocked;

    public Room() {
        this.equipment = new ArrayList<>();
        this.positions = new ArrayList<>();
    }

    public Room(Long id, String name, Integer capacity, RoomStatus status, List<String> equipment, String floor, Integer area, List<RoomPosition> positions) {
        this(id, name, capacity, status, equipment, floor, area, positions, false);
    }

    public Room(Long id, String name, Integer capacity, RoomStatus status, List<String> equipment, String floor, Integer area, List<RoomPosition> positions, boolean blocked) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.status = status;
        this.equipment = equipment == null ? new ArrayList<>() : new ArrayList<>(equipment);
        this.floor = floor;
        this.area = area;
        this.positions = positions == null ? new ArrayList<>() : new ArrayList<>(positions);
        this.blocked = blocked;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    /** Número de posições/mesas desta sala. */
    public Integer getDesks() { return positions != null ? positions.size() : 0; }
    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
    public List<String> getEquipment() { return equipment; }
    public void setEquipment(List<String> equipment) { this.equipment = equipment == null ? new ArrayList<>() : new ArrayList<>(equipment); }
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    public Integer getArea() { return area; }
    public void setArea(Integer area) { this.area = area; }
    public List<RoomPosition> getPositions() { return positions; }
    public void setPositions(List<RoomPosition> positions) { this.positions = positions == null ? new ArrayList<>() : new ArrayList<>(positions); }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}
