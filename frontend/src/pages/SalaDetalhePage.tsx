import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { salasApi } from '../api/salas.api';
import { Badge, statusSalaBadgeVariant } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { Loading } from '../components/ui/Loading';
import type { Sala } from '../types/sala.types';
import { getApiErrorMessage } from '../utils/apiError';
import { statusSalaLabel } from '../utils/formatters';
import { useAuth } from '../hooks/useAuth';
import { canManageSalas } from '../utils/permissions';

export function SalaDetalhePage() {
  const { id } = useParams<{ id: string }>();
  const { roles } = useAuth();
  const [sala, setSala] = useState<Sala | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!id) return;
    salasApi
      .buscar(id)
      .then(({ data }) => setSala(data))
      .catch((err) => setError(getApiErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) return <Loading />;
  if (error || !sala) return <ErrorMessage message={error ?? 'Sala não encontrada.'} />;

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-slate-900">{sala.nome}</h1>
          <div className="mt-2">
            <Badge variant={statusSalaBadgeVariant(sala.status)}>
              {statusSalaLabel(sala.status)}
            </Badge>
          </div>
        </div>
        <div className="flex flex-wrap gap-2">
          <Link to="/salas">
            <Button variant="ghost">Voltar</Button>
          </Link>
          {canManageSalas(roles) && (
            <>
              <Link to={`/salas/${sala.id}/editar`}>
                <Button variant="secondary">Editar</Button>
              </Link>
              <Link to={`/salas/${sala.id}/layout`}>
                <Button variant="secondary">Layout</Button>
              </Link>
            </>
          )}
          <Link to={`/salas/${sala.id}/disponibilidade`}>
            <Button>Disponibilidade</Button>
          </Link>
        </div>
      </div>

      <dl className="grid gap-3 rounded-xl border border-border bg-white p-6 shadow-sm sm:grid-cols-2">
        <div>
          <dt className="text-xs text-slate-500">Descrição</dt>
          <dd className="font-medium">{sala.descricao ?? '—'}</dd>
        </div>
        <div>
          <dt className="text-xs text-slate-500">Bloco / Andar</dt>
          <dd className="font-medium">
            {sala.bloco ?? '—'} / {sala.andar ?? '—'}
          </dd>
        </div>
        <div>
          <dt className="text-xs text-slate-500">Capacidade máxima</dt>
          <dd className="font-medium">{sala.capacidadeMaxima ?? '—'}</dd>
        </div>
        <div>
          <dt className="text-xs text-slate-500">Raio de proximidade</dt>
          <dd className="font-medium">{sala.raioProximidade ?? '—'}</dd>
        </div>
        <div>
          <dt className="text-xs text-slate-500">Imagem</dt>
          <dd className="font-medium">{sala.imagemPath ?? '—'}</dd>
        </div>
      </dl>
    </div>
  );
}
