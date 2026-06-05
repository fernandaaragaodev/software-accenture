package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.entity.Reserva;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.repository.EquipeMembroRepository;
import com.accenture.officehub_v1.repository.ReservaPessoaRepository;
import com.accenture.officehub_v1.security.Roles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservaAutorizacaoServiceTest {

    private static final UUID GESTOR_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID MEMBRO_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private static final UUID OUTRO_ID = UUID.fromString("30000000-0000-0000-0000-000000000003");

    @Mock
    private EquipeMembroRepository equipeMembroRepository;

    @Mock
    private ReservaPessoaRepository reservaPessoaRepository;

    @InjectMocks
    private ReservaAutorizacaoService reservaAutorizacaoService;

    @Test
    void solicitantePodeGerenciarPropriaReserva() {
        Reserva reserva = reservaComSolicitante(MEMBRO_ID);

        assertThat(reservaAutorizacaoService.podeGerenciarReserva(
                MEMBRO_ID, List.of(Roles.USUARIO_FINAL), reserva)).isTrue();
    }

    @Test
    void gestorPodeGerenciarReservaDaEquipe() {
        Reserva reserva = reservaComSolicitante(MEMBRO_ID);
        when(equipeMembroRepository.existsMembroNaEquipeDoGestor(MEMBRO_ID, GESTOR_ID))
                .thenReturn(true);

        assertThat(reservaAutorizacaoService.podeGerenciarReserva(
                GESTOR_ID, List.of(Roles.GESTOR_RESERVAS), reserva)).isTrue();
    }

    @Test
    void usuarioFinalNaoPodeGerenciarReservaDeOutro() {
        Reserva reserva = reservaComSolicitante(MEMBRO_ID);

        assertThat(reservaAutorizacaoService.podeGerenciarReserva(
                OUTRO_ID, List.of(Roles.USUARIO_FINAL), reserva)).isFalse();
    }

    @Test
    void gestorNaoPodeGerenciarReservaForaDaEquipe() {
        Reserva reserva = reservaComSolicitante(OUTRO_ID);
        UUID reservaId = reserva.getId();

        when(equipeMembroRepository.existsMembroNaEquipeDoGestor(OUTRO_ID, GESTOR_ID))
                .thenReturn(false);
        when(reservaPessoaRepository.existsParticipanteNaEquipeDoGestor(reservaId, GESTOR_ID))
                .thenReturn(false);

        assertThat(reservaAutorizacaoService.podeGerenciarReserva(
                GESTOR_ID, List.of(Roles.GESTOR_RESERVAS), reserva)).isFalse();
    }

    private Reserva reservaComSolicitante(UUID solicitanteId) {
        Usuario solicitante = Usuario.builder().id(solicitanteId).build();
        return Reserva.builder()
                .id(UUID.randomUUID())
                .solicitante(solicitante)
                .build();
    }
}
