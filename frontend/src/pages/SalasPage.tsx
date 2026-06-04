import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { salasApi } from '../api/salas.api';
import { Badge, statusSalaBadgeVariant } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { Loading } from '../components/ui/Loading';
import { Select } from '../components/ui/Select';
import { Table } from '../components/ui/Table';
import { useAuth } from '../hooks/useAuth';
import type { Sala } from '../types/sala.types';
import { getApiErrorMessage } from '../utils/apiError';
import { statusSalaLabel } from '../utils/formatters';
import { canManageSalas } from '../utils/permissions';

export function SalasPage() {
  const { roles } = useAuth();
  const [salas, setSalas] = useState<Sala[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filtroStatus, setFiltroStatus] = useState<string>('');

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await salasApi.listar();
      setSalas(data);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const filtradas = useMemo(() => {
    if (!filtroStatus) return salas;
    return salas.filter((s) => s.status === filtroStatus);
  }, [salas, filtroStatus]);

  const handleInativar = async (id: string) => {
    if (!confirm('Deseja inativar esta sala?')) return;
    try {
      await salasApi.inativar(id);
      await load();
    } catch (err) {
      alert(getApiErrorMessage(err));
    }
  };

  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} onRetry={() => void load()} />;

  const admin = canManageSalas(roles);

  return (
    <div className="space-y-4">
      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Salas</h1>
          <p className="text-sm text-slate-500">Listagem e gestão de salas</p>
        </div>
        {admin && (
          <Link to="/salas/nova">
            <Button>Nova sala</Button>
          </Link>
        )}
      </div>

      <Select
        label="Filtrar por status"
        value={filtroStatus}
        onChange={(e) => setFiltroStatus(e.target.value)}
        options={[
          { value: '', label: 'Todos' },
          { value: 'ATIVA', label: 'Ativa' },
          { value: 'INATIVA', label: 'Inativa' },
          { value: 'MANUTENCAO', label: 'Manutenção' },
        ]}
        className="max-w-xs"
      />

      <Table<Sala>
        data={filtradas}
        keyExtractor={(s) => s.id}
        columns={[
          { key: 'nome', header: 'Nome', render: (s) => s.nome },
          { key: 'bloco', header: 'Bloco', render: (s) => s.bloco ?? '—' },
          { key: 'andar', header: 'Andar', render: (s) => s.andar ?? '—' },
          {
            key: 'capacidade',
            header: 'Capacidade',
            render: (s) => s.capacidadeMaxima ?? '—',
          },
          {
            key: 'status',
            header: 'Status',
            render: (s) => (
              <Badge variant={statusSalaBadgeVariant(s.status)}>
                {statusSalaLabel(s.status)}
              </Badge>
            ),
          },
          {
            key: 'acoes',
            header: 'Ações',
            render: (s) => (
              <div className="flex flex-wrap gap-1">
                <Link to={`/salas/${s.id}`}>
                  <Button size="sm" variant="ghost">
                    Ver
                  </Button>
                </Link>
                {admin && (
                  <>
                    <Link to={`/salas/${s.id}/editar`}>
                      <Button size="sm" variant="secondary">
                        Editar
                      </Button>
                    </Link>
                    <Button size="sm" variant="danger" onClick={() => void handleInativar(s.id)}>
                      Inativar
                    </Button>
                    <Link to={`/salas/${s.id}/layout`}>
                      <Button size="sm" variant="ghost">
                        Layout
                      </Button>
                    </Link>
                  </>
                )}
                <Link to={`/salas/${s.id}/disponibilidade`}>
                  <Button size="sm" variant="ghost">
                    Disponibilidade
                  </Button>
                </Link>
              </div>
            ),
          },
        ]}
      />
    </div>
  );
}
