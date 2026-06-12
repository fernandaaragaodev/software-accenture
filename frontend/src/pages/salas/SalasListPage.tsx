import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { salasApi } from '../../api/salas';
import { SalaCard } from '../../components/salas/SalaCard';
import { SalaFiltersBar } from '../../components/salas/SalaFiltersBar';
import { ConfirmDialog, EmptyState, PageHeader, SkeletonGrid } from '../../components/ui';
import { useToast } from '../../context/ToastContext';
import {
  DEFAULT_FILTERS,
  duplicateSala,
  enrichSalas,
  filterAndSortSalas,
  type SalaEnriched,
  type SalaFilters,
} from '../../utils/salas';

export function SalasListPage() {
  const { showToast } = useToast();
  const [salas, setSalas] = useState<SalaEnriched[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filters, setFilters] = useState<SalaFilters>(DEFAULT_FILTERS);
  const [archiveId, setArchiveId] = useState<string | null>(null);
  const [archiving, setArchiving] = useState(false);
  const [duplicatingId, setDuplicatingId] = useState<string | null>(null);

  const carregar = useCallback(() => {
    setLoading(true);
    setError('');
    salasApi
      .listar()
      .then((data) => enrichSalas(data))
      .then(setSalas)
      .catch((err) =>
        setError(err instanceof ApiException ? err.message : 'Erro ao carregar salas'),
      )
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const filtered = useMemo(
    () => filterAndSortSalas(salas, filters),
    [salas, filters],
  );

  async function handleArchive() {
    if (!archiveId) return;
    setArchiving(true);
    try {
      await salasApi.inativar(archiveId);
      showToast('Sala arquivada com sucesso', 'success');
      setArchiveId(null);
      carregar();
    } catch (err) {
      showToast(
        err instanceof ApiException ? err.message : 'Erro ao arquivar sala',
        'error',
      );
    } finally {
      setArchiving(false);
    }
  }

  async function handleDuplicate(id: string) {
    setDuplicatingId(id);
    try {
      const nova = await duplicateSala(id);
      showToast(`Sala "${nova.nome}" duplicada com sucesso`, 'success');
      carregar();
    } catch (err) {
      showToast(
        err instanceof ApiException ? err.message : 'Erro ao duplicar sala',
        'error',
      );
    } finally {
      setDuplicatingId(null);
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

      {error && (
        <div className="alert alert-error" role="alert">
          {error}
          <button type="button" className="btn btn-sm btn-ghost mt-md" onClick={carregar}>
            Tentar novamente
          </button>
        </div>
      )}

      <SalaFiltersBar
        filters={filters}
        onChange={setFilters}
        total={salas.length}
        filtered={filtered.length}
      />

      {loading ? (
        <SkeletonGrid count={6} variant="card" />
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
      ) : filtered.length === 0 ? (
        <EmptyState
          title="Nenhuma sala encontrada"
          description="Ajuste os filtros ou a busca para ver outras salas."
          action={
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => setFilters(DEFAULT_FILTERS)}
            >
              Limpar filtros
            </button>
          }
        />
      ) : (
        <div className="sala-cards-grid">
          {filtered.map((sala) => (
            <SalaCard
              key={sala.id}
              sala={sala}
              onDuplicate={handleDuplicate}
              onArchive={setArchiveId}
              duplicating={duplicatingId === sala.id}
            />
          ))}
        </div>
      )}

      <ConfirmDialog
        open={!!archiveId}
        title="Arquivar sala"
        message="A sala será arquivada e removida da listagem ativa. Esta ação não exclui os dados fisicamente. Deseja continuar?"
        confirmLabel={archiving ? 'Arquivando...' : 'Arquivar'}
        variant="danger"
        onConfirm={handleArchive}
        onCancel={() => setArchiveId(null)}
      />
    </div>
  );
}
