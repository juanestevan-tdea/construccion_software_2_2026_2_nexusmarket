﻿# 📘 Documentación Técnica - NexusMarket

## 🏗️ Proceso de Construcción del Software

### 1. Configuración Inicial del Proyecto

**1.1 Creación del Repositorio**
- Se creó un repositorio público en GitHub.
- Se clonó localmente usando `git clone`.
- Se estableció la rama `main` como rama principal.

**1.2 Estructura de Carpetas**
Se siguió el estándar de Maven para proyectos Spring Boot:
- `src/main/java`: Código fuente.
- `src/main/resources`: Archivos de configuración.
- `src/test/java`: Pruebas.
- `.mvn/wrapper`: Maven Wrapper.

**1.3 Configuración de Dependencias (pom.xml)**
Se incluyeron las siguientes dependencias:
- `spring-boot-starter-web`: Para la API REST.
- `spring-boot-starter-data-jpa`: Para persistencia con MySQL.
- `spring-boot-starter-data-mongodb`: Para persistencia con MongoDB.
- `lombok`: Para reducir código boilerplate.
- `spring-boot-starter-validation`: Para validaciones.
- `spring-boot-starter-test`: Para pruebas.

**1.4 Configuración de Bases de Datos (application.yml)**
Se configuraron dos fuentes de datos:
- **MySQL**: Para datos relacionales (usuarios, pedidos, inventario).
- **MongoDB**: Para datos documentales (catálogo enriquecido, carritos, logs).

**1.5 Maven Wrapper**
Se generó con `mvn wrapper:wrapper` para garantizar reproducibilidad del build.

---

### 2. Modelo de Dominio (Construcción de Entidades)

**2.1 Entidad User (Usuario)**
- **Propósito**: Representa a todos los usuarios del sistema.
- **Atributos**: `id`, `email`, `fullName`, `password`, `role`, `status`, `createdAt`, `updatedAt`.
- **Métodos de negocio**: `block()`, `activate()`, `isActive()`.

**2.2 Entidad Buyer (Comprador)**
- **Propósito**: Representa a los compradores.
- **Relación**: `@OneToOne` con `User`.
- **Atributos**: `primaryAddress`, `additionalAddresses`, `commercialStatus`.

**2.3 Entidad Seller (Vendedor)**
- **Propósito**: Representa a los vendedores.
- **Relación**: `@OneToOne` con `User`.
- **Atributos**: `taxId`, `companyName`, `active`.

**2.4 Entidad Product (Producto)**
- **Propósito**: Representa los productos del catálogo.
- **Atributos**: `name`, `description`, `price`, `type` (Físico/Digital).
- **Relaciones**: `@ManyToOne` con `Seller` y `Category`.

**2.5 Entidad Inventory (Inventario)**
- **Propósito**: Controla el stock de productos en bodegas.
- **Atributos**: `quantity`, `status`.
- **Reglas de Negocio**:
    - No se permiten existencias negativas.
    - No se puede reservar inventario dañado.

**2.6 Entidad Order (Pedido)**
- **Propósito**: Gestionar las compras de los clientes.
- **Ciclo de Vida**: `CART` → `PENDING_PAYMENT` → `PAID` → `DISPATCHED` → `DELIVERED` → `FINISHED`.
- **Regla de Negocio**: Un pedido finalizado no puede modificarse.

---

### 3. Convenciones de Código (Construcción Limpia)
- **Lenguaje**: Todo el código está en inglés.
- **Nombres de clases**: PascalCase (ej. `User`, `UserRepository`).
- **Nombres de métodos**: camelCase (ej. `findByEmail`, `activate`).
- **Anotaciones de JPA**: `@Entity`, `@Table`, `@Column`, `@Enumerated`, `@Id`, `@GeneratedValue`.

---

### 4. Comandos Git Utilizados (Control de Versiones)
| Comando | Propósito |
|---------|-----------|
| `git clone` | Descargar el repositorio remoto. |
| `git status` | Ver el estado de los archivos. |
| `git add .` | Agregar todos los archivos al staging. |
| `git commit -m "mensaje"` | Guardar los cambios en el historial. |
| `git push origin main` | Subir los cambios al repositorio remoto. |

