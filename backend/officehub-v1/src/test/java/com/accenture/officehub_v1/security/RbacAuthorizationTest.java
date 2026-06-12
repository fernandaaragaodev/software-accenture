package com.accenture.officehub_v1.security;

import com.accenture.officehub_v1.dto.response.ConsultaDisponibilidadeResponse;
import com.accenture.officehub_v1.dto.response.RegraDisponibilidadeResponse;
import com.accenture.officehub_v1.entity.enums.StatusSala;
import com.accenture.officehub_v1.service.DisponibilidadeService;
import com.accenture.officehub_v1.service.EquipeService;
import com.accenture.officehub_v1.service.ReservaService;
import com.accenture.officehub_v1.service.SalaService;
import com.accenture.officehub_v1.service.TipoEquipamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RbacAuthorizationTest {

  private static final UUID SALA_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private SalaService salaService;

  @MockitoBean
  private ReservaService reservaService;

  @MockitoBean
  private DisponibilidadeService disponibilidadeService;

  @MockitoBean
  private TipoEquipamentoService tipoEquipamentoService;

  @MockitoBean
  private EquipeService equipeService;

  @BeforeEach
  void configurarDisponibilidade() {
    when(disponibilidadeService.consultarDisponibilidade(
            eq(SALA_ID),
            eq(LocalDate.of(2026, 6, 4)),
            eq(LocalTime.of(8, 0)),
            eq(LocalTime.of(18, 0))))
        .thenReturn(new ConsultaDisponibilidadeResponse(
            SALA_ID,
            LocalDate.of(2026, 6, 4),
            LocalTime.of(8, 0),
            LocalTime.of(18, 0),
            StatusSala.ATIVA,
            true,
            "OK",
            0,
            0,
            0,
            0,
            Map.of(),
            List.of(),
            List.of(),
            List.of()));
  }

  @Test
  void register_semAutenticacao_retorna401() throws Exception {
    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Novo\",\"email\":\"novo@test.com\",\"senha\":\"senha12345\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @WithMockUser(authorities = Roles.USUARIO_FINAL)
  void register_usuarioFinal_retorna403() throws Exception {
    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Novo\",\"email\":\"novo@test.com\",\"senha\":\"senha12345\"}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = Roles.ADMIN_SALA)
  void register_adminSala_passaAutorizacao() throws Exception {
    mockMvc.perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"\",\"email\":\"invalido\",\"senha\":\"curta\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = Roles.ADMIN_SALA)
  void listarSalas_adminSala_permitido() throws Exception {
    when(salaService.listarNaoDeletadas()).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/salas"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = Roles.USUARIO_FINAL)
  void listarSalas_usuarioFinal_permitido() throws Exception {
    when(salaService.listarNaoDeletadas()).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/salas"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = Roles.USUARIO_FINAL)
  void consultarDisponibilidade_usuarioFinal_permitido() throws Exception {
    mockMvc.perform(get("/api/v1/salas/{id}/disponibilidade", SALA_ID)
            .param("data", "2026-06-04")
            .param("horaInicio", "08:00:00")
            .param("horaFim", "18:00:00"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = Roles.USUARIO_FINAL)
  void buscarRegrasDisponibilidade_usuarioFinal_permitido() throws Exception {
    when(disponibilidadeService.buscarRegraPorSala(SALA_ID))
        .thenReturn(new RegraDisponibilidadeResponse(
            UUID.randomUUID(), "Regra padrão", SALA_ID, "Sala 1", 0, List.of()));

    mockMvc.perform(get("/api/v1/salas/{id}/regras-disponibilidade", SALA_ID))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = Roles.GESTOR_RESERVAS)
  void buscarRegrasDisponibilidade_gestor_permitido() throws Exception {
    when(disponibilidadeService.buscarRegraPorSala(SALA_ID))
        .thenReturn(new RegraDisponibilidadeResponse(
            UUID.randomUUID(), "Regra padrão", SALA_ID, "Sala 1", 0, List.of()));

    mockMvc.perform(get("/api/v1/salas/{id}/regras-disponibilidade", SALA_ID))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = Roles.ADMIN_SALA)
  void consultarDisponibilidade_adminSala_permitido() throws Exception {
    mockMvc.perform(get("/api/v1/salas/{id}/disponibilidade", SALA_ID)
            .param("data", "2026-06-04")
            .param("horaInicio", "08:00:00")
            .param("horaFim", "18:00:00"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = Roles.ADMIN_SALA)
  void listarReservas_adminSala_permitido() throws Exception {
    when(reservaService.listarReservas(any(), eq(false), any(), any())).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/reservas")
            .param("data", "2026-06-04"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = Roles.ADMIN_SALA)
  void cancelarReserva_adminSala_passaAutorizacao() throws Exception {
    when(reservaService.cancelarReserva(eq(SALA_ID), any(), any(), any())).thenReturn(null);

    mockMvc.perform(delete("/api/v1/reservas/{id}", SALA_ID)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"motivo\":\"Cancelamento administrativo\"}"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = Roles.GESTOR_RESERVAS)
  void listarTiposEquipamento_gestorReservas_permitido() throws Exception {
    when(tipoEquipamentoService.listar()).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/tipos-equipamento"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = Roles.ADMIN_SALA)
  void listarTiposEquipamento_adminSala_permitido() throws Exception {
    when(tipoEquipamentoService.listar()).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/tipos-equipamento"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = Roles.USUARIO_FINAL)
  void solicitarReserva_usuarioFinal_passaAutorizacao() throws Exception {
    mockMvc.perform(post("/api/v1/reservas")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = Roles.ADMIN_SALA)
  void solicitarReserva_adminSala_retorna403() throws Exception {
    mockMvc.perform(post("/api/v1/reservas")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = Roles.INTEGRADOR)
  void solicitarReserva_integrador_passaAutorizacao() throws Exception {
    mockMvc.perform(post("/api/v1/reservas")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  @WithMockUser(authorities = Roles.USUARIO_FINAL)
  void confirmarReserva_usuarioFinal_retorna403() throws Exception {
    mockMvc.perform(post("/api/v1/reservas/{id}/confirmar", SALA_ID))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = Roles.USUARIO_FINAL)
  void listarEquipes_usuarioFinal_retorna403() throws Exception {
    mockMvc.perform(get("/api/v1/equipes"))
        .andExpect(status().isForbidden());
  }

  @Test
  @WithMockUser(authorities = Roles.GESTOR_RESERVAS)
  void listarEquipes_gestor_permitido() throws Exception {
    when(equipeService.listar(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
        .thenReturn(List.of());

    mockMvc.perform(get("/api/v1/equipes"))
        .andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = Roles.GESTOR_RESERVAS)
  void confirmarReserva_gestor_permitido() throws Exception {
    when(reservaService.confirmarReserva(
            eq(SALA_ID), org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
        .thenReturn(null);

    mockMvc.perform(patch("/api/v1/reservas/{id}/confirmar", SALA_ID))
        .andExpect(status().isOk());
  }
}
