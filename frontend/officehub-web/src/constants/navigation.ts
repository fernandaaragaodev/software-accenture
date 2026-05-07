import type { NavItem, PageId, UserRole } from "../types/officehub";

export const NAV: NavItem[] = [
  {
    id: "dashboard",
    label: "Painel",
    icon: "🏠",
    roles: ["admin", "manager", "employee"],
  },
  {
    id: "rooms",
    label: "Salas",
    icon: "🏢",
    roles: ["admin", "manager", "employee"],
  },
  {
    id: "reservations",
    label: "Reservas",
    icon: "📅",
    roles: ["admin", "manager", "employee"],
    badge: "3",
  },
  {
    id: "history",
    label: "Histórico",
    icon: "📋",
    roles: ["admin", "manager", "employee"],
  },
  {
    id: "notifications",
    label: "Notificações",
    icon: "🔔",
    roles: ["admin", "manager", "employee"],
    badge: "2",
  },
  {
    id: "users",
    label: "Usuários",
    icon: "👥",
    roles: ["admin"],
  },
  {
    id: "api",
    label: "API & Integrações",
    icon: "🔗",
    roles: ["admin"],
  },
];

export const PAGE_TITLES: Record<PageId, string> = {
  dashboard: "Painel geral",
  rooms: "Salas e Escritórios",
  reservations: "Reservas",
  history: "Histórico",
  notifications: "Notificações",
  users: "Usuários",
  api: "API & Integrações",
};

export function filterNavForRole(
  items: NavItem[],
  role: UserRole | undefined,
): NavItem[] {
  if (!role) return items;
  return items.filter((n) => n.roles.includes(role));
}
