# Auditoría de servicios y excepciones

**Fecha:** 2026-10-21  
**Repositorio:** `construccion_software_2_2026_2_nexusmarket`  
**HEAD auditado:** `b29f69918470182bbb31404eb1cf081ea6645eae` (sin commit ni push)  
**Alcance:** servicios de dominio, casos de uso, controladores, puertos y excepciones de producción.

> Esta auditoría se hizo sobre el árbol de trabajo local, que ya contenía cambios
> previos sin confirmar. No se descartó ni se sobrescribió trabajo del usuario.
> Las correcciones de esta misión se aplicaron después del inventario y se
> verificaron con pruebas.

## 1. Resumen del estado actual

Se inspeccionaron:

- **28 servicios** de `domain/services`.
- **16 implementaciones de casos de uso** en `adapters/useCases`.
- **19 controladores REST** en `adapters/rest/controllers`.
- **16 puertos de entrada** y **14 puertos de salida**.
- **18 clases de excepción** en `domain/exceptions`: `DomainException` (abstracta)
  y 17 excepciones concretas.
- **37 clases de prueba** y la suite completa de **211 pruebas**.

El dominio no importa `jakarta.servlet`, Spring MVC, DTO REST ni repositorios
Mongo/JPA. Los controladores no inyectan puertos de salida. La separación de
responsabilidades ya aplicada en misiones anteriores se conserva:

- `UserManagementService` está dividido en cinco servicios especializados.
- La lectura de auditoría de sesiones está separada de `SessionAuditService`.
- La extracción de IP/User-Agent está en `HttpRequestMetadataExtractor`.
- Los controladores delegan y aplican, como máximo, autorización de propiedad.
- `GlobalExceptionHandler` sigue siendo el único traductor a HTTP.

El consolidado de la auditoría contiene **tres defectos confirmados P1**: la
anulación de facturas contraria a RN-010/RF-032; los contratos 409/422 de
reembolsos y devoluciones ya procesados; y la reserva de inventario con
cantidades no positivas, que podía aumentar el stock. Los tres se corrigieron y se cubrieron
con pruebas. También se corrigieron la integridad de la jerarquía de categorías
(P2-04), el rechazo de reembolsos sobre facturas inactivas y varios mensajes de
excepción ambiguos. El resto de hallazgos son oportunidades P2/P3 o riesgos
externos documentados; no se refactorizaron sin evidencia suficiente.

## 2. Inventario de servicios de dominio

La columna **Pruebas directas** indica pruebas que referencian la clase por
nombre. Una celda vacía significa que no hay prueba unitaria directa; puede
existir cobertura indirecta a través de controladores o flujos de registro.

