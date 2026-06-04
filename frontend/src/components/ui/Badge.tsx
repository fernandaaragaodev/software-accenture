type BadgeVariant = 'default' | 'success' | 'warning' | 'danger' | 'info';

interface BadgeProps {
  children: string;
  variant?: BadgeVariant;
}

const variants: Record<BadgeVariant, string> = {
  default: 'bg-slate-100 text-slate-700',
  success: 'bg-emerald-100 text-emerald-800',
  warning: 'bg-amber-100 text-amber-800',
  danger: 'bg-red-100 text-red-800',
  info: 'bg-blue-100 text-blue-800',
};

export function Badge({ children, variant = 'default' }: BadgeProps) {
  return (
    <span
      className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-medium ${variants[variant]}`}
    >
      {children}
    </span>
  );
}

export function statusSalaBadgeVariant(status: string): BadgeVariant {
  if (status === 'ATIVA') return 'success';
  if (status === 'MANUTENCAO') return 'warning';
  return 'default';
}

export function statusReservaBadgeVariant(status: string): BadgeVariant {
  if (status === 'CONFIRMADA') return 'success';
  if (status === 'PENDENTE') return 'info';
  if (status === 'REJEITADA') return 'danger';
  if (status === 'CANCELADA') return 'default';
  return 'default';
}
