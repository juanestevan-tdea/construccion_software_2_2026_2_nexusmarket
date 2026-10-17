# Eventos de Dominio

Los eventos de dominio se identifican a partir de las operaciones de negocio implementadas. Aunque **no están implementados como eventos publicados todavía** (no hay un event bus ni `@EventListener` en el código actual), se listan aquí como candidatos de diseño, con su nombre, payload y los módulos que los consumirían.

La auditoría actual se implementa de forma síncrona mediante `AuditUseCasePort.log(...)` persistido en MongoDB (`AuditLogMongoAdapter`); los eventos de dominio permitirían desacoplar esa reacción (notificaciones, sincronización de inventario, etc.) mediante publicación asíncrona.

| ID | Evento | Módulo que lo publica | Payload (campos) | Módulos consumidores candidatos |
|----|--------|-----------------------|------------------|---------------------------------|
| EVT-001 | `UsuarioRegistrado` | auth | `userId`, `email`, `fullName`, `role`, `timestamp` | notifications (email de bienvenida), auditing |
| EVT-002 | `UsuarioAutenticado` | auth | `userId`, `email`, `role`, `timestamp`, `ip` | auditing, security |
| EVT-003 | `ProductoCreado` | catalog | `productId`, `sku`, `name`, `price`, `sellerId`, `categoryId`, `timestamp` | catalog (búsqueda), notifications |
| EVT-004 | `ProductoDesactivado` | catalog | `productId`, `sku`, `timestamp` | catalog, inventory |
| EVT-005 | `CategoriaCreada` | catalog | `categoryId`, `name`, `parentId`, `timestamp` | catalog |
| EVT-006 | `BodegaCreada` | catalog | `warehouseId`, `name`, `location`, `type`, `capacity`, `timestamp` | inventory, shipping |
| EVT-007 | `InventarioCreado` | inventory | `inventoryId`, `productId`, `warehouseId`, `quantity`, `timestamp` | inventory, orders |
| EVT-008 | `StockReservado` | inventory | `inventoryId`, `productId`, `warehouseId`, `amountReserved`, `quantityRemaining`, `timestamp` | orders, shipping |
| EVT-009 | `StockDañado` | inventory | `inventoryId`, `productId`, `warehouseId`, `timestamp` | inventory, returns |
| EVT-010 | `OrdenCreada` | orders | `orderId`, `buyerId`, `buyerEmail`, `status`, `orderDate`, `timestamp` | invoicing, shipping |
| EVT-011 | `ÍtemAñadidoAOrden` | orders | `orderId`, `productId`, `quantity`, `totalAmount`, `timestamp` | orders, inventory |
| EVT-012 | `OrdenEnPagoPendiente` | orders | `orderId`, `buyerId`, `totalAmount`, `timestamp` | invoicing, payments |
| EVT-013 | `PagoConfirmado` | orders | `orderId`, `buyerId`, `totalAmount`, `timestamp` | invoicing, shipping, inventory (confirm-payment) |
| EVT-014 | `OrdenDespachada` | orders | `orderId`, `buyerId`, `timestamp` | shipping, notifications |
| EVT-015 | `OrdenEntregada` | orders | `orderId`, `buyerId`, `timestamp` | shipping, returns (inicia ventana) |
| EVT-016 | `OrdenFinalizada` | orders | `orderId`, `buyerId`, `timestamp` | auditing |
| EVT-017 | `OrdenCancelada` | orders | `orderId`, `buyerId`, `timestamp` | inventory (liberar reserva), refunds |
| EVT-018 | `FacturaGenerada` | billing | `invoiceId`, `orderId`, `invoiceNumber`, `amount`, `issueDate`, `timestamp` | refunds, notifications |
| EVT-019 | `FacturaAnulada` | billing | `invoiceId`, `orderId`, `timestamp` | refunds (bloquear nuevos reembolsos) |
| EVT-020 | `ReembolsoCreado` | refunds | `refundId`, `invoiceId`, `amount`, `status`, `timestamp` | notifications, auditing |
| EVT-021 | `ReembolsoAprobado` | refunds | `refundId`, `invoiceId`, `amount`, `timestamp` | notifications, payments |
| EVT-022 | `ReembolsoRechazado` | refunds | `refundId`, `invoiceId`, `amount`, `timestamp` | notifications |
| EVT-023 | `ReembolsoCompletado` | refunds | `refundId`, `invoiceId`, `amount`, `timestamp` | notifications, auditing |
| EVT-024 | `DevoluciónCreada` | returns | `returnId`, `orderId`, `reason`, `status`, `requestedAt`, `timestamp` | refunds (vincular returnRequestId), notifications |
| EVT-025 | `DevoluciónAprobada` | returns | `returnId`, `orderId`, `status`, `timestamp` | refunds, inventory |
| EVT-026 | `DevoluciónCompletada` | returns | `returnId`, `orderId`, `status`, `resolvedAt`, `timestamp` | inventory (reingresar stock), notifications |
| EVT-027 | `EnvíoCreado` | shipping | `shipmentId`, `orderId`, `trackingNumber`, `status`, `timestamp` | notifications, orders |
| EVT-028 | `EstadoEnvíoActualizado` | shipping | `shipmentId`, `orderId`, `status`, `timestamp` | notifications, orders |
| EVT-029 | `LogDeAuditoríaRegistrado` | auditing | `auditLogId`, `userId`, `entityName`, `entityId`, `action`, `timestamp` | auditing (consulta), security |
| EVT-030 | `UsuarioBloqueado` / `UsuarioActivado` | users | `userId`, `email`, `status`, `timestamp` | security (denegar token si BLOCKED) |

## Módulos consumidores (diseño propuesto)

| Módulo consumidor | Qué haría con los eventos |
|-------------------|---------------------------|
| **notifications** | Enviar emails/push de confirmación de pedido, pago, despacho, entrega, reembolso aprobado/rechazado. Hoy `NotificationPort`/`NoOpNotificationAdapter` es un stub sin conectar (gap en `SDD/README.md`). |
| **auditing** | Escribir `AuditLog` en MongoDB de forma reactiva (hoy es síncrono vía `AuditUseCasePort.log`). |
| **inventory** | Liberar reservas cuando una orden se cancela; reingresar stock cuando una devolución se completa. |
| **shipping** | Crear envío automático cuando una orden se paga; actualizar tracking. |
| **refunds** | Bloquear creación de reembolsos si la factura fue anulada; vincular `returnRequestId` al crear reembolso desde una devolución. |
| **payments** | (No implementado) Notificar a pasarela externa al confirmar pago o completar reembolso. |
