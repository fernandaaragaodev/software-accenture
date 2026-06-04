import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { z } from 'zod';
import { salasApi } from '../api/salas.api';
import { Button } from '../components/ui/Button';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { Input } from '../components/ui/Input';
import { Loading } from '../components/ui/Loading';
import { getApiErrorMessage } from '../utils/apiError';

const salaSchema = z.object({
  nome: z.string().min(2, 'Nome obrigatório.'),
  descricao: z.string().optional(),
  andar: z.coerce.number().int().optional(),
  bloco: z.string().optional(),
  capacidadeMaxima: z.coerce.number().int().min(1, 'Capacidade mínima: 1.'),
  raioProximidade: z.coerce.number().min(0, 'Raio inválido.'),
  imagemPath: z.string().optional(),
});

type SalaForm = z.infer<typeof salaSchema>;

export function SalaFormPage() {
  const { id } = useParams<{ id: string }>();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const [loading, setLoading] = useState(isEdit);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isSubmitting },
  } = useForm<SalaForm>({
    resolver: zodResolver(salaSchema),
  });

  useEffect(() => {
    if (!id) return;
    salasApi
      .buscar(id)
      .then(({ data }) => {
        reset({
          nome: data.nome,
          descricao: data.descricao ?? '',
          andar: data.andar ?? undefined,
          bloco: data.bloco ?? '',
          capacidadeMaxima: data.capacidadeMaxima ?? 1,
          raioProximidade: Number(data.raioProximidade ?? 0),
          imagemPath: data.imagemPath ?? '',
        });
      })
      .catch((err) => setError(getApiErrorMessage(err)))
      .finally(() => setLoading(false));
  }, [id, reset]);

  const onSubmit = async (data: SalaForm) => {
    setError(null);
    setSuccess(null);
    const payload = {
      nome: data.nome,
      descricao: data.descricao,
      andar: data.andar,
      bloco: data.bloco,
      capacidadeMaxima: data.capacidadeMaxima,
      raioProximidade: data.raioProximidade,
      imagemPath: data.imagemPath,
    };
    try {
      if (isEdit && id) {
        await salasApi.atualizar(id, payload);
        setSuccess('Sala atualizada com sucesso.');
      } else {
        const { data: created } = await salasApi.criar(payload);
        setSuccess('Sala cadastrada com sucesso.');
        navigate(`/salas/${created.id}`);
      }
    } catch (err) {
      setError(getApiErrorMessage(err));
    }
  };

  if (loading) return <Loading />;

  return (
    <div className="mx-auto max-w-2xl space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-900">
          {isEdit ? 'Editar sala' : 'Nova sala'}
        </h1>
        <Link to="/salas" className="text-sm text-primary-600 hover:underline">
          Voltar
        </Link>
      </div>

      {error && <ErrorMessage message={error} />}
      {success && (
        <p className="rounded-lg bg-emerald-50 px-4 py-3 text-sm text-emerald-800">{success}</p>
      )}

      <form
        onSubmit={handleSubmit(onSubmit)}
        className="space-y-4 rounded-xl border border-border bg-white p-6 shadow-sm"
      >
        <Input label="Nome" error={errors.nome?.message} {...register('nome')} />
        <Input label="Descrição" error={errors.descricao?.message} {...register('descricao')} />
        <div className="grid gap-4 sm:grid-cols-2">
          <Input label="Andar" type="number" error={errors.andar?.message} {...register('andar')} />
          <Input label="Bloco" error={errors.bloco?.message} {...register('bloco')} />
        </div>
        <div className="grid gap-4 sm:grid-cols-2">
          <Input
            label="Capacidade máxima"
            type="number"
            error={errors.capacidadeMaxima?.message}
            {...register('capacidadeMaxima')}
          />
          <Input
            label="Raio de proximidade"
            type="number"
            step="0.1"
            error={errors.raioProximidade?.message}
            {...register('raioProximidade')}
          />
        </div>
        <Input label="Caminho da imagem" error={errors.imagemPath?.message} {...register('imagemPath')} />
        <Button type="submit" loading={isSubmitting}>
          {isEdit ? 'Salvar alterações' : 'Cadastrar sala'}
        </Button>
      </form>
    </div>
  );
}
