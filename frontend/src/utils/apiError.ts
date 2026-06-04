import type { AxiosError } from 'axios';
import type { ApiErrorBody } from '../types/auth.types';

export function getApiErrorMessage(error: unknown, fallback = 'Ocorreu um erro inesperado.'): string {
  if (!error || typeof error !== 'object') return fallback;

  const axiosError = error as AxiosError<ApiErrorBody>;
  const status = axiosError.response?.status;
  const mensagem = axiosError.response?.data?.mensagem;

  if (mensagem) return mensagem;

  if (!axiosError.response && axiosError.message) {
    if (axiosError.code === 'ERR_NETWORK') {
      return 'Não foi possível conectar ao servidor. Verifique se a API está em http://localhost:8080 ou use o proxy do Vite (VITE_API_URL vazio).';
    }
    return axiosError.message;
  }

  switch (status) {
    case 401:
      return 'Sessão expirada ou credenciais inválidas. Faça login novamente.';
    case 403:
      return 'Acesso negado. Você não tem permissão para esta ação.';
    case 404:
      return 'Recurso não encontrado.';
    case 409:
      return 'Conflito ao processar a solicitação.';
    case 422:
      return 'Não foi possível atender à regra de negócio.';
    case 400:
      return 'Dados inválidos. Verifique os campos informados.';
    default:
      return fallback;
  }
}
