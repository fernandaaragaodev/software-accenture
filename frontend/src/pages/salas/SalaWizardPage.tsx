import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { regrasDisponibilidadeApi } from '../../api/regras-disponibilidade';
import { salasApi } from '../../api/salas';
import { Wizard } from '../../components/Wizard';
import { useToast } from '../../components/Toast';
import { Alert, FieldError, PageHeader } from '../../components/ui';
import {
  horariosPadrao,
  toApiTime,
  validateInfoForm,
  WIZARD_STEPS,
  type DisponibilidadeForm,
  type SalaInfoForm,
} from './salasUtils';

const DIAS_SEMANA = ['Segunda', 'Terça', 'Quarta', 'Quinta', 'Sexta', 'Sábado', 'Domingo'];

const INITIAL_INFO: SalaInfoForm = {
  nome: '',
  descricao: '',
  capacidadeMaxima: '10',
  bloco: '',
  andar: '',
  raioProximidade: '',
  statusInicial: 'ATIVA',
};

const INITIAL_DISPONIBILIDADE: DisponibilidadeForm = {
  modo: 'nova',
  regraExistenteId: '',
  nomeRegra: '',
  antecedenciaMinimaDias: '1',
  limiteReservasFuturas: '30',
  horarios: horariosPadrao(),
};

