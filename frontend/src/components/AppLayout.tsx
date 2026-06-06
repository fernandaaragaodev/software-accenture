import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ROLE_LABELS: Record<string, string> = {
  ADMIN_SALA: 'Admin Sala',
  GESTOR_RESERVAS: 'Gestor Reservas',
  USUARIO_FINAL: 'Usuário Final',
  INTEGRADOR: 'Integrador',
};

export function AppLayout() {
  const { email, roles, logout, hasAnyRole } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate('/login');
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <span className="brand-icon">OH</span>
          <div>
            <strong>OfficeHub</strong>
            <small>Gestão de salas</small>
          </div>
        </div>

        <nav className="nav">
          <NavLink to="/" end>
            Dashboard
          </NavLink>

          {hasAnyRole(['ADMIN_SALA']) && (
            <>
              <NavLink to="/salas">Salas</NavLink>
              <NavLink to="/layouts">Layouts</NavLink>
              <NavLink to="/posicoes">Posições</NavLink>
              <NavLink to="/equipamentos">Equipamentos</NavLink>
              <NavLink to="/usuarios">Usuários</NavLink>
            </>
          )}

          {hasAnyRole(['GESTOR_RESERVAS', 'ADMIN_SALA']) && (
            <NavLink to="/equipes">Equipes</NavLink>
          )}

          {hasAnyRole(['USUARIO_FINAL', 'GESTOR_RESERVAS', 'INTEGRADOR']) && (
            <>
              <NavLink to="/reservas/nova">Nova Reserva</NavLink>
              <NavLink to="/reservas">Minhas Reservas</NavLink>
              <NavLink to="/disponibilidade">Disponibilidade</NavLink>
            </>
          )}

          {hasAnyRole(['GESTOR_RESERVAS']) && (
            <NavLink to="/reservas/gestao">Gestão de Reservas</NavLink>
          )}
        </nav>

        <div className="sidebar-footer">
          <div className="user-info">
            <strong>{email}</strong>
            <small>{roles.map((r) => ROLE_LABELS[r] ?? r).join(', ')}</small>
          </div>
          <button type="button" className="btn btn-ghost" onClick={handleLogout}>
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
