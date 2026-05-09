package com.accenture.officehub.officehub_api.service.impl;

import com.accenture.officehub.officehub_api.dto.ReservationRequestDto;
import com.accenture.officehub.officehub_api.dto.ReservationResponseDto;
import com.accenture.officehub.officehub_api.enums.ReservationStatus;
import com.accenture.officehub.officehub_api.enums.RoomStatus;
import com.accenture.officehub.officehub_api.exception.BadRequestException;
import com.accenture.officehub.officehub_api.exception.ConflictException;
import com.accenture.officehub.officehub_api.exception.ResourceNotFoundException;
import com.accenture.officehub.officehub_api.model.Reservation;
import com.accenture.officehub.officehub_api.model.Room;
import com.accenture.officehub.officehub_api.repository.ReservationRepository;
import com.accenture.officehub.officehub_api.repository.RoomRepository;
import com.accenture.officehub.officehub_api.service.ReservationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public List<ReservationResponseDto> listReservations() {
        synchronizeStatuses();
        return reservationRepository.findAll().stream()
                .sorted(Comparator.comparing(Reservation::getDate).thenComparing(Reservation::getStart))
                .map(this::toDto)
                .toList();
    }

    @Override
    public ReservationResponseDto createReservation(ReservationRequestDto request) {
        if (request.roomId() == null) {
            throw new BadRequestException("roomId e obrigatorio.");
        }
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Sala com id " + request.roomId() + " nao encontrada."));

        LocalDate date = parseDate(request.date());
        LocalTime start = parseTime(request.start(), "start");
        LocalTime end = parseTime(request.end(), "end");

        if (!start.isBefore(end)) {
            throw new BadRequestException("Horario invalido: start deve ser antes de end.");
        }

        List<Reservation> existing = reservationRepository.findAll();
        boolean hasConflict = existing.stream()
                .filter(r -> !r.getStatus().equals(ReservationStatus.cancelled))
                .filter(r -> r.getRoomId().equals(room.getId()))
                .filter(r -> r.getDate().equals(date))
                .anyMatch(r -> overlaps(start, end, r.getStart(), r.getEnd()));

        if (hasConflict) {
            throw new ConflictException("Conflito de horario: ja existe reserva para essa sala no intervalo informado.");
        }

        ReservationStatus initialStatus = computeReservationStatus(date, start, end);

        Reservation created = reservationRepository.save(
                new Reservation(
                        null,
                        room.getId(),
                        room.getName(),
                        request.user(),
                        date,
                        start,
                        end,
                        initialStatus
                )
        );

        synchronizeStatuses();
        return toDto(created);
    }

    @Override
    public void cancelReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva com id " + reservationId + " nao encontrada."));

        if (reservation.getStatus() == ReservationStatus.cancelled) {
            // idempotente: nao quebra, apenas sincroniza e finaliza
            synchronizeStatuses();
            return;
        }

        reservation.setStatus(ReservationStatus.cancelled);
        reservationRepository.save(reservation);
        synchronizeStatuses();
    }

    @Override
    public void synchronizeStatuses() {
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        List<Reservation> allReservations = reservationRepository.findAll();
        List<Reservation> updatedReservations = new ArrayList<>();
        for (Reservation reservation : allReservations) {
            if (reservation.getStatus() == ReservationStatus.cancelled) {
                updatedReservations.add(reservation);
                continue;
            }
            ReservationStatus recalculated = computeReservationStatus(
                    reservation.getDate(),
                    reservation.getStart(),
                    reservation.getEnd(),
                    nowDate,
                    nowTime
            );
            reservation.setStatus(recalculated);
            updatedReservations.add(reservation);
        }
        reservationRepository.saveAll(updatedReservations);

        List<Room> rooms = roomRepository.findAll();
        List<Reservation> activeReservations = reservationRepository.findAll().stream()
                .filter(r -> r.getStatus() != ReservationStatus.cancelled)
                .toList();

        for (Room room : rooms) {
            RoomStatus status = RoomStatus.available;
            boolean hasActive = activeReservations.stream()
                    .anyMatch(r -> r.getRoomId().equals(room.getId()) && r.getStatus() == ReservationStatus.active);
            if (hasActive) {
                status = RoomStatus.occupied;
            } else {
                boolean hasConfirmed = activeReservations.stream()
                        .anyMatch(r -> r.getRoomId().equals(room.getId()) && r.getStatus() == ReservationStatus.confirmed);
                if (hasConfirmed) {
                    status = RoomStatus.reserved;
                }
            }
            room.setStatus(status);
            roomRepository.save(room);
        }
    }

    private ReservationResponseDto toDto(Reservation reservation) {
        return new ReservationResponseDto(
                reservation.getId(),
                reservation.getRoom(),
                reservation.getUser(),
                reservation.getDate().toString(),
                reservation.getStart().toString(),
                reservation.getEnd().toString(),
                reservation.getStatus()
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

    private ReservationStatus computeReservationStatus(LocalDate date, LocalTime start, LocalTime end) {
        return computeReservationStatus(date, start, end, LocalDate.now(), LocalTime.now());
    }

    private ReservationStatus computeReservationStatus(LocalDate date, LocalTime start, LocalTime end, LocalDate nowDate, LocalTime nowTime) {
        if (date.isEqual(nowDate) && !nowTime.isBefore(start) && nowTime.isBefore(end)) {
            return ReservationStatus.active;
        }
        return ReservationStatus.confirmed;
    }

    private boolean overlaps(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        return startA.isBefore(endB) && endA.isAfter(startB);
    }
}
