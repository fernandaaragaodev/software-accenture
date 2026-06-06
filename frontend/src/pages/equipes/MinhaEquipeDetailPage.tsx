import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { equipesApi } from '../../api/equipes';
import { Alert, PageHeader } from '../../components/ui';
import type { EquipeResponse } from '../../types';

export function MinhaEquipeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [equipe, setEquipe] = useState<EquipeResponse | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    equipesApi
      .obter(id)
      .then(setEquipe)
      .catch((err) => setError(err instanceof ApiException ? err.message : 'Erro ao carregar equipe'))
      .finally(() => setLoading(false));
  }, [id]);

  if (loading) {
    return <div className="page-center"><div className="spinner" /></div>;
  }

  if (!equipe) {
    return <Alert message={error || 'Equipe não encontrada'} />;
  }

  return (
    <div>
      <PageHeader
        title={equipe.nome}
        subtitle={equipe.descricao}
        action={<Link to="/minhas-equipes" className="btn btn-ghost">Voltar</Link>}
      />
      <Alert message={error} />

      <div className="detail-grid">
        <div className="card">
          <h3>Gestor(es)</h3>
          <ul className="simple-list">
            {equipe.gestores.map((g) => (
              <li key={g.id}>
                <strong>{g.nome}</strong> — {g.email}
              </li>
            ))}
          </ul>
        </div>

        <div className="card">
          <h3>Membros ({equipe.membros.length})</h3>
          {equipe.membros.length === 0 ? (
            <p className="muted">Nenhum membro cadastrado.</p>
          ) : (
            <ul className="simple-list">
              {equipe.membros.map((m) => (
                <li key={m.id}>
                  <strong>{m.nome}</strong> — {m.email}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  );
}
