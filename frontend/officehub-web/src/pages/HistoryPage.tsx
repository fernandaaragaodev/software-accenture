import { useEffect, useState } from "react";
import { StatusBadge } from "../components/StatusBadge";
import type { Reservation } from "../types/officehub";
import { fetchReservations } from "../services/reservationsService";

export function HistoryPage() {
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [reservations, setReservations] = useState<Reservation[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function loadHistory() {
    setLoading(true);
    try {
      const data = await fetchReservations();
      setReservations(data);
      setError(null);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Falha ao carregar o histórico do backend.",
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadHistory();
  }, []);

  const filtered = reservations.filter((r) => {
    if (dateFrom && r.date < dateFrom) return false;
    if (dateTo && r.date > dateTo) return false;
    return true;
  });

  return (
    <div>
      <div
        style={{
          fontFamily: "var(--font-head)",
          fontSize: 18,
          fontWeight: 700,
          marginBottom: 4,
        }}
      >
        Histórico de Reservas
      </div>
      <div style={{ fontSize: 13, color: "var(--text3)", marginBottom: 24 }}>
        RF14 — Registros passados de ocupação
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

      <div className="card mb-24">
        <div style={{ fontWeight: 600, fontSize: 13, marginBottom: 14 }}>
          Filtrar por período
        </div>
        <div className="flex gap-12 items-center" style={{ flexWrap: "wrap" }}>
          <div>
            <label className="form-label" htmlFor="hist-from">
              De
            </label>
            <input
              id="hist-from"
              className="form-input"
              type="date"
              style={{ width: 170 }}
              value={dateFrom}
              onChange={(e) => setDateFrom(e.target.value)}
            />
          </div>
          <div>
            <label className="form-label" htmlFor="hist-to">
              Até
            </label>
            <input
              id="hist-to"
              className="form-input"
              type="date"
              style={{ width: 170 }}
              value={dateTo}
              onChange={(e) => setDateTo(e.target.value)}
            />
          </div>
          <div style={{ alignSelf: "flex-end" }}>
            <button
              type="button"
              className="btn btn-primary"
              onClick={() => void loadHistory()}
              disabled={loading}
            >
              {loading ? "Buscando..." : "Buscar"}
            </button>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Sala</th>
                <th>Usuário</th>
                <th>Data</th>
                <th>Período</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {loading && reservations.length === 0 ? (
                <tr>
                  <td colSpan={6}>
                    <div className="empty-state">
                      <div className="empty-icon">⏳</div>
                      <div className="empty-text">Carregando histórico...</div>
                    </div>
                  </td>
                </tr>
              ) : null}
              {filtered
                .sort((a, b) => b.date.localeCompare(a.date))
                .map((r) => (
                  <tr key={r.id}>
                    <td
                      style={{
                        color: "var(--text3)",
                        fontFamily: "monospace",
                      }}
                    >
                      #{r.id}
                    </td>
                    <td style={{ fontWeight: 500, color: "var(--text1)" }}>
                      {r.room}
                    </td>
                    <td>{r.user}</td>
                    <td style={{ fontFamily: "monospace", fontSize: 13 }}>
                      {r.date}
                    </td>
                    <td style={{ fontFamily: "monospace", fontSize: 13 }}>
                      {r.start} – {r.end}
                    </td>
                    <td>
                      <StatusBadge status={r.status} />
                    </td>
                  </tr>
                ))}
              {!loading && filtered.length === 0 ? (
                <tr>
                  <td colSpan={6}>
                    <div className="empty-state">
                      <div className="empty-icon">📅</div>
                      <div className="empty-text">
                        Nenhum registro encontrado
                      </div>
                    </div>
                  </td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

