import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { salasApi } from '../../api/salas';
import {
  buildDisponibilidadePayload,
  DEFAULT_DISPONIBILIDADE,
  DisponibilidadeStepForm,
  validateDisponibilidade,
  type DisponibilidadeFormData,
} from '../../components/salas/DisponibilidadeStepForm';
import { WizardSteps } from '../../components/salas/WizardSteps';
import { Alert, PageHeader } from '../../components/ui';
import { useToast } from '../../context/ToastContext';
import {
  isFormValid,
  validateSalaForm,
  type SalaFormErrors,
  type SalaFormValues,
} from '../../utils/salas';
import type { StatusSala } from '../../types';

const WIZARD_STEPS = ['Informações', 'Disponibilidade', 'Layout'];

const INITIAL_FORM: SalaFormValues = {
  nome: '',
  descricao: '',
  capacidadeMaxima: '10',
  bloco: '',
  andar: '',
  raioProximidade: '',
  statusInicial: 'ATIVA',
};

export function SalaWizardPage() {
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [step, setStep] = useState(1);
  const [form, setForm] = useState<SalaFormValues>(INITIAL_FORM);
  const [formErrors, setFormErrors] = useState<SalaFormErrors>({});
  const [touched, setTouched] = useState<Partial<Record<keyof SalaFormValues, boolean>>>({});
  const [disponibilidade, setDisponibilidade] =
    useState<DisponibilidadeFormData>(DEFAULT_DISPONIBILIDADE);
  const [dispErrors, setDispErrors] = useState<Record<string, string>>({});
  const [salaId, setSalaId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  function updateForm(patch: Partial<SalaFormValues>) {
    const next = { ...form, ...patch };
    setForm(next);
    if (Object.keys(touched).length > 0 || patch.nome !== undefined) {
      setFormErrors(validateSalaForm(next));
    }
  }

  function handleBlur(field: keyof SalaFormValues) {
    setTouched((prev) => ({ ...prev, [field]: true }));
    setFormErrors(validateSalaForm(form));
  }

  function validateStep1(): boolean {
    const errors = validateSalaForm(form);
    setFormErrors(errors);
    setTouched({
      nome: true,
      capacidadeMaxima: true,
      andar: true,
      raioProximidade: true,
    });
    return isFormValid(errors);
  }

  function validateStep2(): boolean {
    const errors = validateDisponibilidade(disponibilidade);
    setDispErrors(errors);
    return Object.keys(errors).length === 0;
  }

  async function criarSalaComDisponibilidade() {
    setLoading(true);
    setError('');
    try {
      const sala = await salasApi.criar({
        nome: form.nome.trim(),
        descricao: form.descricao.trim() || undefined,
        andar: form.andar !== '' ? Number(form.andar) : undefined,
        bloco: form.bloco.trim() || undefined,
        capacidadeMaxima: Number(form.capacidadeMaxima),
        raioProximidade:
          form.raioProximidade !== '' ? Number(form.raioProximidade) : undefined,
      });

      if (form.statusInicial !== 'ATIVA') {
        await salasApi.atualizarStatus(sala.id, { status: form.statusInicial });
      }

      const payload = buildDisponibilidadePayload(disponibilidade);
      if (payload.tipo === 'existente') {
        await salasApi.atribuirRegra(sala.id, { regraId: payload.regraId });
      } else {
        await salasApi.criarRegraDisponibilidade(sala.id, {
          nome: payload.nome,
          antecedenciaMinimaDias: payload.antecedenciaMinimaDias,
          horarios: payload.horarios,
        });
      }

      setSalaId(sala.id);
      setStep(3);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao criar sala');
    } finally {
      setLoading(false);
    }
  }

  async function handleNext() {
    if (step === 1) {
      if (validateStep1()) setStep(2);
      return;
    }
    if (step === 2) {
      if (validateStep2()) await criarSalaComDisponibilidade();
    }
  }

  function handleFinish() {
    if (!salaId) return;
    showToast(
      'Sala criada com sucesso. Agora configure o layout e a disponibilidade.',
      'success',
    );
    navigate(`/salas/${salaId}`);
  }

  return (
    <div>
      <PageHeader
        title="Nova Sala"
        subtitle="Cadastre uma sala seguindo as etapas de configuração"
        action={
          <Link to="/salas" className="btn btn-ghost">
            Cancelar
          </Link>
        }
      />

      <WizardSteps steps={WIZARD_STEPS} current={step} />

      <div className="card form-card wizard-card">
        <Alert message={error} />

        {step === 1 && (
          <div className="wizard-step-content">
            <div className="form-grid">
              <label>
                Nome da Sala *
                <input
                  value={form.nome}
                  onChange={(e) => updateForm({ nome: e.target.value })}
                  onBlur={() => handleBlur('nome')}
                  aria-invalid={!!formErrors.nome}
                />
                {touched.nome && formErrors.nome && (
                  <span className="field-error">{formErrors.nome}</span>
                )}
              </label>
              <label>
                Capacidade Máxima *
                <input
                  type="number"
                  min={1}
                  value={form.capacidadeMaxima}
                  onChange={(e) => updateForm({ capacidadeMaxima: e.target.value })}
                  onBlur={() => handleBlur('capacidadeMaxima')}
                />
                {touched.capacidadeMaxima && formErrors.capacidadeMaxima && (
                  <span className="field-error">{formErrors.capacidadeMaxima}</span>
                )}
              </label>
              <label>
                Bloco
                <input
                  value={form.bloco}
                  onChange={(e) => updateForm({ bloco: e.target.value })}
                />
              </label>
              <label>
                Andar
                <input
                  type="number"
                  min={0}
                  value={form.andar}
                  onChange={(e) => updateForm({ andar: e.target.value })}
                  onBlur={() => handleBlur('andar')}
                />
                {touched.andar && formErrors.andar && (
                  <span className="field-error">{formErrors.andar}</span>
                )}
              </label>
              <label>
                Raio de Proximidade
                <input
                  type="number"
                  min={0}
                  step="0.1"
                  value={form.raioProximidade}
                  onChange={(e) => updateForm({ raioProximidade: e.target.value })}
                  onBlur={() => handleBlur('raioProximidade')}
                />
                {touched.raioProximidade && formErrors.raioProximidade && (
                  <span className="field-error">{formErrors.raioProximidade}</span>
                )}
              </label>
              <label>
                Status Inicial
                <select
                  value={form.statusInicial}
                  onChange={(e) =>
                    updateForm({ statusInicial: e.target.value as StatusSala })
                  }
                >
                  <option value="ATIVA">Ativa</option>
                  <option value="INATIVA">Inativa</option>
                  <option value="MANUTENCAO">Manutenção</option>
                </select>
              </label>
              <label className="full-width">
                Descrição
                <textarea
                  value={form.descricao}
                  onChange={(e) => updateForm({ descricao: e.target.value })}
                  rows={3}
                />
              </label>
            </div>
          </div>
        )}

        {step === 2 && (
          <DisponibilidadeStepForm
            data={disponibilidade}
            onChange={setDisponibilidade}
            errors={dispErrors}
          />
        )}

        {step === 3 && salaId && (
          <div className="wizard-step-content">
            <div className="alert alert-info">
              Esta sala ainda não possui layout configurado.
            </div>
            <p className="muted">
              Configure o layout e as posições para deixar a sala pronta para reservas.
            </p>
            <div className="layout-actions-grid">
              <Link to={`/layouts?salaId=${salaId}`} className="dashboard-card">
                <h3>Criar Layout</h3>
                <p>Defina a versão do layout da sala</p>
              </Link>
              <Link to={`/posicoes?salaId=${salaId}`} className="dashboard-card">
                <h3>Adicionar Posições</h3>
                <p>Cadastre assentos e estações de trabalho</p>
              </Link>
              <Link to={`/layouts?salaId=${salaId}`} className="dashboard-card">
                <h3>Importar Layout</h3>
                <p>Utilize um layout existente como base</p>
              </Link>
              <Link to={`/equipamentos?salaId=${salaId}`} className="dashboard-card">
                <h3>Configurar Equipamentos</h3>
                <p>Vincule equipamentos às posições</p>
              </Link>
              <Link to={`/layouts?salaId=${salaId}`} className="dashboard-card">
                <h3>Aprovar Layout</h3>
                <p>Ative o layout para permitir reservas</p>
              </Link>
            </div>
          </div>
        )}

        <div className="form-actions wizard-actions">
          {step > 1 && step < 3 && (
            <button
              type="button"
              className="btn btn-ghost"
              disabled={loading}
              onClick={() => setStep(step - 1)}
            >
              Voltar
            </button>
          )}
          {step < 3 ? (
            <button
              type="button"
              className="btn btn-primary"
              disabled={loading}
              onClick={handleNext}
            >
              {loading ? 'Salvando...' : step === 2 ? 'Criar sala e continuar' : 'Próximo'}
            </button>
          ) : (
            <button type="button" className="btn btn-primary" onClick={handleFinish}>
              Ir para detalhes da sala
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
