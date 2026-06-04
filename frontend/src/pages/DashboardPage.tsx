import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { notificacoesApi } from '../api/notificacoes.api';
import { posicoesApi } from '../api/posicoes.api';
import { salasApi } from '../api/salas.api';
import { Button } from '../components/ui/Button';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { Loading } from '../components/ui/Loading';
import { useAuth } from '../hooks/useAuth';
import { getApiErrorMessage } from '../utils/apiError';
import {
  canCreateReserva,
  canManageSalas,
  canConsultarDisponibilidade,
  canViewNotificacoes,
  getStoredReservaIds,
} from '../utils/permissions';

interface Stats {
  salas: number;
  posicoes: number;
  reservas: number;
  notificacoes: number;
}

export function DashboardPage() {
  const { roles, user } = useAuth();
  const [stats, setStats] = useState<Stats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      setError(null);
      try {
        let salasCount = 0;
        let posicoesCount = 0;
        let notificacoesCount = 0;

        if (canManageSalas(roles) || canConsultarDisponibilidade(roles)) {
          const { data: salas } = await salasApi.listar();
          salasCount = salas.length;
          if (canManageSalas(roles)) {
            const counts = await Promise.all(
              salas.map((s) =>
                posicoesApi.listarPorSala(s.id).then((r) => r.data.length).catch(() => 0),
              ),
            );
            posicoesCount = counts.reduce((a, b) => a + b, 0);
          }
        }

        if (canViewNotificacoes(roles) && user?.usuarioId) {
          const { data } = await notificacoesApi.listarPorUsuario(user.usuarioId);
          notificacoesCount = data.length;
        }

        setStats({
          salas: salasCount,
          posicoes: posicoesCount,
          reservas: getStoredReservaIds().length,
          notificacoes: notificacoesCount,
        });
      } catch (err) {
        setError(getApiErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };
    void load();
  }, [roles, user?.usuarioId]);

  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} />;

  const cards = [
    { label: 'Salas', value: stats?.salas ?? 0, color: 'bg-blue-500' },
    { label: 'Reservas (recentes)', value: stats?.reservas ?? 0, color: 'bg-violet-500' },
    { label: 'Posições', value: stats?.posicoes ?? 0, color: 'bg-emerald-500' },
    { label: 'Notificações', value: stats?.notificacoes ?? 0, color: 'bg-amber-500' },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-slate-900">Dashboard</h1>
        <p className="text-sm text-slate-500">Visão geral do sistema OfficeHub</p>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        {cards.map((card) => (
          <div
            key={card.label}
            className="rounded-xl border border-border bg-white p-5 shadow-sm"
          >
            <div className={`mb-3 inline-flex rounded-lg ${card.color} px-2 py-1 text-xs font-medium text-white`}>
              {card.label}
            </div>
            <p className="text-3xl font-bold text-slate-900">{card.value}</p>
          </div>
        ))}
      </div>

      <div className="rounded-xl border border-border bg-white p-5 shadow-sm">
        <h2 className="mb-4 text-lg font-semibold text-slate-800">Atalhos</h2>
        <div className="flex flex-wrap gap-3">
          {canCreateReserva(roles) && (
            <Link to="/reservas/nova">
              <Button>Nova reserva</Button>
            </Link>
          )}
          {canManageSalas(roles) && (
            <Link to="/salas/nova">
              <Button variant="secondary">Cadastrar sala</Button>
            </Link>
          )}
          {canConsultarDisponibilidade(roles) && (
            <Link to="/disponibilidade">
              <Button variant="secondary">Consultar disponibilidade</Button>
            </Link>
          )}
          <Link to="/reservas">
            <Button variant="ghost">Ver reservas</Button>
          </Link>
        </div>
      </div>
    </div>
  );
}
