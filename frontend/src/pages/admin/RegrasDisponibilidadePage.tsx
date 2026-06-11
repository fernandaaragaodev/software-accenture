import type { FormEvent } from 'react';
import { useEffect, useState } from 'react';
import { ApiException } from '../../api/client';
import { regrasDisponibilidadeApi } from '../../api/regras-disponibilidade';
import { salasApi } from '../../api/salas';
import { Alert, ConfirmDialog, EmptyState, PageHeader } from '../../components/ui';
import type {
  HorarioDisponibilidade,
  RegraDisponibilidadeResponse,
  SalaResponse,
} from '../../types';

const DIAS_SEMANA = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo'];

interface HorarioForm {
  diaSemana: number;
  ativo: boolean;
  horaAbertura: string;
  horaFechamento: string;
}

function horariosPadrao(): HorarioForm[] {
  return DIAS_SEMANA.map((_, index) => ({
    diaSemana: index,
    ativo: index < 5,
    horaAbertura: '08:00',
    horaFechamento: '18:00',
  }));
}

function toApiTime(value: string) {
  return value.length === 5 ? `${value}:00` : value;
}

function fromRegra(horarios: HorarioDisponibilidade[]): HorarioForm[] {
  return DIAS_SEMANA.map((_, index) => {
    const existente = horarios.find((h) => h.diaSemana === index);
    return {
      diaSemana: index,
      ativo: !!existente,
      horaAbertura: existente ? existente.horaAbertura.slice(0, 5) : '08:00',
      horaFechamento: existente ? existente.horaFechamento.slice(0, 5) : '18:00',
    };
  });
}

