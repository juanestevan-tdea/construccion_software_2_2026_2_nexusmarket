# Reglas de Negocio

Cada regla se expresa en prosa y se acompaña de su **invariante en términos de código** (expresión booleana que debe cumplirse siempre, tal como se implementa en los modelos de dominio y servicios).

---

## RN-001 — Un producto no puede tener precio nulo o menor o igual a cero

**Prosa:** Al crear o actualizar un producto, el precio debe ser obligatorio y estrictamente mayor que cero (`@DecimalMin("0.01")`).

**Inviante en código:** `product.price != null && product.price.compareTo(BigDecimal.ZERO) > 0`

**Origen:** `ProductCreateRequestDTO.price` (`@NotNull`, `@DecimalMin("0.01")`), `ProductUseCasePort.createProduct(...)`, `GlobalExceptionHandler` → `400 Bad Request` si falla la validación.

---

## RN-002 — El SKU de un producto es único

**Prosa:** No pueden existir dos productos activos con el mismo SKU. Se valida antes de persistir.

**Invariante en código:** `!productRepositoryPort.existsBySku(sku)` (antes de `save`).

**Origen:** `ProductUseCaseImpl.createProduct`, `ProductRepositoryPort.existsBySku(String)`, `DuplicateResourceException` → `409 Conflict`.

---

## RN-003 — Un comprador o vendedor requiere un usuario existente

**Prosa:** Al crear un comprador (`Buyer`) o un vendedor (`Seller`), el `userId` debe referenciar un `User` ya existente.

**Invariante en código:** `userRepositoryPort.existsById(userId) == true`

**Origen:** `BuyerUseCaseImpl.createBuyer`, `SellerUseCaseImpl.createSeller` (llaman a `userUseCasePort.getUserByIdOrThrow`), `ResourceNotFoundException` → `404 Not Found`.

---

## RN-004 — Ítems de una orden solo se pueden agregar o quitar mientras esté en CART o PENDING_PAYMENT

**Prosa:** Una vez que la orden avanza más allá de `PENDING_PAYMENT`, sus ítems son inmodificables.

**Invariante en código:** `order.status == OrderStatus.CART || order.status == OrderStatus.PENDING_PAYMENT` (precondición de `addItem` / `removeItem`).

**Origen:** `Order.addItem(OrderItem)`, `Order.removeItem(Long)` lanzan `InvalidStatusTransitionException(status.name(), "MODIFY_ITEMS")` si no se cumple → `409 Conflict`.

---

## RN-005 — El checkout solo es válido desde CART con al menos un ítem

**Prosa:** Una orden en estado `CART` puede pasar a `PENDING_PAYMENT` solo si tiene ítems.

**Invariante en código:** `order.status == OrderStatus.CART && !order.items.isEmpty()`

**Origen:** `Order.checkout()` lanza `InvalidStatusTransitionException` si el estado no es `CART` o si `items.isEmpty()` → `409 Conflict`.

---

## RN-006 — Transiciones del ciclo de vida de la orden

**Prosa:** La orden sigue el flujo estricto `CART → PENDING_PAYMENT → PAID → DISPATCHED → DELIVERED → FINISHED`, con la opción de cancelar en cualquier estado excepto `FINISHED` y `DELIVERED`.

**Invariante en código (por transición):**
- `checkout()`: requiere `status == CART`
- `confirmPayment()`: requiere `status == PENDING_PAYMENT` → resulta `PAID`
- `dispatch()`: requiere `status == PAID` → resulta `DISPATCHED`
- `deliver()`: requiere `status == DISPATCHED` → resulta `DELIVERED`
- `finish()`: requiere `status == DELIVERED` → resulta `FINISHED`
- `cancel()`: requiere `status != FINISHED && status != DELIVERED` → resulta `CANCELLED`

**Origen:** `Order.checkout()`, `confirmPayment()`, `dispatch()`, `deliver()`, `finish()`, `cancel()` — cada uno valida y lanza `InvalidStatusTransitionException(current, target)` → `409 Conflict`.

---

## RN-007 — La reserva de inventario no puede exceder la cantidad disponible

**Prosa:** Al reservar unidades de un registro de inventario, la cantidad a reservar debe ser menor o igual a la `quantity` actual y el inventario no debe estar `DAMAGED`.

**Invariante en código:** `inventory.status != InventoryStatus.DAMAGED && inventory.quantity >= amount`

**Origen:** `Inventory.reserve(int)` lanza `BusinessRuleException("Cannot reserve damaged inventory")` o `BusinessRuleException("Insufficient quantity")` → `422 Unprocessable Entity`.

---

## RN-008 — El ajuste de inventario no puede dejar la cantidad negativa

