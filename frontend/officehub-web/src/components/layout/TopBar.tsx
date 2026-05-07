interface TopBarProps {
  title: string;
  unreadNotifs: number;
  onOpenNotifications: () => void;
}

export function TopBar({
  title,
  unreadNotifs,
  onOpenNotifications,
}: TopBarProps) {
  return (
    <header className="topbar">
      <div className="topbar-title">{title}</div>
      <div className="topbar-search">
        <span style={{ fontSize: 14 }}>🔍</span>
        <input placeholder="Buscar no sistema..." />
      </div>
      <button
        type="button"
        className="icon-btn"
        onClick={onOpenNotifications}
        title="Notificações"
      >
        🔔 {unreadNotifs > 0 ? <span className="notif-dot" /> : null}
      </button>
    </header>
  );
}
