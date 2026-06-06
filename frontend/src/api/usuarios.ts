import { api } from './client';
import type { UsuarioResponse, UsuarioResumo } from '../types';

export const usuariosApi = {
  listar: () => api.get<UsuarioResponse[]>('/usuarios'),
  listarGestores: () => api.get<UsuarioResumo[]>('/usuarios/gestores'),
  listarDisponiveisEquipe: () => api.get<UsuarioResumo[]>('/usuarios/disponiveis-equipe'),
  listarMembrosEquipe: () => api.get<UsuarioResumo[]>('/usuarios/membros-equipe'),
};
