import type { FormEvent } from 'react';
import { useEffect, useState } from 'react';
import { ApiException } from '../api/client';
import { registerUser } from '../api/auth';
import { usuariosApi } from '../api/usuarios';
import { Alert, PageHeader } from '../components/ui';
import type { Role, UsuarioResponse } from '../types';

const PERFIS_DISPONIVEIS: { value: Role; label: string }[] = [
  { value: 'USUARIO_FINAL', label: 'Usuário Final' },
  { value: 'GESTOR_RESERVAS', label: 'Gestor de Reservas' },
];

export function UsuariosPage() {
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [perfis, setPerfis] = useState<Role[]>(['USUARIO_FINAL']);
  const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    usuariosApi.listar().then(setUsuarios).catch(() => setUsuarios([]));
  }, [success]);

  function togglePerfil(perfil: Role) {
    setPerfis((prev) =>
      prev.includes(perfil) ? prev.filter((p) => p !== perfil) : [...prev, perfil],
    );
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (perfis.length === 0) {
      setError('Selecione pelo menos um perfil.');
      return;
    }
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const usuario = await registerUser({ nome, email, senha, perfis });
      setSuccess(`Usuário "${usuario.nome}" criado com perfis: ${usuario.perfis.join(', ')}`);
      setNome('');
      setEmail('');
      setSenha('');
      setPerfis(['USUARIO_FINAL']);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao cadastrar usuário');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <PageHeader
        title="Usuários"
        subtitle="Cadastre novos usuários com perfis no sistema"
      />

      <div className="detail-grid">
        <div className="card form-card">
          <h3>Novo usuário</h3>
          <form onSubmit={handleSubmit} className="form">
            <Alert message={error} />
            <Alert type="success" message={success} />
            <label>
              Nome *
              <input value={nome} onChange={(e) => setNome(e.target.value)} required />
            </label>
            <label>
              E-mail *
              <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
            </label>
            <label>
              Senha * (mín. 8 caracteres)
              <input
                type="password"
                value={senha}
                onChange={(e) => setSenha(e.target.value)}
                required
                minLength={8}
              />
            </label>
            <fieldset className="checkbox-group">
              <legend>Perfis *</legend>
              {PERFIS_DISPONIVEIS.map((p) => (
                <label key={p.value} className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={perfis.includes(p.value)}
                    onChange={() => togglePerfil(p.value)}
                  />
                  {p.label}
                </label>
              ))}
            </fieldset>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Cadastrando...' : 'Cadastrar usuário'}
            </button>
          </form>
        </div>

        <div className="card">
          <h3>Usuários cadastrados ({usuarios.length})</h3>
          {usuarios.length === 0 ? (
            <p className="muted">Nenhum usuário encontrado.</p>
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Nome</th>
                    <th>E-mail</th>
                    <th>Perfis</th>
                  </tr>
                </thead>
                <tbody>
                  {usuarios.map((u) => (
                    <tr key={u.id}>
                      <td>{u.nome}</td>
                      <td>{u.email}</td>
                      <td>
                        <div className="tag-list">
                          {u.perfis.map((p) => (
                            <span key={p} className="tag">{p}</span>
                          ))}
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
