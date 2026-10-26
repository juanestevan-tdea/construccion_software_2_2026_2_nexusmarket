# Contratos de API por Endpoint

Referencia de los **96 endpoints** expuestos por los 19 controladores de
`adapters/rest/controllers/`. Refleja el código en `main` tras la auditoría de
seguridad y arquitectura.

- Autorización efectiva: reglas de ruta en `SecurityConfig` **y**
  `@PreAuthorize` habilitado con `@EnableMethodSecurity`.
- Identidad: el `userId` se obtiene del JWT (`UserDetailsAdapter`). Los cuerpos
  ya no aceptan `userId`, `buyerId` ni `sellerId` para crear recursos.
- Propiedad: los controladores de comprador y vendedor verifican que el recurso
  pertenezca al usuario autenticado antes de leerlo o modificarlo.

## Convenciones

| Símbolo | Significado |
|---|---|
| `Público` | Sin token |
| `Autenticado` | Cualquier JWT válido |
| `ADMIN`, `BUYER`, `SELLER`, `SUPERVISOR`, `WAREHOUSE_OPERATOR` | Rol exigido |
| `—` | Sin cuerpo de petición |
| `204` | Sin contenido |

### Contrato de error (`ErrorResponse`)

```json
{
  "timestamp": "2026-10-19T22:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Product with id '7' was not found",
  "path": "/api/v1/seller/products/7",
  "details": null
}
```

| Código | Causa | Excepción / origen |
|---|---|---|
| 400 | DTO inválido, parámetro faltante o mal formado, excepción de dominio no mapeada | `MethodArgumentNotValidException`, `DomainException` |
| 401 | Token ausente, expirado o inválido | `AuthenticationException` |
| 403 | Rol insuficiente o recurso de otro usuario | `AccessDeniedException` |
| 404 | Recurso inexistente | `ResourceNotFoundException`, `TrackingNotFoundException` |
| 409 | Duplicidad, transición inválida, conflicto de estado | `DuplicateResourceException` y subtipos (`ProductSkuAlreadyExistsException`, `CategoryNameAlreadyExistsException`, `SellerTaxIdAlreadyExistsException`, `WarehouseNameAlreadyExistsException`, `UserEmailAlreadyExistsException`), `InvalidStatusTransitionException`, etc. |
| 422 | Regla de negocio violada | `BusinessRuleException` y subtipos (`ProductNotActiveException`, etc.) |
| 500 | Error no controlado (mensaje genérico) | `Exception` |
| 502 | Falla de pasarela de pago | `PaymentGatewayException` |

## 1. Autenticación — `/api/v1/auth`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| POST | `/register` | Público | `RegisterRequestDTO` | `201` `AuthResponseDTO` |
| POST | `/login` | Público | `LoginRequestDTO` | `200` `AuthResponseDTO` |
| POST | `/logout` | Autenticado | — | `204` |
| POST | `/change-password` | Autenticado | `ChangePasswordRequestDTO` | `204` |

`RegisterRequestDTO`: `email`, `fullName`, `password`, `role` (`BUYER`/`SELLER`),
`primaryAddress`, `taxId`, `companyName`. `AuthResponseDTO`: `tokenType`,
`token`, `expiresIn`, `id`, `email`, `fullName`, `role`.

Errores: `400` validación, `401` credenciales inválidas, `409` email duplicado,
`422` política de contraseña o rol no permitido.

## 2. Catálogo — `/api/v1/catalog`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| GET | `/` | Autenticado | — | `200` lista de `ProductResponseDTO` activos |
| GET | `/search?query=` | Autenticado | query `query` | `200` lista |
| GET | `/products/{id}` | Autenticado | — | `200` `ProductResponseDTO` |

## 3. Usuarios (admin) — `/api/v1/admin/users`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| POST | `/` | ADMIN | `UserCreateRequestDTO` | `201` `UserResponseDTO` |
| GET | `/me` | Autenticado | — | `200` `UserResponseDTO` |
| GET | `/{id}` | ADMIN | — | `200` `UserResponseDTO` |
| GET | `/email?email=` | ADMIN | query | `200` `UserResponseDTO` |
| GET | `/` | ADMIN | — | `200` lista |
| GET | `/role/{role}` | ADMIN | path `UserRole` | `200` lista |
| PATCH | `/{id}/block` | ADMIN | — | `200` `UserResponseDTO` |
| PATCH | `/{id}/activate` | ADMIN | — | `200` `UserResponseDTO` |
| PATCH | `/{id}/role?newRole=` | ADMIN | query `UserRole` | `200` `UserResponseDTO` |

