import { useCallback, useEffect, useMemo, useState } from "react";
import type { SessionUser } from "../../types/officehub";
import { createReservation } from "../../services/reservationsService";
import { fetchRoomPositions, fetchRooms } from "../../services/roomsService";
import {
  availableCount,
  calculateDuration,
  formatFilterDateLabel,
  getEquipIcon,
  mapApiPositions,
  mapApiRoom,
  minReservationDateStr,
  roomAvailabilityClass,
  type BookingPosition,
  type BookingRoom,
} from "./roomsPremiumUtils";
import "../../styles/rooms-premium.css";

const TIME_OPTIONS = [
  "08:00",
  "09:00",
  "10:00",
  "11:00",
  "12:00",
  "13:00",
  "14:00",
  "15:00",
  "16:00",
  "17:00",
  "18:00",
];

const WEEKDAYS = ["Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb"];
const STEP_LABELS = ["Data", "Horário", "Posição", "Confirmação"];

interface RoomsPremiumBookingProps {
  user: SessionUser;
  onAdminBlockRoom?: (room: BookingRoom) => void;
  onAdminUnblockRoom?: (room: BookingRoom) => void;
  isAdmin?: boolean;
}

export function RoomsPremiumBooking({
  user,
  onAdminBlockRoom,
  onAdminUnblockRoom,
  isAdmin = false,
}: RoomsPremiumBookingProps) {
  const [rooms, setRooms] = useState<BookingRoom[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [filterDate, setFilterDate] = useState(minReservationDateStr);
  const [filterStartTime, setFilterStartTime] = useState("09:00");
  const [filterEndTime, setFilterEndTime] = useState("12:00");
  const [selectedResources, setSelectedResources] = useState<string[]>([]);
  const [viewMode, setViewMode] = useState<"grid" | "list">("grid");
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [currentStep, setCurrentStep] = useState(1);
  const [selectedRoom, setSelectedRoom] = useState<BookingRoom | null>(null);
  const [selectedPosition, setSelectedPosition] = useState<BookingPosition | null>(
    null,
  );
  const [selectedCalendarDay, setSelectedCalendarDay] = useState<number | null>(
    null,
  );
  const [customStartTime, setCustomStartTime] = useState("09:00");
  const [customEndTime, setCustomEndTime] = useState("12:00");
  const [isBooking, setIsBooking] = useState(false);
  const [showSuccess, setShowSuccess] = useState(false);

  const minDate = minReservationDateStr();

  const allResources = useMemo(
    () => [...new Set(rooms.flatMap((r) => r.equipments))].sort(),
    [rooms],
  );

  const loadRoomPositions = useCallback(
    async (room: BookingRoom) => {
      if (!filterDate) return;
      try {
        const data = await fetchRoomPositions(
          room.id,
          filterDate,
          filterStartTime,
          filterEndTime,
        );
        room.positions = mapApiPositions(data);
      } catch {
        room.positions = [];
      }
    },
    [filterDate, filterStartTime, filterEndTime],
  );

  const loadRooms = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const apiRooms = await fetchRooms();
      const mapped = apiRooms.map(mapApiRoom);
      const withPositions = await Promise.all(
        mapped.map(async (room) => {
          const copy = { ...room, positions: [] as BookingPosition[] };
          await loadRoomPositions(copy);
          return copy;
        }),
      );
      setRooms(withPositions);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Falha ao carregar salas.",
      );
      setRooms([]);
    } finally {
      setIsLoading(false);
    }
  }, [loadRoomPositions]);

  useEffect(() => {
    void loadRooms();
  }, [filterDate, filterStartTime, filterEndTime, loadRooms]);

  const filteredRooms = useMemo(() => {
    return rooms.filter((room) => {
      const q = searchQuery.toLowerCase();
      const matchesSearch =
        room.name.toLowerCase().includes(q) ||
        room.location.toLowerCase().includes(q);
      const matchesResources = selectedResources.every((res) =>
        room.equipments.includes(res),
      );
      if (room.status === "unavailable") return false;
      return matchesSearch && matchesResources;
    });
  }, [rooms, searchQuery, selectedResources]);

  const totalAvailable = useMemo(
    () =>
      filteredRooms.reduce((acc, room) => acc + availableCount(room), 0),
    [filteredRooms],
  );

  function toggleResource(resource: string) {
    setSelectedResources((prev) =>
      prev.includes(resource)
        ? prev.filter((r) => r !== resource)
        : [...prev, resource],
    );
  }

  function clearFilters() {
    setSearchQuery("");
    setSelectedResources([]);
  }

  async function openModal(room: BookingRoom | null = null) {
    const target = room ?? filteredRooms[0] ?? rooms[0] ?? null;
    if (!target) return;
    const copy = { ...target, positions: [...target.positions] };
    await loadRoomPositions(copy);
    setSelectedRoom(copy);
    setCustomStartTime(filterStartTime);
    setCustomEndTime(filterEndTime);
    const day = parseInt(filterDate.split("-")[2] ?? "", 10);
    setSelectedCalendarDay(Number.isNaN(day) ? null : day);
    setSelectedPosition(null);
    setCurrentStep(1);
    setIsModalOpen(true);
  }

  function closeModal() {
    setIsModalOpen(false);
    setCurrentStep(1);
    setSelectedPosition(null);
  }

  async function confirmBooking() {
    if (!selectedRoom || !selectedPosition) return;
    if (filterDate < minDate) {
      setError(
        "A data da reserva deve ser pelo menos 7 dias à frente.",
      );
      return;
    }
    setIsBooking(true);
    setError(null);
    try {
      await createReservation({
        roomId: selectedRoom.id,
        requesterName: user.name,
        user: user.name,
        requesterRole: user.role,
        seatCode: selectedPosition.name,
        seatType: selectedPosition.type,
        requestedEquipment:
          selectedResources.length > 0
            ? selectedResources
            : selectedRoom.equipments,
        date: filterDate,
        start: customStartTime,
        end: customEndTime,
      });
      setShowSuccess(true);
      await loadRooms();
      window.setTimeout(() => {
        setShowSuccess(false);
        closeModal();
      }, 2500);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Falha ao confirmar reserva.",
      );
    } finally {
      setIsBooking(false);
    }
  }

  function nextStep() {
    if (currentStep < 4) {
      setCurrentStep((s) => s + 1);
      return;
    }
    void confirmBooking();
  }

  const calendarDays = useMemo(() => {
    const [y, m] = filterDate.split("-").map(Number);
    if (!y || !m) return [];
    const daysInMonth = new Date(y, m, 0).getDate();
    const minDay =
      filterDate === minDate ? parseInt(minDate.split("-")[2] ?? "1", 10) : 1;
    return Array.from({ length: daysInMonth }, (_, i) => ({
      day: i + 1,
      disabled: i + 1 < minDay,
    }));
  }, [filterDate, minDate]);

  function selectCalendarDay(day: number) {
    setSelectedCalendarDay(day);
    const [y, m] = filterDate.split("-");
    setFilterDate(`${y}-${m}-${String(day).padStart(2, "0")}`);
  }

  const durationLabel = calculateDuration(customStartTime, customEndTime);

  return (
    <div className="rp-root">
      <div className="flex items-center justify-between mb-20" style={{ flexWrap: "wrap", gap: 12 }}>
        <div>
          <p style={{ margin: 0, fontSize: 13, color: "var(--rp-muted)" }}>
            Gerencie e reserve estações de trabalho
          </p>
        </div>
        <button
          type="button"
          className="rp-btn-highlight"
          onClick={() => void openModal()}
          disabled={!filteredRooms.length}
        >
          + Nova Reserva
        </button>
      </div>

      {error ? <div className="rp-alert">{error}</div> : null}

      <section className="rp-filters">
        <div className="rp-field rp-field--wide">
          <label className="rp-label" htmlFor="rp-search">
            Buscar sala ou estação
          </label>
          <input
            id="rp-search"
            type="search"
            className="rp-input"
            placeholder="Nome da sala, andar..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>
        <div className="rp-field">
          <label className="rp-label" htmlFor="rp-date">
            Data da reserva
          </label>
          <input
            id="rp-date"
            type="date"
            className="rp-input"
            min={minDate}
            value={filterDate}
            onChange={(e) => setFilterDate(e.target.value)}
          />
        </div>
        <div className="rp-field">
          <label className="rp-label" htmlFor="rp-start">
            Início
          </label>
          <select
            id="rp-start"
            className="rp-select"
            value={filterStartTime}
            onChange={(e) => setFilterStartTime(e.target.value)}
          >
            {TIME_OPTIONS.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </div>
        <div className="rp-field">
          <label className="rp-label" htmlFor="rp-end">
            Fim
          </label>
          <select
            id="rp-end"
            className="rp-select"
            value={filterEndTime}
            onChange={(e) => setFilterEndTime(e.target.value)}
          >
            {TIME_OPTIONS.map((t) => (
              <option key={t} value={t}>
                {t}
              </option>
            ))}
          </select>
        </div>
        <div className="rp-field rp-field--wide">
          <span className="rp-label">Recursos da posição</span>
          <div className="rp-chips">
            {allResources.map((resource) => (
              <button
                key={resource}
                type="button"
                className={`rp-chip ${selectedResources.includes(resource) ? "rp-chip--active" : ""}`}
                onClick={() => toggleResource(resource)}
              >
                <span>{getEquipIcon(resource)}</span>
                {resource}
                {selectedResources.includes(resource) ? " ×" : ""}
              </button>
            ))}
          </div>
        </div>
        <button type="button" className="rp-link-btn" onClick={clearFilters}>
          Limpar filtros
        </button>
      </section>

      <div className="rp-toolbar">
        <div style={{ display: "flex", alignItems: "center", gap: 12, flexWrap: "wrap" }}>
          <h2>
            Salas disponíveis
            <span className="rp-toolbar-meta">
              ({filteredRooms.length} salas encontradas)
            </span>
          </h2>
          <span className="rp-stat-pill">
            <span className="rp-stat-dot" />
            {totalAvailable} posições livres
          </span>
        </div>
        <div className="rp-view-toggle">
          <button
            type="button"
            className={`rp-view-btn ${viewMode === "grid" ? "rp-view-btn--active" : ""}`}
            onClick={() => setViewMode("grid")}
            title="Grade"
          >
            ⊞
          </button>
          <button
            type="button"
            className={`rp-view-btn ${viewMode === "list" ? "rp-view-btn--active" : ""}`}
            onClick={() => setViewMode("list")}
            title="Lista"
          >
            ☰
          </button>
        </div>
      </div>

      {isLoading ? (
        <div className="rp-skeleton-grid">
          {[1, 2, 3, 4].map((i) => (
            <div key={i} className="rp-skeleton-card">
              <div className="rp-skeleton" style={{ height: 48, width: "70%", marginBottom: 12 }} />
              <div className="rp-skeleton" style={{ height: 40, marginBottom: 8 }} />
              <div className="rp-skeleton" style={{ height: 40, marginBottom: 8 }} />
              <div className="rp-skeleton" style={{ height: 36 }} />
            </div>
          ))}
        </div>
      ) : null}

      {!isLoading && filteredRooms.length === 0 ? (
        <div className="rp-empty">
          <div className="rp-empty-icon">🔍</div>
          <h3 style={{ color: "#fff", marginBottom: 8 }}>Nenhuma sala encontrada</h3>
          <p style={{ color: "var(--rp-muted)", maxWidth: 400, margin: "0 auto 20px" }}>
            Tente ajustar seus filtros para encontrar posições disponíveis.
          </p>
          <button type="button" className="rp-btn-highlight" onClick={clearFilters}>
            Limpar todos os filtros
          </button>
        </div>
      ) : null}

      {!isLoading && viewMode === "grid" ? (
        <div className="rp-grid">
          {filteredRooms.map((room) => (
            <RoomCard
              key={room.id}
              room={room}
              isAdmin={isAdmin}
              onReserve={() => void openModal(room)}
              onBlock={onAdminBlockRoom}
              onUnblock={onAdminUnblockRoom}
            />
          ))}
        </div>
      ) : null}

      {!isLoading && viewMode === "list" ? (
        <div className="rp-list">
          {filteredRooms.map((room) => {
            const free = availableCount(room);
            const total = room.positions.length;
            return (
              <div key={room.id} className="rp-card rp-card--list">
                <div style={{ display: "flex", alignItems: "center", gap: 20, flex: 1 }}>
                  <div className="rp-card-icon">{room.icon}</div>
                  <div>
                    <h3 className="rp-card-title">{room.name}</h3>
                    <p className="rp-card-loc">📍 {room.location}</p>
                  </div>
                  <div style={{ textAlign: "center" }}>
                    <span className="rp-label">Livres</span>
                    <div>
                      {free}/{total}
                    </div>
                  </div>
                  <div className="rp-equip-row">
                    {room.equipments.map((e) => (
                      <span key={e} className="rp-equip-icon" title={e}>
                        {getEquipIcon(e)}
                      </span>
                    ))}
                  </div>
                </div>
                <button
                  type="button"
                  className="rp-btn-primary"
                  style={{ width: "auto", minWidth: 140 }}
                  onClick={() => void openModal(room)}
                >
                  Ver posições
                </button>
              </div>
            );
          })}
        </div>
      ) : null}

      {isModalOpen && selectedRoom ? (
        <div className="rp-overlay" role="dialog" aria-modal="true">
          <div className="rp-modal">
            <div className="rp-modal-head">
              <div>
                <h2 style={{ margin: 0, color: "#fff" }}>Reserva de estação</h2>
                <p style={{ margin: "4px 0 0", color: "var(--rp-muted)", fontSize: 13 }}>
                  {selectedRoom.name}
                </p>
              </div>
              <button type="button" className="rp-btn-ghost" onClick={closeModal}>
                ✕
              </button>
            </div>
            <div className="rp-modal-body">
              <div className="rp-stepper">
                {STEP_LABELS.map((label, i) => {
                  const step = i + 1;
                  return (
                    <div key={label} className="rp-step">
                      <div
                        className={`rp-step-circle ${currentStep >= step ? "rp-step-circle--active" : ""}`}
                      >
                        {step}
                      </div>
                      <span className="rp-step-label">{label}</span>
                    </div>
                  );
                })}
              </div>

              {currentStep === 1 ? (
                <div className="rp-calendar">
                  {WEEKDAYS.map((d) => (
                    <div key={d} className="rp-cal-day-name">
                      {d}
                    </div>
                  ))}
                  {calendarDays.map(({ day, disabled }) => (
                    <button
                      key={day}
                      type="button"
                      className={`rp-cal-day ${selectedCalendarDay === day ? "rp-cal-day--selected" : ""}`}
                      disabled={disabled}
                      onClick={() => selectCalendarDay(day)}
                    >
                      {day}
                    </button>
                  ))}
                </div>
              ) : null}

              {currentStep === 2 ? (
                <>
                  <p style={{ color: "var(--rp-muted)", fontSize: 13, marginBottom: 12 }}>
                    Defina o intervalo personalizado para sua reserva:
                  </p>
                  <div className="rp-time-grid">
                    <div>
                      <label className="rp-label" htmlFor="rp-custom-start">
                        Início
                      </label>
                      <input
                        id="rp-custom-start"
                        type="time"
                        className="rp-input"
                        value={customStartTime}
                        onChange={(e) => setCustomStartTime(e.target.value)}
                      />
                    </div>
                    <div>
                      <label className="rp-label" htmlFor="rp-custom-end">
                        Término
                      </label>
                      <input
                        id="rp-custom-end"
                        type="time"
                        className="rp-input"
                        value={customEndTime}
                        onChange={(e) => setCustomEndTime(e.target.value)}
                      />
                    </div>
                  </div>
                  <div className="rp-info-box">
                    <span>ℹ️</span>
                    <p style={{ margin: 0 }}>
                      Você está reservando por <strong style={{ color: "#fff" }}>{durationLabel}</strong> no dia{" "}
                      <strong style={{ color: "#fff" }}>{formatFilterDateLabel(filterDate)}</strong>.
                    </p>
                  </div>
                  <p className="rp-label" style={{ marginTop: 16 }}>
                    Sugestões rápidas
                  </p>
                  <div className="rp-quick-btns">
                    <button
                      type="button"
                      className="rp-quick-btn"
                      onClick={() => {
                        setCustomStartTime("09:00");
                        setCustomEndTime("12:00");
                      }}
                    >
                      Manhã (09:00 - 12:00)
                    </button>
                    <button
                      type="button"
                      className="rp-quick-btn"
                      onClick={() => {
                        setCustomStartTime("14:00");
                        setCustomEndTime("18:00");
                      }}
                    >
                      Tarde (14:00 - 18:00)
                    </button>
                    <button
                      type="button"
                      className="rp-quick-btn"
                      onClick={() => {
                        setCustomStartTime("09:00");
                        setCustomEndTime("18:00");
                      }}
                    >
                      Dia inteiro
                    </button>
                  </div>
                </>
              ) : null}

              {currentStep === 3 ? (
                <>
                  <p style={{ color: "var(--rp-muted)", fontSize: 13 }}>
                    Escolha sua posição em <strong style={{ color: "#fff" }}>{selectedRoom.name}</strong>:
                  </p>
                  <div className="rp-floor-plan">
                    <span className="rp-floor-plan-label">Planta de referência</span>
                  </div>
                  <div
                    style={{
                      padding: 20,
                      borderRadius: 16,
                      border: "1px solid var(--rp-border)",
                      background: "rgba(6,11,22,0.6)",
                    }}
                  >
                    <div
                      style={{
                        display: "flex",
                        justifyContent: "space-between",
                        marginBottom: 12,
                        fontSize: 11,
                        color: "var(--rp-muted)",
                        textTransform: "uppercase",
                        fontWeight: 700,
                      }}
                    >
                      <span>Selecione a estação</span>
                      <span>Livre · Sua seleção</span>
                    </div>
                    <div className="rp-seats-grid">
                      {selectedRoom.positions.map((pos) => (
                        <button
                          key={pos.id}
                          type="button"
                          className={`rp-seat ${
                            selectedPosition?.id === pos.id
                              ? "rp-seat--selected"
                              : pos.occupied
                                ? "rp-seat--busy"
                                : ""
                          }`}
                          disabled={pos.occupied}
                          onClick={() => setSelectedPosition(pos)}
                        >
                          {pos.name}
                        </button>
                      ))}
                    </div>
                  </div>
                </>
              ) : null}

              {currentStep === 4 ? (
                <div className="rp-summary">
                  <div className="rp-summary-row">
                    <span>Sala</span>
                    <strong>{selectedRoom.name}</strong>
                  </div>
                  <div className="rp-summary-row">
                    <span>Estação</span>
                    <strong style={{ color: "var(--rp-highlight)" }}>
                      {selectedPosition?.name ?? "—"}
                    </strong>
                  </div>
                  <div className="rp-summary-row">
                    <span>Data</span>
                    <strong>{formatFilterDateLabel(filterDate)}</strong>
                  </div>
                  <div className="rp-summary-row">
                    <span>Horário</span>
                    <strong>
                      {customStartTime} até {customEndTime}
                    </strong>
                  </div>
                  <div className="rp-summary-row">
                    <span>Duração total</span>
                    <strong style={{ color: "var(--rp-highlight)" }}>{durationLabel}</strong>
                  </div>
                  <div className="rp-summary-row">
                    <span>Recursos</span>
                    <span>
                      {selectedRoom.equipments.map((e) => getEquipIcon(e)).join(" ")}
                    </span>
                  </div>
                </div>
              ) : null}
            </div>
            <div className="rp-modal-foot">
              <button
                type="button"
                className="rp-btn-ghost"
                onClick={() =>
                  currentStep > 1 ? setCurrentStep((s) => s - 1) : closeModal()
                }
              >
                Voltar
              </button>
              <button
                type="button"
                className="rp-btn-highlight"
                disabled={
                  isBooking || (currentStep === 3 && !selectedPosition)
                }
                onClick={nextStep}
              >
                {isBooking
                  ? "Confirmando…"
                  : currentStep === 4
                    ? "Confirmar reserva"
                    : "Continuar →"}
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {showSuccess && selectedPosition ? (
        <div className="rp-toast" role="status">
          <span style={{ fontSize: 20 }}>✓</span>
          <div>
            <strong>Reserva confirmada!</strong>
            <p style={{ margin: "2px 0 0", fontSize: 12, opacity: 0.9 }}>
              Estação {selectedPosition.name} reservada com sucesso.
            </p>
          </div>
        </div>
      ) : null}
    </div>
  );
}

function RoomCard({
  room,
  isAdmin,
  onReserve,
  onBlock,
  onUnblock,
}: {
  room: BookingRoom;
  isAdmin: boolean;
  onReserve: () => void;
  onBlock?: (room: BookingRoom) => void;
  onUnblock?: (room: BookingRoom) => void;
}) {
  const free = availableCount(room);
  const total = room.positions.length;
  const occupied = total - free;
  const pct =
    total > 0 ? Math.round((occupied / total) * 100) : 0;

  return (
    <article className="rp-card">
      <div className="rp-card-head">
        <div style={{ display: "flex", gap: 14 }}>
          <div className="rp-card-icon">{room.icon}</div>
          <div>
            <h3 className="rp-card-title">{room.name}</h3>
            <p className="rp-card-loc">📍 {room.location}</p>
          </div>
        </div>
        <span className={roomAvailabilityClass(free)}>{free} livres</span>
      </div>
      <div className="rp-stats-row">
        <div className="rp-stat-box">
          <span>Total posições</span>
          <strong>{total}</strong>
        </div>
        <div className="rp-stat-box">
          <span>Ocupadas</span>
          <strong>{occupied}</strong>
        </div>
      </div>
      {room.equipments.length > 0 ? (
        <div>
          <span className="rp-label">Recursos na sala</span>
          <div className="rp-equip-row">
            {room.equipments.map((e) => (
              <span key={e} className="rp-equip-icon" title={e}>
                {getEquipIcon(e)}
              </span>
            ))}
          </div>
        </div>
      ) : null}
      {total > 0 ? (
        <div className="rp-occupancy">
          <div className="rp-occupancy-labels">
            <span>Ocupação da sala</span>
            <span style={{ color: "var(--rp-highlight)" }}>{pct}% ocupado</span>
          </div>
          <div className="rp-occupancy-bar">
            {room.positions.map((pos) => (
              <div
                key={pos.id}
                className={
                  pos.occupied
                    ? "rp-occupancy-seg--busy"
                    : "rp-occupancy-seg--free"
                }
                style={{ width: `${100 / total}%` }}
              />
            ))}
          </div>
        </div>
      ) : null}
      <div className="rp-card-cta">
        <button type="button" className="rp-btn-primary" onClick={onReserve}>
          Selecionar posição
        </button>
        {isAdmin ? (
          <div className="rp-admin-actions">
            {onUnblock ? (
              <button
                type="button"
                className="rp-admin-btn"
                onClick={() => onUnblock(room)}
              >
                Desbloquear sala
              </button>
            ) : null}
            {onBlock ? (
              <button
                type="button"
                className="rp-admin-btn rp-admin-btn--danger"
                onClick={() => onBlock(room)}
              >
                Desativar sala
              </button>
            ) : null}
          </div>
        ) : null}
      </div>
    </article>
  );
}
