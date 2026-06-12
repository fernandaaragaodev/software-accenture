import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { reservasApi } from '../../api/reservas';
import { Alert, EmptyState, PageHeader, StatusBadge } from '../../components/ui';
import { useAuth } from '../../context/AuthContext';
import type { ReservaResumoResponse } from '../../types';

function formatarHorario(value: string) {
  return value?.slice(0, 5) ?? '';
}

export function ReservasListPage() {
  const { hasRole } = useAuth();
  const isAdmin = hasRole('ADMIN_SALA');
  const [aba, setAba] = useState<'ativas' | 'canceladas'>('ativas');
  const [reservas, setReservas] = useState<ReservaResumoResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const carregar = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const lista = await reservasApi.listar(aba === 'canceladas');
      setReservas(lista);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao carregar reservas');
      setReservas([]);
    } finally {
      setLoading(false);
    }
  }, [aba]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  if (isAdmin) {
    return null;
  }

  return (
    <div>
      <PageHeader
        title="Minhas Reservas"
        subtitle="Todas as suas reservas"
        action={
          <Link to="/reservas/nova" className="btn btn-primary">
            Nova Reserva
          </Link>
        }
      />

      <div className="card form-card mb-lg">
        <div className="tabs">
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
          description={`Não há reservas ${aba === 'canceladas' ? 'canceladas' : 'ativas'}.`}
          action={<Link to="/reservas/nova" className="btn btn-primary">Criar reserva</Link>}
        />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Sala</th>
                <th>Data</th>
                <th>Horário</th>
                <th>Pessoas</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {reservas.map((r) => (
                <tr key={r.id}>
                  <td>{r.salaNome}</td>
                  <td>{r.dataReserva}</td>
                  <td>
                    {formatarHorario(r.horaInicio)} – {formatarHorario(r.horaFim)}
                  </td>
                  <td>{r.quantidadePessoas}</td>
                  <td><StatusBadge status={r.status} /></td>
                  <td>
                    <Link to={`/reservas/${r.id}`} className="btn btn-sm btn-ghost">
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

export function GestaoReservasPage() {
  const [reservaId, setReservaId] = useState('');
  const [error, setError] = useState('');

  function handleBuscar(e: React.FormEvent) {
    e.preventDefault();
    if (!reservaId.trim()) {
      setError('Informe o ID da reserva');
      return;
    }
    window.location.href = `/reservas/${reservaId.trim()}`;
  }

  return (
    <div>
      <PageHeader
        title="Gestão de Reservas"
        subtitle="Busque uma reserva pelo ID para confirmar, rejeitar ou cancelar"
      />
      <Alert message={error} />
      <div className="card form-card">
        <form onSubmit={handleBuscar} className="form inline-form">
          <label>
            ID da reserva
            <input
              value={reservaId}
              onChange={(e) => setReservaId(e.target.value)}
              placeholder="UUID da reserva"
              required
            />
          </label>
          <button type="submit" className="btn btn-primary">Buscar</button>
        </form>
      </div>
    </div>
  );
}