> `GET /me` se permite a cualquier autenticado mediante
> `@PreAuthorize("isAuthenticated()")`, que prevalece sobre el `ADMIN` de clase.

## 4. Compradores — `/api/v1/buyer/buyers`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| POST | `/` | BUYER | `BuyerCreateRequestDTO` (`primaryAddress`) | `201` `BuyerResponseDTO` |
| GET | `/{id}` | BUYER dueño | — | `200` `BuyerResponseDTO` |
| GET | `/` | BUYER | — | `200` lista con el perfil propio |
| GET | `/status/{status}` | BUYER | path `BuyerCommercialStatus` | `200` lista (propia o vacía) |
| POST | `/{id}/addresses?address=` | BUYER dueño | query `address` | `200` `BuyerResponseDTO` |

Errores: `403` si `id` no es el del comprador autenticado, `404` si no existe
perfil de comprador para el usuario del token.

> El cambio de estado comercial (`changeCommercialStatus`) se movió a
> `PATCH /api/v1/admin/buyers/{id}/status?newStatus=` (§4.1), reservado a
> `ADMIN`. Antes el comprador podía reactivarse a sí mismo.

### 4.1 Compradores (admin) — `/api/v1/admin/buyers`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| PATCH | `/{id}/status?newStatus=` | ADMIN | query `BuyerCommercialStatus` | `200` `BuyerResponseDTO` |

## 5. Vendedores — `/api/v1/seller/sellers`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| POST | `/` | SELLER | `SellerCreateRequestDTO` (`taxId`, `companyName`) | `201` `SellerResponseDTO` |
| GET | `/{id}` | SELLER dueño | — | `200` `SellerResponseDTO` |
| GET | `/taxId?taxId=` | SELLER dueño | query | `200` `SellerResponseDTO` |
| GET | `/` | SELLER | — | `200` lista con el perfil propio |
| PATCH | `/{id}/activate` | SELLER dueño | — | `200` `SellerResponseDTO` |
| PATCH | `/{id}/deactivate` | SELLER dueño | — | `200` `SellerResponseDTO` |
| PATCH | `/{id}/update` | SELLER dueño | `SellerUpdateRequestDTO` | `200` `SellerResponseDTO` |

## 6. Productos (vendedor) — `/api/v1/seller/products`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| POST | `/` | SELLER | `ProductCreateRequestDTO` (`sku`, `name`, `description`, `price`, `type`, `categoryId`) | `201` `ProductResponseDTO` |
| GET | `/{id}` | SELLER | — | `200` `ProductResponseDTO` |
| GET | `/` | SELLER | — | `200` lista |
| GET | `/category/{categoryId}` | SELLER | — | `200` lista |
| GET | `/seller/{sellerId}` | SELLER | — | `200` lista |
| GET | `/price-range?min=&max=` | SELLER | query | `200` lista |
| PATCH | `/{id}` | SELLER dueño | `ProductUpdateRequestDTO` | `200` `ProductResponseDTO` |
| DELETE | `/{id}` | SELLER dueño | — | `200` `ProductResponseDTO` (soft delete) |

El `sellerId` de creación se resuelve desde el token. `PATCH` y `DELETE`
devuelven `403` si el producto pertenece a otro vendedor.

## 7. Categorías (admin) — `/api/v1/admin/categories`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| POST | `/` | ADMIN | `CategoryCreateRequestDTO` | `201` `CategoryResponseDTO` |
| GET | `/` | ADMIN | — | `200` lista |
| GET | `/roots` | ADMIN | — | `200` lista |
| GET | `/{id}` | ADMIN | — | `200` `CategoryResponseDTO` |
| PATCH | `/{id}` | ADMIN | `CategoryUpdateRequestDTO` | `200` `CategoryResponseDTO` |
| DELETE | `/{id}` | ADMIN | - | `204`; `409` si tiene productos |

Errores: `409` categoría con productos (`CategoryHasProductsException`); `422`
ciclo de jerarquía o categoría con subcategorías (`BusinessRuleException`);
`404` categoría inexistente.

## 8. Órdenes (comprador) — `/api/v1/buyer/orders`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| POST | `/` | BUYER | — (comprador del token) | `201` `OrderResponseDTO` |
| GET | `/{id}` | BUYER dueño | — | `200` `OrderResponseDTO` |
| GET | `/` | BUYER | — | `200` solo órdenes propias |
| GET | `/buyer/{buyerId}` | BUYER dueño | — | `200` lista; `403` si no coincide |
| POST | `/{id}/items` | BUYER dueño | `OrderItemRequestDTO` | `200` `OrderResponseDTO` |
| DELETE | `/{id}/items/{itemId}` | BUYER dueño | — | `200` `OrderResponseDTO` |
| PATCH | `/{id}/checkout` | BUYER dueño | — | `200` (`PENDING_PAYMENT`) |
| PATCH | `/{id}/confirm-payment` | BUYER dueño | — | `200` (`PAID`) |
| PATCH | `/{id}/cancel` | BUYER dueño | — | `200` (`CANCELLED`) |

