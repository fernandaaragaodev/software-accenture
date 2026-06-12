import type {
  LayoutResponse,
  PosicaoResponse,
  RegraDisponibilidadeResponse,
  ReservaResumoResponse,
  SalaResponse,
  StatusSala,
} from '../../types';
import { salasApi } from '../../api/salas';

export const DIAS_SEMANA = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo'];

export const WIZARD_STEPS = [
  { id: 'info', label: 'Informações' },
  { id: 'disponibilidade', label: 'Disponibilidade' },
  { id: 'layout', label: 'Layout' },
];

export type SalaReadiness = 'pronta' | 'pendente' | 'indisponivel';

export interface SalaEnriched extends SalaResponse {
  regra: RegraDisponibilidadeResponse | null;
  layout: LayoutResponse | null;
  posicoes: PosicaoResponse[];
  totalEquipamentos: number;
  reservas: ReservaResumoResponse[];
  posicoesLivres?: number;
  posicoesOcupadas?: number;
}

export interface SalaInfoForm {
  nome: string;
  descricao: string;
  capacidadeMaxima: string;
  bloco: string;
  andar: string;
  raioProximidade: string;
  statusInicial: StatusSala;
}

export interface HorarioForm {
  diaSemana: number;
  ativo: boolean;
  horaAbertura: string;
  horaFechamento: string;
}

export interface DisponibilidadeForm {
  modo: 'nova' | 'existente';
  regraExistenteId: string;
  nomeRegra: string;
  antecedenciaMinimaDias: string;
  limiteReservasFuturas: string;
  horarios: HorarioForm[];
}

export function horariosPadrao(): HorarioForm[] {
  return DIAS_SEMANA.map((_, index) => ({
    diaSemana: index,
    ativo: index < 5,
    horaAbertura: '08:00',
    horaFechamento: '18:00',
  }));
}

export function toApiTime(value: string) {
  return value.length === 5 ? `${value}:00` : value;
}

export function formatDate(value?: string) {
  if (!value) return '—';
  return new Date(value).toLocaleString('pt-BR');
}

export function formatBlocoAndar(bloco?: string, andar?: number) {
  const blocoText = bloco ?? '—';
  const andarText = andar ?? '—';
  return `${blocoText} / ${andarText}`;
}

export function getSalaReadiness(
  sala: SalaResponse,
  regra: RegraDisponibilidadeResponse | null,
  layout: LayoutResponse | null,
  posicoes: PosicaoResponse[],
): SalaReadiness {
  if (sala.status === 'INATIVA' || sala.status === 'MANUTENCAO') {
    return 'indisponivel';
  }

  const layoutAprovado = !!layout?.aprovadoEm;
  const temPosicoes = posicoes.length > 0;
  const temDisponibilidade = !!regra;

  if (temDisponibilidade && layoutAprovado && temPosicoes) {
    return 'pronta';
  }

  return 'pendente';
}

export function getReadinessLabel(readiness: SalaReadiness) {
  switch (readiness) {
    case 'pronta':
      return 'Pronta para Reservas';
    case 'pendente':
      return 'Configuração Pendente';
    case 'indisponivel':
      return 'Indisponível';
  }
}

export function validateInfoForm(form: SalaInfoForm) {
  const errors: Partial<Record<keyof SalaInfoForm, string>> = {};

  if (!form.nome.trim()) {
    errors.nome = 'Nome é obrigatório';
  }

  const capacidade = Number(form.capacidadeMaxima);
  if (!form.capacidadeMaxima || Number.isNaN(capacidade) || capacidade <= 0) {
    errors.capacidadeMaxima = 'Capacidade deve ser maior que zero';
  }

  if (form.andar !== '' && Number(form.andar) < 0) {
    errors.andar = 'Andar não pode ser negativo';
  }

  if (form.raioProximidade !== '' && Number(form.raioProximidade) < 0) {
    errors.raioProximidade = 'Raio não pode ser negativo';
  }

  return errors;
}

export function computeReservaStats(reservas: ReservaResumoResponse[]) {
  const confirmadas = reservas.filter((r) => r.status === 'CONFIRMADA').length;
  const ultima = [...reservas].sort(
    (a, b) => new Date(b.dataReserva).getTime() - new Date(a.dataReserva).getTime(),
  )[0];

  return {
    total: reservas.length,
    confirmadas,
    ultima,
  };
}

export async function loadDisponibilidadePosicoes(salaId: string) {
  const hoje = new Date().toISOString().slice(0, 10);
  try {
    const consulta = await salasApi.consultarDisponibilidade(salaId, hoje, '08:00', '18:00');
    return {
      posicoesLivres: consulta.totalLivres,
      posicoesOcupadas: consulta.totalOcupadas,
    };
  } catch {
    return { posicoesLivres: undefined, posicoesOcupadas: undefined };
  }
}

export type SortField = 'nome' | 'createdAt' | 'capacidadeMaxima';
export type SortDirection = 'asc' | 'desc';

export interface SalaFilters {
  busca: string;
  status: '' | StatusSala;
  capacidadeMin: string;
  possuiLayout: '' | 'sim' | 'nao';
  possuiDisponibilidade: '' | 'sim' | 'nao';
  ordenarPor: SortField;
  direcao: SortDirection;
}

export const DEFAULT_FILTERS: SalaFilters = {
  busca: '',
  status: '',
  capacidadeMin: '',
  possuiLayout: '',
  possuiDisponibilidade: '',
  ordenarPor: 'nome',
  direcao: 'asc',
};

export function filterAndSortSalas(enriched: SalaEnriched[], filters: SalaFilters) {
  const busca = filters.busca.trim().toLowerCase();

  let result = enriched.filter((sala) => {
    if (busca) {
      const matchNome = sala.nome.toLowerCase().includes(busca);
      const matchBloco = (sala.bloco ?? '').toLowerCase().includes(busca);
      if (!matchNome && !matchBloco) return false;
    }

    if (filters.status && sala.status !== filters.status) return false;

    if (filters.capacidadeMin) {
      const min = Number(filters.capacidadeMin);
      if (!Number.isNaN(min) && sala.capacidadeMaxima < min) return false;
    }

    const temLayout = !!sala.layout?.aprovadoEm && sala.posicoes.length > 0;
    if (filters.possuiLayout === 'sim' && !temLayout) return false;
    if (filters.possuiLayout === 'nao' && temLayout) return false;

    const temDisponibilidade = !!sala.regra;
    if (filters.possuiDisponibilidade === 'sim' && !temDisponibilidade) return false;
    if (filters.possuiDisponibilidade === 'nao' && temDisponibilidade) return false;

    return true;
  });

  result = [...result].sort((a, b) => {
    let cmp = 0;
    switch (filters.ordenarPor) {
      case 'nome':
        cmp = a.nome.localeCompare(b.nome, 'pt-BR');
        break;
      case 'createdAt':
        cmp = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime();
        break;
      case 'capacidadeMaxima':
        cmp = a.capacidadeMaxima - b.capacidadeMaxima;
        break;
    }
    return filters.direcao === 'asc' ? cmp : -cmp;
  });

  return result;
}
