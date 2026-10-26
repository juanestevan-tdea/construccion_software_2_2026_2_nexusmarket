# Requisitos Funcionales

Todos los requisitos se derivan de los endpoints reales de `adapters/rest/controllers/`. Cada uno indica el **actor**, el **caso de uso** (`domain/ports/in/*UseCasePort`) y los **criterios de aceptación** (CAC). Los códigos de error se rigen por `adapters/rest/exception/GlobalExceptionHandler.java` (400 validación, 401 auth, 403 forbidden, 404 not found, 409 conflict, 422 unprocessable, 500 fallback).

Los roles requeridos reflejan la aplicación efectiva de `infrastructure/security/SecurityConfig.java` y de `@PreAuthorize`:
- `PÚBLICO`: `/api/v1/auth/register`, `/api/v1/auth/login` y `/actuator/health`.
- `AUTENTICADO`: `/api/v1/admin/users/me`, `/api/v1/auth/logout`, `/api/v1/auth/change-password` y `GET /api/v1/catalog/**`.
- `ADMIN`: `/api/v1/admin/**` (excepto `/users/me`).
- `BUYER`: `/api/v1/buyer/**`.
- `SELLER`: `/api/v1/seller/**`.
- `SUPERVISOR`: `/api/v1/supervisor/**`.
- `WAREHOUSE_OPERATOR`: `/api/v1/warehouse/**`.

> La autorización se aplica por ruta (`SecurityConfig`) y por método (`@EnableMethodSecurity`). Además, los controladores de comprador y vendedor verifican la propiedad del recurso contra el usuario del token.
---

## Módulo: Autenticación (auth)

### RF-001 — Registro de usuario
- **Actor:** visitante no autenticado.
- **Endpoint:** `POST /api/v1/auth/register` (`AuthController.register`).
- **Caso de uso:** `AuthenticationUseCasePort.register(RegisterCommand)` → `AuthResponseDTO`.
- **Criterios de aceptación:**
  1. Devuelve `201 Created` con `AuthResponseDTO` (token `Bearer`, `expiresIn = 86400000` ms / 24 h, rol `BUYER` o `SELLER`).
  2. Email duplicado → `409 Conflict` (`UserEmailAlreadyExistsException`).
  3. Datos inválidos (email mal formado, contraseña < 8 caracteres, rol nulo) → `400 Bad Request` (`MethodArgumentNotValidException`).
  4. Endpoint público, sin token requerido.

### RF-002 — Login y obtención de JWT
- **Actor:** usuario registrado.
- **Endpoint:** `POST /api/v1/auth/login` (`AuthController.login`).
- **Caso de uso:** `AuthenticationUseCasePort.login(LoginCommand)` → `AuthResponseDTO`.
- **Criterios de aceptación:**
  1. Credenciales válidas → `200 OK` con JWT firmado HMAC-SHA256 y rol incrustado.
  2. Credenciales inválidas → `401 Unauthorized` (`AuthenticationException`).
  3. Endpoint público.

---

## Módulo: Usuarios (users)

### RF-003 — Crear usuario
- **Actor:** `ADMIN`.
- **Endpoint:** `POST /api/v1/admin/users` (`UserController.createUser`).
- **Caso de uso:** `UserUseCasePort.createUser(email, fullName, password, role)` → `User`.
- **Criterios de aceptación:**
  1. Devuelve `201 Created` con `UserResponseDTO`.
  2. Rol obligatorio y dentro de `UserRole` → si no, `400 Bad Request`.
  3. Email duplicado → `409 Conflict` (`UserEmailAlreadyExistsException`).

### RF-004 — Consultar perfil propio
- **Actor:** cualquier usuario autenticado.
- **Endpoint:** `GET /api/v1/admin/users/me` (`UserController.getCurrentUser`).
- **Caso de uso:** lectura del `UserDetailsAdapter` del `SecurityContextHolder` (no hay método dedicado en el port; se resuelve la autenticación vigente).
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `UserResponseDTO` del usuario autenticado.
  2. Sin sesión válida → `401 Unauthorized`.