Transiciones inválidas → `409` (`InvalidStatusTransitionException`). Orden de
otro comprador → `403`.

> `dispatch`, `deliver` y `finish` se movieron a
> `/api/v1/warehouse/orders/{id}/...` (§13.1); un comprador ya no puede
> despachar ni entregar su propia orden.

## 9. Devoluciones (comprador) — `/api/v1/buyer/returns`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| POST | `/` | BUYER dueño de la orden | `ReturnCreateRequestDTO` (`orderId`, `reason`) | `201` `ReturnResponseDTO` |
| GET | `/{id}` | BUYER dueño | — | `200` `ReturnResponseDTO` |
| GET | `/order/{orderId}` | BUYER dueño | — | `200` lista |
| GET | `/` | BUYER | — | `200` solo devoluciones de sus órdenes |

## 10. Devoluciones (supervisor) — `/api/v1/supervisor/returns`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| PATCH | `/{id}/approve` | SUPERVISOR | — | `200` `ReturnResponseDTO` |
| PATCH | `/{id}/reject` | SUPERVISOR | — | `200` `ReturnResponseDTO` |
| PATCH | `/{id}/complete` | SUPERVISOR | — | `200` `ReturnResponseDTO` |

> Antes vivían bajo `/api/v1/buyer/returns`, permitiendo que un comprador
> aprobara su propia devolución. Fueron movidas en la auditoría.

Errores: `404` devolución inexistente; `409` devolución ya procesada
(`ReturnAlreadyProcessedException`, RF-041); `422` completar una devolución que
aún no está aprobada (`ReturnNotAllowedException`).

## 11. Bodegas — `/api/v1/warehouse/warehouses`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| POST | `/` | WAREHOUSE_OPERATOR | `WarehouseCreateRequestDTO` | `201` `WarehouseResponseDTO` |
| GET | `/` | WAREHOUSE_OPERATOR | — | `200` lista |
| GET | `/{id}` | WAREHOUSE_OPERATOR | — | `200` `WarehouseResponseDTO` |
| GET | `/type/{type}` | WAREHOUSE_OPERATOR | path `WarehouseType` | `200` lista |
| PATCH | `/{id}` | WAREHOUSE_OPERATOR | `WarehouseUpdateRequestDTO` | `200` `WarehouseResponseDTO` |

## 12. Inventario — `/api/v1/warehouse/inventories`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| POST | `/` | WAREHOUSE_OPERATOR | `InventoryCreateRequestDTO` | `201` `InventoryResponseDTO` |
| GET | `/{id}` | WAREHOUSE_OPERATOR | — | `200` |
| GET | `/` | WAREHOUSE_OPERATOR | — | `200` lista |
| GET | `/product/{productId}` | WAREHOUSE_OPERATOR | — | `200` lista |
| GET | `/warehouse/{warehouseId}` | WAREHOUSE_OPERATOR | — | `200` lista |
| PATCH | `/{id}/reserve?amount=` | WAREHOUSE_OPERATOR | query `amount` (> 0) | `200` |
| PATCH | `/{id}/confirm-payment` | WAREHOUSE_OPERATOR | — | `200` |
| PATCH | `/{id}/mark-damaged` | WAREHOUSE_OPERATOR | — | `200` |
| PATCH | `/{id}/adjust?amount=` | WAREHOUSE_OPERATOR | query `amount` (delta) | `200` |

Errores: `422` capacidad excedida (`WarehouseCapacityExceededException`),
cantidad inválida (`InvalidQuantityException`, incluida una reserva con
`amount <= 0`), stock insuficiente (`InsufficientStockException`), inventario
dañado (`DamagedInventoryException`) o cantidad no inicializada
(`InventoryNotInitializedException`).

## 13. Envíos — `/api/v1/warehouse/shipments`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| POST | `/` | WAREHOUSE_OPERATOR | `ShipmentCreateRequestDTO` | `201` `ShipmentResponseDTO` |
| GET | `/{id}` | WAREHOUSE_OPERATOR | — | `200` |
| GET | `/tracking/{trackingNumber}` | WAREHOUSE_OPERATOR | — | `200`; `404` si no existe |
| GET | `/order/{orderId}` | WAREHOUSE_OPERATOR | — | `200` lista |
| GET | `/` | WAREHOUSE_OPERATOR | — | `200` lista |
| PATCH | `/{id}/status?newStatus=` | WAREHOUSE_OPERATOR | query | `200` |

