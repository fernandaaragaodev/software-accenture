import type { FormEvent } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { getMe } from '../../api/auth';
import { equipesApi } from '../../api/equipes';
import { reservasApi } from '../../api/reservas';
import { salasApi } from '../../api/salas';
import { tiposEquipamentoApi } from '../../api/tipos-equipamento';
import { Alert, PageHeader } from '../../components/ui';
import { useAuth } from '../../context/AuthContext';
import type {
  CriterioProximidade,
  EquipeResponse,
  EquipeResumoResponse,
  HorarioDisponibilidade,
  PessoaReservaRequest,
  SalaResponse,
  TipoEquipamentoResponse,
  UsuarioResumo,
} from '../../types';

interface PessoaForm extends PessoaReservaRequest {
  key: number;
}

const DIAS_SEMANA = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo'];

function criarPessoaVazia(key: number, usuarioId?: string): PessoaForm {
  return {
    key,
    usuarioId,
    tipoPreferido1: '',
    tipoPreferido2: '',
    tipoPreferido3: '',
  };
}

function toTimeInput(value: string) {
  return value.slice(0, 5);
}

function toApiTime(value: string) {
  return value.length === 5 ? `${value}:00` : value;
}

function diaSemanaDaData(data: string) {
  if (!data) return -1;
  const date = new Date(`${data}T12:00:00`);
  return (date.getDay() + 6) % 7;
}

