import type { FormEvent } from 'react';
import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { reservasApi } from '../../api/reservas';
import { Alert, PageHeader, StatusBadge } from '../../components/ui';
import { useAuth } from '../../context/AuthContext';
import { removeStoredReservationId } from '../../utils/auth';
import type { ReservaResponse } from '../../types';

export function ReservaDetailPage() {
  const { id } = useParams<{ id: string }>();
  const { hasRole } = useAuth();
  const [reserva, setReserva] = useState<ReservaResponse | null>(null);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [motivo, setMotivo] = useState('');
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);

  useEffect(() => {
    if (!id) return;
    reservasApi
      .obter(id)
      .then(setReserva)
      .catch((err) => setError(err instanceof ApiException ? err.message : 'Erro ao carregar reserva'))
      .finally(() => setLoading(false));
  }, [id]);

  async function handleCancelar(e: FormEvent) {
    e.preventDefault();
    if (!id) return;
    setActionLoading(true);
    setError('');
    try {
      const updated = await reservasApi.cancelar(id, { motivo });
      setReserva(updated);
      removeStoredReservationId(id);
      setSuccess('Reserva cancelada');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao cancelar');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleConfirmar() {
    if (!id) return;
    setActionLoading(true);
    try {
      const updated = await reservasApi.confirmar(id);
      setReserva(updated);
      setSuccess('Reserva confirmada');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao confirmar');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleRejeitar(e: FormEvent) {
    e.preventDefault();
    if (!id) return;
    setActionLoading(true);
    try {
      const updated = await reservasApi.rejeitar(id, { motivo });
      setReserva(updated);
      setSuccess('Reserva rejeitada');
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao rejeitar');
    } finally {
      setActionLoading(false);
    }
  }

  if (loading) {
    return <div className="page-center"><div className="spinner" /></div>;
  }

  if (!reserva) {
    return <Alert message={error || 'Reserva não encontrada'} />;
  }

  const canManage = hasRole('GESTOR_RESERVAS') && reserva.status === 'PENDENTE';
  const canCancel = reserva.status === 'CONFIRMADA' || reserva.status === 'PENDENTE';

  return (
    <div>
      <PageHeader
        title={`Reserva ${reserva.id.slice(0, 8)}...`}
        action={<Link to="/reservas" className="btn btn-ghost">Voltar</Link>}
      />
      <Alert message={error} />
      <Alert type="success" message={success} />

      <div className="card">
        <dl className="detail-list">
          <div><dt>Status</dt><dd><StatusBadge status={reserva.status} /></dd></div>
          <div><dt>Data</dt><dd>{reserva.dataReserva}</dd></div>
          <div><dt>Pessoas</dt><dd>{reserva.quantidadePessoas}</dd></div>
          <div><dt>Proximidade</dt><dd>{reserva.criterioProximidade}</dd></div>
          <div><dt>Sala ID</dt><dd className="mono">{reserva.salaId}</dd></div>
          {reserva.avisoProximidade && (
            <div><dt>Aviso</dt><dd className="warning-text">{reserva.avisoProximidade}</dd></div>
          )}
          {reserva.motivoRejeicao && (
            <div><dt>Motivo rejeição</dt><dd>{reserva.motivoRejeicao}</dd></div>
          )}
        </dl>

        <h3 className="section-title">Alocações</h3>
        {reserva.alocacoes.length === 0 ? (
          <p className="muted">Nenhuma posição alocada.</p>
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Posição</th>
                  <th>Identificador</th>
                  <th>Tipo</th>
                </tr>
              </thead>
              <tbody>
                {reserva.alocacoes.map((a) => (
                  <tr key={a.posicaoId}>
                    <td className="mono">{a.posicaoId.slice(0, 8)}...</td>
                    <td><strong>{a.posicaoIdentificador}</strong></td>
                    <td>{a.posicaoTipo ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {canManage && (
        <div className="card mt-lg">
          <h3>Ações do gestor</h3>
          <div className="btn-group">
            <button type="button" className="btn btn-primary" disabled={actionLoading} onClick={handleConfirmar}>
              Confirmar
            </button>
          </div>
          <form onSubmit={handleRejeitar} className="form mt-md">
            <label>
              Motivo da rejeição
              <textarea value={motivo} onChange={(e) => setMotivo(e.target.value)} required rows={2} />
            </label>
            <button type="submit" className="btn btn-danger" disabled={actionLoading}>
              Rejeitar
            </button>
          </form>
        </div>
      )}

      {canCancel && !canManage && (
        <div className="card mt-lg">
          <h3>Cancelar reserva</h3>
          <form onSubmit={handleCancelar} className="form">
            <label>
              Motivo
              <textarea value={motivo} onChange={(e) => setMotivo(e.target.value)} required rows={2} />
            </label>
            <button type="submit" className="btn btn-danger" disabled={actionLoading}>
              Cancelar reserva
            </button>
          </form>
        </div>
      )}
    </div>
  );
}
