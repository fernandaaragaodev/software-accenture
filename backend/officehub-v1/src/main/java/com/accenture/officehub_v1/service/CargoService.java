package com.accenture.officehub_v1.service;

import com.accenture.officehub_v1.dto.response.CargoResponse;
import com.accenture.officehub_v1.repository.CargoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CargoService {

    private final CargoRepository cargoRepository;

    public List<CargoResponse> listar() {
        return cargoRepository.findAllByOrderByNomeAsc().stream()
                .map(CargoResponse::from)
                .toList();
    }
}
