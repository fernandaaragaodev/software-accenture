import { useState } from "react";
import { Modal } from "../components/Modal";
import { StatusBadge } from "../components/StatusBadge";
import { MOCK_RESERVATIONS } from "../data/mock";
import type { Reservation, SessionUser } from "../types/officehub";

type ResTab = "all" | "mine" | "active" | "cancelled";

interface ReservationsPageProps {
  user: SessionUser;
}

export function ReservationsPage({ user }: ReservationsPageProps) {
  const [reservations, setReservations] =
    useState<Reservation[]>(MOCK_RESERVATIONS);
  const [tab, setTab] = useState<ResTab>("all");
  const [cancelTarget, setCancelTarget] = useState<Reservation | null>(null);

  const filtered = reservations.filter((r) => {
    if (tab === "mine")
      return r.user === (user.role === "employee" ? "Carlos Lima" : r.user);
    if (tab === "active")
      return r.status === "active" || r.status === "confirmed";
    if (tab === "cancelled") return r.status === "cancelled";
    return true;
  });

  function doCancel(id: number) {
    setReservations((prev) =>
      prev.map((r) => (r.id === id ? { ...r, status: "cancelled" } : r)),
    );
    setCancelTarget(null);
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-24">
        <div>
          <div
            style={{ fontFamily: "var(--font-head)", fontSize: 18, fontWeight: 700 }}
          >
            Reservas
          </div>
          <div style={{ fontSize: 13, color: "var(--text3)", marginTop: 3 }}>
            Histórico e agendamentos ativos
          </div>
        </div>
      </div>

      <div className="tabs">
        {(
          [
            ["all", "Todas"],
            ["mine", "Minhas reservas"],
            ["active", "Ativas"],
            ["cancelled", "Canceladas"],
          ] as const
        ).map(([k, l]) => (
          <button
            key={k}
            type="button"
            className={`tab ${tab === k ? "active" : ""}`}
            onClick={() => setTab(k)}
          >
            {l}
          </button>
        ))}
      </div>

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Sala</th>
                <th>Usuário</th>
                <th>Data</th>
                <th>Horário</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {filtered.map((r) => (
                <tr key={r.id}>
                  <td style={{ fontWeight: 500, color: "var(--text1)" }}>
                    {r.room}
                  </td>
                  <td>{r.user}</td>
                  <td>{r.date}</td>
                  <td>
                    <span style={{ fontFamily: "monospace", fontSize: 13 }}>
                      {r.start} – {r.end}
                    </span>
                  </td>
                  <td>
                    <StatusBadge status={r.status} />
                  </td>
                  <td>
                    {r.status !== "cancelled" ? (
                      <button
                        type="button"
                        className="btn btn-danger btn-sm"
                        onClick={() => setCancelTarget(r)}
                      >
                        Cancelar
                      </button>
                    ) : (
                      <span style={{ fontSize: 12, color: "var(--text3)" }}>
                        —
                      </span>
                    )}
                  </td>
                </tr>
              ))}
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={6}>
                    <div className="empty-state">
                      <div className="empty-icon">📅</div>
                      <div className="empty-text">Nenhuma reserva encontrada</div>
                    </div>
                  </td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </div>
      </div>

      <Modal
        open={!!cancelTarget}
        onClose={() => setCancelTarget(null)}
        title="Cancelar reserva"
        subtitle="RF07 — Esta ação não pode ser desfeita"
      >
        {cancelTarget ? (
          <>
            <div
              style={{
                fontSize: 14,
                color: "var(--text2)",
                lineHeight: 1.7,
                marginBottom: 8,
              }}
            >
              Deseja cancelar a reserva da{" "}
              <strong style={{ color: "var(--text1)" }}>
                {cancelTarget.room}
              </strong>{" "}
              em{" "}
              <strong style={{ color: "var(--text1)" }}>
                {cancelTarget.date}
              </strong>{" "}
              ({cancelTarget.start}–{cancelTarget.end})?
            </div>
            <div
              style={{
                fontSize: 13,
                color: "var(--text3)",
                background: "rgba(0,229,160,0.06)",
                borderRadius: 8,
                padding: "9px 13px",
              }}
            >
              ✅ A sala voltará automaticamente ao status &quot;Disponível&quot;
              após o cancelamento.
            </div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setCancelTarget(null)}
              >
                Manter reserva
              </button>
              <button
                type="button"
                className="btn btn-danger"
                onClick={() => doCancel(cancelTarget.id)}
              >
                Confirmar cancelamento
              </button>
            </div>
          </>
        ) : null}
      </Modal>
    </div>
  );
}
