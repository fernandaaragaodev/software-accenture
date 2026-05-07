import { useState } from "react";
import { StatusBadge } from "../components/StatusBadge";
import { MOCK_RESERVATIONS } from "../data/mock";
import type { Reservation } from "../types/officehub";

const EXTRA: Reservation[] = [
  {
    id: 10,
    room: "Sala Cronos",
    user: "Pedro Alves",
    date: "2025-05-28",
    start: "13:00",
    end: "15:00",
    status: "confirmed",
  },
  {
    id: 11,
    room: "Sala Zeus",
    user: "Julia Costa",
    date: "2025-05-27",
    start: "09:00",
    end: "10:30",
    status: "confirmed",
  },
  {
    id: 12,
    room: "Sala Apolo",
    user: "Carlos Lima",
    date: "2025-05-25",
    start: "11:00",
    end: "12:00",
    status: "cancelled",
  },
];

export function HistoryPage() {
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");

  const all = [...MOCK_RESERVATIONS, ...EXTRA];

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
            <button type="button" className="btn btn-primary">
              Buscar
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
              {[...all]
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
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
