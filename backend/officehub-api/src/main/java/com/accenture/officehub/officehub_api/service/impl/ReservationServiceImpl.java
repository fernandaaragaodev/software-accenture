package com.accenture.officehub.officehub_api.service.impl;

import com.accenture.officehub.officehub_api.dto.ReservationRequestDto;
import com.accenture.officehub.officehub_api.dto.ReservationResponseDto;
import com.accenture.officehub.officehub_api.dto.ReservationGroupMemberDto;
import com.accenture.officehub.officehub_api.dto.ReservationGroupResponseDto;
import com.accenture.officehub.officehub_api.enums.ReservationStatus;
import com.accenture.officehub.officehub_api.exception.BadRequestException;
import com.accenture.officehub.officehub_api.exception.ConflictException;
import com.accenture.officehub.officehub_api.exception.ForbiddenException;
import com.accenture.officehub.officehub_api.exception.ResourceNotFoundException;
import com.accenture.officehub.officehub_api.model.Reservation;
import com.accenture.officehub.officehub_api.model.Room;
import com.accenture.officehub.officehub_api.model.RoomPosition;
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
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final ReentrantLock reservationLock = new ReentrantLock();

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

    private <T> T withLock(Supplier<T> supplier) {
        reservationLock.lock();
        try {
            return supplier.get();
        } finally {
            reservationLock.unlock();
        }
    }

    private void withLock(Runnable runnable) {
        reservationLock.lock();
        try {
            runnable.run();
        } finally {
            reservationLock.unlock();
        }
    }

    private static String reservationGroupKey(Reservation reservation) {
        return reservation.getReservationGroupId() != null
                ? reservation.getReservationGroupId()
                : "SINGLE-" + reservation.getId();
    }

    private static boolean groupIdMatches(Reservation reservation, String groupId) {
        return groupId.equals(reservation.getReservationGroupId())
                || reservationGroupKey(reservation).equals(groupId);
    }

    @Override
    public List<ReservationResponseDto> listReservations() {
        return withLock(() -> {
            doSynchronizeStatuses();
            return reservationRepository.findAll().stream()
                    .sorted(Comparator.comparing(Reservation::getDate).thenComparing(Reservation::getStart))
                    .map(this::toDto)
                    .toList();
        });
    }

    @Override
    public List<ReservationGroupResponseDto> listReservationGroups() {
        return withLock(() -> {
            doSynchronizeStatuses();
            List<Reservation> all = reservationRepository.findAll().stream()
                    .sorted(Comparator.comparing(Reservation::getDate).thenComparing(Reservation::getStart))
                    .toList();

            Map<String, List<Reservation>> groups = new LinkedHashMap<>();
            for (Reservation res : all) {
                String groupKey = reservationGroupKey(res);
                groups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(res);
            }

            return groups.entrySet().stream()
                    .map(entry -> toGroupDto(entry.getKey(), entry.getValue()))
                    .toList();
        });
    }

    @Override
    public ReservationGroupResponseDto getReservationGroup(String groupId) {
        return withLock(() -> {
            doSynchronizeStatuses();
            List<Reservation> all = reservationRepository.findAll();
            List<Reservation> group = all.stream()
                    .filter(r -> groupIdMatches(r, groupId))
                    .sorted(Comparator.comparing(Reservation::getDate).thenComparing(Reservation::getStart))
                    .toList();
            if (group.isEmpty()) {
                throw new ResourceNotFoundException("Grupo de reserva " + groupId + " nao encontrado.");
            }
            return toGroupDto(groupId, group);
        });
    }

    @Override
    public ReservationResponseDto createReservation(ReservationRequestDto request) {
        return withLock(() -> {
            List<Reservation> currentReservations = reservationRepository.findAll();
            Reservation prepared = prepareReservation(request, currentReservations);
            Reservation created = reservationRepository.save(prepared);

            notificationService.createReservationConfirmedNotification(created);
            doSynchronizeStatuses();
            return toDto(created);
        });
    }

    @Override
    public List<ReservationResponseDto> createReservationsBatch(List<ReservationRequestDto> requests) {
        return withLock(() -> {
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

            for (ReservationRequestDto item : requests) {
                String itemRole = item.requesterRole().trim().toLowerCase();
                if (!itemRole.equals(role)) {
                    throw new BadRequestException("Todas as reservas em lote devem ter o mesmo requesterRole.");
                }
            }

            ReservationRequestDto batchTemplate = requests.getFirst();
            for (int i = 1; i < requests.size(); i++) {
                ReservationRequestDto item = requests.get(i);
                if (!batchTemplate.roomId().equals(item.roomId())) {
                    throw new BadRequestException("Reserva em lote deve ser na mesma sala (roomId).");
                }
                if (!batchTemplate.date().equals(item.date())
                        || !batchTemplate.start().equals(item.start())
                        || !batchTemplate.end().equals(item.end())) {
                    throw new BadRequestException("Reserva em lote deve usar a mesma data e horario para todas as posicoes.");
                }
            }

            String batchRequesterName = batchTemplate.requesterName().trim();
            for (ReservationRequestDto item : requests) {
                if (!item.requesterName().trim().equalsIgnoreCase(batchRequesterName)) {
                    throw new BadRequestException("Todas as reservas em lote devem ter o mesmo requesterName.");
                }
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

            List<Reservation> persisted = reservationRepository.saveBatch(preparedReservations);
            List<ReservationResponseDto> created = persisted.stream().map(this::toDto).toList();

            if (!persisted.isEmpty()) {
                Reservation first = persisted.getFirst();
            notificationService.createReservationGroupConfirmedNotification(
                    reservationGroupId,
                    first.getRoom(),
                    first.getDate().toString(),
                    first.getStart().toString(),
                    first.getEnd().toString(),
                    persisted.size(),
                    first.getRequesterName() == null ? "" : first.getRequesterName().trim()
            );
            }
            doSynchronizeStatuses();
            return created;
        });
    }

    @Override
    public void cancelReservation(Long reservationId, String cancellerName, String cancellerRole) {
        withLock(() -> {
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new ResourceNotFoundException("Reserva com id " + reservationId + " nao encontrada."));

            assertCanCancelReservation(reservation, cancellerName, cancellerRole);

            if (reservation.getStatus() == ReservationStatus.cancelled) {
                doSynchronizeStatuses();
                return;
            }

            reservation.setStatus(ReservationStatus.cancelled);
            reservationRepository.save(reservation);
            notificationService.createReservationCancelledNotification(reservation, cancellerName);
            doSynchronizeStatuses();
        });
    }

    @Override
    public void cancelReservationGroup(String groupId, String cancellerName, String cancellerRole) {
        withLock(() -> {
            List<Reservation> all = reservationRepository.findAll();
            List<Reservation> targets = all.stream()
                    .filter(r -> groupIdMatches(r, groupId))
                    .toList();
            if (targets.isEmpty()) {
                throw new ResourceNotFoundException("Grupo de reserva " + groupId + " nao encontrado.");
            }

            if (!isAdminRole(cancellerRole)) {
                assertCanCancelReservationGroup(targets, cancellerName, cancellerRole);
            }

            boolean anyNonCancelled = targets.stream()
                    .anyMatch(r -> r.getStatus() != ReservationStatus.cancelled);
            if (!anyNonCancelled) {
                doSynchronizeStatuses();
                return;
            }

            for (Reservation target : targets) {
                if (target.getStatus() != ReservationStatus.cancelled) {
                    target.setStatus(ReservationStatus.cancelled);
                    reservationRepository.save(target);
                }
            }

            Reservation first = targets.getFirst();
        notificationService.createReservationGroupCancelledNotification(
                groupId,
                first.getRoom(),
                first.getDate().toString(),
                first.getStart().toString(),
                first.getEnd().toString(),
                targets.size(),
                cancellerName.trim()
        );

            doSynchronizeStatuses();
        });
    }

    @Override
    public void synchronizeStatuses() {
        withLock(this::doSynchronizeStatuses);
    }

    private void doSynchronizeStatuses() {
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        List<Reservation> allReservations = reservationRepository.findAll();
        boolean anyStatusChanged = false;
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
            if (reservation.getStatus() != recalculated) {
                anyStatusChanged = true;
                reservation.setStatus(recalculated);
            }
            updatedReservations.add(reservation);
        }
        if (anyStatusChanged) {
            reservationRepository.saveAll(updatedReservations);
        }
    }

    private ReservationResponseDto toDto(Reservation reservation) {
        return new ReservationResponseDto(
                reservation.getId(),
                reservation.getRoom(),
                reservation.getUser(),
                reservation.getRequesterName(),
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
        if (allCancelled) {
            status = ReservationStatus.cancelled;
        } else if (anyActive) {
            status = ReservationStatus.active;
        }

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
                first.getRequesterName(),
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
        if (room.isBlocked()) {
            throw new BadRequestException("Sala indisponivel: bloqueada pelo administrador.");
        }

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

        RoomPosition position = resolvePosition(room, request.seatCode());
        assertSeatTypeMatches(position, request.seatType());
        List<String> normalizedEquipment = normalizeEquipmentList(request.requestedEquipment());
        validateRequestedEquipmentAgainstPosition(position, normalizedEquipment);

        boolean seatAlreadyReserved = existingReservations.stream()
                .filter(reservation -> reservation.getStatus() != ReservationStatus.cancelled)
                .filter(reservation -> reservation.getRoomId().equals(room.getId()))
                .filter(reservation -> reservation.getDate().equals(date))
                .anyMatch(reservation -> reservation.getSeatCode().equalsIgnoreCase(position.getCode()));
        if (seatAlreadyReserved) {
            throw new ConflictException(
                    "Posicao " + position.getCode() + " ja esta reservada nesta data nesta sala. Cancele a reserva existente para liberar."
            );
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
                request.requesterName().trim(),
                role,
                null,
                position.getCode(),
                position.getType(),
                normalizedEquipment,
                date,
                start,
                end,
                initialStatus
        );
    }

    private static RoomPosition resolvePosition(Room room, String seatCode) {
        if (room.getPositions() == null || room.getPositions().isEmpty()) {
            throw new BadRequestException("Sala sem posicoes cadastradas.");
        }
        String code = seatCode.trim();
        return room.getPositions().stream()
                .filter(p -> p.getCode() != null && p.getCode().equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Posicao " + seatCode + " nao existe nesta sala."));
    }

    private static void assertSeatTypeMatches(RoomPosition position, String requestedSeatType) {
        if (requestedSeatType == null || requestedSeatType.isBlank()) {
            throw new BadRequestException("seatType e obrigatorio.");
        }
        if (position.getType() == null || !position.getType().equalsIgnoreCase(requestedSeatType.trim())) {
            throw new BadRequestException("seatType nao confere com a posicao cadastrada.");
        }
    }

    private static List<String> normalizeEquipmentList(List<String> requested) {
        if (requested == null || requested.isEmpty()) {
            return new ArrayList<>();
        }
        List<String> out = new ArrayList<>();
        for (String item : requested) {
            if (item == null || item.isBlank()) {
                continue;
            }
            out.add(item.trim());
        }
        return out;
    }

    private static void validateRequestedEquipmentAgainstPosition(RoomPosition position, List<String> normalizedRequested) {
        if (normalizedRequested.isEmpty()) {
            return;
        }
        List<String> available = position.getEquipment() == null ? List.of() : position.getEquipment();
        for (String req : normalizedRequested) {
            boolean match = available.stream()
                    .filter(e -> e != null && !e.isBlank())
                    .anyMatch(e -> e.trim().equalsIgnoreCase(req));
            if (!match) {
                throw new BadRequestException("Equipamento exigido indisponivel nesta posicao: " + req);
            }
        }
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

    private boolean isManagerRole(String role) {
        return role != null && role.trim().equalsIgnoreCase("manager");
    }

    private boolean isEmployeeRole(String role) {
        return role != null && role.trim().equalsIgnoreCase("employee");
    }

    private boolean isManagerOrAdminRequesterRole(String requesterRole) {
        if (requesterRole == null) {
            return false;
        }
        String rr = requesterRole.trim().toLowerCase();
        return rr.equals("manager") || rr.equals("admin");
    }

    private void assertCanCancelReservation(Reservation reservation, String cancellerName, String cancellerRole) {
        if (cancellerName == null || cancellerName.isBlank()) {
            throw new BadRequestException("requesterName e obrigatorio para cancelar.");
        }
        if (cancellerRole == null || cancellerRole.isBlank()) {
            throw new BadRequestException("requesterRole e obrigatorio para cancelar.");
        }
        if (isAdminRole(cancellerRole)) {
            return;
        }
        String cn = cancellerName.trim();
        if (reservation.getUser().equalsIgnoreCase(cn)) {
            return;
        }
        if (isManagerRole(cancellerRole)) {
            String requester = reservation.getRequesterName() == null ? "" : reservation.getRequesterName().trim();
            if (requester.equalsIgnoreCase(cn) && isManagerOrAdminRequesterRole(reservation.getRequesterRole())) {
                return;
            }
        }
        throw new ForbiddenException("Sem permissao para cancelar esta reserva.");
    }

    private void assertCanCancelReservationGroup(
            List<Reservation> targets,
            String cancellerName,
            String cancellerRole
    ) {
        String cn = cancellerName.trim();
        if (isManagerRole(cancellerRole)) {
            boolean allCreatedByManager = targets.stream()
                    .allMatch(r -> {
                        String rn = r.getRequesterName() == null ? "" : r.getRequesterName().trim();
                        return rn.equalsIgnoreCase(cn) && isManagerOrAdminRequesterRole(r.getRequesterRole());
                    });
            if (!allCreatedByManager) {
                throw new ForbiddenException("Gestor so pode cancelar reservas do lote ou agendamentos criados por ele.");
            }
            return;
        }
        if (isEmployeeRole(cancellerRole)) {
            boolean allOwn = targets.stream().allMatch(r -> r.getUser().equalsIgnoreCase(cn));
            if (!allOwn) {
                throw new ForbiddenException("Funcionario so pode cancelar as proprias reservas.");
            }
            return;
        }
        throw new ForbiddenException("Papel invalido para cancelamento em grupo.");
    }
}
