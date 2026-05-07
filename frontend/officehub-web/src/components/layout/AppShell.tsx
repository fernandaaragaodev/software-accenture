import type { ReactNode } from "react";
import type { NavItem, PageId, SessionUser } from "../../types/officehub";
import { Sidebar } from "./Sidebar";
import { TopBar } from "./TopBar";

interface AppShellProps {
  user: SessionUser;
  page: PageId;
  pageTitle: string;
  mainNav: NavItem[];
  adminNav: NavItem[];
  unreadNotifs: number;
  onNavigate: (id: PageId) => void;
  onLogout: () => void;
  children: ReactNode;
}

export function AppShell({
  user,
  page,
  pageTitle,
  mainNav,
  adminNav,
  unreadNotifs,
  onNavigate,
  onLogout,
  children,
}: AppShellProps) {
  return (
    <div className="app">
      <Sidebar
        user={user}
        page={page}
        onNavigate={onNavigate}
        onLogout={onLogout}
        mainNav={mainNav}
        adminNav={adminNav}
      />
      <div className="main">
        <TopBar
          title={pageTitle}
          unreadNotifs={unreadNotifs}
          onOpenNotifications={() => onNavigate("notifications")}
        />
        <main className="content">{children}</main>
      </div>
    </div>
  );
}
