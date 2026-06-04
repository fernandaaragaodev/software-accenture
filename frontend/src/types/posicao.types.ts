export interface Posicao {
  id: string;
  salaId: string;
  layoutId: string;
  identificador: string;
  tipo: string;
  coordX: number;
  coordY: number;
  tipoCadeira: string | null;
  tipoMesa: string | null;
  status: string;
  ajustadoManualmente: boolean | null;
}

export interface CriarPosicaoRequest {
  salaId: string;
  identificador: string;
  tipo: string;
  coordX: number;
  coordY: number;
  tipoCadeira?: string;
  tipoMesa?: string;
}

export interface AtualizarCoordenadasRequest {
  coordX: number;
  coordY: number;
}

export interface VincularEquipamentoRequest {
  tipoEquipamentoId: string;
  quantidade: number;
  observacao?: string;
}

export interface PosicaoEquipamento {
  id: string;
  posicaoId: string;
  tipoEquipamentoId: string;
  tipoEquipamentoNome: string;
  quantidade: number;
  observacao: string | null;
}

export type PosicaoMapStatus = 'livre' | 'ocupada' | 'inativa';