### RF-005 — Gestión de usuarios por administrador
- **Actor:** `ADMIN`.
- **Endpoints:** `GET /api/v1/admin/users/{id}`, `GET /api/v1/admin/users/email?email=`, `GET /api/v1/admin/users`, `GET /api/v1/admin/users/role/{role}`, `PATCH /api/v1/admin/users/{id}/block`, `PATCH /api/v1/admin/users/{id}/activate`, `PATCH /api/v1/admin/users/{id}/role?newRole=`.
- **Caso de uso:** `UserUseCasePort.getUserByIdOrThrow`, `getUserByEmailOrThrow`, `findAll`, `findByRole`, `blockUser`, `activateUser`, `changeRole`.
- **Criterios de aceptación:**
  1. Cada consulta devuelve `200 OK` con `UserResponseDTO` (o lista).
  2. Usuario inexistente → `404 Not Found` (`ResourceNotFoundException`).
  3. Rol no válido en path → `400 Bad Request` (`MethodArgumentTypeMismatchException`).
  4. Actor sin rol `ADMIN` → `403 Forbidden`.

---

## Módulo: Compradores (buyers)

### RF-006 — Crear comprador
- **Actor:** `BUYER` (dueño del recurso).
- **Endpoint:** `POST /api/v1/buyer/buyers` (`BuyerController.createBuyer`).
- **Caso de uso:** `BuyerUseCasePort.createBuyer(userId, primaryAddress)` → `Buyer`.
- **Criterios de aceptación:**
  1. Devuelve `201 Created` con `BuyerResponseDTO` (incluye datos del `User` asociado).
  2. `userId` obligatorio y `primaryAddress` no blank → `400 Bad Request`.
  3. Usuario inexistente → `404 Not Found`.

### RF-007 — Consultar y gestionar comprador
- **Actor:** `BUYER` (dueño del recurso).
- **Endpoints:** `GET /api/v1/buyer/buyers/{id}`, `GET /api/v1/buyer/buyers`, `GET /api/v1/buyer/buyers/status/{status}`, `POST /api/v1/buyer/buyers/{id}/addresses?address=`.
- **Caso de uso:** `BuyerUseCasePort.getBuyerByIdOrThrow`, `findAll`, `findByCommercialStatus`, `addAdditionalAddress`.
- **Criterios de aceptación:**
  1. Lecturas devuelven `200 OK` con `BuyerResponseDTO` (o lista).
  2. `BuyerCommercialStatus` inválido en path → `400 Bad Request`.
  3. Comprador inexistente → `404 Not Found`.
- **Administración:** `changeCommercialStatus` solo es accesible por `ADMIN` mediante `PATCH /api/v1/admin/buyers/{id}/status?newStatus=` (`BuyerManagementController`). Antes vivía en la ruta del comprador y permitía auto-reactivarse.

---

## Módulo: Vendedores (sellers)

### RF-008 — Crear vendedor
- **Actor:** `SELLER` (dueño del recurso).
- **Endpoint:** `POST /api/v1/seller/sellers` (`SellerController.createSeller`).
- **Caso de uso:** `SellerUseCasePort.createSeller(userId, taxId, companyName)` → `Seller`.
- **Criterios de aceptación:**
  1. Devuelve `201 Created` con `SellerResponseDTO` (incluye `User` asociado).
  2. `userId`, `taxId` y `companyName` obligatorios → `400 Bad Request`.
  3. Usuario inexistente → `404 Not Found`.

