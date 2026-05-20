import { useEffect, useState } from "react";
import { Modal } from "../components/Modal";
import { RoomsPremiumBooking } from "../components/rooms/RoomsPremiumBooking";
import type { BookingRoom } from "../components/rooms/roomsPremiumUtils";
import type { SessionUser } from "../types/officehub";
import { setRoomBlocked } from "../services/roomsService";
import "../styles/rooms-premium.css";

interface RoomsPageProps {
  user: SessionUser;
}

export function RoomsPage({ user }: RoomsPageProps) {
  const isAdmin = user.role === "admin";
  const [blockTarget, setBlockTarget] = useState<BookingRoom | null>(null);
  const [blockAdminPassword, setBlockAdminPassword] = useState("");
  const [blockingRoomId, setBlockingRoomId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  useEffect(() => {
    if (!successMessage) return;
    const timer = window.setTimeout(() => setSuccessMessage(null), 5000);
    return () => window.clearTimeout(timer);
  }, [successMessage]);

  function triggerRefresh() {
    setRefreshKey((k) => k + 1);
  }

  function openBlockConfirm(room: BookingRoom) {
    setBlockTarget(room);
    setBlockAdminPassword("");
    setError(null);
  }

  async function confirmDeactivateRoom() {
    if (!blockTarget) return;
    if (!blockAdminPassword.trim()) {
      setError("Digite a senha de confirmação.");
      return;
    }
    setBlockingRoomId(blockTarget.id);
    setError(null);
    try {
      await setRoomBlocked(
        blockTarget.id,
        true,
        user.role,
        blockAdminPassword,
      );
      setBlockTarget(null);
      setBlockAdminPassword("");
      setSuccessMessage(`A sala "${blockTarget.name}" foi desativada.`);
      triggerRefresh();
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Senha inválida ou falha ao desativar.",
      );
    } finally {
      setBlockingRoomId(null);
    }
  }

  async function unblockRoom(room: BookingRoom) {
    setBlockingRoomId(room.id);
    setError(null);
    try {
      await setRoomBlocked(room.id, false, user.role);
      setSuccessMessage(`A sala "${room.name}" foi reativada.`);
      triggerRefresh();
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Não foi possível desbloquear a sala.",
      );
    } finally {
      setBlockingRoomId(null);
    }
  }

  return (
    <div>
      {successMessage ? (
        <div className="rp-alert rp-alert--ok" style={{ marginBottom: 16 }}>
          {successMessage}
        </div>
      ) : null}
      {error && !blockTarget ? (
        <div className="rp-alert" style={{ marginBottom: 16 }}>
          {error}
        </div>
      ) : null}

      <RoomsPremiumBooking
        key={refreshKey}
        user={user}
        isAdmin={isAdmin}
        onAdminBlockRoom={isAdmin ? openBlockConfirm : undefined}
        onAdminUnblockRoom={
          isAdmin
            ? (room) => {
                if (blockingRoomId) return;
                void unblockRoom(room);
              }
            : undefined
        }
      />

      <Modal
        open={!!blockTarget}
        onClose={() => {
          setBlockTarget(null);
          setBlockAdminPassword("");
          setError(null);
        }}
        title="Confirmar desativação"
        subtitle={
          blockTarget
            ? `${blockTarget.name} — a sala ficará indisponível para reservas.`
            : ""
        }
      >
        {blockTarget ? (
          <>
            <div
              style={{
                fontSize: 13,
                color: "var(--text2)",
                marginBottom: 14,
                lineHeight: 1.5,
              }}
            >
              Digite a senha de confirmação do administrador (padrão de
              demonstração: <strong>admin</strong>).
            </div>
            {error ? (
              <div
                style={{
                  fontSize: 12,
                  color: "var(--red)",
                  marginBottom: 12,
                }}
              >
                {error}
              </div>
            ) : null}
            <div className="form-group">
              <label className="form-label" htmlFor="admin-block-password">
                Senha do administrador
              </label>
              <input
                id="admin-block-password"
                className="form-input"
                type="password"
                autoComplete="current-password"
                placeholder="Digite a senha"
                value={blockAdminPassword}
                onChange={(e) => setBlockAdminPassword(e.target.value)}
                onKeyDown={(e) => {
                  if (e.key === "Enter") {
                    e.preventDefault();
                    void confirmDeactivateRoom();
                  }
                }}
              />
            </div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => {
                  setBlockTarget(null);
                  setBlockAdminPassword("");
                  setError(null);
                }}
              >
                Cancelar
              </button>
              <button
                type="button"
                className="btn btn-primary"
                style={{ background: "var(--red)", borderColor: "var(--red)" }}
                disabled={blockingRoomId === blockTarget.id}
                onClick={() => void confirmDeactivateRoom()}
              >
                {blockingRoomId === blockTarget.id
                  ? "Confirmando…"
                  : "Confirmar desativação"}
              </button>
            </div>
          </>
        ) : null}
      </Modal>
    </div>
  );
}
