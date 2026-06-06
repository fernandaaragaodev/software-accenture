package com.accenture.officehub_v1.dto.response;

public record DashboardStatsResponse(
        long totalSalas,
        long totalPosicoes,
        long totalPosicoesAtivas,
        long totalTiposEquipamento,
        long totalTiposEquipamentoAtivos,
        long totalEquipamentosVinculados,
        long totalUsuarios,
        long totalEquipes,
        long totalReservas,
        long reservasConfirmadas,
        long reservasPendentes,
        long reservasCanceladas,
        long reservasRejeitadas
) {
}
