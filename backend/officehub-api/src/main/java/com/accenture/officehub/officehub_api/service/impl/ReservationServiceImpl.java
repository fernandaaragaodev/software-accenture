package com.accenture.officehub.officehub_api.service.impl;

import com.accenture.officehub.officehub_api.dto.ReservationRequestDto;
import com.accenture.officehub.officehub_api.dto.ReservationResponseDto;
import com.accenture.officehub.officehub_api.dto.ReservationGroupMemberDto;
import com.accenture.officehub.officehub_api.dto.ReservationGroupResponseDto;
import com.accenture.officehub.officehub_api.enums.ReservationStatus;
import com.accenture.officehub.officehub_api.enums.RoomStatus;
import com.accenture.officehub.officehub_api.exception.BadRequestException;
import com.accenture.officehub.officehub_api.exception.ConflictException;
import com.accenture.officehub.officehub_api.exception.ForbiddenException;
import com.accenture.officehub.officehub_api.exception.ResourceNotFoundException;
import com.accenture.officehub.officehub_api.model.Reservation;
import com.accenture.officehub.officehub_api.model.Room;
import com.accenture.officehub.officehub_api.repository.ReservationRepository;
import com.accenture.officehub.officehub_api.repository.RoomRepository;
import com.accenture.officehub.officehub_api.service.NotificationService;
import com.accenture.officehub.officehub_api.service.ReservationService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final NotificationService notificationService;

    public ReservationServiceImpl(
            ReservationRepository reservationRepository,
            RoomRepository roomRepository,
            NotificationService notificationService
    ) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.notificationService = notificationService;
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
    public List<ReservationGroupResponseDto> listReservationGroups() {
        synchronizeStatuses();
        List<Reservation> all = reservationRepository.findAll().stream()
                .sorted(Comparator.comparing(Reservation::getDate).thenComparing(Reservation::getStart))
                .toList();

        Map<String, List<Reservation>> groups = new LinkedHashMap<>();
        for (Reservation reservation : all) {
            String key = reservation.getReservationGroupId() != null
                    ? reservation.getReservationGroupId()
                    : "SINGLE-" + reservation.getId();
            groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(reservation);
        }

        return groups.entrySet().stream().map(entry -> toGroupDto(entry.getKey(), entry.getValue())).toList();
    }

    @Override
    public ReservationGroupResponseDto getReservationGroup(String groupId) {
        synchronizeStatuses();
        List<Reservation> all = reservationRepository.findAll();
        List<Reservation> group = all.stream()
                .filter(reservation -> groupId.equals(reservation.getReservationGroupId()) || ("SINGLE-" + reservation.getId()).equals(groupId))
                .sorted(Comparator.comparing(Reservation::getDate).thenComparing(Reservation::getStart))
                .toList();
        if (group.isEmpty()) {
            throw new ResourceNotFoundException("Grupo de reserva " + groupId + " nao encontrado.");
        }
        return toGroupDto(groupId, group);
    }

    @Override
    public ReservationResponseDto createReservation(ReservationRequestDto request) {
        List<Reservation> currentReservations = reservationRepository.findAll();
        Reservation prepared = prepareReservation(request, currentReservations);
        Reservation created = reservationRepository.save(prepared);

        notificationService.createReservationConfirmedNotification(created);
        synchronizeStatuses();
        return toDto(created);
    }

    @Override
    public List<ReservationResponseDto> createReservationsBatch(List<ReservationRequestDto> requests) {
        if (requests == null || requests.isEmpty()) {
            throw new BadRequestException("Lista de reservas em lote nao pode ser vazia.");
        }
        String role = requests.getFirst().requesterRole().trim().toLowerCase();
        if (role.equals("employee")) {
            throw new BadRequestException("Reserva em lote permitida apenas para gestor ou admin.");
        }
        if (requests.size() < 2) {
            throw new BadRequestException("Reserva em lote exige ao menos 2 reservas.");
        }

        String reservationGroupId = "GRP-" + System.currentTimeMillis();
        List<Reservation> currentReservations = new ArrayList<>(reservationRepository.findAll());
        List<Reservation> preparedReservations = new ArrayList<>();
        for (ReservationRequestDto request : requests) {
            Reservation prepared = prepareReservation(request, currentReservations);
            prepared.setReservationGroupId(reservationGroupId);
            preparedReservations.add(prepared);
            currentReservations.add(prepared);
        }

        List<ReservationResponseDto> created = new ArrayList<>();
        for (Reservation prepared : preparedReservations) {
            Reservation persisted = reservationRepository.save(prepared);
            created.add(toDto(persisted));
        }
        if (!preparedReservations.isEmpty()) {
            Reservation first = preparedReservations.getFirst();
            notificationService.createReservationGroupConfirmedNotification(
                    reservationGroupId,
                    first.getRoom(),
                    first.getDate().toString(),
                    first.getStart().toString(),
                    first.getEnd().toString(),
                    preparedReservations.size()
            );
        }
        synchronizeStatuses();
        return created;
    }

    @Override
    public void cancelReservation(Long reservationId, String cancellerName, String cancellerRole) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva com id " + reservationId + " nao encontrada."));

        assertCanManageCancellation(reservation.getUser(), cancellerName, cancellerRole);

        if (reservation.getStatus() == ReservationStatus.cancelled) {
            // idempotente: nao quebra, apenas sincroniza e finaliza
            synchronizeStatuses();
            return;
        }

        reservation.setStatus(ReservationStatus.cancelled);
        reservationRepository.save(reservation);
        notificationService.createReservationCancelledNotification(reservation);
        synchronizeStatuses();
    }

    @Override
    public void cancelReservationGroup(String groupId, String cancellerName, String cancellerRole) {
        List<Reservation> all = reservationRepository.findAll();
        List<Reservation> targets = all.stream()
                .filter(reservation ->
                        groupId.equals(reservation.getReservationGroupId())
                                || ("SINGLE-" + reservation.getId()).equals(groupId))
                .toList();
        if (targets.isEmpty()) {
            throw new ResourceNotFoundException("Grupo de reserva " + groupId + " nao encontrado.");
        }

        if (!isAdminRole(cancellerRole)) {
            boolean allOwnedByCanceller = targets.stream()
                    .map(Reservation::getUser)
                    .allMatch(user -> user.equalsIgnoreCase(cancellerName.trim()));
            if (!allOwnedByCanceller) {
                throw new ForbiddenException("Somente administrador pode cancelar reservas de outras pessoas.");
            }
        }

        for (Reservation reservation : targets) {
            if (reservation.getStatus() != ReservationStatus.cancelled) {
                reservation.setStatus(ReservationStatus.cancelled);
                reservationRepository.save(reservation);
            }
        }

        Reservation first = targets.getFirst();
        notificationService.createReservationGroupCancelledNotification(
                groupId,
                first.getRoom(),
                first.getDate().toString(),
                first.getStart().toString(),
                first.getEnd().toString(),
                targets.size()
        );

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
                reservation.getRequesterRole(),
                reservation.getReservationGroupId(),
                reservation.getSeatCode(),
                reservation.getSeatType(),
                reservation.getRequestedEquipment(),
                reservation.getDate().toString(),
                reservation.getStart().toString(),
                reservation.getEnd().toString(),
                reservation.getStatus()
        );
    }

    private ReservationGroupResponseDto toGroupDto(String groupId, List<Reservation> reservations) {
        Reservation first = reservations.getFirst();
        boolean isBatch = first.getReservationGroupId() != null;
        int peopleCount = reservations.size();

        ReservationStatus status = ReservationStatus.confirmed;
        boolean anyActive = reservations.stream().anyMatch(r -> r.getStatus() == ReservationStatus.active);
        boolean allCancelled = reservations.stream().allMatch(r -> r.getStatus() == ReservationStatus.cancelled);
        if (allCancelled) status = ReservationStatus.cancelled;
        else if (anyActive) status = ReservationStatus.active;

        List<ReservationGroupMemberDto> members = reservations.stream()
                .map(reservation -> new ReservationGroupMemberDto(
                        reservation.getId(),
                        reservation.getUser(),
                        reservation.getSeatCode(),
                        reservation.getSeatType(),
                        reservation.getRequestedEquipment(),
                        reservation.getStatus()
                ))
                .toList();

        return new ReservationGroupResponseDto(
                groupId,
                isBatch,
                first.getRoom(),
                first.getRequesterRole(),
                first.getDate().toString(),
                first.getStart().toString(),
                first.getEnd().toString(),
                status,
                peopleCount,
                members
        );
    }

    private Reservation prepareReservation(ReservationRequestDto request, List<Reservation> existingReservations) {
        if (request.roomId() == null) {
            throw new BadRequestException("roomId e obrigatorio.");
        }
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Sala com id " + request.roomId() + " nao encontrada."));

        LocalDate date = parseDate(request.date());
        LocalTime start = parseTime(request.start(), "start");
        LocalTime end = parseTime(request.end(), "end");

        validateReservationDate(date);

        if (!start.isBefore(end)) {
            throw new BadRequestException("Horario invalido: start deve ser antes de end.");
        }

        String role = request.requesterRole().trim().toLowerCase();
        if (!role.equals("employee") && !role.equals("manager") && !role.equals("admin")) {
            throw new BadRequestException("requesterRole invalido. Use employee, manager ou admin.");
        }
        if (role.equals("employee") && !request.requesterName().equalsIgnoreCase(request.user())) {
            throw new BadRequestException("Funcionario pode reservar apenas para si mesmo.");
        }
        if (request.seatCode().isBlank()) {
            throw new BadRequestException("seatCode e obrigatorio.");
        }
        if (request.requestedEquipment() == null || request.requestedEquipment().size() != 1) {
            throw new BadRequestException("Cada reserva deve conter exatamente 1 recurso para a mesa.");
        }

        boolean hasSeatConflict = existingReservations.stream()
                .filter(reservation -> reservation.getStatus() != ReservationStatus.cancelled)
                .filter(reservation -> reservation.getRoomId().equals(room.getId()))
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getSeatCode().equalsIgnoreCase(request.seatCode()))
                .anyMatch(reservation -> overlaps(start, end, reservation.getStart(), reservation.getEnd()));
        if (hasSeatConflict) {
            throw new ConflictException("Conflito de horario: posicao " + request.seatCode() + " indisponivel nesse intervalo.");
        }

        if (role.equals("employee")) {
            boolean hasOwnConflict = existingReservations.stream()
                    .filter(reservation -> reservation.getStatus() != ReservationStatus.cancelled)
                    .filter(reservation -> reservation.getUser().equalsIgnoreCase(request.user()))
                    .filter(reservation -> reservation.getDate().equals(date))
                    .anyMatch(reservation -> overlaps(start, end, reservation.getStart(), reservation.getEnd()));
            if (hasOwnConflict) {
                throw new ConflictException("Funcionario ja possui reserva para esse horario.");
            }
        }

        ReservationStatus initialStatus = computeReservationStatus(date, start, end);
        return new Reservation(
                null,
                room.getId(),
                room.getName(),
                request.user(),
                role,
                null,
                request.seatCode(),
                request.seatType(),
                request.requestedEquipment(),
                date,
                start,
                end,
                initialStatus
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

    private void validateReservationDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new BadRequestException("Nao e possivel reservar para datas que ja passaram.");
        }
        LocalDate earliestAllowed = today.plusDays(7);
        if (date.isBefore(earliestAllowed)) {
            throw new BadRequestException("Reservas exigem pelo menos 7 dias de antecedencia.");
        }
    }

    private boolean isAdminRole(String role) {
        return role != null && role.trim().equalsIgnoreCase("admin");
    }

    private void assertCanManageCancellation(String reservationUser, String cancellerName, String cancellerRole) {
        if (cancellerName == null || cancellerName.isBlank()) {
            throw new BadRequestException("requesterName e obrigatorio para cancelar.");
        }
        if (cancellerRole == null || cancellerRole.isBlank()) {
            throw new BadRequestException("requesterRole e obrigatorio para cancelar.");
        }
        if (isAdminRole(cancellerRole)) {
            return;
        }
        if (reservationUser.equalsIgnoreCase(cancellerName.trim())) {
            return;
        }
        throw new ForbiddenException("Somente administrador pode cancelar reservas de outras pessoas.");
    }
}
