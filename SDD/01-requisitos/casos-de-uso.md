# Casos de uso y trazabilidad RF → Código

Cada requisito funcional (RF) se mapea a su **interfaz de caso de uso** en `domain/ports/in/` y a su **implementación** en `adapters/useCases/`. Todos los 15 puertos de entrada aparecen referenciados al menos una vez.

| RF | Caso de uso (método) | Interfaz `domain/ports/in/` | Implementación `adapters/useCases/` | Módulo |
|----|----------------------|-----------------------------|-------------------------------------|--------|
| RF-001 | `register(RegisterCommand)` | `AuthenticationUseCasePort` | `AuthenticationUseCaseImpl` | auth |
| RF-002 | `login(LoginCommand)` | `AuthenticationUseCasePort` | `AuthenticationUseCaseImpl` | auth |
| RF-003 | `createUser(email, fullName, password, role)` | `UserUseCasePort` | `UserUseCaseImpl` | users |
| RF-004 | (resolución del principal autenticado, sin método de port dedicado) | `UserUseCasePort` (ctx de seguridad) | `UserUseCaseImpl` | users |
| RF-005 | `getUserByIdOrThrow`, `getUserByEmailOrThrow`, `findAll`, `findByRole`, `blockUser`, `activateUser`, `changeRole` | `UserUseCasePort` | `UserUseCaseImpl` | users |
| RF-006 | `createBuyer(userId, primaryAddress)` | `BuyerUseCasePort` | `BuyerUseCaseImpl` | buyers |
| RF-007 | `getBuyerByIdOrThrow`, `findAll`, `findByCommercialStatus`, `addAdditionalAddress`, `changeCommercialStatus` | `BuyerUseCasePort` | `BuyerUseCaseImpl` | buyers |
| RF-008 | `createSeller(userId, taxId, companyName)` | `SellerUseCasePort` | `SellerUseCaseImpl` | sellers |
| RF-009 | `getSellerByIdOrThrow`, `getSellerByTaxIdOrThrow`, `findAll`, `activateSeller`, `deactivateSeller`, `updateSeller` | `SellerUseCasePort` | `SellerUseCaseImpl` | sellers |
| RF-010 | `createProduct(sku, name, description, price, type, sellerId, categoryId)` | `ProductUseCasePort` | `ProductUseCaseImpl` | catalog |
| RF-011 | `getProductByIdOrThrow(id)` | `ProductUseCasePort` | `ProductUseCaseImpl` | catalog |
| RF-012 | `findAll`, `findByCategory`, `findBySeller`, `findByPriceRange` | `ProductUseCasePort` | `ProductUseCaseImpl` | catalog |
| RF-013 | `updateProduct(id, name, description, price, type, categoryId)` | `ProductUseCasePort` | `ProductUseCaseImpl` | catalog |
| RF-014 | `deactivateProduct(id)` | `ProductUseCasePort` | `ProductUseCaseImpl` | catalog |
| RF-015 | `createCategory(name, description, parentId)` | `CategoryUseCasePort` | `CategoryUseCaseImpl` | catalog |
| RF-016 | `findAll`, `findRootCategories`, `getCategoryByIdOrThrow` | `CategoryUseCasePort` | `CategoryUseCaseImpl` | catalog |
| RF-017 | `updateCategory(id, name, description, parentId)`, `deleteCategory(id)` | `CategoryUseCasePort` | `CategoryUseCaseImpl` | catalog |
| RF-018 | `createWarehouse(...)`, `getWarehouseByIdOrThrow`, `findAll`, `findByType`, `updateWarehouse(...)` | `WarehouseUseCasePort` | `WarehouseUseCaseImpl` | catalog |
| RF-019 | `getActiveProducts`, `getRootCategories`, `searchProducts(query)`, `getProductDetail(id)` | `CatalogUseCasePort` | `CatalogUseCaseImpl` | catalog |
| RF-020 | `createInventory(productId, warehouseId, quantity)` | `InventoryUseCasePort` | `InventoryUseCaseImpl` | inventory |
| RF-021 | `getByIdOrThrow`, `findAll`, `findByProduct`, `findByWarehouse` | `InventoryUseCasePort` | `InventoryUseCaseImpl` | inventory |
| RF-022 | `reserve`, `confirmPayment`, `markAsDamaged`, `adjust` | `InventoryUseCasePort` | `InventoryUseCaseImpl` | inventory |
| RF-023 | `createOrder(buyerId)` | `OrderUseCasePort` | `OrderUseCaseImpl` | orders |
| RF-024 | `addItem(orderId, productId, quantity)`, `removeItem(orderId, itemId)` | `OrderUseCasePort` | `OrderUseCaseImpl` | orders |
| RF-025 | `checkout(id)` | `OrderUseCasePort` | `OrderUseCaseImpl` | orders |
| RF-026 | `confirmPayment(id)` | `OrderUseCasePort` | `OrderUseCaseImpl` | orders |
| RF-027 | `dispatch(id)`, `deliver(id)`, `finish(id)` | `OrderUseCasePort` | `OrderUseCaseImpl` | orders |
| RF-028 | `cancel(id)` | `OrderUseCasePort` | `OrderUseCaseImpl` | orders |
| RF-029 | `getByIdOrThrow`, `findAll`, `findByBuyer` | `OrderUseCasePort` | `OrderUseCaseImpl` | orders |
| RF-030 | `generateInvoice(orderId, pdfUrl)` | `InvoiceUseCasePort` | `InvoiceUseCaseImpl` | billing |
| RF-031 | `getByIdOrThrow`, `findByOrder`, `findAll` | `InvoiceUseCasePort` | `InvoiceUseCaseImpl` | billing |
| RF-032 | `voidInvoice(id)` | `InvoiceUseCasePort` | `InvoiceUseCaseImpl` | billing |
| RF-033 | `createRefund(invoiceId, returnRequestId, amount)` | `RefundUseCasePort` | `RefundUseCaseImpl` | refunds |
| RF-034 | `getByIdOrThrow`, `findByInvoice`, `findAll` | `RefundUseCasePort` | `RefundUseCaseImpl` | refunds |
| RF-035 | `approveRefund(id)`, `rejectRefund(id)` | `RefundUseCasePort` | `RefundUseCaseImpl` | refunds |
| RF-036 | `createShipment(orderId, warehouseId, trackingNumber)` | `ShipmentUseCasePort` | `ShipmentUseCaseImpl` | shipping |
| RF-037 | `getByIdOrThrow`, `findByTracking`, `findByOrder`, `findAll` | `ShipmentUseCasePort` | `ShipmentUseCaseImpl` | shipping |
| RF-038 | `updateStatus(id, status)` | `ShipmentUseCasePort` | `ShipmentUseCaseImpl` | shipping |
| RF-039 | `createReturn(orderId, reason)` | `ReturnUseCasePort` | `ReturnUseCaseImpl` | returns |
| RF-040 | `getByIdOrThrow`, `findByOrder`, `findAll` | `ReturnUseCasePort` | `ReturnUseCaseImpl` | returns |
| RF-041 | `approveReturn(id)`, `rejectReturn(id)`, `completeReturn(id)` | `ReturnUseCasePort` | `ReturnUseCaseImpl` | returns |
| RF-042 | `log(userId, performedBy, entityName, entityId, action, details)` | `AuditUseCasePort` | `AuditUseCaseImpl` | auditing |
| RF-043 | `findAll`, `findByUser`, `findByEntity`, `findByDateRange`, `getByIdOrThrow` | `AuditUseCasePort` | `AuditUseCaseImpl` | auditing |

## Cobertura de puertos de entrada

Todos los 15 puertos de `domain/ports/in/` están cubiertos:

1. `AuthenticationUseCasePort` → RF-001, RF-002
2. `UserUseCasePort` → RF-003, RF-004, RF-005
3. `BuyerUseCasePort` → RF-006, RF-007
4. `SellerUseCasePort` → RF-008, RF-009
5. `ProductUseCasePort` → RF-010…RF-014
6. `CategoryUseCasePort` → RF-015…RF-017
7. `CatalogUseCasePort` → RF-019
8. `WarehouseUseCasePort` → RF-018
9. `InventoryUseCasePort` → RF-020…RF-022
10. `OrderUseCasePort` → RF-023…RF-029
11. `InvoiceUseCasePort` → RF-030…RF-032
12. `RefundUseCasePort` → RF-033…RF-035
13. `ShipmentUseCasePort` → RF-036…RF-038
14. `ReturnUseCasePort` → RF-039…RF-041
15. `AuditUseCasePort` → RF-042, RF-043