| Servicio | Responsabilidad y métodos principales | Puertos / dependencias | Excepciones | Pruebas directas | SRP |
|---|---|---|---|---|---|
| `AddBuyerAddressService` | Añadir dirección adicional a un comprador (`addAdditionalAddress`) | `BuyerRepositoryPort` | `ResourceNotFoundException` | — | Cohesivo; operación única |
| `CategoryDomainService` | Crear, actualizar y eliminar categorías (`createCategory`, `updateCategory`, `deleteCategory`) | `CategoryRepositoryPort`, `ProductRepositoryPort` | `DuplicateResourceException`, `BusinessRuleException`, `CategoryHasProductsException`, `ResourceNotFoundException` | `CategoryServiceTest` | Cohesivo para el agregado Categoría; valida ciclos y subcategorías (P2-04 corregido) |
| `ChangeBuyerCommercialStatusService` | Cambiar estado comercial del comprador | `BuyerRepositoryPort` | `ResourceNotFoundException` | — | Cohesivo; operación única |
| `ConsultAuditLogsService` | Consultar auditoría por usuario, entidad, rango y id | `AuditLogRepositoryPort` | `ResourceNotFoundException` | `AuditServiceTest` | Cohesivo; solo lectura |
| `ConsultSessionEventsService` | Consultar eventos de sesión con filtros y paginación | `SessionAuditPort` | — | `ConsultSessionEventsServiceTest`, `SessionEventQueryUseCaseImplTest` | Fachada de lectura delgada pero justificada: aísla el puerto de auditoría de escritura |
| `CreateBuyerService` | Validar usuario/rol/duplicado y crear comprador | `BuyerRepositoryPort`, `UserRepositoryPort` | `BusinessRuleException`, `ResourceNotFoundException` | — | Cohesivo; creación de comprador |
| `InventoryConsultService` | Consultar inventario por id, producto o bodega | `InventoryRepositoryPort`, `ProductRepositoryPort`, `WarehouseRepositoryPort` | `ResourceNotFoundException` | `InventoryServiceTest` | Cohesivo; solo lectura |
| `InventoryManagementService` | Crear inventario, reservar (solo cantidades positivas), confirmar pago, marcar dañado y ajustar | `InventoryRepositoryPort`, `ProductRepositoryPort`, `WarehouseRepositoryPort` | `BusinessRuleException`, `WarehouseCapacityExceededException`, `ResourceNotFoundException` | `InventoryServiceTest` | Cohesivo para el ciclo de vida del inventario; invariante `amount > 0` en el modelo |
| `InvoiceGenerateService` | Validar orden facturable y duplicado; generar factura | `InvoiceRepositoryPort`, `OrderRepositoryPort` | `InvoiceNotPayableException`, `InvoiceAlreadyExistsException`, `ResourceNotFoundException` | `InvoiceServiceTest` | Cohesivo; emisión de factura. **P2-07 pendiente:** genera `paid=true`, lo que deja inalcanzable el camino exitoso de anulación |
| `InvoiceVoidService` | Anular una factura activa e impaga | `InvoiceRepositoryPort` | `InvoiceNotPayableException`, `ResourceNotFoundException` | `InvoiceServiceTest` | Cohesivo; se retiró la dependencia de reembolsos y se corrigió RN-010 |
| `OrderCalculationService` | Añadir/quitar ítems y recalcular el total | `OrderRepositoryPort`, `ProductRepositoryPort` | `BusinessRuleException`, `ResourceNotFoundException` | `OrderServiceTest` | Cohesivo para composición de la orden; validación de disponibilidad queda P2 |
| `OrderCreateService` | Crear orden en `CART` validando comprador y usuario activo | `OrderRepositoryPort`, `BuyerRepositoryPort`, `UserRepositoryPort` | `BusinessRuleException`, `ResourceNotFoundException` | `OrderServiceTest` | Cohesivo; creación de orden |
| `OrderLifecycleService` | Aplicar transiciones de estado de la orden | `OrderRepositoryPort` | `ResourceNotFoundException`; `InvalidStatusTransitionException` desde el modelo | `OrderServiceTest` | Cohesivo; delegación de transiciones |
| `PasswordChangeService` | Verificar contraseña actual, aplicar política, cifrar y revocar sesiones | `UserRepositoryPort`, `PasswordEncoder` | `BusinessRuleException`, `ResourceNotFoundException` | `PasswordChangeServiceTest`, `AuthServiceTest`, `TokenInvalidationServiceTest` | Cohesivo; cambio de contraseña |
| `ProductDomainService` | Crear, actualizar y desactivar productos; validar vendedor, categoría e inventario | `ProductRepositoryPort`, `CategoryRepositoryPort`, `SellerRepositoryPort`, `UserRepositoryPort`, `InventoryRepositoryPort` | `BusinessRuleException`, `DuplicateResourceException`, `ProductNotAvailableException`, `ResourceNotFoundException` | `ProductServiceTest` | Cohesivo para publicación de productos; cinco puertos son un punto P2 a vigilar |
| `RefundApproveService` | Aprobar o rechazar un reembolso `PENDING` | `RefundRepositoryPort` | `InvalidStatusTransitionException`, `ResourceNotFoundException` | `RefundServiceTest` | Cohesivo; estados ya procesados → 409 (RF-035) |
| `RefundProcessService` | Crear reembolso validando factura activa/pagada, monto acumulado y devolución | `RefundRepositoryPort`, `InvoiceRepositoryPort`, `ReturnRepositoryPort` | `RefundAmountExceededException`, `RefundNotAllowedException`, `ResourceNotFoundException` | `RefundServiceTest` | Cohesivo; creación de reembolso; rechaza facturas inactivas o impagas |
| `RegisterAuditLogService` | Validar y persistir un registro de auditoría | `AuditLogRepositoryPort` | `BusinessRuleException` | `AuditServiceTest` | Cohesivo; escritura de auditoría |
| `ReturnProcessService` | Crear, aprobar, rechazar y completar devoluciones | `ReturnRepositoryPort`, `OrderRepositoryPort` | `ReturnAlreadyProcessedException`, `ReturnNotAllowedException`, `ReturnWindowExpiredException`, `ResourceNotFoundException` | `ReturnServiceTest` | Cohesivo para el ciclo de vida; aprobar/rechazar fuera de `REQUESTED` → 409 (RF-041) |
| `SellerDomainService` | Crear, activar, desactivar y actualizar vendedores | `SellerRepositoryPort`, `UserRepositoryPort` | `BusinessRuleException`, `DuplicateResourceException`, `ResourceNotFoundException` | — | Cohesivo; ciclo de vida del vendedor |
| `SessionAuditService` | Registrar eventos de sesión de forma asíncrona y tolerante a fallos | `SessionAuditPort` | Captura `Exception` deliberadamente para no romper autenticación | `SessionAuditServiceTest`, `JwtAuthenticationFilterAuditTest`, `JwtAuthenticationFilterBlockedUserTest`, pruebas de controladores | Cohesivo; escritura de auditoría |
| `SessionInvalidationService` | Incrementar `tokenVersion` para revocar JWT | `UserRepositoryPort` | `ResourceNotFoundException` | `UserServiceTest` | Cohesivo; invalidación explícita |
| `ShipmentCreateService` | Crear envío para una orden `DISPATCHED` | `ShipmentRepositoryPort`, `OrderRepositoryPort`, `WarehouseRepositoryPort` | `BusinessRuleException`, `ShipmentAlreadyExistsException`, `ResourceNotFoundException` | `ShipmentServiceTest` | Cohesivo; creación de envío |
| `ShipmentStatusService` | Actualizar estado respetando estados terminales | `ShipmentRepositoryPort` | `InvalidStatusTransitionException`, `ResourceNotFoundException` | `ShipmentServiceTest` | Cohesivo; transición de envío |
| `UserCreationService` | Validar duplicado, aplicar `PasswordPolicy`, cifrar y crear usuario | `UserRepositoryPort`, `PasswordEncoder` | `DuplicateResourceException`; `BusinessRuleException` desde la política | `UserServiceTest` | Cohesivo; alta de usuario |
| `UserQueryService` | Consultar usuarios por id, email, rol y listados | `UserRepositoryPort` | `ResourceNotFoundException` | `UserServiceTest` | Cohesivo; solo lectura |
| `UserStatusService` | Bloquear, activar y cambiar rol | `UserRepositoryPort` | `ResourceNotFoundException` | `UserServiceTest` | Cohesivo; estado administrativo |
| `WarehouseDomainService` | Crear y actualizar bodegas; validar capacidad | `WarehouseRepositoryPort`, `InventoryRepositoryPort` | `DuplicateResourceException`, `ResourceNotFoundException`, `WarehouseCapacityExceededException` | `WarehouseServiceTest` | Cohesivo; ciclo de vida de bodega |

