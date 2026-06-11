import { useEffect, useState } from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ROLE_LABELS: Record<string, string> = {
  ADMIN_SALA: 'Admin Sala',
  GESTOR_RESERVAS: 'Gestor Reservas',
  USUARIO_FINAL: 'Usuário Final',
  INTEGRADOR: 'Integrador',
};

interface NavItem {
  to: string;
  label: string;
  icon: string;
  end?: boolean;
}

interface NavSection {
  title: string;
  items: NavItem[];
}

function NavLinkItem({ to, label, icon, end, onNavigate }: NavItem & { onNavigate?: () => void }) {
  return (
    <NavLink to={to} end={end} onClick={onNavigate}>
      <span className="nav-icon" aria-hidden="true">{icon}</span>
      {label}
    </NavLink>
  );
}

export function AppLayout() {
  const { email, roles, logout, hasAnyRole } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  useEffect(() => {
    setSidebarOpen(false);
  }, [location.pathname]);

  useEffect(() => {
    if (!sidebarOpen) return;
    function handleKey(e: KeyboardEvent) {
      if (e.key === 'Escape') setSidebarOpen(false);
    }
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [sidebarOpen]);

  const sections: NavSection[] = [];

  sections.push({
    title: 'Geral',
    items: [{ to: '/', label: 'Dashboard', icon: '◉', end: true }],
  });

  if (hasAnyRole(['ADMIN_SALA'])) {
    sections.push({
      title: 'Administração',
      items: [
        { to: '/salas', label: 'Salas', icon: '▣' },
        { to: '/layouts', label: 'Layouts', icon: '▦' },
        { to: '/posicoes', label: 'Posições', icon: '▤' },
        { to: '/equipamentos', label: 'Equipamentos', icon: '⚙' },
        { to: '/regras-disponibilidade', label: 'Regras', icon: '📋' },
        { to: '/usuarios', label: 'Usuários', icon: '👤' },
        { to: '/ia-execucoes', label: 'IA Execuções', icon: '✦' },
      ],
    });
  }

  const reservaItems: NavItem[] = [];

  if (hasAnyRole(['USUARIO_FINAL', 'GESTOR_RESERVAS', 'INTEGRADOR'])) {
    reservaItems.push(
      { to: '/reservas/nova', label: 'Nova Reserva', icon: '＋' },
      { to: '/reservas', label: 'Minhas Reservas', icon: '📅' },
    );
  }

  if (hasAnyRole(['ADMIN_SALA'])) {
    reservaItems.push({ to: '/admin/reservas', label: 'Todas Reservas', icon: '📋' });
  }

  if (hasAnyRole(['ADMIN_SALA', 'USUARIO_FINAL', 'GESTOR_RESERVAS', 'INTEGRADOR'])) {
    reservaItems.push({ to: '/disponibilidade', label: 'Disponibilidade', icon: '◎' });
  }

  if (hasAnyRole(['GESTOR_RESERVAS'])) {
    reservaItems.push({ to: '/reservas/gestao', label: 'Gestão', icon: '✓' });
  }

  if (reservaItems.length > 0) {
    sections.push({ title: 'Reservas', items: reservaItems });
  }

  if (hasAnyRole(['GESTOR_RESERVAS', 'ADMIN_SALA'])) {
    sections.push({
      title: 'Equipes',
      items: [{ to: '/equipes', label: 'Gerenciar Equipes', icon: '👥' }],
    });
  }

  if (hasAnyRole(['USUARIO_FINAL'])) {
    sections.push({
      title: 'Equipes',
      items: [{ to: '/minhas-equipes', label: 'Minhas Equipes', icon: '👥' }],
    });
  }

  async function handleLogout() {
    await logout();
    navigate('/login');
  }

  const closeSidebar = () => setSidebarOpen(false);

  return (
    <div className="app-shell">
      <div
        className={`sidebar-overlay ${sidebarOpen ? 'visible' : ''}`}
        onClick={closeSidebar}
        aria-hidden="true"
      />

      <header className="mobile-header">
        <button
          type="button"
          className="menu-toggle"
          onClick={() => setSidebarOpen((v) => !v)}
          aria-label={sidebarOpen ? 'Fechar menu' : 'Abrir menu'}
          aria-expanded={sidebarOpen}
        >
          {sidebarOpen ? '✕' : '☰'}
        </button>
        <div className="brand">
          <span className="brand-icon">OH</span>
          <strong>OfficeHub</strong>
        </div>
      </header>

      <aside className={`sidebar ${sidebarOpen ? 'open' : ''}`}>
        <div className="brand">
          <span className="brand-icon">OH</span>
          <div>
            <strong>OfficeHub</strong>
            <small>Gestão de salas</small>
          </div>
        </div>

        <nav className="nav" aria-label="Navegação principal">
          {sections.map((section) => (
            <div key={section.title} className="nav-section">
              <span className="nav-section-label">{section.title}</span>
              {section.items.map((item) => (
                <NavLinkItem key={item.to} {...item} onNavigate={closeSidebar} />
              ))}
            </div>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <strong>{email}</strong>
            <small>{roles.map((r) => ROLE_LABELS[r] ?? r).join(', ')}</small>
          </div>
          <button type="button" className="btn btn-ghost btn-block" onClick={handleLogout}>
            Sair
          </button>
        </div>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}