### RF-009 — Consultar y gestionar vendedor
- **Actor:** `SELLER` (dueño del recurso).
- **Endpoints:** `GET /api/v1/seller/sellers/{id}`, `GET /api/v1/seller/sellers/taxId?taxId=`, `GET /api/v1/seller/sellers`, `PATCH /api/v1/seller/sellers/{id}/activate`, `PATCH /api/v1/seller/sellers/{id}/deactivate`, `PATCH /api/v1/seller/sellers/{id}/update`.
- **Caso de uso:** `SellerUseCasePort.getSellerByIdOrThrow`, `getSellerByTaxIdOrThrow`, `findAll`, `activateSeller`, `deactivateSeller`, `updateSeller`.
- **Criterios de aceptación:**
  1. Lecturas devuelven `200 OK` con `SellerResponseDTO` (o lista).
  2. `taxId` duplicado → `409 Conflict` (`SellerTaxIdAlreadyExistsException`).
  3. Vendedor inexistente → `404 Not Found`.

---

## Módulo: Catálogo — productos (catalog)

### RF-010 — Crear producto
- **Actor:** `SELLER` (dueño del producto).
- **Endpoint:** `POST /api/v1/seller/products` (`ProductController.createProduct`).
- **Caso de uso:** `ProductUseCasePort.createProduct(sku, name, description, price, type, sellerIdDelToken, categoryId)` → `Product`.
- **Criterios de aceptación:**
  1. Devuelve `201 Created` con `ProductResponseDTO`.
  2. SKU duplicado → `409 Conflict` (`ProductSkuAlreadyExistsException`).
  3. Precio ≤ 0 → `400 Bad Request` (`@DecimalMin("0.01")`).
  4. Categoría o vendedor inexistentes → `404 Not Found`.

### RF-011 — Consultar producto por ID
- **Actor:** `SELLER` (dueño del recurso).
- **Endpoint:** `GET /api/v1/seller/products/{id}` (`ProductController.getProductById`).
- **Caso de uso:** `ProductUseCasePort.getProductByIdOrThrow(id)` → `Product`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `ProductResponseDTO`.
  2. Producto inexistente → `404 Not Found`.

### RF-012 — Listar y filtrar productos
- **Actor:** `SELLER` (dueño del recurso).
- **Endpoints:** `GET /api/v1/seller/products`, `GET /api/v1/seller/products/category/{categoryId}`, `GET /api/v1/seller/products/seller/{sellerId}`, `GET /api/v1/seller/products/price-range?min=&max=`.
- **Caso de uso:** `ProductUseCasePort.findAll`, `findByCategory`, `findBySeller`, `findByPriceRange`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con lista de `ProductResponseDTO`.
  2. Parámetros `min`/`max` obligatorios y numéricos → `400 Bad Request` si faltan o mal formados.

### RF-013 — Actualizar producto
- **Actor:** `SELLER` (dueño del recurso).
- **Endpoint:** `PATCH /api/v1/seller/products/{id}` (`ProductController.updateProduct`).
- **Caso de uso:** `ProductUseCasePort.updateProduct(id, name, description, price, type, categoryId)` → `Product`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `ProductResponseDTO` actualizado.
  2. Producto inexistente → `404 Not Found`.
  3. Precio inválido si se envía → `400 Bad Request`.

### RF-014 — Desactivar producto (soft delete)
- **Actor:** `SELLER` (dueño del recurso).
- **Endpoint:** `DELETE /api/v1/seller/products/{id}` (`ProductController.deleteProduct`).
- **Caso de uso:** `ProductUseCasePort.deactivateProduct(id)` → `Product` (`active = false`).
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `ProductResponseDTO` desactivado.
  2. Producto inexistente → `404 Not Found`.

---

## Módulo: Catálogo — categorías (catalog)

### RF-015 — Crear categoría
- **Actor:** `ADMIN`.
- **Endpoint:** `POST /api/v1/admin/categories` (`CategoryController.createCategory`).
- **Caso de uso:** `CategoryUseCasePort.createCategory(name, description, parentId)` → `Category`.
- **Criterios de aceptación:**
  1. Devuelve `201 Created` con `CategoryResponseDTO`.
  2. Nombre duplicado → `409 Conflict` (`CategoryNameAlreadyExistsException`).
  3. Nombre obligatorio → `400 Bad Request`.

