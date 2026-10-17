# Glosario de términos de dominio

Todos los términos se toman del código real de `domain/models/`, `domain/valueobjects/` y `domain/ports/`.

| Término | Clase / enum real | Descripción |
|---------|-------------------|-------------|
| Usuario | `domain/models/User.java`; `domain/valueobjects/UserRole.java`; `domain/valueobjects/UserStatus.java` | Usuario del sistema con email, nombre completo, contraseña (BCrypt), rol y estado. Rol: `BUYER`, `SELLER`, `WAREHOUSE_OPERATOR`, `ADMIN`, `SUPERVISOR`. Estado: `ACTIVE`, `BLOCKED`. |
| Comprador | `domain/models/Buyer.java`; `domain/valueobjects/BuyerCommercialStatus.java` | Perfil de comprador vinculado a un `User`, con dirección primaria, direcciones adicionales y estado comercial (`ACTIVE`, `INACTIVE`, `BLOCKED`). |
| Vendedor | `domain/models/Seller.java` | Perfil de vendedor vinculado a un `User`, con `taxId` y `companyName`; activo/inactivo. |
| Producto | `domain/models/Product.java`; `domain/valueobjects/ProductType.java` | Artículo del catálogo con SKU único, nombre, descripción, precio, tipo (`PHYSICAL`/`DIGITAL`), vendedor y categoría. Activo por defecto. |
| SKU | `Product.sku` (`domain/models/Product.java`) | Identificador único de producto; valida unicidad vía `ProductRepositoryPort.existsBySku`. |
| Categoría | `domain/models/Category.java` | Categoría jerárquica con nombre, descripción, `parentId` opcional y lista de subcategorías. |
| Bodega | `domain/models/Warehouse.java`; `domain/valueobjects/WarehouseType.java` | Almacén con nombre, ubicación, tipo (`MARKETPLACE`/`SELLER`) y capacidad. |
| Inventario | `domain/models/Inventory.java`; `domain/valueobjects/InventoryStatus.java` | Stock de un producto en una bodega (`quantity`, `productId`, `warehouseId`). Estado: `AVAILABLE`, `DAMAGED`, `RESERVED`. |
| Orden | `domain/models/Order.java`; `domain/valueobjects/OrderStatus.java` | Orden de compra con ítems, email del comprador, fecha y monto total. Ciclo: `CART → PENDING_PAYMENT → PAID → DISPATCHED → DELIVERED → FINISHED`, o `CANCELLED`. |
| Ítem de orden | `domain/models/OrderItem.java` | Línea de una orden: producto, cantidad, precio unitario y subtotal. |
| Factura | `domain/models/Invoice.java` | Factura vinculada a una orden, con número de factura, fecha de emisión, monto, URL del PDF y flags `active` / `paid`. |
| Reembolso | `domain/models/Refund.java`; `domain/valueobjects/RefundStatus.java` | Devolución de dinero vinculada a una factura (y opcionalmente a una devolución), con monto. Estado: `PENDING`, `APPROVED`, `REJECTED`, `COMPLETED`, `FAILED`. |
| Devolución | `domain/models/Return.java`; `domain/valueobjects/ReturnStatus.java` | Solicitud de devolución de una orden con motivo, fecha de solicitud y resolución. Estado: `REQUESTED`, `APPROVED`, `REJECTED`, `COMPLETED`, `PROCESSED`. |
| Envío | `domain/models/Shipment.java`; `domain/valueobjects/ShipmentStatus.java` | Envío de una orden con número de tracking, bodega y fechas. Estado: `PENDING`, `IN_TRANSIT`, `DELIVERED`, `CANCELLED`. |
| Registro de auditoría | `domain/models/AuditLog.java` | Log inmutable de operación en MongoDB: usuario, ejecutor, entidad, ID de entidad, acción, detalles y timestamp. |
| Rol | `domain/valueobjects/UserRole.java` | Rol de seguridad usado para autorización: `BUYER`, `SELLER`, `WAREHOUSE_OPERATOR`, `ADMIN`, `SUPERVISOR`. |
| Puerto de entrada (Port In) | `domain/ports/in/*UseCasePort.java` | Interfaz de caso de uso que expone una operación de negocio al adaptador REST (ej. `OrderUseCasePort`). |
| Puerto de salida (Port Out) | `domain/ports/out/*RepositoryPort.java` | Interfaz de persistencia que el dominio usa para guardar/consultar agregados (ej. `OrderRepositoryPort`). |
| Servicio de dominio | `domain/services/*Service.java` | Clase con reglas de negocio puras orquestadas por los casos de uso (ej. `OrderCreateService`, `RefundProcessService`). |
| Excepción de negocio | `domain/exceptions/*Exception.java` | Excepción tipificada para cada caso de fallo (ej. `ResourceNotFoundException`, `BusinessRuleException`, `InvalidStatusTransitionException`). |
