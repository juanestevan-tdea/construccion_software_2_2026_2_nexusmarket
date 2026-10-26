# Matriz de trazabilidad

Trazabilidad **requisito → puerto de entrada → servicio/caso de uso →
controlador → prueba**. Las pruebas son unitarias o de controlador con Mockito;
no hay pruebas E2E contra bases reales.

| Módulo | Requisitos | Puerto de entrada | Servicios / casos de uso | Controlador(es) | Pruebas | Estado |
|---|---|---|---|---|---|---|
| Autenticación | RF-001–RF-005 | `AuthenticationUseCasePort` | `AuthenticationUseCaseImpl`, `PasswordChangeService`, `SessionAuditService` | `AuthController` | `AuthServiceTest` (16), `AuthControllerTest` (11), `SessionAuditServiceTest` (6), `PasswordChangeServiceTest` (4), `TokenInvalidationServiceTest` (5), `JwtAuthenticationFilterAuditTest` (2), `JwtAuthenticationFilterBlockedUserTest` (3) | Implementado y probado |
| Usuarios | RF-006-RF-009 | `UserUseCasePort` | `UserCreationService`, `UserQueryService`, `UserStatusService`, `SessionInvalidationService`, `CreateBuyerService`, `SellerDomainService` | `UserController` | `UserServiceTest` (10), `UserControllerTest` (17), `SrpArchitectureTest` (2) | Implementado y probado |
| Compradores | RF-006–RF-009 | `BuyerUseCasePort` | `CreateBuyerService`, `AddBuyerAddressService`, `ChangeBuyerCommercialStatusService` | `BuyerController`, `BuyerManagementController` | `BuyerControllerTest` (6), `BuyerManagementControllerTest` (3) | Implementado; propiedad y cambio de estado por ADMIN probados |
| Vendedores | RF-006–RF-009 | `SellerUseCasePort` | `SellerDomainService` | `SellerController` | `SellerServiceTest` (2), `SellerControllerTest` (5), `AuthServiceTest` (perfil vendedor) | Implementado; propiedad y `taxId` duplicado probados |
| Catálogo / productos | RF-010–RF-014 | `ProductUseCasePort` | `ProductDomainService` | `ProductController` | `ProductServiceTest` (4), `ProductControllerTest` (9) | Implementado; autorización por propiedad probada |
| Categorías | RF-015–RF-017 | `CategoryUseCasePort` | `CategoryDomainService` | `CategoryController` | `CategoryServiceTest` (7) | Implementado; ciclos y borrado con hijas probados |
| Catálogo público | RF-019 | `CatalogUseCasePort` | `CatalogUseCaseImpl` | `CatalogController` | `CatalogServiceTest` (3) | Implementado y probado |
| Órdenes | RF-023–RF-029 | `OrderUseCasePort` | `OrderCreateService`, `OrderCalculationService`, `OrderLifecycleService` | `OrderController`, `WarehouseOrderController` | `OrderServiceTest` (10), `OrderControllerTest` (8), `WarehouseOrderControllerTest` (4) | Implementado; aislamiento por comprador, transiciones por WAREHOUSE_OPERATOR y producto inactivo (422) probados |
| Inventario / bodegas | RF-018, RF-020–RF-022 | `InventoryUseCasePort`, `WarehouseUseCasePort` | `InventoryManagementService`, `InventoryConsultService`, `WarehouseDomainService` | `InventoryController`, `WarehouseController` | `InventoryServiceTest` (12), `WarehouseServiceTest` (4) | Implementado; reserva positiva, cantidades inválidas y cantidad no inicializada probadas |
| Facturación | RF-030–RF-032 | `InvoiceUseCasePort` | `InvoiceGenerateService`, `InvoiceVoidService` | `InvoiceController` | `InvoiceServiceTest` (6) | Implementado; anulación conforme a RN-010/RF-032 |
| Reembolsos | RF-033–RF-035 | `RefundUseCasePort` | `RefundApproveService`, `RefundProcessService` | `RefundController` | `RefundServiceTest` (12), `RefundControllerTest` (2) | Implementado; estados ya procesados, factura inactiva, monto inválido, monto acumulado y contrato HTTP 422 probados |
| Envíos | RF-036–RF-038 | `ShipmentUseCasePort` | `ShipmentCreateService`, `ShipmentStatusService` | `ShipmentController` | `ShipmentServiceTest` (5) | Implementado y probado |
| Devoluciones | RF-039–RF-041 | `ReturnUseCasePort` | `ReturnProcessService` | `ReturnController`, `ReturnManagementController` | `ReturnServiceTest` (9), `ReturnControllerTest` (3), `ReturnManagementControllerTest` (4) | Implementado; aprobación por SUPERVISOR y estados ya procesados probados |
| Auditoría | RNF-009–RNF-010 | `AuditUseCasePort`, `SessionEventQueryUseCasePort` | `ConsultAuditLogsService`, `RegisterAuditLogService`, `SessionAuditService` | `AuditController`, `SessionEventController` | `AuditServiceTest` (3), `SessionEventMongoAdapterTest` (6), `SessionEventQueryUseCaseImplTest` (3), `ConsultSessionEventsServiceTest` (1), `HttpRequestMetadataExtractorTest` (4) | Implementado y probado |
| Errores / contrato HTTP | RNF-003, RNF-011 | — | `GlobalExceptionHandler` | Todos | `GlobalExceptionHandlerTest` (10), `DomainExceptionContractTest` (3) | Implementado y probado |
| Seguridad / roles | RNF-001–RNF-002 | — | `SecurityConfig`, `JwtService`, `JwtAuthenticationFilter` | Todos | `UserControllerTest` (17), `AuthControllerTest` (11), `ProductControllerTest` (9), `OrderControllerTest` (8), `WarehouseOrderControllerTest` (4), `ReturnManagementControllerTest` (4), `BuyerControllerTest` (6), `SellerControllerTest` (5), `BuyerManagementControllerTest` (3), `RefundControllerTest` (2) | Implementado; roles y propiedad probados |