## 3. Inventario de casos de uso

Los casos de uso son fachadas de aplicación: delegan escrituras en servicios de
dominio y resuelven consultas o composición de respuestas. No se encontró acceso
directo a repositorios concretos ni a MongoDB desde controladores.

| Caso de uso | Responsabilidad | Dependencias | Excepciones | Pruebas directas | Evaluación |
|---|---|---|---|---|---|
| `AuditUseCaseImpl` | Construir el `AuditLog` y delegar escritura/lectura | `RegisterAuditLogService`, `ConsultAuditLogsService` | — | `AuditServiceTest` | Cohesivo; mezcla comando y consulta del mismo módulo |
| `AuthenticationUseCaseImpl` | Registro, login, logout, cambio de contraseña, perfiles y auditoría | `UserUseCasePort`, `BuyerUseCasePort`, `SellerUseCasePort`, `PasswordChangeService`, `SessionAuditService`, `JwtService`, `AuthenticationManager` | `BusinessRuleException`; `AuthenticationException` de Spring Security | `AuthServiceTest`, `JwtAuthenticationFilterAuditTest`, `TokenInvalidationServiceTest` | **Potencial P2**: fachada con cuatro operaciones; se mantiene por cohesión del contrato de autenticación |
| `BuyerUseCaseImpl` | Fachada de creación, consulta y cambios de comprador | 3 servicios de comprador + `BuyerRepositoryPort` | `ResourceNotFoundException` | — | Cohesivo; comandos delegados, consultas directas |
| `CatalogUseCaseImpl` | Catálogo público: productos activos, categorías raíz, búsqueda y detalle | `ProductRepositoryPort`, `CategoryRepositoryPort` | `ResourceNotFoundException` | `CatalogServiceTest` | Cohesivo; modelo de lectura |
| `CategoryUseCaseImpl` | Fachada CRUD/consulta de categorías | `CategoryDomainService`, `CategoryRepositoryPort` | `ResourceNotFoundException` | `CategoryServiceTest` | Cohesivo |
| `InventoryUseCaseImpl` | Fachada de gestión y consulta de inventario | `InventoryManagementService`, `InventoryConsultService` | — | `InventoryServiceTest` | Cohesivo; solo delegación |
| `InvoiceUseCaseImpl` | Fachada de emisión, consulta y anulación de facturas | `InvoiceGenerateService`, `InvoiceVoidService`, `InvoiceRepositoryPort`, `OrderRepositoryPort` | `ResourceNotFoundException` | `InvoiceServiceTest` | Cohesivo |
| `OrderUseCaseImpl` | Fachada de creación, composición, ciclo de vida y consulta de órdenes | 3 servicios de orden + 2 puertos de lectura | `ResourceNotFoundException` | `OrderServiceTest` | Cohesivo; comandos/consultas del mismo agregado |
| `ProductUseCaseImpl` | Fachada CRUD/consulta de productos | `ProductDomainService`, `ProductRepositoryPort` | `ResourceNotFoundException` | `ProductServiceTest` | Cohesivo |
| `RefundUseCaseImpl` | Fachada de creación, consulta y decisión de reembolsos | 2 servicios + 2 puertos de lectura | `ResourceNotFoundException` | `RefundServiceTest` | Cohesivo |
| `ReturnUseCaseImpl` | Fachada de devoluciones y agregación por comprador | `ReturnProcessService`, `ReturnRepositoryPort`, `OrderRepositoryPort` | `ResourceNotFoundException` | `ReturnServiceTest` | Cohesivo; la agregación por comprador es orquestación de lectura |
| `SellerUseCaseImpl` | Fachada CRUD/consulta de vendedores | `SellerDomainService`, `SellerRepositoryPort` | `ResourceNotFoundException` | — | Cohesivo; sin prueba directa del caso de uso |
| `SessionEventQueryUseCaseImpl` | Normalizar paginación y delegar consulta de eventos | `ConsultSessionEventsService` | — | `SessionEventQueryUseCaseImplTest` | Cohesivo; borde de aplicación |
| `ShipmentUseCaseImpl` | Fachada de creación, consulta y transición de envíos | 2 servicios + 2 puertos de lectura | `ResourceNotFoundException`, `TrackingNotFoundException` | `ShipmentServiceTest` | Cohesivo |
| `UserUseCaseImpl` | Fachada de administración de usuarios | 4 servicios especializados | — | `UserServiceTest` | Cohesivo; no accede a repositorios |
| `WarehouseUseCaseImpl` | Fachada CRUD/consulta de bodegas | `WarehouseDomainService`, `WarehouseRepositoryPort` | `ResourceNotFoundException` | `WarehouseServiceTest` | Cohesivo |

## 4. Inventario de excepciones

