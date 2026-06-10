package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.response.DashboardStatsResponse;
import com.accenture.officehub_v1.entity.enums.StatusReserva;
import com.accenture.officehub_v1.repository.EquipeRepository;
import com.accenture.officehub_v1.repository.PosicaoEquipamentoRepository;
import com.accenture.officehub_v1.repository.PosicaoRepository;
import com.accenture.officehub_v1.repository.ReservaRepository;
import com.accenture.officehub_v1.repository.SalaRepository;
import com.accenture.officehub_v1.repository.TipoEquipamentoRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final SalaRepository salaRepository;
    private final PosicaoRepository posicaoRepository;
    private final TipoEquipamentoRepository tipoEquipamentoRepository;
    private final PosicaoEquipamentoRepository posicaoEquipamentoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EquipeRepository equipeRepository;
    private final ReservaRepository reservaRepository;

    public DashboardStatsResponse obterEstatisticas() {
        return new DashboardStatsResponse(
                salaRepository.countByDeletedAtIsNull(),
                posicaoRepository.countByDeletedAtIsNull(),
                posicaoRepository.countByDeletedAtIsNullAndStatusIgnoreCase(PosicaoStatus.ATIVA),
                tipoEquipamentoRepository.count(),
                tipoEquipamentoRepository.countByAtivoTrue(),
                posicaoEquipamentoRepository.somarQuantidadeTotal(),
                usuarioRepository.countByDeletedAtIsNullAndAtivoTrue(),
                equipeRepository.countAllEquipes(),
                reservaRepository.countByDeletedAtIsNull(),
                reservaRepository.countByDeletedAtIsNullAndStatus(StatusReserva.CONFIRMADA),
                reservaRepository.countByDeletedAtIsNullAndStatus(StatusReserva.PENDENTE),
                reservaRepository.countByDeletedAtIsNullAndStatus(StatusReserva.CANCELADA),
                reservaRepository.countByDeletedAtIsNullAndStatus(StatusReserva.REJEITADA));
    }
}
