import type { FormEvent } from 'react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { equipesApi } from '../../api/equipes';
import { usuariosApi } from '../../api/usuarios';
import { Alert, EmptyState, PageHeader } from '../../components/ui';
import { useAuth } from '../../context/AuthContext';
import type { EquipeResumoResponse, UsuarioResumo } from '../../types';

export function EquipesListPage() {
  const [equipes, setEquipes] = useState<EquipeResumoResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    equipesApi
      .listar()
      .then(setEquipes)
      .catch((err) => setError(err instanceof ApiException ? err.message : 'Erro ao carregar equipes'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <PageHeader
        title="Equipes"
        subtitle="Gerencie equipes, gestores e membros"
        action={
          <Link to="/equipes/nova" className="btn btn-primary">
            Nova Equipe
          </Link>
        }
      />
      <Alert message={error} />

      {loading ? (
        <div className="page-center"><div className="spinner" /></div>
      ) : equipes.length === 0 ? (
        <EmptyState
          title="Nenhuma equipe cadastrada"
          action={<Link to="/equipes/nova" className="btn btn-primary">Criar equipe</Link>}
        />
      ) : (
        <div className="cards-grid">
          {equipes.map((equipe) => (
            <Link key={equipe.id} to={`/equipes/${equipe.id}`} className="card card-link">
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

export function EquipeFormPage() {
  const { hasRole, roles } = useAuth();
  const [nome, setNome] = useState('');
  const [descricao, setDescricao] = useState('');
  const [gestorId, setGestorId] = useState('');
  const [membrosIds, setMembrosIds] = useState<string[]>([]);
  const [gestores, setGestores] = useState<UsuarioResumo[]>([]);
  const [usuarios, setUsuarios] = useState<UsuarioResumo[]>([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (hasRole('ADMIN_SALA')) {
      usuariosApi.listarGestores().then(setGestores).catch(() => setGestores([]));
    }
    usuariosApi.listarDisponiveisEquipe().then(setUsuarios).catch(() => setUsuarios([]));
  }, [roles]);

  const membrosDisponiveis = usuarios.filter((u) => u.id !== gestorId);

  function toggleMembro(usuarioId: string) {
    setMembrosIds((prev) =>
      prev.includes(usuarioId)
        ? prev.filter((id) => id !== usuarioId)
        : [...prev, usuarioId],
    );
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (membrosIds.length === 0) {
      setError('Selecione pelo menos um membro para a equipe.');
      return;
    }
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const equipe = await equipesApi.criar({
        nome,
        descricao: descricao || undefined,
        gestorId: hasRole('ADMIN_SALA') ? gestorId : undefined,
        membrosIds,
      });
      setSuccess(`Equipe "${equipe.nome}" criada com ${equipe.membros.length} membro(s)!`);
      setNome('');
      setDescricao('');
      setGestorId('');
      setMembrosIds([]);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao criar equipe');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <PageHeader title="Nova Equipe" subtitle="Cadastre uma nova equipe com gestor e membros" />
      <div className="card form-card">
        <form onSubmit={handleSubmit} className="form">
          <Alert message={error} />
          <Alert type="success" message={success} />
          <label>
            Nome *
            <input value={nome} onChange={(e) => setNome(e.target.value)} required />
          </label>
          <label>
            Descrição
            <textarea value={descricao} onChange={(e) => setDescricao(e.target.value)} rows={3} />
          </label>
          {hasRole('ADMIN_SALA') && (
            <label>
              Gestor *
              <select value={gestorId} onChange={(e) => setGestorId(e.target.value)} required>
                <option value="">Selecione o gestor...</option>
                {gestores.map((g) => (
                  <option key={g.id} value={g.id}>
                    {g.nome} ({g.email})
                  </option>
                ))}
              </select>
              <small className="muted">Apenas usuários com perfil GESTOR_RESERVAS.</small>
            </label>
          )}
          <fieldset className="checkbox-group">
            <legend>Membros da equipe *</legend>
            {membrosDisponiveis.length === 0 ? (
              <p className="muted">Nenhum usuário disponível para seleção.</p>
            ) : (
              <div className="member-list">
                {membrosDisponiveis.map((u) => (
                  <label key={u.id} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={membrosIds.includes(u.id)}
                      onChange={() => toggleMembro(u.id)}
                    />
                    <span>
                      <strong>{u.nome}</strong>
                      <small className="muted block">{u.email}</small>
                    </span>
                  </label>
                ))}
              </div>
            )}
          </fieldset>
          <div className="form-actions">
            <Link to="/equipes" className="btn btn-ghost">Voltar</Link>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Salvando...' : 'Criar equipe'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
