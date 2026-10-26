# Matriz de hallazgos SRP

Auditoría de responsabilidad única sobre servicios de dominio, casos de uso,
controladores, adaptadores y excepciones. Clasificación:

- **Confirmado**: violación concreta de SRP o de la dirección de dependencias.
- **Potencial**: clase cohesionada hoy, pero con más de un motivo plausible de
  cambio.
- **Mejora recomendada**: deuda arquitectónica sin defecto funcional inmediato.

## Hallazgos confirmados y cambios aplicados

| Clase | Responsabilidad actual | Problema de cohesión | Cambio realizado | Responsabilidad final | Pruebas |
|---|---|---|---|---|---|
| `SessionAuditService` | Registraba eventos, extraía IP/User-Agent de `HttpServletRequest`, ejecutaba `@Async` y logueaba fallos; además consultaba | El dominio dependía de `jakarta.servlet`; mezclaba transporte HTTP con auditoría y lectura con escritura | Se extrajo `HttpRequestMetadataExtractor` a `infrastructure/security`; el servicio recibe `ipAddress` y `userAgent`; se añadió `SessionEvent` como modelo de dominio; la lectura se separó en `ConsultSessionEventsService` | Registrar eventos de sesión, sin conocer HTTP ni MongoDB | `SessionAuditServiceTest`, `ConsultSessionEventsServiceTest`, `HttpRequestMetadataExtractorTest`, `SrpArchitectureTest` |
| `UserManagementService` | Alta, consultas, bloqueo/activación, cambio de rol, invalidación de sesiones y cambio de contraseña | Seis motivos de cambio y dependencia de `PasswordEncoder` para operaciones heterogéneas | Se dividió en `UserCreationService`, `UserQueryService`, `UserStatusService`, `SessionInvalidationService` y `PasswordChangeService`; se eliminó la clase original | Cada servicio cubre una sola operación | `UserServiceTest`, `PasswordChangeServiceTest`, `TokenInvalidationServiceTest` |
| `ProductController` | Resolvía el vendedor autenticado con `SellerRepositoryPort` | Controlador REST accedía a un puerto de persistencia | Usa `SellerUseCasePort.findByUserId`; las lecturas por ID y por `sellerId` verifican propiedad | Adaptación HTTP y delegación al caso de uso | `ProductControllerTest` |
| `OrderController` | Resolvía el comprador con `BuyerRepositoryPort` | Controlador REST accedía a un puerto de persistencia | Usa `BuyerUseCasePort.findByUserId` | Adaptación HTTP y delegación al caso de uso | `OrderControllerTest` |
| `BuyerController` | Resolvía el comprador con `BuyerRepositoryPort` | Controlador REST accedía a un puerto de persistencia | Usa `BuyerUseCasePort.findByUserId` | Adaptación HTTP y delegación al caso de uso | `BuyerControllerTest` |
| `SellerController` | Resolvía el vendedor con `SellerRepositoryPort` | Controlador REST accedía a un puerto de persistencia | Usa `SellerUseCasePort.findByUserId` | Adaptación HTTP y delegación al caso de uso | `SellerControllerTest` |
| `ReturnController` | Inyectaba `BuyerRepositoryPort` y `OrderRepositoryPort`, resolvía comprador, verificaba propiedad y recorría órdenes para listar devoluciones | Controlador con persistencia y orquestación de aplicación | Usa `BuyerUseCasePort`, `OrderUseCasePort` y `ReturnUseCasePort.findAllByBuyerId`; el agregado de devoluciones se movió al caso de uso | Adaptación HTTP, control de acceso y mapeo de respuestas | `ReturnServiceTest`, `SrpArchitectureTest` |
| `SessionEventController` | Inyectaba `SessionEventMongoRepository`, usaba documentos y mapeaba páginas | Controlador acoplado a MongoDB y con lógica de consulta | Se añadió `SessionEventQueryUseCasePort`, `SessionEventQueryUseCaseImpl` y `SessionEventRestMapper`; el controlador solo traduce parámetros y DTOs | Adaptación HTTP de la consulta administrativa | `SessionEventQueryUseCaseImplTest`, `SessionEventMongoAdapterTest`, `SrpArchitectureTest` |
| `SessionAuditMongoAdapter` | Persistencia Mongo de eventos de sesión y traducción de consultas | Capturaba excepciones y registraba el mismo fallo que `SessionAuditService`; además traducía filtros opcionales con una consulta que podía convertirlos en igualdades `null` | Se retiró el manejo duplicado; la tolerancia queda en el servicio. La consulta usa métodos derivados por combinación de filtros | Adaptador de persistencia sin decisiones de tolerancia | `SessionEventMongoAdapterTest` |
| `InvoiceVoidService` | Anulación de facturas; dependía de `RefundRepositoryPort` para decidir si una factura pagada podía anularse | Mezclaba la regla de anulación con el estado de reembolsos y contradecía RN-010/RF-032 | Se retiró la dependencia de reembolsos: solo se anula una factura activa e impaga; se reutiliza `InvoiceNotPayableException` | Servicio de anulación cohesionado | `InvoiceServiceTest` |
| `RefundApproveService` | Aprobar o rechazar reembolsos | Usaba `RefundNotAllowedException` (422) para estados ya procesados, contradiciendo RF-035 | Los estados distintos de `PENDING` lanzan `InvalidStatusTransitionException` (409), con estado actual y destino | Servicio de decisión de reembolso alineado con el contrato | `RefundServiceTest` |
| `ReturnProcessService` | Aprobar, rechazar y completar devoluciones | Aprobaciones/rechazos sobre estados ya procesados devolvían 422 en lugar del 409 de RF-041 | Toda operación sobre una devolución no `REQUESTED` (salvo completar una `APPROVED`) lanza `ReturnAlreadyProcessedException` (409); completar una `REQUESTED` conserva 422 | Servicio de ciclo de vida de devolución alineado con RF-041 | `ReturnServiceTest` |
| `Inventory` / `InventoryManagementService` | Reserva y ajuste de stock | `reserve` aceptaba cantidades no positivas; un `amount` negativo incrementaba el stock y lo marcaba `RESERVED` | Se exige `amount > 0` (`InvalidQuantityException`), stock suficiente (`InsufficientStockException`), se rechaza inventario dañado (`DamagedInventoryException`), se distingue la cantidad no inicializada (`InventoryNotInitializedException`) y se rechazan cantidades nulas al crear inventario | Invariante de reserva cohesionada en el agregado | `InventoryServiceTest` |
| `CategoryDomainService` | Alta, actualización y borrado de categorías | No detectaba ciclos con descendientes ni bloqueaba el borrado de una categoría con hijas | Se recorre la cadena de ancestros y se valida `findByParentId` antes de eliminar | Integridad de jerarquía dentro del servicio de categorías | `CategoryServiceTest` |

