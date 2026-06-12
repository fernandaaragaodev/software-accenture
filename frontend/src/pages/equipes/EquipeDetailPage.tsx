import type { FormEvent } from 'react';
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { equipesApi } from '../../api/equipes';
import { usuariosApi } from '../../api/usuarios';
import { Alert, PageHeader } from '../../components/ui';
import type { EquipeResponse, UsuarioResumo } from '../../types';

export function EquipeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [equipe, setEquipe] = useState<EquipeResponse | null>(null);
  const [usuarioId, setUsuarioId] = useState('');
  const [usuarios, setUsuarios] = useState<UsuarioResumo[]>([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);

  function loadEquipe() {
    if (!id) return;
    equipesApi
      .obter(id)
      .then(setEquipe)
      .catch((err) => setError(err instanceof ApiException ? err.message : 'Erro ao carregar equipe'))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    loadEquipe();
    usuariosApi.listarDisponiveisEquipe().then(setUsuarios).catch(() => setUsuarios([]));
  }, [id]);

  async function handleAddMembro(e: FormEvent) {
    e.preventDefault();
    if (!id) return;
    setError('');
    setSuccess('');
    try {
      const updated = await equipesApi.adicionarMembro(id, { usuarioId });
      setEquipe(updated);
      setUsuarioId('');
      setSuccess('Membro adicionado com sucesso');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao adicionar membro');
    }
  }

  async function handleRemoveMembro(membroId: string) {
    if (!id) return;
    setError('');
    try {
      const updated = await equipesApi.removerMembro(id, membroId);
      setEquipe(updated);
      setSuccess('Membro removido');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao remover membro');
    }
  }

  async function handleDesmembrar() {
    if (!id || !confirm('Deseja realmente desmembrar esta equipe?')) return;
    try {
      await equipesApi.desmembrar(id);
      window.location.href = '/equipes';
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao desmembrar equipe');
    }
  }

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
        action={<Link to="/equipes" className="btn btn-ghost">Voltar</Link>}
      />
      <Alert message={error} />
      <Alert type="success" message={success} />

      <div className="detail-grid">
        <div className="card">
          <h3>Gestores</h3>
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
            <ul className="member-list">
              {equipe.membros.map((m) => (
                <li key={m.id}>
                  <div>
                    <strong>{m.nome}</strong>
                    <small>{m.email}</small>
                  </div>
                  <button type="button" className="btn btn-sm btn-danger" onClick={() => handleRemoveMembro(m.id)}>
                    Remover
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <div className="card mt-lg">
        <h3>Adicionar membro</h3>
        <form onSubmit={handleAddMembro} className="form inline-form">
          <label>
            Usuário
            <select value={usuarioId} onChange={(e) => setUsuarioId(e.target.value)} required>
              <option value="">Selecione...</option>
              {usuarios
                .filter((u) => !equipe.membros.some((m) => m.id === u.id))
                .filter((u) => !equipe.gestores.some((g) => g.id === u.id))
                .map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.nome} ({u.email})
                  </option>
                ))}
            </select>
          </label>
          <button type="submit" className="btn btn-primary">Adicionar</button>
        </form>
      </div>

      <div className="mt-lg">
        <button type="button" className="btn btn-danger" onClick={handleDesmembrar}>
          Desmembrar equipe
        </button>
      </div>
    </div>
  );
}