Todas las excepciones propias heredan de `DomainException`, que no conoce HTTP
ni Spring. `GlobalExceptionHandler` decide el código HTTP.

| Excepción | Condición | Componentes que la lanzan | HTTP | Evaluación |
|---|---|---|---|---|
| `ResourceNotFoundException` | Recurso inexistente | Casos de uso, servicios y mappers JPA | 404 | Correcta; mensaje con recurso e id |
| `TrackingNotFoundException` | Código de seguimiento inexistente | `ShipmentUseCaseImpl.findByTracking` | 404 | Correcta; específica del caso de tracking |
| `BusinessRuleException` | Regla de negocio genérica | `PasswordPolicy`, `Inventory`, servicios de usuario/compra/producto/categoría/orden/auditoría | 422 | Correcta como regla general; no oculta condiciones que requieran otro código HTTP |
| `DuplicateResourceException` | Recurso duplicado | Usuarios, productos, categorías, vendedores, bodegas | 409 | Correcta |
| `InvalidStatusTransitionException` | Transición de estado inválida | Modelos `Order`/`Shipment`, `ShipmentStatusService`, `RefundApproveService` | 409 | Correcta; expone estado actual y destino como detalles seguros. Se amplió a reembolsos ya procesados (RF-035) |
| `ProductNotAvailableException` | Producto con stock reservado no desactivable | `ProductDomainService.deactivateProduct` | 409 | Correcta |
| `CategoryHasProductsException` | Categoría con productos | `CategoryDomainService.deleteCategory` | 409 | Correcta |
| `WarehouseCapacityExceededException` | Capacidad de bodega excedida | `InventoryManagementService`, `WarehouseDomainService` | 422 | Correcta |
| `InvoiceAlreadyExistsException` | Factura activa duplicada | `InvoiceGenerateService` | 409 | Correcta; semánticamente más específica que `DuplicateResourceException` |
| `InvoiceNotPayableException` | Factura/orden no facturable o anulable | `InvoiceGenerateService`, `InvoiceVoidService` | 422 | Corregida: mensaje y uso para anulación |
| `RefundAmountExceededException` | Reembolso excede el total | `RefundProcessService` | 422 | Correcta |
| `RefundNotAllowedException` | Reembolso no permitido: monto inválido o factura inactiva/impaga | `RefundProcessService` | 422 | Correcta; ya no se usa para estados ya procesados ni para anular facturas |
| `PaymentGatewayException` | Fallo de pasarela de pago | Ningún productor actual | 502 | Sin uso; se conserva por contrato/handler, pendiente de integración real |
| `ShipmentAlreadyExistsException` | Envío duplicado por orden o tracking | `ShipmentCreateService` | 409 | Correcta; específica frente a `DuplicateResourceException` |
| `ReturnWindowExpiredException` | Plazo de devolución vencido | `ReturnProcessService` | 422 | Correcta |
| `ReturnNotAllowedException` | Completar una devolución que aún no está aprobada | `ReturnProcessService` | 422 | Correcta; limitada a `REQUESTED → COMPLETED` |
| `ReturnAlreadyProcessedException` | Devolución ya procesada; aprobar/rechazar fuera de `REQUESTED` o completar un estado final | `ReturnProcessService` | 409 | Mensaje corregido y uso ampliado conforme a RF-041 |
| `DomainException` (abstracta) | Base común | — | 400 si no tiene handler específico | Red de seguridad; todos los tipos concretos actuales tienen mapeo |

No se añadieron excepciones nuevas. Se reutilizaron `InvoiceNotPayableException`
para la anulación e `InvalidStatusTransitionException` para reembolsos ya
procesados; se corrigieron mensajes y no se crearon clases para duplicar
significados existentes.

## 5. Hallazgos y priorización

### P0 — Crítico

| ID | Archivo / clase | Problema | Evidencia | Riesgo | Recomendación / estado |
|---|---|---|---|---|---|
| P0-01 | `SECURITY.md` y commits históricos `910929b`, `d245062`, `c24a81c` | Credenciales históricas de MongoDB presentes en el historial; no hay constancia de revocación | Documentado en `SECURITY.md`; no se reproducen secretos aquí | Acceso no autorizado si las credenciales siguen vigentes | Rotar/revocar credenciales y sanear el historial con un plan autorizado. **Pendiente externo; no se ejecutó** |

### P1 — Defecto funcional / arquitectura importante

