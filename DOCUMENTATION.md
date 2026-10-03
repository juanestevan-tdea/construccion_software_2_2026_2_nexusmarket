# 🛒 NexusMarket — Documentación Técnica del Sistema

## 1. INTRODUCCIÓN

### 1.1 ¿Qué es NexusMarket?

**NexusMarket** es una plataforma backend para comercio electrónico (Marketplace) multiproveedor diseñada bajo principios de arquitectura limpia y hexagonal. Permite la interacción desacoplada entre compradores, vendedores, administradores y operadores logísticos, soportando la gestión integral del ciclo de vida comercial: autenticación y seguridad, catálogo jerárquico de productos, bodegas e inventarios, procesamiento transaccional de órdenes, facturación, reembolsos, envíos, devoluciones y auditoría centralizada.

### 1.2 Stack Tecnológico

| Capa | Tecnología |
|------|-----------|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.4, Spring MVC |
| Seguridad | Spring Security + JWT (JJWT 0.12.6), BCrypt |
| Persistencia relacional | Spring Data JPA / Hibernate → MySQL 8 |
| Persistencia NoSQL | Spring Data MongoDB → MongoDB Atlas |
| Validación | Jakarta Bean Validation |
| Testing | JUnit 5 + Mockito + Spring Test |
| Build | Maven 3.9+ |

### 1.3 Estado del Proyecto

- ✅ Arquitectura hexagonal aplicada en los 8 módulos de negocio
- ✅ 94 tests unitarios e integración en verde
- ✅ Autenticación JWT con autorización por rol
- ✅ Persistencia relacional (MySQL) + NoSQL (MongoDB)

---

## 2. ARQUITECTURA HEXAGONAL (Ports & Adapters)

El proyecto sigue el patrón **Ports & Adapters**, donde el **dominio** es el centro y no conoce ninguna tecnología externa.
+---------------------------------------------------------------------------------+
| CAPA EXTERNA: ADAPTERS |
| |
| [ Driving Adapters / REST ] [ Security Infrastructure ] |
| - AuthController - JwtAuthenticationFilter |
| - ProductController, OrderController... - RestAuthenticationEntryPoint |
| - DTOs (Requests/Responses) & Mappers - RestAccessDeniedHandler |
| | |
| | Invoca (Port In) |
| v |
| +-------------------------------------------------------------------------+ |
| | CAPA DE CASOS DE USO / USE CASES | |
| | - ProductUseCaseImpl, OrderUseCaseImpl, InventoryUseCaseImpl... | |
| | (Implementan ports/in y coordinan Domain Services y Ports Out) | |
| | | | |
| | | Orquesta lógica pura | |
| | v | |
| | +-----------------------------------------------------------------+ | |
| | | NÚCLEO DE DOMINIO (CORE) | | |
| | | | | |
| | | - Models (POJOs puros: User, Product, Order, Inventory...) | | |
| | | - Value Objects / Enums (OrderStatus, UserRole...) | | |
| | | - Domain Services (ProductDomainService, OrderCreateService) | | |
| | | - Exceptions (17 excepciones de negocio específicas) | | |
| | | - Ports In (15 interfaces de casos de uso) | | |
| | | - Ports Out (13 interfaces de repositorios) | | |
| | +-----------------------------------------------------------------+ | |
| | ^ | |
| | | Satisface contratos (Port Out) | |
| +-------------------------------------------------------------------------+ |
| | |
| [ Driven Adapters / Persistence ] |
| - JPA Relational: ProductJpaAdapter, OrderJpaAdapter... |
| (Entities JPA + JpaRepositories + Mappers bidireccionales) |
| - MongoDB NoSQL: AuditLogMongoAdapter |
| (Documents MongoDB + MongoRepository + Mappers) |
+---------------------------------------------------------------------------------+

text

### 2.3 Regla de Oro del Dominio

> La capa de dominio (`com.nexusmarket.domain.*`) es estructuralmente pura respecto a la infraestructura de persistencia y transporte. Los paquetes `domain/models`, `domain/valueobjects`, `domain/exceptions` y `domain/ports` NO conocen ni importan JPA, Hibernate, MongoDB, Jackson ni servlets HTTP.
>
> Los `domain/services` utilizan únicamente las anotaciones de Spring `@Service` y `@Transactional` para inyección de dependencias y manejo transaccional. Toda la lógica de negocio permanece aislada de los adaptadores concretos.
>
> Toda comunicación con el exterior se realiza a través de los **puertos** (`ports/in` y `ports/out`), implementados por adaptadores intercambiables (REST, JPA, MongoDB) sin afectar al núcleo.

