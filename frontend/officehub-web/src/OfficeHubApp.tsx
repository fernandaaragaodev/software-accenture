import { useEffect, useMemo, useState } from "react";
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
import { fetchNotifications } from "./services/notificationsService";

export default function OfficeHubApp() {
  const [user, setUser] = useState<SessionUser | null>(null);
  const [page, setPage] = useState<PageId>("dashboard");
  const [unreadNotifs, setUnreadNotifs] = useState(0);

  const visibleNav = useMemo(() => {
    const roleNav = filterNavForRole(NAV, user?.role);
    return roleNav.map((item) => {
      if (item.id !== "notifications") return item;
      return {
        ...item,
        badge: unreadNotifs > 0 ? String(unreadNotifs) : undefined,
      };
    });
  }, [user?.role, unreadNotifs]);
  const mainNav = visibleNav.slice(0, 5);
  const adminNav = visibleNav.slice(5);

  async function refreshUnreadNotifications() {
    if (!user) {
      setUnreadNotifs(0);
      return;
    }
    try {
      const notifications = await fetchNotifications(user);
      setUnreadNotifs(notifications.filter((item) => !item.read).length);
    } catch {
      // Erro nao bloqueia navegação; mantem ultimo contador conhecido.
    }
  }

  useEffect(() => {
    if (!user) return;
    void refreshUnreadNotifications();
    const polling = window.setInterval(() => {
      void refreshUnreadNotifications();
    }, 15000);
    return () => window.clearInterval(polling);
  }, [user]);

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
        return <HistoryPage user={user} />;
      case "notifications":
        return (
          <NotificationsPage
            user={user}
            onNotificationsChanged={(items) =>
              setUnreadNotifs(items.filter((item) => !item.read).length)
            }
          />
        );
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
      unreadNotifs={unreadNotifs}
      onNavigate={setPage}
      onLogout={() => setUser(null)}
    >
      {renderPage()}
    </AppShell>
  );
}