| ID | Archivo / clase | Responsabilidad actual | Problema detectado | Evidencia | Riesgo | Recomendación / estado |
|---|---|---|---|---|---|---|
| P1-01 | `domain/services/InvoiceVoidService.java` | Anular factura | Permitía anular una factura pagada si existía un reembolso aprobado, y no rechazaba una factura ya inactiva. Contradecía RN-010 y RF-032, que exigen factura activa e impaga | Código anterior consultaba `RefundRepositoryPort`; la prueba solo cubría el caso pagado sin reembolso | Se podía anular una factura pagada por una vía no prevista y devolver una operación ya realizada con 200 | **Corregido:** solo `active == true && paid == false`; se lanza `InvoiceNotPayableException`; pruebas de éxito, pagada e inactiva. Ver `InvoiceServiceTest` |
| P1-02 | `RefundApproveService`, `ReturnProcessService` | Decisiones sobre reembolsos/devoluciones | Aprobar o rechazar un reembolso ya procesado devolvía 422 (`RefundNotAllowedException`) y una devolución ya procesada también devolvía 422 (`ReturnNotAllowedException`), pese a que RF-035/RF-041 exigen 409 | Pruebas antiguas esperaban las excepciones de 422; el contrato HTTP documentado no coincidía con el requisito | Clientes y pruebas contractuales recibían un código distinto para el mismo estado ya procesado | **Corregido:** reembolsos no `PENDING` → `InvalidStatusTransitionException` (409); devoluciones no `REQUESTED` al aprobar/rechazar → `ReturnAlreadyProcessedException` (409). Completar una `REQUESTED` sigue siendo 422. Ver `RefundServiceTest`, `ReturnServiceTest` |
| P1-03 | `domain/models/Inventory.java`, `InventoryManagementService` | Reserva de stock | `reserve` restaba `amount` sin exigir `amount > 0`; un valor negativo incrementaba la cantidad y marcaba el inventario como `RESERVED`. El controlador no validaba el parámetro | `Inventory.reserve` solo comprobaba `DAMAGED` y `quantity < amount`; `@RequestParam int amount` no tenía `@Positive` | Un operador podía inflar el stock con una reserva negativa, afectando disponibilidad y pedidos | **Corregido:** el modelo exige `amount > 0`; `createInventory` rechaza cantidad nula o negativa; pruebas de cero, negativo y nulo. La reserva inválida devuelve 422 (`BusinessRuleException`) |

### P2 — SRP, duplicación o deuda significativa

| ID | Archivo / clase | Responsabilidad actual | Problema detectado | Evidencia | Riesgo | Recomendación / estado |
|---|---|---|---|---|---|---|
| P2-01 | `adapters/useCases/AuthenticationUseCaseImpl.java` | Registro, login, logout y cambio de contraseña | Cuatro operaciones y coordinación de perfiles/JWT/auditoría en una fachada | 7 dependencias inyectadas; métodos públicos de cuatro flujos | Cambios en un flujo pueden afectar a otro; difícil aislar pruebas | **Sin cambio**: se evaluó dividir en servicios por operación, pero se mantiene como fachada cohesiva para no fragmentar el contrato HTTP ni duplicar la transacción de registro. Reevaluar si crece el registro |
| P2-02 | `domain/services/ProductDomainService.java` | Publicación y ciclo de vida del producto | Cinco puertos de salida y validaciones de vendedor, usuario, categoría e inventario | Constructor con 5 dependencias | La publicación puede acumular más reglas y volverse difícil de probar | **Sin cambio**: las reglas pertenecen al mismo agregado. Si aparecen reglas de publicación independientes, extraer una política de publicación |
| P2-03 | Servicios de `domain/services` | Lógica de dominio | Usan `@Service`, `@Transactional` y dos servicios usan `PasswordEncoder` de Spring Security | 28 servicios; `UserCreationService`, `PasswordChangeService` | Acoplamiento al contenedor/seguridad; dificulta reutilizar el dominio fuera de Spring | **Sin cambio** por riesgo y alcance: declarar beans en `infrastructure/config` y abstraer el cifrado mediante un puerto. Documentado como deuda |
| P2-04 | `domain/services/CategoryDomainService.java` | Integridad de la jerarquía de categorías | Solo impedía que una categoría fuera su propio padre; no detectaba ciclos con descendientes ni validaba categorías hijas al borrar | `updateCategory` comparaba `parentId.equals(id)` y no recorría ancestros; `deleteCategory` solo consultaba productos | Jerarquías cíclicas o borrado de una categoría padre con hijas; posible error de integridad en persistencia | **Corregido:** `updateCategory` recorre ancestros y detecta ciclos; `deleteCategory` consulta `findByParentId` y rechaza hijas (`BusinessRuleException`, 422). Pruebas en `CategoryServiceTest` |
| P2-05 | `domain/services/OrderCalculationService.java` | Composición de la orden | `addItem` valida cantidad y existencia, pero no que el producto esté activo ni que haya stock disponible | RF-024 solo exige 404 para producto inexistente; el servicio no consulta inventario | Se pueden añadir productos inactivos o sin stock; el fallo se traslada al checkout o a una validación posterior | **Pendiente de decisión funcional**: si la regla es “solo productos activos/disponibles”, añadir la validación y pruebas; no se inventó una regla no documentada |
| P2-06 | Controladores de comprador/vendedor | Autorización de propiedad | Cada controlador repite comparaciones de `userId`/`buyerId`/`sellerId` | `ProductController`, `OrderController`, `BuyerController`, `SellerController`, `ReturnController` | Duplicación y riesgo de olvidar una comprobación en un endpoint nuevo | **Sin cambio inmediato**: funciona y está cubierto por pruebas; extraer una política de propiedad si se repite en más endpoints |
| P2-07 | `InvoiceGenerateService` / `InvoiceVoidService` | Generación y anulación de facturas | La generación crea la factura con `paid = true`, pero la anulación exige `paid = false`; no existe endpoint para marcar una factura como impaga | `InvoiceGenerateService` usa `paid(true)`; RF-032 describe anular una factura impaga | El camino exitoso de anulación queda inalcanzable en el flujo normal; solo funciona con datos heredados/importados | **Pendiente de decisión funcional:** definir si la factura nace impaga, si el pago se registra en otro flujo o si la anulación debe admitir otro estado. No se cambió el comportamiento para no inventar un pago de factura |
| P2-08 | `RefundProcessService` | Integridad factura–devolución | Verifica que la devolución exista, pero no que pertenezca a la misma orden que la factura | `returnRepositoryPort.existsById(returnRequestId)` no compara `orderId` de factura y devolución | Un reembolso podría asociarse a una devolución de otra orden si el cliente envía ids válidos pero no relacionados | **Pendiente de confirmación:** si se exige la relación, usar `findById` y comparar `invoice.orderId` con `return.orderId`; no se aplicó para no ampliar el contrato sin confirmación |

