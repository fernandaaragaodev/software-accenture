# Controllers — OfficeHub v1 (SGSP)

Este documento descreve os **Controllers REST** da API e como testá-los manualmente com `curl`, Postman ou Insomnia.

**Base URL:** `http://localhost:8080`  
**Versionamento:** `/api/v1/` (RF-33)  
**Header de autenticação temporário:** `X-Usuario-Id: <UUID do usuário>` — substituir por JWT quando RF-31 estiver implementado.

---

## Pré-requisitos para testes

1. PostgreSQL rodando com o banco `sgsp` (ver `application.properties`).
2. Aplicação iniciada:

```bash
./mvnw spring-boot:run
```

3. Pelo menos um **usuário** cadastrado na tabela `usuarios` — anote o `id` (UUID) para usar no header `X-Usuario-Id`.

---

## Mapa de Controllers

| Controller | Base path | Responsabilidade |
|------------|-----------|------------------|
| `SalaController` | `/api/v1/salas` | CRUD de salas (RF-01, RF-06) |
| `PosicaoController` | `/api/v1/posicoes`, `/api/v1/salas/{id}/posicoes` | Posições de trabalho (RF-07, RF-11) |
| `LayoutController` | `/api/v1/layouts`, `/api/v1/salas/{id}/layout` | Layouts e aprovação (RF-04, RF-05) |
| `DisponibilidadeController` | `/api/v1/salas/{id}/...` | Regras e consulta de disponibilidade (RF-12 a RF-16) |
| `ReservaController` | `/api/v1/reservas` | Solicitação, confirmação, rejeição e cancelamento (RF-17 a RF-23) |
| `NotificacaoController` | `/api/v1/notificacoes` | Consulta de notificações enviadas (RF-24) |

---

## Códigos HTTP retornados

| Código | Quando |
|--------|--------|
| **200** | Consulta ou atualização bem-sucedida |
| **201** | Recurso criado |
| **204** | Operação sem corpo de resposta |
| **400** | Payload inválido (`@Valid`) |
| **404** | Recurso não encontrado (`RecursoNaoEncontradoException`) |
| **422** | Regra de negócio violada (`RegraNegocioException`) |

Respostas de erro seguem o formato:

```json
{
  "timestamp": "2026-06-03T10:00:00-03:00",
  "status": 422,
  "mensagem": "Descrição clara em português"
}
```

---

## Fluxo de teste recomendado (end-to-end)

Ordem sugerida para validar o ciclo completo:

```
1. Criar sala
2. Criar layout
3. Aprovar layout
4. Criar posições
5. Configurar regra de disponibilidade
6. Consultar disponibilidade para uma data
7. Solicitar reserva
8. Confirmar ou rejeitar reserva  → dispara notificação (RF-24)
9. Listar notificações do solicitante
```

Substitua `{USUARIO_ID}`, `{SALA_ID}`, `{LAYOUT_ID}`, `{POSICAO_ID}` e `{RESERVA_ID}` pelos UUIDs retornados nas respostas.

---

## 1. SalaController

### Criar sala

```bash
curl -X POST http://localhost:8080/api/v1/salas \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: {USUARIO_ID}" \
  -d '{
    "nome": "Sala Alpha",
    "descricao": "Open space 3º andar",
    "andar": 3,
    "bloco": "B",
    "capacidadeMaxima": 20,
    "raioProximidade": 2.5
  }'
```

**Esperado:** `201 Created` com JSON contendo `id` e `status: "ATIVA"`.

### Listar salas

```bash
curl http://localhost:8080/api/v1/salas
```

### Buscar por ID

```bash
curl http://localhost:8080/api/v1/salas/{SALA_ID}
```

### Atualizar sala

```bash
curl -X PUT http://localhost:8080/api/v1/salas/{SALA_ID} \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Sala Alpha Atualizada",
    "descricao": "Open space renovado",
    "andar": 3,
    "bloco": "B",
    "capacidadeMaxima": 25,
    "raioProximidade": 3.0
  }'
```

### Inativar sala (soft delete)

```bash
curl -X PATCH http://localhost:8080/api/v1/salas/{SALA_ID}/inativar
```

### Alterar status

```bash
curl -X PATCH http://localhost:8080/api/v1/salas/{SALA_ID}/status \
  -H "Content-Type: application/json" \
  -d '{ "status": "MANUTENCAO" }'
```

Valores aceitos: `ATIVA`, `INATIVA`, `MANUTENCAO`.

---

## 2. LayoutController

### Criar layout

```bash
curl -X POST http://localhost:8080/api/v1/layouts \
  -H "Content-Type: application/json" \
  -d '{
    "salaId": "{SALA_ID}",
    "versao": "v1"
  }'
```

**Esperado:** `201` com `ativo: false`.

### Buscar layout ativo da sala

```bash
curl http://localhost:8080/api/v1/salas/{SALA_ID}/layout/ativo
```

Retorna `404` se nenhum layout estiver aprovado/ativo.

