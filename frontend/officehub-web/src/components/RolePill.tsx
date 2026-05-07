import type { UserRole } from "../types/officehub";

const MAP: Record<UserRole, [string, string]> = {
  admin: ["Administrador", "role-admin"],
  manager: ["Gestor", "role-manager"],
  employee: ["Funcionário", "role-employee"],
};

export function RolePill({ role }: { role: UserRole | string }) {
  const entry = MAP[role as UserRole];
  const [label, cls] = entry ?? [role, ""];
  return <span className={`role-pill ${cls}`}>{label}</span>;
}
