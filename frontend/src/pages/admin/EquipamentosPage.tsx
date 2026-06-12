import type { FormEvent } from 'react';
import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { posicoesApi } from '../../api/posicoes';
import { salasApi } from '../../api/salas';
import { tiposEquipamentoApi } from '../../api/tipos-equipamento';
import { Alert, EmptyState, PageHeader, StatusBadge } from '../../components/ui';
import type {
  PosicaoEquipamentoResponse,
  PosicaoResponse,
  SalaResponse,
  TipoEquipamentoResponse,
} from '../../types';

export function EquipamentosPage() {
  const [searchParams] = useSearchParams();
  const [tipos, setTipos] = useState<TipoEquipamentoResponse[]>([]);
  const [salas, setSalas] = useState<SalaResponse[]>([]);
  const [salaId, setSalaId] = useState(searchParams.get('salaId') ?? '');
  const [posicoes, setPosicoes] = useState<PosicaoResponse[]>([]);
  const [posicaoId, setPosicaoId] = useState('');
  const [equipamentosPosicao, setEquipamentosPosicao] = useState<PosicaoEquipamentoResponse[]>([]);

  const [nome, setNome] = useState('');
  const [descricao, setDescricao] = useState('');
  const [tipoEquipamentoId, setTipoEquipamentoId] = useState('');
  const [quantidade, setQuantidade] = useState('1');
  const [observacao, setObservacao] = useState('');

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const posicoesAtivas = posicoes.filter((p) => p.status === 'ATIVA');
  const totalEquipamentos = tipos.length;
  const totalAtivos = tipos.filter((t) => t.ativo).length;

  useEffect(() => {
    tiposEquipamentoApi.listar().then(setTipos).catch(() => setTipos([]));
    salasApi.listar().then((data) => {
      setSalas(data);
      const paramId = searchParams.get('salaId');
      if (paramId && data.some((s) => s.id === paramId)) {
        setSalaId(paramId);
      } else if (data.length > 0 && !salaId) {
        setSalaId(data[0].id);
      }
    }).catch(() => setSalas([]));
  }, [searchParams, success]);

  useEffect(() => {
    if (!salaId) return;
    posicoesApi.listarPorSala(salaId).then((data) => {
      setPosicoes(data);
      const ativas = data.filter((p) => p.status === 'ATIVA');
      if (ativas.length > 0) {
        setPosicaoId(ativas[0].id);
      } else {
        setPosicaoId('');
        setEquipamentosPosicao([]);
      }
    }).catch(() => {
      setPosicoes([]);
      setPosicaoId('');
    });
  }, [salaId, success]);

  useEffect(() => {
    if (!posicaoId) {
      setEquipamentosPosicao([]);
      return;
    }
    tiposEquipamentoApi
      .listarPorPosicao(posicaoId)
      .then(setEquipamentosPosicao)
      .catch(() => setEquipamentosPosicao([]));
  }, [posicaoId, success]);

  async function handleCriarTipo(e: FormEvent) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await tiposEquipamentoApi.criar({ nome, descricao: descricao || undefined });
      setSuccess(`Tipo de equipamento "${nome}" criado com sucesso`);
      setNome('');
      setDescricao('');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao criar equipamento');
    } finally {
      setLoading(false);
    }
  }

  async function handleVincular(e: FormEvent) {
    e.preventDefault();
    if (!posicaoId || !tipoEquipamentoId) return;
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      await tiposEquipamentoApi.vincularPosicao(posicaoId, {
        tipoEquipamentoId,
        quantidade: Number(quantidade),
        observacao: observacao || undefined,
      });
      const posicao = posicoes.find((p) => p.id === posicaoId);
      setSuccess(`Equipamento atribuído à posição ${posicao?.identificador ?? ''}`);
      setTipoEquipamentoId('');
      setQuantidade('1');
      setObservacao('');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao vincular equipamento');
    } finally {
      setLoading(false);
    }
  }

  async function handleInativarTipo(id: string) {
    try {
      await tiposEquipamentoApi.inativar(id);
      setSuccess('Tipo de equipamento inativado');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao inativar');
    }
  }

  async function handleAtivarTipo(id: string) {
    try {
      await tiposEquipamentoApi.ativar(id);
      setSuccess('Tipo de equipamento reativado');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao reativar');
    }
  }

  return (
    <div>
      <PageHeader
        title="Equipamentos"
        subtitle="Gerencie tipos de equipamento e atribua-os às posições das salas"
      />
      <Alert message={error} />
      <Alert type="success" message={success} />

      <div className="stats-grid">
        <div className="stat-card">
          <span className="stat-value">{totalEquipamentos}</span>
          <span className="stat-label">Tipos cadastrados</span>
        </div>
        <div className="stat-card">
          <span className="stat-value">{totalAtivos}</span>
          <span className="stat-label">Tipos ativos</span>
        </div>
        <div className="stat-card">
          <span className="stat-value">{posicoesAtivas.length}</span>
          <span className="stat-label">Posições ativas (sala)</span>
        </div>
        <div className="stat-card">
          <span className="stat-value">{equipamentosPosicao.length}</span>
          <span className="stat-label">Equipamentos na posição</span>
        </div>
      </div>

      <div className="detail-grid mt-lg">
        <div className="card">
          <h3>Novo tipo de equipamento</h3>
          <form onSubmit={handleCriarTipo} className="form">
            <label>
              Nome *
              <input value={nome} onChange={(e) => setNome(e.target.value)} required placeholder='Monitor 27"' />
            </label>
            <label>
              Descrição
              <textarea value={descricao} onChange={(e) => setDescricao(e.target.value)} rows={2} />
            </label>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? 'Criando...' : 'Criar equipamento'}
            </button>
          </form>
        </div>

        <div className="card">
          <h3>Atribuir à posição</h3>
          <form onSubmit={handleVincular} className="form">
            <label>
              Sala
              <select value={salaId} onChange={(e) => setSalaId(e.target.value)}>
                {salas.map((s) => (
                  <option key={s.id} value={s.id}>{s.nome}</option>
                ))}
              </select>
            </label>
            <label>
              Posição
              <select
                value={posicaoId}
                onChange={(e) => setPosicaoId(e.target.value)}
                disabled={posicoesAtivas.length === 0}
              >
                {posicoesAtivas.length === 0 ? (
                  <option value="">Nenhuma posição ativa</option>
                ) : (
                  posicoesAtivas.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.identificador}{p.tipo ? ` (${p.tipo})` : ''}
                    </option>
                  ))
                )}
              </select>
            </label>
            <label>
              Tipo de equipamento *
              <select
                value={tipoEquipamentoId}
                onChange={(e) => setTipoEquipamentoId(e.target.value)}
                required
              >
                <option value="">Selecione...</option>
                {tipos.filter((t) => t.ativo).map((t) => (
                  <option key={t.id} value={t.id}>{t.nome}</option>
                ))}
              </select>
            </label>
            <label>
              Quantidade *
              <input
                type="number"
                min={1}
                value={quantidade}
                onChange={(e) => setQuantidade(e.target.value)}
                required
              />
            </label>
            <label>
              Observação
              <input value={observacao} onChange={(e) => setObservacao(e.target.value)} />
            </label>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading || !posicaoId || !tipoEquipamentoId}
            >
              {loading ? 'Atribuindo...' : 'Atribuir equipamento'}
            </button>
          </form>
        </div>
      </div>

      <div className="detail-grid mt-lg">
        <div className="card">
          <h3>Tipos de equipamento ({tipos.length})</h3>
          {tipos.length === 0 ? (
            <EmptyState title="Nenhum equipamento" description="Cadastre o primeiro tipo de equipamento." />
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Nome</th>
                    <th>Descrição</th>
                    <th>Status</th>
                    <th>Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {tipos.map((t) => (
                    <tr key={t.id}>
                      <td><strong>{t.nome}</strong></td>
                      <td>{t.descricao ?? '—'}</td>
                      <td><StatusBadge status={t.ativo ? 'ATIVA' : 'INATIVA'} /></td>
                      <td>
                        {t.ativo ? (
                          <button
                            type="button"
                            className="btn btn-sm btn-danger"
                            onClick={() => handleInativarTipo(t.id)}
                          >
                            Inativar
                          </button>
                        ) : (
                          <button
                            type="button"
                            className="btn btn-sm btn-primary"
                            onClick={() => handleAtivarTipo(t.id)}
                          >
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

        <div className="card">
          <h3>
            Equipamentos da posição
            {posicaoId && posicoes.find((p) => p.id === posicaoId) && (
              <small className="muted block">
                {posicoes.find((p) => p.id === posicaoId)?.identificador}
                {' — '}
                {salas.find((s) => s.id === salaId)?.nome}
              </small>
            )}
          </h3>
          {equipamentosPosicao.length === 0 ? (
            <EmptyState title="Nenhum equipamento" description="Atribua equipamentos à posição selecionada." />
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Equipamento</th>
                    <th>Qtd</th>
                    <th>Observação</th>
                  </tr>
                </thead>
                <tbody>
                  {equipamentosPosicao.map((eq) => (
                    <tr key={eq.id}>
                      <td>{eq.tipoEquipamentoNome}</td>
                      <td>{eq.quantidade}</td>
                      <td>{eq.observacao ?? '—'}</td>
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
