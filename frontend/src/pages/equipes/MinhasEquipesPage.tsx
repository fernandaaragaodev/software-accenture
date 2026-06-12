import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { equipesApi } from '../../api/equipes';
import { Alert, EmptyState, LoadingState, PageHeader } from '../../components/ui';
import type { EquipeResumoResponse } from '../../types';

export function MinhasEquipesPage() {
  const [equipes, setEquipes] = useState<EquipeResumoResponse[]>([]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    equipesApi
      .listarMinhas()
      .then(setEquipes)
      .catch((err) => setError(err instanceof ApiException ? err.message : 'Erro ao carregar equipes'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <LoadingState message="Carregando equipes..." />;
  }

  return (
    <div>
      <PageHeader
        title="Minhas Equipes"
        subtitle="Equipes das quais você faz parte"
      />
      <Alert message={error} />

      {equipes.length === 0 ? (
        <EmptyState
          title="Nenhuma equipe"
          description="Você ainda não foi adicionado a nenhuma equipe."
        />
      ) : (
        <div className="cards-grid">
          {equipes.map((equipe) => (
            <Link key={equipe.id} to={`/minhas-equipes/${equipe.id}`} className="card card-link">
              <h3>{equipe.nome}</h3>
              {equipe.descricao && <p className="muted">{equipe.descricao}</p>}
              <div className="card-meta">
                <span>{equipe.quantidadeGestores} gestor(es)</span>
                <span>{equipe.quantidadeMembros} membro(s)</span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
