package com.accenture.officehub.officehub_api.repository;

import com.accenture.officehub.officehub_api.model.Room;

import java.util.List;
import java.util.Optional;

public interface RoomRepository {
    List<Room> findAll();
    Optional<Room> findById(Long id);
    Room save(Room room);
    void saveAll(List<Room> rooms);
}
