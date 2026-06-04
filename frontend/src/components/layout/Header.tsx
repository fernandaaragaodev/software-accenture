import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { logout as logoutService } from '../../services/authService';
import { Button } from '../ui/Button';

interface HeaderProps {
  onMenuClick: () => void;
}

export function Header({ onMenuClick }: HeaderProps) {
  const navigate = useNavigate();
  const { user } = useAuth();

  const handleLogout = async () => {
    await logoutService();
    navigate('/login');
  };

  return (
    <header className="sticky top-0 z-30 flex h-16 items-center justify-between border-b border-border bg-white px-4 shadow-sm lg:px-6">
      <button
        type="button"
        className="rounded-lg p-2 text-slate-600 hover:bg-slate-100 lg:hidden"
        onClick={onMenuClick}
        aria-label="Abrir menu"
      >
        <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
        </svg>
      </button>
      <div className="hidden flex-1 lg:block" />
      <div className="flex items-center gap-3">
        <div className="hidden text-right sm:block">
          <p className="text-sm font-medium text-slate-800">{user?.email}</p>
          <p className="text-xs text-slate-500">{user?.roles.join(', ')}</p>
        </div>
        <Button variant="secondary" size="sm" onClick={() => void handleLogout()}>
          Sair
        </Button>
      </div>
    </header>
  );
}
