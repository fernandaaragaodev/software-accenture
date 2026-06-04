/**
 * Autenticação centralizada em:
 * - `services/authService.ts` — login, logout, hydrate
 * - `stores/authStore.ts` — estado React (Zustand)
 * - `api/auth.api.ts` + `api/axios.ts` — HTTP e interceptors
 * - `utils/tokenStorage.ts` — localStorage (`accessToken`, `refreshToken`)
 */
export { useAuthStore } from '../stores/authStore';
export { useAuth } from '../hooks/useAuth';
export { login, logout, hydrateAuthFromStorage } from '../services/authService';
