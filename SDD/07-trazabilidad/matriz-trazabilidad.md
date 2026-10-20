# Matriz de trazabilidad

Trazabilidad **requisito → puerto de entrada → servicio/caso de uso →
controlador → prueba**. Las pruebas son unitarias o de controlador con Mockito;
no hay pruebas E2E contra bases reales.

| Módulo | Requisitos | Puerto de entrada | Servicios / casos de uso | Controlador(es) | Pruebas | Estado |
|---|---|---|---|---|---|---|
| Autenticación | RF-001–RF-005 | `AuthenticationUseCasePort` | `AuthenticationUseCaseImpl`, `UserManagementService`, `SessionAuditService` | `AuthController` | `AuthServiceTest` (15), `AuthControllerTest` (11), `SessionAuditServiceTest` (5), `TokenInvalidationServiceTest` (5), `JwtAuthenticationFilterAuditTest` (2) | Implementado y probado |
| Usuarios | RF-006-RF-009 | `UserUseCasePort` | `UserManagementService`, `CreateBuyerService`, `SellerDomainService` | `UserController` | `UserServiceTest` (8), `UserControllerTest` (16) | Implementado y probado |
| Compradores | RF-006–RF-009 | `BuyerUseCasePort` | `CreateBuyerService`, `AddBuyerAddressService`, `ChangeBuyerCommercialStatusService` | `BuyerController`, `BuyerManagementController` | `BuyerControllerTest` (6), `BuyerManagementControllerTest` (3) | Implementado; propiedad y cambio de estado por ADMIN probados |
| Vendedores | RF-006–RF-009 | `SellerUseCasePort` | `SellerDomainService` | `SellerController` | `SellerControllerTest` (5), `AuthServiceTest` (perfil vendedor) | Implementado; propiedad probada |
| Catálogo / productos | RF-010–RF-014 | `ProductUseCasePort` | `ProductDomainService` | `ProductController` | `ProductServiceTest` (4), `ProductControllerTest` (5) | Implementado; autorización por propiedad probada |
| Categorías | RF-015–RF-019 | `CategoryUseCasePort` | `CategoryDomainService` | `CategoryController` | `CategoryServiceTest` (5) | Implementado y probado (servicio) |
| Catálogo público | RF-020–RF-022 | `CatalogUseCasePort` | `ProductDomainService` | `CatalogController` | `CatalogServiceTest` (3) | Implementado y probado |
| Órdenes | RF-023–RF-029 | `OrderUseCasePort` | `OrderCreateService`, `OrderCalculationService`, `OrderLifecycleService` | `OrderController`, `WarehouseOrderController` | `OrderServiceTest` (4), `OrderControllerTest` (6), `WarehouseOrderControllerTest` (4) | Implementado; aislamiento por comprador y transiciones por WAREHOUSE_OPERATOR probados |
| Inventario / bodegas | RF-030–RF-034 | `InventoryUseCasePort`, `WarehouseUseCasePort` | `InventoryManagementService`, `InventoryConsultService`, `WarehouseDomainService` | `InventoryController`, `WarehouseController` | `InventoryServiceTest` (6), `WarehouseServiceTest` (4) | Implementado y probado (servicio) |
| Facturación | RF-035–RF-037 | `InvoiceUseCasePort` | `InvoiceGenerateService`, `InvoiceVoidService` | `InvoiceController` | `InvoiceServiceTest` (4) | Implementado y probado |
| Reembolsos | RF-038–RF-040 | `RefundUseCasePort` | `RefundApproveService`, `RefundProcessService` | `RefundController` | `RefundServiceTest` (3) | Implementado y probado |
| Envíos | RF-041–RF-042 | `ShipmentUseCasePort` | `ShipmentCreateService`, `ShipmentStatusService` | `ShipmentController` | `ShipmentServiceTest` (3) | Implementado y probado |
| Devoluciones | RF-043 | `ReturnUseCasePort` | `ReturnProcessService` | `ReturnController`, `ReturnManagementController` | `ReturnServiceTest` (5), `ReturnManagementControllerTest` (4) | Implementado; aprobación movida a SUPERVISOR y probada |
| Auditoría | RNF-009–RNF-010 | `AuditUseCasePort` | `ConsultAuditLogsService`, `RegisterAuditLogService` | `AuditController`, `SessionEventController` | `AuditServiceTest` (3), `SessionEventMongoAdapterTest` (1) | Implementado y probado |
| Errores / contrato HTTP | RNF-003, RNF-011 | — | `GlobalExceptionHandler` | Todos | `GlobalExceptionHandlerTest` (6), `DomainExceptionContractTest` (3) | Implementado y probado |
| Seguridad / roles | RNF-001–RNF-002 | — | `SecurityConfig`, `JwtService`, `JwtAuthenticationFilter` | Todos | `UserControllerTest` (16), `AuthControllerTest` (11), `ProductControllerTest` (5), `OrderControllerTest` (6), `WarehouseOrderControllerTest` (4), `ReturnManagementControllerTest` (4), `BuyerControllerTest` (6), `SellerControllerTest` (5), `BuyerManagementControllerTest` (3) | Implementado; roles y propiedad probados |

## Cobertura de requisitos no funcionales

| RNF | Evidencia | Estado |
|---|---|---|
| RNF-001 JWT HMAC-SHA256 | `JwtService`, `JwtAuthenticationFilter`, `AuthServiceTest`, `JwtAuthenticationFilterAuditTest` | Verificado en pruebas unitarias |
| RNF-002 RBAC + 401/403 | `SecurityConfig`, `@EnableMethodSecurity`, `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`, `UserControllerTest`, `WarehouseOrderControllerTest`, `ReturnManagementControllerTest`, `BuyerManagementControllerTest` | Verificado |
| RNF-003 Validación y errores | DTOs con Jakarta Validation, `GlobalExceptionHandlerTest` | Verificado |
| RNF-004 Rendimiento | Sin medición automatizada | **No verificado** |
| RNF-005 Disponibilidad / stateless | `SESSION_STATELESS`, Docker healthchecks | Configurado; sin prueba de carga |
| RNF-006 Transacciones ACID | `@Transactional` en servicios | Configurado; sin prueba de integración real |
| RNF-007 Arquitectura hexagonal | `DomainExceptionContractTest`; puertos sin DTOs REST | Verificado en estructura; `domain/services` usa anotaciones de Spring |
| RNF-008 Pruebas | 149 pruebas, 0 fallos | Verificado con `mvnw clean test` |
| RNF-009 Auditoría MongoDB | `SessionAuditService`, `AuditServiceTest`, `SessionEventMongoAdapterTest` | Verificado con mocks |
| RNF-010 Actuator | `spring-boot-starter-actuator`, `/actuator/health` | Configurado |
| RNF-011 Contrato HTTP | `ErrorResponse`, códigos semánticos | Verificado |

## Vacíos de trazabilidad

1. No hay pruebas de integración JPA/MongoDB ni Testcontainers.
2. No hay prueba E2E del flujo completo contra Docker Compose.
3. RNF-004 (rendimiento) no tiene medición.
4. `ProductController` mantiene listados globales de productos bajo la ruta del
   vendedor; se recomienda moverlos a catálogo o a una ruta administrativa.
