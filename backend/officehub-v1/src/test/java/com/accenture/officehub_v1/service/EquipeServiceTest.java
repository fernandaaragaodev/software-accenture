package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.AdicionarMembroEquipeRequest;
import com.accenture.officehub_v1.dto.request.CriarEquipeRequest;
import com.accenture.officehub_v1.entity.Equipe;
import com.accenture.officehub_v1.entity.Perfil;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.entity.UsuarioPerfil;
import com.accenture.officehub_v1.exception.AcessoNegadoException;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.EquipeGestorRepository;
import com.accenture.officehub_v1.repository.EquipeMembroRepository;
import com.accenture.officehub_v1.repository.EquipeRepository;
import com.accenture.officehub_v1.repository.UsuarioPerfilRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import com.accenture.officehub_v1.security.Roles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EquipeServiceTest {

    private static final UUID GESTOR_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private static final UUID ADMIN_ID = UUID.fromString("40000000-0000-0000-0000-000000000004");
    private static final UUID EQUIPE_ID = UUID.fromString("50000000-0000-0000-0000-000000000005");
    private static final UUID MEMBRO_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");

    @Mock
    private EquipeRepository equipeRepository;

    @Mock
    private EquipeGestorRepository equipeGestorRepository;

    @Mock
    private EquipeMembroRepository equipeMembroRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioPerfilRepository usuarioPerfilRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private EquipeService equipeService;

    @Test
    void gestorCriaEquipeParaSiMesmo() {
        Usuario gestor = usuario(GESTOR_ID);
        Equipe equipeSalva = Equipe.builder().id(EQUIPE_ID).nome("Comercial").build();

        when(usuarioRepository.findByIdAndDeletedAtIsNull(GESTOR_ID)).thenReturn(Optional.of(gestor));
        when(usuarioPerfilRepository.findByUsuarioId(GESTOR_ID))
                .thenReturn(List.of(perfilGestor()));
        when(equipeRepository.save(any(Equipe.class))).thenReturn(equipeSalva);
        when(equipeRepository.findByIdAndDeletedAtIsNull(EQUIPE_ID)).thenReturn(Optional.of(equipeSalva));

        var response = equipeService.criar(
                new CriarEquipeRequest("Comercial", "Equipe comercial", null),
                GESTOR_ID,
                List.of(Roles.GESTOR_RESERVAS));

        assertThat(response.nome()).isEqualTo("Comercial");
        verify(equipeGestorRepository).save(any());
    }

    @Test
    void adminDeveInformarGestorAoCriarEquipe() {
        assertThatThrownBy(() -> equipeService.criar(
                new CriarEquipeRequest("Comercial", null, null),
                ADMIN_ID,
                List.of(Roles.ADMIN_SALA)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("gestor");
    }

    @Test
    void gestorNaoPodeCriarEquipeParaOutroGestor() {
        UUID outroGestor = UUID.randomUUID();

        assertThatThrownBy(() -> equipeService.criar(
                new CriarEquipeRequest("Comercial", null, outroGestor),
                GESTOR_ID,
                List.of(Roles.GESTOR_RESERVAS)))
                .isInstanceOf(AcessoNegadoException.class);
    }

    @Test
    void adicionarMembroNaEquipe() {
        Equipe equipe = Equipe.builder().id(EQUIPE_ID).nome("Comercial").build();
        Usuario membro = usuario(MEMBRO_ID);

        when(equipeRepository.findByIdAndDeletedAtIsNull(EQUIPE_ID)).thenReturn(Optional.of(equipe));
        when(equipeGestorRepository.existsByEquipeIdAndUsuarioId(EQUIPE_ID, GESTOR_ID)).thenReturn(true);
        when(usuarioRepository.findByIdAndDeletedAtIsNull(MEMBRO_ID)).thenReturn(Optional.of(membro));
        when(equipeMembroRepository.existsByEquipeIdAndUsuarioId(EQUIPE_ID, MEMBRO_ID)).thenReturn(false);
        when(equipeMembroRepository.existsEmOutraEquipeAtiva(MEMBRO_ID, EQUIPE_ID)).thenReturn(false);
        when(equipeGestorRepository.existsByEquipeIdAndUsuarioId(EQUIPE_ID, MEMBRO_ID)).thenReturn(false);

        equipeService.adicionarMembro(
                EQUIPE_ID,
                new AdicionarMembroEquipeRequest(MEMBRO_ID),
                GESTOR_ID,
                List.of(Roles.GESTOR_RESERVAS));

        verify(equipeMembroRepository).save(any());
    }

    @Test
    void desmembrarEquipeMarcaDeletedAt() {
        Equipe equipe = Equipe.builder().id(EQUIPE_ID).nome("Comercial").build();

        when(equipeRepository.findByIdAndDeletedAtIsNull(EQUIPE_ID)).thenReturn(Optional.of(equipe));
        when(equipeGestorRepository.existsByEquipeIdAndUsuarioId(EQUIPE_ID, GESTOR_ID)).thenReturn(true);
        when(equipeRepository.save(any(Equipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        equipeService.desmembrar(EQUIPE_ID, GESTOR_ID, List.of(Roles.GESTOR_RESERVAS));

        ArgumentCaptor<Equipe> captor = ArgumentCaptor.forClass(Equipe.class);
        verify(equipeRepository).save(captor.capture());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    private Usuario usuario(UUID id) {
        return Usuario.builder().id(id).nome("Usuário").email(id + "@test.com").build();
    }

    private UsuarioPerfil perfilGestor() {
        return UsuarioPerfil.builder()
                .perfil(Perfil.builder().nome(Roles.GESTOR_RESERVAS).build())
                .build();
    }
}