## 13.1 Órdenes (bodega) — `/api/v1/warehouse/orders`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| PATCH | `/{id}/dispatch` | WAREHOUSE_OPERATOR | — | `200` (`DISPATCHED`) |
| PATCH | `/{id}/deliver` | WAREHOUSE_OPERATOR | — | `200` (`DELIVERED`) |
| PATCH | `/{id}/finish` | WAREHOUSE_OPERATOR | — | `200` (`FINISHED`) |

Transiciones inválidas → `409` (`InvalidStatusTransitionException`). Orden
inexistente → `404`. Un `BUYER` recibe `403`.

## 14. Facturación (supervisor) — `/api/v1/supervisor/invoices`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| POST | `/` | SUPERVISOR | `InvoiceCreateRequestDTO` | `201` `InvoiceResponseDTO` |
| GET | `/{id}` | SUPERVISOR | — | `200` |
| GET | `/order/{orderId}` | SUPERVISOR | — | `200` |
| GET | `/` | SUPERVISOR | — | `200` lista |
| PATCH | `/{id}/void` | SUPERVISOR | — | `200` |

Errores: `409` factura ya existe, `422` factura no pagable o no anulable.

## 15. Reembolsos (supervisor) — `/api/v1/supervisor/refunds`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| POST | `/` | SUPERVISOR | `RefundCreateRequestDTO` | `201` `RefundResponseDTO` |
| GET | `/{id}` | SUPERVISOR | — | `200` |
| GET | `/invoice/{invoiceId}` | SUPERVISOR | — | `200` lista |
| GET | `/` | SUPERVISOR | — | `200` lista |
| PATCH | `/{id}/approve` | SUPERVISOR | — | `200` |
| PATCH | `/{id}/reject` | SUPERVISOR | — | `200` |

Errores: `404` factura o devolución inexistente; `422` monto excedido
(`RefundAmountExceededException`), monto inválido (`InvalidRefundAmountException`),
factura inactiva/impaga (`InvoiceNotRefundableException`) o devolución de otra
orden (`RefundNotAllowedException`); `409` reembolso ya procesado en
aprobar/rechazar (`InvalidStatusTransitionException`, RF-035).

## 16. Auditoría (admin) — `/api/v1/admin`

| Método | Ruta | Rol | Petición | Respuesta |
|---|---|---|---|---|
| GET | `/audit?from=&to=` | ADMIN | query fechas | `200` lista de `AuditLog` |
| GET | `/audit/{id}` | ADMIN | — | `200` `AuditLog` |
| GET | `/session-events?email=` | ADMIN | query | `200` lista de eventos |

## 17. Salud

| Método | Ruta | Rol | Respuesta |
|---|---|---|---|
| GET | `/actuator/health` | Público | `200` `{"status":"UP"}` |

## Cambios de contrato respecto a versiones anteriores

1. `POST /api/v1/buyer/orders`: se eliminó el cuerpo `OrderCreateRequestDTO`
   (`buyerId`); el comprador se toma del JWT.
2. `POST /api/v1/seller/products`: se eliminó `sellerId` del cuerpo.
3. `POST /api/v1/buyer/buyers` y `POST /api/v1/seller/sellers`: se eliminó
   `userId` del cuerpo.
4. `GET /api/v1/buyer/orders` y `GET /api/v1/buyer/returns`: devuelven solo los
   recursos del comprador autenticado.
5. Aprobación/rechazo/finalización de devoluciones: movidas de
   `/api/v1/buyer/returns` a `/api/v1/supervisor/returns`.
6. `dispatch`, `deliver` y `finish` de órdenes: movidas de
   `/api/v1/buyer/orders/{id}/...` a `/api/v1/warehouse/orders/{id}/...`
   (`WAREHOUSE_OPERATOR`).
7. Cambio de estado comercial del comprador: movido de
   `/api/v1/buyer/buyers/{id}/status` a `/api/v1/admin/buyers/{id}/status`
   (`ADMIN`).
8. Todas las rutas usan el prefijo `/api/v1/`.

## OpenAPI

`SDD/04-api/openapi.yaml` documenta los flujos principales (autenticación,
catálogo, órdenes, productos, devoluciones y salud). La referencia completa de
los 96 endpoints es este documento. Además, `springdoc-openapi` (3.0.1) genera
el contrato en runtime desde los controladores y lo publica sin autenticación
en `/v3/api-docs` (JSON) y `/v3/api-docs.yaml`, con Swagger UI en
`/swagger-ui.html`.
