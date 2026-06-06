import type { ReactNode } from 'react';

interface AlertProps {
  type?: 'error' | 'success' | 'info';
  message: string;
}

export function Alert({ type = 'error', message }: AlertProps) {
  if (!message) return null;
  return <div className={`alert alert-${type}`}>{message}</div>;
}

interface PageHeaderProps {
  title: string;
  subtitle?: string;
  action?: ReactNode;
}

export function PageHeader({ title, subtitle, action }: PageHeaderProps) {
  return (
    <div className="page-header">
      <div>
        <h1>{title}</h1>
        {subtitle && <p className="subtitle">{subtitle}</p>}
      </div>
      {action}
    </div>
  );
}

interface StatusBadgeProps {
  status: string;
}

const STATUS_CLASS: Record<string, string> = {
  ATIVA: 'success',
  INATIVA: 'muted',
  MANUTENCAO: 'warning',
  PENDENTE: 'warning',
  CONFIRMADA: 'success',
  REJEITADA: 'danger',
  CANCELADA: 'muted',
  LIVRE: 'success',
  OCUPADA: 'danger',
};

export function StatusBadge({ status }: StatusBadgeProps) {
  return <span className={`badge badge-${STATUS_CLASS[status] ?? 'muted'}`}>{status}</span>;
}

interface EmptyStateProps {
  title: string;
  description?: string;
  action?: ReactNode;
}

export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <div className="empty-state">
      <h3>{title}</h3>
      {description && <p>{description}</p>}
      {action}
    </div>
  );
}
