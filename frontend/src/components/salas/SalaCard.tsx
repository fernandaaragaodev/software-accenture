import { Link } from 'react-router-dom';
import { StatusBadge } from '../ui';
import { SalaReadinessBadge } from './SalaReadinessBadge';
import type { SalaEnriched } from '../../utils/salas';

interface SalaCardProps {
  sala: SalaEnriched;
  onDuplicate?: (id: string) => void;
  onArchive?: (id: string) => void;
  duplicating?: boolean;
}

export function SalaCard({ sala, onDuplicate, onArchive, duplicating }: SalaCardProps) {
  const posicoesAtivas = sala.posicoes.filter((p) => p.status === 'ATIVA').length;

  return (
    <article className="sala-card card">
      <div className="sala-card-header">
        <div>
          <h3>{sala.nome}</h3>
          {sala.descricao && <p className="muted sala-card-desc">{sala.descricao}</p>}
        </div>
        <SalaReadinessBadge readiness={sala.readiness} />
      </div>

      <div className="sala-card-meta">
        <span>{sala.bloco ?? '—'} / Andar {sala.andar ?? '—'}</span>
        <span>Cap. {sala.capacidadeMaxima}</span>
        <StatusBadge status={sala.status} />
      </div>

      <div className="sala-card-stats">
        <div>
          <small>Layout</small>
          <strong>{sala.layout?.aprovadoPorId ? 'Aprovado' : sala.layout ? 'Pendente' : '—'}</strong>
        </div>
        <div>
          <small>Posições</small>
          <strong>{posicoesAtivas}</strong>
        </div>
        <div>
          <small>Disponibilidade</small>
          <strong>{sala.regra ? 'Sim' : 'Não'}</strong>
        </div>
      </div>

      <div className="sala-card-actions">
        <Link to={`/salas/${sala.id}`} className="btn btn-sm btn-primary" title="Visualizar">
          Visualizar
        </Link>
        <Link to={`/salas/${sala.id}/editar`} className="btn btn-sm btn-ghost" title="Editar">
          Editar
        </Link>
        <Link
          to={`/layouts?salaId=${sala.id}`}
          className="btn btn-sm btn-ghost"
          title="Configurar Layout"
        >
          Layout
        </Link>
        {sala.layout && !sala.layout.aprovadoPorId && (
          <Link
            to={`/layouts?salaId=${sala.id}`}
            className="btn btn-sm btn-ghost"
            title="Aprovar Layout"
          >
            Aprovar
          </Link>
        )}
        <Link
          to={`/salas/${sala.id}#disponibilidade`}
          className="btn btn-sm btn-ghost"
          title="Disponibilidade"
        >
          Disponibilidade
        </Link>
        <Link
          to={`/reservas/nova?salaId=${sala.id}`}
          className="btn btn-sm btn-ghost"
          title="Nova Reserva"
        >
          Reservar
        </Link>
      </div>

      {(onDuplicate || onArchive) && (
        <div className="sala-card-secondary-actions">
          {onDuplicate && (
            <button
              type="button"
              className="btn btn-sm btn-ghost"
              disabled={duplicating}
              onClick={() => onDuplicate(sala.id)}
            >
              {duplicating ? 'Duplicando...' : 'Duplicar'}
            </button>
          )}
          {onArchive && (
            <button
              type="button"
              className="btn btn-sm btn-danger"
              onClick={() => onArchive(sala.id)}
            >
              Arquivar
            </button>
          )}
        </div>
      )}
    </article>
  );
}
