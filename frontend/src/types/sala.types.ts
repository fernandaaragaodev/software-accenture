export type StatusSala = 'ATIVA' | 'INATIVA' | 'MANUTENCAO';

export interface Sala {
  id: string;
  nome: string;
  descricao: string | null;
  andar: number | null;
  bloco: string | null;
  capacidadeMaxima: number | null;
  raioProximidade: number | null;
  status: StatusSala;
  imagemPath: string | null;
  createdAt?: string;
  updatedAt?: string;
}

export interface CriarSalaRequest {
  nome: string;
  descricao?: string;
  andar?: number;
  bloco?: string;
  capacidadeMaxima: number;
  raioProximidade: number;
  imagemPath?: string;
}

export interface AtualizarStatusSalaRequest {
  status: StatusSala;
}
