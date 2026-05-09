import type { ReactNode } from "react";

export type UserRole = "admin" | "manager" | "employee";

export interface SessionUser {
  name: string;
  role: UserRole;
  avatar?: string;
  /** JWT retornado por `POST /auth/login` (guardado em `sessionStorage` quando presente). */
  token?: string;
}

export type RoomStatus = "available" | "occupied" | "reserved";

export interface Room {
  id: number;
  name: string;
  capacity: number;
  desks: number;
  status: RoomStatus;
  equipment: string[];
  floor: string;
  area: number;
}

export type ReservationStatus = "confirmed" | "active" | "cancelled";

export interface Reservation {
  id: number;
  room: string;
  user: string;
  date: string;
  start: string;
  end: string;
  status: ReservationStatus;
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
  text: string;
  time: string;
  color: string;
  read: boolean;
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
