# Servicios de dominio y responsabilidades

Inventario de los 28 servicios de `domain/services` y su responsabilidad
principal. La columna **SRP** indica si el servicio tiene una única razón para
cambiar. La auditoría detallada, con dependencias, excepciones, hallazgos y
pruebas, está en [auditoria-servicios-excepciones.md](auditoria-servicios-excepciones.md).

| Servicio | Responsabilidad | SRP |
|---|---|---|
| `AddBuyerAddressService` | Añadir dirección adicional a un comprador | Sí |
| `ChangeBuyerCommercialStatusService` | Cambiar el estado comercial del comprador | Sí |
| `CreateBuyerService` | Validar rol/duplicados y crear comprador | Sí |
| `CategoryDomainService` | Alta, actualización y borrado de categorías; valida ciclos y subcategorías | Sí |
| `ConsultAuditLogsService` | Consultar auditoría por rango/fecha | Sí |
| `ConsultSessionEventsService` | Consultar eventos de sesión con filtros y paginación | Sí |
| `InventoryConsultService` | Consultas de inventario | Sí |
| `InventoryManagementService` | Reserva (solo cantidades positivas), confirmación, daño y ajuste de stock | Sí |
| `InvoiceGenerateService` | Generar factura de una orden | Sí |
| `InvoiceVoidService` | Anular factura | Sí |
| `OrderCalculationService` | Recalcular totales de la orden | Sí |
| `OrderCreateService` | Crear orden en estado `CART` | Sí |
| `OrderLifecycleService` | Transiciones de estado de la orden | Sí |
| `PasswordChangeService` | Verificar contraseña actual, aplicar política, cifrar y revocar sesiones | Sí |
| `ProductDomainService` | Validaciones y CRUD de productos | Sí |
| `RefundApproveService` | Aprobar/rechazar un reembolso `PENDING`; los estados ya procesados producen 409 | Sí |
| `RefundProcessService` | Crear reembolso validando factura activa/pagada, monto y devolución | Sí |
| `RegisterAuditLogService` | Registrar una operación auditada | Sí |
| `ReturnProcessService` | Ciclo de vida de devoluciones; las transiciones ya procesadas producen 409 | Sí |
| `SellerDomainService` | Alta, actualización y activación de vendedores | Sí |
| `SessionAuditService` | Registrar eventos de sesión (sin depender de HTTP) | Sí |
| `SessionInvalidationService` | Incrementar `tokenVersion` para revocar JWT emitidos | Sí |
| `ShipmentCreateService` | Crear envío | Sí |
| `ShipmentStatusService` | Cambiar el estado del envío respetando los estados terminales | Sí |
| `UserCreationService` | Validar duplicado, aplicar la política de contraseña, cifrar y crear usuario | Sí |
| `UserQueryService` | Consultas de usuarios y traducción de ausencia | Sí |
| `UserStatusService` | Bloquear, activar y cambiar rol | Sí |
| `WarehouseDomainService` | CRUD y validaciones de bodegas | Sí |

## Casos de uso

Los 16 casos de uso de `adapters/useCases` son fachadas de aplicación. No
contienen reglas de negocio propias: delegan escrituras en servicios de dominio y
resuelven consultas o composición de respuestas. La tabla completa está en la
auditoría enlazada.

| Caso de uso | Responsabilidad |
|---|---|
| `AuditUseCaseImpl` | Componer y consultar registros de auditoría |
| `AuthenticationUseCaseImpl` | Registro, login, logout y cambio de contraseña |
| `BuyerUseCaseImpl` | Creación, consulta y cambios de comprador |
| `CatalogUseCaseImpl` | Catálogo público y búsqueda de productos |
| `CategoryUseCaseImpl` | CRUD y consulta de categorías |
| `InventoryUseCaseImpl` | Gestión y consulta de inventario |
| `InvoiceUseCaseImpl` | Emisión, consulta y anulación de facturas |
| `OrderUseCaseImpl` | Creación, composición, ciclo de vida y consulta de órdenes |
| `ProductUseCaseImpl` | CRUD y consulta de productos |
| `RefundUseCaseImpl` | Creación, consulta y decisión de reembolsos |
| `ReturnUseCaseImpl` | Devoluciones y agregación por comprador |
| `SellerUseCaseImpl` | CRUD y consulta de vendedores |
| `SessionEventQueryUseCaseImpl` | Normalización y consulta de eventos de sesión |
| `ShipmentUseCaseImpl` | Creación, consulta y transición de envíos |
| `UserUseCaseImpl` | Administración de usuarios |
| `WarehouseUseCaseImpl` | CRUD y consulta de bodegas |

## Política de contraseñas compartida

`PasswordPolicy` (`domain/policies`) concentra la regla de 8 a 72 caracteres,
al menos una letra y un número. La usan `UserCreationService` (alta pública y
administrativa) y `PasswordChangeService`; los DTO aplican la misma regla en el
borde HTTP. Así el alta no puede crear contraseñas más débiles que el cambio.

