# software-accenture

Back-end da aplicação desenvolvida para a Accenture nas aulas de Residência em Software II.

## Frontend — Reserva de Posições (UI premium)

A interface em `frontend/officehub-web/public/reserva-posicoes.html` (Alpine.js + Tailwind) integra-se ao backend existente via `/api/v1` (proxy do Vite em desenvolvimento).

- **Pelo app:** menu **Salas** (dentro do React, sem abrir outra página)
- **URL alternativa (opcional):** `http://127.0.0.1:3000/reserva-posicoes.html`
- **Sessão:** usa o mesmo `localStorage` (`officehub.session`) do login React; sem sessão, redireciona para `/` com `returnTo`

### Executar

1. Backend: `cd backend/officehub-api && ./mvnw spring-boot:run` (porta 8080)
2. Frontend: `cd frontend/officehub-web && npm run dev` (porta 3000)
3. Faça login no app e abra **Salas**, ou acesse `/reserva-posicoes.html` após login
