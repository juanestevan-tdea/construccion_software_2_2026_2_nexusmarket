# Seguridad y autorización

## Autenticación

- **JWT stateless** firmado explícitamente con HMAC-SHA256 (`JwtService`,
  JJWT 0.12.6). `JWT_SECRET` es obligatorio y debe tener al menos 32 bytes; no
  existe fallback en el código.
- Expiración: `jwt.expiration` por defecto 86400000 ms (24 h), ajustable con
  `JWT_EXPIRATION`.
- Claims relevantes: `sub` (email), `jti`, `roles` y `ver` (token version).
- Contraseñas con BCrypt y una política común (`PasswordPolicy`) de 8 a 72 caracteres, al menos una letra y un número, aplicada en alta, alta administrativa y cambio.
- `DaoAuthenticationProvider` construido con `CustomUserDetailsService`
  (requisito de Spring Security 7).

Flujo:

```text
POST /api/v1/auth/login
  → AuthenticationManager (DaoAuthenticationProvider)
  → BCrypt compara la contraseña
  → JwtService genera token (jti + tokenVersion)
  → SessionAuditService registra LOGIN en MongoDB (IP/User-Agent los aporta el adaptador)
  → AuthResponseDTO { token, expiresIn, role, ... }

Petición protegida
  → JwtAuthenticationFilter lee Authorization: Bearer
  → HttpRequestMetadataExtractor obtiene IP/User-Agent
  → valida firma, expiración y tokenVersion
  → comprueba que la cuenta esté habilitada
  → carga UserDetailsAdapter en SecurityContext
  → SecurityConfig / @PreAuthorize deciden

El `jti` se incluye para auditoría; no se usa como lista de revocación.
```

## Autorización en dos niveles

### 1. Reglas de ruta (`SecurityConfig`)

| Ruta | Acceso |
|---|---|
| `/api/v1/auth/register`, `/api/v1/auth/login` | `permitAll` |
| `/actuator/health` | `permitAll` |
| `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html` | `permitAll` en desarrollo; `denyAll` con perfil `prod` |
| `/api/v1/admin/users/me` | Autenticado |
| `/api/v1/admin/**` | `ADMIN` |
| `/api/v1/buyer/**` | `BUYER` |
| `/api/v1/seller/**` | `SELLER` |
| `/api/v1/supervisor/**` | `SUPERVISOR` |
| `/api/v1/warehouse/**` | `WAREHOUSE_OPERATOR` |
| `/api/v1/catalog/**` | Autenticado |
| Cualquier otra | Autenticado |

Sesiones HTTP deshabilitadas (`SessionCreationPolicy.STATELESS`), CSRF
deshabilitado por ser API stateless, CORS se integra en SecurityConfig con `.cors(...)` y `CorsConfig`; el preflight se resuelve antes de la autorización JWT.
`SecurityConfig` consulta el perfil activo: con `prod`, las rutas de Swagger se
deniegan explícitamente; además `application-prod.yml` desactiva springdoc.

### 2. Reglas de método (`@EnableMethodSecurity` + `@PreAuthorize`)

Antes de la auditoría, las anotaciones `@PreAuthorize` existían pero **no
tenían efecto** porque faltaba `@EnableMethodSecurity`. Ahora están activas.
Se añadió `@PreAuthorize("isAuthenticated()")` a
`GET /api/v1/admin/users/me`, que prevalece sobre el `hasRole('ADMIN')` de
clase.

## Propiedad del recurso

Los roles por sí solos no impiden que un `BUYER` lea la orden de otro `BUYER`.
Por eso los controladores resuelven el perfil desde el `userId` del token
delegando en los puertos de entrada (`BuyerUseCasePort`,
`SellerUseCasePort`, `OrderUseCasePort`) y comparan:

| Controlador | Verificación |
|---|---|
| `ProductController` | `seller.id == product.sellerId` antes de `GET` por ID, `GET` por `sellerId`, `PATCH` y `DELETE`; la creación usa el vendedor del token |
| `OrderController` | `order.buyerId == buyer.id` en lectura y transiciones; listados filtrados por comprador |
| `WarehouseOrderController` | transiciones logísticas reservadas a `WAREHOUSE_OPERATOR` |
| `ReturnController` | la orden de la devolución pertenece al comprador autenticado |
| `BuyerController` | el `id` del path debe ser el comprador del token |
| `BuyerManagementController` | cambio de estado comercial reservado a `ADMIN` |
| `SellerController` | el `id`/`taxId` debe ser el vendedor del token |

Violación → `AccessDeniedException` → `403` con `ErrorResponse`.

## Identidad fuera del cuerpo

Se eliminaron de los DTO de creación:

- `ProductCreateRequestDTO.sellerId`
- `OrderCreateRequestDTO.buyerId` (se eliminó el DTO y el cuerpo)
- `BuyerCreateRequestDTO.userId`
- `SellerCreateRequestDTO.userId`

Aceptarlos permitía suplantar a otro usuario enviando su identificador.

## Invalidación de sesiones

- `jti` identifica cada token emitido y se usa para auditoría.
- `tokenVersion` en `User` se incrementa al cambiar contraseña, cerrar sesión o
  bloquear la cuenta; los tokens con versión anterior se rechazan.
- El filtro JWT también rechaza usuarios con `isEnabled() == false`, por lo que
  un bloqueo invalida de inmediato los tokens ya emitidos.
- `SessionAuditService` registra `LOGIN`, `LOGIN_FAILED`, `LOGOUT`,
  `PASSWORD_CHANGED` y tokens rechazados en MongoDB. Un fallo de Mongo se
  registra como error pero no interrumpe la operación principal. El servicio no
  conoce `HttpServletRequest`.
- `GET /api/v1/admin/session-events` consulta los eventos mediante
  `SessionEventQueryUseCasePort` y `ConsultSessionEventsService`; el controlador
  solo traduce filtros y paginación, sin acceder a repositorios Mongo.

## Handlers 401/403

`RestAuthenticationEntryPoint` y `RestAccessDeniedHandler` devuelven el mismo
`ErrorResponse` que el resto de la API, en lugar del HTML por defecto de
Spring Security.

## Riesgos y pendientes

1. El historial público de Git contiene credenciales antiguas de MongoDB. Deben
   rotarse en el proveedor y el historial debe sanearse antes de publicar; ver
   `SECURITY.md`. La reescritura no se ejecutó en esta misión.
2. CORS está limitado a orígenes `localhost` para desarrollo, pero sigue siendo una lista fija; debe externalizarse y restringirse por entorno antes de producción.
3. No hay rotación automática de secretos ni bloqueo por intentos fallidos.
4. No se valida el `aud`/`iss` del JWT porque no se emiten.
