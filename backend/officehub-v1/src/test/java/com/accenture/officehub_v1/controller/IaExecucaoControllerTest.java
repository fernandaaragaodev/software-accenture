package com.accenture.officehub_v1.controller;

import com.accenture.officehub_v1.dto.response.AgenteExecucaoResponse;
import com.accenture.officehub_v1.entity.enums.StatusAgente;
import com.accenture.officehub_v1.service.ia.AgenteExecucaoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IaExecucaoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AgenteExecucaoService agenteExecucaoService;

    @Test
    @WithMockUser(authorities = "ADMIN_SALA")
    void listarExecucoes_adminSala_retorna200() throws Exception {
        UUID id = UUID.randomUUID();
        when(agenteExecucaoService.listar(eq("ALOCACAO"), eq(StatusAgente.SUCESSO), isNull(), isNull()))
                .thenReturn(List.of(new AgenteExecucaoResponse(
                        id,
                        "ALOCACAO",
                        null,
                        StatusAgente.SUCESSO,
                        "ALGORITMO_ESPACIAL_V1",
                        12,
                        null,
                        null,
                        null,
                        OffsetDateTime.now())));

        mockMvc.perform(get("/api/v1/ia/execucoes")
                        .param("tipoAgente", "ALOCACAO")
                        .param("status", "SUCESSO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(id.toString()))
                .andExpect(jsonPath("$[0].tipoAgente").value("ALOCACAO"));
    }

    @Test
    @WithMockUser(authorities = "USUARIO_FINAL")
    void listarExecucoes_usuarioFinal_retorna403() throws Exception {
        when(agenteExecucaoService.listar(any(), any(), any(), any())).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/ia/execucoes"))
                .andExpect(status().isForbidden());
    }
}
