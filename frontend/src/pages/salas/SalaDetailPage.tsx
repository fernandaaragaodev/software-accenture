import type { FormEvent } from 'react';
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { salasApi } from '../../api/salas';
import { Alert, PageHeader, StatusBadge } from '../../components/ui';
import type { RegraDisponibilidadeResponse, SalaResponse } from '../../types';

const DIAS_SEMANA = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo'];

export function SalaDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [sala, setSala] = useState<SalaResponse | null>(null);
  const [regra, setRegra] = useState<RegraDisponibilidadeResponse | null>(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(true);

  const [antecedencia, setAntecedencia] = useState('1');
  const [horarios, setHorarios] = useState([
    { diaSemana: 0, horaAbertura: '08:00:00', horaFechamento: '18:00:00' },
    { diaSemana: 1, horaAbertura: '08:00:00', horaFechamento: '18:00:00' },
    { diaSemana: 2, horaAbertura: '08:00:00', horaFechamento: '18:00:00' },
    { diaSemana: 3, horaAbertura: '08:00:00', horaFechamento: '18:00:00' },
    { diaSemana: 4, horaAbertura: '08:00:00', horaFechamento: '18:00:00' },
  ]);

  useEffect(() => {
    if (!id) return;
    Promise.all([
      salasApi.obter(id),
      salasApi.listarRegrasDisponibilidade(id).catch(() => null),
    ])
      .then(([salaData, regraData]) => {
        setSala(salaData);
        if (regraData) {
          setRegra(regraData);
          setAntecedencia(String(regraData.antecedenciaMinimaDias));
          setHorarios(regraData.horarios.map((h) => ({
            diaSemana: h.diaSemana,
            horaAbertura: h.horaAbertura,
            horaFechamento: h.horaFechamento,
          })));
        }
      })
      .catch((err) => setError(err instanceof ApiException ? err.message : 'Erro ao carregar sala'))
      .finally(() => setLoading(false));
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

  async function handleRegraSubmit(e: FormEvent) {
    e.preventDefault();
    if (!id || regra) return;
    setError('');
    setSuccess('');
    try {
      const created = await salasApi.criarRegraDisponibilidade(id, {
        antecedenciaMinimaDias: Number(antecedencia),
        horarios,
      });
      setRegra(created);
      setSuccess('Regra de disponibilidade criada com sucesso');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao criar regra');
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
              <p>Antecedência mínima: <strong>{regra.antecedenciaMinimaDias} dia(s)</strong></p>
              <ul className="simple-list">
                {regra.horarios.map((h) => (
                  <li key={h.id ?? h.diaSemana}>
                    {DIAS_SEMANA[h.diaSemana]}: {h.horaAbertura.slice(0, 5)} – {h.horaFechamento.slice(0, 5)}
                  </li>
                ))}
              </ul>
            </>
          ) : (
            <form onSubmit={handleRegraSubmit} className="form">
              <label>
                Antecedência mínima (dias)
                <input
                  type="number"
                  min={0}
                  value={antecedencia}
                  onChange={(e) => setAntecedencia(e.target.value)}
                  required
                />
              </label>
              <p className="muted">Horários padrão: Seg–Sex 08:00–18:00</p>
              <button type="submit" className="btn btn-primary">Criar regra</button>
            </form>
          )}
        </div>
      </div>
    </div>
  );
}