### RF-016 — Consultar categorías
- **Actor:** `ADMIN`.
- **Endpoints:** `GET /api/v1/admin/categories`, `GET /api/v1/admin/categories/roots`, `GET /api/v1/admin/categories/{id}`.
- **Caso de uso:** `CategoryUseCasePort.findAll`, `findRootCategories`, `getCategoryByIdOrThrow`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con lista o `CategoryResponseDTO`.
  2. Categoría inexistente → `404 Not Found`.

### RF-017 — Actualizar y eliminar categoría
- **Actor:** `ADMIN`.
- **Endpoints:** `PATCH /api/v1/admin/categories/{id}`, `DELETE /api/v1/admin/categories/{id}`.
- **Caso de uso:** `CategoryUseCasePort.updateCategory(id, name, description, parentId)`, `deleteCategory(id)`.
- **Criterios de aceptación:**
  1. `PATCH` → `200 OK` con `CategoryResponseDTO` actualizado.
  2. `DELETE` → `204 No Content`.
  3. Categoría con productos asociados → `409 Conflict` (`CategoryHasProductsException`).
  4. Categoría inexistente → `404 Not Found`.
  5. Actualización que crea un ciclo en la jerarquía → `422 Unprocessable Entity` (`BusinessRuleException`).
  6. Eliminación de una categoría con subcategorías → `422 Unprocessable Entity` (`BusinessRuleException`).

---

## Módulo: Catálogo — bodegas (catalog)

### RF-018 — CRUD de bodegas
- **Actor:** `WAREHOUSE_OPERATOR`.
- **Endpoints:** `POST /api/v1/warehouse/warehouses`, `GET /api/v1/warehouse/warehouses`, `GET /api/v1/warehouse/warehouses/{id}`, `GET /api/v1/warehouse/warehouses/type/{type}`, `PATCH /api/v1/warehouse/warehouses/{id}`.
- **Caso de uso:** `WarehouseUseCasePort.createWarehouse`, `getWarehouseByIdOrThrow`, `findAll`, `findByType`, `updateWarehouse`.
- **Criterios de aceptación:**
  1. `POST` → `201 Created` con `WarehouseResponseDTO`.
  2. Lecturas → `200 OK`.
  3. `PATCH` → `200 OK` con bodega actualizada.
  4. Capacidad < 1 → `400 Bad Request` (`@Min(1)`).
  5. Bodega inexistente → `404 Not Found`.
  6. Nombre duplicado → `409 Conflict` (`WarehouseNameAlreadyExistsException`).

---

## Módulo: Catálogo general y búsqueda (catalog)

### RF-019 — Catálogo y búsqueda de productos
- **Actor:** cualquier usuario autenticado.
- **Endpoints:** `GET /api/v1/catalog`, `GET /api/v1/catalog/search?query=`, `GET /api/v1/catalog/products/{id}`.
- **Caso de uso:** `CatalogUseCasePort.getActiveProducts`, `getRootCategories`, `searchProducts(query)`, `getProductDetail(id)`.
- **Criterios de aceptación:**
  1. `GET /api/v1/catalog` → `200 OK` con `CatalogOverviewResponseDTO` (productos activos + categorías raíz + totales).
  2. `GET /api/v1/catalog/search` → `200 OK` con lista de productos que coincidan con `query`.
  3. Producto de detalle inexistente → `404 Not Found`.

---

## Módulo: Inventario (inventory)

### RF-020 — Crear registro de inventario
- **Actor:** `WAREHOUSE_OPERATOR`.
- **Endpoint:** `POST /api/v1/warehouse/inventories` (`InventoryController.createInventory`).
- **Caso de uso:** `InventoryUseCasePort.createInventory(productId, warehouseId, quantity)` → `Inventory`.
- **Criterios de aceptación:**
  1. Devuelve `201 Created` con `InventoryResponseDTO`.
  2. `productId`, `warehouseId`, `quantity` obligatorios (`quantity >= 0`) → `400 Bad Request`.

