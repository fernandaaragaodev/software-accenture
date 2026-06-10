import { api } from './client';
import type { AgenteExecucaoResponse, StatusAgente } from '../types';

export interface ListarExecucoesIaParams {
  tipoAgente?: string;
  status?: StatusAgente;
  dataInicio?: string;
  dataFim?: string;
}

function montarQuery(params: ListarExecucoesIaParams = {}) {
  const search = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value) search.set(key, value);
  });

  const query = search.toString();
  return query ? `?${query}` : '';
}

export const iaApi = {
  listarExecucoes: (params?: ListarExecucoesIaParams) =>
    api.get<AgenteExecucaoResponse[]>(`/ia/execucoes${montarQuery(params)}`),
};
