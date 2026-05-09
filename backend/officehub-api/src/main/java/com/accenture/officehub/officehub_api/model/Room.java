package com.accenture.officehub.officehub_api.model;

import com.accenture.officehub.officehub_api.enums.RoomStatus;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private Long id;
    private String name;
    private Integer capacity;
    private Integer desks;
    private RoomStatus status;
    private List<String> equipment;
    private String floor;
    private Integer area;

    public Room() {
        this.equipment = new ArrayList<>();
    }

    public Room(Long id, String name, Integer capacity, Integer desks, RoomStatus status, List<String> equipment, String floor, Integer area) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.desks = desks;
        this.status = status;
        this.equipment = equipment == null ? new ArrayList<>() : new ArrayList<>(equipment);
        this.floor = floor;
        this.area = area;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public Integer getDesks() { return desks; }
    public void setDesks(Integer desks) { this.desks = desks; }
    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
    public List<String> getEquipment() { return equipment; }
    public void setEquipment(List<String> equipment) { this.equipment = equipment == null ? new ArrayList<>() : new ArrayList<>(equipment); }
    public String getFloor() { return floor; }
    public void setFloor(String floor) { this.floor = floor; }
    public Integer getArea() { return area; }
    public void setArea(Integer area) { this.area = area; }
}