---
**Fecha de creación**: 30 de agosto de 2026  
**Autor**: Juan Esteban T-DEA  
**Curso**: Construcción de Software 2 - 2026-2

---

### 5. Reglas de Negocio Extraídas (Del Documento Funcional)
| ID | Regla de Negocio | Módulo | Excepción Asociada |
|----|------------------|--------|--------------------|
| RN-01 | Un pedido finalizado no puede modificarse. | orders | `InvalidStatusTransitionException` |
| RN-02 | No se permiten existencias negativas en inventario. | inventory | `BusinessRuleException` |
| RN-03 | No se puede reservar inventario dañado. | inventory | `BusinessRuleException` |
| RN-04 | El ciclo de vida del pedido es: `CART` → `PENDING_PAYMENT` → `PAID` → `DISPATCHED` → `DELIVERED` → `FINISHED`. | orders | `InvalidStatusTransitionException` |
| RN-05 | Un vendedor debe estar activo para publicar productos. | catalog | `BusinessRuleException` |
| RN-06 | No se puede registrar un usuario con un email ya existente. | users | `DuplicateResourceException` |
| RN-07 | Un comprador debe estar activo para realizar compras. | orders | `BusinessRuleException` |
| RN-08 | Toda acción relevante (creación, modificación, eliminación) debe quedar auditada. | audit | - |
| RN-09 | Un pedido debe estar en estado `PENDING_PAYMENT` para poder registrar el pago. | orders | `InvalidStatusTransitionException` |
| RN-10 | No se puede despachar un pedido que no esté pagado. | logistics | `InvalidStatusTransitionException` |

### 6. Servicios Requeridos por Módulo
| Módulo | Servicio | Responsabilidad Principal |
|--------|----------|---------------------------|
| users | `UserService` | Gestión de usuarios: creación, bloqueo, activación. |
| users | `BuyerService` | Administración de compradores y direcciones. |
| users | `SellerService` | Registro y administración de vendedores. |
| catalog | `CatalogService` | Consulta pública del catálogo. |
| catalog | `ProductService` | Gestión de productos (publicar, actualizar, descontinuar). |
| catalog | `CategoryService` | Administración de categorías. |
| catalog | `WarehouseService` | Control de información de bodegas. |
| inventory | `InventoryService` | Control de stock, reservas y estados del inventario. |
| orders | `OrderService` | Ciclo completo de vida del pedido. |
| logistics | `ShipmentService` | Gestión de envíos y entregas. |
| logistics | `ReturnService` | Gestión de devoluciones. |
| billing | `InvoiceService` | Generación y consulta de facturas. |
| billing | `RefundService` | Gestión de reembolsos. |
| audit | `AuditService` | Registro de acciones en MongoDB. |

### 7. Estrategia de Excepciones Personalizadas
| Excepción | HTTP | Cuándo se lanza |
|-----------|------|-----------------|
| `ResourceNotFoundException` | 404 | El recurso solicitado no existe. |
| `BusinessRuleException` | 422 | Se viola una regla de negocio (RN-02, RN-03, RN-05, RN-07). |
| `InvalidStatusTransitionException` | 409 | Transición de estado inválida (RN-01, RN-04, RN-09, RN-10). |
| `DuplicateResourceException` | 409 | Se intenta crear un recurso que ya existe (RN-06). |
| `ProductNotAvailableException` | 409 | Producto con stock reservado o no disponible para la operación. |
| `CategoryHasProductsException` | 409 | Intento de eliminar una categoría con productos asignados. |
| `WarehouseCapacityExceededException` | 422 | Capacidad de la bodega superada al asignar inventario. |
| `InvoiceAlreadyExistsException` | 409 | Ya existe una factura activa para la orden. |
| `InvoiceNotPayableException` | 422 | La orden no está en estado pagado para facturarse. |
| `RefundAmountExceededException` | 422 | Monto a reembolsar excede el total facturado. |
| `RefundNotAllowedException` | 422 | Reembolso o anulación no permitida según estado de la factura. |
| `PaymentGatewayException` | 502 | Falla de pasarela de pagos. |
| `ShipmentAlreadyExistsException` | 409 | Ya existe un envío para la orden o tracking repetido. |
| `TrackingNotFoundException` | 404 | Número de rastreo de envío no encontrado. |
| `ReturnWindowExpiredException` | 422 | La ventana de devolución (30 días) ha expirado. |
| `ReturnNotAllowedException` | 422 | Retorno no permitido para órdenes no entregadas/finalizadas. |
| `ReturnAlreadyProcessedException` | 409 | Devolución ya procesada o completada anteriormente. |
| `ErrorResponse` | - | DTO estándar de respuesta de error (timestamp, status, message). |

