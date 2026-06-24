import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiException } from '../api/client';
import { relatoriosApi } from '../api/relatorios';
import { useAuth } from '../context/AuthContext';
import type { DashboardStatsResponse } from '../types';
import { Alert, ROLE_LABELS, SkeletonGrid } from '../components/ui';

export function DashboardPage() {
  const { email, roles, hasAnyRole } = useAuth();
  const [stats, setStats] = useState<DashboardStatsResponse | null>(null);
  const [error, setError] = useState('');
  const [loadingStats, setLoadingStats] = useState(false);

  const podeVerStats = hasAnyRole(['ADMIN_SALA', 'GESTOR_RESERVAS']);

  useEffect(() => {
    if (!podeVerStats) return;
    setLoadingStats(true);
    relatoriosApi
      .obterDashboard()
      .then(setStats)
      .catch((err) =>
        setError(err instanceof ApiException ? err.message : 'Erro ao carregar estatísticas'),
      )
      .finally(() => setLoadingStats(false));
  }, [podeVerStats]);

  const primeiroNome = (email ?? 'usuário').split('@')[0].split('.')[0];

  return (
    <div>
      <div className="welcome-banner">
        <h2>Olá, {primeiroNome}!</h2>
        <p>Bem-vindo ao Accendesk — gerencie salas, equipes e reservas em um só lugar.</p>
      </div>

      <Alert message={error} />

      {podeVerStats && (
        <>
          <h2 className="section-title">Estatísticas do sistema</h2>
          {loadingStats ? (
            <SkeletonGrid count={6} variant="stat" />
          ) : stats ? (
            <div className="stats-grid">
              <div className="stat-card">
                <span className="stat-value">{stats.totalSalas}</span>
                <span className="stat-label">Salas</span>
              </div>
              <div className="stat-card">
                <span className="stat-value">{stats.totalPosicoesAtivas}</span>
                <span className="stat-label">Posições ativas</span>
              </div>
              <div className="stat-card">
                <span className="stat-value">{stats.totalTiposEquipamentoAtivos}</span>
                <span className="stat-label">Tipos de equipamento</span>
              </div>
              <div className="stat-card">
                <span className="stat-value">{stats.totalEquipamentosVinculados}</span>
                <span className="stat-label">Equipamentos vinculados</span>
              </div>
              <div className="stat-card">
                <span className="stat-value">{stats.totalUsuarios}</span>
                <span className="stat-label">Usuários</span>
              </div>
              <div className="stat-card">
                <span className="stat-value">{stats.totalEquipes}</span>
                <span className="stat-label">Equipes</span>
              </div>
              <div className="stat-card">
                <span className="stat-value">{stats.totalReservas}</span>
                <span className="stat-label">Reservas totais</span>
              </div>
              <div className="stat-card success">
                <span className="stat-value">{stats.reservasConfirmadas}</span>
                <span className="stat-label">Confirmadas</span>
              </div>
              <div className="stat-card">
                <span className="stat-value">{stats.reservasPendentes}</span>
                <span className="stat-label">Pendentes</span>
              </div>
              <div className="stat-card danger">
                <span className="stat-value">{stats.reservasCanceladas + stats.reservasRejeitadas}</span>
                <span className="stat-label">Canceladas / Rejeitadas</span>
              </div>
            </div>
          ) : null}
        </>
      )}

      <h2 className="section-title mt-lg">Acesso rápido</h2>
      <div className="dashboard-grid">
        {hasAnyRole(['ADMIN_SALA']) && (
          <>
            <Link to="/salas" className="dashboard-card">
              <h3>Salas</h3>
              <p>Criar e gerenciar salas, status e regras de disponibilidade.</p>
            </Link>
            <Link to="/layouts" className="dashboard-card">
              <h3>Layouts</h3>
              <p>Criar layouts e aprovar versões ativas das salas.</p>
            </Link>
            <Link to="/posicoes" className="dashboard-card">
              <h3>Posições</h3>
              <p>Definir assentos, coordenadas e tipos de posição.</p>
            </Link>
            <Link to="/equipamentos" className="dashboard-card">
              <h3>Equipamentos</h3>
              <p>Cadastrar equipamentos e atribuí-los às posições.</p>
            </Link>
            <Link to="/usuarios" className="dashboard-card">
              <h3>Usuários</h3>
              <p>Cadastrar novos usuários com perfis no sistema.</p>
            </Link>
          </>
        )}

        {hasAnyRole(['GESTOR_RESERVAS', 'ADMIN_SALA']) && (
          <Link to="/equipes" className="dashboard-card">
            <h3>Equipes</h3>
            <p>Criar equipes e gerenciar gestores e membros.</p>
          </Link>
        )}

        {hasAnyRole(['USUARIO_FINAL', 'GESTOR_RESERVAS', 'INTEGRADOR']) && (
          <Link to="/reservas/nova" className="dashboard-card">
            <h3>Nova Reserva</h3>
            <p>Solicitar reserva com alocação automática de posições.</p>
          </Link>
        )}

        {hasAnyRole(['ADMIN_SALA', 'USUARIO_FINAL', 'GESTOR_RESERVAS', 'INTEGRADOR']) && (
          <Link to="/disponibilidade" className="dashboard-card">
            <h3>Disponibilidade</h3>
            <p>Consultar posições livres e ocupadas por data.</p>
          </Link>
        )}

        {hasAnyRole(['GESTOR_RESERVAS']) && (
          <Link to="/reservas/gestao" className="dashboard-card">
            <h3>Gestão de Reservas</h3>
            <p>Confirmar, rejeitar ou cancelar reservas pendentes.</p>
          </Link>
        )}
      </div>

      <div className="card mt-lg">
        <h3>Seus perfis</h3>
        <div className="tag-list">
          {roles.map((role) => (
            <span key={role} className="tag">
              {ROLE_LABELS[role] ?? role}
            </span>
          ))}
        </div>
      </div>
    </div>
  );
}
