import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { equipamentosApi } from '../api/equipamentos.api';
import { posicoesApi } from '../api/posicoes.api';
import { salasApi } from '../api/salas.api';
import { Button } from '../components/ui/Button';
import { ErrorMessage } from '../components/ui/ErrorMessage';
import { Input } from '../components/ui/Input';
import { Loading } from '../components/ui/Loading';
import { Modal } from '../components/ui/Modal';
import { Select } from '../components/ui/Select';
import { Table } from '../components/ui/Table';
import type { TipoEquipamento } from '../types/equipamento.types';
import type { Posicao } from '../types/posicao.types';
import type { Sala } from '../types/sala.types';
import { getApiErrorMessage } from '../utils/apiError';

const posicaoSchema = z.object({
  identificador: z.string().min(1, 'Identificador obrigatório.'),
  tipo: z.string().min(1, 'Tipo obrigatório.'),
  coordX: z.coerce.number(),
  coordY: z.coerce.number(),
  tipoCadeira: z.string().optional(),
  tipoMesa: z.string().optional(),
});

type PosicaoForm = z.infer<typeof posicaoSchema>;

export function PosicoesPage() {
  const [salas, setSalas] = useState<Sala[]>([]);
  const [salaId, setSalaId] = useState('');
  const [posicoes, setPosicoes] = useState<Posicao[]>([]);
  const [tipos, setTipos] = useState<TipoEquipamento[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [modalOpen, setModalOpen] = useState(false);
  const [equipModal, setEquipModal] = useState<string | null>(null);
  const [tipoEquipId, setTipoEquipId] = useState('');
  const [quantidade, setQuantidade] = useState('1');

  const form = useForm<PosicaoForm>({ resolver: zodResolver(posicaoSchema) });

  const loadSalas = async () => {
    const { data } = await salasApi.listar();
    setSalas(data);
    if (!salaId && data[0]) setSalaId(data[0].id);
  };

  const loadPosicoes = async (id: string) => {
    setLoading(true);
    try {
      const { data } = await posicoesApi.listarPorSala(id);
      setPosicoes(data);
    } catch (err) {
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void (async () => {
      try {
        await loadSalas();
        const { data: tiposData } = await equipamentosApi.listar();
        setTipos(tiposData.filter((t) => t.ativo));
      } catch (err) {
        setError(getApiErrorMessage(err));
      }
    })();
  }, []);

  useEffect(() => {
    if (salaId) void loadPosicoes(salaId);
  }, [salaId]);

  const onCreate = async (data: PosicaoForm) => {
    if (!salaId) return;
    try {
      await posicoesApi.criar({ salaId, ...data });
      setModalOpen(false);
      form.reset();
      await loadPosicoes(salaId);
    } catch (err) {
      alert(getApiErrorMessage(err));
    }
  };

  const handleInativar = async (id: string) => {
    if (!confirm('Inativar esta posição?')) return;
    try {
      await posicoesApi.inativar(id);
      await loadPosicoes(salaId);
    } catch (err) {
      alert(getApiErrorMessage(err));
    }
  };

  const vincularEquipamento = async () => {
    if (!equipModal || !tipoEquipId) return;
    try {
      await posicoesApi.vincularEquipamento(equipModal, {
        tipoEquipamentoId: tipoEquipId,
        quantidade: Number(quantidade),
      });
      setEquipModal(null);
      alert('Equipamento vinculado.');
    } catch (err) {
      alert(getApiErrorMessage(err));
    }
  };

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h1 className="text-2xl font-bold text-slate-900">Posições</h1>
        <Button onClick={() => setModalOpen(true)} disabled={!salaId}>
          Nova posição
        </Button>
      </div>

      <Select
        label="Sala"
        value={salaId}
        onChange={(e) => setSalaId(e.target.value)}
        options={salas.map((s) => ({ value: s.id, label: s.nome }))}
        className="max-w-md"
      />

      {error && <ErrorMessage message={error} />}
      {loading ? (
        <Loading />
      ) : (
        <Table<Posicao>
          data={posicoes}
          keyExtractor={(p) => p.id}
          columns={[
            { key: 'id', header: 'ID', render: (p) => p.identificador },
            { key: 'tipo', header: 'Tipo', render: (p) => p.tipo },
            {
              key: 'coords',
              header: 'Coordenadas',
              render: (p) => `${p.coordX}, ${p.coordY}`,
            },
            { key: 'status', header: 'Status', render: (p) => p.status },
            {
              key: 'acoes',
              header: 'Ações',
              render: (p) => (
                <div className="flex gap-1">
                  <Button size="sm" variant="secondary" onClick={() => setEquipModal(p.id)}>
                    Equipamentos
                  </Button>
                  <Button size="sm" variant="danger" onClick={() => void handleInativar(p.id)}>
                    Inativar
                  </Button>
                </div>
              ),
            },
          ]}
        />
      )}

      <Modal open={modalOpen} title="Cadastrar posição" onClose={() => setModalOpen(false)}>
        <form onSubmit={form.handleSubmit(onCreate)} className="space-y-3">
          <Input label="Identificador" {...form.register('identificador')} />
          <Input label="Tipo" {...form.register('tipo')} />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Coord X" type="number" step="0.1" {...form.register('coordX')} />
            <Input label="Coord Y" type="number" step="0.1" {...form.register('coordY')} />
          </div>
          <Input label="Tipo de cadeira" {...form.register('tipoCadeira')} />
          <Input label="Tipo de mesa" {...form.register('tipoMesa')} />
          <Button type="submit">Salvar</Button>
        </form>
      </Modal>

      <Modal
        open={Boolean(equipModal)}
        title="Vincular equipamento"
        onClose={() => setEquipModal(null)}
      >
        <div className="space-y-3">
          <Select
            label="Tipo de equipamento"
            value={tipoEquipId}
            onChange={(e) => setTipoEquipId(e.target.value)}
            options={tipos.map((t) => ({ value: t.id, label: t.nome }))}
            placeholder="Selecione"
          />
          <Input
            label="Quantidade"
            type="number"
            min={1}
            value={quantidade}
            onChange={(e) => setQuantidade(e.target.value)}
          />
          <Button onClick={() => void vincularEquipamento()}>Vincular</Button>
        </div>
      </Modal>
    </div>
  );
}
