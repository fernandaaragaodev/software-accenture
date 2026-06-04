import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { disponibilidadeApi } from '../api/disponibilidade.api';
import { posicoesApi } from '../api/posicoes.api';
import { salasApi } from '../api/salas.api';
import { RoomLayoutMap } from '../components/layout/RoomLayoutMap';
import { Button } from '../components/ui/Button';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { Input } from '../components/ui/Input';
import { Loading } from '../components/ui/Loading';
import { Select } from '../components/ui/Select';
import type { Sala } from '../types/sala.types';
import type { ValidacaoDisponibilidade } from '../types/disponibilidade.types';
import type { Posicao, PosicaoMapStatus } from '../types/posicao.types';
import { getApiErrorMessage } from '../utils/apiError';
import { formatDate } from '../utils/formatters';
import { useAuth } from '../hooks/useAuth';
import { canManageSalas } from '../utils/permissions';

export function DisponibilidadePage() {
  const { id: paramSalaId } = useParams<{ id: string }>();
  const { roles } = useAuth();
  const [salas, setSalas] = useState<Sala[]>([]);
  const [salaId, setSalaId] = useState(paramSalaId ?? '');
  const [data, setData] = useState('');
  const [resultado, setResultado] = useState<ValidacaoDisponibilidade | null>(null);
  const [posicoes, setPosicoes] = useState<Posicao[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [salaManual, setSalaManual] = useState('');

  useEffect(() => {
    if (paramSalaId) setSalaId(paramSalaId);
    salasApi
      .listar()
      .then(({ data }) => {
        setSalas(data);
        if (paramSalaId) setSalaId(paramSalaId);
        else if (data[0]) setSalaId(data[0].id);
      })
      .catch(() => setSalas([]));
  }, [paramSalaId]);

  const consultar = async () => {
    if (!salaId || !data) {
      setError('Selecione a sala e a data.');
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const [disp, pos] = await Promise.all([
        disponibilidadeApi.consultar(salaId, data),
        posicoesApi.listarPorSala(salaId).catch(() => ({ data: [] as Posicao[] })),
      ]);
      setResultado(disp.data);
      setPosicoes(pos.data);
    } catch (err) {
      setError(getApiErrorMessage(err));
      setResultado(null);
    } finally {
      setLoading(false);
    }
  };

  const getStatus = (p: Posicao): PosicaoMapStatus => {
    if (p.status === 'INATIVA') return 'inativa';
    if (!resultado?.disponivel) return 'ocupada';
    return 'livre';
  };

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-900">Consulta de disponibilidade</h1>
        {salaId && (
          <Link to={`/salas/${salaId}`}>
            <Button variant="ghost">Voltar à sala</Button>
          </Link>
        )}
      </div>

      <div className="grid gap-4 rounded-xl border border-border bg-white p-4 shadow-sm sm:grid-cols-3">
        {salas.length > 0 ? (
          <Select
            label="Sala"
            value={salaId}
            onChange={(e) => setSalaId(e.target.value)}
            options={salas.map((s) => ({ value: s.id, label: s.nome }))}
          />
        ) : (
          <Input
            label="ID da sala (UUID)"
            value={salaManual}
            onChange={(e) => {
              setSalaManual(e.target.value);
              setSalaId(e.target.value);
            }}
          />
        )}
        <Input
          label="Data"
          type="date"
          value={data}
          onChange={(e) => setData(e.target.value)}
        />
        <div className="flex items-end">
          <Button onClick={() => void consultar()} loading={loading}>
            Consultar
          </Button>
        </div>
      </div>

      {error && <ErrorMessage message={error} />}

      {resultado && (
        <>
          <div
            className={`rounded-lg px-4 py-3 text-sm ${
              resultado.disponivel
                ? 'bg-emerald-50 text-emerald-800'
                : 'bg-amber-50 text-amber-800'
            }`}
          >
            <p className="font-semibold">
              {resultado.disponivel ? 'Disponível' : 'Indisponível'} — {formatDate(resultado.data)}
            </p>
            <p className="mt-1">{resultado.mensagem}</p>
          </div>

          {posicoes.length > 0 && (
            <RoomLayoutMap posicoes={posicoes} getStatus={getStatus} editable={false} />
          )}
        </>
      )}

      {canManageSalas(roles) && salaId && (
        <p className="text-xs text-slate-500">
          Administradores podem configurar regras em{' '}
          <Link to="/salas" className="text-primary-600 underline">
            gestão de salas
          </Link>
          .
        </p>
      )}
    </div>
  );
}
