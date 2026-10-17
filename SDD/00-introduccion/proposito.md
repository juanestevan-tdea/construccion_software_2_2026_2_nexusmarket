# Propósito

**NexusMarket** existe como el backend de un marketplace multi-vendedor que centraliza en una única API REST el ciclo completo de comercio electrónico: autenticación y autorización con JWT y control de acceso por roles (RBAC), catálogo jerárquico de productos, bodegas e inventario, órdenes de compra con ciclo de vida transaccional, facturación, reembolsos, envíos, devoluciones y auditoría inmutable de operaciones en MongoDB. Está construido con arquitectura hexagonal (ports & adapters) sobre Spring Boot 3.4 y Java 17, de modo que el núcleo de dominio queda aislado de la tecnología de persistencia y transporte, y toda comunicación con el exterior se realiza a través de puertos (`domain/ports/in` y `domain/ports/out`) implementados por adaptadores intercambiables (REST, JPA, MongoDB).

## Objetivos medibles

| ID | Objetivo | Métrica | Cómo se verifica |
|----|----------|---------|------------------|
| OBJ-01 | Exponer el 100% de los flujos transaccionales del marketplace (registro/login, catálogo, orden, pago, despacho, factura, reembolso, devolución) a través de una API REST documentada. | 93 endpoints REST mapeados y documentados en `SDD/04-api/contratos-por-endpoint.md` y `SDD/04-api/openapi.yaml`. | Recorrido de `adapters/rest/controllers/` y revisión del `openapi.yaml` válido. |
| OBJ-02 | Mantener el núcleo de dominio puro: cero dependencias a JPA, MongoDB, Jackson ni servlets HTTP en `domain/models`, `domain/valueobjects`, `domain/ports` y `domain/exceptions`. | 0 importaciones de infraestructura en los paquetes del núcleo; 15 ports/in y 13 ports/out como únicos contratos de entrada/salida. | Revisión de `domain/` y pruebas de compilación del módulo `domain` aislado. |
| OBJ-03 | Garantizar trazabilidad completa requisitos → diseño → código: cada caso de uso en `domain/ports/in/` referenciado por al menos un requisito funcional y cada endpoint mapeado a su caso de uso. | 100% de los 15 `*UseCasePort` aparecen en `SDD/01-requisitos/casos-de-uso.md`; 100% de los 93 endpoints tienen rol, request, response y errores en `contratos-por-endpoint.md`. | Revisión de trazabilidad en `casos-de-uso.md` y `contratos-por-endpoint.md`. |
