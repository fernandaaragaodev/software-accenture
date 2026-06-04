# Segurança — JWT, Refresh Token e RBAC (OfficeHub v1)

Este documento descreve tudo que foi adicionado na camada de segurança do backend `officehub-v1`, alinhado aos critérios de aceite do projeto (JWT, refresh token, RBAC, hash de senha, auditoria e rate limiting no login).

---

## Visão geral

| Antes | Depois |
|--------|--------|
| Apenas modelagem (`Usuario`, `Perfil`, `RefreshToken`, etc.) | Fluxo completo de autenticação e autorização |
| ~20% da segurança | Camada funcional JWT + RBAC + auditoria |

**Stack:** Spring Security, BCrypt, JJWT (HS256), filtros stateless, JSON padronizado para 401/403/429.

---

## 1. Dependências (`pom.xml`)

- `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.12.6) para assinar e validar JWT.

---

## 2. Configuração (`application.properties`)

```properties
app.security.jwt.secret=...
app.security.jwt.expiration-minutes=60
app.security.refresh-token.expiration-days=7
app.security.rate-limit.login-per-minute=10
```

- **JWT:** chave e tempo de vida do access token (não fica hardcoded no código).
- **Refresh token:** validade em dias.
- **Rate limit:** tentativas de login por IP por minuto.

> Em produção, use variáveis de ambiente para `app.security.jwt.secret` e habilite **HTTPS** no proxy/reverse proxy (requisito do PDF; a aplicação não força TLS sozinha).

---

## 3. Repositories ajustados

### `UsuarioRepository`
- `findByEmailIgnoreCaseAndDeletedAtIsNull(String email)`
- `existsByEmailIgnoreCase(String email)`

### `UsuarioPerfilRepository`
- `findByUsuarioId(UUID usuarioId)` — carrega perfis com `JOIN FETCH` para montar authorities.

### `RefreshTokenRepository`
- `findByTokenHashAndRevogadoEmIsNull(String tokenHash)`
- `deleteByUsuario_Id(UUID usuarioId)` — rotaciona tokens no login/refresh.

### `PerfilRepository`
- `findByNomeIgnoreCase(String nome)` — usado no registro (perfil `USUARIO_FINAL`).

---

## 4. DTOs de autenticação

| Classe | Uso |
|--------|-----|
| `LoginRequest` | e-mail + senha |
| `LoginResponse` | `accessToken`, `refreshToken`, `tokenType` (`Bearer`) |
| `RefreshTokenRequest` | corpo do refresh/logout |
| `CriarUsuarioRequest` | registro (teste) |
| `UsuarioResponse` | resposta do register |

---

## 5. Endpoints — `AuthController` (`/api/v1/auth`)

| Método | Path | Descrição |
|--------|------|-----------|
| `POST` | `/login` | Valida credenciais, emite JWT + refresh |
| `POST` | `/refresh` | Valida refresh, revoga o antigo, emite novos tokens |
| `POST` | `/logout` | Revoga refresh token; audita logout se houver JWT |
| `POST` | `/register` | Cria usuário com BCrypt e perfil `USUARIO_FINAL` (aberto para testes) |

### Exemplo de resposta de login

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "uuid-uuid...",
  "tokenType": "Bearer"
}
```

### Uso do access token

```http
Authorization: Bearer <accessToken>
```

---

## 6. `PasswordEncoder`

Bean `BCryptPasswordEncoder` em `SecurityConfig` — senhas nunca persistidas em texto puro.

---

## 7. `UsuarioDetailsService` + `UsuarioAutenticado`

- Busca usuário por e-mail (ignorando maiúsculas), não deletado.
- Exige `ativo = true`.
- Carrega perfis de `usuario_perfis` e converte em `SimpleGrantedAuthority` com o **nome do perfil** (`ADMIN_SALA`, etc.).
- `UsuarioAutenticado` expõe `usuarioId` para auditoria e regras de negócio.

---

## 8. `JwtService`

- Gera access token com `sub` = e-mail e claim `roles`.
- Valida assinatura e expiração.
- Extrai e-mail e roles.
- Chave e expiração vêm de `SecurityProperties` / `application.properties`.

---

## 9. `RefreshTokenService`

- Gera token aleatório (dois UUIDs concatenados).
- Persiste apenas **SHA-256** do token (`TokenHashUtil`), nunca o valor bruto.
- Remove tokens anteriores do usuário ao emitir novo.
- Valida expiração e revogação.
- Logout marca `revogadoEm`.

---

## 10. `AuthService`

Fluxos:

1. **Login:** BCrypt → JWT + refresh → auditoria `LOGIN`.
2. **Refresh:** valida hash → revoga → novos tokens.
3. **Logout:** revoga refresh → auditoria `LOGOUT` (se autenticado).
4. **Register:** novo usuário + vínculo `USUARIO_FINAL` → auditoria `REGISTER`.

Exceção `CredenciaisInvalidasException` → HTTP **401** via `GlobalExceptionHandler`.

---

## 11. `JwtAuthenticationFilter`

- Lê `Authorization: Bearer ...`
- Valida JWT, carrega `UserDetails`, preenche `SecurityContext`.
- Executa antes de `UsernamePasswordAuthenticationFilter`.