### P3 — Mejora menor / consistencia

| ID | Archivo / clase | Problema | Evidencia | Recomendación / estado |
|---|---|---|---|---|
| P3-01 | `InvoiceNotPayableException` | El mensaje anterior decía “Order must be PAID” aunque la generación también admite `DISPATCHED`, `DELIVERED` y `FINISHED` | `InvoiceGenerateService` valida cuatro estados | **Corregido:** mensaje neutral de estado facturable/anulable; los servicios lanzan mensajes específicos |
| P3-02 | `ReturnAlreadyProcessedException` | El mensaje decía “processed or is pending” incluso al aprobar/rechazar/completar una devolución ya procesada | Uso en `ReturnProcessService` para estados no válidos | **Corregido:** mensaje que indica que el estado no permite la operación |
| P3-03 | `InvoiceAlreadyExistsException`, `ShipmentAlreadyExistsException`, `DuplicateResourceException` | Semántica de duplicado con tres tipos y el mismo HTTP 409 | Handlers separados en `GlobalExceptionHandler` | Se conservan: aportan contexto de recurso y permiten un tratamiento futuro distinto. No se creó una abstracción adicional |
| P3-04 | `PaymentGatewayException` | No tiene productor en el código actual | Solo aparece en el handler y en `DomainExceptionContractTest` | Se conserva documentada; eliminar solo si se confirma que no se integrará una pasarela |
| P3-05 | `GlobalExceptionHandler.handleUnmappedDomainException` | Devuelve `ex.getMessage()` para una excepción de dominio nueva | Handler de respaldo 400 | Los 17 tipos actuales tienen handler específico; para tipos futuros, considerar mensaje genérico y log interno para no filtrar detalles |
| P3-06 | `SessionAuditService`, `JwtAuthenticationFilter`, `JwtService` | Capturas genéricas de `Exception` | Auditoría de sesión y validación JWT | **Justificadas**: el registro de auditoría es best-effort y el filtro debe fallar cerrado sin autenticar. No se cambió |
| P3-07 | Mappers JPA | Lanzan `ResourceNotFoundException` de dominio desde el adaptador | Varios `*JpaMapper` | Dirección de dependencia correcta (adaptador → dominio), pero mezcla detalle de persistencia con excepción de dominio; revisar si se crea un puerto de carga de relaciones |
| P3-08 | Pruebas | Cuatro servicios y dos casos de uso no tienen prueba unitaria directa | `AddBuyerAddressService`, `ChangeBuyerCommercialStatusService`, `CreateBuyerService`, `SellerDomainService`, `BuyerUseCaseImpl`, `SellerUseCaseImpl` | Añadir pruebas directas cuando se toquen esos flujos; hoy hay cobertura indirecta parcial de controladores/registro |
| P3-09 | `SDD/04-api/contratos-por-endpoint.md` | Los parámetros de inventario documentados (`quantity`, `newQuantity`) no coincidían con el controlador (`amount`) y se listaban parámetros inexistentes en `confirm-payment`/`mark-damaged` | Controlador `InventoryController` | **Corregido en documentación:** se documentan `amount` como reserva y delta de ajuste, y se eliminan parámetros inexistentes |
| P3-10 | `GlobalExceptionHandler` / contratos | El contrato HTTP de reembolsos/devoluciones ya procesados estaba desalineado con RF-035/RF-041 | Documentación y pruebas previas esperaban 422 | **Corregido:** código y pruebas alineados a 409; documentación de API y reglas actualizada |

## 6. Dependencias y arquitectura hexagonal

### Dentro de la dirección permitida

- `domain/services` implementa puertos de salida; no importa `jakarta.servlet`,
  Spring MVC, DTO REST ni repositorios concretos.
- Los controladores dependen de puertos de entrada y mapeadores REST; el test
  `SrpArchitectureTest` verifica que no inyecten puertos de salida ni
  `MongoRepository`.
- Los adaptadores de persistencia implementan los puertos de salida.
- `GlobalExceptionHandler` está en el adaptador REST y centraliza la traducción.
- `SessionAuditService` no conoce HTTP; la extracción de metadatos vive en
  `HttpRequestMetadataExtractor`.

### Deuda o acoplamiento documentado

1. **Anotaciones Spring en dominio:** `@Service` y `@Transactional` en los
   servicios. Es una decisión pragmática existente; la alternativa es configurar
   beans en infraestructura y dejar POJOs.
2. **`PasswordEncoder` en dominio:** `UserCreationService` y
   `PasswordChangeService` dependen de una interfaz de Spring Security. No es una
   implementación concreta, pero sigue siendo un detalle de seguridad externo.
3. **Mappers JPA que lanzan excepciones de dominio:** la dirección es correcta,
   pero el adaptador decide una condición de dominio. No se refactorizó por
   riesgo de cambiar la semántica de carga de relaciones.
4. **`AuthenticationUseCaseImpl` depende de `JwtService`:** está en
   `adapters/useCases`, no en dominio; se acepta como coordinación de aplicación.

