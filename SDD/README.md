# Software Design Document (SDD) — NexusMarket

Documento de diseño del backend de marketplace multi-vendedor **NexusMarket**,
construido con **arquitectura hexagonal (Ports & Adapters)** sobre
**Spring Boot 4.0.0**, **Spring Framework 7**, **Jackson 3**, **Java 17**,
**Spring Security + JWT**, **MySQL (JPA)** y **MongoDB (auditoría)**.

> Estado: proyecto académico. El SDD describe la implementación real; los
> aspectos no verificados o pendientes se marcan explícitamente.

## Índice de contenido

| Sección | Archivo | Descripción |
|---------|---------|-------------|
| 00. Introducción | [00-introduccion/proposito.md](00-introduccion/proposito.md) | Propósito y objetivos medibles |
| 00. Introducción | [00-introduccion/alcance.md](00-introduccion/alcance.md) | Alcance: qué cubre y qué no el sistema |
| 00. Introducción | [00-introduccion/glosario.md](00-introduccion/glosario.md) | Glosario de términos de dominio |
| 01. Requisitos | [01-requisitos/requisitos-funcionales.md](01-requisitos/requisitos-funcionales.md) | RF por módulo, actor y criterios de aceptación |
| 01. Requisitos | [01-requisitos/requisitos-no-funcionales.md](01-requisitos/requisitos-no-funcionales.md) | RNF del sistema |
| 01. Requisitos | [01-requisitos/casos-de-uso.md](01-requisitos/casos-de-uso.md) | Trazabilidad RF → casos de uso |
| 02. Arquitectura | [02-arquitectura/c4-nivel-1-contexto.md](02-arquitectura/c4-nivel-1-contexto.md) | C4 Nivel 1: contexto |
| 02. Arquitectura | [02-arquitectura/c4-nivel-2-contenedores.md](02-arquitectura/c4-nivel-2-contenedores.md) | C4 Nivel 2: contenedores |
| 02. Arquitectura | [02-arquitectura/c4-nivel-3-componentes.md](02-arquitectura/c4-nivel-3-componentes.md) | C4 Nivel 3: componentes por módulo |
| 02. Arquitectura | [02-arquitectura/c4-nivel-4-codigo.md](02-arquitectura/c4-nivel-4-codigo.md) | C4 Nivel 4: diagramas de clase |
| 03. Dominio | [03-dominio/modelo-de-dominio.md](03-dominio/modelo-de-dominio.md) | Entidades, relaciones y agregado Order |
| 03. Dominio | [03-dominio/reglas-de-negocio.md](03-dominio/reglas-de-negocio.md) | Reglas con invariante en código |
| 03. Dominio | [03-dominio/eventos-de-dominio.md](03-dominio/eventos-de-dominio.md) | Eventos y consumidores |
| 03. Dominio | [03-dominio/servicios-y-responsabilidades.md](03-dominio/servicios-y-responsabilidades.md) | Inventario y responsabilidad de los 28 servicios |
| 03. Dominio | [03-dominio/matriz-srp.md](03-dominio/matriz-srp.md) | Matriz de hallazgos SRP y cambios aplicados |
| 03. Dominio | [03-dominio/auditoria-servicios-excepciones.md](03-dominio/auditoria-servicios-excepciones.md) | Auditoría completa de servicios, casos de uso y excepciones |
| 04. API | [04-api/contratos-por-endpoint.md](04-api/contratos-por-endpoint.md) | Contratos de los 96 endpoints |
| 04. API | [04-api/openapi.yaml](04-api/openapi.yaml) | Especificación OpenAPI 3.1 mantenida a mano |
| 05. Despliegue | [05-despliegue/diagrama-despliegue.md](05-despliegue/diagrama-despliegue.md) | Docker Compose: MySQL + MongoDB + App |
| 06. Adaptadores | [06-adaptadores/global-exception-handler.md](06-adaptadores/global-exception-handler.md) | Excepciones de dominio y mapeo HTTP |
| 06. Adaptadores | [06-adaptadores/seguridad-y-autorizacion.md](06-adaptadores/seguridad-y-autorizacion.md) | JWT, roles y propiedad de recursos |
| 06. Adaptadores | [06-adaptadores/persistencia.md](06-adaptadores/persistencia.md) | Adaptadores JPA y MongoDB |
| 07. Trazabilidad | [07-trazabilidad/matriz-trazabilidad.md](07-trazabilidad/matriz-trazabilidad.md) | Requisito → código → prueba |

## Hallazgos y correcciones aplicadas

