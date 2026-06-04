export interface HorarioDisponibilidade {
  id?: string;
  diaSemana: number;
  horaAbertura: string;
  horaFechamento: string;
}

export interface CriarRegraDisponibilidadeRequest {
  antecedenciaMinimaDias: number;
  horarios: HorarioDisponibilidade[];
}

export interface RegraDisponibilidade {
  id: string;
  salaId: string;
  antecedenciaMinimaDias: number;
  horarios: {
    id: string;
    diaSemana: number;
    horaAbertura: string;
    horaFechamento: string;
  }[];
}

export interface ValidacaoDisponibilidade {
  salaId: string;
  data: string;
  disponivel: boolean;
  mensagem: string;
}

export interface ExcecaoDisponibilidadeRequest {
  data: string;
  motivo: string;
}

export interface Layout {
  id: string;
  salaId: string;
  versao: string;
  ativo: boolean;
  aprovadoPorId: string | null;
  aprovadoEm: string | null;
  createdAt: string;
}

export interface CriarLayoutRequest {
  salaId: string;
  versao: string;
}
