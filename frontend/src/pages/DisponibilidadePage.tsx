import type { FormEvent } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiException } from '../api/client';
import { salasApi } from '../api/salas';
import { tiposEquipamentoApi } from '../api/tipos-equipamento';
import { Alert, PageHeader } from '../components/ui';
import type { ConsultaDisponibilidadeResponse, SalaResponse, TipoEquipamentoResponse } from '../types';

function formatarEquipamentos(equipamentos: string[]) {
  if (equipamentos.length === 0) return 'Sem equipamento';
  return equipamentos.join(', ');
}

export function DisponibilidadePage() {
  const [salas, setSalas] = useState<SalaResponse[]>([]);
  const [tiposEquipamento, setTiposEquipamento] = useState<TipoEquipamentoResponse[]>([]);
  const [salaId, setSalaId] = useState('');
  const [data, setData] = useState('');
  const [horaInicio, setHoraInicio] = useState('08:00');
  const [horaFim, setHoraFim] = useState('18:00');
  const [filtroEquipamento, setFiltroEquipamento] = useState('');
  const [resultado, setResultado] = useState<ConsultaDisponibilidadeResponse | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    Promise.all([salasApi.listar(), tiposEquipamentoApi.listar()])
      .then(([salasData, tiposData]) => {
        const ativas = salasData.filter((s) => s.status === 'ATIVA');
        setSalas(ativas);
        setTiposEquipamento(tiposData.filter((t) => t.ativo));
        if (ativas.length > 0) setSalaId(ativas[0].id);
      })
      .catch(() => {
        setSalas([]);
        setTiposEquipamento([]);
      });
  }, []);

  const posicoesLivres = useMemo(() => {
    if (!resultado) return [];
    return resultado.posicoes.filter((p) => p.situacao === 'LIVRE');
  }, [resultado]);

  const posicoesFiltradas = useMemo(() => {
    if (!filtroEquipamento) return posicoesLivres;
    return posicoesLivres.filter((p) => p.equipamentos.includes(filtroEquipamento));
  }, [posicoesLivres, filtroEquipamento]);

  async function handleConsultar(e: FormEvent) {
    e.preventDefault();
    if (!salaId || !data) return;

    if (horaInicio >= horaFim) {
      setError('A hora de início deve ser anterior à hora de fim.');
      return;
    }

    setError('');
    setLoading(true);
    setResultado(null);
    setFiltroEquipamento('');
    try {
      const res = await salasApi.consultarDisponibilidade(salaId, data, horaInicio, horaFim);
      setResultado(res);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao consultar disponibilidade');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <PageHeader
        title="Disponibilidade"
        subtitle="Consulte posições livres por sala, data e horário"
      />

      <div className="card form-card">
        <form onSubmit={handleConsultar} className="form">
          <Alert message={error} />
          <div className="form-grid">
            <label>
              Sala *
              {salas.length > 0 ? (
                <select value={salaId} onChange={(e) => setSalaId(e.target.value)} required>
                  {salas.map((s) => (
                    <option key={s.id} value={s.id}>{s.nome}</option>
                  ))}
                </select>
              ) : (
                <p className="muted">Nenhuma sala ativa disponível.</p>
              )}
            </label>
            <label>
              Data *
              <input type="date" value={data} onChange={(e) => setData(e.target.value)} required />
            </label>
            <label>
              Hora início *
              <input
                type="time"
                value={horaInicio}
                onChange={(e) => setHoraInicio(e.target.value)}
                required
              />
            </label>
            <label>
              Hora fim *
              <input
                type="time"
                value={horaFim}
                onChange={(e) => setHoraFim(e.target.value)}
                required
              />
            </label>
          </div>
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Consultando...' : 'Consultar'}
          </button>
        </form>
      </div>

      {resultado && (
        <div className="mt-lg">
          <div className="stats-grid">
            <div className="stat-card">
              <span>Total</span>
              <strong>{resultado.totalPosicoes}</strong>
            </div>
            <div className="stat-card success">
              <span>Livres</span>
              <strong>{resultado.totalLivres}</strong>
            </div>
            <div className="stat-card danger">
              <span>Ocupadas</span>
              <strong>{resultado.totalOcupadas}</strong>
            </div>
            <div className="stat-card muted">
              <span>Inativas</span>
              <strong>{resultado.totalInativas}</strong>
            </div>
          </div>

          <div className="card mt-lg">
            <div className="flex-between">
              <div>
                <h3>{resultado.disponivelParaReserva ? 'Disponível para reserva' : 'Indisponível'}</h3>
                <p className="muted">{resultado.mensagemRegras}</p>
                <p className="muted">
                  Horário consultado: {resultado.horaInicio?.slice(0, 5)} – {resultado.horaFim?.slice(0, 5)}
                </p>
              </div>
              {resultado.disponivelParaReserva && (
                <Link to={`/reservas/nova?salaId=${salaId}&data=${data}`} className="btn btn-primary">
                  Reservar
                </Link>
              )}
            </div>
          </div>

          <div className="card mt-lg">
            <div className="flex-between">
              <h3>Posições livres ({posicoesFiltradas.length})</h3>
              <label className="filter-inline">
                Filtrar por equipamento
                <select
                  value={filtroEquipamento}
                  onChange={(e) => setFiltroEquipamento(e.target.value)}
                >
                  <option value="">Todos</option>
                  {tiposEquipamento.map((t) => (
                    <option key={t.id} value={t.nome}>{t.nome}</option>
                  ))}
                </select>
              </label>
            </div>

            {posicoesFiltradas.length === 0 ? (
              <p className="muted mt-md">
                {filtroEquipamento
                  ? `Nenhuma posição livre com equipamento "${filtroEquipamento}" no horário informado.`
                  : 'Nenhuma posição livre no horário informado.'}
              </p>
            ) : (
              <div className="position-scroll-list">
                {posicoesFiltradas.map((pos) => (
                  <div key={pos.id} className="position-scroll-item">
                    <strong>{pos.identificador}</strong>
                    <span className="muted"> — {formatarEquipamentos(pos.equipamentos)}</span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
