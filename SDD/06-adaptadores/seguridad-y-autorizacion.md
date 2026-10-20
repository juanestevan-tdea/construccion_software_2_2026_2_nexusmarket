# Seguridad y autorización

## Autenticación

- **JWT stateless** firmado con HMAC-SHA256 (`JwtService`, JJWT 0.12.6).
- Expiración: `jwt.expiration=86400000` ms (24 h).
- Claims relevantes: `sub` (email), `jti`, `tokenVersion`, rol.
- Contraseñas con `BCryptPasswordEncoder`.
- `DaoAuthenticationProvider` construido con `CustomUserDetailsService`
  (requisito de Spring Security 7).

Flujo:

```text
POST /api/v1/auth/login
  → AuthenticationManager (DaoAuthenticationProvider)
  → BCrypt compara la contraseña
  → JwtService genera token (jti + tokenVersion)
  → SessionAuditService registra LOGIN en MongoDB
  → AuthResponseDTO { token, expiresIn, role, ... }

Petición protegida
  → JwtAuthenticationFilter lee Authorization: Bearer
  → valida firma, expiración, jti y tokenVersion
  → carga UserDetailsAdapter en SecurityContext
  → SecurityConfig / @PreAuthorize deciden
```

## Autorización en dos niveles

### 1. Reglas de ruta (`SecurityConfig`)

| Ruta | Acceso |
|---|---|
| `/api/v1/auth/register`, `/api/v1/auth/login` | `permitAll` |
| `/actuator/health` | `permitAll` |
| `/v3/api-docs/**`, `/swagger-ui/**` | `permitAll` (sin springdoc instalado) |
| `/api/v1/admin/users/me` | Autenticado |
| `/api/v1/admin/**` | `ADMIN` |
| `/api/v1/buyer/**` | `BUYER` |
| `/api/v1/seller/**` | `SELLER` |
| `/api/v1/supervisor/**` | `SUPERVISOR` |
| `/api/v1/warehouse/**` | `WAREHOUSE_OPERATOR` |
| `/api/v1/catalog/**` | Autenticado |
| Cualquier otra | Autenticado |

Sesiones HTTP deshabilitadas (`SessionCreationPolicy.STATELESS`), CSRF
deshabilitado por ser API stateless, CORS configurado para desarrollo.

### 2. Reglas de método (`@EnableMethodSecurity` + `@PreAuthorize`)

Antes de la auditoría, las anotaciones `@PreAuthorize` existían pero **no
tenían efecto** porque faltaba `@EnableMethodSecurity`. Ahora están activas.
Se añadió `@PreAuthorize("isAuthenticated()")` a
`GET /api/v1/admin/users/me`, que prevalece sobre el `hasRole('ADMIN')` de
clase.

## Propiedad del recurso

Los roles por sí solos no impiden que un `BUYER` lea la orden de otro `BUYER`.
Por eso los controladores resuelven el perfil desde el `userId` del token y
comparan:

| Controlador | Verificación |
|---|---|
| `ProductController` | `seller.id == product.sellerId` antes de `PATCH`/`DELETE`; creación usa el vendedor del token |
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

- `jti` identifica cada token emitido.
- `tokenVersion` en `User` se incrementa al cambiar contraseña o cerrar sesión;
  los tokens con versión anterior se rechazan.
- `SessionAuditService` registra `LOGIN`, `LOGIN_FAILED`, `LOGOUT` y
  `PASSWORD_CHANGED` en MongoDB. Un fallo de Mongo se registra como error pero
  no interrumpe la operación principal.

## Handlers 401/403

`RestAuthenticationEntryPoint` y `RestAccessDeniedHandler` devuelven el mismo
`ErrorResponse` que el resto de la API, en lugar del HTML por defecto de
Spring Security.

## Riesgos y pendientes

1. `jwt.secret` tiene un fallback público en `application.properties`; en
   producción debe inyectarse `JWT_SECRET` (≥ 32 caracteres).
2. CORS es permisivo; restringir orígenes por entorno.
3. No hay rotación automática de secretos ni bloqueo por intentos fallidos.
4. No se valida el `aud`/`iss` del JWT porque no se emiten.
