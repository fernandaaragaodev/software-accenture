package com.accenture.officehub.officehub_api.service.impl;

import com.accenture.officehub.officehub_api.dto.AvailablePositionTypeDto;
import com.accenture.officehub.officehub_api.dto.AvailableRoomForReservationDto;
import com.accenture.officehub.officehub_api.dto.RoomResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomPositionResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomStatusResponseDto;
import com.accenture.officehub.officehub_api.enums.ReservationStatus;
import com.accenture.officehub.officehub_api.enums.RoomStatus;
import com.accenture.officehub.officehub_api.exception.BadRequestException;
import com.accenture.officehub.officehub_api.exception.ForbiddenException;
import com.accenture.officehub.officehub_api.exception.ResourceNotFoundException;
import com.accenture.officehub.officehub_api.model.Room;
import com.accenture.officehub.officehub_api.model.RoomPosition;
import com.accenture.officehub.officehub_api.repository.ReservationRepository;
import com.accenture.officehub.officehub_api.repository.RoomRepository;
import com.accenture.officehub.officehub_api.service.ReservationService;
import com.accenture.officehub.officehub_api.service.RoomService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    public List<AvailablePositionTypeDto> listAvailablePositionTypes(String date, String start, String end) {
        reservationService.synchronizeStatuses();
        ReservationSlot slot = parseSlot(date, start, end);
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Room room : roomRepository.findAll()) {
            if (room.isBlocked() || room.getPositions() == null) {
                continue;
            }
            for (RoomPosition position : room.getPositions()) {
                if (!isPositionAvailable(room, position, slot)) {
                    continue;
                }
                String type = position.getType() == null ? "" : position.getType().trim();
                if (type.isEmpty()) {
                    continue;
                }
                counts.merge(type, 1, Integer::sum);
            }
        }
        return counts.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .map(e -> new AvailablePositionTypeDto(e.getKey(), e.getValue()))
                .toList();
    }

    @Override
    public List<AvailableRoomForReservationDto> listAvailableRoomsForReservation(
            String date,
            String start,
            String end,
            String seatType
    ) {
        if (seatType == null || seatType.isBlank()) {
            throw new BadRequestException("seatType e obrigatorio.");
        }
        reservationService.synchronizeStatuses();
        ReservationSlot slot = parseSlot(date, start, end);
        String normalizedType = seatType.trim();
        return roomRepository.findAll().stream()
                .filter(room -> !room.isBlocked())
                .map(room -> {
                    int matching = countAvailablePositionsOfType(room, normalizedType, slot);
                    return matching > 0
                            ? new AvailableRoomForReservationDto(
                            room.getId(),
                            room.getName(),
                            room.getCapacity(),
                            room.getDesks(),
                            displayStatus(room),
                            room.getEquipment(),
                            room.getFloor(),
                            room.getArea(),
                            matching
                    )
                            : null;
                })
                .filter(dto -> dto != null)
                .sorted(Comparator.comparing(AvailableRoomForReservationDto::name))
                .toList();
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

        ReservationSlot slot = parseSlot(date, start, end);

        if (room.isBlocked()) {
            return room.getPositions().stream()
                    .map(position -> new RoomPositionResponseDto(
                            position.getCode(),
                            position.getType(),
                            position.getEquipment(),
                            false,
                            position.isBlocked()
                    ))
                    .toList();
        }

        return room.getPositions().stream().map(position -> {
            boolean positionBlocked = position.isBlocked();
            boolean available = !positionBlocked && isPositionAvailable(room, position, slot);
            return new RoomPositionResponseDto(
                    position.getCode(),
                    position.getType(),
                    position.getEquipment(),
                    available,
                    positionBlocked
            );
        }).toList();
    }

    @Override
    public List<RoomPositionResponseDto> listRoomPositionsOverview(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala com id " + roomId + " nao encontrada."));
        return room.getPositions().stream()
                .map(position -> new RoomPositionResponseDto(
                        position.getCode(),
                        position.getType(),
                        position.getEquipment(),
                        !room.isBlocked() && !position.isBlocked(),
                        position.isBlocked()
                ))
                .toList();
    }

    @Override
    public void setPositionBlocked(Long roomId, String positionCode, boolean blocked, String requesterRole) {
        if (requesterRole == null || requesterRole.isBlank()) {
            throw new BadRequestException("requesterRole e obrigatorio.");
        }
        if (!requesterRole.trim().equalsIgnoreCase("admin")) {
            throw new ForbiddenException("Somente administrador pode bloquear ou desbloquear posicoes.");
        }
        if (positionCode == null || positionCode.isBlank()) {
            throw new BadRequestException("positionCode e obrigatorio.");
        }
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Sala com id " + roomId + " nao encontrada."));
        RoomPosition position = room.getPositions().stream()
                .filter(p -> p.getCode() != null && p.getCode().equalsIgnoreCase(positionCode.trim()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Posicao " + positionCode + " nao encontrada na sala " + room.getName() + "."
                ));
        position.setBlocked(blocked);
        roomRepository.save(room);
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

    private int countAvailablePositionsOfType(Room room, String seatType, ReservationSlot slot) {
        if (room.getPositions() == null) {
            return 0;
        }
        int count = 0;
        for (RoomPosition position : room.getPositions()) {
            if (position.getType() == null || !position.getType().equalsIgnoreCase(seatType)) {
                continue;
            }
            if (isPositionAvailable(room, position, slot)) {
                count++;
            }
        }
        return count;
    }

    private boolean isPositionAvailable(Room room, RoomPosition position, ReservationSlot slot) {
        if (room.isBlocked() || position.isBlocked()) {
            return false;
        }
        return !reservationRepository.findAll().stream()
                .filter(current -> current.getRoomId().equals(room.getId()))
                .filter(current -> current.getStatus() != ReservationStatus.cancelled)
                .filter(current -> current.getDate().equals(slot.date()))
                .anyMatch(current ->
                        current.getSeatCode() != null
                                && position.getCode() != null
                                && current.getSeatCode().equalsIgnoreCase(position.getCode())
                                && overlaps(slot.start(), slot.end(), current.getStart(), current.getEnd()));
    }

    private ReservationSlot parseSlot(String date, String start, String end) {
        LocalDate reservationDate = parseDate(date);
        LocalTime startTime = parseTime(start, "start");
        LocalTime endTime = parseTime(end, "end");
        if (!startTime.isBefore(endTime)) {
            throw new BadRequestException("Horario invalido: start deve ser antes de end.");
        }
        return new ReservationSlot(reservationDate, startTime, endTime);
    }

    private record ReservationSlot(LocalDate date, LocalTime start, LocalTime end) {}

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
