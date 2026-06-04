package com.accenture.officehub_v1.config;

import com.accenture.officehub_v1.entity.Perfil;
import com.accenture.officehub_v1.repository.PerfilRepository;
import com.accenture.officehub_v1.security.Roles;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class PerfilDataInitializer implements ApplicationRunner {

    private static final Map<String, String> PERFIS_PADRAO = Map.of(
            Roles.ADMIN_SALA, "Administração de salas, posições, layouts e tipos de equipamento",
            Roles.GESTOR_RESERVAS, "Gestão de reservas, relatórios e notificações",
            Roles.USUARIO_FINAL, "Solicitação e cancelamento de reservas",
            Roles.INTEGRADOR, "Integração via API de disponibilidade e reservas");

    private final PerfilRepository perfilRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        PERFIS_PADRAO.forEach(this::garantirPerfil);
    }

    private void garantirPerfil(String nome, String descricao) {
        perfilRepository.findByNomeIgnoreCase(nome).ifPresentOrElse(
                ignorado -> { },
                () -> {
                    perfilRepository.save(Perfil.builder().nome(nome).descricao(descricao).build());
                    log.info("Perfil '{}' criado na inicialização.", nome);
                });
    }
}