### Aprovar layout

```bash
curl -X PATCH http://localhost:8080/api/v1/layouts/{LAYOUT_ID}/aprovar \
  -H "X-Usuario-Id: {USUARIO_ID}"
```

**Esperado:** `200` com `ativo: true`. Garante um único layout ativo por sala.

### Ajustar coordenadas de posição (RF-05)

```bash
curl -X PATCH http://localhost:8080/api/v1/layouts/posicoes/{POSICAO_ID}/coordenadas \
  -H "Content-Type: application/json" \
  -d '{ "coordX": 10.5, "coordY": 20.0 }'
```

**Esperado:** `204 No Content`.

---

## 3. PosicaoController

> **Pré-requisito:** layout ativo aprovado na sala.

### Criar posição

```bash
curl -X POST http://localhost:8080/api/v1/posicoes \
  -H "Content-Type: application/json" \
  -d '{
    "salaId": "{SALA_ID}",
    "identificador": "P-01",
    "tipo": "Estação Padrão",
    "coordX": 1.0,
    "coordY": 2.0,
    "tipoCadeira": "Ergonômica",
    "tipoMesa": "Retangular"
  }'
```

### Listar posições da sala

```bash
curl http://localhost:8080/api/v1/salas/{SALA_ID}/posicoes
```

### Buscar posição por ID

```bash
curl http://localhost:8080/api/v1/posicoes/{POSICAO_ID}
```

### Atualizar coordenadas

```bash
curl -X PATCH http://localhost:8080/api/v1/posicoes/{POSICAO_ID}/coordenadas \
  -H "Content-Type: application/json" \
  -d '{ "coordX": 5.0, "coordY": 8.0 }'
```

### Inativar posição

```bash
curl -X PATCH http://localhost:8080/api/v1/posicoes/{POSICAO_ID}/inativar
```

---

## 4. DisponibilidadeController

### Criar regra de disponibilidade (RF-12, RF-14, RF-15)

Dias da semana: `0` = segunda, `6` = domingo.

```bash
curl -X POST http://localhost:8080/api/v1/salas/{SALA_ID}/regras-disponibilidade \
  -H "Content-Type: application/json" \
  -d '{
    "antecedenciaMinimaDias": 2,
    "horarios": [
      { "diaSemana": 0, "horaAbertura": "08:00", "horaFechamento": "18:00" },
      { "diaSemana": 1, "horaAbertura": "08:00", "horaFechamento": "18:00" },
      { "diaSemana": 2, "horaAbertura": "08:00", "horaFechamento": "18:00" },
      { "diaSemana": 3, "horaAbertura": "08:00", "horaFechamento": "18:00" },
      { "diaSemana": 4, "horaAbertura": "08:00", "horaFechamento": "18:00" }
    ]
  }'
```

### Consultar regra cadastrada

```bash
curl http://localhost:8080/api/v1/salas/{SALA_ID}/regras-disponibilidade
```

### Consultar disponibilidade para uma data (Fluxo 7.4)

```bash
curl "http://localhost:8080/api/v1/salas/{SALA_ID}/disponibilidade?data=2026-06-10"
```

**Resposta de sucesso:**

```json
{
  "salaId": "...",
  "data": "2026-06-10",
  "disponivel": true,
  "mensagem": "A sala está disponível para reserva na data informada."
}
```

**Resposta quando regra é violada:**

```json
{
  "salaId": "...",
  "data": "2026-06-10",
  "disponivel": false,
  "mensagem": "A reserva deve ser feita com antecedência mínima de 2 dia(s)..."
}
```

### Adicionar exceção (feriado / bloqueio — RF-13)

```bash
curl -X POST http://localhost:8080/api/v1/salas/{SALA_ID}/excecoes-disponibilidade \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: {USUARIO_ID}" \
  -d '{
    "data": "2026-06-10",
    "motivo": "Feriado municipal"
  }'
```

---

## 5. ReservaController

### Solicitar reserva (Fluxo 7.2)

Use uma data que respeite antecedência mínima, dia da semana permitido e sem exceção cadastrada.

```bash
curl -X POST http://localhost:8080/api/v1/reservas \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: {USUARIO_ID}" \
  -d '{
    "salaId": "{SALA_ID}",
    "dataReserva": "2026-06-15",
    "quantidadePessoas": 2,
    "criterioProximidade": "PREFERENCIAL",
    "pessoas": [
      {
        "usuarioId": "{USUARIO_ID}",
        "tipoPreferido1": "Estação Padrão",
        "tipoPreferido2": "Hot Desk"
      },
      {
        "nomeExterno": "Visitante Silva",
        "tipoPreferido1": "Estação Padrão"
      }
    ]
  }'
```

**Esperado:** `201` com `status: "PENDENTE"`.

**Erros comuns (`422`):**
- Sala inativa ou sem regra de disponibilidade
- Data em feriado ou fora dos dias permitidos
- Quantidade de pessoas maior que capacidade ou posições livres

### Buscar reserva

