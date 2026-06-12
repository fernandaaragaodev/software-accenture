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

interface ReadinessBadgeProps {
  readiness: 'pronta' | 'pendente' | 'indisponivel';
}

const READINESS_CLASS: Record<ReadinessBadgeProps['readiness'], string> = {
  pronta: 'success',
  pendente: 'warning',
  indisponivel: 'danger',
};

const READINESS_LABEL: Record<ReadinessBadgeProps['readiness'], string> = {
  pronta: 'Pronta para Reservas',
  pendente: 'Configuração Pendente',
  indisponivel: 'Indisponível',
};

export function ReadinessBadge({ readiness }: ReadinessBadgeProps) {
  return (
    <span className={`badge badge-${READINESS_CLASS[readiness]}`}>
      {READINESS_LABEL[readiness]}
    </span>
  );
}

interface FieldErrorProps {
  message?: string;
}

export function FieldError({ message }: FieldErrorProps) {
  if (!message) return null;
  return <small className="field-error">{message}</small>;
}
