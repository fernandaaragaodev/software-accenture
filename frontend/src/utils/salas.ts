import { layoutsApi } from '../api/layouts';
import { posicoesApi } from '../api/posicoes';
import { regrasDisponibilidadeApi } from '../api/regras-disponibilidade';
import { salasApi } from '../api/salas';
import { tiposEquipamentoApi } from '../api/tipos-equipamento';
import type {
  LayoutResponse,
  PosicaoResponse,
  RegraDisponibilidadeResponse,
  SalaResponse,
  StatusSala,
} from '../types';

export const DIAS_SEMANA = [
  'Segunda',
  'Terça',
  'Quarta',
  'Quinta',
  'Sexta',
  'Sábado',
  'Domingo',
];

export type SalaReadiness = 'pronta' | 'pendente' | 'indisponivel';

export type SortField = 'nome' | 'createdAt' | 'capacidadeMaxima';

export interface SalaEnriched extends SalaResponse {
  regra: RegraDisponibilidadeResponse | null;
  layout: LayoutResponse | null;
  posicoes: PosicaoResponse[];
  totalEquipamentos: number;
  readiness: SalaReadiness;
}

export interface SalaFormValues {
  nome: string;
  descricao: string;
  capacidadeMaxima: string;
  bloco: string;
  andar: string;
  raioProximidade: string;
  statusInicial: StatusSala;
}

export interface SalaFormErrors {
  nome?: string;
  capacidadeMaxima?: string;
  andar?: string;
  raioProximidade?: string;
}

export function validateSalaForm(values: SalaFormValues): SalaFormErrors {
  const errors: SalaFormErrors = {};
  if (!values.nome.trim()) {
    errors.nome = 'Nome é obrigatório';
  }
  const capacidade = Number(values.capacidadeMaxima);
  if (!values.capacidadeMaxima || Number.isNaN(capacidade) || capacidade <= 0) {
    errors.capacidadeMaxima = 'Capacidade deve ser maior que zero';
  }
  if (values.andar !== '' && Number(values.andar) < 0) {
    errors.andar = 'Andar não pode ser negativo';
  }
  if (values.raioProximidade !== '' && Number(values.raioProximidade) < 0) {
    errors.raioProximidade = 'Raio não pode ser negativo';
  }
  return errors;
}

export function isFormValid(errors: SalaFormErrors): boolean {
  return Object.keys(errors).length === 0;
}

export function computeReadiness(
  sala: SalaResponse,
  regra: RegraDisponibilidadeResponse | null,
  layout: LayoutResponse | null,
  posicoes: PosicaoResponse[],
): SalaReadiness {
  if (sala.status === 'INATIVA' || sala.status === 'MANUTENCAO') {
    return 'indisponivel';
  }
  const hasDisponibilidade = !!regra && regra.horarios.length > 0;
  const layoutAprovado = !!layout?.aprovadoPorId;
  const hasPosicoes = posicoes.some((p) => p.status === 'ATIVA');
  if (hasDisponibilidade && layoutAprovado && hasPosicoes) {
    return 'pronta';
  }
  return 'pendente';
}

export function readinessLabel(readiness: SalaReadiness): string {
  switch (readiness) {
    case 'pronta':
      return 'Pronta para Reservas';
    case 'pendente':
      return 'Configuração Pendente';
    case 'indisponivel':
      return 'Indisponível';
  }
}

export function readinessBadgeClass(readiness: SalaReadiness): string {
  switch (readiness) {
    case 'pronta':
      return 'success';
    case 'pendente':
      return 'warning';
    case 'indisponivel':
      return 'danger';
  }
}

export async function enrichSala(sala: SalaResponse): Promise<SalaEnriched> {
  const [regra, layoutResult, posicoes] = await Promise.all([
    salasApi.listarRegrasDisponibilidade(sala.id).catch(() => null),
    layoutsApi.obterAtivo(sala.id).catch(() => null),
    posicoesApi.listarPorSala(sala.id).catch(() => [] as PosicaoResponse[]),
  ]);

  let totalEquipamentos = 0;
  const posicoesAtivas = posicoes.filter((p) => p.status === 'ATIVA');
  if (posicoesAtivas.length > 0) {
    const equipCounts = await Promise.all(
      posicoesAtivas.map((p) =>
        tiposEquipamentoApi.listarPorPosicao(p.id).catch(() => []),
      ),
    );
    totalEquipamentos = equipCounts.reduce(
      (sum, items) => sum + items.reduce((s, e) => s + e.quantidade, 0),
      0,
    );
  }

  return {
    ...sala,
    regra,
    layout: layoutResult,
    posicoes,
    totalEquipamentos,
    readiness: computeReadiness(sala, regra, layoutResult, posicoes),
  };
}

