# SGSP / OfficeHub — Front-end

Aplicação React + Vite + TypeScript para o sistema de gestão de salas e reservas, integrada à API Spring Boot em `../backend/officehub-v1`.

## Requisitos

- Node.js 18+
- API rodando (padrão: `http://localhost:8080`)

## Configuração

```bash
cp .env.example .env
```

Variável principal:

```
VITE_API_URL=http://localhost:8080
```

## Executar

```bash
npm install
npm run dev
```

Acesse: http://localhost:5173

## Perfis (RBAC)

| Perfil | Principais telas |
|--------|------------------|
| `ADMIN_SALA` | Salas, posições, equipamentos, layouts |
| `GESTOR_RESERVAS` | Reservas (confirmar/rejeitar), notificações |
| `USUARIO_FINAL` | Nova reserva, disponibilidade, cancelar reserva |
| `INTEGRADOR` | Nova reserva, disponibilidade |

## Autenticação

- **Login:** `POST http://localhost:8080/api/v1/auth/login`
- **Payload:** `{ "email": "...", "senha": "..." }` (campo `senha`, não `password`)
- **Resposta:** `{ accessToken, refreshToken, tokenType }`
- **Storage:** `localStorage.accessToken` e `localStorage.refreshToken`
- **Header:** `Authorization: Bearer <accessToken>` (interceptor em `src/api/axios.ts`)
- **CORS:** se o navegador bloquear requisições diretas a `:8080`, use `VITE_API_URL=` (vazio) e o proxy do Vite (`vite.config.ts`)

## Observações da API

- **Listagem de reservas**: a API não expõe `GET /reservas`; o front mantém IDs recentes em `localStorage` e permite ao gestor rastrear UUIDs.
- **Detalhe da reserva (`GET /reservas/{id}`)**: restrito a `GESTOR_RESERVAS`; demais perfis usam dados em cache após a criação.
- **Notificações**: exige `usuarioId` (UUID); gravado automaticamente ao criar reserva (`solicitanteId`).
- **Refresh token**: interceptor Axios em `src/api/axios.ts`.

## Estrutura

Ver `src/` — `api/`, `components/`, `pages/`, `routes/`, `stores/`, `types/`, `utils/`.