## Cobertura de requisitos no funcionales

| RNF | Evidencia | Estado |
|---|---|---|
| RNF-001 JWT HMAC-SHA256 | `JwtService`, `JwtAuthenticationFilter`, `AuthServiceTest`, `JwtAuthenticationFilterAuditTest` | Verificado en pruebas unitarias |
| RNF-002 RBAC + 401/403 | `SecurityConfig`, `@EnableMethodSecurity`, `RestAuthenticationEntryPoint`, `RestAccessDeniedHandler`, `UserControllerTest`, `WarehouseOrderControllerTest`, `ReturnManagementControllerTest`, `BuyerManagementControllerTest` | Verificado |
| RNF-003 Validación y errores | DTOs con Jakarta Validation, `GlobalExceptionHandlerTest` | Verificado |
| RNF-004 Rendimiento | Sin medición automatizada | **No verificado** |
| RNF-005 Disponibilidad / stateless | `SESSION_STATELESS`, Docker healthchecks | Configurado; sin prueba de carga |
| RNF-006 Transacciones ACID | `@Transactional` en las fachadas de `adapters/useCases` | Configurado; sin prueba de integración real |
| RNF-007 Arquitectura hexagonal | `DomainExceptionContractTest`, `SrpArchitectureTest`; puertos sin DTOs REST; controladores sin repositorios | Verificado en estructura; `domain/services` no usa anotaciones de Spring |
| RNF-008 Pruebas | 233 pruebas, 0 fallos | Verificado con `mvnw clean package` |
| RNF-009 Auditoría MongoDB | `SessionAuditService`, `ConsultSessionEventsService`, `AuditServiceTest`, `SessionEventMongoAdapterTest`, `SessionEventQueryUseCaseImplTest` | Verificado con mocks |
| RNF-010 Actuator | `spring-boot-starter-actuator`, `/actuator/health` | Configurado |
| RNF-011 Contrato HTTP | `ErrorResponse`, códigos semánticos | Verificado |

## Vacíos de trazabilidad

1. No hay pruebas de integración JPA/MongoDB ni Testcontainers.
2. No hay prueba E2E del flujo completo contra Docker Compose.
3. RNF-004 (rendimiento) no tiene medición.
4. `ProductController` mantiene listados globales de productos bajo la ruta del
   vendedor; se recomienda moverlos a catálogo o a una ruta administrativa.
