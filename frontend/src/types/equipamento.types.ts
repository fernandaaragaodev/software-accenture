export interface TipoEquipamento {
  id: string;
  nome: string;
  descricao: string | null;
  ativo: boolean;
}

export interface CriarTipoEquipamentoRequest {
  nome: string;
  descricao?: string;
  ativo: boolean;
}
