import { api } from './axios';
import type { Notificacao } from '../types/notificacao.types';

export const notificacoesApi = {
  listarPorUsuario: (usuarioId: string) =>
    api.get<Notificacao[]>(`/notificacoes/usuario/${usuarioId}`),
  processarFila: () => api.post<void>('/notificacoes/processar-fila'),
};