Todas son capturadas por el `GlobalExceptionHandler` (`@RestControllerAdvice`), que garantiza respuestas de error consistentes en toda la API.

---

### 8. Endpoints de la API REST

#### Módulo Users
- `POST /api/users` - Crear usuario con DTO validado
- `GET /api/users/{id}` - Obtener usuario por ID
- `GET /api/users/email` - Obtener usuario por email
- `GET /api/users` - Listar usuarios
- `GET /api/users/role/{role}` - Filtrar usuarios por rol
- `PATCH /api/users/{id}/block` - Bloquear usuario
- `PATCH /api/users/{id}/activate` - Activar usuario
- `PATCH /api/users/{id}/role` - Cambiar rol
- `POST /api/buyers` - Crear comprador con DTO
- `GET /api/buyers/{id}` - Obtener comprador
- `GET /api/buyers` - Listar compradores
- `GET /api/buyers/status/{status}` - Filtrar por estado comercial
- `POST /api/buyers/{id}/addresses` - Añadir dirección adicional
- `PATCH /api/buyers/{id}/status` - Cambiar estado comercial
- `POST /api/sellers` - Crear vendedor con DTO
- `GET /api/sellers/{id}` - Obtener vendedor
- `GET /api/sellers/taxId` - Buscar vendedor por NIT/RUT
- `GET /api/sellers` - Listar vendedores
- `PATCH /api/sellers/{id}/activate` - Activar vendedor
- `PATCH /api/sellers/{id}/deactivate` - Desactivar vendedor
- `PATCH /api/sellers/{id}/update` - Actualizar información del vendedor

#### Módulo Catalog
- `POST /api/products` - Crear producto (valida SKU único, rol SELLER activo, precio > 0)
- `GET /api/products/{id}` - Detalle de producto por ID
- `GET /api/products` - Listar todos los productos
- `GET /api/products/category/{categoryId}` - Filtrar por categoría
- `GET /api/products/seller/{sellerId}` - Filtrar por vendedor
- `GET /api/products/price-range` - Filtrar por rango de precio
- `PATCH /api/products/{id}` - Actualizar producto
- `DELETE /api/products/{id}` - Desactivar producto (soft-delete, valida stock reservado)
- `GET /api/catalog` - Vista general agregada del catálogo
- `GET /api/catalog/search` - Búsqueda de productos en catálogo
- `GET /api/catalog/products/{id}` - Detalle de producto en catálogo
- `POST /api/categories` - Crear categoría con soporte de jerarquía
- `GET /api/categories` - Listar todas las categorías
- `GET /api/categories/roots` - Listar categorías raíz
- `GET /api/categories/{id}` - Obtener categoría por ID
- `PATCH /api/categories/{id}` - Actualizar categoría
- `DELETE /api/categories/{id}` - Eliminar categoría (valida que no tenga productos)
- `POST /api/warehouses` - Crear bodega
- `GET /api/warehouses` - Listar bodegas
- `GET /api/warehouses/{id}` - Obtener bodega por ID
- `GET /api/warehouses/type/{type}` - Filtrar bodegas por tipo
- `PATCH /api/warehouses/{id}` - Actualizar bodega (valida capacidad vs stock actual)

