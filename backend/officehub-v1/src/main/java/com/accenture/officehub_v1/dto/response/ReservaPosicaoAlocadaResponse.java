package com.accenture.officehub_v1.dto.response;

import com.accenture.officehub_v1.entity.Posicao;
import com.accenture.officehub_v1.entity.ReservaPosicao;
import com.accenture.officehub_v1.service.alocacao.ItemAlocacao;

import java.util.List;
import java.util.UUID;

public record ReservaPosicaoAlocadaResponse(
        UUID posicaoId,
        String posicaoIdentificador,
        String posicaoTipo,
        String pessoaNome,
        List<String> equipamentos
) {

    public static ReservaPosicaoAlocadaResponse from(Posicao posicao) {
        return new ReservaPosicaoAlocadaResponse(
                posicao.getId(),
                posicao.getIdentificador(),
                posicao.getTipo(),
                null,
                List.of()
        );
    }

    public static ReservaPosicaoAlocadaResponse from(ItemAlocacao item) {
        return from(item.posicao());
    }

    public static ReservaPosicaoAlocadaResponse from(ReservaPosicao reservaPosicao, List<String> equipamentos) {
        Posicao posicao = reservaPosicao.getPosicao();
        String pessoaNome = reservaPosicao.getReservaPessoa().getUsuario() != null
                ? reservaPosicao.getReservaPessoa().getUsuario().getNome()
                : reservaPosicao.getReservaPessoa().getNomeExterno();

        return new ReservaPosicaoAlocadaResponse(
                posicao.getId(),
                posicao.getIdentificador(),
                posicao.getTipo(),
                pessoaNome,
                equipamentos
        );
    }
}