```bash
curl http://localhost:8080/api/v1/reservas/{RESERVA_ID}
```

### Confirmar reserva → dispara notificação RF-24

Simula o resultado positivo do Agente de Alocação (RF-19/RF-22).

```bash
curl -X PATCH http://localhost:8080/api/v1/reservas/{RESERVA_ID}/confirmar
```

**Esperado:** `200` com `status: "CONFIRMADA"`.  
Verifique a notificação na seção 6.

### Rejeitar reserva → dispara notificação RF-24

```bash
curl -X PATCH http://localhost:8080/api/v1/reservas/{RESERVA_ID}/rejeitar \
  -H "Content-Type: application/json" \
  -d '{
    "motivo": "Não há posições do tipo Executivo disponíveis"
  }'
```

**Esperado:** `200` com `status: "REJEITADA"` e `motivoRejeicao` preenchido.

### Cancelar reserva (Fluxo 7.3)

```bash
curl -X DELETE http://localhost:8080/api/v1/reservas/{RESERVA_ID} \
  -H "Content-Type: application/json" \
  -H "X-Usuario-Id: {USUARIO_ID}" \
  -d '{ "motivo": "Reunião remarcada" }'
```

**Esperado:** `200` com `status: "CANCELADA"`. Notificação de cancelamento é enviada ao solicitante.

---

## 6. NotificacaoController (RF-24)

Após confirmar, rejeitar ou cancelar uma reserva, o `NotificacaoService`:

1. Persiste registro na tabela `notificacoes` com status `FILA`.
2. Simula envio (log no console) e atualiza para `ENVIADA`.
3. Em falha, marca como `ERRO` e incrementa `tentativas`.

### Listar notificações de um usuário

```bash
curl http://localhost:8080/api/v1/notificacoes/usuario/{USUARIO_ID}
```

**Exemplo de resposta:**

```json
[
  {
    "id": "...",
    "usuarioId": "...",
    "reservaId": "...",
    "tipo": "RESERVA_CONFIRMADA",
    "assunto": "Reserva confirmada",
    "mensagem": "Sua reserva na sala \"Sala Alpha\" para o dia 2026-06-15 foi confirmada com sucesso.",
    "status": "ENVIADA",
    "tentativas": 0,
    "enviadoEm": "2026-06-03T14:30:00-03:00",
    "createdAt": "2026-06-03T14:30:00-03:00"
  }
]
```

Tipos possíveis: `RESERVA_CONFIRMADA`, `RESERVA_REJEITADA`, `RESERVA_CANCELADA`.

### Reprocessar fila pendente (admin)

```bash
curl -X POST http://localhost:8080/api/v1/notificacoes/processar-fila
```

**Esperado:** `202 Accepted`.

---

## Cenários de teste por requisito

| RF | Cenário | Endpoint | Resultado esperado |
|----|---------|----------|-------------------|
| RF-06 | Inativar sala | `PATCH /salas/{id}/inativar` | Sala some da listagem; histórico preservado |
| RF-13 | Data bloqueada | `POST .../excecoes-disponibilidade` + `GET .../disponibilidade` | `disponivel: false` |
| RF-14 | Antecedência | Reserva com data muito próxima | `422` |
| RF-16 | Regra violada | `POST /reservas` em data inválida | `422` com mensagem explicativa |
| RF-22 | Rejeição com motivo | `PATCH /reservas/{id}/rejeitar` | `motivoRejeicao` preenchido |
| RF-23 | Cancelamento com log | `DELETE /reservas/{id}` | `motivoCancelamento` registrado |
| RF-24 | Notificação | Após confirmar/rejeitar/cancelar | Registro em `/notificacoes/usuario/{id}` |

---

## Testando com Postman / Insomnia

1. Crie uma **Environment** com variáveis: `baseUrl`, `usuarioId`, `salaId`, `layoutId`, `reservaId`.
2. Configure o header global `X-Usuario-Id: {{usuarioId}}` onde necessário.
3. Use **Tests** no Postman para capturar IDs automaticamente:

```javascript
// Exemplo: após POST /salas
const body = pm.response.json();
pm.environment.set("salaId", body.id);
```

---

## Estrutura de arquivos

```
src/main/java/com/accenture/officehub_v1/
├── controller/
│   ├── SalaController.java
│   ├── PosicaoController.java
│   ├── LayoutController.java
│   ├── DisponibilidadeController.java
│   ├── ReservaController.java
│   └── NotificacaoController.java
├── service/
│   └── NotificacaoService.java   ← RF-24
└── exception/
    └── GlobalExceptionHandler.java
```

---

## Observações

- O header `X-Usuario-Id` é provisório até a implementação do JWT (RF-31).
- A alocação automática pelo Agente de IA (RF-19) ainda não está integrada; use `PATCH /confirmar` ou `/rejeitar` para simular o resultado da fila.
- O envio de e-mail/push real será plugado no método `enviar()` do `NotificacaoService` sem alterar os Controllers.
