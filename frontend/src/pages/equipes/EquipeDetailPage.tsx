import type { FormEvent } from 'react';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { equipesApi } from '../../api/equipes';
import { usuariosApi } from '../../api/usuarios';
import { UserProfileCard } from '../../components/UserProfileCard';
import { Alert, LoadingState, PageHeader, PasswordConfirmDialog } from '../../components/ui';
import type { EquipeResponse, UsuarioResumo } from '../../types';

type AcaoSenha = 'desfazer' | 'remover-membro';

export function EquipeDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [equipe, setEquipe] = useState<EquipeResponse | null>(null);
  const [usuarioId, setUsuarioId] = useState('');
  const [usuarios, setUsuarios] = useState<UsuarioResumo[]>([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);
  const [acaoSenha, setAcaoSenha] = useState<AcaoSenha | null>(null);
  const [membroRemoverId, setMembroRemoverId] = useState<string | null>(null);
  const [senhaLoading, setSenhaLoading] = useState(false);
  const [senhaError, setSenhaError] = useState('');
  const [usuarioSelecionado, setUsuarioSelecionado] = useState<UsuarioResumo | null>(null);

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

  function abrirRemoverMembro(membroId: string) {
    setMembroRemoverId(membroId);
    setAcaoSenha('remover-membro');
    setSenhaError('');
  }

  function abrirDesfazerEquipe() {
    setAcaoSenha('desfazer');
    setSenhaError('');
  }

  function fecharConfirmacaoSenha() {
    setAcaoSenha(null);
    setMembroRemoverId(null);
    setSenhaError('');
    setSenhaLoading(false);
  }

  async function handleConfirmarSenha(senha: string) {
    if (!id || !acaoSenha) return;
    setSenhaLoading(true);
    setSenhaError('');
    setError('');
    setSuccess('');

    try {
      if (acaoSenha === 'desfazer') {
        await equipesApi.desfazer(id, { senha });
        navigate('/equipes');
        return;
      }

      if (acaoSenha === 'remover-membro' && membroRemoverId) {
        const updated = await equipesApi.removerMembro(id, membroRemoverId, { senha });
        setEquipe(updated);
        setSuccess('Membro removido');
      }
      fecharConfirmacaoSenha();
    } catch (err) {
      const message = err instanceof ApiException ? err.message : 'Erro ao confirmar ação';
      setSenhaError(message);
    } finally {
      setSenhaLoading(false);
    }
  }

  if (loading) {
    return <LoadingState message="Carregando equipe..." />;
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
          <ul className="member-list">
            {equipe.gestores.map((g) => (
              <li key={g.id}>
                <button
                  type="button"
                  className="member-link"
                  onClick={() => setUsuarioSelecionado(g)}
                >
                  <strong>{g.nome}</strong>
                  <small>{g.cargoNome || 'Cargo não informado'}</small>
                </button>
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
                  <button
                    type="button"
                    className="member-link"
                    onClick={() => setUsuarioSelecionado(m)}
                  >
                    <strong>{m.nome}</strong>
                    <small>{m.cargoNome || 'Cargo não informado'}</small>
                  </button>
                  <button
                    type="button"
                    className="btn btn-sm btn-danger"
                    onClick={() => abrirRemoverMembro(m.id)}
                  >
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
                    {u.nome} ({u.cargoNome || u.email})
                  </option>
                ))}
            </select>
          </label>
          <button type="submit" className="btn btn-primary">Adicionar</button>
        </form>
      </div>

      <div className="mt-lg">
        <button type="button" className="btn btn-danger" onClick={abrirDesfazerEquipe}>
          Desfazer equipe
        </button>
      </div>

      <PasswordConfirmDialog
        open={acaoSenha === 'desfazer'}
        title="Desfazer equipe"
        message="Informe sua senha para desfazer esta equipe. Esta ação não pode ser revertida."
        confirmLabel="Desfazer equipe"
        variant="danger"
        loading={senhaLoading}
        error={senhaError}
        onConfirm={handleConfirmarSenha}
        onCancel={fecharConfirmacaoSenha}
      />

      <PasswordConfirmDialog
        open={acaoSenha === 'remover-membro'}
        title="Remover membro"
        message="Informe sua senha para remover este membro da equipe."
        confirmLabel="Remover membro"
        variant="danger"
        loading={senhaLoading}
        error={senhaError}
        onConfirm={handleConfirmarSenha}
        onCancel={fecharConfirmacaoSenha}
      />

      {usuarioSelecionado && (
        <UserProfileCard
          usuario={usuarioSelecionado}
          open
          onClose={() => setUsuarioSelecionado(null)}
        />
      )}
    </div>
  );
}
