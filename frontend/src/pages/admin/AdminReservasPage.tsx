import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { reservasApi } from '../../api/reservas';
import { Alert, EmptyState, PageHeader, StatusBadge } from '../../components/ui';
import type { ReservaResumoResponse } from '../../types';

function formatarHorario(value: string) {
  return value?.slice(0, 5) ?? '';
}

export function AdminReservasPage() {
  const [aba, setAba] = useState<'ativas' | 'canceladas'>('ativas');
  const [data, setData] = useState('');
  const [reservas, setReservas] = useState<ReservaResumoResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const carregar = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const lista = await reservasApi.listar(aba === 'canceladas', data || undefined);
      setReservas(lista);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao carregar reservas');
      setReservas([]);
    } finally {
      setLoading(false);
    }
  }, [data, aba]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  return (
    <div>
      <PageHeader
        title="Reservas"
        subtitle="Visualize e gerencie todas as reservas do sistema"
      />

      <div className="card form-card mb-lg">
        <div className="form-grid">
          <label>
            Filtrar por data (opcional)
            <input
              type="date"
              value={data}
              onChange={(e) => setData(e.target.value)}
            />
          </label>
          {data && (
            <div className="filter-actions">
              <button type="button" className="btn btn-ghost btn-sm" onClick={() => setData('')}>
                Limpar filtro
              </button>
            </div>
          )}
        </div>
        <div className="tabs mt-md">
          <button
            type="button"
            className={`tab ${aba === 'ativas' ? 'tab-active' : ''}`}
            onClick={() => setAba('ativas')}
          >
            Reservas
          </button>
          <button
            type="button"
            className={`tab ${aba === 'canceladas' ? 'tab-active' : ''}`}
            onClick={() => setAba('canceladas')}
          >
            Reservas canceladas
          </button>
        </div>
      </div>

      <Alert message={error} />

      {loading ? (
        <div className="page-center"><div className="spinner" /></div>
      ) : reservas.length === 0 ? (
        <EmptyState
          title={aba === 'canceladas' ? 'Nenhuma reserva cancelada' : 'Nenhuma reserva'}
          description={
            data
              ? `Não há reservas ${aba === 'canceladas' ? 'canceladas' : 'ativas'} para ${data}.`
              : `Não há reservas ${aba === 'canceladas' ? 'canceladas' : 'ativas'}.`
          }
        />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Sala</th>
                <th>Solicitante</th>
                <th>Data</th>
                <th>Horário</th>
                <th>Pessoas</th>
                <th>Status</th>
                {aba === 'canceladas' && <th>Cancelado por</th>}
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {reservas.map((r) => (
                <tr key={r.id}>
                  <td>{r.salaNome}</td>
                  <td>{r.solicitanteNome}</td>
                  <td>{r.dataReserva}</td>
                  <td>
                    {formatarHorario(r.horaInicio)} – {formatarHorario(r.horaFim)}
                  </td>
                  <td>{r.quantidadePessoas}</td>
                  <td><StatusBadge status={r.status} /></td>
                  {aba === 'canceladas' && (
                    <td>{r.canceladoPorNome ?? '—'}</td>
                  )}
                  <td>
                    <Link to={`/admin/reservas/${r.id}`} className="btn btn-sm btn-ghost">
                      Detalhes
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