**Prosa:** Al ajustar (sumar o restar) el inventario, la cantidad resultante no puede ser menor que cero.

**Invariante en código:** `inventory.quantity + amount >= 0`

**Origen:** `Inventory.adjust(int)` lanza `BusinessRuleException("Quantity cannot be negative")` → `422 Unprocessable Entity`.

---

## RN-009 — Una categoría con productos no se puede eliminar

**Prosa:** No se puede borrar una categoría que tenga productos asociados.

**Invariante en código:** `productRepositoryPort.findByCategoryId(categoryId).isEmpty()` (antes de `delete`).

**Origen:** `CategoryUseCaseImpl.deleteCategory` lanza `CategoryHasProductsException` → `409 Conflict`.

---

## RN-010 — Una factura ya pagada o anulada no se puede anular ni pagar dos veces

**Prosa:** Una factura con `paid == true` o `active == false` no es modificable.

**Invariante en código:** `invoice.active == true && invoice.paid == false` (precondición de anulación).

**Origen:** `InvoiceUseCaseImpl.voidInvoice` / `InvoiceVoidService` lanza `InvoiceNotPayableException` → `422 Unprocessable Entity`; `InvoiceAlreadyExistsException` si se duplica → `409 Conflict`.

---

## RN-011 — El monto de un reembolso no puede exceder el total facturado

**Prosa:** Al crear un reembolso vinculado a una factura, el monto solicitado no puede ser mayor al monto de la factura.

**Invariante en código:** `refund.amount.compareTo(invoice.amount) <= 0`

**Origen:** `RefundUseCaseImpl.createRefund` / `RefundProcessService` lanza `RefundAmountExceededException` → `422 Unprocessable Entity`.

---

## RN-012 — Un reembolso solo se puede aprobar o rechazar una vez

**Prosa:** Un reembolso en estado `PENDING` puede pasar a `APPROVED` o `REJECTED`; ya no admite cambios posteriores.

**Invariante en código:** `refund.status == RefundStatus.PENDING` (precondición de `approveRefund` / `rejectRefund`).

**Origen:** `RefundUseCaseImpl.approveRefund` / `rejectRefund`; si ya fue procesado → `InvalidStatusTransitionException` → `409 Conflict`.

---

## RN-013 — Una devolución fuera de plazo no se puede crear

**Prosa:** Una solicitud de devolución (`Return`) solo puede crearse dentro de la ventana de devolución vigente.

**Invariante en código:** `LocalDateTime.now().isBefore(returnWindowExpiry)` (regla de negocio de ventana).

**Origen:** `ReturnUseCaseImpl.createReturn` lanza `ReturnWindowExpiredException` → `422 Unprocessable Entity`.

---

## RN-014 — Una devolución ya procesada no se puede aprobar, rechazar ni completar dos veces

**Prosa:** Una vez que una devolución alcanza un estado final (`COMPLETED`, `PROCESSED`, `REJECTED`), no admite nuevas transiciones.

**Invariante en código:** `return.status == ReturnStatus.REQUESTED` (precondición de `approveReturn` / `rejectReturn` / `completeReturn`).

**Origen:** `ReturnUseCaseImpl` lanza `ReturnAlreadyProcessedException` → `409 Conflict`; `ReturnNotAllowedException` → `422 Unprocessable Entity`.

---

## RN-015 — No se puede reservar ni confirmar pago de inventario dañado

**Prosa:** Un registro de inventario con estado `DAMAGED` está bloqueado para operaciones de stock.

**Invariante en código:** `inventory.status != InventoryStatus.DAMAGED` (precondición de `reserve` y `confirmPayment`).

**Origen:** `Inventory.reserve(int)` y `Inventory.confirmPayment()` lanzan `BusinessRuleException` → `422 Unprocessable Entity`.

---

## RN-016 — Un usuario bloqueado no puede autenticarse

**Prosa:** Un `User` con `status == BLOCKED` no puede obtener un JWT válido.

**Invariante en código:** `user.isActive() == true` (precondición de login).

**Origen:** `UserManagementService.authenticate` verifica `user.isActive()`; `User.isActive()` retorna `status == UserStatus.ACTIVE`.

---

## RN-017 — El rol determina qué operaciones puede ejecutar un usuario autenticado

**Prosa:** El acceso a endpoints está controlado por RBAC: `ADMIN` gestiona usuarios; `ADMIN` o `SUPERVISOR` leen auditoría; el resto de endpoints requieren cualquier rol autenticado.

**Invariante en código:** `SecurityConfig.authorizeHttpRequests` mapea patrones de ruta a `hasRole(...)` / `hasAnyRole(...)`.

**Origen:** `SecurityConfig.java` (líneas 52-62).
