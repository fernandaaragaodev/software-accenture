import { useEffect, useState } from "react";
import { Modal } from "../components/Modal";
import { StatusBadge } from "../components/StatusBadge";
import type { Room, RoomPosition, RoomStatus, SessionUser } from "../types/officehub";
import {
  createReservation,
  createReservationsBatch,
} from "../services/reservationsService";
import { fetchRoomPositions, fetchRooms } from "../services/roomsService";

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
  seatCode: string;
  seatType: string;
  requestedFor: string;
  selectedEquipment: string[];
}

interface RoomsPageProps {
  user: SessionUser;
}

/** Primeiro dia permitido: hoje + 7 dias (alinhado ao backend). */
function minReservationDateStr(): string {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  d.setDate(d.getDate() + 7);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, "0");
  const day = String(d.getDate()).padStart(2, "0");
  return `${y}-${m}-${day}`;
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
    seatCode: "",
    seatType: "",
    requestedFor: user.name,
    selectedEquipment: [],
  });
  const [positions, setPositions] = useState<RoomPosition[]>([]);
  const [selectedBatchSeats, setSelectedBatchSeats] = useState<string[]>([]);
  const [batchUsersInput, setBatchUsersInput] = useState("");
  const [loadingPositions, setLoadingPositions] = useState(false);
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
    const minDate = minReservationDateStr();
    if (reserveForm.date < minDate) {
      setError(
        "A data da reserva deve ser pelo menos 7 dias à frente e não pode estar no passado.",
      );
      return;
    }
    setReserveLoading(true);
    try {
      const parsedBatchUsers = batchUsersInput
        .split(",")
        .map((item) => item.trim())
        .filter(Boolean);
      const singleTargetUser =
        parsedBatchUsers.length === 1 ? parsedBatchUsers[0] : reserveForm.requestedFor;
      const selectedResource = reserveForm.selectedEquipment[0];

      if (user.role !== "employee" && parsedBatchUsers.length > 1) {
        if (!selectedResource) {
          throw new Error("Escolha 1 recurso para as micro-reservas.");
        }
        if (selectedBatchSeats.length < parsedBatchUsers.length) {
          throw new Error(
            "Selecione pelo menos uma posição para cada colaborador informado.",
          );
        }

        await createReservationsBatch(
          parsedBatchUsers.map((targetUser, index) => {
            const seatCode = selectedBatchSeats[index];
            const selectedPosition = positions.find((item) => item.code === seatCode);
            return {
              roomId: selectedRoom.id,
              requesterName: user.name,
              requesterRole: user.role,
              user: targetUser,
              seatCode,
              seatType: selectedPosition?.type ?? "Mesa",
              requestedEquipment: [selectedResource],
              date: reserveForm.date,
              start: reserveForm.start,
              end: reserveForm.end,
            };
          }),
        );
      } else {
        if (!reserveForm.seatCode) {
          throw new Error("Selecione uma posição.");
        }
        if (!selectedResource) {
          throw new Error("Escolha 1 recurso para a mesa.");
        }
        await createReservation({
          roomId: selectedRoom.id,
          requesterName: user.name,
          requesterRole: user.role,
          user: singleTargetUser,
          seatCode: reserveForm.seatCode,
          seatType: reserveForm.seatType,
          requestedEquipment: [selectedResource],
          date: reserveForm.date,
          start: reserveForm.start,
          end: reserveForm.end,
        });
      }
      await loadRooms();
      await loadPositions();
      setModal(null);
      setReserveForm({
        date: "",
        start: "",
        end: "",
        seatCode: "",
        seatType: "",
        requestedFor: user.name,
        selectedEquipment: [],
      });
      setSelectedBatchSeats([]);
      setBatchUsersInput("");
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

  async function loadPositions() {
    if (
      !selectedRoom ||
      !reserveForm.date ||
      !reserveForm.start ||
      !reserveForm.end
    ) {
      setPositions([]);
      return;
    }
    setLoadingPositions(true);
    try {
      const data = await fetchRoomPositions(
        selectedRoom.id,
        reserveForm.date,
        reserveForm.start,
        reserveForm.end,
      );
      setPositions(data);
      const selectedStillAvailable = data.some(
        (item) => item.code === reserveForm.seatCode && item.available,
      );
      if (!selectedStillAvailable) {
        setReserveForm((prev) => ({
          ...prev,
          seatCode: "",
          seatType: "",
          selectedEquipment: [],
        }));
      }
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Nao foi possivel carregar as posicoes da sala.",
      );
    } finally {
      setLoadingPositions(false);
    }
  }

  useEffect(() => {
    if (modal !== "reserve") return;
    void loadPositions();
  }, [modal, selectedRoom?.id, reserveForm.date, reserveForm.start, reserveForm.end]);

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
                      setReserveForm((prev) => ({
                        ...prev,
                        requestedFor: user.name,
                        seatCode: "",
                        seatType: "",
                        selectedEquipment: [],
                      }));
                      setSelectedBatchSeats([]);
                      setBatchUsersInput("");
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
                  onClick={() => {
                    setReserveForm((prev) => ({
                      ...prev,
                      requestedFor: user.name,
                      seatCode: "",
                      seatType: "",
                      selectedEquipment: [],
                    }));
                    setSelectedBatchSeats([]);
                    setBatchUsersInput("");
                    setModal("reserve");
                  }}
                >
                  Reservar posição
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
        title={`Reservar posição — ${selectedRoom?.name ?? ""}`}
        subtitle="RF06 — Agendamento de cadeira/recursos"
      >
        {selectedRoom ? (
          <>
            {user.role !== "employee" ? (
              <div
                style={{
                  border: "1px solid rgba(0,229,160,0.25)",
                  borderRadius: 10,
                  padding: 12,
                  marginBottom: 12,
                  background: "rgba(0,229,160,0.07)",
                }}
              >
                <div
                  style={{
                    fontSize: 12,
                    fontWeight: 700,
                    color: "var(--green)",
                    marginBottom: 8,
                  }}
                >
                  Interface Gestor/Admin - Reserva em lote
                </div>
                <div className="form-group" style={{ marginBottom: 0 }}>
                  <label className="form-label" htmlFor="res-batch-users">
                    Colaboradores (separados por vírgula)
                  </label>
                  <input
                    id="res-batch-users"
                    className="form-input"
                    placeholder="Ex: Ana Pereira, Bruno Lima, Carla Souza"
                    value={batchUsersInput}
                    onChange={(e) => setBatchUsersInput(e.target.value)}
                  />
                  <div style={{ fontSize: 11, color: "var(--text3)", marginTop: 6 }}>
                    O sistema cria micro-reservas por colaborador e confirma tudo no final.
                  </div>
                </div>
              </div>
            ) : (
              <div className="form-group">
                <label className="form-label" htmlFor="res-for">
                  Reservar para
                </label>
                <input
                  id="res-for"
                  className="form-input"
                  value={reserveForm.requestedFor}
                  disabled
                />
              </div>
            )}
            <div className="form-group">
              <label className="form-label" htmlFor="res-date">
                Data
              </label>
              <input
                id="res-date"
                className="form-input"
                type="date"
                min={minReservationDateStr()}
                value={reserveForm.date}
                onChange={(e) =>
                  setReserveForm((f) => ({ ...f, date: e.target.value }))
                }
              />
              <div style={{ fontSize: 11, color: "var(--text3)", marginTop: 6 }}>
                É necessário agendar com pelo menos 7 dias de antecedência; datas passadas não são permitidas.
              </div>
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
            <div className="form-group">
              <label className="form-label">Posições disponíveis</label>
              {loadingPositions ? (
                <div style={{ fontSize: 12, color: "var(--text3)" }}>
                  Carregando posições...
                </div>
              ) : (
                <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                  {positions.length === 0 ? (
                    <span style={{ fontSize: 12, color: "var(--text3)" }}>
                      Informe data e horário para listar posições.
                    </span>
                  ) : null}
                  {positions.map((position) => (
                    <button
                      key={position.code}
                      type="button"
                      className="btn btn-ghost btn-sm"
                      disabled={!position.available}
                      style={{
                        borderColor:
                          reserveForm.seatCode === position.code ||
                          selectedBatchSeats.includes(position.code)
                            ? "var(--accent)"
                            : undefined,
                        color:
                          reserveForm.seatCode === position.code ||
                          selectedBatchSeats.includes(position.code)
                            ? "var(--accent)"
                            : undefined,
                        opacity: position.available ? 1 : 0.4,
                      }}
                      onClick={() => {
                        if (user.role === "employee") {
                          setReserveForm((prev) => ({
                            ...prev,
                            seatCode: position.code,
                            seatType: position.type,
                            selectedEquipment: [],
                          }));
                          return;
                        }
                        setSelectedBatchSeats((prev) =>
                          prev.includes(position.code)
                            ? prev.filter((seat) => seat !== position.code)
                            : [...prev, position.code],
                        );
                      }}
                    >
                      {position.code} · {position.type}
                    </button>
                  ))}
                </div>
              )}
            </div>
            {user.role !== "employee" && batchUsersInput.trim() ? (
              <div className="form-group">
                <label className="form-label">Micro-reservas do gestor</label>
                <div style={{ display: "flex", flexDirection: "column", gap: 8 }}>
                  {batchUsersInput
                    .split(",")
                    .map((item) => item.trim())
                    .filter(Boolean)
                    .map((employee, index) => {
                      const seatCode = selectedBatchSeats[index];
                      const seatAvailable = seatCode
                        ? positions.some(
                            (position) => position.code === seatCode && position.available,
                          )
                        : false;
                      return (
                        <div
                          key={`${employee}-${index}`}
                          style={{
                            fontSize: 12,
                            border: "1px solid rgba(255,255,255,0.08)",
                            borderRadius: 8,
                            padding: "8px 10px",
                            color: "var(--text2)",
                          }}
                        >
                          {employee} {"->"} {seatCode || "selecionar posição"}
                          {seatCode ? (seatAvailable ? " · disponível" : " · indisponível") : ""}
                        </div>
                      );
                    })}
                </div>
              </div>
            ) : null}
            {reserveForm.seatCode || selectedBatchSeats.length > 0 ? (
              <div className="form-group">
                <label className="form-label">Recursos para a posição</label>
                <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
                  {(positions.find(
                    (position) => position.code === reserveForm.seatCode,
                  )?.availableEquipment ?? positions[0]?.availableEquipment ?? []
                  ).map((equipment) => {
                      const selected = reserveForm.selectedEquipment.includes(equipment);
                      return (
                        <button
                          key={equipment}
                          type="button"
                          className="btn btn-ghost btn-sm"
                          style={{
                            borderColor: selected ? "var(--green)" : undefined,
                            color: selected ? "var(--green)" : undefined,
                          }}
                          onClick={() =>
                            setReserveForm((prev) => ({
                              ...prev,
                              selectedEquipment: selected ? [] : [equipment],
                            }))
                          }
                        >
                          {equipment}
                        </button>
                      );
                    })}
                </div>
              </div>
            ) : null}
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
              ℹ️ Funcionário reserva apenas para si. Gestor/Admin podem reservar
              para terceiros e selecionar mais recursos.
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
                disabled={
                  reserveLoading ||
                  reserveForm.selectedEquipment.length !== 1 ||
                  (user.role === "employee"
                    ? !reserveForm.seatCode
                    : !reserveForm.seatCode && selectedBatchSeats.length === 0)
                }
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
