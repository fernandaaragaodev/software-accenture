import { useEffect, useState } from "react";
import { Modal } from "../components/Modal";
import { StatusBadge } from "../components/StatusBadge";
import type { SessionUser } from "../types/officehub";
import {
  cancelReservation,
} from "../services/reservationsService";
import {
  cancelReservationGroup,
  fetchReservationGroup,
  fetchReservationGroups,
  type ReservationGroup,
} from "../services/reservationGroupsService";

type ResTab = "all" | "mine" | "active" | "cancelled";

interface ReservationsPageProps {
  user: SessionUser;
}

function canCancelEntireGroup(u: SessionUser, g: ReservationGroup): boolean {
  if (u.role === "admin") return true;
  if (u.role === "manager") {
    const owner = (g.requesterName ?? "").trim().toLowerCase();
    return owner === u.name.trim().toLowerCase();
  }
  return g.members.every(
    (m) => m.user.toLowerCase() === u.name.toLowerCase(),
  );
}

function canCancelMemberReservation(
  u: SessionUser,
  memberUser: string,
  g: ReservationGroup,
): boolean {
  if (u.role === "admin") return true;
  if (u.role === "employee") {
    return memberUser.toLowerCase() === u.name.toLowerCase();
  }
  if (u.role === "manager") {
    const owner = (g.requesterName ?? "").trim().toLowerCase();
    if (owner === u.name.trim().toLowerCase()) return true;
    return memberUser.toLowerCase() === u.name.toLowerCase();
  }
  return false;
}

