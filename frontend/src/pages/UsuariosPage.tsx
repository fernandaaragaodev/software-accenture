import type { FormEvent } from 'react';
import { useEffect, useState } from 'react';
import { ApiException } from '../api/client';
import { registerUser } from '../api/auth';
import { cargosApi } from '../api/cargos';
import { especialidadesApi } from '../api/especialidades';
import { usuariosApi } from '../api/usuarios';
import { Alert, PageHeader } from '../components/ui';
import type {
  CargoResponse,
  EspecialidadeResponse,
  Role,
  UsuarioResponse,
} from '../types';

const PERFIS_DISPONIVEIS: { value: Role; label: string }[] = [
  { value: 'USUARIO_FINAL', label: 'Usuário Final' },
  { value: 'GESTOR_RESERVAS', label: 'Gestor de Reservas' },
];

export function UsuariosPage() {
  const [nome, setNome] = useState('');
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [perfis, setPerfis] = useState<Role[]>(['USUARIO_FINAL']);
  const [cargoId, setCargoId] = useState('');
  const [especialidadeIds, setEspecialidadeIds] = useState<string[]>([]);
  const [usuarios, setUsuarios] = useState<UsuarioResponse[]>([]);
  const [cargos, setCargos] = useState<CargoResponse[]>([]);
  const [especialidades, setEspecialidades] = useState<EspecialidadeResponse[]>([]);
  const [editandoId, setEditandoId] = useState<string | null>(null);
  const [editEmail, setEditEmail] = useState('');
  const [editCargoId, setEditCargoId] = useState('');
  const [editEspecialidadeIds, setEditEspecialidadeIds] = useState<string[]>([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  function loadData() {
    Promise.all([
      usuariosApi.listar(),
      cargosApi.listar(),
      especialidadesApi.listar(),
    ])
      .then(([users, cargoList, espList]) => {
        setUsuarios(users);
        setCargos(cargoList);
        setEspecialidades(espList);
      })
      .catch(() => {
        setUsuarios([]);
        setCargos([]);
        setEspecialidades([]);
      });
  }

  useEffect(() => {
    loadData();
  }, [success]);

  function togglePerfil(perfil: Role) {
    setPerfis((prev) =>
      prev.includes(perfil) ? prev.filter((p) => p !== perfil) : [...prev, perfil],
    );
  }

  function toggleEspecialidade(id: string) {
    setEspecialidadeIds((prev) =>
      prev.includes(id) ? prev.filter((e) => e !== id) : [...prev, id],
    );
  }

  function toggleEditEspecialidade(id: string) {
    setEditEspecialidadeIds((prev) =>
      prev.includes(id) ? prev.filter((e) => e !== id) : [...prev, id],
    );
  }

  function iniciarEdicao(usuario: UsuarioResponse) {
    setEditandoId(usuario.id);
    setEditEmail(usuario.email);
    setEditCargoId(usuario.cargo?.id ?? '');
    setEditEspecialidadeIds(usuario.especialidades.map((e) => e.id));
    setError('');
    setSuccess('');
  }

  function cancelarEdicao() {
    setEditandoId(null);
    setEditEmail('');
    setEditCargoId('');
    setEditEspecialidadeIds([]);
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
      const usuario = await registerUser({
        nome,
        email,
        senha,
        perfis,
        cargoId: cargoId || undefined,
        especialidadeIds,
      });
      setSuccess(`Usuário "${usuario.nome}" criado com perfis: ${usuario.perfis.join(', ')}`);
      setNome('');
      setEmail('');
      setSenha('');
      setPerfis(['USUARIO_FINAL']);
      setCargoId('');
      setEspecialidadeIds([]);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao cadastrar usuário');
    } finally {
      setLoading(false);
    }
  }

  async function handleSalvarEdicao(e: FormEvent) {
    e.preventDefault();
    if (!editandoId) return;
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const atualizado = await usuariosApi.atualizar(editandoId, {
        email: editEmail,
        cargoId: editCargoId || null,
        especialidadeIds: editEspecialidadeIds,
      });
      setSuccess(`Usuário "${atualizado.nome}" atualizado com sucesso.`);
      cancelarEdicao();
      loadData();
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao atualizar usuário');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <PageHeader
        title="Usuários"
        subtitle="Cadastre e edite usuários com cargo e especialidades"
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
            <label>
              Cargo
              <select value={cargoId} onChange={(e) => setCargoId(e.target.value)}>
                <option value="">Selecione...</option>
                {cargos.map((c) => (
                  <option key={c.id} value={c.id}>{c.nome}</option>
                ))}
              </select>
            </label>
            <fieldset className="checkbox-group">
              <legend>Especialidades</legend>
              {especialidades.map((esp) => (
                <label key={esp.id} className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={especialidadeIds.includes(esp.id)}
                    onChange={() => toggleEspecialidade(esp.id)}
                  />
                  {esp.nome}
                </label>
              ))}
            </fieldset>
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
                    <th>Cargo</th>
                    <th>Especialidades</th>
                    <th>Perfis</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {usuarios.map((u) => (
                    <tr key={u.id}>
                      <td>{u.nome}</td>
                      <td>{u.email}</td>
                      <td>{u.cargo?.nome ?? '—'}</td>
                      <td>
                        <div className="tag-list">
                          {u.especialidades.map((esp) => (
                            <span key={esp.id} className="tag">{esp.nome}</span>
                          ))}
                        </div>
                      </td>
                      <td>
                        <div className="tag-list">
                          {u.perfis.map((p) => (
                            <span key={p} className="tag">{p}</span>
                          ))}
                        </div>
                      </td>
                      <td>
                        <button
                          type="button"
                          className="btn btn-sm btn-ghost"
                          onClick={() => iniciarEdicao(u)}
                        >
                          Editar
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      {editandoId && (
        <div className="modal-overlay" role="dialog" aria-modal="true">
          <div className="modal">
            <h3>Editar usuário</h3>
            <form onSubmit={handleSalvarEdicao} className="form">
              <label>
                E-mail *
                <input
                  type="email"
                  value={editEmail}
                  onChange={(e) => setEditEmail(e.target.value)}
                  required
                />
              </label>
              <label>
                Cargo
                <select value={editCargoId} onChange={(e) => setEditCargoId(e.target.value)}>
                  <option value="">Selecione...</option>
                  {cargos.map((c) => (
                    <option key={c.id} value={c.id}>{c.nome}</option>
                  ))}
                </select>
              </label>
              <fieldset className="checkbox-group">
                <legend>Especialidades</legend>
                {especialidades.map((esp) => (
                  <label key={esp.id} className="checkbox-label">
                    <input
                      type="checkbox"
                      checked={editEspecialidadeIds.includes(esp.id)}
                      onChange={() => toggleEditEspecialidade(esp.id)}
                    />
                    {esp.nome}
                  </label>
                ))}
              </fieldset>
              <div className="modal-actions">
                <button type="button" className="btn btn-ghost" onClick={cancelarEdicao}>
                  Cancelar
                </button>
                <button type="submit" className="btn btn-primary" disabled={loading}>
                  {loading ? 'Salvando...' : 'Salvar alterações'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
