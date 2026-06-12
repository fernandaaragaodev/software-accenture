import type { FormEvent } from 'react';
import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { layoutsApi } from '../../api/layouts';
import { posicoesApi } from '../../api/posicoes';
import { salasApi } from '../../api/salas';
import { Alert, EmptyState, PageHeader, StatusBadge } from '../../components/ui';
import type { LayoutResponse, PosicaoResponse, SalaResponse } from '../../types';

export function LayoutsPage() {
  const [searchParams] = useSearchParams();
  const [salas, setSalas] = useState<SalaResponse[]>([]);
  const [salaId, setSalaId] = useState(searchParams.get('salaId') ?? '');
  const [layout, setLayout] = useState<LayoutResponse | null>(null);
  const [layouts, setLayouts] = useState<LayoutResponse[]>([]);
  const [versao, setVersao] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    salasApi.listar().then((data) => {
      setSalas(data);
      const paramId = searchParams.get('salaId');
      if (paramId && data.some((s) => s.id === paramId)) {
        setSalaId(paramId);
      } else if (data.length > 0 && !salaId) {
        setSalaId(data[0].id);
      }
    }).catch(() => setSalas([]));
  }, [searchParams]);

  useEffect(() => {
    if (!salaId) return;
    setLayout(null);
    layoutsApi
      .obterAtivo(salaId)
      .then((ativo) => {
        setLayout(ativo);
        setLayouts([ativo]);
      })
      .catch(() => setLayouts([]));
  }, [salaId]);

  async function handleCriarLayout(e: FormEvent) {
    e.preventDefault();
    if (!salaId) return;
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const created = await layoutsApi.criar({ salaId, versao: versao || undefined });
      setLayouts((prev) => [created, ...prev]);
      setSuccess(`Layout v${created.versao ?? created.id.slice(0, 8)} criado. Aprove-o para poder cadastrar posições.`);
      setVersao('');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao criar layout');
    } finally {
      setLoading(false);
    }
  }

  async function handleAprovar(layoutId: string) {
    setError('');
    setSuccess('');
    try {
      const approved = await layoutsApi.aprovar(layoutId);
      setLayout(approved);
      setLayouts((prev) =>
        prev.map((l) => (l.id === layoutId ? approved : { ...l, ativo: false })),
      );
      setSuccess('Layout aprovado e ativado com sucesso!');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao aprovar layout');
    }
  }

  return (
    <div>
      <PageHeader
        title="Layouts"
        subtitle="Crie e aprove layouts antes de cadastrar posições"
      />
      <Alert message={error} />
      <Alert type="success" message={success} />

      <div className="card form-card">
        <label>
          Sala
          <select value={salaId} onChange={(e) => setSalaId(e.target.value)}>
            {salas.map((s) => (
              <option key={s.id} value={s.id}>{s.nome}</option>
            ))}
          </select>
        </label>
      </div>

      <div className="detail-grid mt-lg">
        <div className="card">
          <h3>Layout ativo</h3>
          {layout ? (
            <dl className="detail-list">
              <div><dt>ID</dt><dd className="mono">{layout.id}</dd></div>
              <div><dt>Versão</dt><dd>{layout.versao ?? '—'}</dd></div>
              <div><dt>Status</dt><dd><StatusBadge status={layout.ativo ? 'ATIVA' : 'INATIVA'} /></dd></div>
              <div><dt>Aprovado em</dt><dd>{layout.aprovadoEm ? new Date(layout.aprovadoEm).toLocaleString('pt-BR') : '—'}</dd></div>
            </dl>
          ) : (
            <EmptyState title="Nenhum layout ativo" description="Crie e aprove um layout para esta sala." />
          )}
        </div>

        <div className="card">
          <h3>Novo layout</h3>
          <form onSubmit={handleCriarLayout} className="form">
            <label>
              Versão (opcional)
              <input value={versao} onChange={(e) => setVersao(e.target.value)} placeholder="v1.0" />
            </label>
            <button type="submit" className="btn btn-primary" disabled={loading || !salaId}>
              {loading ? 'Criando...' : 'Criar layout'}
            </button>
          </form>
        </div>
      </div>

      {layouts.length > 0 && (
        <div className="card mt-lg">
          <h3>Layouts da sala</h3>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Versão</th>
                  <th>Ativo</th>
                  <th>Criado em</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {layouts.map((l) => (
                  <tr key={l.id}>
                    <td>{l.versao ?? l.id.slice(0, 8)}</td>
                    <td>{l.ativo ? 'Sim' : 'Não'}</td>
                    <td>{new Date(l.createdAt).toLocaleString('pt-BR')}</td>
                    <td>
                      {!l.ativo && (
                        <button type="button" className="btn btn-sm btn-primary" onClick={() => handleAprovar(l.id)}>
                          Aprovar
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
}

export function PosicoesPage() {
  const [searchParams] = useSearchParams();
  const [salas, setSalas] = useState<SalaResponse[]>([]);
  const [salaId, setSalaId] = useState(searchParams.get('salaId') ?? '');
  const [posicoes, setPosicoes] = useState<PosicaoResponse[]>([]);
  const [identificador, setIdentificador] = useState('');
  const [tipo, setTipo] = useState('');
  const [coordX, setCoordX] = useState('');
  const [coordY, setCoordY] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    salasApi.listar().then((data) => {
      setSalas(data);
      const paramId = searchParams.get('salaId');
      if (paramId && data.some((s) => s.id === paramId)) {
        setSalaId(paramId);
      } else if (data.length > 0 && !salaId) {
        setSalaId(data[0].id);
      }
    });
  }, [searchParams]);

  useEffect(() => {
    if (!salaId) return;
    posicoesApi
      .listarPorSala(salaId)
      .then(setPosicoes)
      .catch(() => setPosicoes([]));
  }, [salaId, success]);

  async function handleCriar(e: FormEvent) {
    e.preventDefault();
    if (!salaId) return;
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await posicoesApi.criar({
        salaId,
        identificador,
        tipo: tipo || undefined,
        coordX: coordX ? Number(coordX) : undefined,
        coordY: coordY ? Number(coordY) : undefined,
      });
      setSuccess(`Posição "${identificador}" criada com sucesso`);
      setIdentificador('');
      setTipo('');
      setCoordX('');
      setCoordY('');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao criar posição');
    } finally {
      setLoading(false);
    }
  }

  async function handleInativar(id: string) {
    try {
      await posicoesApi.inativar(id);
      setPosicoes((prev) =>
        prev.map((p) => (p.id === id ? { ...p, status: 'INATIVA' as const } : p)),
      );
      setSuccess('Posição inativada');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao inativar');
    }
  }

  async function handleReativar(id: string) {
    try {
      const reativada = await posicoesApi.reativar(id);
      setPosicoes((prev) =>
        prev.map((p) => (p.id === id ? reativada : p)),
      );
      setSuccess('Posição reativada');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao reativar');
    }
  }

  return (
    <div>
      <PageHeader
        title="Posições"
        subtitle="Cadastre assentos vinculados ao layout aprovado da sala"
      />
      <Alert message={error} />
      <Alert type="success" message={success} />

      <div className="card form-card">
        <label>
          Sala
          <select value={salaId} onChange={(e) => setSalaId(e.target.value)}>
            {salas.map((s) => (
              <option key={s.id} value={s.id}>{s.nome}</option>
            ))}
          </select>
        </label>
      </div>

      <div className="detail-grid mt-lg">
        <div className="card">
          <h3>Nova posição</h3>
          <form onSubmit={handleCriar} className="form">
            <label>
              Identificador *
              <input value={identificador} onChange={(e) => setIdentificador(e.target.value)} required placeholder="A1" />
            </label>
            <label>
              Tipo
              <input value={tipo} onChange={(e) => setTipo(e.target.value)} placeholder="INDIVIDUAL" />
            </label>
            <div className="form-grid">
              <label>
                Coord X
                <input type="number" step="0.1" value={coordX} onChange={(e) => setCoordX(e.target.value)} />
              </label>
              <label>
                Coord Y
                <input type="number" step="0.1" value={coordY} onChange={(e) => setCoordY(e.target.value)} />
              </label>
            </div>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Criando...' : 'Criar posição'}
            </button>
          </form>
        </div>

        <div className="card">
          <h3>Posições ({posicoes.length})</h3>
          {posicoes.length === 0 ? (
            <EmptyState title="Nenhuma posição" description="A sala precisa de um layout aprovado." />
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Tipo</th>
                    <th>Coords</th>
                    <th>Status</th>
                    <th>Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {posicoes.map((p) => (
                    <tr key={p.id}>
                      <td><strong>{p.identificador}</strong></td>
                      <td>{p.tipo ?? '—'}</td>
                      <td>{p.coordX ?? '—'}, {p.coordY ?? '—'}</td>
                      <td><StatusBadge status={p.status} /></td>
                      <td>
                        {p.status === 'ATIVA' ? (
                          <button type="button" className="btn btn-sm btn-danger" onClick={() => handleInativar(p.id)}>
                            Inativar
                          </button>
                        ) : (
                          <button type="button" className="btn btn-sm btn-primary" onClick={() => handleReativar(p.id)}>
                            Reativar
                          </button>
                        )}
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
