import type { FormEvent } from 'react';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { salasApi } from '../../api/salas';
import { ConfirmDialog, LoadingState, PageHeader, StatusBadge } from '../../components/ui';
import { useToast } from '../../context/ToastContext';
import {
  duplicateSala,
  isFormValid,
  validateSalaForm,
  type SalaFormErrors,
  type SalaFormValues,
} from '../../utils/salas';
import type { SalaResponse, StatusSala } from '../../types';

export function SalaEditPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [sala, setSala] = useState<SalaResponse | null>(null);
  const [form, setForm] = useState<SalaFormValues>({
    nome: '',
    descricao: '',
    capacidadeMaxima: '10',
    bloco: '',
    andar: '',
    raioProximidade: '',
    statusInicial: 'ATIVA',
  });
  const [errors, setErrors] = useState<SalaFormErrors>({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [showArchive, setShowArchive] = useState(false);
  const [duplicating, setDuplicating] = useState(false);

  useEffect(() => {
    if (!id) return;
    salasApi
      .obter(id)
      .then((data) => {
        setSala(data);
        setForm({
          nome: data.nome,
          descricao: data.descricao ?? '',
          capacidadeMaxima: String(data.capacidadeMaxima),
          bloco: data.bloco ?? '',
          andar: data.andar != null ? String(data.andar) : '',
          raioProximidade:
            data.raioProximidade != null ? String(data.raioProximidade) : '',
          statusInicial: data.status,
        });
      })
      .catch(() => setSala(null))
      .finally(() => setLoading(false));
  }, [id]);

  function updateForm(patch: Partial<SalaFormValues>) {
    const next = { ...form, ...patch };
    setForm(next);
    setErrors(validateSalaForm(next));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!id) return;
    const validation = validateSalaForm(form);
    setErrors(validation);
    if (!isFormValid(validation)) return;

    setSaving(true);
    try {
      const updated = await salasApi.atualizar(id, {
        nome: form.nome.trim(),
        descricao: form.descricao.trim() || undefined,
        andar: form.andar !== '' ? Number(form.andar) : undefined,
        bloco: form.bloco.trim() || undefined,
        capacidadeMaxima: Number(form.capacidadeMaxima),
        raioProximidade:
          form.raioProximidade !== '' ? Number(form.raioProximidade) : undefined,
      });

      if (updated.status !== form.statusInicial) {
        await salasApi.atualizarStatus(id, { status: form.statusInicial });
      }

      showToast('Sala atualizada com sucesso', 'success');
      navigate(`/salas/${id}`);
    } catch (err) {
      showToast(
        err instanceof ApiException ? err.message : 'Erro ao atualizar sala',
        'error',
      );
    } finally {
      setSaving(false);
    }
  }

  async function handleStatusChange(status: StatusSala) {
    if (!id) return;
    try {
      await salasApi.atualizarStatus(id, { status });
      setForm((prev) => ({ ...prev, statusInicial: status }));
      showToast(`Status alterado para ${status}`, 'success');
    } catch (err) {
      showToast(
        err instanceof ApiException ? err.message : 'Erro ao alterar status',
        'error',
      );
    }
  }

  async function handleDuplicate() {
    if (!id) return;
    setDuplicating(true);
    try {
      const nova = await duplicateSala(id);
      showToast(`Sala duplicada: ${nova.nome}`, 'success');
      navigate(`/salas/${nova.id}`);
    } catch (err) {
      showToast(
        err instanceof ApiException ? err.message : 'Erro ao duplicar sala',
        'error',
      );
    } finally {
      setDuplicating(false);
    }
  }

  async function handleArchive() {
    if (!id) return;
    try {
      await salasApi.inativar(id);
      showToast('Sala arquivada com sucesso', 'success');
      navigate('/salas');
    } catch (err) {
      showToast(
        err instanceof ApiException ? err.message : 'Erro ao arquivar sala',
        'error',
      );
    }
  }

  if (loading) return <LoadingState message="Carregando sala..." />;
  if (!sala) {
    return (
      <div className="alert alert-error" role="alert">
        Sala não encontrada.{' '}
        <Link to="/salas">Voltar para listagem</Link>
      </div>
    );
  }

  return (
    <div>
      <PageHeader
        title={`Editar: ${sala.nome}`}
        subtitle="Atualize informações, status e ações administrativas"
        action={
          <Link to={`/salas/${id}`} className="btn btn-ghost">
            Voltar
          </Link>
        }
      />

      <div className="detail-grid">
        <div className="card form-card">
          <form onSubmit={handleSubmit} className="form">
            <div className="form-grid">
              <label>
                Nome *
                <input
                  value={form.nome}
                  onChange={(e) => updateForm({ nome: e.target.value })}
                />
                {errors.nome && <span className="field-error">{errors.nome}</span>}
              </label>
              <label>
                Capacidade *
                <input
                  type="number"
                  min={1}
                  value={form.capacidadeMaxima}
                  onChange={(e) => updateForm({ capacidadeMaxima: e.target.value })}
                />
                {errors.capacidadeMaxima && (
                  <span className="field-error">{errors.capacidadeMaxima}</span>
                )}
              </label>
              <label>
                Bloco
                <input value={form.bloco} onChange={(e) => updateForm({ bloco: e.target.value })} />
              </label>
              <label>
                Andar
                <input
                  type="number"
                  min={0}
                  value={form.andar}
                  onChange={(e) => updateForm({ andar: e.target.value })}
                />
                {errors.andar && <span className="field-error">{errors.andar}</span>}
              </label>
              <label>
                Raio de proximidade
                <input
                  type="number"
                  min={0}
                  step="0.1"
                  value={form.raioProximidade}
                  onChange={(e) => updateForm({ raioProximidade: e.target.value })}
                />
                {errors.raioProximidade && (
                  <span className="field-error">{errors.raioProximidade}</span>
                )}
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
            <div className="form-actions">
              <button type="submit" className="btn btn-primary" disabled={saving}>
                {saving ? 'Salvando...' : 'Salvar alterações'}
              </button>
            </div>
          </form>
        </div>

        <div className="card">
          <h3>Status</h3>
          <p className="muted">Status atual: <StatusBadge status={form.statusInicial} /></p>
          <div className="btn-group mt-md">
            <button
              type="button"
              className={`btn btn-sm ${form.statusInicial === 'ATIVA' ? 'btn-primary' : 'btn-ghost'}`}
              onClick={() => handleStatusChange('ATIVA')}
            >
              Ativa
            </button>
            <button
              type="button"
              className={`btn btn-sm ${form.statusInicial === 'MANUTENCAO' ? 'btn-primary' : 'btn-ghost'}`}
              onClick={() => handleStatusChange('MANUTENCAO')}
            >
              Manutenção
            </button>
            <button
              type="button"
              className={`btn btn-sm ${form.statusInicial === 'INATIVA' ? 'btn-danger' : 'btn-ghost'}`}
              onClick={() => handleStatusChange('INATIVA')}
            >
              Inativa
            </button>
          </div>

          <hr className="divider mt-lg" />

          <h3>Ações administrativas</h3>
          <div className="btn-group mt-md">
            <button
              type="button"
              className="btn btn-ghost"
              disabled={duplicating}
              onClick={handleDuplicate}
            >
              {duplicating ? 'Duplicando...' : 'Duplicar sala'}
            </button>
            <button
              type="button"
              className="btn btn-danger"
              onClick={() => setShowArchive(true)}
            >
              Arquivar sala
            </button>
          </div>
        </div>
      </div>

      <ConfirmDialog
        open={showArchive}
        title="Arquivar sala"
        message="A sala será arquivada sem exclusão física dos dados. Deseja continuar?"
        confirmLabel="Arquivar"
        variant="danger"
        onConfirm={handleArchive}
        onCancel={() => setShowArchive(false)}
      />
    </div>
  );
}
