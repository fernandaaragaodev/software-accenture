import type { FormEvent } from 'react';
import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { salasApi } from '../../api/salas';
import { useToast } from '../../components/Toast';
import { Alert, FieldError, PageHeader } from '../../components/ui';
import { validateInfoForm, type SalaInfoForm } from './salasUtils';
import { useSalaEnriched } from './useSalaEnrichment';

export function SalaEditPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const toast = useToast();
  const { data: sala, loading: loadingSala, error: loadError } = useSalaEnriched(id);
  const [form, setForm] = useState<SalaInfoForm | null>(null);
  const [touched, setTouched] = useState<Partial<Record<keyof SalaInfoForm, boolean>>>({});
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (!sala) return;
    setForm({
      nome: sala.nome,
      descricao: sala.descricao ?? '',
      capacidadeMaxima: String(sala.capacidadeMaxima),
      bloco: sala.bloco ?? '',
      andar: sala.andar !== undefined ? String(sala.andar) : '',
      raioProximidade: sala.raioProximidade !== undefined ? String(sala.raioProximidade) : '',
      statusInicial: sala.status,
    });
  }, [sala]);

  const errors = useMemo(() => (form ? validateInfoForm(form) : {}), [form]);
  const isValid = form ? Object.keys(errors).length === 0 : false;

  function updateForm<K extends keyof SalaInfoForm>(key: K, value: SalaInfoForm[K]) {
    setForm((prev) => (prev ? { ...prev, [key]: value } : prev));
    setTouched((prev) => ({ ...prev, [key]: true }));
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    if (!id || !form) return;

    setTouched({
      nome: true,
      capacidadeMaxima: true,
      andar: true,
      raioProximidade: true,
    });

    if (!isValid) return;

    setError('');
    setSaving(true);
    try {
      const updated = await salasApi.atualizar(id, {
        nome: form.nome.trim(),
        descricao: form.descricao.trim() || undefined,
        andar: form.andar !== '' ? Number(form.andar) : undefined,
        bloco: form.bloco.trim() || undefined,
        capacidadeMaxima: Number(form.capacidadeMaxima),
        raioProximidade: form.raioProximidade !== '' ? Number(form.raioProximidade) : undefined,
      });

      if (updated.status !== form.statusInicial) {
        await salasApi.atualizarStatus(id, { status: form.statusInicial });
      }

      toast.success('Sala atualizada com sucesso.');
      navigate(`/salas/${id}`);
    } catch (err) {
      setError(err instanceof ApiException ? err.message : 'Erro ao atualizar sala');
    } finally {
      setSaving(false);
    }
  }

  if (loadingSala || !form) {
    return (
      <div className="page-center">
        <div className="spinner" />
        <p className="muted">Carregando sala...</p>
      </div>
    );
  }

  if (loadError) {
    return <Alert message={loadError} />;
  }

  return (
    <div>
      <PageHeader
        title="Editar Sala"
        subtitle={form.nome}
        action={
          <Link to={`/salas/${id}`} className="btn btn-ghost">
            Cancelar
          </Link>
        }
      />

      <div className="card form-card">
        <form onSubmit={handleSubmit} className="form">
          <Alert message={error} />
          <div className="form-grid">
            <label>
              Nome *
              <input value={form.nome} onChange={(e) => updateForm('nome', e.target.value)} />
              <FieldError message={touched.nome ? errors.nome : undefined} />
            </label>
            <label>
              Capacidade máxima *
              <input
                type="number"
                min={1}
                value={form.capacidadeMaxima}
                onChange={(e) => updateForm('capacidadeMaxima', e.target.value)}
              />
              <FieldError message={touched.capacidadeMaxima ? errors.capacidadeMaxima : undefined} />
            </label>
            <label>
              Bloco
              <input value={form.bloco} onChange={(e) => updateForm('bloco', e.target.value)} />
            </label>
            <label>
              Andar
              <input
                type="number"
                min={0}
                value={form.andar}
                onChange={(e) => updateForm('andar', e.target.value)}
              />
              <FieldError message={touched.andar ? errors.andar : undefined} />
            </label>
            <label>
              Raio de proximidade
              <input
                type="number"
                step="0.1"
                min={0}
                value={form.raioProximidade}
                onChange={(e) => updateForm('raioProximidade', e.target.value)}
              />
              <FieldError message={touched.raioProximidade ? errors.raioProximidade : undefined} />
            </label>
            <label>
              Status
              <select
                value={form.statusInicial}
                onChange={(e) => updateForm('statusInicial', e.target.value as SalaInfoForm['statusInicial'])}
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
                onChange={(e) => updateForm('descricao', e.target.value)}
                rows={3}
              />
            </label>
          </div>
          <div className="form-actions">
            <Link to={`/salas/${id}`} className="btn btn-ghost">
              Voltar
            </Link>
            <button type="submit" className="btn btn-primary" disabled={saving || !isValid}>
              {saving ? 'Salvando...' : 'Salvar alterações'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
