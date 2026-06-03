package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Posicao;
import com.accenture.officehub_v1.entity.Reserva;
import com.accenture.officehub_v1.entity.enums.StatusReserva;
import com.accenture.officehub_v1.service.alocacao.ItemAlocacao;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReservaPosicaoAlocadaResponse(
        UUID posicaoId,
        String posicaoIdentificador,
        String posicaoTipo
) {

    public static ReservaPosicaoAlocadaResponse from(Posicao posicao) {
        return new ReservaPosicaoAlocadaResponse(
                posicao.getId(),
                posicao.getIdentificador(),
                posicao.getTipo()
        );
    }

    public static ReservaPosicaoAlocadaResponse from(ItemAlocacao item) {
        return from(item.posicao());
    }
}
