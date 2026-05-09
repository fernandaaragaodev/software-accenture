package com.accenture.officehub.officehub_api.repository.inmemory;

import com.accenture.officehub.officehub_api.model.Room;
import com.accenture.officehub.officehub_api.repository.RoomRepository;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class InMemoryRoomRepository implements RoomRepository {

    private final List<Room> rooms = new CopyOnWriteArrayList<>();

    @Override
    public List<Room> findAll() {
        return rooms.stream()
                .sorted(Comparator.comparing(Room::getId))
                .map(this::copy)
                .toList();
    }

    @Override
    public Optional<Room> findById(Long id) {
        return rooms.stream().filter(r -> r.getId().equals(id)).findFirst().map(this::copy);
    }

    @Override
    public Room save(Room room) {
        rooms.removeIf(r -> r.getId().equals(room.getId()));
        Room copy = copy(room);
        rooms.add(copy);
        return copy(copy);
    }

    @Override
    public void saveAll(List<Room> newRooms) {
        rooms.clear();
        newRooms.stream().map(this::copy).forEach(rooms::add);
    }

    private Room copy(Room room) {
        return new Room(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getDesks(),
                room.getStatus(),
                room.getEquipment() == null ? new ArrayList<>() : room.getEquipment(),
                room.getFloor(),
                room.getArea()
        );
    }
}
