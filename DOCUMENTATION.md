# NexusMarket — Documentación técnica

> Documento extendido de apoyo. El diseño formal está en [`SDD/`](SDD/README.md)
> y la guía de ejecución en [`SETUP.md`](SETUP.md).

## 1. Introducción

NexusMarket es el backend de un marketplace multiproveedor. Expone una API REST
para gestionar autenticación y sesiones, usuarios y roles, perfiles de
compradores y vendedores, catálogo de productos y categorías, bodegas e
inventario, órdenes, facturación, reembolsos, envíos, devoluciones y auditoría.

Es un proyecto académico de Construcción de Software II (Tecnológico de
Antioquia). No está desplegado en producción. Las limitaciones y los aspectos
no verificados se enumeran al final.

## 2. Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 4.0.0 / Spring Framework 7 |
| Web | Spring MVC |
| Seguridad | Spring Security 7, JWT HMAC-SHA256 (JJWT 0.12.6), BCrypt |
| JSON | Jackson 3 (`tools.jackson.*`) |
| Persistencia relacional | Spring Data JPA / Hibernate + MySQL 8 |
| Persistencia documental | Spring Data MongoDB 7 (auditoría y sesiones) |
| Validación | Jakarta Bean Validation |
| Pruebas | JUnit 5, Mockito, AssertJ, Spring Test |
| Build | Maven 3.9+ con wrapper |
| Contenedores | Docker multi-stage + Docker Compose |
| CI | GitHub Actions (`./mvnw -B clean test`) |

## 3. Arquitectura

El proyecto aplica **Ports & Adapters**. Las dependencias apuntan hacia el
dominio:

```
adapters.rest ──► domain.ports.in ◄── adapters.useCases
                                          │
                                          ▼
                                  domain.services
                                          │
                                          ▼
                                 domain.ports.out
                                          ▲
                                          │
                            adapters.persistence.jpa / mongodb
```

Reglas respetadas:

- `domain/models`, `domain/valueobjects` y `domain/exceptions` no importan
  Spring, JPA, MongoDB ni HTTP.
- `domain/ports/in` tampoco importa DTOs REST: usa `RegisterCommand`,
  `LoginCommand` y `AuthenticationResult`.
- `domain/services` son POJO puros: no usan `@Service`, `@Transactional` ni
  `@Async`. Los beans se registran en las configuraciones especializadas
  `UserServiceConfig`, `CommerceServiceConfig`, `BillingServiceConfig` y
  `AuditServiceConfig`; las transacciones viven en las fachadas de
  `adapters/useCases`. La lógica de negocio no depende de controladores ni
  repositorios concretos.
- Los controladores delegan en puertos de entrada y traducen con mappers.
- Los adaptadores de persistencia implementan los puertos de salida.

### 3.1 Excepciones

`DomainException` es la raíz abstracta. Las 30 excepciones concretas
(`ResourceNotFoundException`, `BusinessRuleException`, `DuplicateResourceException`,
`InvalidStatusTransitionException`, `InsufficientStockException`,
`InvalidQuantityException`, `DamagedInventoryException`,
`InventoryNotInitializedException`, `ProductNotActiveException`,
`UserEmailAlreadyExistsException`, `ProductSkuAlreadyExistsException`,
`CategoryNameAlreadyExistsException`, `SellerTaxIdAlreadyExistsException`,
`WarehouseNameAlreadyExistsException`, etc.) heredan de ella y no llevan
anotaciones de Spring. `GlobalExceptionHandler` es el único punto que traduce a
HTTP:

| Excepción | HTTP |
|---|---|
| `ResourceNotFoundException`, `TrackingNotFoundException` | 404 |
| `DuplicateResourceException`, `UserAlreadyExistsException`, `UserEmailAlreadyExistsException`, `ProductSkuAlreadyExistsException`, `CategoryNameAlreadyExistsException`, `SellerTaxIdAlreadyExistsException`, `WarehouseNameAlreadyExistsException`, `InvalidStatusTransitionException`, `CategoryHasProductsException`, `ProductNotAvailableException`, `InvoiceAlreadyExistsException`, `ShipmentAlreadyExistsException`, `ReturnAlreadyProcessedException` | 409 |
| `BusinessRuleException`, `ProductNotActiveException`, `InvalidQuantityException`, `InsufficientStockException`, `DamagedInventoryException`, `InventoryNotInitializedException`, `InvoiceNotPayableException`, `InvoiceNotRefundableException`, `InvalidRefundAmountException`, `RefundAmountExceededException`, `RefundNotAllowedException`, `ReturnWindowExpiredException`, `ReturnNotAllowedException`, `WarehouseCapacityExceededException` | 422 |
| `PaymentGatewayException` | 502 |
| `DomainException` no mapeada | 400 |
| Cualquier otra excepción | 500 con mensaje genérico |