No se encontraron controladores con lógica de negocio compleja ni acceso directo
a repositorios. La autorización por rol y propiedad sigue en `SecurityConfig` y
en comprobaciones de propiedad de los controladores.

## 7. Duplicación de reglas de negocio

| Regla | Dónde vive | Evaluación |
|---|---|---|
| Política de contraseñas | `domain/policies/PasswordPolicy` | Centralizada; usada por alta y cambio. Correcta |
| Validación de precio positivo | `ProductDomainService.createProduct` y `updateProduct` | Duplicación menor dentro del mismo servicio; no justifica una clase nueva |
| Duplicado de nombre de categoría | `CategoryDomainService.createCategory` y `updateCategory` | Misma regla, distinta operación; aceptable |
| Capacidad de bodega | `WarehouseDomainService.updateWarehouse` e `InventoryManagementService.createInventory` | Cada operación calcula el almacenamiento actual; comparten concepto, no flujo. Aceptable |
| Búsqueda `getByIdOrThrow` | Varios servicios y casos de uso | Patrón repetido de lectura; extraer un helper genérico no aportaría cohesión y ocultaría el recurso |
| Autorización de propiedad | Varios controladores | Duplicación P2; candidata a política común, no aplicada |
| Estados terminales de envío | `ShipmentStatusService` + modelo de dominio | Una sola decisión de transición; correcta |
| Transiciones de devolución/reembolso | `ReturnProcessService`, `RefundApproveService` | Cada agregado mantiene su propia máquina de estados; no se duplican reglas entre sí |

## 8. Riesgos de seguridad y manejo de errores

- **P0 histórico:** credenciales de MongoDB en commits antiguos. No se
  reprodujeron en este documento. La rotación y la limpieza del historial siguen
  pendientes y requieren autorización y coordinación externa.
- **JWT y bloqueo:** el bloqueo incrementa `tokenVersion`; el filtro rechaza
  usuarios deshabilitados y tokens anteriores. Cubierto por
  `JwtAuthenticationFilterBlockedUserTest` y `TokenInvalidationServiceTest`.
- **Roles y propiedad:** las rutas están protegidas por rol y los recursos de
  comprador/vendedor verifican propiedad. Cubierto por pruebas de controladores.
- **Integridad de stock:** la reserva exige `amount > 0`; antes un valor negativo
  incrementaba la cantidad. La regla está en el modelo, no solo en el controlador,
  y se prueba con cero/negativo.
- **Errores HTTP:** `GlobalExceptionHandler` no devuelve stack traces; 500 usa un
  mensaje genérico; 401/403 no exponen el motivo de autorización; las violaciones
  de integridad y bloqueos optimistas se registran sin SQL ni valores.
- **Fallback de dominio:** el handler de `DomainException` no mapeada devuelve el
  mensaje. Es seguro para los tipos actuales, pero debe endurecerse si se añaden
  excepciones con detalles internos (P3-05).
- **Auditoría best-effort:** `SessionAuditService` captura `Exception` y registra
  el fallo; no propaga el error al login. Es una decisión de disponibilidad
  consciente y está probada.
- **Datos personales:** los eventos de sesión registran email, IP, User-Agent y
  jti. No se auditó la retención ni el acceso a esos datos; queda como revisión
  de privacidad pendiente, no como secreto.

## 9. Pruebas existentes y faltantes

### Suite ejecutada

- **Comando:** `.\mvnw.cmd -B clean package`
- **Resultado real:** **211 pruebas, 0 fallos, 0 errores, 0 omitidas**.
- **Clases de prueba:** 37.
- **Pruebas específicas de las correcciones:**
  - `InvoiceServiceTest` (6): generación exitosa, orden no facturable, factura duplicada, anulación exitosa de factura activa e impaga, rechazo de factura pagada y rechazo de factura ya inactiva.
  - `RefundServiceTest` (9): creación, factura impaga, factura inactiva, monto excedido, monto no positivo, monto acumulado excedido, aprobación de `PENDING`, aprobar un reembolso rechazado (409) y rechazar uno aprobado (409).
  - `ReturnServiceTest` (9): creación, orden no entregada, ventana expirada, duplicado activo, agregación por comprador, completar sin aprobar, aprobar un rechazado (409), rechazar un aprobado (409) y completar un rechazado (409).
  - `InventoryServiceTest` (9): creación, cantidad negativa, cantidad nula, capacidad excedida, reserva dañada, cantidad insuficiente, reserva cero, reserva negativa y marcado como dañado.
  - `CategoryServiceTest` (7): creación, duplicado, borrado con productos, borrado con subcategorías, borrado exitoso, ciclo de jerarquía y categoría inexistente.

La salida de `SessionAuditServiceTest` incluye un stack trace de un fallo
simulado de MongoDB; es el comportamiento esperado de la prueba de tolerancia,
no un fallo de la suite.

### Cobertura relevante

- Servicios: usuarios, contraseñas, inventario, productos, categorías, órdenes,
  facturas, reembolsos, devoluciones, envíos, bodegas, auditoría de sesiones.
- Casos de uso: la mayoría tiene prueba directa o cobertura de controlador.
- Seguridad: roles, propiedad, JWT, usuario bloqueado, CORS y contrato de errores.
- Arquitectura: `SrpArchitectureTest` y `DomainExceptionContractTest`.
- Empaquetado: `SensitiveResourcePackagingTest`.

