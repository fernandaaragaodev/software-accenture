import { useState } from "react";
import type { SessionUser } from "../types/officehub";

const DEMO_ROWS: [string, string, string][] = [
  ["admin/admin", "Administrador", "role-admin"],
  ["gestor/gestor", "Gestor", "role-manager"],
  ["func/func", "Funcionário", "role-employee"],
];

interface LoginPageProps {
  onLogin: (user: SessionUser) => void;
}

export function LoginPage({ onLogin }: LoginPageProps) {
  const [form, setForm] = useState({ login: "", password: "" });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  function handleSubmit() {
    if (!form.login || !form.password) {
      setError("Preencha todos os campos.");
      return;
    }
    setLoading(true);
    setTimeout(() => {
      if (form.login === "admin" && form.password === "admin") {
        onLogin({ name: "Rafael Torres", role: "admin", avatar: "RT" });
      } else if (form.login === "gestor" && form.password === "gestor") {
        onLogin({ name: "Maria Souza", role: "manager", avatar: "MS" });
      } else if (form.login === "func" && form.password === "func") {
        onLogin({ name: "Carlos Lima", role: "employee", avatar: "CL" });
      } else {
        setError(
          "Credenciais inválidas. Tente: admin/admin, gestor/gestor, func/func",
        );
        setLoading(false);
      }
    }, 900);
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
              placeholder="seu.login"
              value={form.login}
              onChange={(e) => {
                setForm((f) => ({ ...f, login: e.target.value }));
                setError("");
              }}
              onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
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
              onKeyDown={(e) => e.key === "Enter" && handleSubmit()}
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
            onClick={handleSubmit}
            disabled={loading}
          >
            {loading ? "Autenticando..." : "Entrar"}
          </button>
          <div className="login-divider">
            <span>Credenciais de demonstração</span>
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