El cuerpo de error es uniforme (`ErrorResponse`): `status`, `error`, `message`,
`path`, `timestamp` y `details`.

## 4. Estructura del código

```
com.nexusmarket/
├── domain/
│   ├── models/          # 15 modelos (incluye SessionEvent)
│   ├── valueobjects/    # 11 enums (UserRole, OrderStatus, RefundStatus...)
│   ├── exceptions/      # DomainException + 24 subtipos
│   ├── ports/in/        # 16 puertos de entrada + modelos de comando
│   ├── ports/out/       # 14 puertos de persistencia
│   └── services/        # 28 servicios con responsabilidad única
├── adapters/
│   ├── rest/            # 19 controladores, DTOs, mappers, handler global
│   ├── useCases/        # 16 implementaciones de puertos de entrada
│   └── persistence/
│       ├── jpa/         # 12 adaptadores, 13 entidades, 13 repos, 13 mappers
│       └── mongodb/     # auditoría de operaciones y eventos de sesión
└── infrastructure/
    ├── config/          # CORS, Jackson, async
    ├── notification/    # NotificationPort + NoOpNotificationAdapter
    └── security/        # JwtService, filtro JWT, SecurityConfig, 401/403
```

### 4.1 Servicios de dominio (SRP)

El antiguo `BuyerDomainService` se dividió en:

- `CreateBuyerService`: valida rol y duplicados, crea el comprador.
- `AddBuyerAddressService`: añade una dirección adicional.
- `ChangeBuyerCommercialStatusService`: cambia el estado comercial.

Otros servicios siguen el mismo criterio, por ejemplo `OrderCreateService`,
`OrderCalculationService`, `OrderLifecycleService`, `InvoiceGenerateService`,
`InvoiceVoidService`, `RefundApproveService`, `RefundProcessService`,
`ShipmentCreateService`, `ShipmentStatusService`, `UserCreationService`,
`UserQueryService`, `UserStatusService`, `SessionInvalidationService`,
`PasswordChangeService`, `SessionAuditService` y `RegisterAuditLogService`.

La división de `UserManagementService` y la matriz completa de hallazgos están
en [`SDD/03-dominio/matriz-srp.md`](SDD/03-dominio/matriz-srp.md).

## 5. Flujo de una petición

Ejemplo: `POST /api/v1/seller/products`.

1. `JwtAuthenticationFilter` valida el token y carga `UserDetailsAdapter`.
2. `SecurityConfig` exige rol `SELLER` para `/api/v1/seller/**` y
   `@PreAuthorize` lo confirma a nivel de método.
3. `ProductController` resuelve el vendedor desde el `userId` del token
   mediante `SellerUseCasePort.findByUserId`, no desde el cuerpo ni desde un
   repositorio.
4. El controller llama a `ProductUseCasePort.createProduct(...)`.
5. `ProductUseCaseImpl` delega en `ProductDomainService`, que valida precio,
   SKU, categoría y vendedor activo.
6. `ProductJpaAdapter` guarda la entidad y devuelve el modelo de dominio.
7. `ProductRestMapper` construye el `ProductResponseDTO`; si algo falla,
   `GlobalExceptionHandler` produce el `ErrorResponse` correspondiente.

## 6. Módulos y puertos

