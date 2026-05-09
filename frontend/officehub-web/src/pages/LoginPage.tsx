import { useState } from "react";
import type { SessionUser, UserRole } from "../types/officehub";

const DEMO_ROWS: [string, string, string][] = [
  ["admin/admin", "Administrador", "role-admin"],
  ["gestor/gestor", "Gestor", "role-manager"],
  ["func/func", "Funcionário", "role-employee"],
];

/** Base da API; em dev o Vite faz proxy de `/auth` para `http://localhost:8080`. */
const API_BASE = import.meta.env.VITE_API_URL ?? "";

interface LoginPageProps {
  onLogin: (user: SessionUser) => void;
}

/** Converte logins curtos de demo para o e-mail seed no backend. */
function resolveLoginEmail(login: string): string {
  const t = login.trim();
  if (t.includes("@")) {
    return t;
  }
  const map: Record<string, string> = {
    admin: "admin@officehub.local",
    gestor: "gestor@officehub.local",
    func: "func@officehub.local",
  };
  return map[t] ?? t;
}

function isUserRole(value: string): value is UserRole {
  return value === "admin" || value === "manager" || value === "employee";
}

export function LoginPage({ onLogin }: LoginPageProps) {
  const [form, setForm] = useState({ login: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  async function handleSubmit() {
    if (!form.login || !form.password) {
      setError("Preencha todos os campos.");
      return;
    }
    setLoading(true);
    setError("");
    try {
      const res = await fetch(`${API_BASE}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json", Accept: "application/json" },
        body: JSON.stringify({
          email: resolveLoginEmail(form.login),
          password: form.password,
        }),
      });
      if (!res.ok) {
        setError(
          res.status === 401
            ? "Credenciais inválidas. Verifique login e senha."
            : `Erro ao entrar (${res.status}).`,
        );
        setLoading(false);
        return;
      }
      const data: unknown = await res.json();
      if (
        typeof data !== "object" ||
        data === null ||
        !("token" in data) ||
        !("name" in data) ||
        !("role" in data)
      ) {
        setError("Resposta inválida do servidor.");
        setLoading(false);
        return;
      }
      const token = (data as { token: unknown }).token;
      const name = (data as { name: unknown }).name;
      const roleRaw = (data as { role: unknown }).role;
      const avatar = (data as { avatar?: unknown }).avatar;
      if (
        typeof token !== "string" ||
        typeof name !== "string" ||
        typeof roleRaw !== "string"
      ) {
        setError("Resposta inválida do servidor.");
        setLoading(false);
        return;
      }
      if (!isUserRole(roleRaw)) {
        setError("Perfil de usuário não reconhecido.");
        setLoading(false);
        return;
      }
      onLogin({
        name,
        role: roleRaw,
        avatar: typeof avatar === "string" ? avatar : undefined,
        token,
      });
    } catch {
      setError(
        "Não foi possível conectar ao servidor. Inicie o backend (porta 8080) ou defina VITE_API_URL.",
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-screen">
      <div className="login-box">
        <div className="login-logo">
          <div className="login-logo-mark">
            Space<span>Manager</span>
          </div>
          <div
            style={{
              fontSize: 12,
              color: "var(--text3)",
              marginTop: 6,
            }}
          >
            Gestão inteligente de espaços corporativos
          </div>
        </div>
        <div className="login-card">
          <div className="login-title">Entrar na plataforma</div>
          <div className="login-sub">
            Informe suas credenciais para continuar.
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="login-user">
              Login
            </label>
            <input
              id="login-user"
              className="form-input"
              placeholder="admin ou e-mail"
              value={form.login}
              onChange={(e) => {
                setForm((f) => ({ ...f, login: e.target.value }));
                setError("");
              }}
              onKeyDown={(e) => e.key === "Enter" && void handleSubmit()}
            />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="login-pass">
              Senha
            </label>
            <input
              id="login-pass"
              className="form-input"
              type="password"
              placeholder="••••••••"
              value={form.password}
              onChange={(e) => {
                setForm((f) => ({ ...f, password: e.target.value }));
                setError("");
              }}
              onKeyDown={(e) => e.key === "Enter" && void handleSubmit()}
            />
          </div>
          {error ? (
            <div
              style={{
                fontSize: 12,
                color: "var(--red)",
                marginBottom: 12,
                background: "rgba(255,77,109,0.08)",
                borderRadius: 8,
                padding: "8px 12px",
              }}
            >
              {error}
            </div>
          ) : null}
          <button
            type="button"
            className="btn btn-primary w-full"
            style={{ justifyContent: "center" }}
            onClick={() => void handleSubmit()}
            disabled={loading}
          >
            {loading ? "Autenticando..." : "Entrar"}
          </button>
          <div className="login-divider">
            <span>Credenciais de demonstração (API)</span>
          </div>
          <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
            {DEMO_ROWS.map(([cred, label, cls]) => (
              <button
                key={cred}
                type="button"
                className="btn btn-ghost btn-sm"
                style={{ flex: 1 }}
                onClick={() => {
                  const [l, p] = cred.split("/");
                  setForm({ login: l, password: p });
                  setError("");
                }}
              >
                <span className={`role-pill ${cls}`}>{label}</span>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
