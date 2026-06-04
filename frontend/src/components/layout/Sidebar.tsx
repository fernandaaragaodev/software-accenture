import { NavLink } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import {
  canConsultarDisponibilidade,
  canCreateReserva,
  canManageSalas,
  canViewNotificacoes,
  canManageReservasGestor,
} from '../../utils/permissions';

const linkClass = ({ isActive }: { isActive: boolean }) =>
  `flex items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium transition ${
    isActive
      ? 'bg-primary-600 text-white'
      : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900'
  }`;

interface SidebarProps {
  mobileOpen: boolean;
  onClose: () => void;
}

export function Sidebar({ mobileOpen, onClose }: SidebarProps) {
  const { roles } = useAuth();

  const nav = (
    <nav className="flex flex-col gap-1 p-4">
      <NavLink to="/dashboard" className={linkClass} onClick={onClose}>
        Dashboard
      </NavLink>
      {canManageSalas(roles) && (
        <>
          <NavLink to="/salas" className={linkClass} onClick={onClose}>
            Salas
          </NavLink>
          <NavLink to="/posicoes" className={linkClass} onClick={onClose}>
            Posições
          </NavLink>
          <NavLink to="/equipamentos" className={linkClass} onClick={onClose}>
            Equipamentos
          </NavLink>
        </>
      )}
      {(canCreateReserva(roles) || canManageReservasGestor(roles)) && (
        <NavLink to="/reservas" className={linkClass} onClick={onClose}>
          Reservas
        </NavLink>
      )}
      {canCreateReserva(roles) && (
        <NavLink to="/reservas/nova" className={linkClass} onClick={onClose}>
          Nova reserva
        </NavLink>
      )}
      {canConsultarDisponibilidade(roles) && (
        <NavLink to="/disponibilidade" className={linkClass} onClick={onClose}>
          Disponibilidade
        </NavLink>
      )}
      {canViewNotificacoes(roles) && (
        <NavLink to="/notificacoes" className={linkClass} onClick={onClose}>
          Notificações
        </NavLink>
      )}
    </nav>
  );

  return (
    <>
      {mobileOpen && (
        <div
          className="fixed inset-0 z-40 bg-slate-900/40 lg:hidden"
          onClick={onClose}
          aria-hidden
        />
      )}
      <aside
        className={`fixed inset-y-0 left-0 z-50 w-64 transform border-r border-border bg-white transition lg:static lg:translate-x-0 ${
          mobileOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <div className="flex h-16 items-center border-b border-border px-4">
          <span className="text-lg font-bold text-primary-700">OfficeHub</span>
          <span className="ml-1 text-xs text-slate-500">SGSP</span>
        </div>
        {nav}
      </aside>
    </>
  );
}