| Módulo | Puertos de entrada | Puertos de salida | Servicios principales | Controladores |
|---|---|---|---|---|
| Auth | `AuthenticationUseCasePort` | `UserRepositoryPort`, `SessionAuditPort` | `PasswordChangeService`, `SessionAuditService` | `AuthController` |
| Users | `UserUseCasePort` | `UserRepositoryPort` | `UserCreationService`, `UserQueryService`, `UserStatusService`, `SessionInvalidationService` | `UserController` |
| Buyers | `BuyerUseCasePort` | `BuyerRepositoryPort`, `UserRepositoryPort` | `CreateBuyerService`, `AddBuyerAddressService`, `ChangeBuyerCommercialStatusService` | `BuyerController`, `BuyerManagementController` |
| Sellers | `SellerUseCasePort` | `SellerRepositoryPort`, `UserRepositoryPort` | `SellerDomainService` | `SellerController` |
| Catalog | `ProductUseCasePort`, `CategoryUseCasePort`, `CatalogUseCasePort` | `ProductRepositoryPort`, `CategoryRepositoryPort` | `ProductDomainService`, `CategoryDomainService` | `ProductController`, `CategoryController`, `CatalogController` |
| Inventory | `InventoryUseCasePort`, `WarehouseUseCasePort` | `InventoryRepositoryPort`, `WarehouseRepositoryPort` | `InventoryManagementService`, `InventoryConsultService`, `WarehouseDomainService` | `InventoryController`, `WarehouseController` |
| Orders | `OrderUseCasePort` | `OrderRepositoryPort`, `BuyerRepositoryPort` | `OrderCreateService`, `OrderCalculationService`, `OrderLifecycleService` | `OrderController`, `WarehouseOrderController` |
| Billing | `InvoiceUseCasePort`, `RefundUseCasePort` | `InvoiceRepositoryPort`, `RefundRepositoryPort` | `InvoiceGenerateService`, `InvoiceVoidService`, `RefundApproveService`, `RefundProcessService` | `InvoiceController`, `RefundController` |
| Logistics | `ShipmentUseCasePort`, `ReturnUseCasePort` | `ShipmentRepositoryPort`, `ReturnRepositoryPort` | `ShipmentCreateService`, `ShipmentStatusService`, `ReturnProcessService` | `ShipmentController`, `ReturnController`, `ReturnManagementController` |
| Audit | `AuditUseCasePort`, `SessionEventQueryUseCasePort` | `AuditLogRepositoryPort`, `SessionAuditPort` | `ConsultAuditLogsService`, `RegisterAuditLogService`, `SessionAuditService` | `AuditController`, `SessionEventController` |

## 7. API REST

El contrato detallado está en
[`SDD/04-api/contratos-por-endpoint.md`](SDD/04-api/contratos-por-endpoint.md).
Resumen por prefijo:

| Prefijo | Rol | Módulos |
|---|---|---|
| `/api/v1/auth/**` | Público / autenticado | Registro, login, logout, cambio de contraseña |
| `/api/v1/admin/**` | `ADMIN` (excepto `/users/me`) | Usuarios, compradores (estado comercial), categorías, auditoría, sesiones |
| `/api/v1/buyer/**` | `BUYER` | Perfil, órdenes, devoluciones |
| `/api/v1/seller/**` | `SELLER` | Perfil, productos |
| `/api/v1/supervisor/**` | `SUPERVISOR` | Facturas, reembolsos, devoluciones |
| `/api/v1/warehouse/**` | `WAREHOUSE_OPERATOR` | Bodegas, inventario, envíos, transiciones de orden |
| `/api/v1/catalog/**` | Autenticado | Catálogo y búsqueda |

### 7.1 Cambios de contrato respecto a versiones anteriores

- `POST /api/v1/buyer/orders` ya no recibe `buyerId` ni cuerpo.
- `POST /api/v1/seller/products` ya no recibe `sellerId`.
- `POST /api/v1/buyer/buyers` y `POST /api/v1/seller/sellers` ya no reciben
  `userId`.
- `PATCH /api/v1/buyer/returns/{id}/approve|reject|complete` se movieron a
  `PATCH /api/v1/supervisor/returns/{id}/...`.
- `PATCH /api/v1/buyer/orders/{id}/dispatch|deliver|finish` se movieron a
  `PATCH /api/v1/warehouse/orders/{id}/...` (rol `WAREHOUSE_OPERATOR`).
- `PATCH /api/v1/buyer/buyers/{id}/status` se movió a
  `PATCH /api/v1/admin/buyers/{id}/status` (rol `ADMIN`).
- Los listados de compradores, vendedores, órdenes y devoluciones devuelven
  solo los recursos del usuario autenticado.

## 8. Seguridad

- JWT stateless firmado explícitamente con HMAC-SHA256; `jti` se audita y
  `tokenVersion` permite invalidar sesiones.
- `JWT_SECRET` es obligatorio y se valida al arrancar (mínimo 32 bytes); no hay
  fallback en el código. La firma, la expiración y la versión del token se
  verifican en cada petición autenticada.
- Al bloquear una cuenta se incrementa `tokenVersion` y el filtro JWT rechaza
  usuarios no habilitados; un token emitido antes del bloqueo deja de servir.
- Contraseñas con BCrypt y política común `PasswordPolicy` (8-72 caracteres, letra y número) en alta y cambio.
- Autorización por ruta (`SecurityConfig`) **y** por método
  (`@EnableMethodSecurity` + `@PreAuthorize`).