### Pruebas faltantes o no ejecutables

1. **Sin pruebas de integración reales** contra MySQL o MongoDB; no se usaron
   Testcontainers.
2. **Sin E2E** contra Docker Compose. El motor Linux de Docker no estaba
   disponible en el entorno (`dockerDesktopLinuxEngine`).
3. **Sin prueba directa** para `AddBuyerAddressService`,
   `ChangeBuyerCommercialStatusService`, `CreateBuyerService`,
   `SellerDomainService`, `BuyerUseCaseImpl` ni `SellerUseCaseImpl`; hay cobertura
   indirecta parcial.
4. **Cobertura de jerarquía de categorías:** hay pruebas de ciclo directo y de
   borrado con hijas; no hay prueba de una jerarquía preexistente ya corrupta ni
   de concurrencia entre actualizaciones.
5. **Sin prueba de producto inactivo/sin stock al añadir a la orden** (P2-05).
6. **Sin prueba de la relación factura–devolución** en reembolsos (P2-08).
7. **Sin prueba de endurecimiento del fallback** de `DomainException` no mapeada.
8. **Sin prueba de `PaymentGatewayException`** porque no tiene productor real.

## 10. Componentes revisados que se conservan

| Componente | Motivo |
|---|---|
| `UserCreationService`, `UserQueryService`, `UserStatusService`, `SessionInvalidationService`, `PasswordChangeService` | La división de `UserManagementService` ya responde a responsabilidades diferenciadas; cada clase tiene un motivo de cambio claro |
| `SessionAuditService` y `ConsultSessionEventsService` | La escritura y la lectura están separadas; la captura de fallos de auditoría es deliberada y probada |
| `InventoryManagementService`, `OrderLifecycleService`, `ShipmentStatusService`, `ReturnProcessService`, `RefundApproveService` | Cada uno gobierna el ciclo de vida de un agregado y mantiene sus transiciones cohesionadas. `ReturnProcessService` y `RefundApproveService` solo ajustaron su contrato de excepción; `InventoryManagementService` añadió la validación positiva de reserva |
| `GlobalExceptionHandler` | Una sola responsabilidad: traducir excepciones a HTTP; se mantiene centralizado y probado |
| `ProductDomainService` | Las reglas de publicación pertenecen al agregado Producto; se vigila el crecimiento de sus dependencias |
| `AuthenticationUseCaseImpl` | Fachada de autenticación; división evaluada y descartada por impacto en contrato y transacción |
| `OrderUseCaseImpl`, `InvoiceUseCaseImpl`, `ReturnUseCaseImpl` y demás fachadas | Combinan comandos y consultas del mismo agregado; separar lectura/escritura no aporta valor con el tamaño actual |
| `AuthController`, `OrderController`, `ProductController`, `ReturnController` | Solo adaptan HTTP, delegan y aplican autorización de propiedad; no acceden a repositorios |

## 11. Plan priorizado

1. **P1-01 — Hecho.** Corregir la anulación de facturas según RN-010/RF-032,
   eliminar la dependencia de reembolsos del servicio de anulación y cubrir los
   tres casos con pruebas.
2. **P1-02 — Hecho.** Alinear reembolsos y devoluciones ya procesados a 409
   (`InvalidStatusTransitionException` / `ReturnAlreadyProcessedException`) según
   RF-035/RF-041, con pruebas actualizadas.
3. **P1-03 — Hecho.** Exigir `amount > 0` en la reserva de inventario y rechazar
   cantidades nulas/negativas al crear inventario, con pruebas.
4. **P2-04 — Hecho.** Detectar ciclos de categoría y bloquear el borrado con
   subcategorías, con pruebas.
5. **P2-05 — Pendiente de decisión funcional.** Confirmar si añadir un producto a
   la orden exige producto activo y stock; si se confirma, implementarlo con una
   excepción existente y pruebas.
6. **P2-07 — Pendiente de decisión funcional.** Resolver la contradicción
   `paid=true` en generación vs. anulación de facturas impagas.
7. **P2-08 — Pendiente de confirmación.** Validar que la devolución pertenece a
   la misma orden que la factura antes de crear el reembolso.
8. **P2-03 — Deuda arquitectónica.** Evaluar la extracción de anotaciones Spring
   y del cifrado a puertos/configuración de infraestructura.
9. **P2-01/P2-02 — Vigilar.** Reevaluar `AuthenticationUseCaseImpl` y
   `ProductDomainService` si crecen sus operaciones o dependencias.
10. **P3-08 — Pruebas.** Añadir pruebas directas de los servicios y casos de uso
    sin cobertura cuando se modifiquen.
11. **P0-01 — Bloqueado.** Rotación/revocación de credenciales y saneamiento del
    historial Git bajo un plan autorizado.

## 12. Limitaciones de esta auditoría

- No se ejecutaron pruebas contra MySQL/MongoDB reales ni Docker.
- No se pudo obtener el diagnóstico completo del editor; la ruta de JDK de
  `.vscode/settings.json` se corrigió en una misión anterior, pero la salida de
  errores del editor no está disponible en este entorno.
- No se auditó el historial Git completo commit por commit; se verificó el estado
  documentado de credenciales históricas y no se reprodujeron secretos.
- No se afirma que el proyecto sea completamente seguro ni que todas las
  responsabilidades sean perfectas: quedan los riesgos P2/P3 y el P0 externo
  indicados arriba.
