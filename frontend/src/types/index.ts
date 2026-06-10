export type Role = 'ADMIN_SALA' | 'GESTOR_RESERVAS' | 'USUARIO_FINAL' | 'INTEGRADOR';

export type StatusSala = 'ATIVA' | 'INATIVA' | 'MANUTENCAO';
export type StatusReserva = 'PENDENTE' | 'CONFIRMADA' | 'REJEITADA' | 'CANCELADA';
export type CriterioProximidade = 'OBRIGATORIA' | 'PREFERENCIAL';
export type PosicaoSituacao = 'LIVRE' | 'OCUPADA' | 'INATIVA';
export type StatusAgente = 'SUCESSO' | 'FALHA';

export interface ApiError {
  timestamp: string;
  status: number;
  mensagem: string;
}

export interface LoginRequest {
  email: string;
  senha: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface UsuarioResponse {
  id: string;
  nome: string;
  email: string;
  ativo: boolean;
  perfis: Role[];
}

export interface CriarUsuarioRequest {
  nome: string;
  email: string;
  senha: string;
  perfis?: Role[];
}

export interface SalaResponse {
  id: string;
  nome: string;
  descricao?: string;
  andar?: number;
  bloco?: string;
  capacidadeMaxima: number;
  raioProximidade?: number;
  status: StatusSala;
  imagemPath?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CriarSalaRequest {
  nome: string;
  descricao?: string;
  andar?: number;
  bloco?: string;
  capacidadeMaxima: number;
  raioProximidade?: number;
  imagemPath?: string;
}

export interface AtualizarSalaRequest extends CriarSalaRequest {}

export interface AtualizarStatusSalaRequest {
  status: StatusSala;
}

export interface HorarioDisponibilidade {
  id?: string;
  diaSemana: number;
  horaAbertura: string;
  horaFechamento: string;
}

export interface CriarRegraDisponibilidadeRequest {
  nome?: string;
  antecedenciaMinimaDias: number;
  horarios: Omit<HorarioDisponibilidade, 'id'>[];
}

export interface CriarRegraDisponibilidadeIndependenteRequest {
  nome: string;
  antecedenciaMinimaDias: number;
  horarios: Omit<HorarioDisponibilidade, 'id'>[];
}

export interface AtribuirRegraSalaRequest {
  regraId: string;
}

export interface RegraDisponibilidadeResponse {
  id: string;
  nome: string;
  salaId?: string;
  salaNome?: string;
  antecedenciaMinimaDias: number;
  horarios: HorarioDisponibilidade[];
}

export interface ConsultaDisponibilidadeResponse {
  salaId: string;
  data: string;
  statusSala: StatusSala;
  disponivelParaReserva: boolean;
  mensagemRegras: string;
  totalPosicoes: number;
  totalLivres: number;
  totalOcupadas: number;
  totalInativas: number;
  livresPorTipo: Record<string, number>;
  posicoesOcupadas: { id: string; identificador: string; tipo?: string }[];
  layout: {
    id: string;
    identificador: string;
    tipo?: string;
    coordX?: number;
    coordY?: number;
    situacao: PosicaoSituacao;
  }[];
}

export interface EquipeResumoResponse {
  id: string;
  nome: string;
  descricao?: string;
  quantidadeGestores: number;
  quantidadeMembros: number;
}

export interface UsuarioResumo {
  id: string;
  nome: string;
  email: string;
}

export interface EquipeResponse {
  id: string;
  nome: string;
  descricao?: string;
  gestores: UsuarioResumo[];
  membros: UsuarioResumo[];
}

export interface CriarEquipeRequest {
  nome: string;
  descricao?: string;
  gestorId?: string;
  membrosIds: string[];
}

export interface AdicionarMembroEquipeRequest {
  usuarioId: string;
}

export interface PessoaReservaRequest {
  usuarioId?: string;
  nomeExterno?: string;
  tipoPreferido1?: string;
  tipoPreferido2?: string;
  tipoPreferido3?: string;
}

export interface SolicitarReservaRequest {
  salaId: string;
  equipeId?: string;
  dataReserva: string;
  horaInicio: string;
  horaFim: string;
  quantidadePessoas: number;
  criterioProximidade: CriterioProximidade;
  pessoas: PessoaReservaRequest[];
}

export interface AlocacaoResponse {
  posicaoId: string;
  posicaoIdentificador: string;
  posicaoTipo?: string;
  pessoaNome?: string;
  equipamentos?: string[];
}

export interface ReservaResponse {
  id: string;
  salaId: string;
  solicitanteId: string;
  dataReserva: string;
  horaInicio: string;
  horaFim: string;
  quantidadePessoas: number;
  criterioProximidade: CriterioProximidade;
  status: StatusReserva;
  motivoRejeicao?: string;
  avisoProximidade?: string;
  alocacoes: AlocacaoResponse[];
}

export interface RejeitarReservaRequest {
  motivo: string;
}

export interface CancelarReservaRequest {
  motivo: string;
}

export interface PosicaoResponse {
  id: string;
  salaId: string;
  layoutId: string;
  identificador: string;
  tipo?: string;
  coordX?: number;
  coordY?: number;
  tipoCadeira?: string;
  tipoMesa?: string;
  status: 'ATIVA' | 'INATIVA';
  ajustadoManualmente: boolean;
}

export interface CriarPosicaoRequest {
  salaId: string;
  identificador: string;
  tipo?: string;
  coordX?: number;
  coordY?: number;
  tipoCadeira?: string;
  tipoMesa?: string;
}

export interface AtualizarCoordenadasPosicaoRequest {
  coordX: number;
  coordY: number;
}

export interface LayoutResponse {
  id: string;
  salaId: string;
  versao?: string;
  ativo: boolean;
  aprovadoPorId?: string;
  aprovadoEm?: string;
  createdAt: string;
}

export interface CriarLayoutRequest {
  salaId: string;
  versao?: string;
}

export interface JwtPayload {
  sub: string;
  roles: Role[];
  exp: number;
  iat: number;
}

export interface TipoEquipamentoResponse {
  id: string;
  nome: string;
  descricao?: string;
  ativo: boolean;
}

export interface TipoEquipamentoRequest {
  nome: string;
  descricao?: string;
  ativo?: boolean;
}

export interface PosicaoEquipamentoResponse {
  id: string;
  posicaoId: string;
  tipoEquipamentoId: string;
  tipoEquipamentoNome: string;
  quantidade: number;
  observacao?: string;
  createdAt: string;
}

export interface VincularEquipamentoPosicaoRequest {
  tipoEquipamentoId: string;
  quantidade: number;
  observacao?: string;
}


export interface AgenteExecucaoResponse {
  id: string;
  tipoAgente: string;
  referenciaId?: string;
  status: StatusAgente;
  versaoModelo: string;
  tempoProcessamentoMs?: number;
  erroMensagem?: string;
  payloadEntrada?: unknown;
  payloadSaida?: unknown;
  executadoEm: string;
}

export interface DashboardStatsResponse {
  totalSalas: number;
  totalPosicoes: number;
  totalPosicoesAtivas: number;
  totalTiposEquipamento: number;
  totalTiposEquipamentoAtivos: number;
  totalEquipamentosVinculados: number;
  totalUsuarios: number;
  totalEquipes: number;
  totalReservas: number;
  reservasConfirmadas: number;
  reservasPendentes: number;
  reservasCanceladas: number;
  reservasRejeitadas: number;
}