- Verificación de propiedad del recurso en compradores y vendedores: el
  identificador se obtiene del token, no del cuerpo ni de un path arbitrario. En productos, las lecturas por ID y por `sellerId` también verifican propiedad.
- Handlers REST para 401 (`RestAuthenticationEntryPoint`) y 403
  (`RestAccessDeniedHandler`) con el mismo formato `ErrorResponse`.
- Auditoría de login, logout, login fallido y cambio de contraseña en MongoDB.
- El perfil `prod` exige secretos por entorno, mantiene `show-sql=false` y
  desactiva Swagger/OpenAPI.

Riesgos y pendientes:

- El historial público de Git contiene credenciales antiguas de MongoDB. Deben
  rotarse en el proveedor y el historial debe sanearse antes de publicar; ver
  `SECURITY.md`. La reescritura no se ejecutó en esta misión.
- CORS está limitado a localhost y se integra en la cadena de Security; debe externalizarse y restringirse por entorno.
- No hay rotación automática de secretos ni revocación global más allá de
  `tokenVersion` y del estado de la cuenta.

## 9. Persistencia

### MySQL (JPA)

- Entidades con `@Entity`, relaciones `@ManyToOne`/`@OneToMany` y mappers
  bidireccionales hacia el dominio.
- `spring.jpa.open-in-view=false`.
- `spring.jpa.hibernate.ddl-auto=update`: Hibernate gobierna el esquema.
- Existe `src/main/resources/db/migration/V1__add_token_version.sql`, pero
  **Flyway no está habilitado**; el script no se ejecuta.

### MongoDB

- `AuditLogDocument` y `SessionEventDocument`.
- Auditoría de operaciones y eventos de sesión con TTL de 90 días.
- Si Mongo falla, `SessionAuditService` registra el error y no interrumpe la
  operación principal (consistencia eventual, no transaccional).

### Consistencia

No hay transacciones distribuidas ni patrón outbox. Una operación que escribe en
MySQL y MongoDB puede dejar la auditoría incompleta si falla el segundo paso.

## 10. Pruebas

```powershell
.\mvnw.cmd clean test
```

- **233 pruebas**, 0 fallos, 0 errores, 0 omitidas.
- Unitarias con Mockito para servicios y casos de uso.
- `@WebMvcTest` para controladores, seguridad, roles y propiedad de recursos.
- Transiciones inválidas de devolución, reembolso y envío (estados terminales).
- Pruebas del contrato 400/401/403/404/409/422/500 del `GlobalExceptionHandler` y de la jerarquía
  `DomainException`.
- No hay pruebas E2E contra MySQL/MongoDB ni contra el stack Docker.

## 11. Despliegue

- `Dockerfile` multi-stage (build con Maven/JDK 17, runtime JRE 17, usuario no
  root).
- `docker-compose.yml` con MySQL 8, MongoDB 7 y la app; healthchecks y
  `depends_on: condition: service_healthy`. Compose exige
  `MYSQL_ROOT_PASSWORD` y `JWT_SECRET`.
- `.env.example` documenta las variables con valores ficticios. `.env` y los
  perfiles locales están ignorados.
- Maven excluye `application-local*` y `.env*` del JAR; `.dockerignore` evita
  que entren en el contexto de construcción.
- CI en `.github/workflows/ci.yml` compila y ejecuta las pruebas con JDK 17.

## 12. Limitaciones conocidas

1. En desarrollo, Swagger UI está expuesto de forma pública en `/swagger-ui.html`
   y el contrato generado por `springdoc-openapi` en `/v3/api-docs`; no exigen
   JWT. El perfil `prod` desactiva ambos endpoints.
2. Flyway no está habilitado; `ddl-auto=update` controla el esquema.
3. Sin transacciones distribuidas entre MySQL y MongoDB.
4. Notificaciones implementadas solo como `NoOpNotificationAdapter`.
5. Sin pruebas de integración reales ni E2E.
6. Los listados globales de compradores y vendedores deberían moverse a rutas
   administrativas; hoy devuelven solo el perfil propio.
7. `GET /api/v1/seller/products` sigue exponiendo productos de todos los
   vendedores; conviene moverlo al catálogo o a una ruta administrativa.

## 13. Estado del repositorio

- Rama principal: `main`.
- Compilación: `BUILD SUCCESS` con `release 17`; la suite local se ejecutó con JDK 21 (Temurin). El CI declara JDK 17.
- Pruebas: 233 en verde.
- Docker Compose: configuración validada con `docker compose config`.
- No se ha validado un despliegue productivo.
