import type { ReactNode } from "react";

export type UserRole = "admin" | "manager" | "employee";

export interface SessionUser {
  name: string;
  role: UserRole;
  avatar?: string;
}

export type RoomStatus = "available" | "unavailable" | "occupied" | "reserved";

export interface Room {
  id: number;
  name: string;
  capacity: number;
  desks: number;
  status: RoomStatus;
  equipment: string[];
  floor: string;
  area: number;
  occupiedDesks: number;
}

export type ReservationStatus = "confirmed" | "active" | "cancelled";

export interface Reservation {
  id: number;
  room: string;
  user: string;
  requesterName?: string;
  requesterRole?: "admin" | "manager" | "employee";
  reservationGroupId?: string | null;
  seatCode?: string;
  seatType?: string;
  requestedEquipment?: string[];
  date: string;
  start: string;
  end: string;
  status: ReservationStatus;
}

export interface CreateReservationPayload {
  roomId: number;
  requesterName: string;
  user: string;
  requesterRole: "admin" | "manager" | "employee";
  seatCode: string;
  seatType: string;
  requestedEquipment: string[];
  date: string;
  start: string;
  end: string;
}

export interface RoomPosition {
  code: string;
  type: string;
  availableEquipment: string[];
  available: boolean;
}

export type DirectoryUserStatus = "active" | "inactive";

export interface DirectoryUser {
  id: number;
  name: string;
  email: string;
  login: string;
  role: UserRole;
  status: DirectoryUserStatus;
}

export interface NotificationItem {
  id: number;
  type: "reservation_confirmed" | "reservation_cancelled" | string;
  text: string;
  color: string;
  read: boolean;
  createdAt: string;
  reservationId: number | null;
  reservationGroupId: string | null;
}

export type PageId =
  | "dashboard"
  | "rooms"
  | "reservations"
  | "history"
  | "notifications"
  | "users"
  | "api";

export interface NavItem {
  id: PageId;
  label: string;
  icon: string;
  roles: UserRole[];
  badge?: string;
}

/** Status strings used by StatusBadge across rooms, reservations, users */
export type BadgeStatus =
  | RoomStatus
  | ReservationStatus
  | DirectoryUserStatus
  | string;

export interface ModalProps {
  open: boolean;
  onClose: () => void;
  title: string;
  subtitle?: string;
  wide?: boolean;
  children?: ReactNode;
}
