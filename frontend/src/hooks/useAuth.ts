import { useAuthStore } from '../stores/authStore';

export function useAuth() {
  const accessToken = useAuthStore((s) => s.accessToken);
  const refreshToken = useAuthStore((s) => s.refreshToken);
  const user = useAuthStore((s) => s.user);
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const setSession = useAuthStore((s) => s.setSession);
  const setUsuarioId = useAuthStore((s) => s.setUsuarioId);
  const clearSession = useAuthStore((s) => s.clearSession);
  const hydrate = useAuthStore((s) => s.hydrate);

  return {
    accessToken,
    refreshToken,
    user,
    isAuthenticated,
    roles: user?.roles ?? [],
    setSession,
    setUsuarioId,
    clearSession,
    hydrate,
  };
}