## Hallazgos potenciales

| Clase | Responsabilidad actual | Problema de cohesión | Cambio realizado o recomendado | Responsabilidad final | Pruebas |
|---|---|---|---|---|---|
| `AuthenticationUseCaseImpl` | Orquesta registro, login, logout y cambio de contraseña; crea perfiles y audita | Cuatro operaciones de autenticación en una fachada; el registro incluye creación de perfil | **Sin cambio.** Se evaluó dividir en casos de uso por operación; se descartó para no fragmentar el contrato HTTP ni duplicar coordinación | Fachada cohesiva de autenticación | `AuthServiceTest`, `TokenInvalidationServiceTest` |
| `ProductDomainService` | CRUD de producto y validaciones contra vendedor, usuario, categoría e inventario | Concentra reglas de publicación y disponibilidad con cinco puertos de salida | **Sin cambio.** Si crecen las reglas de publicación, extraer `ProductPublicationPolicy` | Servicio de dominio de producto | `ProductServiceTest` |
| `OrderUseCaseImpl` | Fachada de creación, cálculo, ciclo de vida y consultas de órdenes | Mezcla comandos y consultas sobre el mismo agregado | **Sin cambio.** Separar lectura/escritura solo si el módulo de consultas crece | Caso de uso de órdenes | `OrderServiceTest` |
| `JwtAuthenticationFilter` | Extrae token, valida, audita rechazos y carga el `SecurityContext` | Varias tareas en el pipeline del filtro | **Sin cambio.** Es el punto natural de integración de Spring Security; la extracción HTTP ya se delegó | Filtro de autenticación JWT | `JwtAuthenticationFilterAuditTest`, `JwtAuthenticationFilterBlockedUserTest` |
| `GlobalExceptionHandler` | Traduce excepciones de dominio, framework y seguridad a HTTP | Clase extensa, pero con una sola responsabilidad: traducción centralizada | Se mantuvo; se sanearon mensajes de framework y se evitó registrar detalles SQL | Traductor centralizado de errores HTTP | `GlobalExceptionHandlerTest` |

