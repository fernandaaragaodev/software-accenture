package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.entity.Reserva;
import com.accenture.officehub_v1.repository.EquipeMembroRepository;
import com.accenture.officehub_v1.repository.ReservaPessoaRepository;
import com.accenture.officehub_v1.security.Roles;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReservaAutorizacaoService {

    private final EquipeMembroRepository equipeMembroRepository;
    private final ReservaPessoaRepository reservaPessoaRepository;

    public boolean podeGerenciarReserva(UUID usuarioId, Collection<String> perfis, Reserva reserva) {
        if (reserva.getSolicitante().getId().equals(usuarioId)) {
            return true;
        }

        if (perfis.contains(Roles.GESTOR_RESERVAS) && reservaPertenceEquipe(usuarioId, reserva)) {
            return true;
        }

        return false;
    }

    public boolean usuarioPertenceEquipe(UUID gestorId, UUID membroId) {
        if (gestorId.equals(membroId)) {
            return true;
        }

        return equipeMembroRepository.existsMembroNaEquipeDoGestor(membroId, gestorId);
    }

    private boolean reservaPertenceEquipe(UUID gestorId, Reserva reserva) {
        if (usuarioPertenceEquipe(gestorId, reserva.getSolicitante().getId())) {
            return true;
        }

        return reservaPessoaRepository.existsParticipanteNaEquipeDoGestor(reserva.getId(), gestorId);
    }
}
