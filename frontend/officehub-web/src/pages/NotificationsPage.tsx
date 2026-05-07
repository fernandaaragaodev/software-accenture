import { useState } from "react";
import { MOCK_NOTIFS } from "../data/mock";
import type { NotificationItem } from "../types/officehub";

export function NotificationsPage() {
  const [notifs, setNotifs] = useState<NotificationItem[]>(MOCK_NOTIFS);

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
            RF15 — Alertas de reservas e mudanças
          </div>
        </div>
        <button
          type="button"
          className="btn btn-ghost btn-sm"
          onClick={() =>
            setNotifs((prev) => prev.map((n) => ({ ...n, read: true })))
          }
        >
          Marcar todas como lidas
        </button>
      </div>
      <div className="card">
        {notifs.map((n) => (
          <div
            key={n.id}
            className="notif-item"
            style={{ opacity: n.read ? 0.55 : 1 }}
            onClick={() =>
              setNotifs((prev) =>
                prev.map((x) => (x.id === n.id ? { ...x, read: true } : x)),
              )
            }
          >
            <span
              className="notif-dot-big"
              style={{ background: n.color }}
            />
            <div className="notif-content">
              <div className="notif-text">{n.text}</div>
              <div className="notif-time">
                {n.time}{" "}
                {!n.read ? (
                  <span style={{ color: "var(--accent)", fontWeight: 600 }}>
                    · Nova
                  </span>
                ) : null}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
