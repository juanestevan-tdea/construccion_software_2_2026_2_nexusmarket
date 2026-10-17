# C4 — Nivel 2: Contenedores

Diagrama de contenedores: la API REST, los adaptadores de persistencia, la base de datos relacional (MySQL), MongoDB y el sistema externo de notificaciones.

```mermaid
C4Container
    title Contenedores - NexusMarket (C4 Nivel 2)

    Person(user, "Usuario del sistema", "Comprador, vendedor, administrador, supervisor u operador de bodega")

    Container_Boundary(c1, "NexusMarket Application") {
        Container(api, "API REST (Spring Boot 3.4)", "Java 17, Spring MVC, Spring Security + JWT", "Puerto 8080")
        Container(controllers, "Controladores REST", "15 @RestController en adapters/rest/controllers", "Dentro de api")
        Container(useCases, "Casos de uso", "15 implementaciones de ports/in en adapters/useCases", "Dentro de api")
        Container(domain, "Núcleo de dominio", "Modelos, value objects, excepciones, servicios, puertos in/out", "Dentro de api")
        Container(jpaAdapter, "Adaptador de persistencia JPA", "12 adaptadores JPA + 13 entidades + 13 repositorios + 13 mappers", "Dentro de api")
        Container(mongoAdapter, "Adaptador de persistencia MongoDB", "AuditLogMongoAdapter + documento + repositorio + mapper", "Dentro de api")
        Container(security, "Infraestructura de seguridad", "JwtService, filtro JWT, SecurityConfig, entry point/denied handler", "Dentro de api")
        Container(notification, "Puerto de notificaciones", "NotificationPort + NoOpNotificationAdapter (stub)", "Dentro de api")
    }

    ContainerDb(mysql, "MySQL 8.x", "Base de datos relacional transacional", "Puerto 3306")
    ContainerDb(mongo, "MongoDB 7.x", "Base de datos documental de auditoría", "Puerto 27017")
    Container_Ext(notifSys, "Servicio de Notificaciones", "Email/push externo", "System_Ext")

    Rel(user, api, "Llamadas HTTP autenticadas (Bearer JWT)", "HTTPS / JSON")
    Rel(api, controllers, "Ruta peticiones a", "Spring MVC")
    Rel(controllers, useCases, "Invoca puertos de entrada", "Java interfaces")
    Rel(useCases, domain, "Orquesta lógica de negocio y servicios de dominio", "Java")
    Rel(useCases, jpaAdapter, "Guarda y consulta agregados", "Java interfaces (ports/out)")
    Rel(useCases, mongoAdapter, "Escribe logs de auditoría", "Java interfaces (ports/out)")
    Rel(useCases, notification, "Envía notificaciones de negocio", "NotificationPort")
    Rel(api, security, "Autenticación y autorización por petición", "Spring Security Filter Chain")
    Rel(jpaAdapter, mysql, "SQL transaccional (JDBC)", "Spring Data JPA / Hibernate")
    Rel(mongoAdapter, mongo, "Operaciones CRUD de auditoría", "Spring Data MongoDB")
    Rel(api, notifSys, "Notificaciones (stub en desarrollo)", "NotificationPort")

    BiRel(user, mysql, "(ninguna — acceso solo vía API)", "")
```

## Resumen de contenedores

| Contenedor | Tecnología | Detalle |
|------------|------------|---------|
| API REST | Spring Boot 3.4 / Java 17 | 15 controladores, Puerto 8080 |
| Adaptador JPA | Spring Data JPA + Hibernate | MySQL, 13 entidades, 13 repositorios |
| Adaptador MongoDB | Spring Data MongoDB | Auditoría (AuditLog) |
| Seguridad | Spring Security + JWT | Stateless, HMAC-SHA256, RBAC 5 roles |
| Notificaciones | `NotificationPort` (NoOp) | Stub sin implementar |
| BD relacional | MySQL 8.x | Puerto 3306, BD `nexusmarket` |
| BD documental | MongoDB 7.x | Puerto 27017, BD `nexusmarket_audit` |
