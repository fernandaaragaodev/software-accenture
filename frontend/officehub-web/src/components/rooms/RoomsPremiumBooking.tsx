import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import type { SessionUser } from "../../types/officehub";
import { createReservation } from "../../services/reservationsService";
import {
  fetchRoomPositions,
  fetchRoomPositionsOverview,
  fetchRooms,
  setPositionBlocked,
} from "../../services/roomsService";
import {
  availableCount,
  calculateDuration,
  formatFilterDateLabel,
  getEquipIcon,
  mapApiPositions,
  buildCalendarDays,
  clampReservationDate,
  isReservationDateAllowed,
  isRoomDeactivated,
  mapApiRoom,
  minReservationDateStr,
  isPositionCompatibleWithPreferences,
  isPositionSelectable,
  positionStatusLabel,
  resolveRequestedEquipmentForApi,
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
const STEP_LABELS_FROM_ROOM = [
  "Data",
  "Horário",
  "Preferências",
  "Posição",
  "Confirmação",
];
const STEP_LABELS_GLOBAL = [
  "Data",
  "Horário",
  "Preferências",
  "Sala",
  "Posição",
  "Confirmação",
];

function compatiblePositionsCount(
  room: BookingRoom,
  preferences: string[],
): number {
  return room.positions.filter(
    (p) =>
      isPositionSelectable(p) &&
      isPositionCompatibleWithPreferences(p, preferences, room.equipments),
  ).length;
}

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
  const [startedFromGlobal, setStartedFromGlobal] = useState(false);
  const [modalSelectedResources, setModalSelectedResources] = useState<string[]>(
    [],
  );
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
  const [positionManageRoom, setPositionManageRoom] =
    useState<BookingRoom | null>(null);
  const [managePositions, setManagePositions] = useState<BookingPosition[]>([]);
  const [manageLoading, setManageLoading] = useState(false);
  const [manageError, setManageError] = useState<string | null>(null);
  const [blockingPositionCode, setBlockingPositionCode] = useState<string | null>(
    null,
  );
  const [resourcesFilterOpen, setResourcesFilterOpen] = useState(false);
  const resourcesFilterRef = useRef<HTMLDivElement>(null);

  const allResources = useMemo(
    () => [...new Set(rooms.flatMap((r) => r.equipments))].sort(),
    [rooms],
  );

  const loadRoomPositions = useCallback(
    async (
      room: BookingRoom,
      date = filterDate,
      start = filterStartTime,
      end = filterEndTime,
    ) => {
      if (!date) return;
      try {
        const data = await fetchRoomPositions(room.id, date, start, end);
        room.positions = mapApiPositions(data, room.id, room.equipments);
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

  useEffect(() => {
    if (!resourcesFilterOpen) return;
    function handlePointerDown(event: MouseEvent) {
      if (
        resourcesFilterRef.current &&
        !resourcesFilterRef.current.contains(event.target as Node)
      ) {
        setResourcesFilterOpen(false);
      }
    }
    function handleEscape(event: KeyboardEvent) {
      if (event.key === "Escape") setResourcesFilterOpen(false);
    }
    document.addEventListener("mousedown", handlePointerDown);
    document.addEventListener("keydown", handleEscape);
    return () => {
      document.removeEventListener("mousedown", handlePointerDown);
      document.removeEventListener("keydown", handleEscape);
    };
  }, [resourcesFilterOpen]);

  const filteredRooms = useMemo(() => {
    return rooms.filter((room) => {
      const q = searchQuery.toLowerCase();
      const matchesSearch =
        room.name.toLowerCase().includes(q) ||
        room.location.toLowerCase().includes(q);
      const matchesResources = selectedResources.every((res) =>
        room.equipments.includes(res),
      );
      return matchesSearch && matchesResources;
    });
  }, [rooms, searchQuery, selectedResources]);

  const bookableRooms = useMemo(
    () => filteredRooms.filter((room) => !isRoomDeactivated(room)),
    [filteredRooms],
  );

  const totalAvailable = useMemo(
    () =>
      bookableRooms.reduce((acc, room) => acc + availableCount(room), 0),
    [bookableRooms],
  );

  const stepLabels = startedFromGlobal
    ? STEP_LABELS_GLOBAL
    : STEP_LABELS_FROM_ROOM;
  const maxStep = stepLabels.length;
  const positionStep = startedFromGlobal ? 5 : 4;
  const confirmationStep = startedFromGlobal ? 6 : 5;
  const roomPickStep = 4;

  const roomsWithCompatiblePositions = useMemo(() => {
    return bookableRooms.filter(
      (room) => compatiblePositionsCount(room, modalSelectedResources) > 0,
    );
  }, [bookableRooms, modalSelectedResources]);

  const filteredPositions = useMemo(() => {
    if (!selectedRoom) return [];
    return selectedRoom.positions.filter(
      (p) =>
        isPositionSelectable(p) &&
        isPositionCompatibleWithPreferences(
          p,
          modalSelectedResources,
          selectedRoom.equipments,
        ),
    );
  }, [selectedRoom, modalSelectedResources]);

  async function loadManagePositions(room: BookingRoom) {
    setManageLoading(true);
    setManageError(null);
    try {
      const overview = await fetchRoomPositionsOverview(room.id);
      setManagePositions(mapApiPositions(overview, room.id, room.equipments));
    } catch (err) {
      setManageError(
        err instanceof Error ? err.message : "Falha ao carregar estações.",
      );
      setManagePositions([]);
    } finally {
      setManageLoading(false);
    }
  }

  async function openPositionManage(room: BookingRoom) {
    setPositionManageRoom(room);
    await loadManagePositions(room);
  }

  function closePositionManage() {
    setPositionManageRoom(null);
    setManagePositions([]);
    setManageError(null);
    setBlockingPositionCode(null);
  }

  async function togglePositionBlock(pos: BookingPosition, block: boolean) {
    if (!positionManageRoom) return;
    setBlockingPositionCode(pos.name);
    setManageError(null);
    try {
      await setPositionBlocked(
        positionManageRoom.id,
        pos.name,
        block,
        user.role,
      );
      await loadManagePositions(positionManageRoom);
      await loadRooms();
    } catch (err) {
      setManageError(
        err instanceof Error ? err.message : "Não foi possível atualizar a estação.",
      );
    } finally {
      setBlockingPositionCode(null);
    }
  }

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
    setResourcesFilterOpen(false);
  }

  function toggleModalResource(resource: string) {
    setModalSelectedResources((prev) =>
      prev.includes(resource)
        ? prev.filter((r) => r !== resource)
        : [...prev, resource],
    );
  }

  async function refreshSelectedRoomPositions(
    room: BookingRoom,
    start = customStartTime,
    end = customEndTime,
  ) {
    const copy = { ...room, positions: [...room.positions] };
    await loadRoomPositions(copy, filterDate, start, end);
    setSelectedRoom(copy);
    return copy;
  }

  async function openModal(room: BookingRoom | null = null) {
    if (room && isRoomDeactivated(room)) return;
    if (!room && bookableRooms.length === 0) return;
    setStartedFromGlobal(!room);
    setModalSelectedResources([...selectedResources]);
    setCustomStartTime(filterStartTime);
    setCustomEndTime(filterEndTime);
    const clampedDate = clampReservationDate(filterDate);
    setFilterDate(clampedDate);
    const day = parseInt(clampedDate.split("-")[2] ?? "", 10);
    setSelectedCalendarDay(Number.isNaN(day) ? null : day);
    setSelectedPosition(null);
    setCurrentStep(1);
    if (room) {
      await refreshSelectedRoomPositions(room);
    } else {
      setSelectedRoom(null);
    }
    setIsModalOpen(true);
  }

  async function selectRoomInModal(room: BookingRoom) {
    await refreshSelectedRoomPositions(room);
    setSelectedPosition(null);
    setCurrentStep((s) => s + 1);
  }

  function closeModal() {
    setIsModalOpen(false);
    setCurrentStep(1);
    setSelectedPosition(null);
    setStartedFromGlobal(false);
    setModalSelectedResources([]);
  }

  async function confirmBooking() {
    if (!selectedRoom || !selectedPosition) return;
    if (isRoomDeactivated(selectedRoom)) {
      setError("Esta sala está desativada e não aceita reservas.");
      return;
    }
    if (!isReservationDateAllowed(filterDate)) {
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
        requestedEquipment: resolveRequestedEquipmentForApi(
          selectedPosition,
          modalSelectedResources,
        ),
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

  async function nextStep() {
    if (currentStep === 1 && !isReservationDateAllowed(filterDate)) {
      setError("A data da reserva deve ser pelo menos 7 dias à frente.");
      return;
    }
    setError(null);
    if (currentStep < maxStep) {
      const next = currentStep + 1;
      if (next === positionStep && selectedRoom) {
        await refreshSelectedRoomPositions(selectedRoom);
      }
      setCurrentStep(next);
      return;
    }
    void confirmBooking();
  }

  function isNextDisabled(): boolean {
    if (isBooking) return true;
    if (currentStep === 1) {
      return !isReservationDateAllowed(filterDate) || selectedCalendarDay === null;
    }
    if (currentStep === positionStep) return !selectedPosition;
    if (startedFromGlobal && currentStep === roomPickStep) return !selectedRoom;
    return false;
  }

  const calendarDays = useMemo(() => {
    const [y, m] = filterDate.split("-").map(Number);
    return buildCalendarDays({ year: y, month: m });
  }, [filterDate]);

  function handleFilterDateChange(value: string) {
    const clamped = clampReservationDate(value);
    setFilterDate(clamped);
    const day = parseInt(clamped.split("-")[2] ?? "", 10);
    setSelectedCalendarDay(Number.isNaN(day) ? null : day);
  }

  function selectCalendarDay(day: number, iso: string, disabled: boolean) {
    if (disabled) return;
    setSelectedCalendarDay(day);
    setFilterDate(iso);
    setError(null);
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
          disabled={bookableRooms.length === 0}
        >
          + Nova Reserva
        </button>
      </div>

      {error ? <div className="rp-alert">{error}</div> : null}

      <section
        className={`rp-filters ${resourcesFilterOpen ? "rp-filters--popover-open" : ""}`}
      >
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
            min={minReservationDateStr()}
            value={filterDate}
            onChange={(e) => handleFilterDateChange(e.target.value)}
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
        <div className="rp-field rp-field--filter" ref={resourcesFilterRef}>
          <span className="rp-label" id="rp-resources-label">
            Recursos da posição
          </span>
          <button
            type="button"
            className={`rp-filter-trigger ${resourcesFilterOpen ? "rp-filter-trigger--open" : ""}`}
            aria-haspopup="listbox"
            aria-expanded={resourcesFilterOpen}
            aria-labelledby="rp-resources-label"
            onClick={() => setResourcesFilterOpen((open) => !open)}
          >
            <svg
              className="rp-filter-trigger-icon"
              width="14"
              height="14"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="2"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden
            >
              <polygon points="22 3 2 3 10 12.46 10 19 14 21 14 12.46 22 3" />
            </svg>
            <span>Filtrar</span>
            {selectedResources.length > 0 ? (
              <span className="rp-filter-badge">{selectedResources.length}</span>
            ) : null}
          </button>
          {resourcesFilterOpen ? (
            <div className="rp-filter-popover" role="listbox" aria-multiselectable="true">
              <p className="rp-filter-popover-title">Recursos necessários na sala</p>
              {allResources.length === 0 ? (
                <p className="rp-filter-popover-empty">Nenhum recurso cadastrado.</p>
              ) : (
                <ul className="rp-filter-options">
                  {allResources.map((resource) => {
                    const checked = selectedResources.includes(resource);
                    return (
                      <li key={resource}>
                        <label
                          className={`rp-filter-option ${checked ? "rp-filter-option--checked" : ""}`}
                        >
                          <input
                            type="checkbox"
                            checked={checked}
                            onChange={() => toggleResource(resource)}
                          />
                          <span className="rp-filter-option-icon" aria-hidden>
                            {getEquipIcon(resource)}
                          </span>
                          <span className="rp-filter-option-label">{resource}</span>
                        </label>
                      </li>
                    );
                  })}
                </ul>
              )}
              {selectedResources.length > 0 ? (
                <button
                  type="button"
                  className="rp-filter-popover-clear"
                  onClick={() => setSelectedResources([])}
                >
                  Limpar seleção
                </button>
              ) : null}
            </div>
          ) : null}
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
              onManagePositions={
                isAdmin ? () => void openPositionManage(room) : undefined
              }
              onBlock={onAdminBlockRoom}
              onUnblock={onAdminUnblockRoom}
            />
          ))}
        </div>
      ) : null}

      {!isLoading && viewMode === "list" ? (
        <div className="rp-list">
          {filteredRooms.map((room) => {
            const deactivated = isRoomDeactivated(room);
            const free = availableCount(room);
            const total = room.positions.length;
            return (
              <div
                key={room.id}
                className={`rp-card rp-card--list ${deactivated ? "rp-card--deactivated" : ""}`}
              >
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
                <div style={{ display: "flex", flexDirection: "column", gap: 8, alignItems: "flex-end" }}>
                  {deactivated ? (
                    <span className="rp-badge rp-badge--inactive">Desativada</span>
                  ) : (
                    <button
                      type="button"
                      className="rp-btn-primary"
                      style={{ width: "auto", minWidth: 140 }}
                      onClick={() => void openModal(room)}
                    >
                      Ver posições
                    </button>
                  )}
                  {isAdmin && !deactivated ? (
                    <button
                      type="button"
                      className="rp-admin-btn"
                      onClick={() => void openPositionManage(room)}
                    >
                      Gerenciar estações
                    </button>
                  ) : null}
                </div>
              </div>
            );
          })}
        </div>
      ) : null}

      {isModalOpen ? (
        <div className="rp-overlay" role="dialog" aria-modal="true">
          <div className="rp-modal">
            <div className="rp-modal-head">
              <div>
                <h2 style={{ margin: 0, color: "#fff" }}>Reserva de estação</h2>
                <p style={{ margin: "4px 0 0", color: "var(--rp-muted)", fontSize: 13 }}>
                  {selectedRoom?.name ?? "Selecione uma sala"}
                </p>
              </div>
              <button type="button" className="rp-btn-ghost" onClick={closeModal}>
                ✕
              </button>
            </div>
            <div className="rp-modal-body">
              <div
                className={`rp-stepper ${stepLabels.length > 5 ? "rp-stepper--many" : ""}`}
              >
                {stepLabels.map((label, i) => {
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
                  {calendarDays.map(({ day, iso, disabled }) => (
                    <button
                      key={iso}
                      type="button"
                      className={`rp-cal-day ${selectedCalendarDay === day ? "rp-cal-day--selected" : ""}`}
                      disabled={disabled}
                      onClick={() => selectCalendarDay(day, iso, disabled)}
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
                  <p style={{ color: "var(--rp-muted)", fontSize: 13, marginBottom: 16 }}>
                    Quais recursos sua estação de trabalho deve ter?
                  </p>
                  <div className="rp-pref-grid">
                    {allResources.map((resource) => {
                      const active = modalSelectedResources.includes(resource);
                      return (
                        <button
                          key={resource}
                          type="button"
                          className={`rp-pref-card ${active ? "rp-pref-card--active" : ""}`}
                          onClick={() => toggleModalResource(resource)}
                        >
                          <span className="rp-pref-card-main">
                            <span>{getEquipIcon(resource)}</span>
                            <span style={{ fontSize: 13, fontWeight: 600 }}>
                              {resource}
                            </span>
                          </span>
                          <span className="rp-pref-check">{active ? "✓" : ""}</span>
                        </button>
                      );
                    })}
                  </div>
                </>
              ) : null}

              {startedFromGlobal && currentStep === roomPickStep ? (
                <>
                  <p style={{ color: "var(--rp-muted)", fontSize: 13, marginBottom: 16 }}>
                    Selecione uma sala que atenda às suas necessidades:
                  </p>
                  <div className="rp-room-pick-list">
                    {roomsWithCompatiblePositions.length === 0 ? (
                      <p style={{ color: "var(--rp-muted)", fontSize: 13 }}>
                        Nenhuma sala com estações compatíveis com suas preferências.
                        Ajuste os recursos ou limpe os filtros.
                      </p>
                    ) : (
                      roomsWithCompatiblePositions.map((room) => (
                        <button
                          key={room.id}
                          type="button"
                          className={`rp-room-pick ${selectedRoom?.id === room.id ? "rp-room-pick--active" : ""}`}
                          onClick={() => void selectRoomInModal(room)}
                        >
                          <span style={{ display: "flex", alignItems: "center", gap: 12 }}>
                            <span className="rp-card-icon">{room.icon}</span>
                            <span>
                              <strong style={{ color: "#fff", fontSize: 14 }}>
                                {room.name}
                              </strong>
                              <br />
                              <span style={{ fontSize: 11, color: "var(--rp-muted)" }}>
                                {room.location}
                              </span>
                            </span>
                          </span>
                          <span style={{ textAlign: "right" }}>
                            <span className="rp-compat-badge">
                              {compatiblePositionsCount(room, modalSelectedResources)} livres
                            </span>
                            <br />
                            <span
                              style={{
                                fontSize: 8,
                                color: "var(--rp-muted)",
                                textTransform: "uppercase",
                              }}
                            >
                              Compatíveis
                            </span>
                          </span>
                        </button>
                      ))
                    )}
                  </div>
                </>
              ) : null}

              {currentStep === positionStep && selectedRoom ? (
                <>
                  <div
                    style={{
                      display: "flex",
                      justifyContent: "space-between",
                      alignItems: "center",
                      marginBottom: 12,
                      flexWrap: "wrap",
                      gap: 8,
                    }}
                  >
                    <p style={{ color: "var(--rp-muted)", fontSize: 13, margin: 0 }}>
                      Estações disponíveis em{" "}
                      <strong style={{ color: "#fff" }}>{selectedRoom.name}</strong>:
                    </p>
                    <span className="rp-compat-badge">
                      {filteredPositions.length} estações compatíveis
                    </span>
                  </div>
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
                      <span>Livre · Compatível · Sua seleção</span>
                    </div>
                    <div className="rp-seats-grid">
                      {selectedRoom.positions.map((pos) => {
                        const compatible = isPositionCompatibleWithPreferences(
                          pos,
                          modalSelectedResources,
                          selectedRoom.equipments,
                        );
                        const disabled =
                          pos.blocked || pos.occupied || !compatible;
                        return (
                          <button
                            key={pos.id}
                            type="button"
                            className={`rp-seat ${
                              selectedPosition?.id === pos.id
                                ? "rp-seat--selected"
                                : pos.blocked
                                  ? "rp-seat--blocked"
                                  : pos.occupied
                                    ? "rp-seat--busy"
                                    : !compatible
                                      ? "rp-seat--incompatible"
                                      : ""
                            }`}
                            disabled={disabled}
                            title={
                              pos.blocked
                                ? "Estação bloqueada pelo administrador"
                                : !compatible && isPositionSelectable(pos)
                                  ? "Não atende às preferências selecionadas"
                                  : undefined
                            }
                            onClick={() => setSelectedPosition(pos)}
                          >
                            {pos.name}
                            {!compatible && isPositionSelectable(pos) ? (
                              <span style={{ fontSize: 10, marginTop: 2 }}>⚠</span>
                            ) : null}
                          </button>
                        );
                      })}
                    </div>
                  </div>
                </>
              ) : null}

              {currentStep === confirmationStep && selectedRoom ? (
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
                    <span>Preferências</span>
                    <span style={{ display: "flex", flexWrap: "wrap", gap: 6, justifyContent: "flex-end" }}>
                      {modalSelectedResources.length > 0 ? (
                        modalSelectedResources.map((pref) => (
                          <span key={pref} className="rp-compat-badge">
                            {pref}
                          </span>
                        ))
                      ) : (
                        <strong style={{ color: "var(--rp-muted)" }}>Nenhuma</strong>
                      )}
                    </span>
                  </div>
                  <div className="rp-summary-row">
                    <span>Recursos da estação</span>
                    <span>
                      {(selectedPosition?.equipment ?? []).map((e) => (
                        <span key={e} title={e} style={{ marginLeft: 4 }}>
                          {getEquipIcon(e)}
                        </span>
                      ))}
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
                disabled={isNextDisabled()}
                onClick={() => void nextStep()}
              >
                {isBooking
                  ? "Confirmando…"
                  : currentStep === confirmationStep
                    ? "Confirmar reserva"
                    : "Continuar →"}
              </button>
            </div>
          </div>
        </div>
      ) : null}

      {positionManageRoom ? (
        <div className="rp-overlay" role="dialog" aria-modal="true">
          <div className="rp-modal" style={{ maxWidth: 720 }}>
            <div className="rp-modal-head">
              <div>
                <h2 style={{ margin: 0, color: "#fff" }}>Gerenciar estações</h2>
                <p style={{ margin: "4px 0 0", color: "var(--rp-muted)", fontSize: 13 }}>
                  {positionManageRoom.name} — bloqueie posições específicas
                </p>
              </div>
              <button type="button" className="rp-btn-ghost" onClick={closePositionManage}>
                ✕
              </button>
            </div>
            <div className="rp-modal-body">
              {manageError ? <div className="rp-alert">{manageError}</div> : null}
              {manageLoading ? (
                <p style={{ color: "var(--rp-muted)", fontSize: 13 }}>Carregando estações…</p>
              ) : managePositions.length === 0 ? (
                <p style={{ color: "var(--rp-muted)", fontSize: 13 }}>
                  Nenhuma estação cadastrada nesta sala.
                </p>
              ) : (
                <div className="rp-pos-manage-grid">
                  {managePositions.map((pos) => {
                    const status = positionStatusLabel(pos);
                    const statusClass =
                      pos.blocked
                        ? "rp-pos-status--blocked"
                        : pos.occupied
                          ? "rp-pos-status--busy"
                          : "rp-pos-status--free";
                    return (
                      <div
                        key={pos.id}
                        className={`rp-pos-manage-item ${pos.blocked ? "rp-pos-manage-item--blocked" : ""}`}
                      >
                        <div className="rp-pos-manage-head">
                          <div>
                            <div className="rp-pos-manage-code">{pos.name}</div>
                            <div className="rp-pos-manage-type">{pos.type}</div>
                          </div>
                          <span className={`rp-pos-status ${statusClass}`}>{status}</span>
                        </div>
                        <button
                          type="button"
                          className={
                            pos.blocked ? "rp-admin-btn" : "rp-admin-btn rp-admin-btn--danger"
                          }
                          disabled={blockingPositionCode === pos.name}
                          onClick={() =>
                            void togglePositionBlock(pos, !pos.blocked)
                          }
                        >
                          {blockingPositionCode === pos.name
                            ? "Salvando…"
                            : pos.blocked
                              ? "Desbloquear estação"
                              : "Bloquear estação"}
                        </button>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>
            <div className="rp-modal-foot">
              <button type="button" className="rp-btn-ghost" onClick={closePositionManage}>
                Fechar
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
  onManagePositions,
  onBlock,
  onUnblock,
}: {
  room: BookingRoom;
  isAdmin: boolean;
  onReserve: () => void;
  onManagePositions?: () => void;
  onBlock?: (room: BookingRoom) => void;
  onUnblock?: (room: BookingRoom) => void;
}) {
  const deactivated = isRoomDeactivated(room);
  const free = deactivated ? 0 : availableCount(room);
  const total = room.positions.length;
  const occupied = total - free;
  const pct =
    total > 0 ? Math.round((occupied / total) * 100) : 0;

  return (
    <article className={`rp-card ${deactivated ? "rp-card--deactivated" : ""}`}>
      <div className="rp-card-head">
        <div style={{ display: "flex", gap: 14 }}>
          <div className="rp-card-icon">{room.icon}</div>
          <div>
            <h3 className="rp-card-title">{room.name}</h3>
            <p className="rp-card-loc">📍 {room.location}</p>
          </div>
        </div>
        {deactivated ? (
          <span className="rp-badge rp-badge--inactive">Desativada</span>
        ) : (
          <span className={roomAvailabilityClass(free)}>{free} livres</span>
        )}
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
                  pos.blocked
                    ? "rp-occupancy-seg--blocked"
                    : pos.occupied
                      ? "rp-occupancy-seg--busy"
                      : "rp-occupancy-seg--free"
                }
                style={{ width: `${100 / total}%` }}
                title={positionStatusLabel(pos)}
              />
            ))}
          </div>
        </div>
      ) : null}
      <div className="rp-card-cta">
        {deactivated ? (
          <button type="button" className="rp-btn-primary" disabled>
            Sala desativada
          </button>
        ) : (
          <button type="button" className="rp-btn-primary" onClick={onReserve}>
            Selecionar posição
          </button>
        )}
        {isAdmin ? (
          <div className="rp-admin-actions">
            {!deactivated && onManagePositions ? (
              <button
                type="button"
                className="rp-admin-btn"
                onClick={onManagePositions}
              >
                Gerenciar estações
              </button>
            ) : null}
            {deactivated && onUnblock ? (
              <button
                type="button"
                className="rp-admin-btn"
                onClick={() => onUnblock(room)}
              >
                Reativar sala
              </button>
            ) : null}
            {!deactivated && onBlock ? (
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
