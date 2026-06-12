import type { SalaFilters } from '../../utils/salas';
import type { StatusSala } from '../../types';

interface SalaFiltersBarProps {
  filters: SalaFilters;
  onChange: (filters: SalaFilters) => void;
  total: number;
  filtered: number;
}

export function SalaFiltersBar({ filters, onChange, total, filtered }: SalaFiltersBarProps) {
  function update<K extends keyof SalaFilters>(key: K, value: SalaFilters[K]) {
    onChange({ ...filters, [key]: value });
  }

  return (
    <div className="sala-filters card">
      <div className="sala-filters-row">
        <label className="sala-search">
          Buscar
          <input
            type="search"
            placeholder="Nome ou bloco..."
            value={filters.busca}
            onChange={(e) => update('busca', e.target.value)}
          />
        </label>

        <label>
          Status
          <select
            value={filters.status}
            onChange={(e) => update('status', e.target.value as StatusSala | '')}
          >
            <option value="">Todos</option>
            <option value="ATIVA">Ativa</option>
            <option value="INATIVA">Inativa</option>
            <option value="MANUTENCAO">Manutenção</option>
          </select>
        </label>

        <label>
          Capacidade mín.
          <input
            type="number"
            min={0}
            placeholder="0"
            value={filters.capacidadeMin}
            onChange={(e) => update('capacidadeMin', e.target.value)}
          />
        </label>

        <label>
          Layout
          <select
            value={filters.possuiLayout}
            onChange={(e) => update('possuiLayout', e.target.value as '' | 'sim' | 'nao')}
          >
            <option value="">Todos</option>
            <option value="sim">Com layout</option>
            <option value="nao">Sem layout</option>
          </select>
        </label>

        <label>
          Disponibilidade
          <select
            value={filters.possuiDisponibilidade}
            onChange={(e) =>
              update('possuiDisponibilidade', e.target.value as '' | 'sim' | 'nao')
            }
          >
            <option value="">Todos</option>
            <option value="sim">Configurada</option>
            <option value="nao">Pendente</option>
          </select>
        </label>

        <label>
          Ordenar por
          <select
            value={filters.ordenacao}
            onChange={(e) =>
              update('ordenacao', e.target.value as SalaFilters['ordenacao'])
            }
          >
            <option value="nome">Nome</option>
            <option value="createdAt">Data de criação</option>
            <option value="capacidadeMaxima">Capacidade</option>
          </select>
        </label>
      </div>
      <p className="sala-filters-count muted">
        Exibindo {filtered} de {total} sala(s)
      </p>
    </div>
  );
}