export function ReservationsPage({ user }: ReservationsPageProps) {
  const [groups, setGroups] = useState<ReservationGroup[]>([]);
  const [tab, setTab] = useState<ResTab>("all");
  const [detailsTarget, setDetailsTarget] = useState<ReservationGroup | null>(null);
  const [cancelGroupTarget, setCancelGroupTarget] = useState<ReservationGroup | null>(null);
  const [cancelMemberTarget, setCancelMemberTarget] = useState<{
    group: ReservationGroup;
    memberId: number;
    memberUser: string;
  } | null>(null);
  const [loading, setLoading] = useState(false);
  const [cancelLoading, setCancelLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function loadGroups() {
    setLoading(true);
    try {
      const data = await fetchReservationGroups();
      setGroups(data);
      setError(null);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Falha ao carregar grupos de reserva do backend.",
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadGroups();
    }, 0);
    return () => window.clearTimeout(timer);
  }, []);

  const filtered = groups.filter((g) => {
    if (tab === "mine") {
      if (user.role === "employee") {
        return g.members.some((m) => m.user === user.name);
      }
      if (user.role === "manager") {
        const createdByMe =
          (g.requesterName ?? "").trim().toLowerCase() ===
          user.name.trim().toLowerCase();
        return (
          createdByMe ||
          g.members.some((m) => m.user === user.name)
        );
      }
      return true;
    }
    if (tab === "active") return g.status === "active" || g.status === "confirmed";
    if (tab === "cancelled") return g.status === "cancelled";
    return true;
  });

  async function doCancelGroup(groupId: string) {
    setCancelLoading(true);
    try {
      await cancelReservationGroup(groupId, user.name, user.role);
      await loadGroups();
      setCancelGroupTarget(null);
      setError(null);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Nao foi possivel cancelar a reserva em lote.",
      );
    } finally {
      setCancelLoading(false);
    }
  }

  async function doCancelMember(reservationId: number) {
    setCancelLoading(true);
    try {
      await cancelReservation(reservationId, user.name, user.role);
      await loadGroups();
      setCancelMemberTarget(null);
      setError(null);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Nao foi possivel cancelar a reserva.",
      );
    } finally {
      setCancelLoading(false);
    }
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
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Sala</th>
                <th>Envolvidos</th>
                <th>Data</th>
                <th>Horário</th>
                <th>Status</th>
                <th>Ações</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={6}>
                    <div className="empty-state">
                      <div className="empty-icon">⏳</div>
                      <div className="empty-text">Carregando reservas...</div>
                    </div>
                  </td>
                </tr>
              ) : null}
              {filtered.map((g) => (
                <tr key={g.groupId}>
                  <td style={{ fontWeight: 500, color: "var(--text1)" }}>
                    {g.room}
                  </td>
                  <td>
                    <span style={{ fontSize: 12, color: "var(--text2)" }}>
                      {g.peopleCount} pessoa{g.peopleCount !== 1 ? "s" : ""}
                    </span>
                    <div style={{ fontSize: 11, color: "var(--text3)", marginTop: 2 }}>
                      {g.members
                        .slice(0, 2)
                        .map((m) => m.user)
                        .join(", ")}
                      {g.members.length > 2 ? "..." : ""}
                    </div>
                  </td>
                  <td>{g.date}</td>
                  <td>
                    <span style={{ fontFamily: "monospace", fontSize: 13 }}>
                      {g.start} – {g.end}
                    </span>
                  </td>
                  <td>
                    <StatusBadge status={g.status} />
                  </td>
                  <td>
                    <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                      <button
                        type="button"
                        className="btn btn-ghost btn-sm"
                        onClick={async () => {
                          const full = await fetchReservationGroup(g.groupId);
                          setDetailsTarget(full);
                        }}
                      >
                        Detalhes
                      </button>
                      {g.status !== "cancelled" && canCancelEntireGroup(user, g) ? (
                        <button
                          type="button"
                          className="btn btn-danger btn-sm"
                          onClick={() => setCancelGroupTarget(g)}
                        >
                          Cancelar {g.isBatch ? "lote" : "reserva"}
                        </button>
                      ) : g.status !== "cancelled" ? (
                        <span
                          style={{ fontSize: 12, color: "var(--text3)" }}
                          title="Funcionário: só as próprias. Gestor: só lotes criados por ele. Admin: tudo."
                        >
                          Sem permissão
                        </span>
                      ) : (
                        <span style={{ fontSize: 12, color: "var(--text3)" }}>
                          —
                        </span>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {!loading && filtered.length === 0 ? (
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
        open={!!detailsTarget}
        onClose={() => setDetailsTarget(null)}
        title={detailsTarget?.isBatch ? "Reserva em lote" : "Reserva"}
        subtitle={detailsTarget ? `${detailsTarget.room} · ${detailsTarget.date} (${detailsTarget.start}-${detailsTarget.end})` : undefined}
        wide
      >
        {detailsTarget ? (
          <>
            <div style={{ display: "flex", gap: 8, flexWrap: "wrap", marginBottom: 12 }}>
              <span className="chip">
                {detailsTarget.peopleCount} pessoa{detailsTarget.peopleCount !== 1 ? "s" : ""}
              </span>
              <span className="chip">
                Criado por: {detailsTarget.requesterName || "—"} ({detailsTarget.requesterRole})
              </span>
            </div>
            <div className="card card-sm">
              <div style={{ fontSize: 12, color: "var(--text3)", marginBottom: 10 }}>
                Envolvidos
              </div>
              <div style={{ display: "flex", flexDirection: "column", gap: 10 }}>
                {detailsTarget.members.map((m) => (
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
                    <div style={{ display: "flex", gap: 10, alignItems: "center" }}>
                      <StatusBadge status={m.status} />
                      {m.status !== "cancelled" &&
                      canCancelMemberReservation(user, m.user, detailsTarget) ? (
                        <button
                          type="button"
                          className="btn btn-danger btn-sm"
                          onClick={() =>
                            setCancelMemberTarget({
                              group: detailsTarget,
                              memberId: m.reservationId,
                              memberUser: m.user,
                            })
                          }
                        >
                          Cancelar
                        </button>
                      ) : null}
                    </div>
                  </div>
                ))}
              </div>
            </div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setDetailsTarget(null)}
              >
                Fechar
              </button>
            </div>
          </>
        ) : null}
      </Modal>

      <Modal
        open={!!cancelGroupTarget}
        onClose={() => setCancelGroupTarget(null)}
        title={cancelGroupTarget?.isBatch ? "Cancelar reserva em lote" : "Cancelar reserva"}
        subtitle="RF07 — Esta ação não pode ser desfeita"
      >
        {cancelGroupTarget ? (
          <>
            <div
              style={{
                fontSize: 14,
                color: "var(--text2)",
                lineHeight: 1.7,
                marginBottom: 8,
              }}
            >
              Deseja cancelar {cancelGroupTarget.isBatch ? "todas as micro-reservas" : "a reserva"} da{" "}
              <strong style={{ color: "var(--text1)" }}>{cancelGroupTarget.room}</strong>{" "}
              em{" "}
              <strong style={{ color: "var(--text1)" }}>{cancelGroupTarget.date}</strong>{" "}
              ({cancelGroupTarget.start}–{cancelGroupTarget.end})?
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
                onClick={() => setCancelGroupTarget(null)}
              >
                Manter reserva
              </button>
              <button
                type="button"
                className="btn btn-danger"
                onClick={() => doCancelGroup(cancelGroupTarget.groupId)}
                disabled={cancelLoading}
              >
                {cancelLoading ? "Cancelando..." : "Confirmar cancelamento"}
              </button>
            </div>
          </>
        ) : null}
      </Modal>

      <Modal
        open={!!cancelMemberTarget}
        onClose={() => setCancelMemberTarget(null)}
        title="Cancelar micro-reserva"
        subtitle="RF07 — Esta ação não pode ser desfeita"
      >
        {cancelMemberTarget ? (
          <>
            <div style={{ fontSize: 14, color: "var(--text2)", lineHeight: 1.7, marginBottom: 8 }}>
              Deseja cancelar a reserva de{" "}
              <strong style={{ color: "var(--text1)" }}>{cancelMemberTarget.memberUser}</strong>?
            </div>
            <div className="modal-footer">
              <button type="button" className="btn btn-ghost" onClick={() => setCancelMemberTarget(null)}>
                Manter
              </button>
              <button
                type="button"
                className="btn btn-danger"
                onClick={() => doCancelMember(cancelMemberTarget.memberId)}
                disabled={cancelLoading}
              >
                {cancelLoading ? "Cancelando..." : "Confirmar cancelamento"}
              </button>
            </div>
          </>
        ) : null}
      </Modal>
    </div>
  );
}
