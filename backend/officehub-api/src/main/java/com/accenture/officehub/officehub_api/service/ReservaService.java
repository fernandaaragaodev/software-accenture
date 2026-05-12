package com.accenture.officehub.officehub_api.service;

import com.accenture.officehub.officehub_api.dto.ReservaRequest;
import com.accenture.officehub.officehub_api.dto.ReservaResponse;
import com.accenture.officehub.officehub_api.model.Reserva;
import com.accenture.officehub.officehub_api.repository.ReservaRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository repository;

    public ReservaService(ReservaRepository repository) {
        this.repository = repository;
    }

    public ReservaResponse criar(ReservaRequest request) {
        Reserva reserva = new Reserva();
        reserva.setTitulo(request.getTitulo());
        reserva.setNomeEspaco(request.getNomeEspaco());
        reserva.setLocal(request.getLocal());
        reserva.setRecursos(request.getRecursos());

        Reserva salva = repository.save(reserva);

        String link = "http://localhost:8080/reservas/" + salva.getId() + "/cancelar";
        salva.setLinkCancelamento(link);
        repository.save(salva);

        return toResponse(salva);
    }

    public void cancelar(String id) {
        Reserva reserva = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reserva não encontrada"));
        repository.delete(reserva);
    }

    public List<ReservaResponse> listar() {
        return repository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private ReservaResponse toResponse(Reserva r) {
        return ReservaResponse.of(
                r.getId(),
                r.getTitulo(),
                r.getNomeEspaco(),
                r.getLocal(),
                r.getRecursos(),
                r.getLinkCancelamento()
        );
    }
}
