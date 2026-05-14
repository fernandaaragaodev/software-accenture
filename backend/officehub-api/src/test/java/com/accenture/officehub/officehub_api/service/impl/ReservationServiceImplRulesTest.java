package com.accenture.officehub.officehub_api.service.impl;

import com.accenture.officehub.officehub_api.dto.ReservationRequestDto;
import com.accenture.officehub.officehub_api.enums.RoomStatus;
import com.accenture.officehub.officehub_api.exception.BadRequestException;
import com.accenture.officehub.officehub_api.model.Room;
import com.accenture.officehub.officehub_api.model.RoomPosition;
import com.accenture.officehub.officehub_api.repository.inmemory.InMemoryReservationRepository;
import com.accenture.officehub.officehub_api.repository.RoomRepository;
import com.accenture.officehub.officehub_api.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplRulesTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private NotificationService notificationService;

    private InMemoryReservationRepository reservationRepository;
    private ReservationServiceImpl service;

    private static String futureDate() {
        return LocalDate.now().plusDays(10).toString();
    }

    @BeforeEach
    void setUp() {
        reservationRepository = new InMemoryReservationRepository();
        service = new ReservationServiceImpl(reservationRepository, roomRepository, notificationService);

        Room room = new Room(
                1L,
                "Sala Teste",
                4,
                RoomStatus.available,
                List.of(),
                "1o andar",
                20,
                List.of(
                        new RoomPosition("A1", "Mesa maior", List.of("Mesa ergonômica", "Monitor 27")),
                        new RoomPosition("A2", "Mesa vazia", List.of("Mesa ergonômica"))
                ),
                false
        );
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
    }

    @Test
    void rejectsUnknownSeatCode() {
        var req = new ReservationRequestDto(
                1L, "Maria", "Maria", "employee", "Z9", "Mesa maior",
                List.of("Monitor 27"), futureDate(), "09:00", "17:00"
        );
        assertThatThrownBy(() -> service.createReservation(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("nao existe nesta sala");
    }

    @Test
    void rejectsSeatTypeMismatch() {
        var req = new ReservationRequestDto(
                1L, "Maria", "Maria", "employee", "A1", "Tipo errado",
                List.of("Monitor 27"), futureDate(), "09:00", "17:00"
        );
        assertThatThrownBy(() -> service.createReservation(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("seatType nao confere");
    }

    @Test
    void rejectsEquipmentNotOnPosition() {
        var req = new ReservationRequestDto(
                1L, "Maria", "Maria", "employee", "A2", "Mesa vazia",
                List.of("Monitor 27"), futureDate(), "09:00", "17:00"
        );
        assertThatThrownBy(() -> service.createReservation(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Equipamento exigido indisponivel");
    }

    @Test
    void allowsMultipleRequestedEquipmentWhenSubsetOfPosition() {
        var req = new ReservationRequestDto(
                1L, "Maria", "Maria", "employee", "A1", "Mesa maior",
                List.of("Mesa ergonômica", "Monitor 27"), futureDate(), "09:00", "17:00"
        );
        var dto = service.createReservation(req);
        assertThat(dto.requestedEquipment()).containsExactly("Mesa ergonômica", "Monitor 27");
        assertThat(dto.seatCode()).isEqualTo("A1");
    }

    @Test
    void allowsEmptyEquipmentRequirements() {
        var req = new ReservationRequestDto(
                1L, "Maria", "Maria", "employee", "A2", "Mesa vazia",
                List.of(), futureDate(), "09:00", "17:00"
        );
        var dto = service.createReservation(req);
        assertThat(dto.requestedEquipment()).isEmpty();
    }

    @Test
    void batchPersistsAllOrNothing() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(
                new Room(1L, "Sala Teste", 4, RoomStatus.available, List.of(), "1o", 20,
                        List.of(
                                new RoomPosition("B1", "Mesa vazia", List.of("Mesa ergonômica")),
                                new RoomPosition("B2", "Mesa vazia", List.of("Mesa ergonômica"))
                        ),
                        false
        )));

        String d = futureDate();
        var r1 = new ReservationRequestDto(1L, "Gestor", "U1", "manager", "B1", "Mesa vazia", List.of(), d, "10:00", "11:00");
        var r2 = new ReservationRequestDto(1L, "Gestor", "U2", "manager", "B1", "Mesa vazia", List.of(), d, "10:00", "11:00");

        assertThatThrownBy(() -> service.createReservationsBatch(List.of(r1, r2)))
                .hasMessageContaining("Conflito");

        assertThat(reservationRepository.findAll()).isEmpty();
    }
}
