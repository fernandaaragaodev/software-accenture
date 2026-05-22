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

export function isReservationDateAllowed(dateStr: string): boolean {
  return dateStr >= minReservationDateStr();
}

export function clampReservationDate(dateStr: string): string {
  const min = minReservationDateStr();
  return dateStr < min ? min : dateStr;
}

export function buildCalendarDays(
  yearMonth: { year: number; month: number },
  minDateStr = minReservationDateStr(),
): { day: number; iso: string; disabled: boolean }[] {
  const { year, month } = yearMonth;
  if (!year || !month) return [];
  const daysInMonth = new Date(year, month, 0).getDate();
  return Array.from({ length: daysInMonth }, (_, i) => {
    const day = i + 1;
    const iso = `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
    return { day, iso, disabled: iso < minDateStr };
  });
}

export function isRoomDeactivated(room: BookingRoom): boolean {
  return room.status === "unavailable";
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
  Projetor: "📽️",
  Videoconferencia: "📹",
  "TV 55\"": "📺",
  "TV 65\"": "📺",
  "Quadro Branco": "📋",
  Som: "🔊",
  "Mesa ergonômica": "🪑",
  "Monitor 27": "🖥️",
  "Mesa digitalizadora": "✏️",
  "PC Workstation": "💻",
};

/** Recursos exibidos nos filtros e cards (estável por sala). */
export const DEMO_POSITION_RESOURCES = [
  "Monitor 4K",
  "Cadeira Ergonômica",
  "Mouse Vertical",
  "Teclado Mecânico",
  "Ar-condicionado",
] as const;

function seededIndex(seed: number, max: number): number {
  const next = (seed * 1103515245 + 12345) & 0x7fffffff;
  return next % max;
}

/** Sorteia 2–4 recursos por sala; mesma sala sempre recebe o mesmo conjunto. */
export function randomizeRoomEquipments(roomId: number): string[] {
  const pool = [...DEMO_POSITION_RESOURCES];
  const count = 2 + (roomId % 3);
  const picked: string[] = [];
  let seed = roomId * 7919;

  while (picked.length < count && pool.length > 0) {
    const idx = seededIndex(seed, pool.length);
    seed += idx + 1;
    picked.push(pool.splice(idx, 1)[0]);
  }

  return picked;
}

/** Recursos por estação: subconjunto dos recursos da sala. */
export function randomizePositionEquipments(
  roomId: number,
  positionKey: string,
  roomEquipments: string[],
): string[] {
  if (!roomEquipments.length) return [];
  const pool = [...roomEquipments];
  const count = Math.max(1, Math.min(pool.length, 1 + (positionKey.length % pool.length)));
  const picked: string[] = [];
  let seed = roomId * 104729 + positionKey.split("").reduce((a, c) => a + c.charCodeAt(0), 0);

  while (picked.length < count && pool.length > 0) {
    const idx = seededIndex(seed, pool.length);
    seed += idx + 7;
    picked.push(pool.splice(idx, 1)[0]);
  }

  return picked;
}

export function resolveRoomEquipments(
  roomId: number,
  apiEquipment: string[] | undefined,
): string[] {
  const fromApi = (apiEquipment ?? []).filter(Boolean);
  const demo = randomizeRoomEquipments(roomId);
  return [...new Set([...demo, ...fromApi])];
}

export function getRoomIcon(name: string): string {
  const key = Object.keys(ROOM_ICONS).find((k) => name.includes(k));
  return key ? ROOM_ICONS[key] : "🏢";
}

export function getEquipIcon(equip: string): string {
  return EQUIP_ICONS[equip] ?? "✓";
}

/** Mapeia rótulos da UI para equipamentos reais do backend (sem alterar API). */
const EQUIPMENT_ALIASES: Record<string, string[]> = {
  "Monitor 4K": ["Monitor 27", "PC Workstation"],
  "Cadeira Ergonômica": ["Mesa ergonômica"],
  "Mouse Vertical": ["Mesa ergonômica"],
  "Teclado Mecânico": ["Mesa ergonômica", "PC Workstation"],
  "Ar-condicionado": ["Ar-condicionado"],
};

export interface BookingPosition {
  id: string;
  name: string;
  type: string;
  /** Reservada no horário consultado. */
  occupied: boolean;
  /** Bloqueada pelo administrador. */
  blocked: boolean;
  /** Recursos exibidos na UI (inclui rótulos de demonstração). */
  equipment: string[];
  /** Equipamentos reais da posição no backend (para criar reserva). */
  apiEquipment: string[];
}

export function isPositionSelectable(pos: BookingPosition): boolean {
  return !pos.blocked && !pos.occupied;
}

export function positionStatusLabel(pos: BookingPosition): string {
  if (pos.blocked) return "Bloqueada";
  if (pos.occupied) return "Ocupada";
  return "Livre";
}

export function isPositionCompatibleWithPreferences(
  _pos: BookingPosition,
  preferences: string[],
  roomEquipments: string[],
): boolean {
  if (preferences.length === 0) return true;
  return preferences.every((pref) => roomEquipments.includes(pref));
}

export function resolveRequestedEquipmentForApi(
  position: BookingPosition,
  preferences: string[],
): string[] {
  const api = position.apiEquipment;
  if (preferences.length === 0) {
    return api;
  }
  const resolved: string[] = [];
  for (const pref of preferences) {
    const direct = api.find((e) => e.toLowerCase() === pref.toLowerCase());
    if (direct) {
      resolved.push(direct);
      continue;
    }
    const aliases = EQUIPMENT_ALIASES[pref] ?? [];
    const aliasHit = api.find((e) =>
      aliases.some((a) => a.toLowerCase() === e.toLowerCase()),
    );
    if (aliasHit) {
      resolved.push(aliasHit);
    }
  }
  return [...new Set(resolved)];
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
  const equipments = resolveRoomEquipments(room.id, room.equipment);
  return {
    id: room.id,
    name: room.name,
    location: room.floor || "—",
    icon: getRoomIcon(room.name),
    equipments,
    positions: [],
    status: room.status,
    capacity: room.capacity,
    desks: room.desks,
  };
}

export function mapApiPositions(
  apiPositions: RoomPosition[],
  _roomId: number,
  roomEquipments: string[],
): BookingPosition[] {
  return apiPositions.map((p) => {
    const apiEquip = (p.availableEquipment ?? []).filter(Boolean);
    const equipment = [...new Set([...apiEquip, ...roomEquipments])];
    return {
      id: p.code,
      name: p.code,
      type: p.type || "standard",
      blocked: p.blocked,
      occupied: !p.available && !p.blocked,
      equipment,
      apiEquipment: apiEquip,
    };
  });
}

export function availableCount(room: BookingRoom): number {
  return room.positions.filter((p) => isPositionSelectable(p)).length;
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
