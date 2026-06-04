import { useEffect, useState } from 'react';
import { notificacoesApi } from '../api/notificacoes.api';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { Input } from '../components/ui/Input';
import { Loading } from '../components/ui/Loading';
import { Table } from '../components/ui/Table';
import { useAuth } from '../hooks/useAuth';
import type { Notificacao } from '../types/notificacao.types';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDateTime } from '../utils/formatters';

export function NotificacoesPage() {
  const { user, setUsuarioId } = useAuth();
  const [usuarioId, setUsuarioIdLocal] = useState(user?.usuarioId ?? '');
  const [notificacoes, setNotificacoes] = useState<Notificacao[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = async () => {
    if (!usuarioId.trim()) {
      setError('Informe o ID do usuário para listar notificações (a API exige o UUID).');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const { data } = await notificacoesApi.listarPorUsuario(usuarioId.trim());
      setNotificacoes(data);
      setUsuarioId(usuarioId.trim());
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (user?.usuarioId) {
      setUsuarioIdLocal(user.usuarioId);
      void load();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const processarFila = async () => {
    try {
      await notificacoesApi.processarFila();
      alert('Fila processada com sucesso.');
      await load();
    } catch (err) {
      alert(getApiErrorMessage(err));
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">Notificações</h1>
          <p className="text-sm text-slate-500">
            Endpoint restrito a gestores de reservas
          </p>
        </div>
        <Button variant="secondary" onClick={() => void processarFila()}>
          Processar fila
        </Button>
      </div>

      <div className="flex flex-wrap gap-2 rounded-lg border border-border bg-white p-4">
        <Input
          label="ID do usuário (UUID)"
          value={usuarioId}
          onChange={(e) => setUsuarioIdLocal(e.target.value)}
          className="max-w-md flex-1"
        />
        <div className="flex items-end">
          <Button onClick={() => void load()} loading={loading}>
            Buscar
          </Button>
        </div>
      </div>

      <p className="text-xs text-slate-500">
        O ID do usuário é gravado automaticamente após criar uma reserva (campo solicitanteId).
      </p>

      {error && <ErrorMessage message={error} />}
      {loading ? (
        <Loading />
      ) : (
        <Table<Notificacao>
          data={notificacoes}
          keyExtractor={(n) => n.id}
          columns={[
            { key: 'tipo', header: 'Tipo', render: (n) => n.tipo },
            { key: 'assunto', header: 'Assunto', render: (n) => n.assunto },
            {
              key: 'mensagem',
              header: 'Mensagem',
              render: (n) => (
                <span className="line-clamp-2 max-w-xs">{n.mensagem}</span>
              ),
            },
            {
              key: 'status',
              header: 'Status',
              render: (n) => <Badge variant="info">{n.status}</Badge>,
            },
            {
              key: 'data',
              header: 'Data',
              render: (n) => formatDateTime(n.createdAt),
            },
          ]}
        />
      )}
    </div>
  );
}
