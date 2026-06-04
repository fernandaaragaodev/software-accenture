import type { ReactNode } from 'react';

interface Column<T> {
  key: string;
  header: string;
  render: (row: T) => ReactNode;
  className?: string;
}

interface TableProps<T> {
  columns: Column<T>[];
  data: T[];
  keyExtractor: (row: T) => string;
  emptyMessage?: string;
}

export function Table<T>({
  columns,
  data,
  keyExtractor,
  emptyMessage = 'Nenhum registro encontrado.',
}: TableProps<T>) {
  if (data.length === 0) {
    return (
      <p className="rounded-lg border border-dashed border-border bg-white px-4 py-8 text-center text-sm text-slate-500">
        {emptyMessage}
      </p>
    );
  }

  return (
    <div className="overflow-x-auto rounded-xl border border-border bg-white shadow-sm">
      <table className="min-w-full divide-y divide-border text-sm">
        <thead className="bg-slate-50">
          <tr>
            {columns.map((col) => (
              <th
                key={col.key}
                className={`px-4 py-3 text-left font-semibold text-slate-600 ${col.className ?? ''}`}
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-border">
          {data.map((row) => (
            <tr key={keyExtractor(row)} className="hover:bg-slate-50/80">
              {columns.map((col) => (
                <td key={col.key} className={`px-4 py-3 text-slate-700 ${col.className ?? ''}`}>
                  {col.render(row)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