---

## 3. ESTRUCTURA DEL PROYECTO
com.nexusmarket/
├── NexusMarketApplication.java
├── domain/ ← Núcleo puro (sin Spring/JPA/Mongo)
│ ├── models/ ← 14 POJOs de dominio
│ ├── valueobjects/ ← 10 enums y VOs de estado/tipo
│ ├── exceptions/ ← 17 excepciones de negocio
│ ├── ports/
│ │ ├── in/ ← 15 interfaces de casos de uso
│ │ └── out/ ← 13 interfaces de repositorios
│ └── services/ ← 20 servicios de dominio
├── adapters/
│ ├── persistence/
│ │ ├── jpa/ ← Persistencia SQL relacional
│ │ │ ├── adapters/ ← Implementan ports/out
│ │ │ ├── entities/ ← Entidades @Entity
│ │ │ ├── mappers/ ← Entity ↔ Domain
│ │ │ └── repositories/ ← Spring Data JpaRepository
│ │ └── mongodb/ ← Persistencia NoSQL (auditoría)
│ │ ├── adapters/ ← AuditLogMongoAdapter
│ │ ├── documents/ ← AuditLogDocument (@Document)
│ │ ├── mappers/ ← Document ↔ Domain
│ │ └── repositories/ ← MongoRepository
│ ├── rest/ ← Adaptadores de entrada API REST
│ │ ├── controllers/ ← @RestController
│ │ ├── dtos/
│ │ │ ├── requests/ ← DTOs de entrada con @Valid
│ │ │ └── responses/ ← DTOs de salida + ErrorResponse
│ │ ├── mappers/ ← Domain ↔ DTO
│ │ └── exception/ ← GlobalExceptionHandler
│ └── useCases/ ← 15 implementaciones de ports/in
└── infrastructure/
└── security/ ← Spring Security + JWT

text

### Descripción de Componentes Principales

- **`domain/models/`**: Entidades centrales como POJOs con métodos de comportamiento puro.
- **`domain/valueobjects/`**: Tipos que encapsulan estado o enumeraciones del negocio.
- **`domain/exceptions/`**: Excepciones de negocio tipadas para cada caso de fallo.
- **`domain/ports/in/`**: Contratos de entrada que aíslan la lógica aplicativa de la capa web.
- **`domain/ports/out/`**: Contratos de persistencia que desacoplan el motor de BD del negocio.
- **`adapters/persistence/`**: Implementaciones JPA y MongoDB que satisfacen los ports/out.
- **`adapters/rest/`**: Controllers, DTOs, mappers y manejador global de excepciones.
- **`adapters/useCases/`**: Implementaciones de los ports/in que orquestan servicios de dominio.
- **`infrastructure/security/`**: Configuración de Spring Security, JWT y filtros.

---

## 4. FLUJO DE UNA PETICIÓN HTTP