export async function enrichSalas(salas: SalaResponse[]): Promise<SalaEnriched[]> {
  return Promise.all(salas.map(enrichSala));
}

export interface SalaFilters {
  busca: string;
  status: StatusSala | '';
  capacidadeMin: string;
  possuiLayout: '' | 'sim' | 'nao';
  possuiDisponibilidade: '' | 'sim' | 'nao';
  ordenacao: SortField;
}

export const DEFAULT_FILTERS: SalaFilters = {
  busca: '',
  status: '',
  capacidadeMin: '',
  possuiLayout: '',
  possuiDisponibilidade: '',
  ordenacao: 'nome',
};

export function filterAndSortSalas(
  salas: SalaEnriched[],
  filters: SalaFilters,
): SalaEnriched[] {
  const busca = filters.busca.trim().toLowerCase();
  const capMin = filters.capacidadeMin ? Number(filters.capacidadeMin) : 0;

  let result = salas.filter((s) => {
    if (busca) {
      const matchNome = s.nome.toLowerCase().includes(busca);
      const matchBloco = (s.bloco ?? '').toLowerCase().includes(busca);
      if (!matchNome && !matchBloco) return false;
    }
    if (filters.status && s.status !== filters.status) return false;
    if (capMin > 0 && s.capacidadeMaxima < capMin) return false;
    if (filters.possuiLayout === 'sim' && !s.layout) return false;
    if (filters.possuiLayout === 'nao' && s.layout) return false;
    if (filters.possuiDisponibilidade === 'sim' && !s.regra) return false;
    if (filters.possuiDisponibilidade === 'nao' && s.regra) return false;
    return true;
  });

  result = [...result].sort((a, b) => {
    switch (filters.ordenacao) {
      case 'capacidadeMaxima':
        return b.capacidadeMaxima - a.capacidadeMaxima;
      case 'createdAt':
        return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
      default:
        return a.nome.localeCompare(b.nome, 'pt-BR');
    }
  });

  return result;
}

export function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });
}

export function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function toApiTime(value: string): string {
  return value.length === 5 ? `${value}:00` : value;
}

export async function duplicateSala(salaId: string): Promise<SalaResponse> {
  const [sala, regra, layout, posicoes] = await Promise.all([
    salasApi.obter(salaId),
    salasApi.listarRegrasDisponibilidade(salaId).catch(() => null),
    layoutsApi.obterAtivo(salaId).catch(() => null),
    posicoesApi.listarPorSala(salaId).catch(() => [] as PosicaoResponse[]),
  ]);

  const novaSala = await salasApi.criar({
    nome: `Cópia de ${sala.nome}`,
    descricao: sala.descricao,
    andar: sala.andar,
    bloco: sala.bloco,
    capacidadeMaxima: sala.capacidadeMaxima,
    raioProximidade: sala.raioProximidade,
    imagemPath: sala.imagemPath,
  });

  if (regra) {
    const novaRegra = await regrasDisponibilidadeApi.criar({
      nome: `${regra.nome} (cópia)`,
      antecedenciaMinimaDias: regra.antecedenciaMinimaDias,
      horarios: regra.horarios.map((h) => ({
        diaSemana: h.diaSemana,
        horaAbertura: h.horaAbertura,
        horaFechamento: h.horaFechamento,
      })),
    });
    await salasApi.atribuirRegra(novaSala.id, { regraId: novaRegra.id });
  }

  if (layout && layout.aprovadoPorId) {
    const novoLayout = await layoutsApi.criar({
      salaId: novaSala.id,
      versao: layout.versao ? `${layout.versao}-copia` : undefined,
    });
    const layoutAprovado = await layoutsApi.aprovar(novoLayout.id);

    for (const pos of posicoes.filter((p) => p.status === 'ATIVA')) {
      const novaPos = await posicoesApi.criar({
        salaId: novaSala.id,
        identificador: pos.identificador,
        tipo: pos.tipo,
        coordX: pos.coordX,
        coordY: pos.coordY,
        tipoCadeira: pos.tipoCadeira,
        tipoMesa: pos.tipoMesa,
      });

      const equipamentos = await tiposEquipamentoApi
        .listarPorPosicao(pos.id)
        .catch(() => []);
      for (const eq of equipamentos) {
        await tiposEquipamentoApi.vincularPosicao(novaPos.id, {
          tipoEquipamentoId: eq.tipoEquipamentoId,
          quantidade: eq.quantidade,
          observacao: eq.observacao,
        });
      }
    }

    void layoutAprovado;
  }

  return novaSala;
}
