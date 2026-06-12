import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { salasApi } from '../../api/salas';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { SalaCardSkeleton } from '../../components/Skeleton';
import { useToast } from '../../components/Toast';
import { Alert, EmptyState, PageHeader } from '../../components/ui';
import { SalaCard } from './SalaCard';
import {
  DEFAULT_FILTERS,
  filterAndSortSalas,
  type SalaEnriched,
  type SalaFilters,
} from './salasUtils';
import { useSalasEnrichedList } from './useSalaEnrichment';

export function SalasListPage() {
  const { salas, loading, error, reload } = useSalasEnrichedList();
  const toast = useToast();
  const [filters, setFilters] = useState<SalaFilters>(DEFAULT_FILTERS);
  const [archiveTarget, setArchiveTarget] = useState<SalaEnriched | null>(null);
  const [archiving, setArchiving] = useState(false);

  const filteredSalas = useMemo(() => filterAndSortSalas(salas, filters), [salas, filters]);

  function updateFilter<K extends keyof SalaFilters>(key: K, value: SalaFilters[K]) {
    setFilters((prev) => ({ ...prev, [key]: value }));
  }

  async function handleArchiveConfirm() {
    if (!archiveTarget) return;
    setArchiving(true);
    try {
      await salasApi.inativar(archiveTarget.id);
      toast.success(`Sala "${archiveTarget.nome}" arquivada com sucesso.`);
      setArchiveTarget(null);
      reload();
    } catch (err) {
      toast.error(err instanceof ApiException ? err.message : 'Erro ao arquivar sala');
    } finally {
      setArchiving(false);
    }
  }

  return (
    <div>
      <PageHeader
        title="Salas"
        subtitle="Gerencie salas, disponibilidade, layouts e reservas"
        action={
          <Link to="/salas/nova" className="btn btn-primary">
            Nova Sala
          </Link>
        }
      />

      <Alert message={error} />

      <div className="card filters-card mb-lg">
        <div className="filters-grid">
          <label>
            Buscar
            <input
              type="search"
              placeholder="Nome ou bloco..."
              value={filters.busca}
              onChange={(e) => updateFilter('busca', e.target.value)}
            />
          </label>
          <label>
            Status
            <select
              value={filters.status}
              onChange={(e) => updateFilter('status', e.target.value as SalaFilters['status'])}
            >
              <option value="">Todos</option>
              <option value="ATIVA">Ativa</option>
              <option value="INATIVA">Inativa</option>
              <option value="MANUTENCAO">Manutenção</option>
            </select>
          </label>
          <label>
            Capacidade mínima
            <input
              type="number"
              min={0}
              placeholder="Ex: 10"
              value={filters.capacidadeMin}
              onChange={(e) => updateFilter('capacidadeMin', e.target.value)}
            />
          </label>
          <label>
            Possui layout
            <select
              value={filters.possuiLayout}
              onChange={(e) => updateFilter('possuiLayout', e.target.value as SalaFilters['possuiLayout'])}
            >
              <option value="">Todos</option>
              <option value="sim">Sim</option>
              <option value="nao">Não</option>
            </select>
          </label>
          <label>
            Possui disponibilidade
            <select
              value={filters.possuiDisponibilidade}
              onChange={(e) =>
                updateFilter('possuiDisponibilidade', e.target.value as SalaFilters['possuiDisponibilidade'])
              }
            >
              <option value="">Todos</option>
              <option value="sim">Sim</option>
              <option value="nao">Não</option>
            </select>
          </label>
          <label>
            Ordenar por
            <select
              value={filters.ordenarPor}
              onChange={(e) => updateFilter('ordenarPor', e.target.value as SalaFilters['ordenarPor'])}
            >
              <option value="nome">Nome</option>
              <option value="createdAt">Data de criação</option>
              <option value="capacidadeMaxima">Capacidade</option>
            </select>
          </label>
          <label>
            Direção
            <select
              value={filters.direcao}
              onChange={(e) => updateFilter('direcao', e.target.value as SalaFilters['direcao'])}
            >
              <option value="asc">Crescente</option>
              <option value="desc">Decrescente</option>
            </select>
          </label>
        </div>
      </div>

      {loading ? (
        <div className="sala-cards-grid">
          {Array.from({ length: 6 }).map((_, i) => (
            <SalaCardSkeleton key={i} />
          ))}
        </div>
      ) : salas.length === 0 ? (
        <EmptyState
          title="Nenhuma sala cadastrada"
          description="Crie a primeira sala para começar a configurar layouts e posições."
          action={
            <Link to="/salas/nova" className="btn btn-primary">
              Criar sala
            </Link>
          }
        />
      ) : filteredSalas.length === 0 ? (
        <EmptyState
          title="Nenhuma sala encontrada"
          description="Ajuste os filtros ou limpe a busca para ver mais resultados."
          action={
            <button type="button" className="btn btn-ghost" onClick={() => setFilters(DEFAULT_FILTERS)}>
              Limpar filtros
            </button>
          }
        />
      ) : (
        <div className="sala-cards-grid">
          {filteredSalas.map((sala) => (
            <SalaCard key={sala.id} sala={sala} onArchive={setArchiveTarget} />
          ))}
        </div>
      )}

      <ConfirmDialog
        open={!!archiveTarget}
        title="Arquivar sala"
        message={`Tem certeza que deseja arquivar "${archiveTarget?.nome}"? A sala será removida da listagem ativa sem exclusão física.`}
        confirmLabel="Arquivar"
        variant="danger"
        loading={archiving}
        onConfirm={handleArchiveConfirm}
        onCancel={() => setArchiveTarget(null)}
      />
    </div>
  );
}