Ejemplo: **`POST /api/products`**
Cliente HTTP (Frontend / Postman)
│
│ 1. POST /api/products con JSON body + Header "Authorization: Bearer <token>"
▼
Security Filter Chain (JwtAuthenticationFilter)
│ 2. Valida firma del JWT y extrae claims (roles y usuario)
│ 3. Establece la autenticación en SecurityContextHolder
▼
ProductController (adapters/rest/controllers/ProductController.java)
│ 4. Recibe @Valid ProductCreateRequestDTO
│ 5. Invoca ProductUseCasePort.createProduct(...)
▼
ProductUseCaseImpl (adapters/useCases/ProductUseCaseImpl.java)
│ 6. Implementa ProductUseCasePort
│ 7. Delega la ejecución de reglas al ProductDomainService
▼
ProductDomainService (domain/services/ProductDomainService.java)
│ 8. Ejecuta reglas de negocio puras:
│ - Valida precio > 0 (si no, BusinessRuleException)
│ - Verifica unicidad de SKU vía ProductRepositoryPort.existsBySku()
│ - Valida existencia de categoría y vendedor
│ 9. Instancia el modelo de dominio puro Product
│ 10. Invoca ProductRepositoryPort.save(product)
▼
ProductJpaAdapter (adapters/persistence/jpa/adapters/ProductJpaAdapter.java)
│ 11. Implementa ProductRepositoryPort
│ 12. Convierte vía ProductJpaMapper.toEntity(product) → ProductJpaEntity
│ 13. Invoca ProductJpaRepository.save(entity)
▼
Base de Datos (MySQL 8)
│ 14. Ejecuta INSERT INTO productos (...)
│ 15. Retorna la fila persistida con ID autogenerado
▲
ProductJpaAdapter
│ 16. Recibe ProductJpaEntity con ID
│ 17. Transforma vía ProductJpaMapper.toDomain(savedEntity) → Product
▲
ProductUseCaseImpl
│ 18. Recibe el Product de dominio persistido
▲
ProductController
│ 19. Transforma con ProductRestMapper.toResponseDTO(product)
│ 20. Construye ResponseEntity<>(responseDTO, HttpStatus.CREATED)
▲
Cliente HTTP

Recibe HTTP 201 Created con el payload JSON del producto

text

---

## 5. MÓDULOS Y PUERTOS

| Módulo | Port In (Casos de Uso) | Port Out (Persistencia) | Domain Services | Adapters |
|--------|------------------------|-------------------------|-----------------|----------|
| **Auth** | `AuthenticationUseCasePort` | — | — | `AuthController`, `AuthenticationUseCaseImpl` |
| **Users** | `UserUseCasePort`, `BuyerUseCasePort`, `SellerUseCasePort` | `UserRepositoryPort`, `BuyerRepositoryPort`, `SellerRepositoryPort` | `UserManagementService`, `BuyerDomainService`, `SellerDomainService` | `UserController`, `BuyerController`, `SellerController` |
| **Catalog** | `ProductUseCasePort`, `CategoryUseCasePort`, `WarehouseUseCasePort`, `CatalogUseCasePort` | `ProductRepositoryPort`, `CategoryRepositoryPort`, `WarehouseRepositoryPort` | `ProductDomainService`, `CategoryDomainService`, `WarehouseDomainService` | `ProductController`, `CategoryController`, `WarehouseController`, `CatalogController` |
| **Inventory** | `InventoryUseCasePort` | `InventoryRepositoryPort` | `InventoryManagementService`, `InventoryConsultService` | `InventoryController`, `InventoryJpaAdapter` |
| **Orders** | `OrderUseCasePort` | `OrderRepositoryPort` | `OrderCreateService`, `OrderCalculationService`, `OrderLifecycleService` | `OrderController`, `OrderJpaAdapter` |
| **Billing** | `InvoiceUseCasePort`, `RefundUseCasePort` | `InvoiceRepositoryPort`, `RefundRepositoryPort` | `InvoiceGenerateService`, `InvoiceVoidService`, `RefundProcessService`, `RefundApproveService` | `InvoiceController`, `RefundController` |
| **Logistics** | `ShipmentUseCasePort`, `ReturnUseCasePort` | `ShipmentRepositoryPort`, `ReturnRepositoryPort` | `ShipmentCreateService`, `ShipmentStatusService`, `ReturnProcessService` | `ShipmentController`, `ReturnController` |
| **Audit** | `AuditUseCasePort` | `AuditLogRepositoryPort` | `RegisterAuditLogService`, `ConsultAuditLogsService` | `AuditController`, `AuditLogMongoAdapter` |

---

## 6. ENDPOINTS REST

### 6.1 Autenticación (público)

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/auth/register | Autorregistro de BUYER o SELLER |
| POST | /api/auth/login | Login y obtención de JWT |

### 6.2 Usuarios

| Método | Ruta | Rol |
|--------|------|-----|
| GET | /api/users/me | Autenticado |
| GET | /api/users/{id} | ADMIN |
| POST | /api/users | ADMIN |

### 6.3 Catálogo