### RF-021 — Consultar inventario
- **Actor:** `WAREHOUSE_OPERATOR`.
- **Endpoints:** `GET /api/v1/warehouse/inventories/{id}`, `GET /api/v1/warehouse/inventories`, `GET /api/v1/warehouse/inventories/product/{productId}`, `GET /api/v1/warehouse/inventories/warehouse/{warehouseId}`.
- **Caso de uso:** `InventoryUseCasePort.getByIdOrThrow`, `findAll`, `findByProduct`, `findByWarehouse`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `InventoryResponseDTO` (o lista).
  2. Registro inexistente → `404 Not Found`.

### RF-022 — Operaciones de stock (reserva, pago, dañado, ajuste)
- **Actor:** `WAREHOUSE_OPERATOR`.
- **Endpoints:** `PATCH /api/v1/warehouse/inventories/{id}/reserve?amount=`, `PATCH /api/v1/warehouse/inventories/{id}/confirm-payment`, `PATCH /api/v1/warehouse/inventories/{id}/mark-damaged`, `PATCH /api/v1/warehouse/inventories/{id}/adjust?amount=`.
- **Caso de uso:** `InventoryUseCasePort.reserve`, `confirmPayment`, `markAsDamaged`, `adjust`.
- **Criterios de aceptación:**
  1. Cada operación devuelve `200 OK` con `InventoryResponseDTO` actualizado.
  2. Reserva con cantidad `<= 0` → `422 Unprocessable Entity` (`InvalidQuantityException`).
  3. Reserva con cantidad > disponible → `422 Unprocessable Entity` (`InsufficientStockException`).
  4. Operar inventario `DAMAGED` → `422 Unprocessable Entity` (`DamagedInventoryException`).
  5. Ajuste que deja cantidad negativa → `422 Unprocessable Entity` (`InvalidQuantityException` "Quantity cannot be negative").
  6. Ajuste o liberación con cantidad no inicializada → `422 Unprocessable Entity` (`InventoryNotInitializedException`).
  7. Inventario inexistente → `404 Not Found`.

---

## Módulo: Órdenes (orders)

### RF-023 — Crear orden
- **Actor:** `BUYER`.
- **Endpoint:** `POST /api/v1/buyer/orders` (`OrderController.createOrder`).
- **Caso de uso:** `OrderUseCasePort.createOrder(buyerIdDelToken)` → `Order` (estado `CART`).
- **Criterios de aceptación:**
  1. Devuelve `201 Created` con `OrderResponseDTO` (estado `CART`, ítems vacíos).
  2. Comprador inexistente → `404 Not Found`.

### RF-024 — Agregar y quitar ítems de orden
- **Actor:** `BUYER` (dueño de la orden).
- **Endpoints:** `POST /api/v1/buyer/orders/{id}/items`, `DELETE /api/v1/buyer/orders/{id}/items/{itemId}`.
- **Caso de uso:** `OrderUseCasePort.addItem(orderId, productId, quantity)`, `removeItem(orderId, itemId)`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `OrderResponseDTO` actualizado y `totalAmount` recalculado.
  2. Solo permitido si la orden está en `CART` o `PENDING_PAYMENT` → `409 Conflict` (`InvalidStatusTransitionException`) en otro estado.
  3. Producto inexistente → `404 Not Found`.
  4. Ítem inexistente → `404 Not Found`.
  5. Producto inactivo → `422 Unprocessable Entity` (`ProductNotActiveException`).
  6. Cantidad no disponible → `422 Unprocessable Entity` (`InsufficientStockException`).

