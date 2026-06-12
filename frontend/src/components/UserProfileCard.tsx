import type { UsuarioResumo } from '../types';

interface UserProfileCardProps {
  usuario: UsuarioResumo;
  open: boolean;
  onClose: () => void;
}

export function UserProfileCard({ usuario, open, onClose }: UserProfileCardProps) {
  if (!open) return null;

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true" onClick={onClose}>
      <div className="modal user-profile-card" onClick={(e) => e.stopPropagation()}>
        <h3>{usuario.nome}</h3>
        <dl className="profile-details">
          <div>
            <dt>Cargo</dt>
            <dd>{usuario.cargoNome || 'Não informado'}</dd>
          </div>
          <div>
            <dt>E-mail</dt>
            <dd>{usuario.email}</dd>
          </div>
          <div>
            <dt>Especialidades</dt>
            <dd>
              {usuario.especialidades.length > 0 ? (
                <div className="tag-list">
                  {usuario.especialidades.map((esp) => (
                    <span key={esp.id} className="tag">{esp.nome}</span>
                  ))}
                </div>
              ) : (
                <span className="muted">Nenhuma especialidade cadastrada</span>
              )}
            </dd>
          </div>
        </dl>
        <div className="modal-actions">
          <button type="button" className="btn btn-primary" onClick={onClose}>
            Fechar
          </button>
        </div>
      </div>
    </div>
  );
}
