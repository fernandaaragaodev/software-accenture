import { Fragment, type FormEvent } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { ApiException } from '../../api/client';
import { iaApi } from '../../api/ia';
import { Alert, EmptyState, PageHeader, StatusBadge } from '../../components/ui';
import type { AgenteExecucaoResponse, StatusAgente } from '../../types';

function formatarDataHora(value: string) {
  return new Intl.DateTimeFormat('pt-BR', {
    dateStyle: 'short',
    timeStyle: 'short',
  }).format(new Date(value));
}

function resumirPayload(payload: unknown) {
  if (!payload || typeof payload !== 'object') return '—';

  const data = payload as Record<string, unknown>;
  const pessoas = Array.isArray(data.pessoas) ? data.pessoas.length : undefined;
  const posicoes = Array.isArray(data.posicoesLivres) ? data.posicoesLivres.length : undefined;
  const sucesso = typeof data.sucesso === 'boolean' ? data.sucesso : undefined;
  const score = typeof data.scoreTotal === 'number' ? data.scoreTotal : undefined;
  const alocacoes = Array.isArray(data.alocacoes) ? data.alocacoes.length : undefined;

  const partes = [
    pessoas !== undefined ? `${pessoas} pessoa(s)` : null,
    posicoes !== undefined ? `${posicoes} posição(ões) livres` : null,
    sucesso !== undefined ? (sucesso ? 'sucesso' : 'falha') : null,
    score !== undefined ? `score ${score}` : null,
    alocacoes !== undefined ? `${alocacoes} alocação(ões)` : null,
  ].filter(Boolean);

  return partes.length > 0 ? partes.join(' • ') : 'Payload registrado';
}

function motorLabel(versaoModelo: string) {
  if (versaoModelo.includes('OPENROUTER')) return 'OpenRouter + Gemini Flash';
  if (versaoModelo.includes('ESPACIAL')) return 'Algoritmo espacial local';
  return versaoModelo;
}

export function IaExecucoesPage() {
  const hoje = useMemo(() => new Date().toISOString().slice(0, 10), []);
  const [execucoes, setExecucoes] = useState<AgenteExecucaoResponse[]>([]);
  const [status, setStatus] = useState<StatusAgente | ''>('');
  const [dataInicio, setDataInicio] = useState('');
  const [dataFim, setDataFim] = useState(hoje);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState<string | null>(null);

  async function carregar() {
    setLoading(true);
    setError('');
    try {
      const data = await iaApi.listarExecucoes({
        tipoAgente: 'ALOCACAO',
        status: status || undefined,
        dataInicio: dataInicio || undefined,
        dataFim: dataFim || undefined,
      });
      setExecucoes(data);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao carregar execuções da IA');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    carregar();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    carregar();
  }

  return (
    <div>
      <PageHeader
        title="Execuções da IA"
        subtitle="Histórico do agente de alocação: OpenRouter/Gemini Flash e fallback espacial local"
      />
      <Alert message={error} />

      <div className="card form-card">
        <form onSubmit={handleSubmit} className="form inline-form">
          <label>
            Status
            <select value={status} onChange={(e) => setStatus(e.target.value as StatusAgente | '')}>
              <option value="">Todos</option>
              <option value="SUCESSO">Sucesso</option>
              <option value="FALHA">Falha</option>
            </select>
          </label>
          <label>
            Data inicial
            <input type="date" value={dataInicio} onChange={(e) => setDataInicio(e.target.value)} />
          </label>
          <label>
            Data final
            <input type="date" value={dataFim} onChange={(e) => setDataFim(e.target.value)} />
          </label>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            Filtrar
          </button>
        </form>
      </div>

      {loading ? (
        <div className="page-center"><div className="spinner" /></div>
      ) : execucoes.length === 0 ? (
        <EmptyState
          title="Nenhuma execução encontrada"
          description="Crie uma reserva para gerar logs do agente de alocação."
        />
      ) : (
        <div className="table-wrap mt-lg">
          <table>
            <thead>
              <tr>
                <th>Data</th>
                <th>Status</th>
                <th>Motor</th>
                <th>Tempo</th>
                <th>Reserva</th>
                <th>Resumo</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {execucoes.map((execucao) => (
                <Fragment key={execucao.id}>
                  <tr>
                    <td>{formatarDataHora(execucao.executadoEm)}</td>
                    <td><StatusBadge status={execucao.status} /></td>
                    <td>{motorLabel(execucao.versaoModelo)}</td>
                    <td>{execucao.tempoProcessamentoMs ?? 0} ms</td>
                    <td className="mono">{execucao.referenciaId?.slice(0, 8) ?? '—'}</td>
                    <td>{resumirPayload(execucao.payloadSaida ?? execucao.payloadEntrada)}</td>
                    <td>
                      <button
                        type="button"
                        className="btn btn-sm btn-ghost"
                        onClick={() => setExpandedId(expandedId === execucao.id ? null : execucao.id)}
                      >
                        {expandedId === execucao.id ? 'Ocultar' : 'Ver JSON'}
                      </button>
                    </td>
                  </tr>
                  {expandedId === execucao.id && (
                    <tr>
                      <td colSpan={7}>
                        {execucao.erroMensagem && (
                          <p className="warning-text"><strong>Erro:</strong> {execucao.erroMensagem}</p>
                        )}
                        <div className="json-grid">
                          <div>
                            <strong>Entrada</strong>
                            <pre>{JSON.stringify(execucao.payloadEntrada, null, 2)}</pre>
                          </div>
                          <div>
                            <strong>Saída</strong>
                            <pre>{JSON.stringify(execucao.payloadSaida, null, 2)}</pre>
                          </div>
                        </div>
                      </td>
                    </tr>
                  )}
                </Fragment>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
