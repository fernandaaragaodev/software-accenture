package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.CriarRegraDisponibilidadeRequest;
import com.accenture.officehub_v1.dto.request.ExcecaoDisponibilidadeRequest;
import com.accenture.officehub_v1.dto.request.HorarioDisponibilidadeRequest;
import com.accenture.officehub_v1.dto.response.RegraDisponibilidadeResponse;
import com.accenture.officehub_v1.dto.response.ValidacaoDisponibilidadeResponse;
import com.accenture.officehub_v1.entity.ExcecaoDisponibilidade;
import com.accenture.officehub_v1.entity.HorarioDisponibilidade;
import com.accenture.officehub_v1.entity.RegraDisponibilidade;
import com.accenture.officehub_v1.entity.Sala;
import com.accenture.officehub_v1.entity.Usuario;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.ExcecaoDisponibilidadeRepository;
import com.accenture.officehub_v1.repository.HorarioDisponibilidadeRepository;
import com.accenture.officehub_v1.repository.RegraDisponibilidadeRepository;
import com.accenture.officehub_v1.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DisponibilidadeService {

    private final RegraDisponibilidadeRepository regraDisponibilidadeRepository;
    private final HorarioDisponibilidadeRepository horarioDisponibilidadeRepository;
    private final ExcecaoDisponibilidadeRepository excecaoDisponibilidadeRepository;
    private final SalaService salaService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public RegraDisponibilidadeResponse criarRegra(UUID salaId, CriarRegraDisponibilidadeRequest request) {
        Sala sala = salaService.buscarEntidadeAtiva(salaId);

        if (regraDisponibilidadeRepository.existsBySalaId(salaId)) {
            throw new RegraNegocioException(
                    "Esta sala já possui regra de disponibilidade cadastrada.");
        }

        validarHorarios(request.horarios());

        RegraDisponibilidade regra = RegraDisponibilidade.builder()
                .sala(sala)
                .antecedenciaMinimaDias(request.antecedenciaMinimaDias())
                .build();

        regra = regraDisponibilidadeRepository.save(regra);

        RegraDisponibilidade regraFinal = regra;
        List<HorarioDisponibilidade> horarios = request.horarios().stream()
                .map(h -> HorarioDisponibilidade.builder()
                        .regraDisponibilidade(regraFinal)
                        .diaSemana(h.diaSemana())
                        .horaAbertura(h.horaAbertura())
                        .horaFechamento(h.horaFechamento())
                        .build())
                .map(horarioDisponibilidadeRepository::save)
                .toList();

        return RegraDisponibilidadeResponse.from(regra, horarios);
    }

    @Transactional
    public void adicionarExcecao(UUID salaId, ExcecaoDisponibilidadeRequest request, UUID createdById) {
        Sala sala = salaService.buscarEntidadeAtiva(salaId);

        if (excecaoDisponibilidadeRepository.existsBySalaIdAndData(salaId, request.data())) {
            throw new RegraNegocioException(
                    "Já existe uma exceção de disponibilidade cadastrada para esta data.");
        }

        Usuario createdBy = usuarioRepository.findByIdAndDeletedAtIsNull(createdById)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Usuário responsável pelo cadastro não encontrado."));

        ExcecaoDisponibilidade excecao = ExcecaoDisponibilidade.builder()
                .sala(sala)
                .data(request.data())
                .motivo(request.motivo())
                .createdBy(createdBy)
                .build();

        excecaoDisponibilidadeRepository.save(excecao);
    }

    public ValidacaoDisponibilidadeResponse validarReservaPermitida(UUID salaId, LocalDate data) {
        salaService.buscarEntidadeAtiva(salaId);

        try {
            validarDisponibilidade(salaId, data);
            return new ValidacaoDisponibilidadeResponse(salaId, data, true,
                    "A sala está disponível para reserva na data informada.");
        } catch (RegraNegocioException ex) {
            return new ValidacaoDisponibilidadeResponse(salaId, data, false, ex.getMessage());
        }
    }

    public void validarDisponibilidade(UUID salaId, LocalDate data) {
        RegraDisponibilidade regra = regraDisponibilidadeRepository.findBySalaId(salaId)
                .orElseThrow(() -> new RegraNegocioException(
                        "A sala não possui regra de disponibilidade configurada."));

        verificarExcecao(salaId, data);
        verificarAntecedenciaMinima(regra, data);
        verificarDiaSemanaPermitido(regra, data);
    }

    public void verificarAntecedenciaMinima(RegraDisponibilidade regra, LocalDate data) {
        LocalDate dataMinima = LocalDate.now().plusDays(regra.getAntecedenciaMinimaDias());
        if (data.isBefore(dataMinima)) {
            throw new RegraNegocioException(String.format(
                    "A reserva deve ser feita com antecedência mínima de %d dia(s). " +
                            "A data mais próxima permitida é %s.",
                    regra.getAntecedenciaMinimaDias(),
                    dataMinima));
        }
    }

    public void verificarExcecao(UUID salaId, LocalDate data) {
        excecaoDisponibilidadeRepository.findBySalaIdAndData(salaId, data).ifPresent(excecao -> {
            String motivo = excecao.getMotivo() != null && !excecao.getMotivo().isBlank()
                    ? excecao.getMotivo()
                    : "data bloqueada ou feriado";
            throw new RegraNegocioException(
                    "A sala não está disponível para reserva em " + data + ": " + motivo + ".");
        });
    }

    public void verificarDiaSemanaPermitido(RegraDisponibilidade regra, LocalDate data) {
        int diaSemana = converterDiaSemana(data);
        boolean permitido = horarioDisponibilidadeRepository
                .existsByRegraDisponibilidadeIdAndDiaSemana(regra.getId(), diaSemana);

        if (!permitido) {
            throw new RegraNegocioException(
                    "A sala não aceita reservas no dia da semana informado.");
        }
    }

    public RegraDisponibilidadeResponse buscarRegraPorSala(UUID salaId) {
        salaService.buscarEntidadeAtiva(salaId);

        RegraDisponibilidade regra = regraDisponibilidadeRepository.findBySalaId(salaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Regra de disponibilidade não encontrada para esta sala."));

        List<HorarioDisponibilidade> horarios =
                horarioDisponibilidadeRepository.findByRegraDisponibilidadeId(regra.getId());

        return RegraDisponibilidadeResponse.from(regra, horarios);
    }

    /**
     * Converte LocalDate para dia da semana no formato 0=segunda ... 6=domingo.
     */
    int converterDiaSemana(LocalDate data) {
        return data.getDayOfWeek().getValue() - 1;
    }

    private void validarHorarios(List<HorarioDisponibilidadeRequest> horarios) {
        Set<Integer> dias = new HashSet<>();
        for (HorarioDisponibilidadeRequest horario : horarios) {
            if (!horario.horaAbertura().isBefore(horario.horaFechamento())) {
                throw new RegraNegocioException(
                        "A hora de abertura deve ser anterior à hora de fechamento.");
            }
            if (!dias.add(horario.diaSemana())) {
                throw new RegraNegocioException(
                        "Não é permitido cadastrar mais de um horário para o mesmo dia da semana.");
            }
        }
    }
}
