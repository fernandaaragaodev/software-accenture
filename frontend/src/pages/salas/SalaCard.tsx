import { Link } from 'react-router-dom';
import { ReadinessBadge, StatusBadge } from '../../components/ui';
import {
  formatBlocoAndar,
  getReadinessLabel,
  getSalaReadiness,
  type SalaEnriched,
} from './salasUtils';

interface SalaCardProps {
  sala: SalaEnriched;
  onArchive: (sala: SalaEnriched) => void;
}

export function SalaCard({ sala, onArchive }: SalaCardProps) {
  const readiness = getSalaReadiness(sala, sala.regra, sala.layout, sala.posicoes);
  const layoutAprovado = !!sala.layout?.aprovadoEm;

  return (
    <article className="sala-card">
      <div className="sala-card-header">
        <div>
          <h3>{sala.nome}</h3>
          {sala.descricao && <p className="muted sala-card-desc">{sala.descricao}</p>}
        </div>
        <ReadinessBadge readiness={readiness} />
      </div>

      <div className="sala-card-meta">
        <span>{formatBlocoAndar(sala.bloco, sala.andar)}</span>
        <span>Capacidade: {sala.capacidadeMaxima}</span>
        <span><StatusBadge status={sala.status} /></span>
      </div>

      <p className="muted sala-card-hint">{getReadinessLabel(readiness)}</p>

      <div className="sala-card-actions">
        <Link to={`/salas/${sala.id}`} className="btn btn-sm btn-ghost">
          Visualizar
        </Link>
        <Link to={`/salas/${sala.id}/editar`} className="btn btn-sm btn-ghost">
          Editar
        </Link>
        <Link to={`/layouts?salaId=${sala.id}`} className="btn btn-sm btn-ghost">
          Configurar Layout
        </Link>
        {sala.layout && !layoutAprovado && (
          <Link to={`/layouts?salaId=${sala.id}`} className="btn btn-sm btn-primary">
            Aprovar Layout
          </Link>
        )}
        <Link to={`/salas/${sala.id}#disponibilidade`} className="btn btn-sm btn-ghost">
          Disponibilidade
        </Link>
        <Link to={`/reservas/nova?salaId=${sala.id}`} className="btn btn-sm btn-primary">
          Nova Reserva
        </Link>
        <button type="button" className="btn btn-sm btn-danger" onClick={() => onArchive(sala)}>
          Arquivar
        </button>
      </div>
    </article>
  );
}