export function RegrasDisponibilidadePage() {
  const [regras, setRegras] = useState<RegraDisponibilidadeResponse[]>([]);
  const [salas, setSalas] = useState<SalaResponse[]>([]);
  const [nome, setNome] = useState('');
  const [antecedencia, setAntecedencia] = useState('1');
  const [horarios, setHorarios] = useState<HorarioForm[]>(horariosPadrao());
  const [editandoId, setEditandoId] = useState<string | null>(null);
  const [atribuirRegraId, setAtribuirRegraId] = useState('');
  const [atribuirSalaId, setAtribuirSalaId] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const [excluirId, setExcluirId] = useState<string | null>(null);

  function carregar() {
    Promise.all([regrasDisponibilidadeApi.listar(), salasApi.listar()])
      .then(([regrasData, salasData]) => {
        setRegras(regrasData);
        setSalas(salasData);
      })
      .catch((err) => setError(err instanceof ApiException ? err.message : 'Erro ao carregar regras'));
  }

  useEffect(() => {
    carregar();
  }, []);

  function resetForm() {
    setNome('');
    setAntecedencia('1');
    setHorarios(horariosPadrao());
    setEditandoId(null);
  }

  function iniciarEdicao(regra: RegraDisponibilidadeResponse) {
    setEditandoId(regra.id);
    setNome(regra.nome);
    setAntecedencia(String(regra.antecedenciaMinimaDias));
    setHorarios(fromRegra(regra.horarios));
  }

  function montarHorariosPayload() {
    return horarios
      .filter((h) => h.ativo)
      .map((h) => ({
        diaSemana: h.diaSemana,
        horaAbertura: toApiTime(h.horaAbertura),
        horaFechamento: toApiTime(h.horaFechamento),
      }));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);

    const payload = {
      nome,
      antecedenciaMinimaDias: Number(antecedencia),
      horarios: montarHorariosPayload(),
    };

    if (payload.horarios.length === 0) {
      setError('Selecione ao menos um dia da semana.');
      setLoading(false);
      return;
    }

    try {
      if (editandoId) {
        await regrasDisponibilidadeApi.atualizar(editandoId, payload);
        setSuccess('Regra atualizada com sucesso');
      } else {
        await regrasDisponibilidadeApi.criar(payload);
        setSuccess('Regra criada com sucesso');
      }
      resetForm();
      carregar();
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao salvar regra');
    } finally {
      setLoading(false);
    }
  }

  async function handleExcluir() {
    if (!excluirId) return;
    setError('');
    try {
      await regrasDisponibilidadeApi.excluir(excluirId);
      setSuccess('Regra excluída');
      carregar();
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao excluir regra');
    } finally {
      setExcluirId(null);
    }
  }

  async function handleAtribuir(e: FormEvent) {
    e.preventDefault();
    if (!atribuirRegraId || !atribuirSalaId) return;
    setError('');
    try {
      await regrasDisponibilidadeApi.atribuirSala(atribuirSalaId, { regraId: atribuirRegraId });
      setSuccess('Regra atribuída à sala');
      setAtribuirRegraId('');
      setAtribuirSalaId('');
      carregar();
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao atribuir regra');
    }
  }

  async function handleDesatribuir(salaId: string) {
    setError('');
    try {
      await regrasDisponibilidadeApi.desatribuirSala(salaId);
      setSuccess('Regra desatribuída da sala');
      carregar();
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao desatribuir regra');
    }
  }

  const regrasDisponiveis = regras.filter((r) => !r.salaId);
  const salasSemRegra = salas.filter((s) => !regras.some((r) => r.salaId === s.id));

  return (
    <div>
      <PageHeader
        title="Regras de Disponibilidade"
        subtitle="Crie regras independentes e atribua-as às salas"
      />
      <Alert message={error} />
      <Alert type="success" message={success} />

      <div className="detail-grid">
        <div className="card">
          <h3>{editandoId ? 'Editar regra' : 'Nova regra'}</h3>
          <form onSubmit={handleSubmit} className="form">
            <label>
              Nome *
              <input value={nome} onChange={(e) => setNome(e.target.value)} required />
            </label>
            <label>
              Antecedência mínima (dias)
              <input
                type="number"
                min={0}
                value={antecedencia}
                onChange={(e) => setAntecedencia(e.target.value)}
                required
              />
            </label>

            <h4 className="section-title">Dias e horários</h4>
            {horarios.map((h, index) => (
              <div key={h.diaSemana} className="form-grid">
                <label className="checkbox-label">
                  <input
                    type="checkbox"
                    checked={h.ativo}
                    onChange={(e) =>
                      setHorarios((prev) =>
                        prev.map((item, i) =>
                          i === index ? { ...item, ativo: e.target.checked } : item,
                        ),
                      )
                    }
                  />
                  {DIAS_SEMANA[h.diaSemana]}
                </label>
                <label>
                  Abertura
                  <input
                    type="time"
                    value={h.horaAbertura}
                    disabled={!h.ativo}
                    onChange={(e) =>
                      setHorarios((prev) =>
                        prev.map((item, i) =>
                          i === index ? { ...item, horaAbertura: e.target.value } : item,
                        ),
                      )
                    }
                  />
                </label>
                <label>
                  Fechamento
                  <input
                    type="time"
                    value={h.horaFechamento}
                    disabled={!h.ativo}
                    onChange={(e) =>
                      setHorarios((prev) =>
                        prev.map((item, i) =>
                          i === index ? { ...item, horaFechamento: e.target.value } : item,
                        ),
                      )
                    }
                  />
                </label>
              </div>
            ))}

            <div className="form-actions">
              {editandoId && (
                <button type="button" className="btn btn-ghost" onClick={resetForm}>
                  Cancelar
                </button>
              )}
              <button type="submit" className="btn btn-primary" disabled={loading}>
                {loading ? 'Salvando...' : editandoId ? 'Atualizar regra' : 'Criar regra'}
              </button>
            </div>
          </form>
        </div>

        <div className="card">
          <h3>Atribuir regra a sala</h3>
          <form onSubmit={handleAtribuir} className="form">
            <label>
              Regra disponível
              <select value={atribuirRegraId} onChange={(e) => setAtribuirRegraId(e.target.value)} required>
                <option value="">Selecione...</option>
                {regrasDisponiveis.map((r) => (
                  <option key={r.id} value={r.id}>{r.nome}</option>
                ))}
              </select>
            </label>
            <label>
              Sala
              <select value={atribuirSalaId} onChange={(e) => setAtribuirSalaId(e.target.value)} required>
                <option value="">Selecione...</option>
                {salasSemRegra.map((s) => (
                  <option key={s.id} value={s.id}>{s.nome}</option>
                ))}
              </select>
            </label>
            <button type="submit" className="btn btn-primary" disabled={!atribuirRegraId || !atribuirSalaId}>
              Atribuir
            </button>
          </form>
        </div>
      </div>

      <div className="card mt-lg">
        <h3>Regras cadastradas ({regras.length})</h3>
        {regras.length === 0 ? (
          <EmptyState title="Nenhuma regra" description="Crie a primeira regra de disponibilidade." />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Nome</th>
                  <th>Sala</th>
                  <th>Antecedência</th>
                  <th>Horários</th>
                  <th>Ações</th>
                </tr>
              </thead>
              <tbody>
                {regras.map((regra) => (
                  <tr key={regra.id}>
                    <td><strong>{regra.nome}</strong></td>
                    <td>
                      {regra.salaNome ? (
                        <>
                          {regra.salaNome}
                          <button
                            type="button"
                            className="btn btn-sm btn-ghost ml-sm"
                            onClick={() => handleDesatribuir(regra.salaId!)}
                          >
                            Desatribuir
                          </button>
                        </>
                      ) : (
                        <span className="muted">Não atribuída</span>
                      )}
                    </td>
                    <td>{regra.antecedenciaMinimaDias} dia(s)</td>
                    <td>
                      {regra.horarios.map((h) => (
                        <div key={h.id ?? h.diaSemana}>
                          {DIAS_SEMANA[h.diaSemana]}: {h.horaAbertura.slice(0, 5)}–{h.horaFechamento.slice(0, 5)}
                        </div>
                      ))}
                    </td>
                    <td>
                      <button type="button" className="btn btn-sm btn-ghost" onClick={() => iniciarEdicao(regra)}>
                        Editar
                      </button>
                      {!regra.salaId && (
                        <button
                          type="button"
                          className="btn btn-sm btn-danger"
                          onClick={() => setExcluirId(regra.id)}
                        >
                          Excluir
                        </button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <ConfirmDialog
        open={!!excluirId}
        title="Excluir regra"
        message="Deseja excluir esta regra? Ela não pode estar atribuída a nenhuma sala."
        confirmLabel="Excluir"
        variant="danger"
        onConfirm={handleExcluir}
        onCancel={() => setExcluirId(null)}
      />
    </div>
  );
}
