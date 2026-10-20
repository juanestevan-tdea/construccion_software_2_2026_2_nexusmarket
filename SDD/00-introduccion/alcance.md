# Alcance

A continuación se indica qué cubre y qué no cubre el sistema NexusMarket en esta versión, basándose en los controladores, casos de uso y adaptadores reales del código.

## Qué SÍ cubre el sistema

| Área | Alcance real (tomado del código) |
|------|----------------------------------|
| Autenticación y seguridad | Registro y login de usuarios (`/api/v1/auth/register`, `/api/v1/auth/login`), JWT stateless firmado con HMAC-SHA256 (JJWT 0.12.6), expiración 24 h, contraseñas con BCrypt, RBAC con 5 roles (`BUYER`, `SELLER`, `WAREHOUSE_OPERATOR`, `ADMIN`, `SUPERVISOR`), manejo global de errores con `ErrorResponse`. |
| Usuarios, compradores y vendedores | CRUD de usuarios (solo `ADMIN`), consulta de perfil propio (`/api/v1/admin/users/me`), creación y consulta de compradores y vendedores, bloqueo/activación de usuarios, cambio de rol, estado comercial de comprador. |
| Catálogo de productos | Creación, consulta individual, listado con filtros (por categoría, vendedor, rango de precio), actualización y desactivación (soft delete) de productos con SKU único. |
| Categorías y bodegas | Categorías jerárquicas (con padre/hijos), creación, consulta de raíces, actualización y eliminación (bloqueada si tiene productos). Bodegas con nombre, ubicación, tipo (`MARKETPLACE`/`SELLER`) y capacidad. |
| Inventario | Creación de registros de inventario (producto × bodega × cantidad), consulta por producto/bodega, operaciones de reserva, confirmación de pago, marcado como dañado y ajuste de cantidad (sin permitir stock negativo). |
| Órdenes | Creación de orden (estado `CART`), agregar/quitar ítems (solo en `CART`/`PENDING_PAYMENT`), checkout, confirmación de pago, despacho, entrega, finalización y cancelación con transiciones de estado controladas por `OrderStatus`. |
| Facturación | Generación de factura vinculada a una orden (número, monto, PDF), consulta por orden, listado y anulación (bloqueada si ya está pagada/anulada). |
| Reembolsos | Creación de reembolso vinculado a factura (y opcionalmente a devolución), consulta, aprobación y rechazo (montos validados contra lo facturado). |
| Envíos | Creación de envío con número de tracking, consulta por orden/tracking, actualización de estado de envío. |
| Devoluciones | Creación de devolución con motivo, consulta, aprobación, rechazo y completado, con control de ventana y estado. |
| Auditoría | Registro inmutable de operaciones en MongoDB (`AuditLog`), consulta con filtros por usuario, entidad y rango de fechas (solo `ADMIN`/`SUPERVISOR`). |
| Persistencia híbrida | MySQL 8 vía Spring Data JPA para datos transaccionales; MongoDB vía Spring Data Mongo para auditoría. |
| Despliegue | Dockerfile multi-stage y `docker-compose.yml` (app + MySQL + MongoDB) con red bridge y volúmenes persistentes. |

## Qué NO cubre el sistema

| Área | Motivo / evidencia en el código |
|------|---------------------------------|
| Interfaz de usuario / frontend | Es solo backend API REST; los clientes se integran por HTTP/JSON. |
| Pasarela de pagos real | Existe `PaymentGatewayException` pero no hay adaptador de pasarela integrado; el pago se modela como confirmación de orden/factura. |
| Envío físico con transportista | Se registra número de tracking y estado de envío, pero no hay integración con carrier externo. |
| Notificaciones push/email reales | `NotificationPort` y `NoOpNotificationAdapter` existen como stub; no se invocan desde los casos de uso. |
| Búsqueda full-text / recomendaciones | `searchProducts` existe sobre el catálogo en memoria/BD, sin motor de búsqueda dedicado. |
| Reportes analíticos / dashboards | No existen endpoints de analítica; solo auditoría de operaciones. |
| Multi-inquilino / white-label | Un solo marketplace; no hay concepto de inquilino en el modelo. |
| Sincronización de inventario con ERP externo | El inventario se gestiona internamente; no hay adaptador de integración externa. |
| Despliegue en Kubernetes | Solo se provee Docker Compose para desarrollo/local. |
