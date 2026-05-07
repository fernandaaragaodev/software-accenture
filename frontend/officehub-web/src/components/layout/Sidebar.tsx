import type { NavItem, PageId, SessionUser } from "../../types/officehub";
import { RolePill } from "../RolePill";

interface SidebarProps {
  user: SessionUser;
  page: PageId;
  onNavigate: (id: PageId) => void;
  onLogout: () => void;
  mainNav: NavItem[];
  adminNav: NavItem[];
}

export function Sidebar({
  user,
  page,
  onNavigate,
  onLogout,
  mainNav,
  adminNav,
}: SidebarProps) {
  const initials =
    user.avatar ||
    user.name
      .split(" ")
      .map((n) => n[0])
      .join("")
      .slice(0, 2);

  return (
    <aside className="sidebar">
      <div className="sidebar-logo">
        <div className="logo-mark">
          Space<span>Manager</span>
        </div>
        <div className="logo-sub">Espaços inteligentes</div>
      </div>
      <nav className="sidebar-nav">
        <div className="nav-section-label">Principal</div>
        {mainNav.map((n) => (
          <button
            key={n.id}
            type="button"
            className={`nav-item ${page === n.id ? "active" : ""}`}
            onClick={() => onNavigate(n.id)}
          >
            <span className="nav-icon">{n.icon}</span>
            {n.label}
            {n.badge ? <span className="nav-badge">{n.badge}</span> : null}
          </button>
        ))}
        {user.role === "admin" ? (
          <>
            <div className="nav-section-label">Administração</div>
            {adminNav.map((n) => (
              <button
                key={n.id}
                type="button"
                className={`nav-item ${page === n.id ? "active" : ""}`}
                onClick={() => onNavigate(n.id)}
              >
                <span className="nav-icon">{n.icon}</span>
                {n.label}
              </button>
            ))}
          </>
        ) : null}
      </nav>
      <div className="sidebar-footer">
        <div className="avatar">{initials}</div>
        <div className="avatar-info">
          <div className="avatar-name">{user.name}</div>
          <div className="avatar-role">
            <RolePill role={user.role} />
          </div>
        </div>
        <button
          type="button"
          className="avatar-logout"
          title="Sair"
          onClick={onLogout}
        >
          ⎋
        </button>
      </div>
    </aside>
  );
}
