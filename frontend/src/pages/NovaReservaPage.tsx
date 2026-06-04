import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { reservasApi } from '../api/reservas.api';
import { salasApi } from '../api/salas.api';
import { Button } from '../components/ui/Button';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { Input } from '../components/ui/Input';
import { Loading } from '../components/ui/Loading';
import { Select } from '../components/ui/Select';
import { useAuth } from '../hooks/useAuth';
import type { CriterioProximidade, PessoaReserva, Reserva } from '../types/reserva.types';
import type { Sala } from '../types/sala.types';
import { getApiErrorMessage } from '../utils/apiError';
import { addStoredReservaId } from '../utils/permissions';

const TIPOS_POSICAO = [
  'Estação Padrão',
  'Hot Desk',
  'Posição Acessível',
  'Estação Executiva',
];

const STEPS = [
  'Escolher sala',
  'Escolher data',
  'Quantidade de pessoas',
  'Preferências por pessoa',
  'Critério de proximidade',
];

export function NovaReservaPage() {
  const navigate = useNavigate();
  const { setUsuarioId } = useAuth();
  const [step, setStep] = useState(0);
  const [salas, setSalas] = useState<Sala[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [resultado, setResultado] = useState<Reserva | null>(null);

  const [salaId, setSalaId] = useState('');
  const [dataReserva, setDataReserva] = useState('');
  const [quantidadePessoas, setQuantidadePessoas] = useState(1);
  const [pessoas, setPessoas] = useState<PessoaReserva[]>([]);
  const [criterioProximidade, setCriterioProximidade] =
    useState<CriterioProximidade>('PREFERENCIAL');

  const [salaManual, setSalaManual] = useState('');

  useEffect(() => {
    salasApi
      .listar()
      .then(({ data }) => {
        setSalas(data.filter((s) => s.status === 'ATIVA'));
        if (data[0]) setSalaId(data[0].id);
      })
      .catch(() => {
        setSalas([]);
      })
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    const p: PessoaReserva[] = [];
    for (let i = 0; i < quantidadePessoas; i++) {
      p.push({
        usuarioId: null,
        nomeExterno: `Pessoa ${i + 1}`,
        tipoPreferido1: TIPOS_POSICAO[0],
        tipoPreferido2: TIPOS_POSICAO[1] ?? TIPOS_POSICAO[0],
        tipoPreferido3: TIPOS_POSICAO[2] ?? TIPOS_POSICAO[0],
      });
    }
    setPessoas(p);
  }, [quantidadePessoas]);

  const next = () => {
    setError(null);
    const idSala = salaId || salaManual;
    if (step === 0 && !idSala) {
      setError('Selecione ou informe o ID da sala.');
      return;
    }
    if (step === 0 && salaManual && !salaId) setSalaId(salaManual);
    if (step === 1 && !dataReserva) {
      setError('Informe a data da reserva.');
      return;
    }
    if (step === 2 && quantidadePessoas < 1) {
      setError('Informe ao menos 1 pessoa.');
      return;
    }
    setStep((s) => Math.min(s + 1, STEPS.length - 1));
  };

  const back = () => setStep((s) => Math.max(s - 1, 0));

  const submit = async () => {
    setSubmitting(true);
    setError(null);
    try {
      const idSala = salaId || salaManual;
      const { data, status } = await reservasApi.solicitar({
        salaId: idSala,
        dataReserva,
        quantidadePessoas,
        criterioProximidade,
        pessoas,
      });
      setResultado(data);
      addStoredReservaId(data.id);
      if (data.solicitanteId) setUsuarioId(data.solicitanteId);
      if (status === 409 || data.status === 'REJEITADA') {
        setError(data.motivoRejeicao ?? 'Reserva rejeitada pelo sistema.');
      }
    } catch (err) {
      const reserva = (err as { response?: { data?: Reserva; status?: number } })?.response
        ?.data;
      if (reserva?.id) {
        setResultado(reserva);
        addStoredReservaId(reserva.id);
        if (reserva.solicitanteId) setUsuarioId(reserva.solicitanteId);
        setError(reserva.motivoRejeicao ?? getApiErrorMessage(err));
      } else {
        setError(getApiErrorMessage(err));
      }
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <Loading />;

  if (resultado) {
    return (
      <div className="mx-auto max-w-lg space-y-4 rounded-xl border border-border bg-white p-6 shadow-sm">
        <h1 className="text-xl font-bold text-slate-900">
          {resultado.status === 'REJEITADA' ? 'Reserva rejeitada' : 'Reserva registrada'}
        </h1>
        <p className="text-sm text-slate-600">
          Status: <strong>{resultado.status}</strong>
        </p>
        {resultado.motivoRejeicao && (
          <p className="text-sm text-red-700">{resultado.motivoRejeicao}</p>
        )}
        {resultado.alocacoes.length > 0 && (
          <ul className="text-sm text-slate-700">
            {resultado.alocacoes.map((a) => (
              <li key={a.posicaoId}>
                {a.posicaoIdentificador} — {a.posicaoTipo}
              </li>
            ))}
          </ul>
        )}
        <div className="flex gap-2">
          <Button onClick={() => navigate(`/reservas/${resultado.id}`, { state: { reserva: resultado } })}>
            Ver detalhes
          </Button>
          <Button variant="secondary" onClick={() => navigate('/reservas')}>
            Lista de reservas
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-2xl space-y-6">
      <h1 className="text-2xl font-bold text-slate-900">Nova reserva</h1>

      <div className="flex gap-2">
        {STEPS.map((label, i) => (
          <div
            key={label}
            className={`flex-1 rounded-lg px-2 py-2 text-center text-xs font-medium ${
              i === step
                ? 'bg-primary-600 text-white'
                : i < step
                  ? 'bg-primary-100 text-primary-800'
                  : 'bg-slate-100 text-slate-500'
            }`}
          >
            {i + 1}. {label}
          </div>
        ))}
      </div>

      {error && <ErrorMessage message={error} />}

      <div className="rounded-xl border border-border bg-white p-6 shadow-sm">
        {step === 0 &&
          (salas.length > 0 ? (
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
              placeholder="Cole o UUID da sala"
            />
          ))}
        {step === 1 && (
          <Input
            label="Data da reserva"
            type="date"
            value={dataReserva}
            onChange={(e) => setDataReserva(e.target.value)}
          />
        )}
        {step === 2 && (
          <Input
            label="Quantidade de pessoas"
            type="number"
            min={1}
            max={20}
            value={quantidadePessoas}
            onChange={(e) => setQuantidadePessoas(Number(e.target.value))}
          />
        )}
        {step === 3 && (
          <div className="space-y-4">
            {pessoas.map((p, idx) => (
              <div key={idx} className="rounded-lg border border-border p-3">
                <p className="mb-2 font-medium text-slate-700">Pessoa {idx + 1}</p>
                <Input
                  label="Nome"
                  value={p.nomeExterno}
                  onChange={(e) => {
                    const copy = [...pessoas];
                    copy[idx] = { ...copy[idx], nomeExterno: e.target.value };
                    setPessoas(copy);
                  }}
                />
                <div className="mt-2 grid gap-2 sm:grid-cols-3">
                  {[1, 2, 3].map((n) => (
                    <Select
                      key={n}
                      label={`Preferência ${n}`}
                      value={
                        p[`tipoPreferido${n}` as keyof PessoaReserva] as string
                      }
                      onChange={(e) => {
                        const copy = [...pessoas];
                        copy[idx] = {
                          ...copy[idx],
                          [`tipoPreferido${n}`]: e.target.value,
                        };
                        setPessoas(copy);
                      }}
                      options={TIPOS_POSICAO.map((t) => ({ value: t, label: t }))}
                    />
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
        {step === 4 && (
          <Select
            label="Critério de proximidade"
            value={criterioProximidade}
            onChange={(e) =>
              setCriterioProximidade(e.target.value as CriterioProximidade)
            }
            options={[
              { value: 'PREFERENCIAL', label: 'Preferencial' },
              { value: 'OBRIGATORIA', label: 'Obrigatória' },
            ]}
          />
        )}
      </div>

      <div className="flex justify-between">
        <Button variant="ghost" onClick={back} disabled={step === 0}>
          Voltar
        </Button>
        {step < STEPS.length - 1 ? (
          <Button onClick={next}>Próximo</Button>
        ) : (
          <Button onClick={() => void submit()} loading={submitting}>
            Confirmar reserva
          </Button>
        )}
      </div>
    </div>
  );
}
