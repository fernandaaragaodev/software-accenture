import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { login as apiLogin, logout as apiLogout } from '../api/auth';
import type { LoginRequest, Role } from '../types';
import {
  clearTokens,
  decodeJwt,
  getAccessToken,
  getEmailFromToken,
  getRolesFromToken,
  isTokenExpired,
} from '../utils/auth';

interface AuthContextValue {
  email: string | null;
  roles: Role[];
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: LoginRequest) => Promise<void>;
  logout: () => Promise<void>;
  hasRole: (role: Role) => boolean;
  hasAnyRole: (roles: Role[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [email, setEmail] = useState<string | null>(null);
  const [roles, setRoles] = useState<Role[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  const syncFromToken = useCallback(() => {
    const token = getAccessToken();
    if (!token || isTokenExpired()) {
      clearTokens();
      setEmail(null);
      setRoles([]);
      return;
    }
    setEmail(getEmailFromToken());
    setRoles(getRolesFromToken());
  }, []);

  useEffect(() => {
    syncFromToken();
    setIsLoading(false);
  }, [syncFromToken]);

  const login = useCallback(async (credentials: LoginRequest) => {
    const response = await apiLogin(credentials);
    const payload = decodeJwt(response.accessToken);
    setEmail(payload?.sub ?? credentials.email);
    setRoles(payload?.roles ?? []);
  }, []);

  const logout = useCallback(async () => {
    await apiLogout();
    setEmail(null);
    setRoles([]);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      email,
      roles,
      isAuthenticated: !!email && roles.length > 0,
      isLoading,
      login,
      logout,
      hasRole: (role) => roles.includes(role),
      hasAnyRole: (required) => required.some((role) => roles.includes(role)),
    }),
    [email, roles, isLoading, login, logout],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth(): AuthContextValue {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