| Método | Ruta | Rol |
|--------|------|-----|
| GET | /api/products | Autenticado |
| GET | /api/products/{id} | Autenticado |
| POST | /api/products | SELLER, ADMIN |
| PATCH | /api/products/{id} | SELLER, ADMIN |
| DELETE | /api/products/{id} | ADMIN |
| GET | /api/categories | Autenticado |
| POST | /api/categories | ADMIN |
| GET | /api/warehouses | Autenticado |
| GET | /api/catalog | Autenticado |

### 6.4 Inventario

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/inventories | Listar |
| POST | /api/inventories | Crear |
| PATCH | /api/inventories/{id}/reserve | Reservar |
| PATCH | /api/inventories/{id}/confirm-payment | Confirmar pago |
| PATCH | /api/inventories/{id}/mark-damaged | Marcar dañado |

### 6.5 Órdenes

| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | /api/orders | Listar |
| POST | /api/orders | Crear |
| POST | /api/orders/{id}/items | Añadir ítem |
| DELETE | /api/orders/{id}/items/{itemId} | Quitar ítem |
| PATCH | /api/orders/{id}/checkout | Checkout |
| PATCH | /api/orders/{id}/confirm-payment | Confirmar pago |
| PATCH | /api/orders/{id}/dispatch | Despachar |
| PATCH | /api/orders/{id}/deliver | Entregar |
| PATCH | /api/orders/{id}/finish | Finalizar |
| PATCH | /api/orders/{id}/cancel | Cancelar |

### 6.6 Facturación

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/invoices | Generar factura |
| GET | /api/invoices | Listar |
| PATCH | /api/invoices/{id}/void | Anular |
| POST | /api/refunds | Crear reembolso |
| PATCH | /api/refunds/{id}/approve | Aprobar |

### 6.7 Logística

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | /api/shipments | Crear envío |
| PATCH | /api/shipments/{id}/status | Actualizar estado |
| POST | /api/returns | Crear devolución |
| PATCH | /api/returns/{id}/approve | Aprobar devolución |

### 6.8 Auditoría

| Método | Ruta | Rol |
|--------|------|-----|
| GET | /api/audit | ADMIN, SUPERVISOR |
| GET | /api/audit/{id} | ADMIN, SUPERVISOR |

---

## 7. SEGURIDAD JWT

Los archivos de seguridad están en `infrastructure/security/`:

| Archivo | Rol |
|---------|-----|
| `SecurityConfig` | Configuración de Spring Security (filtros, rutas, CORS) |
| `JwtService` | Generación y validación de JWT |
| `JwtAuthenticationFilter` | Lee `Authorization: Bearer <token>` |
| `CustomUserDetailsService` | Carga usuarios desde `UserRepositoryPort` |
| `UserDetailsAdapter` | Adapta `User` (dominio) a `UserDetails` |
| `RestAuthenticationEntryPoint` | Responde 401 con `ErrorResponse` |
| `RestAccessDeniedHandler` | Responde 403 con `ErrorResponse` |

### Flujo de autenticación

1. Cliente invoca `POST /api/auth/login` con credenciales (email y password).
2. `AuthenticationUseCase` valida el hash BCrypt a través del `UserManagementService`.
3. Se genera un JWT con expiración (24 horas) y rol incrustado.
4. El cliente almacena el token y lo añade: `Authorization: Bearer eyJ...`.
5. `JwtAuthenticationFilter` extrae el token del encabezado:
   - Si no existe y el endpoint es público → continúa la cadena.
   - Si existe y es válido → resuelve el `User` y crea `UsernamePasswordAuthenticationToken` en `SecurityContextHolder`.
   - Si está expirado o corrupto → interrumpe con 401 Unauthorized.
6. Spring Security autoriza comparando autoridades con las reglas de `SecurityConfig`.

---

## 8. EXCEPCIONES Y MANEJO DE ERRORES

### 8.1 Catálogo de Excepciones de Dominio (`domain/exceptions/`)

17 excepciones que representan casos de borde y reglas de negocio:

