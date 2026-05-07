import { useState } from "react";
import { Modal } from "../components/Modal";
import { RolePill } from "../components/RolePill";
import { StatusBadge } from "../components/StatusBadge";
import { MOCK_USERS } from "../data/mock";
import type { DirectoryUser, UserRole } from "../types/officehub";

interface UserFormState {
  name: string;
  email: string;
  login: string;
  password: string;
  role: UserRole;
}

export function UsersPage() {
  const [users, setUsers] = useState<DirectoryUser[]>(MOCK_USERS);
  const [modal, setModal] = useState(false);
  const [form, setForm] = useState<UserFormState>({
    name: "",
    email: "",
    login: "",
    password: "",
    role: "employee",
  });

  function addUser() {
    if (!form.name || !form.email) return;
    setUsers((prev) => [
      {
        id: Date.now(),
        name: form.name,
        email: form.email,
        login: form.login,
        role: form.role,
        status: "active",
      },
      ...prev,
    ]);
    setModal(false);
    setForm({
      name: "",
      email: "",
      login: "",
      password: "",
      role: "employee",
    });
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-24">
        <div>
          <div
            style={{ fontFamily: "var(--font-head)", fontSize: 18, fontWeight: 700 }}
          >
            Usuários
          </div>
          <div style={{ fontSize: 13, color: "var(--text3)", marginTop: 3 }}>
            Gerenciamento de acesso e perfis
          </div>
        </div>
        <button
          type="button"
          className="btn btn-primary"
          onClick={() => setModal(true)}
        >
          + Cadastrar usuário
        </button>
      </div>

      <div className="card">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Nome</th>
                <th>E-mail</th>
                <th>Login</th>
                <th>Perfil</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>
                    <div style={{ display: "flex", alignItems: "center", gap: 10 }}>
                      <div
                        className="avatar"
                        style={{ width: 28, height: 28, fontSize: 10 }}
                      >
                        {u.name
                          .split(" ")
                          .map((n) => n[0])
                          .join("")
                          .slice(0, 2)}
                      </div>
                      <span style={{ color: "var(--text1)", fontWeight: 500 }}>
                        {u.name}
                      </span>
                    </div>
                  </td>
                  <td style={{ fontFamily: "monospace", fontSize: 13 }}>
                    {u.email}
                  </td>
                  <td
                    style={{
                      fontFamily: "monospace",
                      fontSize: 13,
                      color: "var(--text3)",
                    }}
                  >
                    {u.login}
                  </td>
                  <td>
                    <RolePill role={u.role} />
                  </td>
                  <td>
                    <StatusBadge status={u.status} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <Modal
        open={modal}
        onClose={() => setModal(false)}
        title="Cadastrar usuário"
        subtitle="RF01 — Registro de novo usuário no sistema"
      >
        <div className="form-row">
          <div className="form-group">
            <label className="form-label" htmlFor="user-name">
              Nome completo *
            </label>
            <input
              id="user-name"
              className="form-input"
              placeholder="João Silva"
              value={form.name}
              onChange={(e) =>
                setForm((f) => ({ ...f, name: e.target.value }))
              }
            />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="user-email">
              E-mail *
            </label>
            <input
              id="user-email"
              className="form-input"
              type="email"
              placeholder="joao@empresa.com"
              value={form.email}
              onChange={(e) =>
                setForm((f) => ({ ...f, email: e.target.value }))
              }
            />
          </div>
        </div>
        <div className="form-row">
          <div className="form-group">
            <label className="form-label" htmlFor="user-login">
              Login
            </label>
            <input
              id="user-login"
              className="form-input"
              placeholder="jsilva"
              value={form.login}
              onChange={(e) =>
                setForm((f) => ({ ...f, login: e.target.value }))
              }
            />
          </div>
          <div className="form-group">
            <label className="form-label" htmlFor="user-pass">
              Senha temporária
            </label>
            <input
              id="user-pass"
              className="form-input"
              type="password"
              placeholder="••••••••"
              value={form.password}
              onChange={(e) =>
                setForm((f) => ({ ...f, password: e.target.value }))
              }
            />
          </div>
        </div>
        <div className="form-group">
          <label className="form-label" htmlFor="user-role">
            Perfil de acesso (RF03)
          </label>
          <select
            id="user-role"
            className="form-select"
            value={form.role}
            onChange={(e) =>
              setForm((f) => ({
                ...f,
                role: e.target.value as UserRole,
              }))
            }
          >
            <option value="employee">Funcionário — acesso básico</option>
            <option value="manager">Gestor — gerencia salas e relatórios</option>
            <option value="admin">Administrador — acesso total</option>
          </select>
        </div>
        <div
          style={{
            fontSize: 12,
            color: "var(--text3)",
            background: "var(--bg3)",
            borderRadius: 8,
            padding: "9px 13px",
          }}
        >
          🔒 O usuário receberá notificação por e-mail com as instruções de
          primeiro acesso.
        </div>
        <div className="modal-footer">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => setModal(false)}
          >
            Cancelar
          </button>
          <button type="button" className="btn btn-primary" onClick={addUser}>
            Cadastrar
          </button>
        </div>
      </Modal>
    </div>
  );
}