### RF-025 — Checkout de orden
- **Actor:** `BUYER`.
- **Endpoint:** `PATCH /api/v1/buyer/orders/{id}/checkout` (`OrderController.checkout`).
- **Caso de uso:** `OrderUseCasePort.checkout(id)` → `Order` (`PENDING_PAYMENT`).
- **Criterios de aceptación:**
  1. Orden en `CART` con ítems → `200 OK`, estado pasa a `PENDING_PAYMENT`.
  2. Orden no en `CART` → `409 Conflict` (`InvalidStatusTransitionException`).
  3. Carrito vacío → `409 Conflict` (`InvalidStatusTransitionException`).

### RF-026 — Confirmar pago de orden
- **Actor:** `BUYER`.
- **Endpoint:** `PATCH /api/v1/buyer/orders/{id}/confirm-payment` (`OrderController.confirmPayment`).
- **Caso de uso:** `OrderUseCasePort.confirmPayment(id)` → `Order` (`PAID`).
- **Criterios de aceptación:**
  1. Orden en `PENDING_PAYMENT` → `200 OK`, estado pasa a `PAID`.
  2. Otra estado → `409 Conflict` (`InvalidStatusTransitionException`).

### RF-027 — Despachar, entregar y finalizar orden
- **Actor:** `WAREHOUSE_OPERATOR`.
- **Endpoints:** `PATCH /api/v1/warehouse/orders/{id}/dispatch`, `PATCH /api/v1/warehouse/orders/{id}/deliver`, `PATCH /api/v1/warehouse/orders/{id}/finish` (`WarehouseOrderController`).
- **Caso de uso:** `OrderUseCasePort.dispatch`, `deliver`, `finish`.
- **Criterios de aceptación:**
  1. `dispatch`: requiere `PAID` → `DISPATCHED`.
  2. `deliver`: requiere `DISPATCHED` → `DELIVERED`.
  3. `finish`: requiere `DELIVERED` → `FINISHED`.
  4. Cualquier transición inválida → `409 Conflict` (`InvalidStatusTransitionException`).

### RF-028 — Cancelar orden
- **Actor:** `BUYER`.
- **Endpoint:** `PATCH /api/v1/buyer/orders/{id}/cancel` (`OrderController.cancel`).
- **Caso de uso:** `OrderUseCasePort.cancel(id)` → `Order` (`CANCELLED`).
- **Criterios de aceptación:**
  1. Orden no `FINISHED` ni `DELIVERED` → `200 OK`, estado pasa a `CANCELLED`.
  2. Orden `FINISHED` o `DELIVERED` → `409 Conflict` (`InvalidStatusTransitionException`).

### RF-029 — Consultar órdenes
- **Actor:** `BUYER` (dueño del recurso).
- **Endpoints:** `GET /api/v1/buyer/orders/{id}`, `GET /api/v1/buyer/orders`, `GET /api/v1/buyer/orders/buyer/{buyerId}`.
- **Caso de uso:** `OrderUseCasePort.getByIdOrThrow`, `findByBuyer` (scoped al comprador autenticado).
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `OrderResponseDTO` (o lista).
  2. Orden inexistente → `404 Not Found`.

---

## Módulo: Facturación (billing / invoices)

### RF-030 — Generar factura
- **Actor:** `SUPERVISOR`.
- **Endpoint:** `POST /api/v1/supervisor/invoices` (`InvoiceController.generateInvoice`).
- **Caso de uso:** `InvoiceUseCasePort.generateInvoice(orderId, pdfUrl)` → `Invoice`.
- **Criterios de aceptación:**
  1. Devuelve `201 Created` con `InvoiceResponseDTO`.
  2. Ya existe factura activa para la orden → `409 Conflict` (`InvoiceAlreadyExistsException`).
  3. Orden inexistente → `404 Not Found`.

### RF-031 — Consultar facturas
- **Actor:** `SUPERVISOR`.
- **Endpoints:** `GET /api/v1/supervisor/invoices/{id}`, `GET /api/v1/supervisor/invoices/order/{orderId}`, `GET /api/v1/supervisor/invoices`.
- **Caso de uso:** `InvoiceUseCasePort.getByIdOrThrow`, `findByOrder`, `findAll`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `InvoiceResponseDTO` (o lista).
  2. Factura inexistente → `404 Not Found`.

