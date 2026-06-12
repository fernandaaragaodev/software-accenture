import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { ApiException } from '../../api/client';
import { layoutsApi } from '../../api/layouts';
import { posicoesApi } from '../../api/posicoes';
import { regrasDisponibilidadeApi } from '../../api/regras-disponibilidade';
import { salasApi } from '../../api/salas';
import { tiposEquipamentoApi } from '../../api/tipos-equipamento';
import { ConfirmDialog } from '../../components/ConfirmDialog';
import { DetailCardSkeleton } from '../../components/Skeleton';
import { useToast } from '../../components/Toast';
import { Alert, PageHeader, ReadinessBadge, StatusBadge } from '../../components/ui';
import {
  computeReservaStats,
  DIAS_SEMANA,
  formatBlocoAndar,
  formatDate,
  getSalaReadiness,
  toApiTime,
} from './salasUtils';
import { useSalaEnriched } from './useSalaEnrichment';

export function SalaDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const toast = useToast();
  const { data: sala, loading, error, reload } = useSalaEnriched(id);
  const [regrasDisponiveis, setRegrasDisponiveis] = useState<{ id: string; nome: string }[]>([]);
  const [regraSelecionada, setRegraSelecionada] = useState('');
  const [actionError, setActionError] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [confirmArchive, setConfirmArchive] = useState(false);
  const [confirmDuplicate, setConfirmDuplicate] = useState(false);

  useEffect(() => {
    regrasDisponibilidadeApi
      .listar()
      .then((regras) => setRegrasDisponiveis(regras.filter((r) => !r.salaId)))
      .catch(() => setRegrasDisponiveis([]));
  }, []);

  if (loading) {
    return (
      <div>
        <PageHeader title="Carregando..." subtitle="Buscando informações da sala" />
        <div className="detail-grid">
          {Array.from({ length: 5 }).map((_, i) => (
            <DetailCardSkeleton key={i} />
          ))}
        </div>
      </div>
    );
  }

  if (!sala) {
    return <Alert message={error || 'Sala não encontrada'} />;
  }

  const readiness = getSalaReadiness(sala, sala.regra, sala.layout, sala.posicoes);
  const posicoesAtivas = sala.posicoes.filter((p) => p.status === 'ATIVA');
  const reservaStats = computeReservaStats(sala.reservas);
  const totalPosicoes = sala.posicoes.length;
  const posicoesLivres = sala.posicoesLivres ?? posicoesAtivas.length;
  const posicoesOcupadas = sala.posicoesOcupadas ?? 0;
  const taxaOcupacao =
    totalPosicoes > 0
      ? Math.round((posicoesOcupadas / totalPosicoes) * 100)
      : 0;
  const temLayout = !!sala.layout;

  async function handleStatusChange(status: 'ATIVA' | 'INATIVA' | 'MANUTENCAO') {
    if (!id) return;
    setActionError('');
    setActionLoading(true);
    try {
      await salasApi.atualizarStatus(id, { status });
      toast.success(`Status atualizado para ${status}`);
      reload();
    } catch (err) {
      setActionError(err instanceof ApiException ? err.message : 'Erro ao atualizar status');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleAtribuirRegra() {
    if (!id || !regraSelecionada) return;
    setActionError('');
    setActionLoading(true);
    try {
      await salasApi.atribuirRegra(id, { regraId: regraSelecionada });
      toast.success('Regra atribuída à sala');
      setRegraSelecionada('');
      reload();
    } catch (err) {
      setActionError(err instanceof ApiException ? err.message : 'Erro ao atribuir regra');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleDesatribuirRegra() {
    if (!id) return;
    setActionError('');
    setActionLoading(true);
    try {
      await salasApi.desatribuirRegra(id);
      toast.success('Regra desatribuída da sala');
      reload();
    } catch (err) {
      setActionError(err instanceof ApiException ? err.message : 'Erro ao desatribuir regra');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleAprovarLayout() {
    if (!sala?.layout) return;
    setActionLoading(true);
    try {
      await layoutsApi.aprovar(sala.layout.id);
      toast.success('Layout aprovado com sucesso');
      reload();
    } catch (err) {
      toast.error(err instanceof ApiException ? err.message : 'Erro ao aprovar layout');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleDuplicate() {
    if (!sala) return;
    setActionLoading(true);
    try {
      const nova = await salasApi.criar({
        nome: `${sala.nome} (Cópia)`,
        descricao: sala.descricao,
        andar: sala.andar,
        bloco: sala.bloco,
        capacidadeMaxima: sala.capacidadeMaxima,
        raioProximidade: sala.raioProximidade,
      });

      if (sala.status !== 'ATIVA') {
        await salasApi.atualizarStatus(nova.id, { status: sala.status });
      }

      if (sala.regra) {
        await salasApi.criarRegraDisponibilidade(nova.id, {
          nome: `${sala.regra.nome} (Cópia)`,
          antecedenciaMinimaDias: sala.regra.antecedenciaMinimaDias,
          horarios: sala.regra.horarios.map((h) => ({
            diaSemana: h.diaSemana,
            horaAbertura: toApiTime(h.horaAbertura.slice(0, 5)),
            horaFechamento: toApiTime(h.horaFechamento.slice(0, 5)),
          })),
        });
      }

      if (sala.layout) {
        const novoLayout = await layoutsApi.criar({
          salaId: nova.id,
          versao: sala.layout.versao ? `${sala.layout.versao}-copia` : undefined,
        });

        if (sala.layout.aprovadoEm) {
          await layoutsApi.aprovar(novoLayout.id);
        }

        for (const posicao of sala.posicoes) {
          const novaPosicao = await posicoesApi.criar({
            salaId: nova.id,
            identificador: posicao.identificador,
            tipo: posicao.tipo,
            coordX: posicao.coordX,
            coordY: posicao.coordY,
            tipoCadeira: posicao.tipoCadeira,
            tipoMesa: posicao.tipoMesa,
          });

          const equipamentos = await tiposEquipamentoApi.listarPorPosicao(posicao.id).catch(() => []);
          for (const equip of equipamentos) {
            await tiposEquipamentoApi.vincularPosicao(novaPosicao.id, {
              tipoEquipamentoId: equip.tipoEquipamentoId,
              quantidade: equip.quantidade,
              observacao: equip.observacao,
            });
          }
        }
      }

      toast.success('Sala duplicada com sucesso.');
      setConfirmDuplicate(false);
      navigate(`/salas/${nova.id}`);
    } catch (err) {
      toast.error(err instanceof ApiException ? err.message : 'Erro ao duplicar sala');
    } finally {
      setActionLoading(false);
    }
  }

  async function handleArchive() {
    if (!id) return;
    setActionLoading(true);
    try {
      await salasApi.inativar(id);
      toast.success('Sala arquivada com sucesso.');
      setConfirmArchive(false);
      navigate('/salas');
    } catch (err) {
      toast.error(err instanceof ApiException ? err.message : 'Erro ao arquivar sala');
    } finally {
      setActionLoading(false);
    }
  }

  return (
    <div>
      <PageHeader
        title={sala.nome}
        subtitle={sala.descricao}
        action={
          <div className="btn-group">
            <Link to="/salas" className="btn btn-ghost">
              Voltar
            </Link>
            <Link to={`/salas/${sala.id}/editar`} className="btn btn-ghost">
              Editar
            </Link>
            <button type="button" className="btn btn-ghost" onClick={() => setConfirmDuplicate(true)}>
              Duplicar
            </button>
            <button type="button" className="btn btn-danger" onClick={() => setConfirmArchive(true)}>
              Arquivar
            </button>
          </div>
        }
      />

      <div className="sala-detail-badges mb-lg">
        <ReadinessBadge readiness={readiness} />
        <StatusBadge status={sala.status} />
      </div>

      <Alert message={error || actionError} />

      <div className="detail-grid">
        <div className="card">
          <h3>Informações Gerais</h3>
          <dl className="detail-list">
            <div><dt>Nome</dt><dd>{sala.nome}</dd></div>
            <div><dt>Capacidade</dt><dd>{sala.capacidadeMaxima}</dd></div>
            <div><dt>Bloco / Andar</dt><dd>{formatBlocoAndar(sala.bloco, sala.andar)}</dd></div>
            <div><dt>Status</dt><dd><StatusBadge status={sala.status} /></dd></div>
            <div><dt>Raio proximidade</dt><dd>{sala.raioProximidade ?? '—'}</dd></div>
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
        </div>

        <div className="card" id="disponibilidade">
          <h3>Disponibilidade</h3>
          {sala.regra ? (
            <>
              <p><strong>{sala.regra.nome}</strong></p>
              <p>Situação atual: <strong>{sala.status === 'ATIVA' ? 'Disponível para reservas' : 'Indisponível'}</strong></p>
              <p>Antecedência mínima: <strong>{sala.regra.antecedenciaMinimaDias} dia(s)</strong></p>
              <ul className="simple-list">
                {sala.regra.horarios.map((h) => (
                  <li key={h.id ?? h.diaSemana}>
                    {DIAS_SEMANA[h.diaSemana]}: {h.horaAbertura.slice(0, 5)} – {h.horaFechamento.slice(0, 5)}
                  </li>
                ))}
              </ul>
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
              <div className="alert alert-warning">Nenhuma regra de disponibilidade configurada.</div>
              <div className="form inline-form mt-md">
                <label>
                  Selecionar regra existente
                  <select value={regraSelecionada} onChange={(e) => setRegraSelecionada(e.target.value)}>
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
              <p className="muted mt-md">
                <Link to="/regras-disponibilidade">Gerenciar regras de disponibilidade</Link>
              </p>
            </>
          )}
        </div>

        <div className="card">
          <h3>Layout</h3>
          {!temLayout ? (
            <div className="alert alert-warning">
              Esta sala ainda não possui layout configurado.
            </div>
          ) : (
            <dl className="detail-list">
              <div><dt>Layout ativo</dt><dd>{sala.layout?.versao ?? sala.layout?.id.slice(0, 8)}</dd></div>
              <div><dt>Posições cadastradas</dt><dd>{totalPosicoes}</dd></div>
              <div><dt>Posições ocupadas</dt><dd>{posicoesOcupadas}</dd></div>
              <div><dt>Posições livres</dt><dd>{posicoesLivres}</dd></div>
              <div><dt>Aprovado</dt><dd>{sala.layout?.aprovadoEm ? formatDate(sala.layout.aprovadoEm) : 'Pendente'}</dd></div>
            </dl>
          )}

          <div className="btn-group mt-md">
            <Link to={`/layouts?salaId=${sala.id}`} className="btn btn-sm btn-ghost">
              Criar Layout
            </Link>
            <Link to={`/posicoes?salaId=${sala.id}`} className="btn btn-sm btn-ghost">
              Adicionar Posições
            </Link>
            <Link to={`/layouts?salaId=${sala.id}`} className="btn btn-sm btn-ghost">
              Importar Layout
            </Link>
            <Link to={`/equipamentos?salaId=${sala.id}`} className="btn btn-sm btn-ghost">
              Configurar Equipamentos
            </Link>
            {sala.layout && !sala.layout.aprovadoEm && (
              <button
                type="button"
                className="btn btn-sm btn-primary"
                disabled={actionLoading}
                onClick={handleAprovarLayout}
              >
                Aprovar Layout
              </button>
            )}
          </div>
        </div>

        <div className="card">
          <h3>Equipamentos</h3>
          <dl className="detail-list">
            <div><dt>Quantidade total</dt><dd>{sala.totalEquipamentos}</dd></div>
            <div><dt>Posições com equipamento</dt><dd>{sala.posicoes.length > 0 ? 'Consulte posições' : '—'}</dd></div>
          </dl>
          <Link to={`/equipamentos?salaId=${sala.id}`} className="btn btn-sm btn-ghost mt-md">
            Gerenciar equipamentos
          </Link>
        </div>

        <div className="card">
          <h3>Estatísticas</h3>
          <div className="stats-grid">
            <div className="stat-card">
              <span>Total de reservas</span>
              <strong>{reservaStats.total}</strong>
            </div>
            <div className="stat-card success">
              <span>Taxa de ocupação</span>
              <strong>{taxaOcupacao}%</strong>
            </div>
            <div className="stat-card">
              <span>Última reserva</span>
              <strong className="stat-small">
                {reservaStats.ultima
                  ? `${reservaStats.ultima.dataReserva} ${reservaStats.ultima.horaInicio.slice(0, 5)}`
                  : '—'}
              </strong>
            </div>
          </div>
          <Link to={`/reservas/nova?salaId=${sala.id}`} className="btn btn-sm btn-primary mt-md">
            Nova Reserva
          </Link>
        </div>
      </div>

      <ConfirmDialog
        open={confirmArchive}
        title="Arquivar sala"
        message={`Tem certeza que deseja arquivar "${sala.nome}"? Esta ação não exclui os dados permanentemente.`}
        confirmLabel="Arquivar"
        variant="danger"
        loading={actionLoading}
        onConfirm={handleArchive}
        onCancel={() => setConfirmArchive(false)}
      />

      <ConfirmDialog
        open={confirmDuplicate}
        title="Duplicar sala"
        message={`Deseja criar uma cópia de "${sala.nome}" incluindo regras, layout e posições?`}
        confirmLabel="Duplicar"
        loading={actionLoading}
        onConfirm={handleDuplicate}
        onCancel={() => setConfirmDuplicate(false)}
      />
    </div>
  );
}
