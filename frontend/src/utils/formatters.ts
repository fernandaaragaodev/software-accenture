const DIAS_SEMANA = [
  'Domingo',
  'Segunda-feira',
  'Terça-feira',
  'Quarta-feira',
  'Quinta-feira',
  'Sexta-feira',
  'Sábado',
];

export function formatDate(iso: string | null | undefined): string {
  if (!iso) return '—';
  const date = iso.includes('T') ? iso.slice(0, 10) : iso;
  const [y, m, d] = date.split('-');
  if (!y || !m || !d) return iso;
  return `${d}/${m}/${y}`;
}

export function formatDateTime(iso: string | null | undefined): string {
  if (!iso) return '—';
  try {
    return new Intl.DateTimeFormat('pt-BR', {
      dateStyle: 'short',
      timeStyle: 'short',
    }).format(new Date(iso));
  } catch {
    return iso;
  }
}

export function diaSemanaLabel(dia: number): string {
  return DIAS_SEMANA[dia] ?? `Dia ${dia}`;
}

export function statusSalaLabel(status: string): string {
  const map: Record<string, string> = {
    ATIVA: 'Ativa',
    INATIVA: 'Inativa',
    MANUTENCAO: 'Manutenção',
  };
  return map[status] ?? status;
}

export function statusReservaLabel(status: string): string {
  const map: Record<string, string> = {
    PENDENTE: 'Pendente',
    CONFIRMADA: 'Confirmada',
    REJEITADA: 'Rejeitada',
    CANCELADA: 'Cancelada',
  };
  return map[status] ?? status;
}
