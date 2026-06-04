export type StatusReserva = 'PENDENTE' | 'CONFIRMADA' | 'REJEITADA' | 'CANCELADA';

export type CriterioProximidade = 'OBRIGATORIA' | 'PREFERENCIAL';

export interface PessoaReserva {
  usuarioId: string | null;
  nomeExterno: string;
  tipoPreferido1: string;
  tipoPreferido2: string;
  tipoPreferido3: string;
}

export interface SolicitarReservaRequest {
  salaId: string;
  dataReserva: string;
  quantidadePessoas: number;
  criterioProximidade: CriterioProximidade;
  pessoas: PessoaReserva[];
}

export interface ReservaPosicaoAlocada {
  posicaoId: string;
  posicaoIdentificador: string;
  posicaoTipo: string;
}

export interface Reserva {
  id: string;
  salaId: string;
  solicitanteId: string;
  dataReserva: string;
  quantidadePessoas: number;
  criterioProximidade: string;
  status: StatusReserva;
  motivoRejeicao: string | null;
  alocacoes: ReservaPosicaoAlocada[];
}

export interface RejeitarReservaRequest {
  motivo: string;
}

export interface CancelarReservaRequest {
  motivo: string;
}
