# Servicios de dominio y responsabilidades

Inventario de los 23 servicios de `domain/services` y su responsabilidad
principal. La columna **SRP** indica si el servicio tiene una única razón para
cambiar.

| Servicio | Responsabilidad | SRP |
|---|---|---|
| `AddBuyerAddressService` | Añadir dirección adicional a un comprador | Sí |
| `ChangeBuyerCommercialStatusService` | Cambiar el estado comercial del comprador | Sí |
| `CreateBuyerService` | Validar rol/duplicados y crear comprador | Sí |
| `CategoryDomainService` | Alta, actualización y borrado de categorías | Sí |
| `ConsultAuditLogsService` | Consultar auditoría por rango/fecha | Sí |
| `InventoryConsultService` | Consultas de inventario | Sí |
| `InventoryManagementService` | Reserva, confirmación, daño y ajuste de stock | Sí |
| `InvoiceGenerateService` | Generar factura de una orden | Sí |
| `InvoiceVoidService` | Anular factura | Sí |
| `OrderCalculationService` | Recalcular totales de la orden | Sí |
| `OrderCreateService` | Crear orden en estado `CART` | Sí |
| `OrderLifecycleService` | Transiciones de estado de la orden | Sí |
| `ProductDomainService` | Validaciones y CRUD de productos | Sí |
| `RefundApproveService` | Aprobar/rechazar reembolso | Sí |
| `RefundProcessService` | Procesar el reembolso aprobado | Sí |
| `RegisterAuditLogService` | Registrar una operación auditada | Sí |
| `ReturnProcessService` | Ciclo de vida de devoluciones | Sí |
| `SellerDomainService` | Alta, actualización y activación de vendedores | Sí |
| `SessionAuditService` | Registrar eventos de sesión (best-effort) | Sí |
| `ShipmentCreateService` | Crear envío | Sí |
| `ShipmentStatusService` | Cambiar estado del envío | Sí |
| `UserManagementService` | Crear usuario, bloquear, activar, cambiar rol, invalidar sesiones y cambiar contraseña | **No** |
| `WarehouseDomainService` | CRUD y validaciones de bodegas | Sí |

## Deuda identificada: `UserManagementService`

El servicio agrupa seis operaciones con motivos de cambio distintos:

1. Alta de usuario.
2. Bloqueo/activación (estado).
3. Cambio de rol.
4. Invalidación de sesiones (`tokenVersion`).
5. Cambio de contraseña.
6. Búsquedas (`getUserByIdOrThrow`, `findAll`, `findByEmail`, `findByRole`).

### División propuesta

| Nuevo servicio | Métodos extraídos |
|---|---|
| `UserCreationService` | `createUser` |
| `UserQueryService` | `getUserByIdOrThrow`, `findAll`, `findByEmail`, `findByRole` |
| `UserStatusService` | `blockUser`, `activateUser`, `changeRole` |
| `SessionInvalidationService` | `invalidateSessions` |
| `PasswordChangeService` | `changePassword` |

`UserUseCaseImpl` y `AuthenticationUseCaseImpl` delegarían en estos servicios,
igual que `BuyerUseCaseImpl` delega en los tres servicios de comprador.

**No se aplicó en esta iteración** para no romper los 149 tests verdes sin una
prueba de integración que cubra el flujo completo. Queda registrado como deuda
técnica prioritaria.

## `AuthenticationUseCaseImpl`

Implementa cuatro operaciones del puerto: `register`, `login`, `logout` y
`changePassword`. Es un **orquestador** de casos de uso, no un servicio de
dominio, por lo que su tamaño es aceptable; aun así, una separación en
`RegistrationService`, `LoginService`, `LogoutService` y `PasswordChangeService`
reduciría el acoplamiento con JWT, auditoría y perfiles.

## Servicios con `@Service` y `@Transactional`

Los servicios usan anotaciones de Spring (`@Service`, `@Transactional`). Esto
facilita la inyección y las transacciones, pero acopla `domain/services` al
contenedor. La alternativa sería declarar los beans en
`infrastructure/config` y dejar los servicios como POJOs. Es una mejora
arquitectónica pendiente; no se aplicó para no reescribir la configuración de
todos los módulos.
