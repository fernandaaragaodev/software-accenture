import { useCallback, useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { disponibilidadeApi, layoutsApi } from '../api/disponibilidade.api';
import { posicoesApi } from '../api/posicoes.api';
import { RoomLayoutMap } from '../components/layout/RoomLayoutMap';
import { Button } from '../components/ui/Button';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { Input } from '../components/ui/Input';
import { Loading } from '../components/ui/Loading';
import type { Layout } from '../types/disponibilidade.types';
import type { Posicao } from '../types/posicao.types';
import { getApiErrorMessage } from '../utils/apiError';

export function LayoutSalaPage() {
  const { id: salaId } = useParams<{ id: string }>();
  const [layout, setLayout] = useState<Layout | null>(null);
  const [posicoes, setPosicoes] = useState<Posicao[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [manualId, setManualId] = useState('');
  const [coordX, setCoordX] = useState('');
  const [coordY, setCoordY] = useState('');

  const load = useCallback(async () => {
    if (!salaId) return;
    setLoading(true);
    setError(null);
    try {
      const [layoutRes, posRes] = await Promise.all([
        layoutsApi.buscarAtivo(salaId).catch(() => ({ data: null })),
        posicoesApi.listarPorSala(salaId),
      ]);
      setLayout(layoutRes.data);
      setPosicoes(posRes.data);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [salaId]);

  useEffect(() => {
    void load();
  }, [load]);

  const handleCoordChange = async (posicaoId: string, x: number, y: number) => {
    try {
      await layoutsApi.ajustarCoordenadasPosicao(posicaoId, { coordX: x, coordY: y });
      setPosicoes((prev) =>
        prev.map((p) => (p.id === posicaoId ? { ...p, coordX: x, coordY: y } : p)),
      );
    } catch (err) {
      setError(getApiErrorMessage(err));
    }
  };

  const handleManualSave = async () => {
    if (!manualId || !coordX || !coordY) return;
    try {
      await posicoesApi.atualizarCoordenadas(manualId, {
        coordX: Number(coordX),
        coordY: Number(coordY),
      });
      setSuccess('Coordenadas atualizadas.');
      await load();
    } catch (err) {
      setError(getApiErrorMessage(err));
    }
  };

  const handleCriarLayout = async () => {
    if (!salaId) return;
    try {
      const { data } = await layoutsApi.criar({ salaId, versao: `v${Date.now()}` });
      setLayout(data);
      setSuccess('Layout criado.');
    } catch (err) {
      setError(getApiErrorMessage(err));
    }
  };

  const handleAprovar = async () => {
    if (!layout) return;
    try {
      const { data } = await layoutsApi.aprovar(layout.id);
      setLayout(data);
      setSuccess('Layout aprovado com sucesso.');
    } catch (err) {
      setError(getApiErrorMessage(err));
    }
  };

  if (loading) return <Loading />;
  if (!salaId) return <ErrorMessage message="Sala não informada." />;

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-slate-900">Layout da sala</h1>
        <Link to={`/salas/${salaId}`}>
          <Button variant="ghost">Voltar</Button>
        </Link>
      </div>

      {error && <ErrorMessage message={error} />}
      {success && (
        <p className="rounded-lg bg-emerald-50 px-4 py-3 text-sm text-emerald-800">{success}</p>
      )}

      <div className="flex flex-wrap gap-2">
        {!layout && <Button onClick={() => void handleCriarLayout()}>Criar layout</Button>}
        {layout && !layout.aprovadoEm && (
          <Button onClick={() => void handleAprovar()}>Aprovar layout</Button>
        )}
      </div>

      {layout && (
        <p className="text-sm text-slate-600">
          Versão: {layout.versao} — {layout.aprovadoEm ? 'Aprovado' : 'Pendente de aprovação'}
        </p>
      )}

      <RoomLayoutMap
        posicoes={posicoes}
        editable
        onCoordChange={(id, x, y) => void handleCoordChange(id, x, y)}
      />

      <div className="rounded-xl border border-border bg-white p-4 shadow-sm">
        <h2 className="mb-3 font-semibold text-slate-800">Editar coordenadas manualmente</h2>
        <div className="grid gap-3 sm:grid-cols-4">
          <SelectPosicao posicoes={posicoes} value={manualId} onChange={setManualId} />
          <Input
            label="Coord X"
            type="number"
            step="0.1"
            value={coordX}
            onChange={(e) => setCoordX(e.target.value)}
          />
          <Input
            label="Coord Y"
            type="number"
            step="0.1"
            value={coordY}
            onChange={(e) => setCoordY(e.target.value)}
          />
          <div className="flex items-end">
            <Button onClick={() => void handleManualSave()}>Salvar</Button>
          </div>
        </div>
      </div>
    </div>
  );
}

function SelectPosicao({
  posicoes,
  value,
  onChange,
}: {
  posicoes: Posicao[];
  value: string;
  onChange: (v: string) => void;
}) {
  return (
    <div className="w-full">
      <label className="mb-1 block text-sm font-medium text-slate-700">Posição</label>
      <select
        className="w-full rounded-lg border border-border px-3 py-2 text-sm"
        value={value}
        onChange={(e) => onChange(e.target.value)}
      >
        <option value="">Selecione</option>
        {posicoes.map((p) => (
          <option key={p.id} value={p.id}>
            {p.identificador}
          </option>
        ))}
      </select>
    </div>
  );
}
