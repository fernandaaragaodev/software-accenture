import { useEffect, useState } from "react";
import type { SessionUser } from "../types/officehub";
import {
  fetchWorkplaceContext,
  type WorkplaceContext,
} from "../services/workplaceService";

const DESKS_DATA: { name: string; status: string }[] = [
  { name: "M1", status: "desk-free" },
  { name: "M2", status: "desk-taken" },
  { name: "M3", status: "desk-free" },
  { name: "M4", status: "desk-reserved" },
  { name: "M5", status: "desk-free" },
  { name: "M6", status: "desk-taken" },
  { name: "M7", status: "desk-free" },
  { name: "M8", status: "desk-free" },
  { name: "M9", status: "desk-taken" },
  { name: "M10", status: "desk-free" },
  { name: "M11", status: "desk-reserved" },
  { name: "M12", status: "desk-free" },
];

const STATS: {
  label: string;
  value: string;
  change: string;
  color: string;
  icon: string;
}[] = [
  {
    label: "Salas cadastradas",
    value: "12",
    change: "+2 este mês",
    color: "blue",
    icon: "🏢",
  },
  {
    label: "Disponíveis agora",
    value: "7",
    change: "58% do total",
    color: "green",
    icon: "✅",
  },
  {
    label: "Reservas hoje",
    value: "23",
    change: "↑ 4 vs ontem",
    color: "amber",
    icon: "📅",
  },
  {
    label: "Ocupadas",
    value: "5",
    change: "42% do total",
    color: "red",
    icon: "🔴",
  },
];

const TIMELINE: {
  time: string;
  label: string;
  who: string;
  dur: string;
  color: string;
}[] = [
  {
    time: "09:00",
    label: "Sala Apolo",
    who: "Maria Souza",
    dur: "2h",
    color: "var(--accent)",
  },
  {
    time: "11:00",
    label: "Sala Cronos",
    who: "Pedro Alves",
    dur: "1h",
    color: "var(--purple)",
  },
  {
    time: "14:00",
    label: "Sala Hermes",
    who: "Carlos Lima",
    dur: "2h",
    color: "var(--green)",
  },
  {
    time: "16:00",
    label: "Sala Athena",
    who: "Ana Pereira",
    dur: "1.5h",
    color: "var(--amber)",
  },
];

const OCCUPANCY: { name: string; pct: number; cap: number }[] = [
  { name: "Sala Apolo", pct: 75, cap: 12 },
  { name: "Sala Hermes", pct: 100, cap: 6 },
  { name: "Sala Athena", pct: 40, cap: 20 },
  { name: "Sala Zeus", pct: 60, cap: 8 },
  { name: "Sala Poseidon", pct: 90, cap: 30 },
];

interface DashboardPageProps {
  user: SessionUser;
}

