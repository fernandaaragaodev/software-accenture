import { type FormEvent, useState } from 'react';
import { gerarLayoutPorIa } from '../../api/ia-layout';
import { Alert, PageHeader } from '../../components/ui';

export function GerarLayoutIaPage() {
  const [nomeSala, setNomeSala] = useState('Sala Desenvolvimento');
  const [largura, setLargura] = useState('12');
  const [altura, setAltura] = useState('8');
  const [imagem, setImagem] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [resultado, setResultado] = useState<string | null>(null);

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    setError('');
    setResultado(null);

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
      setResultado(JSON.stringify(data, null, 2));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Falha ao gerar layout por IA.');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="page">
      <PageHeader
        title="Gerar Sala por IA"
        subtitle="Envie uma planta baixa para criar automaticamente sala, layout, posições e equipamentos."
      />

      {error && <Alert message={error} />}

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

      {resultado && (
        <section className="card" style={{ marginTop: '1rem' }}>
          <h3>Resultado</h3>
          <pre style={{ whiteSpace: 'pre-wrap', overflow: 'auto' }}>{resultado}</pre>
        </section>
      )}
    </div>
  );
}
