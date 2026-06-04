export type Role =
  | 'ADMIN_SALA'
  | 'GESTOR_RESERVAS'
  | 'USUARIO_FINAL'
  | 'INTEGRADOR';

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RegisterRequest {
  nome: string;
  email: string;
  senha: string;
}

export interface UsuarioResponse {
  id: string;
  nome: string;
  email: string;
  ativo: boolean;
  perfis: string[];
}

export interface AuthUser {
  email: string;
  roles: Role[];
  usuarioId: string | null;
}

export interface ApiErrorBody {
  timestamp?: string;
  status?: number;
  mensagem?: string;
}
