import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import type { Role } from '../../types/auth.types';
import { hasAnyRole } from '../../utils/permissions';

interface RoleGuardProps {
  allowed: Role[];
  children: ReactNode;
  fallbackTo?: string;
}

export function RoleGuard({ allowed, children, fallbackTo = '/dashboard' }: RoleGuardProps) {
  const { roles } = useAuth();

  if (!hasAnyRole(roles, allowed)) {
    return <Navigate to={fallbackTo} replace />;
  }

  return <>{children}</>;
}