export function DashboardPage({ user }: DashboardPageProps) {
  const [ctx, setCtx] = useState<WorkplaceContext | null>(null);
  const [ctxErr, setCtxErr] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const data = await fetchWorkplaceContext(user.name);
        if (!cancelled) {
          setCtx(data);
          setCtxErr(null);
        }
      } catch (e) {
        if (!cancelled) {
          setCtxErr(
            e instanceof Error ? e.message : "Nao foi possivel carregar o contexto.",
          );
        }
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [user.name]);

  const todayLabel = new Date().toLocaleDateString("pt-BR", {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  });

  return (
    <div>
      <div className="mb-24">
        <div
          style={{
            fontSize: 22,
            fontFamily: "var(--font-head)",
            fontWeight: 800,
            marginBottom: 4,
          }}
        >
          Bom dia, {user.name.split(" ")[0]} 👋
        </div>
        <div style={{ fontSize: 13, color: "var(--text3)" }}>
          {todayLabel.charAt(0).toUpperCase() + todayLabel.slice(1)} — Visão geral
          dos espaços
        </div>
      </div>

      {ctxErr ? (
        <div
          style={{
            marginBottom: 16,
            fontSize: 12,
            color: "var(--red)",
            padding: "10px 12px",
            borderRadius: 8,
            border: "1px solid rgba(255,77,109,0.25)",
            background: "rgba(255,77,109,0.08)",
          }}
        >
          {ctxErr}
        </div>
      ) : null}

      {ctx ? (
        <div className="card mb-24">
          <div className="section-header">
            <span className="section-title">Seu contexto no escritório</span>
            <span className="chip" style={{ fontSize: 11 }}>
              {ctx.employeeRegistered ? "Cadastro local" : "Perfil padrão"}
            </span>
          </div>
          <div
            style={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fit, minmax(200px, 1fr))",
              gap: 16,
              fontSize: 13,
              color: "var(--text2)",
            }}
          >
            <div>
              <div
                style={{
                  fontSize: 11,
                  color: "var(--text3)",
                  textTransform: "uppercase",
                  letterSpacing: "0.04em",
                  marginBottom: 6,
                }}
              >
                Equipe
              </div>
              <div style={{ fontWeight: 600, color: "var(--text1)" }}>
                {ctx.teamName ?? "—"}
              </div>
              <div style={{ fontSize: 12, color: "var(--text3)", marginTop: 4 }}>
                Andar preferido: {ctx.teamPreferredFloor ?? "—"}
              </div>
            </div>
            <div>
              <div
                style={{
                  fontSize: 11,
                  color: "var(--text3)",
                  textTransform: "uppercase",
                  letterSpacing: "0.04em",
                  marginBottom: 6,
                }}
              >
                Perfil profissional
              </div>
              <div style={{ fontWeight: 600, color: "var(--text1)" }}>
                {ctx.profileLabel}
              </div>
              <div style={{ fontSize: 11, color: "var(--text3)", marginTop: 4 }}>
                {ctx.professionalProfile}
              </div>
            </div>
            <div>
              <div
                style={{
                  fontSize: 11,
                  color: "var(--text3)",
                  textTransform: "uppercase",
                  letterSpacing: "0.04em",
                  marginBottom: 6,
                }}
              >
                Colegas (presença visível)
              </div>
              {ctx.visibleColleagues.length === 0 ? (
                <span style={{ color: "var(--text3)" }}>Nenhum colega listado</span>
              ) : (
                <ul
                  style={{
                    margin: 0,
                    paddingLeft: 18,
                    lineHeight: 1.6,
                  }}
                >
                  {ctx.visibleColleagues.map((c) => (
                    <li key={c.name}>
                      <span style={{ color: "var(--text1)", fontWeight: 500 }}>
                        {c.name}
                      </span>
                      {c.typicalStartTime ? (
                        <span style={{ color: "var(--text3)" }}>
                          {" "}
                          · chegada típica {c.typicalStartTime}
                        </span>
                      ) : null}
                      <div style={{ fontSize: 11, color: "var(--text3)" }}>
                        {c.profileLabel}
                      </div>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          </div>
        </div>
      ) : null}

      <div className="stat-grid">
        {STATS.map((s) => (
          <div key={s.label} className={`stat-card ${s.color}`}>
            <div className="stat-icon">{s.icon}</div>
            <div className="stat-label">{s.label}</div>
            <div className={`stat-value ${s.color}`}>{s.value}</div>
            <div className="stat-change">{s.change}</div>
          </div>
        ))}
      </div>

      <div className="grid-2 mb-24">
        <div className="card">
          <div className="section-header">
            <span className="section-title">Próximas reservas</span>
            <button type="button" className="section-action">
              Ver todas →
            </button>
          </div>
          <div className="timeline">
            {TIMELINE.map((t) => (
              <div key={t.time} className="timeline-row">
                <div className="timeline-label">{t.time}</div>
                <div className="timeline-slots">
                  <div
                    className="slot slot-booked"
                    style={{
                      background: `${t.color}18`,
                      color: t.color,
                      borderColor: `${t.color}30`,
                    }}
                  >
                    <span
                      style={{
                        width: 6,
                        height: 6,
                        borderRadius: "50%",
                        background: t.color,
                        flexShrink: 0,
                      }}
                    />
                    {t.label} · {t.who} · {t.dur}
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="card">
          <div className="section-header">
            <span className="section-title">Mapa — Sala Apolo (2º andar)</span>
            <span className="ai-pulse">
              <span className="ai-dot" />
              Ao vivo
            </span>
          </div>
          <div className="floor-map">
            {DESKS_DATA.map((d) => (
              <div key={d.name} className={`floor-desk ${d.status}`}>
                <div className="floor-desk-num">{d.name}</div>
                <div style={{ fontSize: 9 }}>
                  {d.status === "desk-free"
                    ? "Livre"
                    : d.status === "desk-taken"
                      ? "Ocupada"
                      : "Reserv."}
                </div>
              </div>
            ))}
          </div>
          <div style={{ display: "flex", gap: 12, marginTop: 14 }}>
            {(
              [
                ["var(--green)", "Livre"],
                ["var(--red)", "Ocupada"],
                ["var(--amber)", "Reservada"],
              ] as const
            ).map(([c, l]) => (
              <div
                key={l}
                style={{
                  display: "flex",
                  alignItems: "center",
                  gap: 5,
                  fontSize: 11,
                  color: "var(--text3)",
                }}
              >
                <span
                  style={{
                    width: 8,
                    height: 8,
                    borderRadius: 2,
                    background: c,
                  }}
                />
                {l}
              </div>
            ))}
          </div>
        </div>
      </div>

      <div className="card">
        <div className="section-header">
          <span className="section-title">Ocupação por sala — hoje</span>
        </div>
        <div style={{ display: "flex", flexDirection: "column", gap: 12 }}>
          {OCCUPANCY.map((r) => (
            <div
              key={r.name}
              style={{ display: "flex", alignItems: "center", gap: 14 }}
            >
              <div
                style={{
                  width: 120,
                  fontSize: 13,
                  color: "var(--text2)",
                  flexShrink: 0,
                }}
              >
                {r.name}
              </div>
              <div className="progress-bar" style={{ flex: 1 }}>
                <div
                  className="progress-fill"
                  style={{
                    width: `${r.pct}%`,
                    background:
                      r.pct >= 90
                        ? "var(--red)"
                        : r.pct >= 60
                          ? "var(--amber)"
                          : "var(--green)",
                  }}
                />
              </div>
              <div
                style={{
                  width: 60,
                  fontSize: 12,
                  color: "var(--text3)",
                  textAlign: "right",
                }}
              >
                {r.pct}% · {r.cap} lug.
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
