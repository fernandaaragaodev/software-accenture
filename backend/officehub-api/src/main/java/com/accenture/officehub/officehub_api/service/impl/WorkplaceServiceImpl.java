package com.accenture.officehub.officehub_api.service.impl;

import com.accenture.officehub.officehub_api.dto.ColleagueContextDto;
import com.accenture.officehub.officehub_api.dto.ManagerTeamResponseDto;
import com.accenture.officehub.officehub_api.dto.RoomSuggestionResponseDto;
import com.accenture.officehub.officehub_api.dto.TeamMemberResponseDto;
import com.accenture.officehub.officehub_api.dto.WorkplaceContextResponseDto;
import com.accenture.officehub.officehub_api.enums.ProfessionalProfile;
import com.accenture.officehub.officehub_api.enums.ReservationStatus;
import com.accenture.officehub.officehub_api.exception.BadRequestException;
import com.accenture.officehub.officehub_api.exception.ForbiddenException;
import com.accenture.officehub.officehub_api.model.Employee;
import com.accenture.officehub.officehub_api.model.Reservation;
import com.accenture.officehub.officehub_api.model.Room;
import com.accenture.officehub.officehub_api.model.Team;
import com.accenture.officehub.officehub_api.repository.EmployeeRepository;
import com.accenture.officehub.officehub_api.repository.ReservationRepository;
import com.accenture.officehub.officehub_api.repository.RoomRepository;
import com.accenture.officehub.officehub_api.repository.TeamRepository;
import com.accenture.officehub.officehub_api.service.ReservationService;
import com.accenture.officehub.officehub_api.service.WorkplaceService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WorkplaceServiceImpl implements WorkplaceService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final EmployeeRepository employeeRepository;
    private final TeamRepository teamRepository;
    private final ReservationService reservationService;

    public WorkplaceServiceImpl(
            RoomRepository roomRepository,
            ReservationRepository reservationRepository,
            EmployeeRepository employeeRepository,
            TeamRepository teamRepository,
            ReservationService reservationService
    ) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.employeeRepository = employeeRepository;
        this.teamRepository = teamRepository;
        this.reservationService = reservationService;
    }

    @Override
    public WorkplaceContextResponseDto getContextForUser(String userDisplayName) {
        if (userDisplayName == null || userDisplayName.isBlank()) {
            throw new BadRequestException("userName e obrigatorio.");
        }
        Optional<Employee> emp = employeeRepository.findByDisplayNameIgnoreCase(userDisplayName.trim());
        if (emp.isEmpty()) {
            return new WorkplaceContextResponseDto(
                    false,
                    userDisplayName.trim(),
                    null,
                    null,
                    null,
                    ProfessionalProfile.NEGOCIOS_ANALISE.name(),
                    profileLabel(ProfessionalProfile.NEGOCIOS_ANALISE),
                    List.of()
            );
        }
        Employee e = emp.get();
        Optional<Team> team = teamRepository.findById(e.getTeamId());
        List<ColleagueContextDto> colleagues = employeeRepository.findByTeamId(e.getTeamId()).stream()
                .filter(other -> !other.getId().equals(e.getId()))
                .filter(other -> !other.isHidePresenceFromTeam())
                .map(other -> new ColleagueContextDto(
                        other.getDisplayName(),
                        other.getProfessionalProfile().name(),
                        profileLabel(other.getProfessionalProfile()),
                        other.getTypicalStartTime() == null ? "" : other.getTypicalStartTime()
                ))
                .toList();
        return new WorkplaceContextResponseDto(
                true,
                e.getDisplayName(),
                e.getTeamId(),
                team.map(Team::getName).orElse(null),
                team.map(Team::getPreferredFloor).orElse(null),
                e.getProfessionalProfile().name(),
                profileLabel(e.getProfessionalProfile()),
                colleagues
        );
    }

    @Override
    public List<ManagerTeamResponseDto> listTeamsForManager(String requesterName, String requesterRole) {
        if (requesterName == null || requesterName.isBlank()) {
            throw new BadRequestException("requesterName e obrigatorio.");
        }
        if (requesterRole == null || requesterRole.isBlank()) {
            throw new BadRequestException("requesterRole e obrigatorio.");
        }
        String role = requesterRole.trim().toLowerCase(Locale.ROOT);
        if (role.equals("employee")) {
            throw new ForbiddenException("Listagem de equipes permitida apenas para gestor ou admin.");
        }
        if (!role.equals("manager") && !role.equals("admin")) {
            throw new BadRequestException("requesterRole invalido. Use manager ou admin.");
        }

        String requester = requesterName.trim();

        if (role.equals("admin")) {
            return teamRepository.findAll().stream()
                    .map(team -> toManagerTeamDto(team, null))
                    .toList();
        }

        Employee manager = employeeRepository.findByDisplayNameIgnoreCase(requester)
                .orElseThrow(() -> new ForbiddenException(
                        "Gestor nao cadastrado no diretorio de colaboradores."
                ));
        if (manager.getTeamId() == null) {
            return List.of();
        }
        return teamRepository.findById(manager.getTeamId())
                .map(team -> List.of(toManagerTeamDto(team, manager.getId())))
                .orElse(List.of());
    }

    private ManagerTeamResponseDto toManagerTeamDto(Team team, Long excludeEmployeeId) {
        List<TeamMemberResponseDto> members = employeeRepository.findByTeamId(team.getId()).stream()
                .filter(e -> excludeEmployeeId == null || !e.getId().equals(excludeEmployeeId))
                .sorted(Comparator.comparing(Employee::getDisplayName, String.CASE_INSENSITIVE_ORDER))
                .map(e -> new TeamMemberResponseDto(
                        e.getId(),
                        e.getDisplayName(),
                        e.getProfessionalProfile().name(),
                        profileLabel(e.getProfessionalProfile()),
                        e.isHidePresenceFromTeam()
                ))
                .toList();
        return new ManagerTeamResponseDto(
                team.getId(),
                team.getName(),
                team.getPreferredFloor(),
                members
        );
    }

    @Override
    public List<RoomSuggestionResponseDto> suggestRooms(
            String userDisplayName,
            String date,
            String start,
            String end,
            int limit
    ) {
        if (userDisplayName == null || userDisplayName.isBlank()) {
            throw new BadRequestException("userName e obrigatorio.");
        }
        reservationService.synchronizeStatuses();
        LocalDate reservationDate = parseDate(date);
        LocalTime startTime = parseTime(start, "start");
        LocalTime endTime = parseTime(end, "end");
        if (!startTime.isBefore(endTime)) {
            throw new BadRequestException("Horario invalido: start deve ser antes de end.");
        }
        validateSuggestionDate(reservationDate);

        int cap = Math.min(Math.max(limit, 1), 20);

        Optional<Employee> selfOpt = employeeRepository.findByDisplayNameIgnoreCase(userDisplayName.trim());
        ProfessionalProfile profile = selfOpt.map(Employee::getProfessionalProfile).orElse(ProfessionalProfile.NEGOCIOS_ANALISE);
        Optional<Team> teamOpt = selfOpt.flatMap(e -> teamRepository.findById(e.getTeamId()));

        Set<String> colleagueFloorsThisSlot = colleagueFloorsDuringSlot(selfOpt, reservationDate, startTime, endTime);

        List<ScoredRoom> scored = new ArrayList<>();
        for (Room room : roomRepository.findAll()) {
            if (room.isBlocked()) {
                continue;
            }
            int free = countFreeDesks(room, reservationDate, startTime, endTime);
            if (free <= 0) {
                continue;
            }
            List<String> reasons = new ArrayList<>();
            double score = free * 2.0;
            reasons.add(free + " posicao(oes) livres no intervalo");

            String blob = equipmentBlob(room);
            if (containsInsensitive(blob, "ar-condicionado") || containsInsensitive(blob, "ar condicionado")) {
                score += 50;
                reasons.add("Climatizacao (Ar-condicionado)");
            }
            if (teamOpt.isPresent()) {
                Team team = teamOpt.get();
                if (floorsMatch(room.getFloor(), team.getPreferredFloor())) {
                    score += 50;
                    reasons.add("Mesmo andar preferido da equipe (" + team.getName() + ")");
                }
            }
            double p = profileEquipmentScore(profile, room, blob);
            if (p > 0) {
                score += p;
                reasons.add("Alinhado ao perfil " + profileLabel(profile));
            }
            for (String f : colleagueFloorsThisSlot) {
                if (floorsMatch(room.getFloor(), f)) {
                    score += 25;
                    reasons.add("Proximidade: colegas da equipe com reserva no mesmo andar neste horario");
                    break;
                }
            }
            scored.add(new ScoredRoom(room, score, free, reasons));
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble((ScoredRoom s) -> s.score).reversed())
                .limit(cap)
                .map(s -> new RoomSuggestionResponseDto(
                        s.room.getId(),
                        s.room.getName(),
                        s.room.getFloor(),
                        Math.round(s.score * 10.0) / 10.0,
                        s.freeDesks,
                        s.reasons
                ))
                .toList();
    }

    private Set<String> colleagueFloorsDuringSlot(
            Optional<Employee> selfOpt,
            LocalDate date,
            LocalTime start,
            LocalTime end
    ) {
        Set<String> floors = new HashSet<>();
        if (selfOpt.isEmpty()) {
            return floors;
        }
        Long teamId = selfOpt.get().getTeamId();
        Set<String> colleagueNames = employeeRepository.findByTeamId(teamId).stream()
                .filter(e -> !e.getId().equals(selfOpt.get().getId()))
                .filter(e -> !e.isHidePresenceFromTeam())
                .map(e -> e.getDisplayName().trim().toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());

        for (Reservation r : reservationRepository.findAll()) {
            if (r.getStatus() == ReservationStatus.cancelled) {
                continue;
            }
            if (!r.getDate().equals(date)) {
                continue;
            }
            if (!overlaps(start, end, r.getStart(), r.getEnd())) {
                continue;
            }
            String ru = r.getUser() == null ? "" : r.getUser().trim().toLowerCase(Locale.ROOT);
            if (!colleagueNames.contains(ru)) {
                continue;
            }
            roomRepository.findById(r.getRoomId()).ifPresent(room -> floors.add(room.getFloor()));
        }
        return floors;
    }

    private int countFreeDesks(Room room, LocalDate date, LocalTime start, LocalTime end) {
        if (room.getPositions() == null) {
            return 0;
        }
        int free = 0;
        for (var position : room.getPositions()) {
            if (position.isBlocked()) {
                continue;
            }
            boolean occupied = reservationRepository.findAll().stream()
                    .filter(r -> r.getRoomId().equals(room.getId()))
                    .filter(r -> r.getStatus() != ReservationStatus.cancelled)
                    .filter(r -> r.getDate().equals(date))
                    .anyMatch(r ->
                            r.getSeatCode() != null
                                    && position.getCode() != null
                                    && r.getSeatCode().equalsIgnoreCase(position.getCode())
                                    && overlaps(start, end, r.getStart(), r.getEnd()));
            if (!occupied) {
                free++;
            }
        }
        return free;
    }

    private String equipmentBlob(Room room) {
        StringBuilder sb = new StringBuilder();
        if (room.getEquipment() != null) {
            for (String s : room.getEquipment()) {
                sb.append(s).append(' ');
            }
        }
        if (room.getPositions() != null) {
            room.getPositions().forEach(p -> {
                if (p.getEquipment() != null) {
                    p.getEquipment().forEach(e -> sb.append(e).append(' '));
                }
                if (p.getType() != null) {
                    sb.append(p.getType()).append(' ');
                }
            });
        }
        return sb.toString().toLowerCase(Locale.ROOT);
    }

    private double profileEquipmentScore(ProfessionalProfile profile, Room room, String blob) {
        double raw = switch (profile) {
            case DESENVOLVIMENTO -> pick(blob, "pc workstation", 22)
                    + pick(blob, "workstation", 12)
                    + pick(blob, "mesa vazia", 8)
                    + pick(blob, "ar-condicionado", 6)
                    + (room.getArea() != null && room.getArea() < 45 ? 10 : 0);
            case DESIGN_ENTREGA -> pick(blob, "digitalizadora", 25)
                    + pick(blob, "monitor", 12)
                    + pick(blob, "mesa maior", 8);
            case NEGOCIOS_ANALISE -> pick(blob, "quadro", 18)
                    + pick(blob, "monitor", 10)
                    + pick(blob, "branco", 6);
            case GESTAO_PROJETOS -> pick(blob, "videoconferencia", 22)
                    + pick(blob, "videoconferência", 22)
                    + pick(blob, "projetor", 18)
                    + pick(blob, "microfone", 10)
                    + pick(blob, "palco", 8);
        };
        return Math.min(raw, 40);
    }

    private static double pick(String blob, String needle, double pts) {
        return blob.contains(needle) ? pts : 0;
    }

    private static boolean containsInsensitive(String blob, String needle) {
        return blob.contains(needle.toLowerCase(Locale.ROOT));
    }

    private static boolean floorsMatch(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return normalizeFloor(a).equals(normalizeFloor(b));
    }

    private static String normalizeFloor(String f) {
        return f.trim().toLowerCase(Locale.ROOT).replace('º', 'o').replaceAll("\\s+", "");
    }

    private static boolean overlaps(LocalTime startA, LocalTime endA, LocalTime startB, LocalTime endB) {
        return startA.isBefore(endB) && endA.isAfter(startB);
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

    private void validateSuggestionDate(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.isBefore(today)) {
            throw new BadRequestException("Nao e possivel sugerir salas para datas que ja passaram.");
        }
        LocalDate earliestAllowed = today.plusDays(7);
        if (date.isBefore(earliestAllowed)) {
            throw new BadRequestException("Sugestoes seguem a mesma regra de reserva: pelo menos 7 dias de antecedencia.");
        }
    }

    private static String profileLabel(ProfessionalProfile p) {
        return switch (p) {
            case DESENVOLVIMENTO -> "Desenvolvimento";
            case DESIGN_ENTREGA -> "Design & entrega visual";
            case NEGOCIOS_ANALISE -> "Negocios & analise";
            case GESTAO_PROJETOS -> "Gestao de projetos";
        };
    }

    private record ScoredRoom(Room room, double score, int freeDesks, List<String> reasons) {}
}
