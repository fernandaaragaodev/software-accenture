package com.accenture.officehub.officehub_api.service.impl;

import com.accenture.officehub.officehub_api.dto.RoomResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomStatusResponseDto;
import com.accenture.officehub.officehub_api.exception.ResourceNotFoundException;
import com.accenture.officehub.officehub_api.model.Room;
import com.accenture.officehub.officehub_api.repository.RoomRepository;
import com.accenture.officehub.officehub_api.service.ReservationService;
import com.accenture.officehub.officehub_api.service.RoomService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ReservationService reservationService;

    public RoomServiceImpl(RoomRepository roomRepository, ReservationService reservationService) {
        this.roomRepository = roomRepository;
        this.reservationService = reservationService;
    }

    @Override
    public List<RoomResponseDto> listRooms() {
        reservationService.synchronizeStatuses();
        return roomRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    public RoomStatusResponseDto getRoomStatus(Long roomId) {
        reservationService.synchronizeStatuses();
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala com id " + roomId + " nao encontrada."));
        return new RoomStatusResponseDto(room.getId(), room.getStatus());
    }

    private RoomResponseDto toDto(Room room) {
        return new RoomResponseDto(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getDesks(),
                room.getStatus(),
                room.getEquipment(),
                room.getFloor(),
                room.getArea()
        );
    }
}
