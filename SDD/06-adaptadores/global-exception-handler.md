# Adaptador REST — manejo global de excepciones

## Propósito

`GlobalExceptionHandler` (`@RestControllerAdvice`) es el **único** punto que
traduce excepciones a HTTP. El dominio no conoce códigos de estado, Spring ni
Jakarta Servlet.

## Jerarquía

```text
RuntimeException
└── DomainException (abstracta)
    ├── BusinessRuleException
    ├── CategoryHasProductsException
    ├── DamagedInventoryException
    ├── DuplicateResourceException
    ├── InsufficientStockException
    ├── InvalidQuantityException
    ├── InvalidRefundAmountException
    ├── InvalidStatusTransitionException
    ├── InventoryNotInitializedException
    ├── InvoiceAlreadyExistsException
    ├── InvoiceNotPayableException
    ├── InvoiceNotRefundableException
    ├── PaymentGatewayException
    ├── ProductNotAvailableException
    ├── RefundAmountExceededException
    ├── RefundNotAllowedException
    ├── ResourceNotFoundException
    ├── ReturnAlreadyProcessedException
    ├── ReturnNotAllowedException
    ├── ReturnWindowExpiredException
    ├── ShipmentAlreadyExistsException
    ├── TrackingNotFoundException
    ├── UserAlreadyExistsException
    └── WarehouseCapacityExceededException
```

`DomainException` no declara campos ni métodos con anotaciones de Spring. La
prueba `DomainExceptionContractTest` verifica por reflexión que las 24
excepciones concretas heredan de la base, no extienden `ResponseStatusException`
y no llevan anotaciones `org.springframework.*`.

## Mapeo HTTP

| Excepción | Código | Cuerpo |
|---|---|---|
| `ResourceNotFoundException` | 404 | `ErrorResponse` |
| `TrackingNotFoundException` | 404 | `ErrorResponse` |
| `DuplicateResourceException` | 409 | `ErrorResponse` |
| `UserAlreadyExistsException` | 409 | `ErrorResponse` |
| `InvalidStatusTransitionException` | 409 | `ErrorResponse` + `details` (`Current status`, `Target status`) |
| `CategoryHasProductsException` | 409 | `ErrorResponse` |
| `ProductNotAvailableException` | 409 | `ErrorResponse` |
| `InvoiceAlreadyExistsException` | 409 | `ErrorResponse` |
| `ShipmentAlreadyExistsException` | 409 | `ErrorResponse` |
| `ReturnAlreadyProcessedException` | 409 | `ErrorResponse` |
| `BusinessRuleException` | 422 | `ErrorResponse` |
| `InvalidQuantityException` | 422 | `ErrorResponse` |
| `InsufficientStockException` | 422 | `ErrorResponse` |
| `DamagedInventoryException` | 422 | `ErrorResponse` |
| `InventoryNotInitializedException` | 422 | `ErrorResponse` |
| `InvoiceNotPayableException` | 422 | `ErrorResponse` |
| `InvoiceNotRefundableException` | 422 | `ErrorResponse` |
| `InvalidRefundAmountException` | 422 | `ErrorResponse` |
| `RefundAmountExceededException` | 422 | `ErrorResponse` |
| `RefundNotAllowedException` | 422 | `ErrorResponse` |
| `ReturnWindowExpiredException` | 422 | `ErrorResponse` |
| `ReturnNotAllowedException` | 422 | `ErrorResponse` |
| `WarehouseCapacityExceededException` | 422 | `ErrorResponse` |
| `PaymentGatewayException` | 502 | `ErrorResponse` |
| `DomainException` no mapeada | 400 | `ErrorResponse` + log WARN |

`InvoiceNotPayableException` se usa para dos condiciones del mismo concepto:
orden no facturable al generar y factura pagada/inactiva al anular. Ambas
devuelven 422; el mensaje lo construye el servicio y no expone detalles internos.
`InvalidStatusTransitionException` también representa un reembolso ya procesado
(409, RF-035) y `ReturnAlreadyProcessedException` una devolución ya procesada
(409, RF-041); ambos mensajes describen el estado actual sin filtrar datos
internos.

Las siete excepciones granulares añadidas mantienen el mismo contrato:
`InvalidQuantityException` e `InsufficientStockException` distinguen cantidad
inválida de falta de stock; `DamagedInventoryException` bloquea operaciones sobre
inventario dañado; `InventoryNotInitializedException` representa una cantidad de
inventario no inicializada; `UserAlreadyExistsException` representa el duplicado
de email; `InvalidRefundAmountException` el monto no positivo; e
`InvoiceNotRefundableException` una factura inactiva o impaga. Salvo
`UserAlreadyExistsException`, que mapea a 409, las demás mapean a 422.

Las pruebas `OrderControllerTest`, `UserControllerTest` y `RefundControllerTest`
verifican el contrato HTTP real de `InsufficientStockException` (422),
`UserAlreadyExistsException` (409), `InvalidRefundAmountException` (422) y
`RefundNotAllowedException` (422) mediante `@WebMvcTest` y `MockMvc`, además de
las pruebas unitarias del handler.

### Excepciones de framework y seguridad

| Excepción | Código | Cuerpo |
|---|---|---|
| `MethodArgumentNotValidException` | 400 | `ErrorResponse` + `details` por campo |
| `AuthenticationException` | 401 | `ErrorResponse` |
| `AccessDeniedException` | 403 | `ErrorResponse` |
| `HttpMediaTypeNotSupportedException` | 415 | mensaje genérico |
| `HttpRequestMethodNotSupportedException` | 405 | mensaje genérico |
| `NoResourceFoundException` | 404 | mensaje genérico |
| `Exception` | 500 | mensaje genérico, sin stack trace |

Spring selecciona el handler **más específico** por tipo; el orden de
declaración no altera el resultado.

Los mensajes de excepciones de framework (`415`, `405`, rutas inexistentes) se
normalizan para no devolver rutas internas ni detalles del contenedor. Las
violaciones de integridad y bloqueos optimistas se registran con la URI y el
tipo de excepción, sin volcar el mensaje SQL ni valores de columnas.

## Formato de error

```json
{
  "timestamp": "2026-10-19T22:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "Cannot transition order from CART to PAID",
  "path": "/api/v1/buyer/orders/10/confirm-payment",
  "details": ["Current status: CART", "Target status: PAID"]
}
```

- 401 para `AuthenticationException`, con mensaje genérico.
- 403 para `AccessDeniedException`, sin exponer el detalle de propiedad.

## Seguridad del error 500

Para excepciones no controladas se registra el stack trace en el log del
servidor, pero el cliente recibe siempre:

```json
{"status":500,"error":"Internal Server Error","message":"An unexpected error occurred"}
```

Esto evita filtrar SQL, rutas internas o credenciales. La prueba
`GlobalExceptionHandlerTest` verifica que una excepción con mensaje sensible
(`database password leaked`) no se propague al cuerpo.

## Pruebas

`src/test/java/com/nexusmarket/adapters/rest/exception/GlobalExceptionHandlerTest.java`
usa `MockMvc` en modo standalone y cubre:

- 404 para recurso inexistente.
- 422 para regla de negocio.
- 409 para duplicidad y transición inválida.
- 400 para `DomainException` no mapeada.
- 500 con mensaje genérico.
