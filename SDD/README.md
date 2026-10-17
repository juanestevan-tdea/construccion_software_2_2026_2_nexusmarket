# 📐 Software Design Document (SDD) — NexusMarket

Documento de diseño formal del backend de marketplace multi-vendedor **NexusMarket**, construido con **arquitectura hexagonal (ports & adapters)**, **Spring Boot 3.4**, **Java 17**, **Spring Security + JWT**, **MySQL (JPA)** y **MongoDB (auditoría)**.

## Índice de contenido

| Sección | Archivo | Descripción |
|---------|---------|-------------|
| 00. Introducción | [00-introduccion/proposito.md](00-introduccion/proposito.md) | Propósito y objetivos medibles |
| 00. Introducción | [00-introduccion/alcance.md](00-introduccion/alcance.md) | Alcance: qué cubre y qué no el sistema |
| 00. Introducción | [00-introduccion/glosario.md](00-introduccion/glosario.md) | Glosario de términos de dominio |
| 01. Requisitos | [01-requisitos/requisitos-funcionales.md](01-requisitos/requisitos-funcionales.md) | RF-001…RF-043, por módulo, con actor y criterios de aceptación |
| 01. Requisitos | [01-requisitos/requisitos-no-funcionales.md](01-requisitos/requisitos-no-funcionales.md) | RNF-001…RNF-007 |
| 01. Requisitos | [01-requisitos/casos-de-uso.md](01-requisitos/casos-de-uso.md) | Trazabilidad RF → casos de uso (`domain/ports/in/`) |
| 02. Arquitectura | [02-arquitectura/c4-nivel-1-contexto.md](02-arquitectura/c4-nivel-1-contexto.md) | C4 Nivel 1: Contexto |
| 02. Arquitectura | [02-arquitectura/c4-nivel-2-contenedores.md](02-arquitectura/c4-nivel-2-contenedores.md) | C4 Nivel 2: Contenedores |
| 02. Arquitectura | [02-arquitectura/c4-nivel-3-componentes.md](02-arquitectura/c4-nivel-3-componentes.md) | C4 Nivel 3: Componentes por módulo |
| 02. Arquitectura | [02-arquitectura/c4-nivel-4-codigo.md](02-arquitectura/c4-nivel-4-codigo.md) | C4 Nivel 4: Código (classDiagram por módulo) |
| 03. Dominio | [03-dominio/modelo-de-dominio.md](03-dominio/modelo-de-dominio.md) | Entidades, relaciones y classDiagram del agregado Order |
| 03. Dominio | [03-dominio/reglas-de-negocio.md](03-dominio/reglas-de-negocio.md) | RN-001… con invariante en código |
| 03. Dominio | [03-dominio/eventos-de-dominio.md](03-dominio/eventos-de-dominio.md) | Eventos de dominio, payload y consumidores |
| 04. API | [04-api/contratos-por-endpoint.md](04-api/contratos-por-endpoint.md) | Contratos de los 93 endpoints (rol, request, response, errores) |
| 04. API | [04-api/openapi.yaml](04-api/openapi.yaml) | Especificación OpenAPI 3.1 generada de los controladores reales |
| 05. Despliegue | [05-despliegue/diagrama-despliegue.md](05-despliegue/diagrama-despliegue.md) | Diagrama de despliegue (docker-compose) |

## 🚨 Gaps detectados

Los siguientes hallazgos reales se detectaron durante el recorrido del código y se documentan aquí (no se agregan funcionalidades al SDD, solo se evidencian):

1. **Dependencia de dominio hacia adaptador REST** — `domain/ports/in/AuthenticationUseCasePort.java` importa `com.nexusmarket.adapters.rest.dtos.requests.LoginRequestDTO`, `RegisterRequestDTO` y `com.nexusmarket.adapters.rest.dtos.responses.AuthResponseDTO`. Rompe el principio hexagonal de que `domain/` no conoce la capa de `adapters/`.
2. **Autorización más gruesa de lo documentado** — `SecurityConfig` exige solo `authenticated` para escrituras de catálogo (`POST/PATCH/DELETE /api/products`), mientras el README declara roles `SELLER` / `ADMIN`. Solo `/api/users/**` (salvo `/me`) exige `ADMIN` y `/api/audit/**` exige `ADMIN` o `SUPERVISOR`.
3. **OpenAPI no servido automáticamente** — `SecurityConfig` permite `/v3/api-docs/**` y `/swagger-ui/**`, pero `pom.xml` no incluye `springdoc-openapi`; no hay generación automática de OpenAPI. Este SDD incluye `openapi.yaml` escrito a mano a partir de los controladores reales.
4. **Notificaciones son un stub** — `infrastructure/notification/NotificationPort` y `NoOpNotificationAdapter` existen pero no están conectadas a ningún caso de uso; `NotificationPort` no se invoca desde `adapters/useCases/`.

## Documentación relacionada

- [DOCUMENTATION.md](../DOCUMENTATION.md) — Documentación técnica completa
- [README.md](../README.md) — Overview del proyecto
- [Especificacion-Funcional.pdf](Especificacion-Funcional.pdf) — Especificación funcional del negocio (material de clase)

## Estructura de paquetes del código (resumen)

```
com.nexusmarket/
├── domain/                     # Núcleo puro (sin Spring/JPA/Mongo)
│   ├── models/                 # 14 POJOs (Order, User, Product, ...)
│   ├── valueobjects/           # 10 enums (UserRole, OrderStatus, ...)
│   ├── exceptions/             # 17 excepciones de negocio
│   ├── ports/
│   │   ├── in/                 # 15 interfaces de casos de uso
│   │   └── out/                # 13 interfaces de persistencia
│   └── services/               # 20 servicios de dominio
├── adapters/
│   ├── rest/                   # 15 controladores, DTOs, mappers, GlobalExceptionHandler
│   ├── persistence/jpa/        # 12 adaptadores, 13 entidades, 13 repositorios, 13 mappers
│   ├── persistence/mongodb/    # Adaptador de auditoría (AuditLog)
│   └── useCases/               # 15 implementaciones de ports/in
└── infrastructure/             # security (JWT), config, notification (NoOp)
```
