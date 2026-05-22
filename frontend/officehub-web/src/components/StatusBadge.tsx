import type { BadgeStatus } from "../types/officehub";

const MAP: Record<
  string,
  { label: string; cls: string }
> = {
  available: { label: "Disponível", cls: "badge-green" },
  unavailable: { label: "Indisponível", cls: "badge-red" },
  occupied: { label: "Ocupada", cls: "badge-red" },
  reserved: { label: "Reservada", cls: "badge-amber" },
  confirmed: { label: "Confirmada", cls: "badge-green" },
  active: { label: "Em andamento", cls: "badge-blue" },
  cancelled: { label: "Cancelada", cls: "badge-red" },
  inactive: { label: "Inativo", cls: "badge-red" },
};

export function StatusBadge({ status }: { status: BadgeStatus }) {
  const m = MAP[status] ?? { label: String(status), cls: "badge-blue" };
  return <span className={`badge ${m.cls}`}>{m.label}</span>;
}
