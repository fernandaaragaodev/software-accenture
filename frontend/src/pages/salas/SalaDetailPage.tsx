import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { regrasDisponibilidadeApi } from '../../api/regras-disponibilidade';
import { reservasApi } from '../../api/reservas';
import { salasApi } from '../../api/salas';
import { SalaReadinessBadge } from '../../components/salas/SalaReadinessBadge';
import {
  ConfirmDialog,
  LoadingState,
  PageHeader,
  SkeletonGrid,
  StatusBadge,
} from '../../components/ui';
import { useToast } from '../../context/ToastContext';
import {
  DIAS_SEMANA,
  enrichSala,
  formatDate,
  formatDateTime,
  type SalaEnriched,
} from '../../utils/salas';
import type { RegraDisponibilidadeResponse, ReservaResumoResponse } from '../../types';

export function SalaDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { showToast } = useToast();
  const [sala, setSala] = useState<SalaEnriched | null>(null);
  const [regrasDisponiveis, setRegrasDisponiveis] = useState<RegraDisponibilidadeResponse[]>([]);
  const [regraSelecionada, setRegraSelecionada] = useState('');
  const [reservas, setReservas] = useState<ReservaResumoResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState(false);
  const [showArchive, setShowArchive] = useState(false);

  const carregar = useCallback(async () => {
    if (!id) return;
    setLoading(true);
    try {
      const [enriched, todasRegras, reservasData] = await Promise.all([
        enrichSala(await salasApi.obter(id)),
        regrasDisponibilidadeApi.listar().catch(() => []),
        reservasApi.listar(false, undefined, 0, 200).catch(() => ({
          content: [] as ReservaResumoResponse[],
        })),
      ]);
      setSala(enriched);
      setRegrasDisponiveis(todasRegras.filter((r) => !r.salaId));
      setReservas(reservasData.content.filter((r) => r.salaId === id));
    } catch (err) {
      showToast(
        err instanceof ApiException ? err.message : 'Erro ao carregar sala',
        'error',
      );
      setSala(null);
    } finally {
      setLoading(false);
    }
  }, [id, showToast]);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const stats = useMemo(() => {
    const posicoesAtivas = sala?.posicoes.filter((p) => p.status === 'ATIVA') ?? [];
    const totalReservas = reservas.length;
    const confirmadas = reservas.filter((r) => r.status === 'CONFIRMADA').length;
    const taxaOcupacao =
      posicoesAtivas.length > 0
        ? Math.round((confirmadas / Math.max(totalReservas, 1)) * 100)
        : 0;
    const ultimaReserva = reservas.sort(
      (a, b) => new Date(b.dataReserva).getTime() - new Date(a.dataReserva).getTime(),
    )[0];

    return { totalReservas, taxaOcupacao, ultimaReserva, posicoesAtivas };
  }, [sala, reservas]);

  async function handleStatusChange(status: 'ATIVA' | 'INATIVA' | 'MANUTENCAO') {
    if (!id) return;
    setActionLoading(true);
    try {
      await salasApi.atualizarStatus(id, { status });
      showToast(`Status atualizado para ${status}`, 'success');
      carregar();
    } catch (err) {
      showToast(
        err instanceof ApiException ? err.message : 'Erro ao atualizar status',
        'error',
      );
    } finally {
      setActionLoading(false);
    }
  }

  async function handleAtribuirRegra() {
    if (!id || !regraSelecionada) return;
    setActionLoading(true);
    try {
      await salasApi.atribuirRegra(id, { regraId: regraSelecionada });
      showToast('Regra atribuída à sala', 'success');
      setRegraSelecionada('');
      carregar();
    } catch (err) {
      showToast(
        err instanceof ApiException ? err.message : 'Erro ao atribuir regra',
        'error',
      );
    } finally {
      setActionLoading(false);
    }
  }

  async function handleDesatribuirRegra() {
    if (!id) return;
    setActionLoading(true);
    try {
      await salasApi.desatribuirRegra(id);
      showToast('Regra desatribuída', 'success');
      carregar();
    } catch (err) {
      showToast(
        err instanceof ApiException ? err.message : 'Erro ao desatribuir regra',
        'error',
      );
    } finally {
      setActionLoading(false);
    }
  }

  async function handleArchive() {
    if (!id) return;
    try {
      await salasApi.inativar(id);
      showToast('Sala arquivada', 'success');
      navigate('/salas');
    } catch (err) {
      showToast(
        err instanceof ApiException ? err.message : 'Erro ao arquivar',
        'error',
      );
    }
  }

  if (loading) {
    return (
      <div>
        <SkeletonGrid count={4} variant="card" />
        <LoadingState message="Carregando detalhes da sala..." />
      </div>
    );
  }

  if (!sala) {
    return (
      <div className="empty-state">
        <h3>Sala não encontrada</h3>
        <Link to="/salas" className="btn btn-primary">
          Voltar para salas
        </Link>
      </div>
    );
  }

  const posLivres = stats.posicoesAtivas.length;
  const posOcupadas = 0;

  return (
    <div>
      <PageHeader
        title={sala.nome}
        subtitle={sala.descricao ?? 'Detalhes e configuração da sala'}
        action={
          <div className="btn-group">
            <SalaReadinessBadge readiness={sala.readiness} />
            <Link to={`/salas/${id}/editar`} className="btn btn-ghost">
              Editar
            </Link>
            <Link to="/salas" className="btn btn-ghost">
              Voltar
            </Link>
          </div>
        }
      />

      {!sala.layout && (
        <div className="alert alert-warning" role="alert">
          Esta sala ainda não possui layout configurado.
        </div>
      )}

      <div className="sala-detail-grid">
        <section className="card sala-detail-card">
          <h3>Informações Gerais</h3>
          <dl className="detail-list">
            <div><dt>Nome</dt><dd>{sala.nome}</dd></div>
            <div><dt>Capacidade</dt><dd>{sala.capacidadeMaxima}</dd></div>
            <div><dt>Bloco</dt><dd>{sala.bloco ?? '—'}</dd></div>
            <div><dt>Andar</dt><dd>{sala.andar ?? '—'}</dd></div>
            <div><dt>Status</dt><dd><StatusBadge status={sala.status} /></dd></div>
            <div><dt>Criada em</dt><dd>{formatDate(sala.createdAt)}</dd></div>
          </dl>
          <div className="btn-group mt-md">
            <button
              type="button"
              className="btn btn-sm btn-ghost"
              disabled={actionLoading}
              onClick={() => handleStatusChange('ATIVA')}
            >
              Ativar
            </button>
            <button
              type="button"
              className="btn btn-sm btn-ghost"
              disabled={actionLoading}
              onClick={() => handleStatusChange('MANUTENCAO')}
            >
              Manutenção
            </button>
            <button
              type="button"
              className="btn btn-sm btn-danger"
              disabled={actionLoading}
              onClick={() => handleStatusChange('INATIVA')}
            >
              Inativar
            </button>
          </div>
        </section>

        <section className="card sala-detail-card" id="disponibilidade">
          <h3>Disponibilidade</h3>
          {sala.regra ? (
            <>
              <p><strong>{sala.regra.nome}</strong></p>
              <p className="muted">
                Antecedência mínima: {sala.regra.antecedenciaMinimaDias} dia(s)
              </p>
              <ul className="simple-list">
                {sala.regra.horarios.map((h) => (
                  <li key={h.id ?? h.diaSemana}>
                    {DIAS_SEMANA[h.diaSemana]}: {h.horaAbertura.slice(0, 5)} –{' '}
                    {h.horaFechamento.slice(0, 5)}
                  </li>
                ))}
              </ul>
              <p className="mt-md">
                <span className="badge badge-success">Configurada</span>
              </p>
              <button
                type="button"
                className="btn btn-sm btn-ghost mt-md"
                disabled={actionLoading}
                onClick={handleDesatribuirRegra}
              >
                Desatribuir regra
              </button>
            </>
          ) : (
            <>
              <p className="muted">Disponibilidade não configurada.</p>
              <div className="form inline-form mt-md">
                <label>
                  Selecionar regra
                  <select
                    value={regraSelecionada}
                    onChange={(e) => setRegraSelecionada(e.target.value)}
                  >
                    <option value="">Selecione...</option>
                    {regrasDisponiveis.map((r) => (
                      <option key={r.id} value={r.id}>{r.nome}</option>
                    ))}
                  </select>
                </label>
                <button
                  type="button"
                  className="btn btn-primary"
                  disabled={!regraSelecionada || actionLoading}
                  onClick={handleAtribuirRegra}
                >
                  Atribuir
                </button>
              </div>
            </>
          )}
        </section>

        <section className="card sala-detail-card">
          <h3>Layout</h3>
          {sala.layout ? (
            <dl className="detail-list">
              <div>
                <dt>Layout ativo</dt>
                <dd>v{sala.layout.versao ?? sala.layout.id.slice(0, 8)}</dd>
              </div>
              <div>
                <dt>Status</dt>
                <dd>
                  {sala.layout.aprovadoPorId ? (
                    <span className="badge badge-success">Aprovado</span>
                  ) : (
                    <span className="badge badge-warning">Pendente aprovação</span>
                  )}
                </dd>
              </div>
              <div><dt>Posições</dt><dd>{stats.posicoesAtivas.length}</dd></div>
              <div><dt>Livres</dt><dd>{posLivres - posOcupadas}</dd></div>
              <div><dt>Ocupadas</dt><dd>{posOcupadas}</dd></div>
            </dl>
          ) : (
            <p className="muted">Nenhum layout cadastrado.</p>
          )}
          <div className="btn-group mt-md">
            <Link to={`/layouts?salaId=${id}`} className="btn btn-sm btn-ghost">
              Configurar Layout
            </Link>
            <Link to={`/posicoes?salaId=${id}`} className="btn btn-sm btn-ghost">
              Posições
            </Link>
          </div>
        </section>

        <section className="card sala-detail-card">
          <h3>Equipamentos</h3>
          <dl className="detail-list">
            <div><dt>Total vinculados</dt><dd>{sala.totalEquipamentos}</dd></div>
          </dl>
          <Link to={`/equipamentos?salaId=${id}`} className="btn btn-sm btn-ghost mt-md">
            Gerenciar equipamentos
          </Link>
        </section>

        <section className="card sala-detail-card">
          <h3>Estatísticas</h3>
          <dl className="detail-list">
            <div><dt>Total de reservas</dt><dd>{stats.totalReservas}</dd></div>
            <div><dt>Taxa de ocupação</dt><dd>{stats.taxaOcupacao}%</dd></div>
            <div>
              <dt>Última reserva</dt>
              <dd>
                {stats.ultimaReserva
                  ? formatDateTime(`${stats.ultimaReserva.dataReserva}T${stats.ultimaReserva.horaInicio}`)
                  : '—'}
              </dd>
            </div>
          </dl>
          <Link to={`/reservas/nova?salaId=${id}`} className="btn btn-sm btn-primary mt-md">
            Nova Reserva
          </Link>
        </section>
      </div>

      <div className="mt-lg">
        <button type="button" className="btn btn-danger btn-sm" onClick={() => setShowArchive(true)}>
          Arquivar sala
        </button>
      </div>

      <ConfirmDialog
        open={showArchive}
        title="Arquivar sala"
        message="A sala será arquivada sem exclusão física. Confirma?"
        confirmLabel="Arquivar"
        variant="danger"
        onConfirm={handleArchive}
        onCancel={() => setShowArchive(false)}
      />
    </div>
  );
}