export function SalaWizardPage() {
  const navigate = useNavigate();
  const toast = useToast();
  const [step, setStep] = useState(0);
  const [info, setInfo] = useState<SalaInfoForm>(INITIAL_INFO);
  const [disponibilidade, setDisponibilidade] = useState<DisponibilidadeForm>(INITIAL_DISPONIBILIDADE);
  const [regrasExistentes, setRegrasExistentes] = useState<{ id: string; nome: string }[]>([]);
  const [touched, setTouched] = useState<Partial<Record<keyof SalaInfoForm, boolean>>>({});
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const infoErrors = useMemo(() => validateInfoForm(info), [info]);
  const infoValid = Object.keys(infoErrors).length === 0;

  useEffect(() => {
    regrasDisponibilidadeApi
      .listar()
      .then((regras) => setRegrasExistentes(regras.filter((r) => !r.salaId)))
      .catch(() => setRegrasExistentes([]));
  }, []);

  function updateInfo<K extends keyof SalaInfoForm>(key: K, value: SalaInfoForm[K]) {
    setInfo((prev) => ({ ...prev, [key]: value }));
    setTouched((prev) => ({ ...prev, [key]: true }));
  }

  function updateHorario(index: number, patch: Partial<(typeof disponibilidade.horarios)[0]>) {
    setDisponibilidade((prev) => ({
      ...prev,
      horarios: prev.horarios.map((h, i) => (i === index ? { ...h, ...patch } : h)),
    }));
  }

  function handleNextStep() {
    setTouched({
      nome: true,
      capacidadeMaxima: true,
      andar: true,
      raioProximidade: true,
    });
    if (!infoValid) return;
    setDisponibilidade((prev) => ({
      ...prev,
      nomeRegra: prev.nomeRegra || `Disponibilidade - ${info.nome.trim()}`,
    }));
    setStep(1);
  }

  async function handleCreate() {
    setError('');

    if (disponibilidade.modo === 'existente' && !disponibilidade.regraExistenteId) {
      setError('Selecione uma regra de disponibilidade existente.');
      return;
    }

    if (disponibilidade.modo === 'nova') {
      const horariosAtivos = disponibilidade.horarios.filter((h) => h.ativo);
      if (horariosAtivos.length === 0) {
        setError('Selecione ao menos um dia da semana.');
        return;
      }
      if (!disponibilidade.nomeRegra.trim()) {
        setError('Informe um nome para a regra de disponibilidade.');
        return;
      }
    }

    setLoading(true);
    try {
      const sala = await salasApi.criar({
        nome: info.nome.trim(),
        descricao: info.descricao.trim() || undefined,
        andar: info.andar !== '' ? Number(info.andar) : undefined,
        bloco: info.bloco.trim() || undefined,
        capacidadeMaxima: Number(info.capacidadeMaxima),
        raioProximidade: info.raioProximidade !== '' ? Number(info.raioProximidade) : undefined,
      });

      if (info.statusInicial !== 'ATIVA') {
        await salasApi.atualizarStatus(sala.id, { status: info.statusInicial });
      }

      if (disponibilidade.modo === 'existente') {
        await salasApi.atribuirRegra(sala.id, { regraId: disponibilidade.regraExistenteId });
      } else {
        await salasApi.criarRegraDisponibilidade(sala.id, {
          nome: disponibilidade.nomeRegra.trim(),
          antecedenciaMinimaDias: Number(disponibilidade.antecedenciaMinimaDias),
          horarios: disponibilidade.horarios
            .filter((h) => h.ativo)
            .map((h) => ({
              diaSemana: h.diaSemana,
              horaAbertura: toApiTime(h.horaAbertura),
              horaFechamento: toApiTime(h.horaFechamento),
            })),
        });
      }

      toast.success('Sala criada com sucesso. Agora configure o layout e a disponibilidade.');
      navigate(`/salas/${sala.id}`, { state: { fromCreate: true } });
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao criar sala');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div>
      <PageHeader
        title="Nova Sala"
        subtitle="Cadastre uma sala seguindo o fluxo guiado"
        action={
          <Link to="/salas" className="btn btn-ghost">
            Voltar
          </Link>
        }
      />

      <Wizard steps={WIZARD_STEPS} currentStep={step}>
        <Alert message={error} />

        {step === 0 && (
          <div className="card form-card">
            <h3>Informações da Sala</h3>
            <div className="form-grid mt-md">
              <label>
                Nome da Sala *
                <input
                  value={info.nome}
                  onChange={(e) => updateInfo('nome', e.target.value)}
                  onBlur={() => setTouched((prev) => ({ ...prev, nome: true }))}
                />
                <FieldError message={touched.nome ? infoErrors.nome : undefined} />
              </label>
              <label>
                Capacidade Máxima *
                <input
                  type="number"
                  min={1}
                  value={info.capacidadeMaxima}
                  onChange={(e) => updateInfo('capacidadeMaxima', e.target.value)}
                  onBlur={() => setTouched((prev) => ({ ...prev, capacidadeMaxima: true }))}
                />
                <FieldError message={touched.capacidadeMaxima ? infoErrors.capacidadeMaxima : undefined} />
              </label>
              <label>
                Bloco
                <input value={info.bloco} onChange={(e) => updateInfo('bloco', e.target.value)} />
              </label>
              <label>
                Andar
                <input
                  type="number"
                  min={0}
                  value={info.andar}
                  onChange={(e) => updateInfo('andar', e.target.value)}
                  onBlur={() => setTouched((prev) => ({ ...prev, andar: true }))}
                />
                <FieldError message={touched.andar ? infoErrors.andar : undefined} />
              </label>
              <label>
                Raio de Proximidade
                <input
                  type="number"
                  step="0.1"
                  min={0}
                  value={info.raioProximidade}
                  onChange={(e) => updateInfo('raioProximidade', e.target.value)}
                  onBlur={() => setTouched((prev) => ({ ...prev, raioProximidade: true }))}
                />
                <FieldError message={touched.raioProximidade ? infoErrors.raioProximidade : undefined} />
              </label>
              <label>
                Status Inicial
                <select
                  value={info.statusInicial}
                  onChange={(e) => updateInfo('statusInicial', e.target.value as SalaInfoForm['statusInicial'])}
                >
                  <option value="ATIVA">Ativa</option>
                  <option value="INATIVA">Inativa</option>
                  <option value="MANUTENCAO">Manutenção</option>
                </select>
              </label>
              <label className="full-width">
                Descrição
                <textarea
                  value={info.descricao}
                  onChange={(e) => updateInfo('descricao', e.target.value)}
                  rows={3}
                />
              </label>
            </div>
            <div className="form-actions">
              <button type="button" className="btn btn-primary" onClick={handleNextStep} disabled={!infoValid}>
                Próximo: Disponibilidade
              </button>
            </div>
          </div>
        )}

        {step === 1 && (
          <div className="card form-card">
            <h3>Disponibilidade</h3>
            <p className="muted">Configure os horários ou selecione uma regra existente.</p>

            <div className="form mt-md">
              <label>
                Modo de configuração
                <select
                  value={disponibilidade.modo}
                  onChange={(e) =>
                    setDisponibilidade((prev) => ({
                      ...prev,
                      modo: e.target.value as DisponibilidadeForm['modo'],
                    }))
                  }
                >
                  <option value="nova">Criar nova regra</option>
                  <option value="existente">Usar regra existente</option>
                </select>
              </label>

              {disponibilidade.modo === 'existente' ? (
                <label>
                  Regra existente
                  <select
                    value={disponibilidade.regraExistenteId}
                    onChange={(e) =>
                      setDisponibilidade((prev) => ({ ...prev, regraExistenteId: e.target.value }))
                    }
                  >
                    <option value="">Selecione...</option>
                    {regrasExistentes.map((regra) => (
                      <option key={regra.id} value={regra.id}>
                        {regra.nome}
                      </option>
                    ))}
                  </select>
                </label>
              ) : (
                <>
                  <div className="form-grid">
                    <label>
                      Nome da regra
                      <input
                        value={disponibilidade.nomeRegra}
                        onChange={(e) =>
                          setDisponibilidade((prev) => ({ ...prev, nomeRegra: e.target.value }))
                        }
                        placeholder={`Disponibilidade - ${info.nome || 'Nova sala'}`}
                      />
                    </label>
                    <label>
                      Antecedência mínima (dias)
                      <input
                        type="number"
                        min={0}
                        value={disponibilidade.antecedenciaMinimaDias}
                        onChange={(e) =>
                          setDisponibilidade((prev) => ({
                            ...prev,
                            antecedenciaMinimaDias: e.target.value,
                          }))
                        }
                      />
                    </label>
                    <label>
                      Limite de reservas futuras (dias)
                      <input
                        type="number"
                        min={1}
                        value={disponibilidade.limiteReservasFuturas}
                        onChange={(e) =>
                          setDisponibilidade((prev) => ({
                            ...prev,
                            limiteReservasFuturas: e.target.value,
                          }))
                        }
                      />
                    </label>
                  </div>

                  <fieldset className="checkbox-group">
                    <legend>Dias da semana e horários</legend>
                    {disponibilidade.horarios.map((horario, index) => (
                      <div key={horario.diaSemana} className="horario-row">
                        <label className="checkbox-label">
                          <input
                            type="checkbox"
                            checked={horario.ativo}
                            onChange={(e) => updateHorario(index, { ativo: e.target.checked })}
                          />
                          {DIAS_SEMANA[horario.diaSemana]}
                        </label>
                        <input
                          type="time"
                          value={horario.horaAbertura}
                          disabled={!horario.ativo}
                          onChange={(e) => updateHorario(index, { horaAbertura: e.target.value })}
                        />
                        <input
                          type="time"
                          value={horario.horaFechamento}
                          disabled={!horario.ativo}
                          onChange={(e) => updateHorario(index, { horaFechamento: e.target.value })}
                        />
                      </div>
                    ))}
                  </fieldset>
                </>
              )}
            </div>

            <div className="form-actions">
              <button type="button" className="btn btn-ghost" onClick={() => setStep(0)}>
                Voltar
              </button>
              <button type="button" className="btn btn-primary" onClick={handleCreate} disabled={loading}>
                {loading ? 'Criando sala...' : 'Criar sala e continuar'}
              </button>
            </div>
          </div>
        )}
      </Wizard>
    </div>
  );
}
