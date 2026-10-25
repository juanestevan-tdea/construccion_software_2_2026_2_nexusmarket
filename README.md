# 🛒 NexusMarket

[![Java 17](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![Tests](https://img.shields.io/badge/Tests-224%20passed-brightgreen?style=for-the-badge&logo=junit5&logoColor=white)](https://junit.org/junit5/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=for-the-badge)](LICENSE)

Backend de un marketplace multiproveedor construido con **arquitectura hexagonal
(Ports & Adapters)** sobre **Spring Boot 4.0.0**, **Spring Framework 7**,
**Jackson 3** y **Java 17**.

> Proyecto académico de Construcción de Software II — Tecnológico de Antioquia (TdeA).
> No está desplegado en producción ni se presenta como un sistema terminado.

---

## 📦 Descripción

**NexusMarket** centraliza en una API REST el ciclo de comercio electrónico de
varios vendedores: autenticación con JWT, catálogo de productos y categorías,
bodegas e inventario, órdenes, facturación, reembolsos, envíos, devoluciones y
auditoría de operaciones y sesiones en MongoDB.

El núcleo de dominio es independiente de Spring, JPA, MongoDB y HTTP. Las
excepciones de dominio heredan de `DomainException` y el adaptador REST es el
único que decide el código HTTP.

---

## 🏗️ Arquitectura

```
┌──────────────────────────────────────────────────────────────────┐
│ ADAPTADORES DE ENTRADA                                           │
│  REST Controllers · DTOs · Mappers · GlobalExceptionHandler      │
│  Spring Security · JWT Authentication Filter                     │
└───────────────────────────────┬──────────────────────────────────┘
                                │ invoca puertos de entrada
┌───────────────────────────────▼──────────────────────────────────┐
│ APLICACIÓN / DOMINIO                                             │
│  domain/ports/in        → casos de uso e interfaces              │
│  adapters/useCases      → implementaciones de los puertos        │
│  domain/services        → reglas de negocio (SRP)                │
│  domain/models          → entidades POJO                         │
│  domain/valueobjects    → enums de dominio                       │
│  domain/exceptions      → DomainException y subtipos             │
└───────────────────────────────┬──────────────────────────────────┘
                                │ implementa puertos de salida
┌───────────────────────────────▼──────────────────────────────────┐
│ ADAPTADORES DE SALIDA                                            │
│  JPA/MySQL: entidades, repositorios, mappers, adaptadores        │
│  MongoDB: auditoría de operaciones y eventos de sesión           │
└──────────────────────────────────────────────────────────────────┘
```

Decisiones de diseño relevantes:

- Los servicios de dominio tienen una sola responsabilidad (por ejemplo,
  `CreateBuyerService`, `AddBuyerAddressService` y
  `ChangeBuyerCommercialStatusService` reemplazan al antiguo
  `BuyerDomainService`).
- `AuthenticationUseCasePort` usa `RegisterCommand`, `LoginCommand` y
  `AuthenticationResult`; ya no importa DTOs de `adapters.rest`.
- La autorización se aplica en dos niveles: reglas de ruta en `SecurityConfig`
  y `@PreAuthorize` con `@EnableMethodSecurity`. Además, los controladores de
  comprador y vendedor validan la **propiedad del recurso** contra el usuario
  del token.

---

## ⚙️ Stack tecnológico

| Capa / Rol | Tecnología | Detalle |
|---|---|---|
| Lenguaje | Java 17 | Verificado con Eclipse Temurin 17 y 21 |
| Framework | Spring Boot 4.0.0 / Spring Framework 7 | Spring MVC, IoC, transacciones |
| Seguridad | Spring Security 7 + JJWT 0.12.6 | JWT HMAC-SHA256 stateless, BCrypt |
| JSON | Jackson 3 (`tools.jackson.*`) | Migrado desde Jackson 2 |
| Persistencia relacional | MySQL 8 + Spring Data JPA / Hibernate | Transacciones ACID |
| Persistencia documental | MongoDB 7 + Spring Data Mongo | Auditoría y eventos de sesión |
| Build | Maven 3.9+ (wrapper incluido) | `mvnw` / `mvnw.cmd` |
| Pruebas | JUnit 5, Mockito, AssertJ, Spring Test | **224 pruebas** |
| Contenedores | Dockerfile multi-stage + Docker Compose | MySQL + MongoDB + App |
| CI | GitHub Actions | `./mvnw -B clean test` |

---

## 🧩 Módulos implementados

| Módulo | Estado | Observaciones |
|---|---|---|
| Autenticación y sesiones | Implementado | Registro, login, logout, cambio de contraseña, auditoría de sesión en MongoDB |
| Usuarios y roles | Implementado | CRUD administrativo, bloqueo/activación, cambio de rol |
| Compradores y vendedores | Implementado | Perfil autoservicio ligado al usuario autenticado |
| Catálogo y categorías | Implementado | Productos, categorías jerárquicas, búsqueda y detalle |
| Inventario y bodegas | Implementado | Reserva, confirmación, daño y ajuste de stock |
| Órdenes | Implementado | Carrito, ítems, checkout y ciclo de estados |
| Facturación | Implementado | Generación y anulación de facturas |
| Reembolsos | Implementado | Solicitud, aprobación y rechazo por supervisor |
| Envíos | Implementado | Creación, seguimiento y cambios de estado |
| Devoluciones | Implementado | Solicitud del comprador; aprobación/rechazo por supervisor |
| Auditoría | Implementado | Operaciones en MongoDB y consulta administrativa |
| Notificaciones | Stub | `NotificationPort` + `NoOpNotificationAdapter`, sin uso en casos de uso |

---

## 📂 Estructura del repositorio

```
construccion_software_2_2026_2_nexusmarket/
├── src/main/java/com/nexusmarket/
│   ├── domain/
│   │   ├── models/            # 15 modelos (incluye SessionEvent)
│   │   ├── valueobjects/      # 11 enums de dominio
│   │   ├── exceptions/        # DomainException + 23 excepciones concretas
│   │   ├── ports/in/          # 16 puertos de entrada (+ modelos de comando)
│   │   ├── ports/out/         # 14 puertos de salida
│   │   └── services/          # 28 servicios de dominio con SRP
│   ├── adapters/
│   │   ├── rest/              # 19 controladores, DTOs, mappers, handler global
│   │   ├── useCases/          # 16 implementaciones de puertos de entrada
│   │   └── persistence/
│   │       ├── jpa/           # 12 adaptadores, 13 entidades, 13 repositorios, 13 mappers
│   │       └── mongodb/       # Auditoría y eventos de sesión
│   └── infrastructure/
│       ├── config/            # CORS, Jackson, async
│       ├── notification/      # puerto y adaptador NoOp
│       └── security/          # JWT, filtros, SecurityConfig, handlers 401/403
├── src/test/java/             # 224 pruebas unitarias y de controlador
├── SDD/                       # Software Design Document
├── .github/workflows/ci.yml   # CI de compilación y pruebas
├── Dockerfile
├── docker-compose.yml
├── .env.example
├── pom.xml
├── README.md
├── SETUP.md
├── DOCUMENTATION.md
└── LICENSE
```

---

## 🚀 Requisitos previos

- **JDK 17** (`java -version`). El wrapper puede ejecutarse con JDK 17 o superior.
- **Docker Desktop** con Docker Compose (recomendado para MySQL y MongoDB).
- Puertos libres: `8080` (API), `3306` (MySQL) y `27017` (MongoDB).
- Opcional: MySQL 8 y MongoDB 7 instalados localmente.

---

## 🐳 Ejecución con Docker (recomendada)

```powershell
Copy-Item .env.example .env
# Edita .env y define contraseñas y JWT_SECRET
docker compose up -d --build
docker compose ps
```

La API queda en `http://localhost:8080`. Comprueba salud:

```powershell
curl.exe -s http://localhost:8080/actuator/health
```

El stack incluye `healthcheck` en MySQL y MongoDB; la aplicación espera a que
ambos estén saludables (`depends_on: condition: service_healthy`).

Detener y limpiar:

```powershell
docker compose down
# Para borrar también los datos:
docker compose down -v
```

---

## 💻 Ejecución local

1. Crea la base de datos:

```sql
CREATE DATABASE nexusmarket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Copia `.env.example` a `.env`, define `MYSQL_ROOT_PASSWORD` y `JWT_SECRET`, y levanta MySQL y MongoDB (o usa `docker compose up -d mysql mongodb`).
3. Define las variables obligatorias en la misma sesión:

```powershell
$env:DB_PASSWORD = "<tu-password-local>"
$bytes = New-Object byte[] 64
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
```

4. Compila y ejecuta:

```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target\nexus-market-1.0.0-SNAPSHOT.jar
```

5. Alternativa en desarrollo:

```powershell
.\mvnw.cmd spring-boot:run
```

La guía detallada, incluida una prueba de humo con PowerShell, está en
[SETUP.md](SETUP.md).

---

## 🔐 Variables de entorno

| Variable | Obligatoria | Descripción |
|---|---|---|
| `JWT_SECRET` | Sí | Clave HMAC de al menos 32 bytes aleatorios (se recomiendan 64). **Sin fallback**: la aplicación falla al arrancar si falta o es demasiado corta. Rotarla invalida los tokens emitidos. |
| `JWT_EXPIRATION` | No | Vida del token en milisegundos (por defecto `86400000`, 24 h). |
| `DB_URL` | Producción | JDBC URL de MySQL; por defecto apunta a `localhost`. |
| `DB_USERNAME` | Producción | Usuario de MySQL; por defecto `root` en desarrollo local. |
| `DB_PASSWORD` | Sí | Contraseña de MySQL. **Sin fallback** en la configuración versionada. |
| `MONGODB_URI` | Producción | URI de MongoDB; por defecto apunta a MongoDB local. |
| `SERVER_PORT` | No | Puerto HTTP (por defecto `8080`). |
| `JPA_SHOW_SQL` | No | `false` por defecto; activar solo en depuración local. |
| `LOG_LEVEL`, `SQL_LOG_LEVEL`, `MONGODB_LOG_LEVEL` | No | Niveles SLF4J; `INFO` por defecto para no filtrar consultas/datos. |
| `MYSQL_ROOT_PASSWORD` | Solo Docker | Contraseña root del contenedor MySQL; Compose la exige. |
| `MYSQL_DATABASE` | Solo Docker | Nombre de la base de datos. |

No hay fallback para `JWT_SECRET` ni para `DB_PASSWORD`. Define los secretos en
`.env` (ignorado por Git) o en variables de entorno del proceso. Los perfiles
locales (`application-local*.properties`) están ignorados y excluidos tanto de
Maven como de Docker; no deben contener credenciales compartidas ni publicarse.
Si una credencial estuvo expuesta, debe rotarse en el proveedor y revisarse el
plan de saneamiento de [SECURITY.md](SECURITY.md).

---

## 🔑 Seguridad y roles

Roles definidos en `UserRole`: `ADMIN`, `BUYER`, `SELLER`, `SUPERVISOR` y
`WAREHOUSE_OPERATOR`.

| Ruta | Rol requerido |
|---|---|
| `/api/v1/auth/register`, `/api/v1/auth/login` | Público |
| `/api/v1/auth/logout`, `/api/v1/auth/change-password` | Autenticado |
| `/api/v1/admin/**` | `ADMIN` (excepto `/me`, autenticado) |
| `/api/v1/seller/**` | `SELLER` |
| `/api/v1/buyer/**` | `BUYER` |
| `/api/v1/supervisor/**` | `SUPERVISOR` |
| `/api/v1/warehouse/**` | `WAREHOUSE_OPERATOR` |
| `GET /api/v1/catalog/**` | Autenticado |

Controles adicionales implementados:

- Un vendedor solo puede crear, consultar por ID, listar por `sellerId`, actualizar o desactivar **sus propios**
  productos. El `sellerId` se obtiene del token, no del cuerpo.
- Un comprador solo puede consultar y operar **sus propias** órdenes y
  devoluciones. `GET /api/v1/buyer/orders` ya no devuelve las órdenes de todos.
- La aprobación, el rechazo y la finalización de devoluciones se movieron a
  `/api/v1/supervisor/returns/**`.
- Las transiciones logísticas `dispatch`, `deliver` y `finish` solo las puede
  ejecutar un `WAREHOUSE_OPERATOR` en `/api/v1/warehouse/orders/{id}/...`.
- El cambio de estado comercial de un comprador solo lo puede ejecutar un
  `ADMIN` en `/api/v1/admin/buyers/{id}/status`.
- Los perfiles de comprador y vendedor se resuelven con el `userId` del token;
  no se aceptan identificadores de usuario en el cuerpo.

Limitaciones de seguridad conocidas:

- `JWT_SECRET` es obligatorio y se valida al arrancar (mínimo 32 bytes); no hay
  un valor por defecto en el código. Los tokens se firman explícitamente con
  HS256 y se valida firma, expiración y `tokenVersion`.
- Al bloquear una cuenta se incrementa `tokenVersion` y el filtro JWT rechaza
  usuarios no habilitados. Un token emitido antes del bloqueo deja de servir.
- No hay rotación automática de secretos ni revocación global más allá de
  `tokenVersion`/estado de la cuenta.
- El historial público de Git contiene credenciales antiguas de MongoDB. Deben
  rotarse en el proveedor y el historial debe sanearse antes de publicar; ver
  [SECURITY.md](SECURITY.md). La misión actual no reescribió el historial.
- CORS está configurado para desarrollo con orígenes localhost; se aplica antes de la autorización JWT y debe restringirse
  por entorno antes de un despliegue real.

---

## 🔌 Endpoints principales

La lista completa (96 endpoints) está en
[SDD/04-api/contratos-por-endpoint.md](SDD/04-api/contratos-por-endpoint.md) y
en [SDD/04-api/openapi.yaml](SDD/04-api/openapi.yaml).

Con la aplicación en marcha, la documentación interactiva queda publicada por
`springdoc-openapi` y es de acceso público (sin JWT):

- Swagger UI: `http://localhost:8080/swagger-ui.html` (también `/swagger-ui/index.html`)
- Contrato generado en JSON: `http://localhost:8080/v3/api-docs`
- Contrato generado en YAML: `http://localhost:8080/v3/api-docs.yaml`

| Método | Ruta | Descripción |
|---|---|---|
| `POST` | `/api/v1/auth/register` | Registro de BUYER o SELLER |
| `POST` | `/api/v1/auth/login` | Login y emisión de JWT |
| `GET` | `/api/v1/catalog` | Catálogo activo |
| `GET` | `/api/v1/catalog/search?query=` | Búsqueda de productos |
| `POST` | `/api/v1/buyer/orders` | Crear orden para el comprador autenticado |
| `POST` | `/api/v1/buyer/orders/{id}/items` | Añadir ítem a la orden |
| `PATCH` | `/api/v1/buyer/orders/{id}/checkout` | Pasar a pago pendiente |
| `PATCH` | `/api/v1/warehouse/orders/{id}/dispatch` | Despachar orden (bodega) |
| `POST` | `/api/v1/seller/products` | Crear producto del vendedor autenticado |
| `POST` | `/api/v1/warehouse/inventories` | Registrar inventario |
| `PATCH` | `/api/v1/admin/buyers/{id}/status?newStatus=` | Cambiar estado comercial (admin) |
| `POST` | `/api/v1/supervisor/invoices` | Generar factura |
| `POST` | `/api/v1/supervisor/refunds` | Crear reembolso |
| `PATCH` | `/api/v1/supervisor/returns/{id}/approve` | Aprobar devolución |

Ejemplo de registro:

```powershell
$body = @{
  email = "buyer@nexus.com"
  fullName = "Buyer Demo"
  password = "Test1234"
  role = "BUYER"
  primaryAddress = "Calle 50 #40-20"
} | ConvertTo-Json

Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/v1/auth/register `
  -ContentType "application/json" -Body $body
```

La contraseña debe tener entre 8 y 72 caracteres, al menos una letra y un
número. La misma política (`PasswordPolicy`) se aplica en el DTO, en el alta
administrativa y en el cambio de contraseña; no depende solo del controlador.

---

## 🧪 Pruebas

```powershell
.\mvnw.cmd clean test
```

Resultado esperado:

```
Tests run: 224, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Las pruebas son unitarias y de controlador con Mockito; **no requieren MySQL
ni MongoDB**. No hay pruebas E2E contra bases reales ni contra el stack Docker;
esa verificación queda documentada como pendiente.

Cobertura funcional relevante:

- Servicios de dominio y casos de uso de todos los módulos.
- Autenticación JWT, auditoría de sesión y cambio de contraseña.
- Seguridad por rol y por propiedad de recurso (`ProductController`,
  `OrderController`, `UserController`).
- Contrato del `GlobalExceptionHandler` (400, 401, 403, 404, 409, 422, 500).
- Preflight CORS antes de la autorización JWT.
- Contrato arquitectónico de excepciones de dominio.

---

## 📚 Documentación

| Documento | Contenido |
|---|---|
| [SDD/README.md](SDD/README.md) | Índice del Software Design Document |
| [SDD/00-introduccion](SDD/00-introduccion) | Propósito, alcance y glosario |
| [SDD/01-requisitos](SDD/01-requisitos) | Requisitos funcionales, no funcionales y casos de uso |
| [SDD/02-arquitectura](SDD/02-arquitectura) | C4 niveles 1–4 |
| [SDD/03-dominio](SDD/03-dominio) | Modelo, reglas de negocio y eventos |
| [SDD/04-api](SDD/04-api) | Contratos por endpoint y OpenAPI 3.1 |
| [SDD/05-despliegue](SDD/05-despliegue) | Diagrama de despliegue |
| [SDD/06-adaptadores](SDD/06-adaptadores) | Handler global, seguridad y persistencia |
| [SDD/07-trazabilidad](SDD/07-trazabilidad) | Matriz requisito → código → prueba |
| [SETUP.md](SETUP.md) | Instalación, variables y solución de problemas |
| [DOCUMENTATION.md](DOCUMENTATION.md) | Documentación técnica extendida |
| [SECURITY.md](SECURITY.md) | Secretos, rotación y plan de saneamiento del historial |

---

## ⚠️ Limitaciones conocidas

**Estado de la arquitectura (verificado por la suite):**

- **Swagger/OpenAPI implementado y público**: `springdoc-openapi` sirve
  `/swagger-ui.html` y `/v3/api-docs` sin JWT. Es el comportamiento intencional
  para la entrega; antes de un despliegue real conviene restringir esas rutas
  por perfil.
- **Dominio sin dependencias de Spring**: los **28 servicios** de
  `domain/services` son clases POJO puras. Las transacciones viven en las
  fachadas de `adapters/useCases` y el registro asíncrono de auditoría se aplica
  desde un decorador de infraestructura. `SrpArchitectureTest` falla si algún
  servicio vuelve a importar `org.springframework.*` o `jakarta.*`.

Limitaciones funcionales y operativas pendientes:

1. **Esquema con `ddl-auto=update`**: existe `db/migration/V1__add_token_version.sql`
   pero Flyway no está habilitado; el script es informativo y no se ejecuta.
2. **Sin transacciones distribuidas**: MySQL y MongoDB se actualizan por
   separado. Un fallo entre ambos puede dejar la auditoría incompleta; no hay
   patrón outbox implementado.
3. **Notificaciones no conectadas**: el adaptador es un NoOp.
4. **Sin pruebas de integración reales** contra MySQL/MongoDB ni pruebas E2E.
5. **Listados autoservicio**: `GET /api/v1/buyer/buyers` y
   `GET /api/v1/seller/sellers` devuelven solo el perfil propio. Los listados
   globales deberían exponerse en rutas administrativas.
6. **Listado global de productos en ruta de vendedor**:
   `GET /api/v1/seller/products` sigue devolviendo productos de todos los
   vendedores; se recomienda moverlo al catálogo o a una ruta administrativa.

---

## 📄 Licencia

Este proyecto está bajo la Licencia MIT. Consulta [LICENSE](LICENSE).