## Excepciones de dominio

Todas heredan de `DomainException`; ninguna conoce HTTP. Cada tipo representa una
condición distinta y se traduce en `GlobalExceptionHandler`.

| Excepción | Condición | HTTP | Evaluación |
|---|---|---|---|
| `ResourceNotFoundException` | Recurso inexistente | 404 | Confirmada, sin cambio |
| `TrackingNotFoundException` | Código de seguimiento inexistente | 404 | Confirmada, sin cambio |
| `BusinessRuleException` | Regla de negocio genérica: estado, política de contraseñas o auditoría | 422 | Confirmada; ya no cubre cantidades, stock, inventario dañado o no inicializado, producto inactivo, duplicados de recurso ni elegibilidad de factura |
| `ProductNotActiveException` | Producto inactivo añadido a una orden | 422 | Nueva; subtipo de `BusinessRuleException` que expresa la regla concreta |
| `InvalidQuantityException` | Cantidad nula, cero o negativa en inventario o pedido | 422 | Nueva; reemplaza usos genéricos en `Inventory` y `OrderCalculationService` |
| `InsufficientStockException` | Stock disponible menor que la cantidad solicitada | 422 | Nueva; reemplaza el uso genérico en `Inventory.reserve` y `OrderCalculationService` |
| `DamagedInventoryException` | Operación sobre inventario en estado `DAMAGED` | 422 | Nueva; reemplaza el uso genérico en `Inventory.reserve` y `confirmPayment` |
| `InventoryNotInitializedException` | Operación sobre inventario con `quantity == null` | 422 | Nueva; reemplaza los dos usos genéricos en `Inventory.release` y `Inventory.adjust` |
| `DuplicateResourceException` | Recurso duplicado | 409 | Fallback genérico; los servicios usan subtipos específicos |
| `UserAlreadyExistsException` | Email de usuario ya registrado | 409 | Base de compatibilidad; el productor usa `UserEmailAlreadyExistsException` |
| `UserEmailAlreadyExistsException` | Email de usuario duplicado | 409 | Nueva; subtipo de `UserAlreadyExistsException` |
| `ProductSkuAlreadyExistsException` | SKU de producto duplicado | 409 | Nueva; subtipo de `DuplicateResourceException` |
| `CategoryNameAlreadyExistsException` | Nombre de categoría duplicado | 409 | Nueva; subtipo de `DuplicateResourceException` |
| `SellerTaxIdAlreadyExistsException` | `taxId` de vendedor duplicado | 409 | Nueva; subtipo de `DuplicateResourceException` |
| `WarehouseNameAlreadyExistsException` | Nombre de bodega duplicado | 409 | Nueva; subtipo de `DuplicateResourceException` |
| `InvalidStatusTransitionException` | Transición de estado inválida; también reembolso ya procesado | 409 | Confirmada; se amplió su uso a `RefundApproveService` conforme a RF-035 |
| `ProductNotAvailableException` | Producto no disponible | 409 | Confirmada, sin cambio |
| `CategoryHasProductsException` | Categoría con productos | 409 | Confirmada, sin cambio |
| `WarehouseCapacityExceededException` | Capacidad excedida | 422 | Confirmada, sin cambio |
| `InvoiceAlreadyExistsException` | Factura duplicada | 409 | Confirmada, sin cambio |
| `InvoiceNotPayableException` | Orden no facturable o factura no anulable | 422 | Confirmada, sin cambio |
| `InvoiceNotRefundableException` | Factura inactiva o impaga para reembolso | 422 | Nueva; separa la elegibilidad de la factura del resto de reglas de reembolso |
| `InvalidRefundAmountException` | Monto de reembolso nulo o no positivo | 422 | Nueva; reemplaza el uso genérico de `RefundNotAllowedException` |
| `RefundAmountExceededException` | Reembolso excede el monto | 422 | Confirmada, sin cambio |
| `RefundNotAllowedException` | Devolución asociada a otra orden | 422 | Confirmada; limitada a la integridad factura–devolución |
| `PaymentGatewayException` | Fallo del gateway | 502 | Confirmada, sin cambio |
| `ShipmentAlreadyExistsException` | Envío duplicado | 409 | Confirmada, sin cambio |
| `ReturnWindowExpiredException` | Plazo de devolución vencido | 422 | Confirmada, sin cambio |
| `ReturnNotAllowedException` | Devolución no permitida antes de estar aprobada (por ejemplo, completar una `REQUESTED`) | 422 | Confirmada; se limitó a la transición `REQUESTED → COMPLETED` |
| `ReturnAlreadyProcessedException` | Devolución ya procesada; aprobar/rechazar fuera de `REQUESTED` o completar un estado final | 409 | Confirmada; mensaje corregido y uso ampliado conforme a RF-041 |
| `DomainException` (sin mapeo) | Excepción de dominio nueva | 400 | Red de seguridad; añadir handler específico si se crea un tipo nuevo |

