import { useCallback, useEffect, useState } from 'react';
import { ApiException } from '../../api/client';
import { layoutsApi } from '../../api/layouts';
import { posicoesApi } from '../../api/posicoes';
import { reservasApi } from '../../api/reservas';
import { salasApi } from '../../api/salas';
import { tiposEquipamentoApi } from '../../api/tipos-equipamento';
import type { PosicaoEquipamentoResponse } from '../../types';
import { loadDisponibilidadePosicoes, type SalaEnriched } from './salasUtils';

async function loadEquipamentosCount(posicoes: { id: string }[]) {
  if (posicoes.length === 0) return 0;

  const equipamentosPorPosicao: PosicaoEquipamentoResponse[][] = await Promise.all(
    posicoes.map((p) =>
      tiposEquipamentoApi
        .listarPorPosicao(p.id)
        .catch((): PosicaoEquipamentoResponse[] => []),
    ),
  );

  return equipamentosPorPosicao.reduce((total, items) => {
    const subtotal = items.reduce((sum, item) => sum + item.quantidade, 0);
    return total + subtotal;
  }, 0);
}

async function enrichSala(salaId: string): Promise<SalaEnriched> {
  const sala = await salasApi.obter(salaId);
  const [regra, layout, posicoes, reservas] = await Promise.all([
    salasApi.listarRegrasDisponibilidade(salaId).catch(() => null),
    layoutsApi.obterAtivo(salaId).catch(() => null),
    posicoesApi.listarPorSala(salaId).catch(() => []),
    reservasApi.listar(true).catch(() => []),
  ]);

  const reservasSala = reservas.filter((r) => r.salaId === salaId);
  const totalEquipamentos = await loadEquipamentosCount(posicoes);
  const disponibilidade = await loadDisponibilidadePosicoes(salaId);

  return {
    ...sala,
    regra,
    layout,
    posicoes,
    totalEquipamentos,
    reservas: reservasSala,
    posicoesLivres: disponibilidade.posicoesLivres,
    posicoesOcupadas: disponibilidade.posicoesOcupadas,
  };
}

export function useSalaEnriched(salaId?: string) {
  const [data, setData] = useState<SalaEnriched | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const reload = useCallback(async () => {
    if (!salaId) return;
    setLoading(true);
    setError('');
    try {
      const enriched = await enrichSala(salaId);
      setData(enriched);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao carregar sala');
      setData(null);
    } finally {
      setLoading(false);
    }
  }, [salaId]);

  useEffect(() => {
    reload();
  }, [reload]);

  return { data, loading, error, reload };
}

export function useSalasEnrichedList() {
  const [salas, setSalas] = useState<SalaEnriched[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const reload = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const baseSalas = await salasApi.listar();
      const reservas = await reservasApi.listar(true).catch(() => []);

      const enriched = await Promise.all(
        baseSalas.map(async (sala) => {
          const [regra, layout, posicoes] = await Promise.all([
            salasApi.listarRegrasDisponibilidade(sala.id).catch(() => null),
            layoutsApi.obterAtivo(sala.id).catch(() => null),
            posicoesApi.listarPorSala(sala.id).catch(() => []),
          ]);

          const reservasSala = reservas.filter((r) => r.salaId === sala.id);
          const totalEquipamentos = await loadEquipamentosCount(posicoes);

          return {
            ...sala,
            regra,
            layout,
            posicoes,
            totalEquipamentos,
            reservas: reservasSala,
          };
        }),
      );

      setSalas(enriched);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao carregar salas');
      setSalas([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    reload();
  }, [reload]);

  return { salas, loading, error, reload };
}

export { enrichSala };
