import { useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { reservasApi } from '../api/reservas.api';
import { salasApi } from '../api/salas.api';
import { Badge, statusReservaBadgeVariant } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { Loading } from '../components/ui/Loading';
import { Modal } from '../components/ui/Modal';
import { Textarea } from '../components/ui/Textarea';
import { useAuth } from '../hooks/useAuth';
import type { Reserva } from '../types/reserva.types';
import type { Sala } from '../types/sala.types';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate, statusReservaLabel } from '../utils/formatters';
import {
  canCancelReserva,
  canManageReservasGestor,
  canViewReservaDetalheApi,
} from '../utils/permissions';

export function ReservaDetalhePage() {
  const { id } = useParams<{ id: string }>();
  const location = useLocation();
  const { roles } = useAuth();
  const cached = (location.state as { reserva?: Reserva })?.reserva;

  const [reserva, setReserva] = useState<Reserva | null>(cached ?? null);
  const [sala, setSala] = useState<Sala | null>(null);
  const [loading, setLoading] = useState(!cached);
  const [error, setError] = useState<string | null>(null);
  const [motivo, setMotivo] = useState('');
  const [modal, setModal] = useState<'rejeitar' | 'cancelar' | null>(null);

  useEffect(() => {
    if (!id) return;
    if (canViewReservaDetalheApi(roles)) {
      reservasApi
        .buscar(id)
        .then(({ data }) => setReserva(data))
        .catch((err) => setError(getApiErrorMessage(err)))
        .finally(() => setLoading(false));
    } else if (cached) {
      setLoading(false);
    } else {
      setError(
        'Detalhes disponíveis apenas para gestores via API. Acesse a partir da confirmação da reserva.',
      );
      setLoading(false);
    }
  }, [id, roles, cached]);

  useEffect(() => {
    if (!reserva) return;
    salasApi
      .buscar(reserva.salaId)
      .then(({ data }) => setSala(data))
      .catch(() => setSala(null));
  }, [reserva]);

  const confirmar = async () => {
    if (!id) return;
    try {
      const { data } = await reservasApi.confirmar(id);
      setReserva(data);
    } catch (err) {
      alert(getApiErrorMessage(err));
    }
  };

  const rejeitar = async () => {
    if (!id || !motivo.trim()) return;
    try {
      const { data } = await reservasApi.rejeitar(id, { motivo });
      setReserva(data);
      setModal(null);
    } catch (err) {
      alert(getApiErrorMessage(err));
    }
  };

  const cancelar = async () => {
    if (!id || !motivo.trim()) return;
    try {
      const { data } = await reservasApi.cancelar(id, { motivo });
      setReserva(data);
      setModal(null);
    } catch (err) {
      alert(getApiErrorMessage(err));
    }
  };

  if (loading) return <Loading />;
  if (error || !reserva) return <ErrorMessage message={error ?? 'Reserva não encontrada.'} />;

  const gestor = canManageReservasGestor(roles);
  const podeCancelar = canCancelReserva(roles);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-900">Detalhe da reserva</h1>
        <Link to="/reservas">
          <Button variant="ghost">Voltar</Button>
        </Link>
      </div>

      <div className="rounded-xl border border-border bg-white p-6 shadow-sm">
        <div className="mb-4 flex items-center gap-2">
          <Badge variant={statusReservaBadgeVariant(reserva.status)}>
            {statusReservaLabel(reserva.status)}
          </Badge>
        </div>

        <dl className="grid gap-3 sm:grid-cols-2">
          <div>
            <dt className="text-xs text-slate-500">Sala</dt>
            <dd className="font-medium">{sala?.nome ?? reserva.salaId}</dd>
          </div>
          <div>
            <dt className="text-xs text-slate-500">Data</dt>
            <dd className="font-medium">{formatDate(reserva.dataReserva)}</dd>
          </div>
          <div>
            <dt className="text-xs text-slate-500">Pessoas</dt>
            <dd className="font-medium">{reserva.quantidadePessoas}</dd>
          </div>
          <div>
            <dt className="text-xs text-slate-500">Proximidade</dt>
            <dd className="font-medium">{reserva.criterioProximidade}</dd>
          </div>
        </dl>

        {reserva.motivoRejeicao && (
          <p className="mt-4 text-sm text-red-700">
            Motivo: {reserva.motivoRejeicao}
          </p>
        )}

        {reserva.alocacoes.length > 0 && (
          <div className="mt-4">
            <h2 className="font-semibold text-slate-800">Posições alocadas</h2>
            <ul className="mt-2 space-y-1 text-sm">
              {reserva.alocacoes.map((a) => (
                <li key={a.posicaoId}>
                  {a.posicaoIdentificador} — {a.posicaoTipo}
                </li>
              ))}
            </ul>
          </div>
        )}

        <div className="mt-6 flex flex-wrap gap-2">
          {gestor && reserva.status === 'PENDENTE' && (
            <>
              <Button onClick={() => void confirmar()}>Confirmar</Button>
              <Button variant="danger" onClick={() => setModal('rejeitar')}>
                Rejeitar
              </Button>
            </>
          )}
          {podeCancelar &&
            (reserva.status === 'PENDENTE' || reserva.status === 'CONFIRMADA') && (
              <Button variant="secondary" onClick={() => setModal('cancelar')}>
                Cancelar
              </Button>
            )}
        </div>
      </div>

      <Modal
        open={modal === 'rejeitar'}
        title="Rejeitar reserva"
        onClose={() => setModal(null)}
      >
        <Textarea
          label="Motivo"
          value={motivo}
          onChange={(e) => setMotivo(e.target.value)}
        />
        <Button className="mt-3" variant="danger" onClick={() => void rejeitar()}>
          Confirmar rejeição
        </Button>
      </Modal>

      <Modal
        open={modal === 'cancelar'}
        title="Cancelar reserva"
        onClose={() => setModal(null)}
      >
        <Textarea
          label="Motivo do cancelamento"
          value={motivo}
          onChange={(e) => setMotivo(e.target.value)}
        />
        <Button className="mt-3" variant="danger" onClick={() => void cancelar()}>
          Confirmar cancelamento
        </Button>
      </Modal>
    </div>
  );
}