`InvoiceNotPayableException` se reutiliza tanto para la generación como para la
anulación de facturas. Su constructor de mensaje permite describir cada
condición sin crear una excepción nueva.

Con las trece excepciones granulares añadidas, `domain/exceptions` contiene
**31 clases**: `DomainException` (abstracta) y **30 excepciones concretas**.
`DomainExceptionContractTest` verifica por reflexión que todas heredan de la
base, no extienden `ResponseStatusException` y no usan anotaciones de Spring.

## Mejoras recomendadas

| Clase / aspecto | Responsabilidad actual | Problema de cohesión | Cambio recomendado | Responsabilidad final | Pruebas |
|---|---|---|---|---|---|
| Servicios de `domain/services` | POJO puros sin `@Service` ni `@Transactional` | Resuelto: el acoplamiento al contenedor se eliminó | Beans en `UserServiceConfig`, `CommerceServiceConfig`, `BillingServiceConfig` y `AuditServiceConfig`; transacciones en las fachadas de `adapters/useCases` | Dominio puro | `SrpArchitectureTest` (28 servicios) |
| Controladores con reglas de propiedad | Comparan `userId`/`sellerId`/`buyerId` | La autorización de recurso vive en el adaptador | Extraer un `ResourceOwnershipPolicy` si se repite en más controladores | Adaptador HTTP delgado | `SrpArchitectureTest` |
| `UserCreationService` | Alta de usuario y aplicación de la política de contraseña | La política solo se aplicaba al cambiar contraseña | Se creó `PasswordPolicy` y se reutiliza en alta, alta administrativa y cambio | Alta de usuario coherente | `UserServiceTest`, `PasswordChangeServiceTest` |
| `InvoiceGenerateService` / `InvoiceVoidService` | Generación y anulación de facturas | La generación marca `paid = true`, pero la anulación exige `paid = false`; no existe endpoint para marcar impaga | **Pendiente de decisión funcional:** definir si la factura nace impaga, si el pago se registra aparte o si la anulación debe admitir otro estado | Flujo de facturación coherente | `InvoiceServiceTest` (solo cubre datos con `paid=false`) |
| `OrderCalculationService` | Composición de la orden | Valida producto activo, cantidad positiva (`InvalidQuantityException`) y stock disponible (`InsufficientStockException`) | **Resuelto:** el servicio usa stock `AVAILABLE` agregado y rechaza productos inactivos con `ProductNotActiveException` | Composición de orden | `OrderServiceTest` |

## Contratos arquitectónicos agregados

`SrpArchitectureTest` verifica de forma automática que:

1. Ningún controlador REST declare campos de tipo `com.nexusmarket.domain.ports.out.*`
   ni repositorios Mongo.
2. Ningún servicio de `com.nexusmarket.domain.services` declare parámetros ni
   campos de `jakarta.servlet`.
