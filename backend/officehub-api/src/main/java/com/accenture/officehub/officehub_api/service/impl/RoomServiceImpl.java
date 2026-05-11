package com.accenture.officehub.officehub_api.service.impl;

import com.accenture.officehub.officehub_api.dto.RoomResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomPositionResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomStatusResponseDto;
import com.accenture.officehub.officehub_api.enums.ReservationStatus;
import com.accenture.officehub.officehub_api.enums.RoomStatus;
import com.accenture.officehub.officehub_api.exception.BadRequestException;
import com.accenture.officehub.officehub_api.exception.ForbiddenException;
import com.accenture.officehub.officehub_api.exception.ResourceNotFoundException;
import com.accenture.officehub.officehub_api.model.Room;
import com.accenture.officehub.officehub_api.repository.ReservationRepository;
import com.accenture.officehub.officehub_api.repository.RoomRepository;
import com.accenture.officehub.officehub_api.service.ReservationService;
import com.accenture.officehub.officehub_api.service.RoomService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final String adminRoomBlockPassword;

    public RoomServiceImpl(
            RoomRepository roomRepository,
            ReservationRepository reservationRepository,
            ReservationService reservationService,
            @Value("${officehub.admin.room-block-password}") String adminRoomBlockPassword
    ) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
        this.adminRoomBlockPassword = adminRoomBlockPassword;
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
        return new RoomStatusResponseDto(room.getId(), displayStatus(room));
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

        if (room.isBlocked()) {
            return room.getPositions().stream()
                    .map(position -> new RoomPositionResponseDto(
                            position.getCode(),
                            position.getType(),
                            position.getEquipment(),
                            false
                    ))
                    .toList();
        }

        return room.getPositions().stream().map(position -> {
            boolean occupied = reservationRepository.findAll().stream()
                    .filter(current -> current.getRoomId().equals(roomId))
                    .filter(current -> current.getStatus() != ReservationStatus.cancelled)
                    .filter(current -> current.getDate().equals(reservationDate))
                    .filter(current -> position.getCode().equals(current.getSeatCode()))
                    .anyMatch(current -> overlaps(startTime, endTime, current.getStart(), current.getEnd()));
            return new RoomPositionResponseDto(
                    position.getCode(),
                    position.getType(),
                    position.getEquipment(),
                    !occupied
            );
        }).toList();
    }

    @Override
    public void setRoomBlocked(Long roomId, boolean blocked, String requesterRole, String adminPassword) {
        if (requesterRole == null || requesterRole.isBlank()) {
            throw new BadRequestException("requesterRole e obrigatorio.");
        }
        if (!requesterRole.trim().equalsIgnoreCase("admin")) {
            throw new ForbiddenException("Somente administrador pode bloquear ou desbloquear salas.");
        }
        if (blocked) {
            if (adminPassword == null || adminPassword.isBlank()) {
                throw new BadRequestException("adminPassword e obrigatorio para desativar a sala.");
            }
            if (!adminRoomBlockPassword.equals(adminPassword)) {
                throw new ForbiddenException("Senha de confirmacao invalida.");
            }
        }
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala com id " + roomId + " nao encontrada."));
        room.setBlocked(blocked);
        roomRepository.save(room);
    }

    private RoomStatus displayStatus(Room room) {
        return room.isBlocked() ? RoomStatus.unavailable : RoomStatus.available;
    }

    private RoomResponseDto toDto(Room room) {
        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();
        long occupiedDesks = reservationRepository.findAll().stream()
                .filter(r -> r.getRoomId().equals(room.getId()))
                .filter(r -> r.getStatus() != ReservationStatus.cancelled)
                .filter(r -> r.getDate().equals(today))
                .filter(r -> overlaps(now, now.plusSeconds(1), r.getStart(), r.getEnd()))
                .count();
        return new RoomResponseDto(
                room.getId(),
                room.getName(),
                room.getCapacity(),
                room.getDesks(),
                displayStatus(room),
                room.getEquipment(),
                room.getFloor(),
                room.getArea(),
                (int) occupiedDesks
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

    private boolean overlaps(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        return startA.isBefore(endB) && endA.isAfter(startB);
    }
}
