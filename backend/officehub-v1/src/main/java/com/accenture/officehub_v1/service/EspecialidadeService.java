package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.response.EspecialidadeResponse;
import com.accenture.officehub_v1.repository.EspecialidadeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EspecialidadeService {

    private final EspecialidadeRepository especialidadeRepository;

    public List<EspecialidadeResponse> listar() {
        return especialidadeRepository.findAllByOrderByNomeAsc().stream()
                .map(EspecialidadeResponse::from)
                .toList();
    }
}
