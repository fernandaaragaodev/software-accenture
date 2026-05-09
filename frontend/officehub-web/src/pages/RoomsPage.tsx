import { useEffect, useState } from "react";
import { Modal } from "../components/Modal";
import { StatusBadge } from "../components/StatusBadge";
import type { Room, RoomStatus, SessionUser } from "../types/officehub";
import { createReservation } from "../services/reservationsService";
import { fetchRooms } from "../services/roomsService";

type RoomsModal = "add" | "view" | "reserve" | "upload" | null;

interface NewRoomForm {
  name: string;
  capacity: string;
  floor: string;
  area: string;
  equipment: string;
}

interface ReserveForm {
  date: string;
  start: string;
  end: string;
}

interface RoomsPageProps {
  user: SessionUser;
}

export function RoomsPage({ user }: RoomsPageProps) {
  const [rooms, setRooms] = useState<Room[]>([]);
  const [filter, setFilter] = useState<"all" | RoomStatus>("all");
  const [search, setSearch] = useState("");
  const [modal, setModal] = useState<RoomsModal>(null);
  const [selectedRoom, setSelectedRoom] = useState<Room | null>(null);
  const [loadingRooms, setLoadingRooms] = useState(false);
  const [reserveLoading, setReserveLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [newRoom, setNewRoom] = useState<NewRoomForm>({
    name: "",
    capacity: "",
    floor: "",
    area: "",
    equipment: "",
  });
  const [reserveForm, setReserveForm] = useState<ReserveForm>({
    date: "",
    start: "",
    end: "",
  });
  const [aiLoading, setAiLoading] = useState(false);

  const canEdit = user.role === "admin" || user.role === "manager";

  async function loadRooms() {
    setLoadingRooms(true);
    try {
      const data = await fetchRooms();
      setRooms(data);
      setError(null);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Falha ao carregar salas do backend.",
      );
    } finally {
      setLoadingRooms(false);
    }
  }

  useEffect(() => {
    const timer = window.setTimeout(() => {
      void loadRooms();
    }, 0);
    return () => window.clearTimeout(timer);
  }, []);

  const filtered = rooms.filter((r) => {
    if (filter !== "all" && r.status !== filter) return false;
    if (search && !r.name.toLowerCase().includes(search.toLowerCase()))
      return false;
    return true;
  });

  function addRoom() {
    if (!newRoom.name) return;
    const r: Room = {
      id: Date.now(),
      name: newRoom.name,
      capacity: parseInt(newRoom.capacity, 10) || 8,
      desks: parseInt(newRoom.capacity, 10) || 8,
      status: "available",
      equipment: newRoom.equipment
        .split(",")
        .map((s) => s.trim())
        .filter(Boolean),
      floor: newRoom.floor || "1º andar",
      area: parseInt(newRoom.area, 10) || 30,
    };
    setRooms((prev) => [r, ...prev]);
    setModal(null);
    setNewRoom({
      name: "",
      capacity: "",
      floor: "",
      area: "",
      equipment: "",
    });
  }

  async function handleReserve() {
    if (!reserveForm.date || !reserveForm.start || !reserveForm.end) return;
    if (!selectedRoom) return;
    setReserveLoading(true);
    try {
      await createReservation({
        roomId: selectedRoom.id,
        user: user.name,
        date: reserveForm.date,
        start: reserveForm.start,
        end: reserveForm.end,
      });
      await loadRooms();
      setModal(null);
      setReserveForm({ date: "", start: "", end: "" });
      setError(null);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Nao foi possivel criar a reserva.",
      );
    } finally {
      setReserveLoading(false);
    }
  }

  function simulateAI() {
    setAiLoading(true);
    setTimeout(() => setAiLoading(false), 2200);
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-24">
        <div>
          <div
            className="section-title"
            style={{
              fontSize: 18,
              fontFamily: "var(--font-head)",
              fontWeight: 700,
            }}
          >
            Salas e Escritórios
          </div>
          <div style={{ fontSize: 13, color: "var(--text3)", marginTop: 3 }}>
            {filtered.length} sala{filtered.length !== 1 ? "s" : ""} exibida
            {filtered.length !== 1 ? "s" : ""}
          </div>
        </div>
        <div className="flex gap-8">
          {canEdit ? (
            <>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setModal("upload")}
              >
                ⬆ Importar layout
              </button>
              <button
                type="button"
                className="btn btn-primary"
                onClick={() => setModal("add")}
              >
                + Nova sala
              </button>
            </>
          ) : null}
        </div>
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

      <div className="flex gap-12 mb-16" style={{ flexWrap: "wrap" }}>
        <div className="topbar-search" style={{ flex: 1, minWidth: 200 }}>
          <span style={{ fontSize: 14 }}>🔍</span>
          <input
            placeholder="Buscar sala..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        {(["all", "available", "occupied", "reserved"] as const).map((f) => (
          <button
            key={f}
            type="button"
            className="btn btn-ghost btn-sm"
            style={{
              borderColor: filter === f ? "var(--accent)" : undefined,
              color: filter === f ? "var(--accent)" : undefined,
            }}
            onClick={() => setFilter(f)}
          >
            {f === "all"
              ? "Todas"
              : f === "available"
                ? "Disponíveis"
                : f === "occupied"
                  ? "Ocupadas"
                  : "Reservadas"}
          </button>
        ))}
      </div>

      <div className="rooms-grid">
        {loadingRooms ? (
          <div className="empty-state" style={{ gridColumn: "1/-1" }}>
            <div className="empty-icon">⏳</div>
            <div className="empty-text">Carregando salas...</div>
          </div>
        ) : null}
        {filtered.map((room) => (
          <div
            key={room.id}
            className="room-card"
            onClick={() => {
              setSelectedRoom(room);
              setModal("view");
            }}
          >
            <div className={`room-thumb ${room.status}`}>
              <span>
                {room.status === "available"
                  ? "🏢"
                  : room.status === "occupied"
                    ? "👥"
                    : "📅"}
              </span>
              <div className="room-status-overlay">
                <StatusBadge status={room.status} />
              </div>
            </div>
            <div className="room-body">
              <div className="room-name">{room.name}</div>
              <div className="room-meta">
                <span>👤 {room.capacity} pessoas</span>
                <span>🪑 {room.desks} mesas</span>
                <span>📍 {room.floor}</span>
              </div>
              <div
                style={{
                  display: "flex",
                  gap: 6,
                  marginTop: 10,
                  flexWrap: "wrap",
                }}
              >
                {room.equipment.slice(0, 2).map((e) => (
                  <span
                    key={e}
                    className="chip"
                    style={{ fontSize: 11, padding: "2px 8px" }}
                  >
                    {e}
                  </span>
                ))}
                {room.equipment.length > 2 ? (
                  <span
                    className="chip"
                    style={{ fontSize: 11, padding: "2px 8px" }}
                  >
                    +{room.equipment.length - 2}
                  </span>
                ) : null}
              </div>
              <div
                className="room-actions"
                onClick={(e) => e.stopPropagation()}
              >
                {room.status === "available" ? (
                  <button
                    type="button"
                    className="btn btn-primary btn-sm"
                    onClick={() => {
                      setSelectedRoom(room);
                      setModal("reserve");
                    }}
                  >
                    Reservar
                  </button>
                ) : (
                  <button
                    type="button"
                    className="btn btn-ghost btn-sm"
                    disabled
                  >
                    Indisponível
                  </button>
                )}
                <button
                  type="button"
                  className="btn btn-ghost btn-sm"
                  onClick={() => {
                    setSelectedRoom(room);
                    setModal("view");
                  }}
                >
                  Detalhes
                </button>
              </div>
            </div>
          </div>
        ))}
        {!loadingRooms && filtered.length === 0 ? (
          <div className="empty-state" style={{ gridColumn: "1/-1" }}>
            <div className="empty-icon">🏢</div>
            <div className="empty-text">Nenhuma sala encontrada</div>
          </div>
        ) : null}
      </div>

      <Modal
        open={modal === "add"}
        onClose={() => setModal(null)}
        title="Cadastrar nova sala"
        subtitle="RF04 — Registro de espaços"
      >
        <div className="form-group">
          <label className="form-label" htmlFor="new-room-name">
            Nome da sala *
          </label>
          <input
            id="new-room-name"
            className="form-input"
            placeholder="Ex: Sala Hermes"
            value={newRoom.name}
            onChange={(e) =>
              setNewRoom((f) => ({ ...f, name: e.target.value }))
            }
          />
        </div>
        <div className="form-row">
          <div className="form-group">
            <label className="form-label" htmlFor="new-room-cap">
              Capacidade (pessoas)
            </label>
            <input
              id="new-room-cap"
              className="form-input"
              type="number"
              placeholder="12"
              value={newRoom.capacity}
              onChange={(e) =>
                setNewRoom((f) => ({ ...f, capacity: e.target.value }))
              }
            />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="new-room-area">
              Área (m²)
            </label>
            <input
              id="new-room-area"
              className="form-input"
              type="number"
              placeholder="45"
              value={newRoom.area}
              onChange={(e) =>
                setNewRoom((f) => ({ ...f, area: e.target.value }))
              }
            />
          </div>
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor="new-room-floor">
            Andar / localização
          </label>
          <select
            id="new-room-floor"
            className="form-select"
            value={newRoom.floor}
            onChange={(e) =>
              setNewRoom((f) => ({ ...f, floor: e.target.value }))
            }
          >
            <option value="">Selecione...</option>
            <option>Térreo</option>
            <option>1º andar</option>
            <option>2º andar</option>
            <option>3º andar</option>
          </select>
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor="new-room-eq">
            Equipamentos (separados por vírgula)
          </label>
          <input
            id="new-room-eq"
            className="form-input"
            placeholder="Projetor, Videoconferência, Ar-condicionado"
            value={newRoom.equipment}
            onChange={(e) =>
              setNewRoom((f) => ({ ...f, equipment: e.target.value }))
            }
          />
        </div>
        <div
          style={{
            marginBottom: 16,
            padding: 14,
            borderRadius: 10,
            background: "rgba(167,139,250,0.06)",
            border: "1px dashed rgba(167,139,250,0.25)",
          }}
        >
          <div
            style={{
              fontSize: 12,
              color: "var(--purple)",
              marginBottom: 8,
              fontWeight: 600,
            }}
          >
            🤖 Gerar imagem com IA (RF10)
          </div>
          <div
            style={{
              fontSize: 12,
              color: "var(--text3)",
              marginBottom: 10,
            }}
          >
            A IA criará uma representação visual da sala com base nas
            informações acima.
          </div>
          <button
            type="button"
            className="btn btn-ghost btn-sm"
            onClick={simulateAI}
            style={{
              borderColor: "rgba(167,139,250,0.3)",
              color: "var(--purple)",
            }}
          >
            {aiLoading ? "⏳ Gerando..." : "✨ Gerar imagem"}
          </button>
          {!aiLoading ? (
            <div className="ai-room-img" style={{ marginTop: 10 }}>
              <div className="ai-room-img-icon">🖼️</div>
              <span>A imagem aparecerá aqui</span>
            </div>
          ) : (
            <div className="ai-room-img" style={{ marginTop: 10 }}>
              <span className="ai-pulse">
                <span className="ai-dot" />
                Gerando imagem...
              </span>
            </div>
          )}
        </div>
        <div className="modal-footer">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => setModal(null)}
          >
            Cancelar
          </button>
          <button type="button" className="btn btn-primary" onClick={addRoom}>
            Cadastrar sala
          </button>
        </div>
      </Modal>

      <Modal
        open={modal === "view" && !!selectedRoom}
        onClose={() => setModal(null)}
        title={selectedRoom?.name ?? ""}
        subtitle={`${selectedRoom?.floor} · ${selectedRoom?.area}m²`}
        wide
      >
        {selectedRoom ? (
          <>
            <div className="grid-2 mb-16">
              <div>
                <div className="ai-room-img" style={{ marginBottom: 14 }}>
                  <span style={{ fontSize: 48 }}>🏢</span>
                  <span style={{ fontSize: 12, color: "var(--text3)" }}>
                    Visualização IA (RF12)
                  </span>
                </div>
                <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                  {selectedRoom.equipment.map((e) => (
                    <span key={e} className="chip">
                      {e}
                    </span>
                  ))}
                </div>
              </div>
              <div>
                <div className="card card-sm mb-16">
                  <div
                    style={{
                      display: "grid",
                      gridTemplateColumns: "1fr 1fr",
                      gap: 14,
                    }}
                  >
                    {(
                      [
                        ["Capacidade", `${selectedRoom.capacity} pessoas`],
                        ["Mesas", `${selectedRoom.desks} unidades`],
                        ["Área", `${selectedRoom.area} m²`],
                        [
                          "Status",
                          <StatusBadge
                            key="s"
                            status={selectedRoom.status}
                          />,
                        ],
                      ] as const
                    ).map(([k, v]) => (
                      <div key={k}>
                        <div
                          style={{
                            fontSize: 11,
                            color: "var(--text3)",
                            marginBottom: 4,
                            textTransform: "uppercase",
                            letterSpacing: "0.5px",
                          }}
                        >
                          {k}
                        </div>
                        <div style={{ fontSize: 14, fontWeight: 500 }}>
                          {v}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
                <div className="card card-sm">
                  <div
                    style={{
                      fontSize: 12,
                      color: "var(--text3)",
                      marginBottom: 10,
                      fontWeight: 600,
                      textTransform: "uppercase",
                      letterSpacing: "0.5px",
                    }}
                  >
                    Ocupação atual (RF08)
                  </div>
                  <div className="progress-bar mb-8" style={{ height: 10 }}>
                    <div
                      className="progress-fill"
                      style={{
                        width:
                          selectedRoom.status === "occupied"
                            ? "100%"
                            : selectedRoom.status === "reserved"
                              ? "60%"
                              : "0%",
                        background:
                          selectedRoom.status === "occupied"
                            ? "var(--red)"
                            : "var(--green)",
                      }}
                    />
                  </div>
                  <div style={{ fontSize: 12, color: "var(--text3)" }}>
                    {selectedRoom.status === "occupied"
                      ? `${selectedRoom.capacity}/${selectedRoom.capacity}`
                      : selectedRoom.status === "reserved"
                        ? `0/${selectedRoom.capacity} · Reservada`
                        : `0/${selectedRoom.capacity} · Disponível`}
                  </div>
                </div>
              </div>
            </div>
            <div className="modal-footer">
              {selectedRoom.status === "available" ? (
                <button
                  type="button"
                  className="btn btn-primary"
                  onClick={() => setModal("reserve")}
                >
                  Reservar esta sala
                </button>
              ) : null}
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setModal(null)}
              >
                Fechar
              </button>
            </div>
          </>
        ) : null}
      </Modal>

      <Modal
        open={modal === "reserve" && !!selectedRoom}
        onClose={() => setModal(null)}
        title={`Reservar — ${selectedRoom?.name ?? ""}`}
        subtitle="RF06 — Agendamento de sala"
      >
        {selectedRoom ? (
          <>
            <div className="form-group">
              <label className="form-label" htmlFor="res-date">
                Data
              </label>
              <input
                id="res-date"
                className="form-input"
                type="date"
                value={reserveForm.date}
                onChange={(e) =>
                  setReserveForm((f) => ({ ...f, date: e.target.value }))
                }
              />
            </div>
            <div className="form-row">
              <div className="form-group">
                <label className="form-label" htmlFor="res-start">
                  Início
                </label>
                <input
                  id="res-start"
                  className="form-input"
                  type="time"
                  value={reserveForm.start}
                  onChange={(e) =>
                    setReserveForm((f) => ({ ...f, start: e.target.value }))
                  }
                />
              </div>
              <div className="form-group">
                <label className="form-label" htmlFor="res-end">
                  Término
                </label>
                <input
                  id="res-end"
                  className="form-input"
                  type="time"
                  value={reserveForm.end}
                  onChange={(e) =>
                    setReserveForm((f) => ({ ...f, end: e.target.value }))
                  }
                />
              </div>
            </div>
            <div
              style={{
                fontSize: 13,
                color: "var(--text3)",
                background: "var(--bg3)",
                borderRadius: 8,
                padding: "10px 14px",
                marginBottom: 4,
              }}
            >
              ℹ️ O status da sala será atualizado automaticamente (RF09) no
              horário de início da reserva.
            </div>
            <div className="modal-footer">
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setModal(null)}
              >
                Cancelar
              </button>
              <button
                type="button"
                className="btn btn-primary"
                onClick={handleReserve}
                disabled={reserveLoading}
              >
                {reserveLoading ? "Confirmando..." : "Confirmar reserva"}
              </button>
            </div>
          </>
        ) : null}
      </Modal>

      <Modal
        open={modal === "upload"}
        onClose={() => setModal(null)}
        title="Importar layout por IA"
        subtitle="RF11 — Interpretação automática de planta"
      >
        <div
          style={{
            textAlign: "center",
            padding: "32px 20px",
            border: "2px dashed rgba(167,139,250,0.3)",
            borderRadius: 12,
            marginBottom: 20,
            cursor: "pointer",
          }}
        >
          <div style={{ fontSize: 36, marginBottom: 10 }}>📐</div>
          <div
            style={{ fontSize: 14, color: "var(--text2)", marginBottom: 6 }}
          >
            Arraste o arquivo de planta aqui
          </div>
          <div style={{ fontSize: 12, color: "var(--text3)" }}>
            Formatos: PDF, PNG, SVG, DWG
          </div>
        </div>
        <div
          style={{
            padding: 14,
            borderRadius: 10,
            background: "rgba(167,139,250,0.06)",
            border: "1px solid rgba(167,139,250,0.15)",
            fontSize: 13,
            color: "var(--text3)",
            lineHeight: 1.6,
          }}
        >
          A IA identificará automaticamente mesas, cadeiras, divisórias e áreas
          — preenchendo o cadastro sem intervenção manual.
        </div>
        <div className="modal-footer">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => setModal(null)}
          >
            Cancelar
          </button>
          <button
            type="button"
            className="btn btn-primary"
            onClick={() => setModal(null)}
          >
            Processar com IA
          </button>
        </div>
      </Modal>
    </div>
  );
}
