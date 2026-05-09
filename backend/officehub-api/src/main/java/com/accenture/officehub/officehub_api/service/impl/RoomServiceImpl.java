package com.accenture.officehub.officehub_api.service.impl;

import com.accenture.officehub.officehub_api.dto.RoomResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomPositionResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomStatusResponseDto;
import com.accenture.officehub.officehub_api.enums.ReservationStatus;
import com.accenture.officehub.officehub_api.exception.BadRequestException;
import com.accenture.officehub.officehub_api.exception.ResourceNotFoundException;
import com.accenture.officehub.officehub_api.model.Room;
import com.accenture.officehub.officehub_api.repository.ReservationRepository;
import com.accenture.officehub.officehub_api.repository.RoomRepository;
import com.accenture.officehub.officehub_api.service.ReservationService;
import com.accenture.officehub.officehub_api.service.RoomService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;

    public RoomServiceImpl(
            RoomRepository roomRepository,
            ReservationRepository reservationRepository,
            ReservationService reservationService
    ) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
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

    @Override
    public List<RoomPositionResponseDto> listRoomPositions(Long roomId, String date, String start, String end) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala com id " + roomId + " nao encontrada."));

        LocalDate reservationDate = parseDate(date);
        LocalTime startTime = parseTime(start, "start");
        LocalTime endTime = parseTime(end, "end");
        if (!startTime.isBefore(endTime)) {
            throw new BadRequestException("Horario invalido: start deve ser antes de end.");
        }

        List<RoomPositionResponseDto> positions = new ArrayList<>();
        for (int index = 1; index <= room.getDesks(); index++) {
            String seatCode = "P" + index;
            List<String> availableEquipment = buildSeatEquipment(index);
            boolean occupied = reservationRepository.findAll().stream()
                    .filter(current -> current.getRoomId().equals(roomId))
                    .filter(current -> current.getStatus() != ReservationStatus.cancelled)
                    .filter(current -> current.getDate().equals(reservationDate))
                    .filter(current -> seatCode.equals(current.getSeatCode()))
                    .anyMatch(current -> overlaps(startTime, endTime, current.getStart(), current.getEnd()));
            positions.add(new RoomPositionResponseDto(
                    seatCode,
                    seatType(index),
                    availableEquipment,
                    !occupied
            ));
        }
        return positions;
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

    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Data invalida. Use formato yyyy-MM-dd.");
        }
    }

    private LocalTime parseTime(String value, String field) {
        try {
            return LocalTime.parse(value);
        } catch (DateTimeParseException ex) {
            throw new BadRequestException("Horario invalido para campo " + field + ". Use formato HH:mm.");
        }
    }

    private String seatType(int index) {
        if (index % 5 == 0) return "Mesa com digitalizadora";
        if (index % 3 == 0) return "Mesa com PC maior";
        if (index % 2 == 0) return "Mesa maior";
        return "Mesa vazia";
    }

    private List<String> buildSeatEquipment(int index) {
        List<String> equipment = new ArrayList<>();
        equipment.add("Mesa ergonômica");
        if (index % 2 == 0) equipment.add("Monitor 27");
        if (index % 3 == 0) equipment.add("PC Workstation");
        if (index % 5 == 0) equipment.add("Mesa digitalizadora");
        return equipment;
    }

    private boolean overlaps(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        return startA.isBefore(endB) && endA.isAfter(startB);
    }
}