### RF-032 — Anular factura
- **Actor:** `SUPERVISOR`.
- **Endpoint:** `PATCH /api/v1/supervisor/invoices/{id}/void` (`InvoiceController.voidInvoice`).
- **Caso de uso:** `InvoiceUseCasePort.voidInvoice(id)` → `Invoice`.
- **Criterios de aceptación:**
  1. Factura pagable → `200 OK` con factura anulada.
  2. Factura ya pagada o anulada → `422 Unprocessable Entity` (`InvoiceNotPayableException`).

---

## Módulo: Reembolsos (refunds)

### RF-033 — Crear reembolso
- **Actor:** `SUPERVISOR`.
- **Endpoint:** `POST /api/v1/supervisor/refunds` (`RefundController.createRefund`).
- **Caso de uso:** `RefundUseCasePort.createRefund(invoiceId, returnRequestId, amount)` → `Refund` (estado `PENDING`).
- **Criterios de aceptación:**
  1. Devuelve `201 Created` con `RefundResponseDTO`.
  2. Monto `<= 0` → `422 Unprocessable Entity` (`InvalidRefundAmountException`).
  3. Monto individual o acumulado (`PENDING`/`APPROVED`/`COMPLETED`) mayor al total facturado → `422 Unprocessable Entity` (`RefundAmountExceededException`).
  4. Factura inactiva o impaga → `422 Unprocessable Entity` (`InvoiceNotRefundableException`).
  5. Factura o devolución inexistente → `404 Not Found`; devolución de otra orden → `422 Unprocessable Entity` (`RefundNotAllowedException`).

### RF-034 — Consultar reembolsos
- **Actor:** `SUPERVISOR`.
- **Endpoints:** `GET /api/v1/supervisor/refunds/{id}`, `GET /api/v1/supervisor/refunds/invoice/{invoiceId}`, `GET /api/v1/supervisor/refunds`.
- **Caso de uso:** `RefundUseCasePort.getByIdOrThrow`, `findByInvoice`, `findAll`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `RefundResponseDTO` (o lista).
  2. Reembolso inexistente → `404 Not Found`.

### RF-035 — Aprobar y rechazar reembolso
- **Actor:** `SUPERVISOR`.
- **Endpoints:** `PATCH /api/v1/supervisor/refunds/{id}/approve`, `PATCH /api/v1/supervisor/refunds/{id}/reject`.
- **Caso de uso:** `RefundUseCasePort.approveRefund(id)`, `rejectRefund(id)`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `RefundResponseDTO` estado `APPROVED` / `REJECTED`.
  2. Reembolso inexistente → `404 Not Found`.
  3. Reembolso ya procesado → `409 Conflict` (`InvalidStatusTransitionException`).

---

## Módulo: Envíos (shipping / shipments)

### RF-036 — Crear envío
- **Actor:** `WAREHOUSE_OPERATOR`.
- **Endpoint:** `POST /api/v1/warehouse/shipments` (`ShipmentController.createShipment`).
- **Caso de uso:** `ShipmentUseCasePort.createShipment(orderId, warehouseId, trackingNumber)` → `Shipment` (estado `PENDING`).
- **Criterios de aceptación:**
  1. Devuelve `201 Created` con `ShipmentResponseDTO`.
  2. Ya existe envío para la orden → `409 Conflict` (`ShipmentAlreadyExistsException`).
  3. Orden inexistente → `404 Not Found`.

### RF-037 — Consultar envío
- **Actor:** `WAREHOUSE_OPERATOR`.
- **Endpoints:** `GET /api/v1/warehouse/shipments/{id}`, `GET /api/v1/warehouse/shipments/tracking/{trackingNumber}`, `GET /api/v1/warehouse/shipments/order/{orderId}`, `GET /api/v1/warehouse/shipments`.
- **Caso de uso:** `ShipmentUseCasePort.getByIdOrThrow`, `findByTracking`, `findByOrder`, `findAll`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `ShipmentResponseDTO` (o lista).
  2. Envío/tracking inexistente → `404 Not Found` (`TrackingNotFoundException` para tracking).

