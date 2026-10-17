# C4 — Nivel 1: Contexto

Diagrama de contexto del sistema NexusMarket con los 5 actores de negocio y los sistemas externos.

```mermaid
C4Context
    title Contexto del Sistema - NexusMarket (C4 Nivel 1)

    Person(buyer, "Comprador", "BUYER: registra órdenes, consulta catálogo, gestiona perfil y devoluciones")
    Person(seller, "Vendedor", "SELLER: publica productos y gestiona su catálogo y bodegas")
    Person(admin, "Administrador", "ADMIN: gestión total de usuarios, categorías y marketplace")
    Person(supervisor, "Supervisor", "SUPERVISOR: aprueba y rechaza reembolsos y devoluciones")
    Person(warehouseOperator, "Operador de Bodega", "WAREHOUSE_OPERATOR: gestiona bodegas, recepciones y despachos")

    System(nxm, "NexusMarket API REST", "Backend marketplace multi-vendedor. Spring Boot 3.4, Java 17, arquitectura hexagonal (ports & adapters).", "http://localhost:8080/api")

    System_Ext(mysql, "MySQL 8.x", "Persistencia relacional transaccional: usuarios, compradores, vendedores, productos, categorías, bodegas, inventario, órdenes, ítems, facturas, reembolsos, envíos, devoluciones.", "jdbc:mysql://localhost:3306/nexusmarket")
    System_Ext(mongo, "MongoDB 7.x", "Persistencia documental de auditoría inmutable: logs de operaciones (AuditLog).", "mongodb://localhost:27017/nexusmarket_audit")
    System_Ext(notif, "Servicio de Notificaciones", "Email / push para eventos de negocio. En desarrollo se usa adaptador NoOp (stub).", "NotificationPort")

    Rel(buyer, nxm, "Llama a la API REST (autenticado, JWT Bearer)", "HTTPS / JSON")
    Rel(seller, nxm, "Llama a la API REST (autenticado, JWT Bearer)", "HTTPS / JSON")
    Rel(admin, nxm, "Llama a la API REST (autenticado, JWT Bearer, rol ADMIN)", "HTTPS / JSON")
    Rel(supervisor, nxm, "Llama a la API REST (autenticado, JWT Bearer, rol SUPERVISOR)", "HTTPS / JSON")
    Rel(warehouseOperator, nxm, "Llama a la API REST (autenticado, JWT Bearer, rol WAREHOUSE_OPERATOR)", "HTTPS / JSON")

    Rel(nxm, mysql, "Lee y escribe datos transaccionales", "Spring Data JPA / Hibernate (JDBC)")
    Rel(nxm, mongo, "Escribe logs de auditoría inmutables", "Spring Data MongoDB")
    Rel(nxm, notif, "Envía notificaciones de negocio", "NotificationPort (adaptador NoOp en desarrollo)")
```

## Actores (5 roles)

| Actor | Rol en el código | Endpoints principales |
|-------|------------------|-----------------------|
| Comprador | `BUYER` | `/api/orders`, `/api/returns`, `/api/users/me`, `/api/buyers` |
| Vendedor | `SELLER` | `/api/products`, `/api/sellers` |
| Administrador | `ADMIN` | `/api/users/**`, `/api/audit/**`, administración completa |
| Supervisor | `SUPERVISOR` | `/api/audit/**`, aprobación de reembolsos/devoluciones |
| Operador de Bodega | `WAREHOUSE_OPERATOR` | `/api/warehouses`, `/api/inventories`, `/api/shipments`, despacho de órdenes |

## Sistemas externos

| Sistema | Tecnología | Responsabilidad |
|---------|------------|-----------------|
| MySQL | MySQL 8.x + Spring Data JPA | Datos transaccionales del marketplace |
| MongoDB | MongoDB 7.x + Spring Data Mongo | Auditoría inmutable de operaciones |
| Servicio de Notificaciones | `NotificationPort` / `NoOpNotificationAdapter` | Notificaciones email/push (stub en desarrollo) |
