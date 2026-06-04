import { useCallback, useState, type MouseEvent } from 'react';
import type { Posicao, PosicaoMapStatus } from '../../types/posicao.types';

interface RoomLayoutMapProps {
  posicoes: Posicao[];
  editable?: boolean;
  onCoordChange?: (posicaoId: string, coordX: number, coordY: number) => void;
  getStatus?: (posicao: Posicao) => PosicaoMapStatus;
  width?: number;
  height?: number;
}

const statusColors: Record<PosicaoMapStatus, string> = {
  livre: '#22c55e',
  ocupada: '#ef4444',
  inativa: '#94a3b8',
};

function defaultStatus(posicao: Posicao): PosicaoMapStatus {
  if (posicao.status === 'INATIVA') return 'inativa';
  return 'livre';
}

export function RoomLayoutMap({
  posicoes,
  editable = false,
  onCoordChange,
  getStatus = defaultStatus,
  width = 600,
  height = 400,
}: RoomLayoutMapProps) {
  const [draggingId, setDraggingId] = useState<string | null>(null);

  const scaleX = width / 100;
  const scaleY = height / 100;

  const handleMouseMove = useCallback(
    (e: MouseEvent<SVGSVGElement>) => {
      if (!draggingId || !editable || !onCoordChange) return;
      const svg = e.currentTarget;
      const rect = svg.getBoundingClientRect();
      const x = ((e.clientX - rect.left) / rect.width) * 100;
      const y = ((e.clientY - rect.top) / rect.height) * 100;
      onCoordChange(
        draggingId,
        Math.round(Math.max(0, Math.min(100, x)) * 10) / 10,
        Math.round(Math.max(0, Math.min(100, y)) * 10) / 10,
      );
    },
    [draggingId, editable, onCoordChange],
  );

  const handleMouseUp = () => setDraggingId(null);

  if (posicoes.length === 0) {
    return (
      <p className="text-center text-sm text-slate-500">
        Nenhuma posição cadastrada para exibir no mapa.
      </p>
    );
  }

  return (
    <div className="overflow-auto rounded-xl border border-border bg-slate-50 p-4">
      <svg
        viewBox={`0 0 ${width} ${height}`}
        className="mx-auto w-full max-w-full bg-white shadow-inner"
        style={{ maxHeight: 480 }}
        onMouseMove={handleMouseMove}
        onMouseUp={handleMouseUp}
        onMouseLeave={handleMouseUp}
      >
        <rect width={width} height={height} fill="#f8fafc" />
        {posicoes.map((p) => {
          const status = getStatus(p);
          const cx = Number(p.coordX) * scaleX;
          const cy = Number(p.coordY) * scaleY;
          return (
            <g
              key={p.id}
              transform={`translate(${cx}, ${cy})`}
              style={{ cursor: editable ? 'grab' : 'default' }}
              onMouseDown={() => editable && setDraggingId(p.id)}
            >
              <circle r={18} fill={statusColors[status]} opacity={0.9} />
              <text
                textAnchor="middle"
                dominantBaseline="middle"
                fill="white"
                fontSize={10}
                fontWeight="bold"
              >
                {p.identificador}
              </text>
              <title>
                {p.identificador} — {p.tipo} ({status})
              </title>
            </g>
          );
        })}
      </svg>
      <div className="mt-3 flex flex-wrap justify-center gap-4 text-xs text-slate-600">
        <span className="flex items-center gap-1">
          <span className="inline-block h-3 w-3 rounded-full bg-emerald-500" /> Livre
        </span>
        <span className="flex items-center gap-1">
          <span className="inline-block h-3 w-3 rounded-full bg-red-500" /> Ocupada
        </span>
        <span className="flex items-center gap-1">
          <span className="inline-block h-3 w-3 rounded-full bg-slate-400" /> Inativa
        </span>
      </div>
    </div>
  );
}
