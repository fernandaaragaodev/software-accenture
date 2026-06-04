import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { reservasApi } from '../api/reservas.api';
import { Badge, statusReservaBadgeVariant } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { Input } from '../components/ui/Input';
import { Loading } from '../components/ui/Loading';
import { Table } from '../components/ui/Table';
import { useAuth } from '../hooks/useAuth';
import type { Reserva } from '../types/reserva.types';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate, statusReservaLabel } from '../utils/formatters';
import {
  addStoredReservaId,
  canManageReservasGestor,
  canViewReservaDetalheApi,
  getStoredReservaIds,
} from '../utils/permissions';

export function ReservasPage() {
  const { roles } = useAuth();
  const [reservas, setReservas] = useState<Reserva[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [novoId, setNovoId] = useState('');

  const load = async () => {
    setLoading(true);
    setError(null);
    const ids = getStoredReservaIds();

    if (!canViewReservaDetalheApi(roles)) {
      setReservas([]);
      setLoading(false);
      return;
    }

    try {
      const results = await Promise.all(
        ids.map((id) =>
          reservasApi.buscar(id).then((r) => r.data).catch(() => null),
        ),
      );
      setReservas(results.filter((r): r is Reserva => r !== null));
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, [roles]);

  const adicionarId = () => {
    if (!novoId.trim()) return;
    addStoredReservaId(novoId.trim());
    setNovoId('');
    void load();
  };

  if (loading) return <Loading />;

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Reservas</h1>
          <p className="text-sm text-slate-500">
            {canManageReservasGestor(roles)
              ? 'Gestão de reservas do sistema'
              : 'Suas reservas recentes neste navegador'}
          </p>
        </div>
        <Link to="/reservas/nova">
          <Button>Nova reserva</Button>
        </Link>
      </div>

      {canManageReservasGestor(roles) && (
        <div className="flex flex-wrap gap-2 rounded-lg border border-border bg-white p-4">
          <Input
            placeholder="UUID da reserva"
            value={novoId}
            onChange={(e) => setNovoId(e.target.value)}
            className="max-w-md flex-1"
          />
          <Button variant="secondary" onClick={adicionarId}>
            Rastrear reserva
          </Button>
        </div>
      )}

      {error && <ErrorMessage message={error} onRetry={() => void load()} />}

      {!canViewReservaDetalheApi(roles) && reservas.length === 0 && (
        <p className="text-sm text-slate-600">
          As reservas criadas aparecerão aqui com link para detalhes. A API não expõe listagem
          geral; use os atalhos após criar uma nova reserva.
        </p>
      )}

      <Table<Reserva>
        data={reservas}
        keyExtractor={(r) => r.id}
        emptyMessage="Nenhuma reserva rastreada. Crie uma nova reserva ou adicione um ID."
        columns={[
          {
            key: 'data',
            header: 'Data',
            render: (r) => formatDate(r.dataReserva),
          },
          { key: 'sala', header: 'Sala ID', render: (r) => r.salaId.slice(0, 8) + '…' },
          { key: 'qtd', header: 'Pessoas', render: (r) => r.quantidadePessoas },
          {
            key: 'status',
            header: 'Status',
            render: (r) => (
              <Badge variant={statusReservaBadgeVariant(r.status)}>
                {statusReservaLabel(r.status)}
              </Badge>
            ),
          },
          {
            key: 'acoes',
            header: 'Ações',
            render: (r) => (
              <Link to={`/reservas/${r.id}`} state={{ reserva: r }}>
                <Button size="sm" variant="ghost">
                  Detalhes
                </Button>
              </Link>
            ),
          },
        ]}
      />

      {getStoredReservaIds().length > 0 && !canViewReservaDetalheApi(roles) && (
        <ul className="space-y-2">
          {getStoredReservaIds().map((id) => (
            <li key={id}>
              <Link
                to={`/reservas/${id}`}
                className="text-primary-600 hover:underline"
              >
                Reserva {id.slice(0, 8)}…
              </Link>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