1. **Dependencia de dominio hacia REST (corregida).**
   `AuthenticationUseCasePort` importaba `LoginRequestDTO`, `RegisterRequestDTO`
   y `AuthResponseDTO`. Ahora usa `LoginCommand`, `RegisterCommand` y
   `AuthenticationResult` en `domain/ports/in/model`. El controller traduce.
2. **Autorización por método inerte (corregida).**
   `@PreAuthorize` no tenía efecto porque faltaba `@EnableMethodSecurity`.
   Se habilitó y se añadió `@PreAuthorize("isAuthenticated()")` a
   `GET /api/v1/admin/users/me`.
3. **Suplantación de identidad en cuerpos (corregida).**
   `sellerId`, `buyerId` y `userId` ya no se aceptan en los cuerpos de creación;
   se resuelven desde el JWT. Los controladores de comprador y vendedor validan
   la propiedad del recurso antes de leer o modificar.
4. **Fuga de datos entre compradores (corregida).**
   `GET /api/v1/buyer/orders` usaba `findAll()`. Ahora filtra por el comprador
   autenticado; las devoluciones y perfiles siguen la misma regla.
5. **Devoluciones aprobadas por el propio comprador (corregida).**
   Los endpoints de aprobación, rechazo y finalización se movieron a
   `/api/v1/supervisor/returns/**`.
6. **Excepciones acopladas a Spring (corregida).**
   Se añadió `DomainException` y las 17 excepciones concretas heredan de ella.
   `GlobalExceptionHandler` mapea 404/409/422/400/502 y tiene un fallback 400
   para excepciones de dominio no mapeadas.
7. **OpenAPI no servido automáticamente (pendiente).**
   `SecurityConfig` permite `/v3/api-docs/**` y `/swagger-ui/**`, pero no se
   incluye `springdoc-openapi`. El contrato vive en `04-api/openapi.yaml`.
8. **Notificaciones sin uso (pendiente).**
   `NotificationPort` y `NoOpNotificationAdapter` existen, pero ningún caso de
   uso los invoca.
9. **Esquema sin Flyway (pendiente).**
   `spring.jpa.hibernate.ddl-auto=update` gobierna el esquema. Existe
   `V1__add_token_version.sql`, pero Flyway no está en el `pom.xml`.
10. **SRP de usuarios y controladores (corregida).**
   `UserManagementService` se dividió en cinco servicios cohesionados.
   Los controladores ya no inyectan repositorios de salida y
   `SessionAuditService` dejó de depender de `jakarta.servlet`. El detalle
   está en [03-dominio/matriz-srp.md](03-dominio/matriz-srp.md).
11. **Transiciones de orden y estado comercial fuera de rol (corregida).**
   `dispatch`, `deliver` y `finish` se movieron a
   `/api/v1/warehouse/orders/{id}/...` (`WAREHOUSE_OPERATOR`). El cambio de
   estado comercial del comprador se movió a
   `/api/v1/admin/buyers/{id}/status` (`ADMIN`).
12. **Anulación de facturas pagadas (corregida).**
   `InvoiceVoidService` permitía anular una factura pagada si existía un
   reembolso aprobado y no rechazaba una factura ya inactiva. Ahora solo anula
   facturas activas e impagas, conforme a RN-010/RF-032. Ver
   [03-dominio/auditoria-servicios-excepciones.md](03-dominio/auditoria-servicios-excepciones.md).

## Documentación relacionada

- [README.md](../README.md) — visión general del proyecto
- [SETUP.md](../SETUP.md) — instalación y ejecución
- [DOCUMENTATION.md](../DOCUMENTATION.md) — documentación técnica extendida
- [SECURITY.md](../SECURITY.md) — estado de secretos y plan de saneamiento del historial
- [Especificacion-Funcional.pdf](Especificacion-Funcional.pdf) — material de clase
  (instantánea; las cifras vigentes de pruebas y configuración están en los
  documentos Markdown de esta carpeta)

## Estructura de paquetes (resumen)

```
com.nexusmarket/
├── domain/
│   ├── models/          # 15 modelos (incluye SessionEvent)
│   ├── valueobjects/    # 11 enums
│   ├── exceptions/      # DomainException + 17 subtipos
│   ├── ports/in/        # 16 puertos + modelos de comando/resultado
│   ├── ports/out/       # 14 puertos de persistencia
│   └── services/        # 28 servicios de dominio (SRP)
├── adapters/
│   ├── rest/            # 19 controladores, DTOs, mappers, handler
│   ├── persistence/
│   │   ├── jpa/         # 12 adaptadores, 13 entidades/repos/mappers
│   │   └── mongodb/     # auditoría y sesiones
│   └── useCases/        # 16 implementaciones de puertos de entrada
└── infrastructure/      # security (JWT), config, notification
```