export function NovaReservaPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const { hasRole } = useAuth();
  const isGestor = hasRole('GESTOR_RESERVAS');
  const isUsuarioFinal = hasRole('USUARIO_FINAL');

  const [salas, setSalas] = useState<SalaResponse[]>([]);
  const [tiposEquipamento, setTiposEquipamento] = useState<TipoEquipamentoResponse[]>([]);
  const [equipes, setEquipes] = useState<EquipeResumoResponse[]>([]);
  const [equipeSelecionada, setEquipeSelecionada] = useState<EquipeResponse | null>(null);
  const [horarioDia, setHorarioDia] = useState<HorarioDisponibilidade | null>(null);
  const [usuarioAtual, setUsuarioAtual] = useState<UsuarioResumo | null>(null);

  const [salaId, setSalaId] = useState(searchParams.get('salaId') ?? '');
  const [equipeId, setEquipeId] = useState('');
  const [dataReserva, setDataReserva] = useState(searchParams.get('data') ?? '');
  const [horaInicio, setHoraInicio] = useState('08:00');
  const [horaFim, setHoraFim] = useState('18:00');
  const [quantidadePessoas, setQuantidadePessoas] = useState(1);
  const [criterioProximidade, setCriterioProximidade] = useState<CriterioProximidade>('PREFERENCIAL');
  const [pessoas, setPessoas] = useState<PessoaForm[]>([criarPessoaVazia(1)]);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [carregandoDados, setCarregandoDados] = useState(true);

  const opcoesEquipamento = useMemo(
    () => tiposEquipamento.filter((t) => t.ativo),
    [tiposEquipamento],
  );

  const opcoesPessoas = useMemo(() => {
    if (isGestor && equipeSelecionada) {
      const map = new Map<string, UsuarioResumo>();
      equipeSelecionada.gestores.forEach((g) => map.set(g.id, g));
      equipeSelecionada.membros.forEach((m) => map.set(m.id, m));
      return Array.from(map.values());
    }
    return usuarioAtual ? [usuarioAtual] : [];
  }, [isGestor, equipeSelecionada, usuarioAtual]);

  useEffect(() => {
    async function carregarDados() {
      setCarregandoDados(true);
      try {
        const [salasData, tiposData, me] = await Promise.all([
          salasApi.listar(),
          tiposEquipamentoApi.listar(),
          getMe(),
        ]);

        const salasAtivas = salasData.filter((s) => s.status === 'ATIVA');
        setSalas(salasAtivas);
        setTiposEquipamento(tiposData);

        const atual: UsuarioResumo = { id: me.id, nome: me.nome, email: me.email };
        setUsuarioAtual(atual);

        const salaInicial = searchParams.get('salaId') ?? salasAtivas[0]?.id ?? '';
        setSalaId(salaInicial);

        if (isGestor) {
          const equipesData = await equipesApi.listar();
          setEquipes(equipesData);
          if (equipesData.length > 0) {
            setEquipeId(equipesData[0].id);
          }
        }

        setPessoas([criarPessoaVazia(1, isUsuarioFinal ? me.id : '')]);
      } catch (err) {
        setError(err instanceof ApiException ? err.message : 'Erro ao carregar dados da reserva');
      } finally {
        setCarregandoDados(false);
      }
    }

    carregarDados();
  }, [isGestor, isUsuarioFinal, searchParams]);

  useEffect(() => {
    if (!isGestor || !equipeId) {
      setEquipeSelecionada(null);
      return;
    }

    equipesApi.obter(equipeId).then(setEquipeSelecionada).catch(() => setEquipeSelecionada(null));
  }, [isGestor, equipeId]);

  useEffect(() => {
    if (!salaId || !dataReserva) {
      setHorarioDia(null);
      return;
    }

    salasApi
      .listarRegrasDisponibilidade(salaId)
      .then((regra) => {
        const dia = diaSemanaDaData(dataReserva);
        const horario = regra.horarios.find((h) => h.diaSemana === dia) ?? null;
        setHorarioDia(horario);
        if (horario) {
          setHoraInicio(toTimeInput(horario.horaAbertura));
          setHoraFim(toTimeInput(horario.horaFechamento));
        }
      })
      .catch(() => setHorarioDia(null));
  }, [salaId, dataReserva]);

  useEffect(() => {
    if (isUsuarioFinal && usuarioAtual) {
      setQuantidadePessoas(1);
      setPessoas([criarPessoaVazia(1, usuarioAtual.id)]);
      return;
    }

    setPessoas((prev) => {
      const next: PessoaForm[] = [];
      for (let i = 0; i < quantidadePessoas; i++) {
        next.push(prev[i] ?? criarPessoaVazia(i + 1));
      }
      return next;
    });
  }, [quantidadePessoas, isUsuarioFinal, usuarioAtual]);

  function updatePessoa(index: number, field: keyof PessoaReservaRequest, value: string) {
    setPessoas((prev) =>
      prev.map((p, i) => (i === index ? { ...p, [field]: value || undefined } : p)),
    );
  }

  function nomePessoa(usuarioId?: string) {
    if (!usuarioId) return '';
    return opcoesPessoas.find((p) => p.id === usuarioId)?.nome ?? '';
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');

    if (isGestor && !equipeId) {
      setError('Selecione a equipe para a reserva.');
      return;
    }

    if (horaInicio >= horaFim) {
      setError('A hora de início deve ser anterior à hora de fim.');
      return;
    }

    if (horarioDia) {
      const abertura = toTimeInput(horarioDia.horaAbertura);
      const fechamento = toTimeInput(horarioDia.horaFechamento);
      if (horaInicio < abertura || horaFim > fechamento) {
        setError(`O horário deve estar entre ${abertura} e ${fechamento}.`);
        return;
      }
    }

    if (isGestor) {
      const ids = pessoas.map((p) => p.usuarioId).filter(Boolean);
      if (ids.length !== pessoas.length) {
        setError('Selecione o participante para cada pessoa.');
        return;
      }
      if (new Set(ids).size !== ids.length) {
        setError('Cada membro só pode ser selecionado uma vez na mesma reserva.');
        return;
      }
    }

    setLoading(true);
    try {
      const reserva = await reservasApi.solicitar({
        salaId,
        equipeId: isGestor ? equipeId : undefined,
        dataReserva,
        horaInicio: toApiTime(horaInicio),
        horaFim: toApiTime(horaFim),
        quantidadePessoas,
        criterioProximidade,
        pessoas: pessoas.map(({ key: _, ...p }) => p),
      });
      navigate(`/reservas/${reserva.id}`);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao solicitar reserva');
    } finally {
      setLoading(false);
    }
  }

  if (carregandoDados) {
    return (
      <div className="page-center">
        <div className="spinner" />
      </div>
    );
  }

  return (
    <div>
      <PageHeader
        title="Nova Reserva"
        subtitle="A alocação de posições é feita automaticamente pelo agente de IA"
      />

      <div className="card form-card">
        <form onSubmit={handleSubmit} className="form">
          <Alert message={error} />

          <div className="form-grid">
            <label>
              Sala *
              {salas.length > 0 ? (
                <select value={salaId} onChange={(e) => setSalaId(e.target.value)} required>
                  <option value="">Selecione a sala...</option>
                  {salas.map((s) => (
                    <option key={s.id} value={s.id}>
                      {s.nome} (cap. {s.capacidadeMaxima})
                    </option>
                  ))}
                </select>
              ) : (
                <p className="muted">Nenhuma sala ativa disponível.</p>
              )}
            </label>

            {isGestor && (
              <label>
                Equipe *
                <select value={equipeId} onChange={(e) => setEquipeId(e.target.value)} required>
                  <option value="">Selecione a equipe...</option>
                  {equipes.map((eq) => (
                    <option key={eq.id} value={eq.id}>
                      {eq.nome} ({eq.quantidadeMembros} membros)
                    </option>
                  ))}
                </select>
              </label>
            )}

            <label>
              Data *
              <input
                type="date"
                value={dataReserva}
                onChange={(e) => setDataReserva(e.target.value)}
                required
              />
            </label>

            <label>
              Hora início *
              <input
                type="time"
                value={horaInicio}
                onChange={(e) => setHoraInicio(e.target.value)}
                min={horarioDia ? toTimeInput(horarioDia.horaAbertura) : undefined}
                max={horarioDia ? toTimeInput(horarioDia.horaFechamento) : undefined}
                required
              />
            </label>

            <label>
              Hora fim *
              <input
                type="time"
                value={horaFim}
                onChange={(e) => setHoraFim(e.target.value)}
                min={horarioDia ? toTimeInput(horarioDia.horaAbertura) : undefined}
                max={horarioDia ? toTimeInput(horarioDia.horaFechamento) : undefined}
                required
              />
            </label>

            <label>
              Quantidade de pessoas *
              <input
                type="number"
                min={1}
                value={quantidadePessoas}
                onChange={(e) => setQuantidadePessoas(Number(e.target.value))}
                required
                disabled={isUsuarioFinal}
              />
              {isUsuarioFinal && (
                <small className="muted">Reserva individual — apenas para você.</small>
              )}
            </label>

            <label>
              Critério de proximidade *
              <select
                value={criterioProximidade}
                onChange={(e) => setCriterioProximidade(e.target.value as CriterioProximidade)}
              >
                <option value="PREFERENCIAL">Preferencial</option>
                <option value="OBRIGATORIA">Obrigatória</option>
              </select>
            </label>
          </div>

          {horarioDia && (
            <p className="muted">
              Horário permitido em {DIAS_SEMANA[horarioDia.diaSemana]}:{' '}
              {toTimeInput(horarioDia.horaAbertura)} – {toTimeInput(horarioDia.horaFechamento)}
            </p>
          )}

          {!horarioDia && dataReserva && salaId && (
            <Alert message="A sala não possui horário configurado para o dia selecionado." />
          )}

          <h3 className="section-title">Pessoas</h3>
          {pessoas.map((pessoa, index) => (
            <div key={pessoa.key} className="person-card">
              <h4>Pessoa {index + 1}</h4>
              <div className="form-grid">
                {isGestor ? (
                  <label>
                    Participante *
                    <select
                      value={pessoa.usuarioId ?? ''}
                      onChange={(e) => updatePessoa(index, 'usuarioId', e.target.value)}
                      required
                      disabled={!equipeSelecionada}
                    >
                      <option value="">Selecione...</option>
                      {opcoesPessoas.map((u) => (
                        <option key={u.id} value={u.id}>
                          {u.nome} ({u.email})
                        </option>
                      ))}
                    </select>
                  </label>
                ) : (
                  <label>
                    Participante
                    <input value={nomePessoa(pessoa.usuarioId) || usuarioAtual?.nome || ''} readOnly />
                  </label>
                )}
                <label>
                  Equipamento preferido 1
                  <select
                    value={pessoa.tipoPreferido1 ?? ''}
                    onChange={(e) => updatePessoa(index, 'tipoPreferido1', e.target.value)}
                  >
                    <option value="">Sem preferência</option>
                    {opcoesEquipamento.map((t) => (
                      <option key={t.id} value={t.nome}>
                        {t.nome}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Equipamento preferido 2
                  <select
                    value={pessoa.tipoPreferido2 ?? ''}
                    onChange={(e) => updatePessoa(index, 'tipoPreferido2', e.target.value)}
                  >
                    <option value="">Sem preferência</option>
                    {opcoesEquipamento.map((t) => (
                      <option key={t.id} value={t.nome}>
                        {t.nome}
                      </option>
                    ))}
                  </select>
                </label>
                <label>
                  Equipamento preferido 3
                  <select
                    value={pessoa.tipoPreferido3 ?? ''}
                    onChange={(e) => updatePessoa(index, 'tipoPreferido3', e.target.value)}
                  >
                    <option value="">Sem preferência</option>
                    {opcoesEquipamento.map((t) => (
                      <option key={t.id} value={t.nome}>
                        {t.nome}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
            </div>
          ))}

          <div className="form-actions">
            <Link to="/reservas" className="btn btn-ghost">Voltar</Link>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading || !salaId || (!horarioDia && !!dataReserva)}
            >
              {loading ? 'Processando alocação...' : 'Solicitar reserva'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
