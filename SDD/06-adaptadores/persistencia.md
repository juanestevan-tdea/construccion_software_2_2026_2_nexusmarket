# Adaptadores de persistencia

## Principio

El dominio define puertos (`domain/ports/out`) y no conoce JPA, Hibernate ni
MongoDB. Los adaptadores implementan esos puertos y traducen entre modelos de
dominio y entidades/documentos.

```text
DomainService / UseCase
        │ usa
        ▼
XxxRepositoryPort (dominio)
        ▲ implementa
        │
XxxJpaAdapter ──► XxxJpaRepository ──► XxxJpaEntity ──► MySQL
        │
        └── XxxJpaMapper (dominio ⇄ entidad)
```

## MySQL (Spring Data JPA)

| Elemento | Cantidad | Ubicación |
|---|---|---|
| Adaptadores | 12 | `adapters/persistence/jpa/adapters` |
| Entidades | 13 | `adapters/persistence/jpa/entities` |
| Repositorios Spring Data | 13 | `adapters/persistence/jpa/repositories` |
| Mappers | 13 | `adapters/persistence/jpa/mappers` |

Entidades y tablas principales:

| Entidad | Tabla | Relaciones |
|---|---|---|
| `UserJpaEntity` | `usuarios` | — |
| `BuyerJpaEntity` | `compradores` | `@OneToOne` User |
| `SellerJpaEntity` | `vendedores` | `@OneToOne` User |
| `CategoryJpaEntity` | `categorias` | jerarquía padre/hijos |
| `ProductJpaEntity` | `productos` | `@ManyToOne` Seller, Category |
| `WarehouseJpaEntity` | `bodegas` | — |
| `InventoryJpaEntity` | `inventarios` | `@ManyToOne` Product, Warehouse |
| `OrderJpaEntity` | `pedidos` | `@ManyToOne` Buyer; `@OneToMany` items |
| `OrderItemJpaEntity` | `pedido_items` | `@ManyToOne` Order, Product |
| `InvoiceJpaEntity` | `facturas` | `@OneToOne` Order |
| `RefundJpaEntity` | `reembolsos` | `@ManyToOne` Invoice, Return |
| `ShipmentJpaEntity` | `envios` | `@OneToOne` Order |
| `ReturnJpaEntity` | `devoluciones` | `@ManyToOne` Order |

Convenciones:

- `spring.jpa.open-in-view=false` para evitar consultas implícitas en la capa
  web.
- `@Transactional` en servicios de dominio que mutan estado.
- Índices declarados en `@Table(indexes = ...)` para claves foráneas y estados.
- Los mappers JPA no exponen entidades fuera del adaptador.

## MongoDB (auditoría)

| Componente | Función |
|---|---|
| `AuditLogDocument` | Operación auditada: usuario, entidad, acción, timestamp |
| `SessionEventDocument` | Evento de sesión: login, login fallido, logout, cambio de contraseña |
| `AuditLogMongoAdapter` | Implementa `AuditLogRepositoryPort` |
| `SessionEventMongoAdapter` | Implementa `SessionAuditPort` |
| `SessionEventQueryUseCaseImpl` | Consulta administrativa de eventos a través de `SessionAuditPort` |

- TTL de 90 días (`@Indexed(expireAfter = "PT7776000S")`).
- La auditoría es best-effort: un fallo de Mongo no revierte la operación de
  MySQL. `SessionAuditService` captura y registra el fallo una sola vez; el adaptador no lo oculta ni lo registra de nuevo.
- La consulta administrativa elige una consulta derivada distinta según los filtros opcionales; así `email` o `type` ausentes no se interpretan como igualdad `null` en MongoDB.
- No hay transacciones distribuidas ni patrón outbox.

## Esquema y migraciones

`application.properties` usa:

```properties
spring.jpa.hibernate.ddl-auto=update
```

Hibernate crea y actualiza el esquema a partir de las entidades. Existe
`src/main/resources/db/migration/V1__add_token_version.sql`, pero **Flyway no
está incluido en `pom.xml`**, por lo que el script no se ejecuta.

Recomendación para producción:

1. Añadir `flyway-core` y `flyway-mysql`.
2. Cambiar a `spring.jpa.hibernate.ddl-auto=validate`.
3. Versionar el esquema con migraciones y probarlas en CI con Testcontainers.

No se aplicó en esta iteración para no alterar el arranque actual sin una
verificación contra una base real.

## Pruebas

Las pruebas actuales mockean los puertos de persistencia; no levantan MySQL ni
MongoDB. Por tanto:

- Está verificado el contrato de los adaptadores a nivel de código y de
  servicios.
- **No** está verificado el mapeo real JPA→tabla ni las consultas derivadas
  contra una base real.
- **No** hay pruebas de integración con Testcontainers.

Esta limitación se documenta explícitamente en el informe de auditoría.
