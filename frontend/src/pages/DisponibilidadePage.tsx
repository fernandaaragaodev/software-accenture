import type { FormEvent } from 'react';
import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiException } from '../api/client';
import { salasApi } from '../api/salas';
import { Alert, PageHeader } from '../components/ui';
import type { ConsultaDisponibilidadeResponse, SalaResponse } from '../types';

export function DisponibilidadePage() {
  const [salas, setSalas] = useState<SalaResponse[]>([]);
  const [salaId, setSalaId] = useState('');
  const [data, setData] = useState('');
  const [resultado, setResultado] = useState<ConsultaDisponibilidadeResponse | null>(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    salasApi
      .listar()
      .then((data) => {
        const ativas = data.filter((s) => s.status === 'ATIVA');
        setSalas(ativas);
        if (ativas.length > 0) setSalaId(ativas[0].id);
      })
      .catch(() => setSalas([]));
  }, []);

  async function handleConsultar(e: FormEvent) {
    e.preventDefault();
    if (!salaId || !data) return;
    setError('');
    setLoading(true);
    setResultado(null);
    try {
      const res = await salasApi.consultarDisponibilidade(salaId, data);
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
        subtitle="Consulte posições livres e ocupadas por data"
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
              </div>
              {resultado.disponivelParaReserva && (
                <Link to={`/reservas/nova?salaId=${salaId}&data=${data}`} className="btn btn-primary">
                  Reservar
                </Link>
              )}
            </div>
          </div>

          {resultado.layout.length > 0 && (
            <div className="card mt-lg">
              <h3>Mapa de posições</h3>
              <div className="layout-map">
                {resultado.layout.map((pos) => (
                  <div
                    key={pos.id}
                    className={`layout-seat layout-seat-${pos.situacao.toLowerCase()}`}
                    style={{
                      left: `${(pos.coordX ?? 0) * 4}px`,
                      top: `${(pos.coordY ?? 0) * 4}px`,
                    }}
                    title={`${pos.identificador} — ${pos.situacao}`}
                  >
                    {pos.identificador}
                  </div>
                ))}
              </div>
              <div className="legend mt-md">
                <span><i className="dot livre" /> Livre</span>
                <span><i className="dot ocupada" /> Ocupada</span>
                <span><i className="dot inativa" /> Inativa</span>
              </div>
            </div>
          )}

          {resultado.posicoesOcupadas.length > 0 && (
            <div className="card mt-lg">
              <h3>Posições ocupadas</h3>
              <ul className="simple-list">
                {resultado.posicoesOcupadas.map((p) => (
                  <li key={p.id}>
                    <strong>{p.identificador}</strong> — {p.tipo ?? 'Sem tipo'}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
