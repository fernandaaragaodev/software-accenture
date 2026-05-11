import { useState } from "react";

const ENDPOINTS: [string, string, string][] = [
  ["GET", "/api/v1/rooms", "Lista todas as salas"],
  ["GET", "/api/v1/rooms/:id/status", "Status em tempo real"],
  [
    "POST",
    "/api/v1/rooms/:id/block?requesterRole=admin + JSON {\"adminPassword\":\"…\"}",
    "Desativar/bloquear sala (somente admin; senha em application.properties)",
  ],
  [
    "POST",
    "/api/v1/rooms/:id/unblock?requesterRole=admin",
    "Desbloquear sala (somente admin)",
  ],
  ["GET", "/api/v1/reservations", "Reservas ativas"],
  ["POST", "/api/v1/reservations", "Criar reserva"],
  [
    "DELETE",
    "/api/v1/reservations/:id?requesterName=&requesterRole=",
    "Cancelar reserva (próprio usuário ou admin)",
  ],
];

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
            📡 Endpoints disponíveis
          </div>
          {ENDPOINTS.map(([method, path, desc]) => (
            <div
              key={path}
              style={{
                display: "flex",
                alignItems: "center",
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
                  background:
                    method === "GET"
                      ? "rgba(0,229,160,0.1)"
                      : method === "POST"
                        ? "rgba(0,212,255,0.1)"
                        : "rgba(255,77,109,0.1)",
                  color:
                    method === "GET"
                      ? "var(--green)"
                      : method === "POST"
                        ? "var(--accent)"
                        : "var(--red)",
                  flexShrink: 0,
                }}
              >
                {method}
              </span>
              <span
                style={{
                  fontFamily: "monospace",
                  fontSize: 12,
                  color: "var(--accent)",
                  flex: 1,
                }}
              >
                {path}
              </span>
              <span style={{ fontSize: 11, color: "var(--text3)" }}>{desc}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