### RF-038 — Actualizar estado de envío
- **Actor:** `WAREHOUSE_OPERATOR`.
- **Endpoint:** `PATCH /api/v1/warehouse/shipments/{id}/status` (`ShipmentController.updateStatus`).
- **Caso de uso:** `ShipmentUseCasePort.updateStatus(id, ShipmentStatus)` → `Shipment`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `ShipmentResponseDTO` actualizado.
  2. `status` obligatorio → `400 Bad Request`.
  3. Envío inexistente → `404 Not Found`.

---

## Módulo: Devoluciones (returns)

### RF-039 — Crear devolución
- **Actor:** `BUYER`.
- **Endpoint:** `POST /api/v1/buyer/returns` (`ReturnController.createReturn`).
- **Caso de uso:** `ReturnUseCasePort.createReturn(orderId, reason)` → `Return` (estado `REQUESTED`).
- **Criterios de aceptación:**
  1. Devuelve `201 Created` con `ReturnResponseDTO`.
  2. `orderId` y `reason` obligatorios → `400 Bad Request`.
  3. Orden inexistente → `404 Not Found`.
  4. Fuera de ventana de devolución → `422 Unprocessable Entity` (`ReturnWindowExpiredException`).

### RF-040 — Consultar devoluciones
- **Actor:** `BUYER` (dueño del recurso).
- **Endpoints:** `GET /api/v1/buyer/returns/{id}`, `GET /api/v1/buyer/returns/order/{orderId}`, `GET /api/v1/buyer/returns`.
- **Caso de uso:** `ReturnUseCasePort.getByIdOrThrow`, `findByOrder`, `findAll`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `ReturnResponseDTO` (o lista).
  2. Devolución inexistente → `404 Not Found`.

### RF-041 — Aprobar, rechazar y completar devolución
- **Actor:** `SUPERVISOR`.
- **Endpoints:** `PATCH /api/v1/supervisor/returns/{id}/approve`, `PATCH /api/v1/supervisor/returns/{id}/reject`, `PATCH /api/v1/supervisor/returns/{id}/complete`.
- **Caso de uso:** `ReturnUseCasePort.approveReturn`, `rejectReturn`, `completeReturn`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con `ReturnResponseDTO` estado `APPROVED` / `REJECTED` / `COMPLETED`.
  2. Devolución ya procesada → `409 Conflict` (`ReturnAlreadyProcessedException`).
  3. Devolución inexistente → `404 Not Found`.

---

## Módulo: Auditoría (auditing)

### RF-042 — Registrar log de auditoría
- **Actor:** sistema (interno de los casos de uso).
- **Caso de uso:** `AuditUseCasePort.log(userId, performedBy, entityName, entityId, action, details)` → `AuditLog` (persistido en MongoDB).
- **Criterios de aceptación:**
  1. Cada operación de negocio registra un `AuditLog` con timestamp y entidad afectada.
  2. Almacenamiento inmutable en MongoDB (`AuditLogMongoAdapter`).

### RF-043 — Consultar logs de auditoría
- **Actor:** `ADMIN`.
- **Endpoints:** `GET /api/v1/admin/audit?userId=&entityName=&start=&end=`, `GET /api/v1/admin/audit/{id}`.
- **Caso de uso:** `AuditUseCasePort.findAll`, `findByUser`, `findByEntity`, `findByDateRange`, `getByIdOrThrow`.
- **Criterios de aceptación:**
  1. Devuelve `200 OK` con lista de `AuditLogResponseDTO` (filtrada o completa) o un solo log.
  4. Actor sin rol `ADMIN`/`SUPERVISOR` → `403 Forbidden`.
  5. Log inexistente → `404 Not Found`.