- **ResourceNotFoundException**: ID o identificador no existe.
- **BusinessRuleException**: Infracción genérica de reglas de negocio.
- **InvalidStatusTransitionException**: Transición de estado inválida.
- **DuplicateResourceException**: Conflicto de unicidad (SKU, email).
- **CategoryHasProductsException**: Eliminar categoría con productos.
- **ProductNotAvailableException**: Producto descontinuado.
- **WarehouseCapacityExceededException**: Excede capacidad de bodega.
- **InvoiceAlreadyExistsException**: Factura duplicada para una orden.
- **InvoiceNotPayableException**: Pagar factura ya pagada o anulada.
- **RefundAmountExceededException**: Reembolso mayor al total facturado.
- **RefundNotAllowedException**: Reembolso rechazado por condiciones.
- **PaymentGatewayException**: Fallo con pasarela de pago.
- **ShipmentAlreadyExistsException**: Envío duplicado.
- **TrackingNotFoundException**: Tracking inexistente.
- **ReturnWindowExpiredException**: Devolución fuera de plazo.
- **ReturnNotAllowedException**: Devolución no permitida.
- **ReturnAlreadyProcessedException**: Devolución ya procesada.

### 8.2 Manejador Centralizado

`GlobalExceptionHandler` (en `adapters/rest/exception/`) captura todas las excepciones y las serializa en `ErrorResponse`:

```java
public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private List<String> details;
}
8.3 Mapeo de Códigos HTTP
Código	Significado	Excepciones asociadas
400	Bad Request	MethodArgumentNotValidException, HttpMessageNotReadableException
401	Unauthorized	AuthenticationException, token ausente/expirado
403	Forbidden	AccessDeniedException
404	Not Found	ResourceNotFoundException, TrackingNotFoundException
409	Conflict	InvalidStatusTransitionException, DuplicateResourceException, CategoryHasProductsException
422	Unprocessable Entity	BusinessRuleException, WarehouseCapacityExceededException, InvoiceNotPayableException
500	Internal Server Error	Exception.class (fallback)
9. TESTING
9.1 Estrategia
Pruebas unitarias con Mockito: Aíslan los ports/out con @Mock. Verifican cálculos, transiciones de estado y reglas de negocio.

Pruebas de controladores con @WebMvcTest: Validan serialización JSON, códigos HTTP, validaciones Jakarta y seguridad.

9.2 Resultados actuales
94 tests verdes (0 fallos, 0 errores).

Suite	Tests
AuditServiceTest	3
AuthControllerTest	11
AuthServiceTest	15
InvoiceServiceTest	4
RefundServiceTest	3
CatalogServiceTest	3
CategoryServiceTest	5
ProductServiceTest	4
WarehouseServiceTest	4
InventoryServiceTest	6
ReturnServiceTest	5
ShipmentServiceTest	3
OrderServiceTest	4
UserControllerTest	16
UserServiceTest	8
TOTAL	94
9.3 Ejecución
bash
mvn clean test
10. CÓMO EJECUTAR EL PROYECTO
10.1 Requisitos
JDK: 17 o superior

MySQL: 8.0+ corriendo en localhost:3306

MongoDB: local o Atlas

Maven: 3.9+

10.2 Configuración (application.properties)
properties
# ===== Servidor =====
server.port=8080
spring.application.name=nexus-market

# ===== MySQL =====
spring.datasource.url=jdbc:mysql://localhost:3306/nexusmarket?useSSL=false&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ===== JPA =====
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.open-in-view=false

# ===== MongoDB =====
spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/nexusmarket_audit}

# ===== JWT =====
jwt.secret=${JWT_SECRET:...}
jwt.expiration=86400000
10.3 Pasos
bash
# 1. Crear BD MySQL
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS nexusmarket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. Compilar
mvn clean install -DskipTests

# 3. Iniciar
mvn spring-boot:run
App disponible en http://localhost:8080.

11. RAMAS GIT Y CONTROL DE VERSIONES
11.1 Política de Ramas
main: Rama principal y definitiva. Contiene la versión oficial con Arquitectura Hexagonal completa, 94 tests verdes y desacoplamiento estricto de capas.

Ramas de migración/experimentación: Consolidadas en main, pueden eliminarse para conservar un historial limpio.

11.2 Convenciones de Commits
Seguir el estándar Conventional Commits:

feat(modulo): Nueva funcionalidad respetando puertos y adaptadores.

fix(modulo): Corrección de errores.

test(modulo): Nuevas pruebas unitarias o de integración.

refactor(modulo): Ajustes estructurales sin alterar comportamiento.

docs: Actualización de documentación.
