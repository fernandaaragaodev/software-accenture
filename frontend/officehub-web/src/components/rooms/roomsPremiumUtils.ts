import type { Room, RoomPosition } from "../../types/officehub";

export function minReservationDateStr(): string {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  d.setDate(d.getDate() + 7);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
}

export function calculateDuration(start: string, end: string): string {
  if (!start || !end) return "0min";
  const [sh, sm] = start.split(":").map(Number);
  const [eh, em] = end.split(":").map(Number);
  const startDate = new Date(0, 0, 0, sh, sm);
  const endDate = new Date(0, 0, 0, eh, em);
  let diff = endDate.getTime() - startDate.getTime();
  if (diff < 0) return "Horário inválido";
  const hours = Math.floor(diff / 1000 / 60 / 60);
  diff -= hours * 1000 * 60 * 60;
  const minutes = Math.floor(diff / 1000 / 60);
  return (hours > 0 ? `${hours}h ` : "") + `${minutes}min`;
}

const ROOM_ICONS: Record<string, string> = {
  Apolo: "🖥️",
  Hermes: "💼",
  Athena: "👥",
  Zeus: "👑",
  Cronos: "⏰",
  Poseidon: "🎤",
};

const EQUIP_ICONS: Record<string, string> = {
  "Monitor 4K": "🖥️",
  "Cadeira Ergonômica": "🪑",
  "Mouse Vertical": "🖱️",
  "Teclado Mecânico": "⌨️",
  "Ar-condicionado": "❄️",
};

export function getRoomIcon(name: string): string {
  const key = Object.keys(ROOM_ICONS).find((k) => name.includes(k));
  return key ? ROOM_ICONS[key] : "🏢";
}

export function getEquipIcon(equip: string): string {
  return EQUIP_ICONS[equip] ?? "✓";
}

export interface BookingPosition {
  id: string;
  name: string;
  type: string;
  occupied: boolean;
  equipment: string[];
}

export interface BookingRoom {
  id: number;
  name: string;
  location: string;
  icon: string;
  equipments: string[];
  positions: BookingPosition[];
  status: Room["status"];
  capacity: number;
  desks: number;
}

export function mapApiRoom(room: Room): BookingRoom {
  return {
    id: room.id,
    name: room.name,
    location: room.floor || "—",
    icon: getRoomIcon(room.name),
    equipments: room.equipment ?? [],
    positions: [],
    status: room.status,
    capacity: room.capacity,
    desks: room.desks,
  };
}

export function mapApiPositions(apiPositions: RoomPosition[]): BookingPosition[] {
  return apiPositions.map((p) => ({
    id: p.code,
    name: p.code,
    type: p.type || "standard",
    occupied: !p.available || p.blocked,
    equipment: p.availableEquipment ?? [],
  }));
}

export function availableCount(room: BookingRoom): number {
  return room.positions.filter((p) => !p.occupied).length;
}

export function roomAvailabilityClass(count: number): string {
  if (count === 0) return "rp-badge rp-badge--error";
  if (count < 3) return "rp-badge rp-badge--warn";
  return "rp-badge rp-badge--success";
}

export function formatFilterDateLabel(isoDate: string): string {
  const [y, m, d] = isoDate.split("-").map(Number);
  if (!y || !m || !d) return isoDate;
  return `${d} de ${new Date(y, m - 1, d).toLocaleString("pt-BR", { month: "long" })}, ${y}`;
}
