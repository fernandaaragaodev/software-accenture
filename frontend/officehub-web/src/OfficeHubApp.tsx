import { useState } from "react";
import { NAV, PAGE_TITLES, filterNavForRole } from "./constants/navigation";
import { AppShell } from "./components/layout/AppShell";
import type { PageId, SessionUser } from "./types/officehub";
import { LoginPage } from "./pages/LoginPage";
import { DashboardPage } from "./pages/DashboardPage";
import { RoomsPage } from "./pages/RoomsPage";
import { ReservationsPage } from "./pages/ReservationsPage";
import { HistoryPage } from "./pages/HistoryPage";
import { NotificationsPage } from "./pages/NotificationsPage";
import { UsersPage } from "./pages/UsersPage";
import { ApiPage } from "./pages/ApiPage";

const UNREAD_NOTIFS = 2;

export default function OfficeHubApp() {
  const [user, setUser] = useState<SessionUser | null>(null);
  const [page, setPage] = useState<PageId>("dashboard");

  const visibleNav = filterNavForRole(NAV, user?.role);
  const mainNav = visibleNav.slice(0, 5);
  const adminNav = visibleNav.slice(5);

  function renderPage() {
    if (!user) return null;
    switch (page) {
      case "dashboard":
        return <DashboardPage user={user} />;
      case "rooms":
        return <RoomsPage user={user} />;
      case "reservations":
        return <ReservationsPage user={user} />;
      case "history":
        return <HistoryPage />;
      case "notifications":
        return <NotificationsPage />;
      case "users":
        return <UsersPage />;
      case "api":
        return <ApiPage />;
      default:
        return <DashboardPage user={user} />;
    }
  }

  if (!user) {
    return (
      <LoginPage
        onLogin={(u) => {
          setUser(u);
          setPage("dashboard");
        }}
      />
    );
  }

  return (
    <AppShell
      user={user}
      page={page}
      pageTitle={PAGE_TITLES[page]}
      mainNav={mainNav}
      adminNav={adminNav}
      unreadNotifs={UNREAD_NOTIFS}
      onNavigate={setPage}
      onLogout={() => setUser(null)}
    >
      {renderPage()}
    </AppShell>
  );
}