## SRP aplicado: división de `UserManagementService`

`UserManagementService` agrupaba alta, consultas, estado, rol, invalidación de
sesiones y cambio de contraseña, con seis motivos de cambio distintos. Se
reemplazó por cinco servicios cohesionados:

| Nuevo servicio | Métodos extraídos |
|---|---|
| `UserCreationService` | `createUser` |
| `UserQueryService` | `getUserByIdOrThrow`, `getUserByEmailOrThrow`, `findAll`, `findByEmail`, `findById`, `findByRole` |
| `UserStatusService` | `blockUser`, `activateUser`, `changeRole` |
| `SessionInvalidationService` | `invalidateSessions` |
| `PasswordChangeService` | `changePassword` |

`UserUseCaseImpl` y `AuthenticationUseCaseImpl` delegan en estos servicios,
igual que `BuyerUseCaseImpl` delega en los tres servicios de comprador. El
bloqueo de usuario sigue incrementando `tokenVersion` para revocar JWT ya
emitidos.

## `SessionAuditService` y `ConsultSessionEventsService`

`SessionAuditService` mantiene una única responsabilidad de aplicación:
construir y persistir eventos de sesión. La extracción de IP y `User-Agent` se
movió a `HttpRequestMetadataExtractor` en `infrastructure/security`; el servicio
recibe cadenas y no importa `jakarta.servlet`.

La lectura se separó en `ConsultSessionEventsService`, que delega en
`SessionAuditPort.find`. La consulta administrativa se expone mediante
`SessionEventQueryUseCasePort`, de modo que el controlador no conoce MongoDB ni
la lógica de paginación.

## Anulación de facturas

`InvoiceVoidService` solo anula facturas **activas e impagas**. Una factura
pagada o ya inactiva produce `InvoiceNotPayableException` (422), conforme a
RN-010 y RF-032. La decisión de reembolso pertenece a
`RefundProcessService`/`RefundApproveService` y no habilita la anulación. La
corrección está cubierta por `InvoiceServiceTest`.

Queda una **contradicción funcional pendiente**: `InvoiceGenerateService` crea
la factura con `paid = true` y no existe un endpoint para marcarla impaga. Por
tanto, el éxito de `voidInvoice` solo es alcanzable con datos heredados o
importados. No se cambió el flujo de generación porque requeriría definir el
pago de facturas y afectaría también a los reembolsos.

## `CategoryDomainService`

Además de validar nombre duplicado y existencia del padre, `updateCategory`
recorre la cadena de ancestros y rechaza ciclos con `BusinessRuleException`
(422). `deleteCategory` rechaza categorías con subcategorías y con productos;
las primeras producen 422 y las segundas `CategoryHasProductsException` (409).
La verificación se cubre en `CategoryServiceTest`.

## `RefundApproveService` y `ReturnProcessService`

`RefundApproveService` solo acepta reembolsos `PENDING`; aprobar o rechazar un
reembolso ya procesado lanza `InvalidStatusTransitionException` (409), alineado
con RF-035. `ReturnProcessService` solo acepta `REQUESTED` para aprobar o
rechazar y `APPROVED` para completar; una devolución ya procesada lanza
`ReturnAlreadyProcessedException` (409), alineado con RF-041. Completar una
devolución aún `REQUESTED` sigue siendo una regla de estado no permitida (422).
Ambos flujos están cubiertos por `RefundServiceTest` y `ReturnServiceTest`.

## `Inventory.reserve`

La reserva exige `amount > 0`. Sin esta validación, un `amount` negativo
aumentaba la cantidad y marcaba el inventario como `RESERVED`, corrompiendo el
stock. La regla vive en el modelo `Inventory` para que se aplique también a
invocaciones directas del servicio, no solo al controlador. Está cubierta por
`InventoryServiceTest`.

## `AuthenticationUseCaseImpl`

Implementa cuatro operaciones del puerto: `register`, `login`, `logout` y
`changePassword`. Es un **orquestador** de casos de uso, no un servicio de
dominio. Se mantiene como fachada cohesiva de autenticación; una separación en
`RegistrationService`, `LoginService`, `LogoutService` y `PasswordChangeService`
reduciría el acoplamiento con JWT, auditoría y perfiles, pero no se aplicó para
no fragmentar el contrato HTTP ni duplicar la coordinación de perfiles. Queda
como mejora recomendada, no como violación confirmada.

## Servicios con `@Service` y `@Transactional`

Los servicios usan anotaciones de Spring (`@Service`, `@Transactional`). Esto
facilita la inyección y las transacciones, pero acopla `domain/services` al
contenedor. La alternativa sería declarar los beans en
`infrastructure/config` y dejar los servicios como POJOs. Es una mejora
arquitectónica pendiente; no se aplicó para no reescribir la configuración de
todos los módulos.
