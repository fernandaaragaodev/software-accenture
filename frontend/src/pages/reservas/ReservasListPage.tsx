import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, Navigate } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { reservasApi } from '../../api/reservas';
import { Alert, EmptyState, LoadingState, PageHeader, StatusBadge } from '../../components/ui';
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
    return <Navigate to="/admin/reservas" replace />;
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
        <LoadingState message="Carregando reservas..." />
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
                <tr key={r.id} className={r.status === 'PENDENTE' ? 'highlight-pending' : undefined}>
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
  const [aba, setAba] = useState<'pendentes' | 'todas'>('pendentes');
  const [busca, setBusca] = useState('');
  const [reservas, setReservas] = useState<ReservaResumoResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const carregar = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const lista = await reservasApi.listar(false);
      setReservas(lista);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao carregar reservas');
      setReservas([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const reservasFiltradas = useMemo(() => {
    let lista = reservas;
    if (aba === 'pendentes') {
      lista = lista.filter((r) => r.status === 'PENDENTE');
    }
    if (busca.trim()) {
      const termo = busca.trim().toLowerCase();
      lista = lista.filter(
        (r) =>
          r.salaNome.toLowerCase().includes(termo) ||
          r.dataReserva.includes(termo) ||
          r.id.toLowerCase().includes(termo),
      );
    }
    return lista;
  }, [reservas, aba, busca]);

  return (
    <div>
      <PageHeader
        title="Gestão de Reservas"
        subtitle="Visualize e gerencie reservas pendentes de confirmação"
      />

      <div className="card form-card mb-lg">
        <div className="search-bar">
          <label>
            Buscar por sala, data ou ID
            <input
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
              placeholder="Digite para filtrar..."
            />
          </label>
        </div>
        <div className="tabs mt-md">
          <button
            type="button"
            className={`tab ${aba === 'pendentes' ? 'tab-active' : ''}`}
            onClick={() => setAba('pendentes')}
          >
            Pendentes
          </button>
          <button
            type="button"
            className={`tab ${aba === 'todas' ? 'tab-active' : ''}`}
            onClick={() => setAba('todas')}
          >
            Todas ativas
          </button>
        </div>
      </div>

      <Alert message={error} />

      {loading ? (
        <LoadingState message="Carregando reservas..." />
      ) : reservasFiltradas.length === 0 ? (
        <EmptyState
          title={aba === 'pendentes' ? 'Nenhuma reserva pendente' : 'Nenhuma reserva encontrada'}
          description={
            busca
              ? 'Tente ajustar os termos da busca.'
              : aba === 'pendentes'
                ? 'Não há reservas aguardando confirmação no momento.'
                : 'Não há reservas ativas no sistema.'
          }
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
              {reservasFiltradas.map((r) => (
                <tr key={r.id} className={r.status === 'PENDENTE' ? 'highlight-pending' : undefined}>
                  <td>{r.salaNome}</td>
                  <td>{r.dataReserva}</td>
                  <td>
                    {formatarHorario(r.horaInicio)} – {formatarHorario(r.horaFim)}
                  </td>
                  <td>{r.quantidadePessoas}</td>
                  <td><StatusBadge status={r.status} /></td>
                  <td>
                    <Link to={`/reservas/${r.id}`} className="btn btn-sm btn-primary">
                      Gerenciar
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
