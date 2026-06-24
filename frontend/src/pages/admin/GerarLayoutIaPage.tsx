import { type FormEvent, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { confirmarSalaIa, gerarLayoutPorIa, negarSalaIa } from '../../api/ia-layout';
import { Alert, PageHeader, StatusBadge } from '../../components/ui';
import { useToast } from '../../context/ToastContext';
import type { GerarLayoutPorIaResponse } from '../../types';

function formatCoord(value?: number | string | null) {
  if (value == null || value === '') return '—';
  const num = typeof value === 'number' ? value : Number(value);
  return Number.isNaN(num) ? '—' : `${num.toFixed(2)} m`;
}

function formatEquipamentos(
  equipamentos: GerarLayoutPorIaResponse['posicoes'][number]['equipamentos'],
) {
  if (!equipamentos.length) return 'Nenhum equipamento detectado';
  return equipamentos
    .map((eq) =>
      eq.quantidade > 1
        ? `${eq.tipoEquipamentoNome} (×${eq.quantidade})`
        : eq.tipoEquipamentoNome,
    )
    .join(', ');
}

export function GerarLayoutIaPage() {
  const navigate = useNavigate();
  const { showToast } = useToast();

  const [nomeSala, setNomeSala] = useState('Sala Desenvolvimento');
  const [largura, setLargura] = useState('12');
  const [altura, setAltura] = useState('8');
  const [imagem, setImagem] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [negando, setNegando] = useState(false);
  const [error, setError] = useState('');
  const [preview, setPreview] = useState<GerarLayoutPorIaResponse | null>(null);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    setPreview(null);

    if (!imagem) {
      setError('Selecione a imagem da planta baixa.');
      return;
    }

    setLoading(true);
    try {
      const data = await gerarLayoutPorIa({
        nomeSala,
        largura: Number(largura),
        altura: Number(altura),
        imagem,
      });
      setPreview(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao gerar layout por IA.');
    } finally {
      setLoading(false);
    }
  }

  async function handleConfirmar() {
    if (!preview) return;
    setConfirmando(true);
    setError('');
    try {
      await confirmarSalaIa(preview.sala.id);
      showToast(`Sala "${preview.sala.nome}" confirmada e disponível em Salas.`, 'success');
      navigate('/salas');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao confirmar a sala.');
    } finally {
      setConfirmando(false);
    }
  }

  async function handleNegar() {
    if (!preview) return;
    setNegando(true);
    setError('');
    try {
      await negarSalaIa(preview.sala.id);
      showToast('Geração descartada. Você pode gerar uma nova sala.', 'info');
      setPreview(null);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao descartar a sala.');
    } finally {
      setNegando(false);
    }
  }

  function handleGerarOutra() {
    setPreview(null);
    setError('');
  }

  return (
    <div className="page">
      <PageHeader
        title="Gerar Sala por IA"
        subtitle={
          preview
            ? 'Revise o layout gerado antes de confirmar ou descartar a sala.'
            : 'Envie uma planta baixa para criar automaticamente sala, layout, posições e equipamentos.'
        }
      />

      {error && <Alert message={error} />}

      {!preview ? (
        <form className="card form-grid" onSubmit={handleSubmit}>
          <label>
            Nome da sala
            <input
              value={nomeSala}
              onChange={(e) => setNomeSala(e.target.value)}
              required
            />
          </label>

          <div className="form-row">
            <label>
              Largura (m)
              <input
                type="number"
                min="0.1"
                step="0.1"
                value={largura}
                onChange={(e) => setLargura(e.target.value)}
                required
              />
            </label>

            <label>
              Altura (m)
              <input
                type="number"
                min="0.1"
                step="0.1"
                value={altura}
                onChange={(e) => setAltura(e.target.value)}
                required
              />
            </label>
          </div>

          <label>
            Planta baixa (imagem)
            <input
              type="file"
              accept="image/*"
              onChange={(e) => setImagem(e.target.files?.[0] ?? null)}
              required
            />
          </label>

          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Processando...' : 'Gerar layout por IA'}
          </button>
        </form>
      ) : (
        <section className="card ia-layout-preview">
          <div className="ia-preview-header">
            <div>
              <h3>{preview.sala.nome}</h3>
              <p className="muted">
                Revise as posições e equipamentos detectados na planta baixa antes de publicar a sala.
              </p>
            </div>
            <StatusBadge status="PENDENTE_APROVACAO" />
          </div>

          <div className="detail-grid mt-lg">
            <div className="info-box">
              <strong>Dimensões</strong>
              <p>
                {formatCoord(preview.sala.largura)} × {formatCoord(preview.sala.altura)}
              </p>
            </div>
            <div className="info-box">
              <strong>Capacidade detectada</strong>
              <p>{preview.sala.capacidadeMaxima} posições</p>
            </div>
            <div className="info-box">
              <strong>Detecções YOLO</strong>
              <p>{preview.totalDetecoes} objetos · {preview.totalEstacoes} estações agrupadas</p>
            </div>
            <div className="info-box">
              <strong>Layout</strong>
              <p>Versão {preview.layout.versao ?? '1'} · {preview.layout.ativo ? 'Ativo' : 'Inativo'}</p>
            </div>
          </div>

          <div className="table-wrap mt-lg">
            <h4>Posições criadas ({preview.posicoes.length})</h4>
            <table>
              <thead>
                <tr>
                  <th>Posição</th>
                  <th>Tipo</th>
                  <th>Coordenadas (X, Y)</th>
                  <th>Equipamentos atribuídos</th>
                </tr>
              </thead>
              <tbody>
                {preview.posicoes.map(({ posicao, equipamentos }) => (
                  <tr key={posicao.id}>
                    <td><strong>{posicao.identificador}</strong></td>
                    <td>{posicao.tipo?.replace(/_/g, ' ') ?? '—'}</td>
                    <td>
                      {formatCoord(posicao.coordX)}, {formatCoord(posicao.coordY)}
                    </td>
                    <td>{formatEquipamentos(equipamentos)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="info-box mt-lg">
            <strong>O que acontece ao confirmar?</strong>
            <p>
              A sala será publicada em <Link to="/salas">Salas</Link> e ficará disponível para
              configuração de disponibilidade e reservas.
            </p>
          </div>

          <div className="form-actions mt-lg">
            <button
              type="button"
              className="btn btn-ghost"
              onClick={handleGerarOutra}
              disabled={confirmando || negando}
            >
              Voltar ao formulário
            </button>
            <button
              type="button"
              className="btn btn-danger"
              onClick={handleNegar}
              disabled={confirmando || negando}
            >
              {negando ? 'Descartando...' : 'Negar sala'}
            </button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleConfirmar}
              disabled={confirmando || negando}
            >
              {confirmando ? 'Confirmando...' : 'Confirmar sala'}
            </button>
          </div>
        </section>
      )}
    </div>
  );
}
