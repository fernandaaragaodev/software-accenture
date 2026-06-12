import { useEffect, useState, type FormEvent, type ReactNode } from 'react';

interface AlertProps {
  type?: 'error' | 'success' | 'info';
  message: string;
}

export function Alert({ type = 'error', message }: AlertProps) {
  if (!message) return null;
  return (
    <div className={`alert alert-${type}`} role="alert">
      {message}
    </div>
  );
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

interface LoadingStateProps {
  message?: string;
}

export function LoadingState({ message = 'Carregando...' }: LoadingStateProps) {
  return (
    <div className="loading-state" role="status" aria-live="polite">
      <div className="spinner" aria-hidden="true" />
      <p>{message}</p>
    </div>
  );
}

interface PaginationProps {
  page: number;
  totalPages: number;
  totalElements: number;
  size: number;
  onPageChange: (page: number) => void;
}

export function Pagination({
  page,
  totalPages,
  totalElements,
  size,
  onPageChange,
}: PaginationProps) {
  if (totalElements === 0) return null;

  const inicio = page * size + 1;
  const fim = Math.min((page + 1) * size, totalElements);
  const paginaAtual = page + 1;

  return (
    <div className="pagination-bar">
      <p className="pagination-info">
        Mostrando {inicio}–{fim} de {totalElements} reservas
      </p>
      <div className="pagination-controls">
        <button
          type="button"
          className="btn btn-ghost btn-sm"
          disabled={page <= 0}
          onClick={() => onPageChange(page - 1)}
        >
          ← Anterior
        </button>
        <span className="pagination-status">
          Página {paginaAtual} de {Math.max(totalPages, 1)}
        </span>
        <button
          type="button"
          className="btn btn-ghost btn-sm"
          disabled={page >= totalPages - 1}
          onClick={() => onPageChange(page + 1)}
        >
          Próxima →
        </button>
      </div>
    </div>
  );
}

interface SkeletonGridProps {
  count?: number;
  variant?: 'stat' | 'card' | 'row';
}

export function SkeletonGrid({ count = 4, variant = 'stat' }: SkeletonGridProps) {
  const className =
    variant === 'card' ? 'skeleton skeleton-card' :
    variant === 'row' ? 'skeleton skeleton-row' :
    'skeleton skeleton-stat';

  return (
    <div className={variant === 'stat' ? 'stats-grid' : variant === 'card' ? 'dashboard-grid' : undefined}>
      {Array.from({ length: count }, (_, i) => (
        <div key={i} className={className} aria-hidden="true" />
      ))}
    </div>
  );
}

interface ConfirmDialogProps {
  open: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: 'danger' | 'primary';
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmDialog({
  open,
  title,
  message,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  variant = 'primary',
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  useEffect(() => {
    if (!open) return;
    function handleKey(e: KeyboardEvent) {
      if (e.key === 'Escape') onCancel();
    }
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [open, onCancel]);

  if (!open) return null;

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="confirm-title">
      <div className="modal">
        <h3 id="confirm-title">{title}</h3>
        <p>{message}</p>
        <div className="modal-actions">
          <button type="button" className="btn btn-ghost" onClick={onCancel}>
            {cancelLabel}
          </button>
          <button
            type="button"
            className={`btn ${variant === 'danger' ? 'btn-danger' : 'btn-primary'}`}
            onClick={onConfirm}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}

export const ROLE_LABELS: Record<string, string> = {
  ADMIN_SALA: 'Admin Sala',
  GESTOR_RESERVAS: 'Gestor de Reservas',
  USUARIO_FINAL: 'Usuário Final',
  INTEGRADOR: 'Integrador',
};

interface PasswordConfirmDialogProps {
  open: boolean;
  title: string;
  message: string;
  confirmLabel?: string;
  cancelLabel?: string;
  variant?: 'danger' | 'primary';
  loading?: boolean;
  error?: string;
  onConfirm: (senha: string) => void;
  onCancel: () => void;
}

export function PasswordConfirmDialog({
  open,
  title,
  message,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  variant = 'primary',
  loading = false,
  error = '',
  onConfirm,
  onCancel,
}: PasswordConfirmDialogProps) {
  const [senha, setSenha] = useState('');

  useEffect(() => {
    if (!open) {
      setSenha('');
    }
  }, [open]);

  useEffect(() => {
    if (!open) return;
    function handleKey(e: KeyboardEvent) {
      if (e.key === 'Escape') onCancel();
    }
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [open, onCancel]);

  if (!open) return null;

  function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!senha.trim() || loading) return;
    onConfirm(senha);
  }

  return (
    <div className="modal-overlay" role="dialog" aria-modal="true" aria-labelledby="password-confirm-title">
      <div className="modal">
        <h3 id="password-confirm-title">{title}</h3>
        <p>{message}</p>
        <form onSubmit={handleSubmit} className="form">
          <Alert message={error} />
          <label>
            Sua senha *
            <input
              type="password"
              value={senha}
              onChange={(e) => setSenha(e.target.value)}
              required
              autoFocus
              disabled={loading}
            />
          </label>
          <div className="modal-actions">
            <button type="button" className="btn btn-ghost" onClick={onCancel} disabled={loading}>
              {cancelLabel}
            </button>
            <button
              type="submit"
              className={`btn ${variant === 'danger' ? 'btn-danger' : 'btn-primary'}`}
              disabled={loading || !senha.trim()}
            >
              {loading ? 'Validando...' : confirmLabel}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
