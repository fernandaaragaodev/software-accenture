# YOLO FastAPI - Plantas OfficeHub

API FastAPI pronta com o modelo `models/best.pt` treinado para:

- cadeira
- impressora
- mesa-digitalizadora
- monitor
- notebook
- projetor

## Rodar

```bash
pip install -r requirements.txt
uvicorn app.main:app --reload
```

Ou no Windows:

```bat
run.bat
```

## Testar

Abra:

```txt
http://localhost:8000/docs
```

Use:

```txt
POST /detect
```

O retorno agora contém:

```json
{
  "filename": "planta.jpg",
  "count": 16,
  "summary": {
    "cadeira": 8,
    "monitor": 6,
    "notebook": 2
  },
  "detections": []
}
```

## Confiança

O filtro foi reduzido para `0.001` para retornar praticamente todas as detecções.
Para mudar, crie/edite o arquivo `.env`:

```env
CONFIDENCE_THRESHOLD=0.25
```
