package com.accenture.officehub.officehub_api.config;

import com.accenture.officehub.officehub_api.enums.ReservationStatus;
import com.accenture.officehub.officehub_api.enums.RoomStatus;
import com.accenture.officehub.officehub_api.model.Reservation;
import com.accenture.officehub.officehub_api.model.Room;
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
                new Room(1L, "Sala Apolo", 12, 10, RoomStatus.available, List.of("Projetor", "Videoconferencia", "Ar-condicionado"), "2o andar", 45),
                new Room(2L, "Sala Hermes", 6, 6, RoomStatus.occupied, List.of("TV 55\"", "Quadro Branco"), "2o andar", 25),
                new Room(3L, "Sala Athena", 20, 18, RoomStatus.available, List.of("Projetor", "Videoconferencia", "Som"), "3o andar", 80),
                new Room(4L, "Sala Zeus", 8, 8, RoomStatus.reserved, List.of("TV 65\"", "Ar-condicionado"), "1o andar", 35),
                new Room(5L, "Sala Cronos", 4, 4, RoomStatus.available, List.of("Monitor Extra", "Quadro Branco"), "1o andar", 18),
                new Room(6L, "Sala Poseidon", 30, 28, RoomStatus.occupied, List.of("Palco", "Microfone", "Projetor 4K"), "Terreo", 120)
        ));

        reservationRepository.saveAll(List.of(
                new Reservation(1L, 1L, "Sala Apolo", "Maria Souza", "manager", null, "P1", "Mesa vazia", List.of("Mesa ergonômica"), LocalDate.parse("2025-06-10"), LocalTime.parse("09:00"), LocalTime.parse("11:00"), ReservationStatus.confirmed),
                new Reservation(2L, 2L, "Sala Hermes", "Carlos Lima", "employee", null, "P2", "Mesa maior", List.of("Monitor 27"), LocalDate.parse("2025-06-10"), LocalTime.parse("14:00"), LocalTime.parse("16:00"), ReservationStatus.active),
                new Reservation(3L, 3L, "Sala Athena", "Ana Pereira", "manager", null, "P3", "Mesa com PC maior", List.of("PC Workstation"), LocalDate.parse("2025-06-11"), LocalTime.parse("10:00"), LocalTime.parse("12:00"), ReservationStatus.confirmed),
                new Reservation(4L, 4L, "Sala Zeus", "Pedro Alves", "manager", null, "P1", "Mesa vazia", List.of("Mesa ergonômica"), LocalDate.parse("2025-06-09"), LocalTime.parse("08:00"), LocalTime.parse("09:30"), ReservationStatus.cancelled),
                new Reservation(5L, 5L, "Sala Cronos", "Julia Costa", "manager", null, "P2", "Mesa maior", List.of("Monitor 27"), LocalDate.parse("2025-06-12"), LocalTime.parse("15:00"), LocalTime.parse("17:00"), ReservationStatus.confirmed)
        ));

        reservationRepository.findById(1L).ifPresent(notificationService::createReservationConfirmedNotification);
        reservationRepository.findById(4L).ifPresent(notificationService::createReservationCancelledNotification);
        reservationService.synchronizeStatuses();
    }
}
