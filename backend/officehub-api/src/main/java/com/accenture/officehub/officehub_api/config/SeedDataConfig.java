package com.accenture.officehub.officehub_api.config;

import com.accenture.officehub.officehub_api.enums.ReservationStatus;
import com.accenture.officehub.officehub_api.enums.RoomStatus;
import com.accenture.officehub.officehub_api.model.Reservation;
import com.accenture.officehub.officehub_api.model.Room;
import com.accenture.officehub.officehub_api.model.RoomPosition;
import com.accenture.officehub.officehub_api.repository.ReservationRepository;
import com.accenture.officehub.officehub_api.repository.RoomRepository;
import com.accenture.officehub.officehub_api.service.NotificationService;
import com.accenture.officehub.officehub_api.service.ReservationService;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Configuration
public class SeedDataConfig {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationService reservationService;
    private final NotificationService notificationService;

    public SeedDataConfig(
            RoomRepository roomRepository,
            ReservationRepository reservationRepository,
            ReservationService reservationService,
            NotificationService notificationService
    ) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
        this.notificationService = notificationService;
    }

    @PostConstruct
    public void seed() {
        roomRepository.saveAll(List.of(
                // Sala Apolo — 10 posições, mix de mesa vazia e equipadas
                new Room(1L, "Sala Apolo", 12, RoomStatus.available,
                        List.of("Projetor", "Videoconferencia", "Ar-condicionado"), "2o andar", 45,
                        List.of(
                                new RoomPosition("P1", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P2", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P3", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P4", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P5", "Mesa com digitalizadora", List.of("Mesa ergonômica", "Mesa digitalizadora")),
                                new RoomPosition("P6", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation")),
                                new RoomPosition("P7", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P8", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P9", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P10", "Mesa com digitalizadora", List.of("Mesa ergonômica", "Mesa digitalizadora"))
                        )),
                // Sala Hermes — 6 posições simples
                new Room(2L, "Sala Hermes", 6, RoomStatus.available,
                        List.of("TV 55\"", "Quadro Branco"), "2o andar", 25,
                        List.of(
                                new RoomPosition("P1", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P2", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P3", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P4", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P5", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P6", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation"))
                        )),
                // Sala Athena — 18 posições, várias com digitalizadora e PC
                new Room(3L, "Sala Athena", 20, RoomStatus.available,
                        List.of("Projetor", "Videoconferencia", "Som"), "3o andar", 80,
                        List.of(
                                new RoomPosition("P1", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P2", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P3", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation")),
                                new RoomPosition("P4", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P5", "Mesa com digitalizadora", List.of("Mesa ergonômica", "Mesa digitalizadora")),
                                new RoomPosition("P6", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P7", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P8", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation")),
                                new RoomPosition("P9", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P10", "Mesa com digitalizadora", List.of("Mesa ergonômica", "Mesa digitalizadora")),
                                new RoomPosition("P11", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P12", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P13", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation")),
                                new RoomPosition("P14", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P15", "Mesa com digitalizadora", List.of("Mesa ergonômica", "Mesa digitalizadora")),
                                new RoomPosition("P16", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P17", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P18", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation"))
                        )),
                // Sala Zeus — 8 posições
                new Room(4L, "Sala Zeus", 8, RoomStatus.available,
                        List.of("TV 65\"", "Ar-condicionado"), "1o andar", 35,
                        List.of(
                                new RoomPosition("P1", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P2", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P3", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation")),
                                new RoomPosition("P4", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P5", "Mesa com digitalizadora", List.of("Mesa ergonômica", "Mesa digitalizadora")),
                                new RoomPosition("P6", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P7", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P8", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation"))
                        )),
                // Sala Cronos — 4 posições compactas
                new Room(5L, "Sala Cronos", 4, RoomStatus.available,
                        List.of("Monitor Extra", "Quadro Branco"), "1o andar", 18,
                        List.of(
                                new RoomPosition("P1", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P2", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P3", "Mesa com digitalizadora", List.of("Mesa ergonômica", "Mesa digitalizadora")),
                                new RoomPosition("P4", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation"))
                        )),
                // Sala Poseidon — 28 posições, auditório
                new Room(6L, "Sala Poseidon", 30, RoomStatus.available,
                        List.of("Palco", "Microfone", "Projetor 4K"), "Terreo", 120,
                        List.of(
                                new RoomPosition("P1", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P2", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P3", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation")),
                                new RoomPosition("P4", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P5", "Mesa com digitalizadora", List.of("Mesa ergonômica", "Mesa digitalizadora")),
                                new RoomPosition("P6", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P7", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P8", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation")),
                                new RoomPosition("P9", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P10", "Mesa com digitalizadora", List.of("Mesa ergonômica", "Mesa digitalizadora")),
                                new RoomPosition("P11", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P12", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P13", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation")),
                                new RoomPosition("P14", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P15", "Mesa com digitalizadora", List.of("Mesa ergonômica", "Mesa digitalizadora")),
                                new RoomPosition("P16", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P17", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P18", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation")),
                                new RoomPosition("P19", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P20", "Mesa com digitalizadora", List.of("Mesa ergonômica", "Mesa digitalizadora")),
                                new RoomPosition("P21", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P22", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P23", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation")),
                                new RoomPosition("P24", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P25", "Mesa com digitalizadora", List.of("Mesa ergonômica", "Mesa digitalizadora")),
                                new RoomPosition("P26", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("P27", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                                new RoomPosition("P28", "Mesa com PC maior", List.of("Mesa ergonômica", "PC Workstation"))
                        ))
        ));

        reservationRepository.saveAll(List.of(
                new Reservation(1L, 1L, "Sala Apolo", "Maria Souza", "Maria Souza", "manager", null, "P1", "Mesa vazia", List.of("Mesa ergonômica"), LocalDate.parse("2025-06-10"), LocalTime.parse("09:00"), LocalTime.parse("11:00"), ReservationStatus.confirmed),
                new Reservation(2L, 2L, "Sala Hermes", "Carlos Lima", "Carlos Lima", "employee", null, "P2", "Mesa maior", List.of("Monitor 27"), LocalDate.parse("2025-06-10"), LocalTime.parse("14:00"), LocalTime.parse("16:00"), ReservationStatus.active),
                new Reservation(3L, 3L, "Sala Athena", "Ana Pereira", "Ana Pereira", "manager", null, "P3", "Mesa com PC maior", List.of("PC Workstation"), LocalDate.parse("2025-06-11"), LocalTime.parse("10:00"), LocalTime.parse("12:00"), ReservationStatus.confirmed),
                new Reservation(4L, 4L, "Sala Zeus", "Pedro Alves", "Pedro Alves", "manager", null, "P1", "Mesa vazia", List.of("Mesa ergonômica"), LocalDate.parse("2025-06-09"), LocalTime.parse("08:00"), LocalTime.parse("09:30"), ReservationStatus.cancelled),
                new Reservation(5L, 5L, "Sala Cronos", "Julia Costa", "Julia Costa", "manager", null, "P2", "Mesa maior", List.of("Monitor 27"), LocalDate.parse("2025-06-12"), LocalTime.parse("15:00"), LocalTime.parse("17:00"), ReservationStatus.confirmed)
        ));

        reservationRepository.findById(1L).ifPresent(notificationService::createReservationConfirmedNotification);
        reservationRepository.findById(4L).ifPresent(notificationService::createReservationCancelledNotification);
        reservationService.synchronizeStatuses();
    }
}