#### Módulo Inventory
- `POST /api/inventory` (y `/api/inventories`) - Crear inventario (valida capacidad de bodega)
- `GET /api/inventory/{id}` - Obtener inventario por ID
- `GET /api/inventory` - Listar inventarios
- `GET /api/inventory/product/{productId}` - Filtrar inventario por producto
- `GET /api/inventory/warehouse/{warehouseId}` - Filtrar inventario por bodega
- `PATCH /api/inventory/{id}/reserve` - Reservar existencias
- `PATCH /api/inventory/{id}/confirm-payment` - Confirmar pago/salida
- `PATCH /api/inventory/{id}/damage` (o `/mark-damaged`) - Marcar inventario como dañado
- `PATCH /api/inventory/{id}/adjust` - Ajustar cantidades

#### Módulo Orders
- `POST /api/orders` - Crear orden para un comprador activo
- `GET /api/orders/{id}` - Obtener orden con sus items y montos calculados
- `GET /api/orders` - Listar órdenes
- `GET /api/orders/buyer/{buyerId}` - Listar órdenes por comprador
- `POST /api/orders/{id}/items` - Añadir ítem a la orden (recalcula total)
- `DELETE /api/orders/{id}/items/{itemId}` - Eliminar ítem de la orden (recalcula total)
- `PATCH /api/orders/{id}/confirm-payment` - Confirmar pago (PENDING_PAYMENT -> PAID)
- `PATCH /api/orders/{id}/dispatch` - Despachar orden (PAID -> DISPATCHED)
- `PATCH /api/orders/{id}/deliver` - Registrar entrega (DISPATCHED -> DELIVERED)
- `PATCH /api/orders/{id}/finish` - Finalizar orden (DELIVERED -> FINISHED)
- `PATCH /api/orders/{id}/cancel` - Cancelar orden

#### Módulo Billing
- `POST /api/invoices` - Generar factura (valida orden pagada y factura única)
- `GET /api/invoices/{id}` - Obtener factura por ID
- `GET /api/invoices/order/{orderId}` - Obtener factura por orden
- `GET /api/invoices` - Listar facturas
- `PATCH /api/invoices/{id}/void` - Anular factura
- `POST /api/refunds` - Solicitar reembolso (valida monto y factura pagada)
- `GET /api/refunds/{id}` - Obtener reembolso por ID
- `GET /api/refunds/invoice/{invoiceId}` - Listar reembolsos por factura
- `GET /api/refunds` - Listar todos los reembolsos
- `PATCH /api/refunds/{id}/approve` - Aprobar reembolso
- `PATCH /api/refunds/{id}/reject` - Rechazar reembolso

#### Módulo Logistics
- `POST /api/shipments` - Crear envío (valida orden DISPATCHED y tracking único)
- `GET /api/shipments/{id}` - Obtener envío por ID
- `GET /api/shipments/tracking/{trackingNumber}` - Consultar por guía de rastreo
- `GET /api/shipments/order/{orderId}` - Consultar envío por orden
- `GET /api/shipments` - Listar envíos
- `PATCH /api/shipments/{id}/status` - Actualizar estado de envío
- `POST /api/returns` - Solicitar devolución (valida orden entregada y ventana de 30 días)
- `GET /api/returns/{id}` - Obtener devolución por ID
- `GET /api/returns/order/{orderId}` - Consultar devoluciones por orden
- `GET /api/returns` - Listar devoluciones
- `PATCH /api/returns/{id}/approve` - Aprobar devolución
- `PATCH /api/returns/{id}/reject` - Rechazar devolución
- `PATCH /api/returns/{id}/complete` - Completar devolución

#### Módulo Audit
- `GET /api/audit` - Consultar logs de auditoría (con filtros por usuario, entidad y rango de fechas)
- `GET /api/audit/{id}` - Obtener log de auditoría por ID