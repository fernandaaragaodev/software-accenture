import { useCallback, useEffect, useState } from "react";
import {
  fetchManagerTeams,
  type ManagerTeam,
} from "../services/workplaceService";
import type { SessionUser } from "../types/officehub";
import "../styles/teams-page.css";

const BATCH_BOOKING_FLAG = "officehub:openBatchBooking";

export function setOpenBatchBookingFlag() {
  sessionStorage.setItem(BATCH_BOOKING_FLAG, "1");
}

export function consumeOpenBatchBookingFlag(): boolean {
  const value = sessionStorage.getItem(BATCH_BOOKING_FLAG);
  if (!value) return false;
  sessionStorage.removeItem(BATCH_BOOKING_FLAG);
  return true;
}

interface TeamsPageProps {
  user: SessionUser;
  onReserveForTeam?: () => void;
}

function teamIcon(name: string): string {
  const n = name.toLowerCase();
  if (n.includes("design")) return "🎨";
  if (n.includes("front") || n.includes("dev")) return "💻";
  if (n.includes("alpha") || n.includes("beta")) return "⚡";
  return "👥";
}

function memberInitials(name: string): string {
  return name
    .split(/\s+/)
    .map((p) => p[0])
    .join("")
    .toUpperCase()
    .slice(0, 2);
}

export function TeamsPage({ user, onReserveForTeam }: TeamsPageProps) {
  const [teams, setTeams] = useState<ManagerTeam[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expandedId, setExpandedId] = useState<number | null>(null);

  const loadTeams = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchManagerTeams(user.name, user.role);
      setTeams(data);
      if (data.length > 0) {
        setExpandedId((prev) => prev ?? data[0].id);
      }
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Falha ao carregar equipes.",
      );
      setTeams([]);
    } finally {
      setLoading(false);
    }
  }, [user.name, user.role]);

  useEffect(() => {
    void loadTeams();
  }, [loadTeams]);

  function handleReserveForTeam() {
    setOpenBatchBookingFlag();
    onReserveForTeam?.();
  }

  return (
    <div className="teams-page">
      <header className="teams-page-header">
        <div>
          <h2 className="teams-page-title">Minhas Equipes</h2>
          <p className="teams-page-subtitle">
            Gerencie os membros e reserve estações para sua squad
          </p>
        </div>
        <button
          type="button"
          className="teams-page-cta"
          disabled={teams.length === 0 || loading}
          onClick={handleReserveForTeam}
        >
          + Reservar para equipe
        </button>
      </header>

      {error ? <div className="teams-page-alert">{error}</div> : null}

      {loading ? (
        <p className="teams-page-muted">Carregando equipes…</p>
      ) : teams.length === 0 ? (
        <div className="teams-page-empty">
          <p>Nenhuma equipe vinculada ao seu perfil de gestor.</p>
          <p className="teams-page-muted">
            Cadastre-se no diretório de colaboradores (ex.: Maria Souza) para
            ver a squad.
          </p>
        </div>
      ) : (
        <div className="teams-page-grid">
          {teams.map((team) => {
            const expanded = expandedId === team.id;
            return (
              <article
                key={team.id}
                className={`teams-card ${expanded ? "teams-card--expanded" : ""}`}
              >
                <div className="teams-card-head">
                  <div className="teams-card-icon">{teamIcon(team.name)}</div>
                  <div className="teams-card-avatars">
                    {team.members.slice(0, 3).map((m) => (
                      <span
                        key={m.id}
                        className="teams-avatar"
                        title={m.displayName}
                      >
                        {memberInitials(m.displayName)}
                      </span>
                    ))}
                    {team.members.length > 3 ? (
                      <span className="teams-avatar teams-avatar--more">
                        +{team.members.length - 3}
                      </span>
                    ) : null}
                  </div>
                </div>
                <h3 className="teams-card-name">{team.name}</h3>
                <p className="teams-card-meta">
                  {team.members.length} membros · Andar preferido:{" "}
                  {team.preferredFloor ?? "—"}
                </p>

                <div className="teams-card-members">
                  <p className="teams-members-label">Lista de membros</p>
                  <ul className="teams-members-list">
                    {team.members.map((member) => (
                      <li key={member.id} className="teams-member-row">
                        <span className="teams-member-avatar">
                          {memberInitials(member.displayName)}
                        </span>
                        <span className="teams-member-info">
                          <strong>{member.displayName}</strong>
                          <span>{member.profileLabel}</span>
                        </span>
                        {member.hidePresenceFromTeam ? (
                          <span
                            className="teams-member-badge"
                            title="Presença oculta para colegas"
                          >
                            Privado
                          </span>
                        ) : null}
                      </li>
                    ))}
                  </ul>
                </div>

                <div className="teams-card-foot">
                  <button
                    type="button"
                    className="teams-card-btn-primary"
                    onClick={() => {
                      setExpandedId(team.id);
                      handleReserveForTeam();
                    }}
                  >
                    Reservar para equipe
                  </button>
                  <button
                    type="button"
                    className="teams-card-btn-ghost"
                    aria-expanded={expanded}
                    onClick={() =>
                      setExpandedId(expanded ? null : team.id)
                    }
                  >
                    {expanded ? "Recolher" : "Detalhes"}
                  </button>
                </div>
              </article>
            );
          })}
        </div>
      )}
    </div>
  );
}
