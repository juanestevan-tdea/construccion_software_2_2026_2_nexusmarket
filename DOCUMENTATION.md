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
| `ErrorResponse` | - | DTO estándar de respuesta de error (timestamp, status, message). |

Todas son capturadas por el `GlobalExceptionHandler` (`@RestControllerAdvice`), que garantiza respuestas de error consistentes en toda la API.