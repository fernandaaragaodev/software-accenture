import { api } from './client';
import type { GerarLayoutPorIaResponse, SalaResponse } from '../types';
import { getAccessToken } from '../utils/auth';

const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

export interface GerarLayoutPorIaParams {
  nomeSala: string;
  largura: number;
  altura: number;
  imagem: File;
}

export async function gerarLayoutPorIa(params: GerarLayoutPorIaParams): Promise<GerarLayoutPorIaResponse> {
  const formData = new FormData();
  formData.append('nomeSala', params.nomeSala);
  formData.append('largura', String(params.largura));
  formData.append('altura', String(params.altura));
  formData.append('imagem', params.imagem);

  const token = getAccessToken();
  const response = await fetch(`${API_BASE}/layouts/gerar-por-ia`, {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : undefined,
    body: formData,
  });

  if (!response.ok) {
    let message = `Erro ${response.status}`;
    try {
      const data = (await response.json()) as { mensagem?: string };
      message = data.mensagem || message;
    } catch {
      // mantém mensagem padrão
    }
    throw new Error(message);
  }

  return response.json();
}

export function confirmarSalaIa(salaId: string): Promise<SalaResponse> {
  return api.patch<SalaResponse>(`/layouts/ia/${salaId}/confirmar`);
}

export function negarSalaIa(salaId: string): Promise<SalaResponse> {
  return api.patch<SalaResponse>(`/layouts/ia/${salaId}/negar`);
}
