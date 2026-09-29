# 📘 Documentación Técnica - NexusMarket

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

#### Módulo Auth (nuevo)
- `POST /api/auth/register` - **Público.** Autorregistro de BUYER o SELLER (crea User + perfil). Devuelve JWT.
- `POST /api/auth/login` - **Público.** Valida credenciales y devuelve JWT.

#### Módulo Users
- `GET /api/users/me` - **Autenticado (cualquier rol).** Perfil del usuario del token.
- `POST /api/users` - Crear usuario con DTO validado. **Solo ADMIN.**
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

---

### 9. Seguridad: Autenticación y Autorización (JWT)

> **Todos los endpoints exigen un JWT válido salvo los dos de `/api/auth/**`, Swagger y `/actuator/health`.**

#### 9.1 Flujo de autenticación

```
1. POST /api/auth/register  → crea el usuario (BCrypt) + perfil → devuelve un JWT
        (o)
   POST /api/auth/login     → valida credenciales → devuelve un JWT

2. El cliente guarda el token y lo envía en cada petición:
   Authorization: Bearer <token>

3. JwtAuthenticationFilter valida el token en cada request y puebla el
   SecurityContext. Si el token falta o es inválido, la petición llega
   sin autenticar y la API responde 401.
```

#### 9.2 Ejemplos con curl

**Registrar un comprador**
```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
        "email": "buyer1@test.com",
        "fullName": "Juan Perez",
        "password": "secret123",
        "role": "BUYER",
        "primaryAddress": "Calle 123 #45-67"
      }'
# 201 Created → {"tokenType":"Bearer","token":"eyJ...","expiresIn":86400000,...}
```

**Registrar un vendedor**
```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
        "email": "seller1@test.com",
        "fullName": "Tienda SA",
        "password": "secret123",
        "role": "SELLER",
        "taxId": "900123456-7",
        "companyName": "Tienda Nexus"
      }'
```

**Iniciar sesión**
```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "buyer1@test.com", "password": "secret123"}'
# 200 OK → {"tokenType":"Bearer","token":"eyJ...","expiresIn":86400000,...}
```

**Usar el token**
```bash
TOKEN="eyJhbGciOiJIUzI1NiJ9..."

curl -i http://localhost:8080/api/users/me \
  -H "Authorization: Bearer $TOKEN"
```

#### 9.3 Formato del JWT

Algoritmo **HS256** (HMAC-SHA256). Payload de ejemplo:

```json
{
  "roles": ["ROLE_BUYER"],
  "sub": "buyer1@test.com",
  "iat": 1759100000,
  "exp": 1759186400
}
```

| Claim | Significado |
|-------|-------------|
| `sub` | Email del usuario (el "username" del sistema). |
| `roles` | Autoridades concedidas, con prefijo `ROLE_`. |
| `iat` | Momento de emisión (issued at). |
| `exp` | Momento de expiración. |

**TTL del token: 24 horas** (`jwt.expiration=86400000` ms). Pasado ese tiempo, cualquier petición devuelve 401 y el cliente debe volver a hacer login.

#### 9.4 Cómo registrar un rol privilegiado

`ADMIN`, `SUPERVISOR` y `WAREHOUSE_OPERATOR` **no pueden autorregistrarse**. Intentar `role=ADMIN` en `/api/auth/register` devuelve **422** con el mensaje *"Self-registration is only allowed for BUYER and SELLER roles"*. Se crean exclusivamente por un administrador:

```bash
curl -i -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin2@test.com","fullName":"Admin Dos","password":"secret123","role":"ADMIN"}'
```

#### 9.5 Mapa de roles → endpoints permitidos

| Rol | Puede acceder a |
|-----|-----------------|
| *(anónimo)* | `POST /api/auth/register`, `POST /api/auth/login`, `/swagger-ui/**`, `/v3/api-docs/**`, `/actuator/health` |
| `ADMIN` | Todo. En particular `/api/users/**` (salvo `/api/users/me`, que es de todos), `/api/audit/**` |
| `SUPERVISOR` | `/api/audit/**` + el resto de endpoints autenticados |
| `BUYER` | `/api/users/me`, lecturas de catálogo, órdenes, facturas, devoluciones propias |
| `SELLER` | `/api/users/me`, lecturas de catálogo, sus productos, envíos de sus órdenes |
| `WAREHOUSE_OPERATOR` | `/api/users/me`, `/api/inventory/**`, `/api/shipments/**`, `/api/warehouses/**` |

#### 9.6 Códigos HTTP de seguridad

| Código | Cuándo | Body |
|--------|--------|------|
| **401 Unauthorized** | Sin token, token malformado, firma inválida o token expirado | `ErrorResponse` JSON |
| **403 Forbidden** | Token **válido** pero el rol no tiene permiso para la ruta | `ErrorResponse` JSON |

Ambos devuelven el formato estándar de error del proyecto:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication is required to access this resource",
  "path": "/api/users/me",
  "timestamp": "2026-09-29T10:15:30",
  "details": null
}
```

```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "You do not have permission to access this resource",
  "path": "/api/users/1",
  "timestamp": "2026-09-29T10:15:30",
  "details": null
}
```

#### 9.7 Gestión de secretos (variables de entorno)

Los secretos se leen de variables de entorno con **fallback de desarrollo** en `application.properties`:

| Propiedad | Variable de entorno | Fallback de dev |
|-----------|---------------------|-----------------|
| `spring.datasource.username` | `DB_USERNAME` | `root` |
| `spring.datasource.password` | `DB_PASSWORD` | `root` |
| `spring.data.mongodb.uri` | `MONGODB_URI` | cadena de Atlas |
| `jwt.secret` | `JWT_SECRET` | clave de 64 chars |

**PowerShell (desarrollo local):**
```powershell
$env:JWT_SECRET = "<nuevo-secreto-de-64-caracteres>"
$env:DB_PASSWORD = "<password-mysql>"
$env:MONGODB_URI = "mongodb+srv://..."
```

**Docker:**
```bash
docker run -e JWT_SECRET="..." -e DB_PASSWORD="..." -e MONGODB_URI="..." nexusmarket
```

**Producción:** usar un secrets manager (AWS Secrets Manager, Azure Key Vault, HashiCorp Vault) o los secrets nativos del orquestador (Kubernetes Secrets), nunca el repositorio.

#### 9.8 Cómo rotar el `jwt.secret`

1. Generar un valor nuevo de al menos 32 bytes (`openssl rand -base64 48`).
2. Actualizar la variable `JWT_SECRET` y reiniciar la aplicación.
3. **Efecto inmediato:** todos los tokens emitidos con el secreto anterior dejan de validar → logout global forzado. Los clientes deben hacer login otra vez.

#### 9.9 Notas importantes y limitaciones conocidas

- **Usuarios antiguos en texto plano**: los usuarios creados **antes** de la Fase 2 tienen la contraseña en texto plano en MySQL y **ya no pueden iniciar sesión** (BCrypt nunca hará match contra texto plano). Deben recrearse vía `POST /api/auth/register` o `POST /api/users`.
- **JWT stateless sin refresh token**: no hay refresh token ni blacklist. Consecuencia práctica: un usuario **bloqueado** que ya tenga un token válido lo conservará hasta que expire (máximo 24 h). Mitigación futura: refresh tokens + blacklist.
- **Aviso de seguridad**: los secretos siguen teniendo un fallback literal en `application.properties`, que **está en git**, para no romper el desarrollo local. En producción es obligatorio inyectar las variables de entorno y eliminar los fallbacks.