---

## 12. `SecurityConfig` — rotas públicas e RBAC

### Públicas (sem token)

- `/api/v1/auth/**`
- `/swagger-ui/**`, `/v3/api-docs/**`, `/api/docs/**`
- `/api/v1/health`

### Demais rotas

- Exigem autenticação (`anyRequest().authenticated()`), salvo regras específicas abaixo.

### RBAC por perfil

| Perfil | Acesso |
|--------|--------|
| **ADMIN_SALA** | `/api/v1/salas/**`, `/api/v1/posicoes/**`, `/api/v1/tipos-equipamento/**`, `/api/v1/layouts/**` |
| **GESTOR_RESERVAS** | `/api/v1/reservas/**`, `/api/v1/relatorios/**`, `/api/v1/notificacoes/**` |
| **USUARIO_FINAL** | `POST /api/v1/reservas`, `GET /api/v1/salas/{id}/disponibilidade`, `DELETE /api/v1/reservas/{id}` |
| **INTEGRADOR** | `GET .../disponibilidade`, `POST /api/v1/reservas` |

Regras mais específicas (método + path) são avaliadas **antes** das regras amplas de módulo.

Constantes de perfil: `security/Roles.java`.

---

## 13. Respostas JSON de erro de segurança

| Componente | HTTP | Quando |
|------------|------|--------|
| `JsonAuthenticationEntryPoint` | 401 | Sem token ou token inválido |
| `JsonAccessDeniedHandler` | 403 | Token válido, sem permissão |
| `LoginRateLimitFilter` | 429 | Muitas tentativas em `/api/v1/auth/login` |

Formato alinhado ao `GlobalExceptionHandler`:

```json
{
  "timestamp": "2026-06-04T12:00:00-03:00",
  "status": 401,
  "mensagem": "..."
}
```

---

## 14. Rate limiting

- `LoginRateLimitFilter`: limite por **IP** em `POST /api/v1/auth/login`.
- Configurável via `app.security.rate-limit.login-per-minute`.
- Implementação em memória (adequada para dev/single instance; em cluster, evoluir para Redis).

**Roadmap sugerido:** limite por token em APIs sensíveis e bucket distribuído.

---

## 15. Auditoria — `AuditService`

Registra em `audit_log` (IP, User-Agent, usuário, ação, entidade, id):

| Ação | Onde |
|------|------|
| `LOGIN` / `LOGOUT` / `REGISTER` | `AuthService` |
| `CRIAR` / `ATUALIZAR` sala | `SalaService` |
| `CRIAR` / `ATUALIZAR` posição | `PosicaoService` |
| `CRIAR` / `CANCELAR` reserva | `ReservaService` |
| `CRIAR` regra disponibilidade | `DisponibilidadeService` |
| `ALTERAR_DISPONIBILIDADE` (exceção) | `DisponibilidadeService` |

---

## 16. Health check

- `GET /api/v1/health` — público, retorna `status: UP`.

---

## 17. Utilitários

- `SecurityUtils` — `getUsuarioIdAtual()`, `getEmailAtual()` a partir do contexto.
- `TokenHashUtil` — SHA-256 hex para refresh tokens.

---

## 18. Pré-requisitos no banco de dados

Os perfis devem existir na tabela `perfis` com estes nomes exatos:

- `ADMIN_SALA`
- `GESTOR_RESERVAS`
- `USUARIO_FINAL`
- `INTEGRADOR`

O **register** associa automaticamente `USUARIO_FINAL`. Demais perfis devem ser vinculados em `usuario_perfis` (administração futura).

---

## 19. Como testar (curl)

```bash
# Registro
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nome":"Teste","email":"teste@accenture.com","senha":"senha12345"}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"teste@accenture.com","senha":"senha12345"}'

# API protegida
curl http://localhost:8080/api/v1/salas \
  -H "Authorization: Bearer <accessToken>"
```

---

## 20. Estrutura de pacotes criada/alterada

```
config/
  SecurityConfig.java
  SecurityProperties.java
security/
  Roles.java
  JwtService.java
  JwtAuthenticationFilter.java
  UsuarioDetailsService.java
  UsuarioAutenticado.java
  SecurityUtils.java
  TokenHashUtil.java
  JsonAuthenticationEntryPoint.java
  JsonAccessDeniedHandler.java
  LoginRateLimitFilter.java
controller/
  AuthController.java
  HealthController.java
service/
  AuthService.java
  RefreshTokenService.java
  AuditService.java
dto/request|response/
  LoginRequest, RefreshTokenRequest, CriarUsuarioRequest
  LoginResponse, UsuarioResponse
exception/
  CredenciaisInvalidasException.java
```

---

## 21. Próximos passos recomendados

1. Restringir `/api/v1/auth/register` a `ADMIN_SALA` em produção.
2. Substituir header `X-Usuario-Id` pelo `usuarioId` do JWT nos controllers.
3. HTTPS terminado no gateway/load balancer.
4. Rate limiting distribuído (Redis) e por token.
5. Rotação de `app.security.jwt.secret` com estratégia de migração de tokens.

---

*Documento gerado após implementação da camada de segurança funcional do OfficeHub v1.*
