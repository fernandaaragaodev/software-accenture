import { useState } from "react";

type HttpMethod = "GET" | "POST" | "DELETE" | "PATCH";

const ENDPOINTS: [HttpMethod, string, string][] = [
  [
    "GET",
    "/api/v1/workspace/context?userName=",
    "Contexto do colaborador (equipe, perfil profissional, colegas visíveis)",
  ],
  [
    "GET",
    "/api/v1/workspace/room-suggestions?userName=&date=YYYY-MM-DD&start=HH:mm&end=HH:mm&limit=5",
    "Sugestão de salas (score: disponibilidade no intervalo, andar da equipe, perfil, proximidade a colegas)",
  ],
  ["GET", "/api/v1/rooms", "Listar salas"],
  [
    "GET",
    "/api/v1/rooms/available-position-types?date=YYYY-MM-DD&start=HH:mm&end=HH:mm",
    "Tipos de posição com vagas no intervalo (antes de escolher a sala)",
  ],
  [
    "GET",
    "/api/v1/rooms/available?date=YYYY-MM-DD&start=HH:mm&end=HH:mm&seatType=",
    "Salas com posições livres do tipo indicado no intervalo",
  ],
  ["GET", "/api/v1/rooms/:id/status", "Status da sala"],
  ["GET", "/api/v1/rooms/:id/positions?date=YYYY-MM-DD&start=HH:mm&end=HH:mm", "Posições e disponibilidade no intervalo"],
  ["GET", "/api/v1/rooms/:id/positions/overview", "Visão geral das posições (bloqueio admin)"],
  [
    "POST",
    "/api/v1/rooms/:id/positions/:code/block?requesterRole=admin",
    "Bloquear posição específica (ex.: P1)",
  ],
  [
    "POST",
    "/api/v1/rooms/:id/positions/:code/unblock?requesterRole=admin",
    "Desbloquear posição específica",
  ],
  [
    "POST",
    "/api/v1/rooms/:id/block?requesterRole=admin",
    "Bloquear sala — corpo JSON: { \"adminPassword\": \"…\" }",
  ],
  ["POST", "/api/v1/rooms/:id/unblock?requesterRole=admin", "Desbloquear sala"],
  ["GET", "/api/v1/reservations", "Listar reservas"],
  ["GET", "/api/v1/reservations/groups", "Listar grupos de reserva"],
  ["GET", "/api/v1/reservations/groups/:groupId", "Detalhe de um grupo"],
  ["POST", "/api/v1/reservations", "Criar reserva — corpo JSON ReservationRequestDto"],
  ["POST", "/api/v1/reservations/batch", "Reserva em lote — corpo { \"reservations\": [ … ] }"],
  [
    "DELETE",
    "/api/v1/reservations/:id?requesterName=&requesterRole=",
    "Cancelar reserva individual",
  ],
  [
    "DELETE",
    "/api/v1/reservations/groups/:groupId?requesterName=&requesterRole=",
    "Cancelar grupo de reservas",
  ],
  [
    "GET",
    "/api/v1/notifications?viewerName=&viewerRole=",
    "Listar (admin: todas; gestor/funcionário: só ações próprias)",
  ],
  [
    "PATCH",
    "/api/v1/notifications/:id/read?viewerName=&viewerRole=",
    "Marcar uma notificação como lida",
  ],
  [
    "PATCH",
    "/api/v1/notifications/read-all?viewerName=&viewerRole=",
    "Marcar como lidas todas as visíveis ao viewer",
  ],
];

function methodStyle(method: HttpMethod): { bg: string; color: string } {
  switch (method) {
    case "GET":
      return { bg: "rgba(0,229,160,0.1)", color: "var(--green)" };
    case "POST":
      return { bg: "rgba(0,212,255,0.1)", color: "var(--accent)" };
    case "PATCH":
      return { bg: "rgba(180,160,255,0.12)", color: "#c4b5fd" };
    case "DELETE":
    default:
      return { bg: "rgba(255,77,109,0.1)", color: "var(--red)" };
  }
}

export function ApiPage() {
  const [key, setKey] = useState("sk-space-**************************3f2a");
  const [copied, setCopied] = useState(false);

  function copyKey() {
    setCopied(true);
    setTimeout(() => setCopied(false), 1800);
  }

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
        API & Integrações
      </div>
      <div style={{ fontSize: 13, color: "var(--text3)", marginBottom: 24 }}>
        RF16 — Chaves de acesso para sistemas externos
      </div>

      <div className="grid-2">
        <div className="card card-accent">
          <div
            style={{
              fontSize: 13,
              fontWeight: 600,
              marginBottom: 16,
              color: "var(--accent)",
            }}
          >
            🔑 Chave de API ativa
          </div>
          <div
            style={{
              background: "var(--bg1)",
              borderRadius: 8,
              padding: "12px 14px",
              fontFamily: "monospace",
              fontSize: 13,
              color: "var(--text2)",
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              marginBottom: 14,
            }}
          >
            <span>{key}</span>
            <button type="button" className="btn btn-ghost btn-sm" onClick={copyKey}>
              {copied ? "✅ Copiado" : "📋 Copiar"}
            </button>
          </div>
          <div style={{ display: "flex", gap: 8 }}>
            <button
              type="button"
              className="btn btn-ghost btn-sm"
              onClick={() =>
                setKey(
                  "sk-space-" + Math.random().toString(36).slice(2, 18) + "xxxx",
                )
              }
            >
              Regenerar chave
            </button>
            <button type="button" className="btn btn-danger btn-sm">
              Revogar
            </button>
          </div>
        </div>

        <div className="card">
          <div style={{ fontSize: 13, fontWeight: 600, marginBottom: 14 }}>
            📡 Endpoints disponíveis (base: /api/v1)
          </div>
          {ENDPOINTS.map(([method, path, desc], idx) => {
            const ms = methodStyle(method);
            return (
              <div
                key={`${method}-${idx}`}
                style={{
                  display: "flex",
                  alignItems: "flex-start",
                  gap: 10,
                  padding: "9px 0",
                  borderBottom: "1px solid var(--border)",
                }}
              >
                <span
                  style={{
                    fontSize: 10,
                    fontWeight: 700,
                    borderRadius: 4,
                    padding: "2px 7px",
                    background: ms.bg,
                    color: ms.color,
                    flexShrink: 0,
                    marginTop: 2,
                  }}
                >
                  {method}
                </span>
                <span
                  style={{
                    fontFamily: "monospace",
                    fontSize: 11,
                    color: "var(--accent)",
                    flex: 1,
                    lineHeight: 1.45,
                    wordBreak: "break-all",
                  }}
                >
                  {path}
                </span>
                <span style={{ fontSize: 11, color: "var(--text3)", maxWidth: "38%", flexShrink: 0 }}>
                  {desc}
                </span>
              </div>
            );
          })}
        </div>
      </div>
    </div>
  );
}
