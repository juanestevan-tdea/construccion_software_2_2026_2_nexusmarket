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
    ├── DuplicateResourceException
    ├── InvalidStatusTransitionException
    ├── InvoiceAlreadyExistsException
    ├── InvoiceNotPayableException
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
    └── WarehouseCapacityExceededException
```

`DomainException` no declara campos ni métodos con anotaciones de Spring. La
prueba `DomainExceptionContractTest` verifica por reflexión que las 17
excepciones concretas heredan de la base y no llevan anotaciones
`org.springframework.*`.

## Mapeo HTTP

| Excepción | Código | Cuerpo |
|---|---|---|
| `ResourceNotFoundException` | 404 | `ErrorResponse` |
| `TrackingNotFoundException` | 404 | `ErrorResponse` |
| `DuplicateResourceException` | 409 | `ErrorResponse` |
| `InvalidStatusTransitionException` | 409 | `ErrorResponse` + `details` (`Current status`, `Target status`) |
| `CategoryHasProductsException` | 409 | `ErrorResponse` |
| `ProductNotAvailableException` | 409 | `ErrorResponse` |
| `InvoiceAlreadyExistsException` | 409 | `ErrorResponse` |
| `ShipmentAlreadyExistsException` | 409 | `ErrorResponse` |
| `ReturnAlreadyProcessedException` | 409 | `ErrorResponse` |
| `BusinessRuleException` | 422 | `ErrorResponse` |
| `InvoiceNotPayableException` | 422 | `ErrorResponse` |
| `RefundAmountExceededException` | 422 | `ErrorResponse` |
| `RefundNotAllowedException` | 422 | `ErrorResponse` |
| `ReturnWindowExpiredException` | 422 | `ErrorResponse` |
| `ReturnNotAllowedException` | 422 | `ErrorResponse` |
| `WarehouseCapacityExceededException` | 422 | `ErrorResponse` |
| `PaymentGatewayException` | 502 | `ErrorResponse` |
| `DomainException` no mapeada | 400 | `ErrorResponse` + log WARN |
| `MethodArgumentNotValidException` | 400 | `ErrorResponse` + `details` por campo |
| `AuthenticationException` | 401 | `ErrorResponse` |
| `AccessDeniedException` | 403 | `ErrorResponse` |
| `Exception` | 500 | mensaje genérico, sin stack trace |

Spring selecciona el handler **más específico** por tipo; el orden de
declaración no altera el resultado.

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
