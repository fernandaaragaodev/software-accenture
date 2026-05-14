import { useEffect, useMemo, useState } from "react";
import { Modal } from "../components/Modal";
import type { NotificationItem, SessionUser } from "../types/officehub";
import {
  fetchNotifications,
  markAllNotificationsAsRead,
  markNotificationAsRead,
} from "../services/notificationsService";
import {
  fetchReservationGroup,
  type ReservationGroup,
} from "../services/reservationGroupsService";

interface NotificationsPageProps {
  user: SessionUser;
  onNotificationsChanged?: (items: NotificationItem[]) => void;
}

function formatRelativeTime(isoDate: string): string {
  const createdAt = new Date(isoDate);
  const diffMs = Date.now() - createdAt.getTime();
  const diffMinutes = Math.max(0, Math.floor(diffMs / 60000));

  if (diffMinutes < 1) return "Agora mesmo";
  if (diffMinutes < 60) return `${diffMinutes} min atrás`;
  const diffHours = Math.floor(diffMinutes / 60);
  if (diffHours < 24) return `${diffHours} hora${diffHours > 1 ? "s" : ""} atrás`;
  const diffDays = Math.floor(diffHours / 24);
  return `${diffDays} dia${diffDays > 1 ? "s" : ""} atrás`;
}

export function NotificationsPage({ user, onNotificationsChanged }: NotificationsPageProps) {
  const [notifs, setNotifs] = useState<NotificationItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [groupDetails, setGroupDetails] = useState<ReservationGroup | null>(null);
  const [groupLoading, setGroupLoading] = useState(false);

  async function loadNotifications() {
    setLoading(true);
    try {
      const data = await fetchNotifications(user);
      setNotifs(data);
      onNotificationsChanged?.(data);
      setError(null);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Falha ao carregar notificacoes do backend.",
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadNotifications();
    }, 0);

    const polling = window.setInterval(() => {
      void loadNotifications();
    }, 15000);

    return () => {
      window.clearTimeout(timer);
      window.clearInterval(polling);
    };
  }, [user]);

  const renderedNotifs = useMemo(
    () =>
      notifs.map((notification) => ({
        ...notification,
        timeLabel: formatRelativeTime(notification.createdAt),
      })),
    [notifs],
  );

  async function markAllRead() {
    await markAllNotificationsAsRead(user);
    await loadNotifications();
  }

  async function markOneRead(item: NotificationItem) {
    if (item.read) return;
    await markNotificationAsRead(item.id, user);
    await loadNotifications();
  }

  async function openGroupDetails(groupId: string) {
    setGroupLoading(true);
    try {
      const data = await fetchReservationGroup(groupId);
      setGroupDetails(data);
    } finally {
      setGroupLoading(false);
    }
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-24">
        <div>
          <div
            style={{ fontFamily: "var(--font-head)", fontSize: 18, fontWeight: 700 }}
          >
            Notificações
          </div>
          <div style={{ fontSize: 13, color: "var(--text3)", marginTop: 3 }}>
            {user.role === "admin"
              ? "RF15 — Todas as notificações do sistema"
              : "RF15 — Apenas alertas das suas próprias ações"}
          </div>
        </div>
        <button
          type="button"
          className="btn btn-ghost btn-sm"
        onClick={() => void markAllRead()}
        disabled={loading || notifs.length === 0}
        >
          Marcar todas como lidas
        </button>
      </div>
    {error ? (
      <div
        style={{
          marginBottom: 12,
          fontSize: 12,
          color: "var(--red)",
          background: "rgba(255,77,109,0.12)",
          border: "1px solid rgba(255,77,109,0.25)",
          borderRadius: 8,
          padding: "8px 12px",
        }}
      >
        {error}
      </div>
    ) : null}
      <div className="card">
      {loading && renderedNotifs.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">⏳</div>
          <div className="empty-text">Carregando notificações...</div>
        </div>
      ) : null}
      {!loading && renderedNotifs.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">🔔</div>
          <div className="empty-text">Nenhuma notificação no momento</div>
        </div>
      ) : null}
      {renderedNotifs.map((n) => (
          <div
            key={n.id}
            className="notif-item"
            style={{ opacity: n.read ? 0.55 : 1 }}
            onClick={() => void markOneRead(n)}
          >
            <span
              className="notif-dot-big"
              style={{ background: n.color }}
            />
            <div className="notif-content">
              <div className="notif-text">{n.text}</div>
              <div className="notif-time">
                {n.timeLabel}{" "}
                {!n.read ? (
                  <span style={{ color: "var(--accent)", fontWeight: 600 }}>
                    · Nova
                  </span>
                ) : null}
                {n.reservationGroupId ? (
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm"
                    style={{ marginLeft: 10 }}
                    onClick={(e) => {
                      e.stopPropagation();
                      void openGroupDetails(n.reservationGroupId!);
                    }}
                    disabled={groupLoading}
                  >
                    {groupLoading ? "Abrindo..." : "Ver detalhes"}
                  </button>
                ) : null}
              </div>
            </div>
          </div>
        ))}
    </div>
      <Modal
        open={!!groupDetails}
        onClose={() => setGroupDetails(null)}
        title="Detalhes da reserva em lote"
        subtitle={
          groupDetails
            ? `${groupDetails.room} · ${groupDetails.date} (${groupDetails.start}-${groupDetails.end})`
            : undefined
        }
        wide
      >
        {groupDetails ? (
          <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
            {groupDetails.members.map((m) => (
              <div
                key={m.reservationId}
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  gap: 12,
                  alignItems: "center",
                  border: "1px solid rgba(255,255,255,0.08)",
                  borderRadius: 10,
                  padding: "10px 12px",
                }}
              >
                <div>
                  <div style={{ fontSize: 13, fontWeight: 600, color: "var(--text1)" }}>
                    {m.user}
                  </div>
                  <div style={{ fontSize: 12, color: "var(--text3)", marginTop: 2 }}>
                    {m.seatCode} · {m.seatType} · {m.requestedEquipment?.[0] ?? "—"}
                  </div>
                </div>
                <div style={{ fontSize: 12, color: "var(--text3)" }}>{m.status}</div>
              </div>
            ))}
          </div>
        ) : null}
      </Modal>
    </div>
  );
}
