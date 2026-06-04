import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { equipamentosApi } from '../api/equipamentos.api';
import { Badge } from '../components/ui/Badge';
import { Button } from '../components/ui/Button';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { Input } from '../components/ui/Input';
import { Loading } from '../components/ui/Loading';
import { Modal } from '../components/ui/Modal';
import { Table } from '../components/ui/Table';
import { Textarea } from '../components/ui/Textarea';
import type { TipoEquipamento } from '../types/equipamento.types';
import { getApiErrorMessage } from '../utils/apiError';

const schema = z.object({
  nome: z.string().min(2, 'Nome obrigatório.'),
  descricao: z.string().optional(),
  ativo: z.boolean(),
});

type FormData = z.infer<typeof schema>;

export function EquipamentosPage() {
  const [itens, setItens] = useState<TipoEquipamento[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState<TipoEquipamento | null>(null);

  const form = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { nome: '', descricao: '', ativo: true },
  });

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const { data } = await equipamentosApi.listar();
      setItens(data);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void load();
  }, []);

  const openCreate = () => {
    setEditing(null);
    form.reset({ nome: '', descricao: '', ativo: true });
    setModalOpen(true);
  };

  const openEdit = (item: TipoEquipamento) => {
    setEditing(item);
    form.reset({
      nome: item.nome,
      descricao: item.descricao ?? '',
      ativo: item.ativo,
    });
    setModalOpen(true);
  };

  const onSubmit = async (data: FormData) => {
    try {
      if (editing) {
        await equipamentosApi.atualizar(editing.id, data);
      } else {
        await equipamentosApi.criar(data);
      }
      setModalOpen(false);
      await load();
    } catch (err) {
      alert(getApiErrorMessage(err));
    }
  };

  const inativar = async (id: string) => {
    if (!confirm('Inativar este tipo de equipamento?')) return;
    try {
      await equipamentosApi.inativar(id);
      await load();
    } catch (err) {
      alert(getApiErrorMessage(err));
    }
  };

  if (loading) return <Loading />;
  if (error) return <ErrorMessage message={error} onRetry={() => void load()} />;

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-slate-900">Tipos de equipamento</h1>
        <Button onClick={openCreate}>Novo tipo</Button>
      </div>

      <Table<TipoEquipamento>
        data={itens}
        keyExtractor={(t) => t.id}
        columns={[
          { key: 'nome', header: 'Nome', render: (t) => t.nome },
          { key: 'desc', header: 'Descrição', render: (t) => t.descricao ?? '—' },
          {
            key: 'ativo',
            header: 'Status',
            render: (t) => (
              <Badge variant={t.ativo ? 'success' : 'default'}>
                {t.ativo ? 'Ativo' : 'Inativo'}
              </Badge>
            ),
          },
          {
            key: 'acoes',
            header: 'Ações',
            render: (t) => (
              <div className="flex gap-1">
                <Button size="sm" variant="secondary" onClick={() => openEdit(t)}>
                  Editar
                </Button>
                {t.ativo && (
                  <Button size="sm" variant="danger" onClick={() => void inativar(t.id)}>
                    Inativar
                  </Button>
                )}
              </div>
            ),
          },
        ]}
      />

      <Modal
        open={modalOpen}
        title={editing ? 'Editar equipamento' : 'Novo equipamento'}
        onClose={() => setModalOpen(false)}
      >
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-3">
          <Input label="Nome" {...form.register('nome')} />
          <Textarea label="Descrição" {...form.register('descricao')} />
          <label className="flex items-center gap-2 text-sm">
            <input type="checkbox" {...form.register('ativo')} />
            Ativo
          </label>
          <Button type="submit">Salvar</Button>
        </form>
      </Modal>
    </div>
  );
}
