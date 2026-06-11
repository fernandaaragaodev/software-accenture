import { Link, Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { Role } from '../types';
import { LoadingState } from './ui';

interface ProtectedRouteProps {
  roles?: Role[];
}

export function ProtectedRoute({ roles }: ProtectedRouteProps) {
  const { isAuthenticated, isLoading, hasAnyRole } = useAuth();

  if (isLoading) {
    return <LoadingState message="Verificando sessão..." />;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (roles && !hasAnyRole(roles)) {
    return (
      <div className="access-denied">
        <div className="access-denied-icon" aria-hidden="true">🔒</div>
        <h2>Acesso negado</h2>
        <p>Você não possui permissão para acessar esta página.</p>
        <Link to="/" className="btn btn-primary mt-md">
          Voltar ao Dashboard
        </Link>
      </div>
    );
  }

  return <Outlet />;
}
