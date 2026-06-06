package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.request.TipoEquipamentoRequest;
import com.accenture.officehub_v1.dto.response.TipoEquipamentoResponse;
import com.accenture.officehub_v1.entity.TipoEquipamento;
import com.accenture.officehub_v1.exception.RecursoNaoEncontradoException;
import com.accenture.officehub_v1.exception.RegraNegocioException;
import com.accenture.officehub_v1.repository.TipoEquipamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TipoEquipamentoService {

    private final TipoEquipamentoRepository tipoEquipamentoRepository;

    @Transactional
    public TipoEquipamentoResponse criar(TipoEquipamentoRequest request) {
        validarNomeDuplicado(request.nome(), null);

        TipoEquipamento tipoEquipamento = TipoEquipamento.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .ativo(request.ativo() != null ? request.ativo() : true)
                .build();

        return TipoEquipamentoResponse.from(tipoEquipamentoRepository.save(tipoEquipamento));
    }

    public List<TipoEquipamentoResponse> listar() {
        return tipoEquipamentoRepository.findAllByOrderByNomeAsc().stream()
                .map(TipoEquipamentoResponse::from)
                .toList();
    }

    public TipoEquipamentoResponse buscarPorId(UUID id) {
        return TipoEquipamentoResponse.from(buscarEntidade(id));
    }

    @Transactional
    public TipoEquipamentoResponse atualizar(UUID id, TipoEquipamentoRequest request) {
        TipoEquipamento tipoEquipamento = buscarEntidade(id);
        validarNomeDuplicado(request.nome(), id);

        tipoEquipamento.setNome(request.nome());
        tipoEquipamento.setDescricao(request.descricao());
        if (request.ativo() != null) {
            tipoEquipamento.setAtivo(request.ativo());
        }

        return TipoEquipamentoResponse.from(tipoEquipamentoRepository.save(tipoEquipamento));
    }

    @Transactional
    public TipoEquipamentoResponse inativar(UUID id) {
        TipoEquipamento tipoEquipamento = buscarEntidade(id);
        tipoEquipamento.setAtivo(false);
        return TipoEquipamentoResponse.from(tipoEquipamentoRepository.save(tipoEquipamento));
    }

    @Transactional
    public TipoEquipamentoResponse ativar(UUID id) {
        TipoEquipamento tipoEquipamento = buscarEntidade(id);
        tipoEquipamento.setAtivo(true);
        return TipoEquipamentoResponse.from(tipoEquipamentoRepository.save(tipoEquipamento));
    }

    private TipoEquipamento buscarEntidade(UUID id) {
        return tipoEquipamentoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Tipo de equipamento não encontrado."));
    }

    private void validarNomeDuplicado(String nome, UUID idExcluir) {
        boolean duplicado = idExcluir == null
                ? tipoEquipamentoRepository.existsByNomeIgnoreCase(nome)
                : tipoEquipamentoRepository.existsByNomeIgnoreCaseAndIdNot(nome, idExcluir);

        if (duplicado) {
            throw new RegraNegocioException(
                    "Já existe um tipo de equipamento cadastrado com o nome informado.");
        }
    }
}
