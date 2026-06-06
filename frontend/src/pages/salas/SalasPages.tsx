import type { FormEvent } from 'react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { salasApi } from '../../api/salas';
import { Alert, EmptyState, PageHeader, StatusBadge } from '../../components/ui';
import type { SalaResponse } from '../../types';

export function SalasListPage() {
  const [salas, setSalas] = useState<SalaResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    salasApi
      .listar()
      .then(setSalas)
      .catch((err) => setError(err instanceof ApiException ? err.message : 'Erro ao carregar salas'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div>
      <PageHeader
        title="Salas"
        subtitle="Gerencie salas, capacidade e status"
        action={
          <Link to="/salas/nova" className="btn btn-primary">
            Nova Sala
          </Link>
        }
      />

      <Alert message={error} />

      {loading ? (
        <div className="page-center"><div className="spinner" /></div>
      ) : salas.length === 0 ? (
        <EmptyState
          title="Nenhuma sala cadastrada"
          description="Crie a primeira sala para começar a configurar layouts e posições."
          action={
            <Link to="/salas/nova" className="btn btn-primary">
              Criar sala
            </Link>
          }
        />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>Bloco / Andar</th>
                <th>Capacidade</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {salas.map((sala) => (
                <tr key={sala.id}>
                  <td>
                    <strong>{sala.nome}</strong>
                    {sala.descricao && <small className="block muted">{sala.descricao}</small>}
                  </td>
                  <td>
                    {sala.bloco ?? '—'} / {sala.andar ?? '—'}
                  </td>
                  <td>{sala.capacidadeMaxima}</td>
                  <td><StatusBadge status={sala.status} /></td>
                  <td>
                    <Link to={`/salas/${sala.id}`} className="btn btn-sm btn-ghost">
                      Detalhes
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}

export function SalaFormPage() {
  const [nome, setNome] = useState('');
  const [descricao, setDescricao] = useState('');
  const [andar, setAndar] = useState('');
  const [bloco, setBloco] = useState('');
  const [capacidadeMaxima, setCapacidadeMaxima] = useState('10');
  const [raioProximidade, setRaioProximidade] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const sala = await salasApi.criar({
        nome,
        descricao: descricao || undefined,
        andar: andar ? Number(andar) : undefined,
        bloco: bloco || undefined,
        capacidadeMaxima: Number(capacidadeMaxima),
        raioProximidade: raioProximidade ? Number(raioProximidade) : undefined,
      });
      setSuccess(`Sala "${sala.nome}" criada com sucesso!`);
      setNome('');
      setDescricao('');
      setAndar('');
      setBloco('');
      setCapacidadeMaxima('10');
      setRaioProximidade('');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao criar sala');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <PageHeader title="Nova Sala" subtitle="Cadastre uma nova sala no sistema" />
      <div className="card form-card">
        <form onSubmit={handleSubmit} className="form">
          <Alert message={error} />
          <Alert type="success" message={success} />
          <div className="form-grid">
            <label>
              Nome *
              <input value={nome} onChange={(e) => setNome(e.target.value)} required />
            </label>
            <label>
              Capacidade máxima *
              <input
                type="number"
                min={1}
                value={capacidadeMaxima}
                onChange={(e) => setCapacidadeMaxima(e.target.value)}
                required
              />
            </label>
            <label>
              Bloco
              <input value={bloco} onChange={(e) => setBloco(e.target.value)} />
            </label>
            <label>
              Andar
              <input type="number" value={andar} onChange={(e) => setAndar(e.target.value)} />
            </label>
            <label>
              Raio de proximidade
              <input
                type="number"
                step="0.1"
                value={raioProximidade}
                onChange={(e) => setRaioProximidade(e.target.value)}
              />
            </label>
            <label className="full-width">
              Descrição
              <textarea value={descricao} onChange={(e) => setDescricao(e.target.value)} rows={3} />
            </label>
          </div>
          <div className="form-actions">
            <Link to="/salas" className="btn btn-ghost">Voltar</Link>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Salvando...' : 'Criar sala'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
