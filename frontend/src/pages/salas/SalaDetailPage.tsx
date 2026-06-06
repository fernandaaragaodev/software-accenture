import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { regrasDisponibilidadeApi } from '../../api/regras-disponibilidade';
import { salasApi } from '../../api/salas';
import { Alert, PageHeader, StatusBadge } from '../../components/ui';
import type { RegraDisponibilidadeResponse, SalaResponse } from '../../types';

const DIAS_SEMANA = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo'];

export function SalaDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [sala, setSala] = useState<SalaResponse | null>(null);
  const [regra, setRegra] = useState<RegraDisponibilidadeResponse | null>(null);
  const [regrasDisponiveis, setRegrasDisponiveis] = useState<RegraDisponibilidadeResponse[]>([]);
  const [regraSelecionada, setRegraSelecionada] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);

  function carregarDados() {
    if (!id) return;
    Promise.all([
      salasApi.obter(id),
      salasApi.listarRegrasDisponibilidade(id).catch(() => null),
      regrasDisponibilidadeApi.listar().catch(() => []),
    ])
      .then(([salaData, regraData, todasRegras]) => {
        setSala(salaData);
        setRegra(regraData);
        setRegrasDisponiveis(todasRegras.filter((r) => !r.salaId));
      })
      .catch((err) => setError(err instanceof ApiException ? err.message : 'Erro ao carregar sala'))
      .finally(() => setLoading(false));
  }

  useEffect(() => {
    carregarDados();
  }, [id]);

  async function handleStatusChange(status: 'ATIVA' | 'INATIVA' | 'MANUTENCAO') {
    if (!id) return;
    setError('');
    try {
      const updated = await salasApi.atualizarStatus(id, { status });
      setSala(updated);
      setSuccess(`Status atualizado para ${status}`);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao atualizar status');
    }
  }

  async function handleAtribuirRegra() {
    if (!id || !regraSelecionada) return;
    setError('');
    setSuccess('');
    try {
      const atribuida = await salasApi.atribuirRegra(id, { regraId: regraSelecionada });
      setRegra(atribuida);
      setRegraSelecionada('');
      setSuccess('Regra atribuída à sala');
      carregarDados();
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao atribuir regra');
    }
  }

  async function handleDesatribuirRegra() {
    if (!id) return;
    setError('');
    setSuccess('');
    try {
      await salasApi.desatribuirRegra(id);
      setRegra(null);
      setSuccess('Regra desatribuída da sala');
      carregarDados();
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao desatribuir regra');
    }
  }

  if (loading) {
    return <div className="page-center"><div className="spinner" /></div>;
  }

  if (!sala) {
    return <Alert message={error || 'Sala não encontrada'} />;
  }

  return (
    <div>
      <PageHeader
        title={sala.nome}
        subtitle={sala.descricao}
        action={<Link to="/salas" className="btn btn-ghost">Voltar</Link>}
      />

      <Alert message={error} />
      <Alert type="success" message={success} />

      <div className="detail-grid">
        <div className="card">
          <h3>Informações</h3>
          <dl className="detail-list">
            <div><dt>Status</dt><dd><StatusBadge status={sala.status} /></dd></div>
            <div><dt>Capacidade</dt><dd>{sala.capacidadeMaxima}</dd></div>
            <div><dt>Bloco / Andar</dt><dd>{sala.bloco ?? '—'} / {sala.andar ?? '—'}</dd></div>
            <div><dt>Raio proximidade</dt><dd>{sala.raioProximidade ?? '—'}</dd></div>
          </dl>

          <div className="btn-group mt-md">
            <button type="button" className="btn btn-sm btn-ghost" onClick={() => handleStatusChange('ATIVA')}>
              Ativar
            </button>
            <button type="button" className="btn btn-sm btn-ghost" onClick={() => handleStatusChange('MANUTENCAO')}>
              Manutenção
            </button>
            <button type="button" className="btn btn-sm btn-danger" onClick={() => handleStatusChange('INATIVA')}>
              Inativar
            </button>
          </div>
        </div>

        <div className="card">
          <h3>Regra de disponibilidade</h3>
          {regra ? (
            <>
              <p><strong>{regra.nome}</strong></p>
              <p>Antecedência mínima: <strong>{regra.antecedenciaMinimaDias} dia(s)</strong></p>
              <ul className="simple-list">
                {regra.horarios.map((h) => (
                  <li key={h.id ?? h.diaSemana}>
                    {DIAS_SEMANA[h.diaSemana]}: {h.horaAbertura.slice(0, 5)} – {h.horaFechamento.slice(0, 5)}
                  </li>
                ))}
              </ul>
              <button type="button" className="btn btn-sm btn-ghost mt-md" onClick={handleDesatribuirRegra}>
                Desatribuir regra
              </button>
            </>
          ) : (
            <>
              <p className="muted">Nenhuma regra atribuída a esta sala.</p>
              <div className="form inline-form mt-md">
                <label>
                  Selecionar regra
                  <select value={regraSelecionada} onChange={(e) => setRegraSelecionada(e.target.value)}>
                    <option value="">Selecione...</option>
                    {regrasDisponiveis.map((r) => (
                      <option key={r.id} value={r.id}>{r.nome}</option>
                    ))}
                  </select>
                </label>
                <button
                  type="button"
                  className="btn btn-primary"
                  disabled={!regraSelecionada}
                  onClick={handleAtribuirRegra}
                >
                  Atribuir
                </button>
              </div>
              <p className="muted mt-md">
                <Link to="/regras-disponibilidade">Gerenciar regras de disponibilidade</Link>
              </p>
            </>
          )}
        </div>
      </div>
    </div>
  );
}
