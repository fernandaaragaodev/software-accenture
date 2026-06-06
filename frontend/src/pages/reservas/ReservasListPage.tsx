import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { reservasApi } from '../../api/reservas';
import { Alert, EmptyState, PageHeader, StatusBadge } from '../../components/ui';
import { getStoredReservationIds } from '../../utils/auth';
import type { ReservaResponse } from '../../types';

export function ReservasListPage() {
  const [reservas, setReservas] = useState<ReservaResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const ids = getStoredReservationIds();
    if (ids.length === 0) {
      setLoading(false);
      return;
    }

    Promise.all(
      ids.map((id) =>
        reservasApi.obter(id).catch(() => null),
      ),
    )
      .then((results) => setReservas(results.filter((r): r is ReservaResponse => r !== null)))
      .catch((err) => setError(err instanceof ApiException ? err.message : 'Erro ao carregar reservas'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <PageHeader
        title="Minhas Reservas"
        subtitle="Reservas solicitadas neste navegador"
        action={
          <Link to="/reservas/nova" className="btn btn-primary">
            Nova Reserva
          </Link>
        }
      />
      <Alert message={error} />

      {loading ? (
        <div className="page-center"><div className="spinner" /></div>
      ) : reservas.length === 0 ? (
        <EmptyState
          title="Nenhuma reserva encontrada"
          description="As reservas criadas neste navegador aparecerão aqui. O backend não possui endpoint de listagem."
          action={<Link to="/reservas/nova" className="btn btn-primary">Criar reserva</Link>}
        />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Data</th>
                <th>Pessoas</th>
                <th>Status</th>
                <th>Alocações</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {reservas.map((r) => (
                <tr key={r.id}>
                  <td>{r.dataReserva}</td>
                  <td>{r.quantidadePessoas}</td>
                  <td><StatusBadge status={r.status} /></td>
                  <td>{r.alocacoes.length}</td>
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
