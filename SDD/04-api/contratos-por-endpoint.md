# Contratos de API por Endpoint

Este documento cubre el **100% de los 93 endpoints REST** encontrados en `adapters/rest/controllers/` (15 controladores). Cada bloque indica el **rol requerido** (según `SecurityConfig`), el **request JSON de ejemplo**, el **response 2xx** y la **tabla de errores 4xx/5xx**.

> Convención de roles: `PÚBLICO` (sin token), `AUTENTICADO` (cualquier rol con JWT válido), `ADMIN`, `ADMIN o SUPERVISOR`, `BUYER`. Nota de ajuste: `SecurityConfig` protege escrituras de catálogo solo como `AUTENTICADO` (gap documentado en `SDD/README.md`).

> Códigos de error comunes (aplican a todos los endpoints salvo que se indique lo contrario):
> | Código | Significado | Causa típica |
> |--------|-------------|--------------|
> | 400 | Bad Request | DTO inválido (`@Valid`), parámetro faltante o mal formado |
> | 401 | Unauthorized | Token ausente, expirado o inválido (`AuthenticationException`) |
> | 403 | Forbidden | Rol insuficiente (`AccessDeniedException`) |
> | 404 | Not Found | Recurso inexistente (`ResourceNotFoundException`) |
> | 409 | Conflict | Transición inválida, duplicidad, categoría con productos |
> | 422 | Unprocessable Entity | Regla de negocio violada (`BusinessRuleException`) |
> | 500 | Internal Server Error | Error no controlado (fallback `Exception.class`) |
> | Cuerpo de error | `ErrorResponse` (`status`, `error`, `message`, `path`, `timestamp`, `details`)

---

## Módulo: Autenticación (auth)

### POST /api/auth/register
- **Rol requerido:** PÚBLICO (permitAll)
- **Controlador:** `AuthController.register`
- **Caso de uso:** `AuthenticationUseCasePort.register(RegisterRequestDTO)`
- **Request JSON de ejemplo:**
  ```json
  {
    "email": "comprador@nexus.com",
    "fullName": "Juan Pérez",
    "password": "Password123",
    "role": "BUYER",
    "primaryAddress": "Calle 123 #45-67",
    "taxId": null,
    "companyName": null
  }
  ```
- **Response 201 Created:** `AuthResponseDTO`
  ```json
  {
    "tokenType": "Bearer",
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "expiresIn": 86400000,
    "id": 1,
    "email": "comprador@nexus.com",
    "fullName": "Juan Pérez",
    "role": "BUYER"
  }
  ```
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | Email inválido, contraseña < 6 caracteres, rol nulo | `MethodArgumentNotValidException` |
  | 409 | Email ya registrado | `DuplicateResourceException` |
  | 500 | Error interno | `Exception` (fallback) |

---

### POST /api/auth/login
- **Rol requerido:** PÚBLICO (permitAll)
- **Controlador:** `AuthController.login`
- **Caso de uso:** `AuthenticationUseCasePort.login(LoginRequestDTO)`
- **Request JSON de ejemplo:**
  ```json
  {
    "email": "comprador@nexus.com",
    "password": "Password123"
  }
  ```
- **Response 200 OK:** `AuthResponseDTO` (misma estructura que register)
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | Email o contraseña vacíos | `MethodArgumentNotValidException` |
  | 401 | Credenciales inválidas | `AuthenticationException` |
  | 500 | Error interno | `Exception` (fallback) |

---

## Módulo: Usuarios (users)

### POST /api/users
- **Rol requerido:** ADMIN
- **Controlador:** `UserController.createUser`
- **Caso de uso:** `UserUseCasePort.createUser(email, fullName, password, role)`
- **Request JSON de ejemplo:**
  ```json
  {
    "email": "vendedor@nexus.com",
    "fullName": "María Gómez",
    "password": "Pass456",
    "role": "SELLER"
  }
  ```
- **Response 201 Created:** `UserResponseDTO`
  ```json
  {
    "id": 2,
    "email": "vendedor@nexus.com",
    "fullName": "María Gómez",
    "role": "SELLER",
    "status": "ACTIVE",
    "createdAt": "2026-01-15T10:30:00",
    "updatedAt": "2026-01-15T10:30:00"
  }
  ```
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | DTO inválido | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 403 | Rol no ADMIN | `AccessDeniedException` |
  | 409 | Email duplicado | `DuplicateResourceException` |

---

### GET /api/users/me
- **Rol requerido:** AUTENTICADO (cualquier rol)
- **Controlador:** `UserController.getCurrentUser`
- **Caso de uso:** lectura del `UserDetailsAdapter` del `SecurityContextHolder`
- **Request:** sin body
- **Response 200 OK:** `UserResponseDTO` del usuario autenticado
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 401 | Sin sesión válida | `AuthenticationException` / `BadCredentialsException` |

---

### GET /api/users/{id}
- **Rol requerido:** ADMIN
- **Controlador:** `UserController.getUserById`
- **Caso de uso:** `UserUseCasePort.getUserByIdOrThrow(Long)`
- **Request:** path variable `id` (Long)
- **Response 200 OK:** `UserResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 401 | Sin autenticación | `AuthenticationException` |
  | 403 | Rol no ADMIN | `AccessDeniedException` |
  | 404 | Usuario inexistente | `ResourceNotFoundException` |

---

### GET /api/users/email?email=
- **Rol requerido:** ADMIN
- **Controlador:** `UserController.getUserByEmail`
- **Caso de uso:** `UserUseCasePort.getUserByEmailOrThrow(String)`
- **Request:** query param `email`
- **Response 200 OK:** `UserResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 401 | Sin autenticación | `AuthenticationException` |
  | 403 | Rol no ADMIN | `AccessDeniedException` |
  | 404 | Usuario inexistente | `ResourceNotFoundException` |

---

### GET /api/users
- **Rol requerido:** ADMIN
- **Controlador:** `UserController.getAllUsers`
- **Caso de uso:** `UserUseCasePort.findAll()`
- **Request:** sin body
- **Response 200 OK:** `List<UserResponseDTO>`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 401 | Sin autenticación | `AuthenticationException` |
  | 403 | Rol no ADMIN | `AccessDeniedException` |

---

### GET /api/users/role/{role}
- **Rol requerido:** ADMIN
- **Controlador:** `UserController.getUsersByRole`
- **Caso de uso:** `UserUseCasePort.findByRole(UserRole)`
- **Request:** path variable `role` (UserRole: BUYER, SELLER, WAREHOUSE_OPERATOR, ADMIN, SUPERVISOR)
- **Response 200 OK:** `List<UserResponseDTO>`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | Rol inválido | `MethodArgumentTypeMismatchException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 403 | Rol no ADMIN | `AccessDeniedException` |

---

### PATCH /api/users/{id}/block
- **Rol requerido:** ADMIN
- **Controlador:** `UserController.blockUser`
- **Caso de uso:** `UserUseCasePort.blockUser(Long)`
- **Request:** path variable `id` (Long)
- **Response 200 OK:** `UserResponseDTO` (status = BLOCKED)
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 401 | Sin autenticación | `AuthenticationException` |
  | 403 | Rol no ADMIN | `AccessDeniedException` |
  | 404 | Usuario inexistente | `ResourceNotFoundException` |

---

### PATCH /api/users/{id}/activate
- **Rol requerido:** ADMIN
- **Controlador:** `UserController.activateUser`
- **Caso de uso:** `UserUseCasePort.activateUser(Long)`
- **Request:** path variable `id` (Long)
- **Response 200 OK:** `UserResponseDTO` (status = ACTIVE)
- **Tabla de errores:** igual que block.

---

### PATCH /api/users/{id}/role?newRole=
- **Rol requerido:** ADMIN
- **Controlador:** `UserController.changeUserRole`
- **Caso de uso:** `UserUseCasePort.changeRole(Long, UserRole)`
- **Request:** path variable `id`, query param `newRole` (UserRole)
- **Response 200 OK:** `UserResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | Rol inválido | `MethodArgumentTypeMismatchException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 403 | Rol no ADMIN | `AccessDeniedException` |
  | 404 | Usuario inexistente | `ResourceNotFoundException` |

---

## Módulo: Compradores (buyers)

### POST /api/buyers
- **Rol requerido:** AUTENTICADO
- **Controlador:** `BuyerController.createBuyer`
- **Caso de uso:** `BuyerUseCasePort.createBuyer(userId, primaryAddress)`
- **Request JSON de ejemplo:**
  ```json
  {
    "userId": 1,
    "primaryAddress": "Calle 123 #45-67"
  }
  ```
- **Response 201 Created:** `BuyerResponseDTO` (incluye `user` embebido)
  ```json
  {
    "id": 1,
    "user": { "id": 1, "email": "comprador@nexus.com", "fullName": "Juan Pérez", "role": "BUYER", "status": "ACTIVE", "createdAt": "...", "updatedAt": "..." },
    "primaryAddress": "Calle 123 #45-67",
    "additionalAddresses": [],
    "commercialStatus": "ACTIVE"
  }
  ```
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | userId nulo o primaryAddress vacío | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 404 | Usuario inexistente | `ResourceNotFoundException` |

---

### GET /api/buyers/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `BuyerController.getBuyerById`
- **Caso de uso:** `BuyerUseCasePort.getBuyerByIdOrThrow(Long)`
- **Response 200 OK:** `BuyerResponseDTO`
- **Tabla de errores:** 401, 404 (comprador inexistente).

---

### GET /api/buyers
- **Rol requerido:** AUTENTICADO
- **Controlador:** `BuyerController.getAllBuyers`
- **Caso de uso:** `BuyerUseCasePort.findAll()`
- **Response 200 OK:** `List<BuyerResponseDTO>`
- **Tabla de errores:** 401.

---

### GET /api/buyers/status/{status}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `BuyerController.getBuyersByStatus`
- **Caso de uso:** `BuyerUseCasePort.findByCommercialStatus(BuyerCommercialStatus)`
- **Request:** path variable `status` (ACTIVE, INACTIVE, BLOCKED)
- **Response 200 OK:** `List<BuyerResponseDTO>`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | Estado inválido | `MethodArgumentTypeMismatchException` |
  | 401 | Sin autenticación | `AuthenticationException` |

---

### POST /api/buyers/{id}/addresses?address=
- **Rol requerido:** AUTENTICADO
- **Controlador:** `BuyerController.addAddress`
- **Caso de uso:** `BuyerUseCasePort.addAdditionalAddress(Long, String)`
- **Request:** path variable `id`, query param `address`
- **Response 200 OK:** `BuyerResponseDTO`
- **Tabla de errores:** 401, 404.

---

### PATCH /api/buyers/{id}/status?newStatus=
- **Rol requerido:** AUTENTICADO
- **Controlador:** `BuyerController.changeCommercialStatus`
- **Caso de uso:** `BuyerUseCasePort.changeCommercialStatus(Long, BuyerCommercialStatus)`
- **Request:** path variable `id`, query param `newStatus`
- **Response 200 OK:** `BuyerResponseDTO`
- **Tabla de errores:** 400 (estado inválido), 401, 404.

---

## Módulo: Vendedores (sellers)

### POST /api/sellers
- **Rol requerido:** AUTENTICADO
- **Controlador:** `SellerController.createSeller`
- **Caso de uso:** `SellerUseCasePort.createSeller(userId, taxId, companyName)`
- **Request JSON de ejemplo:**
  ```json
  {
    "userId": 2,
    "taxId": "900.123.456-7",
    "companyName": "TechStore SAS"
  }
  ```
- **Response 201 Created:** `SellerResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | DTO inválido | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 404 | Usuario inexistente | `ResourceNotFoundException` |
  | 409 | taxId duplicado | `DuplicateResourceException` |

---

### GET /api/sellers/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `SellerController.getSellerById`
- **Caso de uso:** `SellerUseCasePort.getSellerByIdOrThrow(Long)`
- **Response 200 OK:** `SellerResponseDTO`
- **Tabla de errores:** 401, 404.

---

### GET /api/sellers/taxId?taxId=
- **Rol requerido:** AUTENTICADO
- **Controlador:** `SellerController.getSellerByTaxId`
- **Caso de uso:** `SellerUseCasePort.getSellerByTaxIdOrThrow(String)`
- **Request:** query param `taxId`
- **Response 200 OK:** `SellerResponseDTO`
- **Tabla de errores:** 401, 404.

---

### GET /api/sellers
- **Rol requerido:** AUTENTICADO
- **Controlador:** `SellerController.getAllSellers`
- **Caso de uso:** `SellerUseCasePort.findAll()`
- **Response 200 OK:** `List<SellerResponseDTO>`
- **Tabla de errores:** 401.

---

### PATCH /api/sellers/{id}/activate
- **Rol requerido:** AUTENTICADO
- **Controlador:** `SellerController.activateSeller`
- **Caso de uso:** `SellerUseCasePort.activateSeller(Long)`
- **Response 200 OK:** `SellerResponseDTO`
- **Tabla de errores:** 401, 404.

---

### PATCH /api/sellers/{id}/deactivate
- **Rol requerido:** AUTENTICADO
- **Controlador:** `SellerController.deactivateSeller`
- **Caso de uso:** `SellerUseCasePort.deactivateSeller(Long)`
- **Response 200 OK:** `SellerResponseDTO`
- **Tabla de errores:** 401, 404.

---

### PATCH /api/sellers/{id}/update
- **Rol requerido:** AUTENTICADO
- **Controlador:** `SellerController.updateSeller`
- **Caso de uso:** `SellerUseCasePort.updateSeller(Long, companyName, taxId)`
- **Request JSON de ejemplo:**
  ```json
  {
    "companyName": "TechStore Updated SAS",
    "taxId": "900.123.456-7"
  }
  ```
- **Response 200 OK:** `SellerResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | taxId o companyName > longitud | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 404 | Vendedor inexistente | `ResourceNotFoundException` |
  | 409 | taxId duplicado | `DuplicateResourceException` |

---

## Módulo: Catálogo — productos

### POST /api/products
- **Rol requerido:** AUTENTICADO (intención de negocio: SELLER/ADMIN)
- **Controlador:** `ProductController.createProduct`
- **Caso de uso:** `ProductUseCasePort.createProduct(sku, name, description, price, type, sellerId, categoryId)`
- **Request JSON de ejemplo:**
  ```json
  {
    "sku": "SKU-001",
    "name": "Laptop Dell",
    "description": "Laptop 16GB RAM",
    "price": 1250000.00,
    "type": "PHYSICAL",
    "sellerId": 2,
    "categoryId": 5
  }
  ```
- **Response 201 Created:** `ProductResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | DTO inválido (precio ≤ 0) | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 404 | Vendedor o categoría inexistentes | `ResourceNotFoundException` |
  | 409 | SKU duplicado | `DuplicateResourceException` |

---

### GET /api/products/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ProductController.getProductById`
- **Caso de uso:** `ProductUseCasePort.getProductByIdOrThrow(Long)`
- **Response 200 OK:** `ProductResponseDTO`
- **Tabla de errores:** 401, 404.

---

### GET /api/products
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ProductController.getAllProducts`
- **Caso de uso:** `ProductUseCasePort.findAll()`
- **Response 200 OK:** `List<ProductResponseDTO>`
- **Tabla de errores:** 401.

---

### GET /api/products/category/{categoryId}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ProductController.getProductsByCategory`
- **Caso de uso:** `ProductUseCasePort.findByCategory(Long)`
- **Response 200 OK:** `List<ProductResponseDTO>`
- **Tabla de errores:** 401.

---

### GET /api/products/seller/{sellerId}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ProductController.getProductsBySeller`
- **Caso de uso:** `ProductUseCasePort.findBySeller(Long)`
- **Response 200 OK:** `List<ProductResponseDTO>`
- **Tabla de errores:** 401.

---

### GET /api/products/price-range?min=&max=
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ProductController.getProductsByPriceRange`
- **Caso de uso:** `ProductUseCasePort.findByPriceRange(BigDecimal, BigDecimal)`
- **Request:** query params `min`, `max` (BigDecimal)
- **Response 200 OK:** `List<ProductResponseDTO>`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | Parámetros faltantes o no numéricos | `MissingServletRequestParameterException` / `MethodArgumentTypeMismatchException` |
  | 401 | Sin autenticación | `AuthenticationException` |

---

### PATCH /api/products/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ProductController.updateProduct`
- **Caso de uso:** `ProductUseCasePort.updateProduct(id, name, description, price, type, categoryId)`
- **Request JSON de ejemplo:**
  ```json
  {
    "name": "Laptop Dell Actualizada",
    "price": 1199000.00,
    "type": "PHYSICAL",
    "categoryId": 5
  }
  ```
- **Response 200 OK:** `ProductResponseDTO`
- **Tabla de errores:** 400, 401, 404.

---

### DELETE /api/products/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ProductController.deleteProduct`
- **Caso de uso:** `ProductUseCasePort.deactivateProduct(Long)` (soft delete, active = false)
- **Response 200 OK:** `ProductResponseDTO` (desactivado)
- **Tabla de errores:** 401, 404.

---

## Módulo: Catálogo — categorías

### POST /api/categories
- **Rol requerido:** AUTENTICADO
- **Controlador:** `CategoryController.createCategory`
- **Caso de uso:** `CategoryUseCasePort.createCategory(name, description, parentId)`
- **Request JSON de ejemplo:**
  ```json
  {
    "name": "Electrónica",
    "description": "Productos electrónicos",
    "parentId": null
  }
  ```
- **Response 201 Created:** `CategoryResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | Nombre vacío | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 409 | Nombre duplicado | `DuplicateResourceException` |

---

### GET /api/categories
- **Rol requerido:** AUTENTICADO
- **Controlador:** `CategoryController.getAllCategories`
- **Caso de uso:** `CategoryUseCasePort.findAll()`
- **Response 200 OK:** `List<CategoryResponseDTO>`
- **Tabla de errores:** 401.

---

### GET /api/categories/roots
- **Rol requerido:** AUTENTICADO
- **Controlador:** `CategoryController.getRootCategories`
- **Caso de uso:** `CategoryUseCasePort.findRootCategories()`
- **Response 200 OK:** `List<CategoryResponseDTO>` (categorías sin padre)
- **Tabla de errores:** 401.

---

### GET /api/categories/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `CategoryController.getCategoryById`
- **Caso de uso:** `CategoryUseCasePort.getCategoryByIdOrThrow(Long)`
- **Response 200 OK:** `CategoryResponseDTO`
- **Tabla de errores:** 401, 404.

---

### PATCH /api/categories/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `CategoryController.updateCategory`
- **Caso de uso:** `CategoryUseCasePort.updateCategory(Long, name, description, parentId)`
- **Request JSON de ejemplo:**
  ```json
  {
    "name": "Electrónica y Computadores",
    "description": "Productos electrónicos y accesorios",
    "parentId": null
  }
  ```
- **Response 200 OK:** `CategoryResponseDTO`
- **Tabla de errores:** 400, 401, 404.

---

### DELETE /api/categories/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `CategoryController.deleteCategory`
- **Caso de uso:** `CategoryUseCasePort.deleteCategory(Long)`
- **Response 204 No Content:** sin body
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 401 | Sin autenticación | `AuthenticationException` |
  | 404 | Categoría inexistente | `ResourceNotFoundException` |
  | 409 | Categoría tiene productos | `CategoryHasProductsException` |

---

## Módulo: Catálogo — bodegas

### POST /api/warehouses
- **Rol requerido:** AUTENTICADO
- **Controlador:** `WarehouseController.createWarehouse`
- **Caso de uso:** `WarehouseUseCasePort.createWarehouse(name, location, type, capacity)`
- **Request JSON de ejemplo:**
  ```json
  {
    "name": "Bodega Norte",
    "location": "Bogotá, Colombia",
    "type": "MARKETPLACE",
    "capacity": 1000
  }
  ```
- **Response 201 Created:** `WarehouseResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | capacidad < 1 | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |

---

### GET /api/warehouses
- **Rol requerido:** AUTENTICADO
- **Controlador:** `WarehouseController.getAllWarehouses`
- **Caso de uso:** `WarehouseUseCasePort.findAll()`
- **Response 200 OK:** `List<WarehouseResponseDTO>`
- **Tabla de errores:** 401.

---

### GET /api/warehouses/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `WarehouseController.getWarehouseById`
- **Caso de uso:** `WarehouseUseCasePort.getWarehouseByIdOrThrow(Long)`
- **Response 200 OK:** `WarehouseResponseDTO`
- **Tabla de errores:** 401, 404.

---

### GET /api/warehouses/type/{type}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `WarehouseController.getWarehousesByType`
- **Caso de uso:** `WarehouseUseCasePort.findByType(WarehouseType)`
- **Request:** path variable `type` (MARKETPLACE, SELLER)
- **Response 200 OK:** `List<WarehouseResponseDTO>`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | Tipo inválido | `MethodArgumentTypeMismatchException` |
  | 401 | Sin autenticación | `AuthenticationException` |

---

### PATCH /api/warehouses/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `WarehouseController.updateWarehouse`
- **Caso de uso:** `WarehouseUseCasePort.updateWarehouse(id, name, location, type, capacity)`
- **Request JSON de ejemplo:**
  ```json
  {
    "name": "Bodega Norte",
    "location": "Medellín, Colombia",
    "type": "MARKETPLACE",
    "capacity": 1500
  }
  ```
- **Response 200 OK:** `WarehouseResponseDTO`
- **Tabla de errores:** 400, 401, 404.

---

## Módulo: Catálogo general y búsqueda

### GET /api/catalog
- **Rol requerido:** AUTENTICADO
- **Controlador:** `CatalogController.getCatalog`
- **Caso de uso:** `CatalogUseCasePort.getActiveProducts()`, `getRootCategories()`
- **Request:** sin body
- **Response 200 OK:** `CatalogOverviewResponseDTO`
  ```json
  {
    "products": [ {...ProductResponseDTO...} ],
    "categories": [ {...CategoryResponseDTO...} ],
    "totalProducts": 42,
    "totalCategories": 8
  }
  ```
- **Tabla de errores:** 401.

---

### GET /api/catalog/search?query=
- **Rol requerido:** AUTENTICADO
- **Controlador:** `CatalogController.searchProducts`
- **Caso de uso:** `CatalogUseCasePort.searchProducts(String)`
- **Request:** query param `query` (opcional)
- **Response 200 OK:** `List<ProductResponseDTO>`
- **Tabla de errores:** 401.

---

### GET /api/catalog/products/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `CatalogController.getProductDetail`
- **Caso de uso:** `CatalogUseCasePort.getProductDetail(Long)`
- **Response 200 OK:** `ProductResponseDTO`
- **Tabla de errores:** 401, 404.

---

## Módulo: Inventario

### POST /api/inventories
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InventoryController.createInventory`
- **Caso de uso:** `InventoryUseCasePort.createInventory(productId, warehouseId, quantity)`
- **Request JSON de ejemplo:**
  ```json
  {
    "productId": 10,
    "warehouseId": 3,
    "quantity": 50
  }
  ```
- **Response 201 Created:** `InventoryResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | productId/warehouseId nulos, quantity < 0 | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |

---

### GET /api/inventories/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InventoryController.getInventoryById`
- **Caso de uso:** `InventoryUseCasePort.getByIdOrThrow(Long)`
- **Response 200 OK:** `InventoryResponseDTO`
- **Tabla de errores:** 401, 404.

---

### GET /api/inventories
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InventoryController.getAllInventories`
- **Caso de uso:** `InventoryUseCasePort.findAll()`
- **Response 200 OK:** `List<InventoryResponseDTO>`
- **Tabla de errores:** 401.

---

### GET /api/inventories/product/{productId}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InventoryController.getInventoriesByProduct`
- **Caso de uso:** `InventoryUseCasePort.findByProduct(Long)`
- **Response 200 OK:** `List<InventoryResponseDTO>`
- **Tabla de errores:** 401.

---

### GET /api/inventories/warehouse/{warehouseId}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InventoryController.getInventoriesByWarehouse`
- **Caso de uso:** `InventoryUseCasePort.findByWarehouse(Long)`
- **Response 200 OK:** `List<InventoryResponseDTO>`
- **Tabla de errores:** 401.

---

### PATCH /api/inventories/{id}/reserve?amount=
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InventoryController.reserve`
- **Caso de uso:** `InventoryUseCasePort.reserve(Long, int)`
- **Request:** path variable `id`, query param `amount` (int)
- **Response 200 OK:** `InventoryResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 401 | Sin autenticación | `AuthenticationException` |
  | 404 | Inventario inexistente | `ResourceNotFoundException` |
  | 422 | Insuficiente cantidad o dañado | `BusinessRuleException` |

---

### PATCH /api/inventories/{id}/confirm-payment
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InventoryController.confirmPayment`
- **Caso de uso:** `InventoryUseCasePort.confirmPayment(Long)`
- **Response 200 OK:** `InventoryResponseDTO`
- **Tabla de errores:** 401, 404, 422 (dañado).

---

### PATCH /api/inventories/{id}/mark-damaged
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InventoryController.markAsDamaged`
- **Caso de uso:** `InventoryUseCasePort.markAsDamaged(Long)`
- **Response 200 OK:** `InventoryResponseDTO`
- **Tabla de errores:** 401, 404.

---

### PATCH /api/inventories/{id}/adjust?amount=
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InventoryController.adjust`
- **Caso de uso:** `InventoryUseCasePort.adjust(Long, int)`
- **Request:** path variable `id`, query param `amount` (int)
- **Response 200 OK:** `InventoryResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 401 | Sin autenticación | `AuthenticationException` |
  | 404 | Inventario inexistente | `ResourceNotFoundException` |
  | 422 | Resultado negativo | `BusinessRuleException` |

---

## Módulo: Órdenes

### POST /api/orders
- **Rol requerido:** BUYER
- **Controlador:** `OrderController.createOrder`
- **Caso de uso:** `OrderUseCasePort.createOrder(Long)`
- **Request JSON de ejemplo:**
  ```json
  {
    "buyerId": 1
  }
  ```
- **Response 201 Created:** `OrderResponseDTO` (estado CART)
  ```json
  {
    "id": 100,
    "buyerId": 1,
    "buyerEmail": "comprador@nexus.com",
    "orderDate": "2026-01-15T10:30:00",
    "status": "CART",
    "totalAmount": 0.00,
    "items": []
  }
  ```
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | buyerId nulo | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 403 | Rol no BUYER | `AccessDeniedException` |
  | 404 | Comprador inexistente | `ResourceNotFoundException` |

---

### GET /api/orders/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `OrderController.getOrderById`
- **Caso de uso:** `OrderUseCasePort.getByIdOrThrow(Long)`
- **Response 200 OK:** `OrderResponseDTO`
- **Tabla de errores:** 401, 404.

---

### GET /api/orders
- **Rol requerido:** AUTENTICADO
- **Controlador:** `OrderController.getAllOrders`
- **Caso de uso:** `OrderUseCasePort.findAll()`
- **Response 200 OK:** `List<OrderResponseDTO>`
- **Tabla de errores:** 401.

---

### GET /api/orders/buyer/{buyerId}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `OrderController.getOrdersByBuyer`
- **Caso de uso:** `OrderUseCasePort.findByBuyer(Long)`
- **Response 200 OK:** `List<OrderResponseDTO>`
- **Tabla de errores:** 401.

---

### POST /api/orders/{id}/items
- **Rol requerido:** BUYER
- **Controlador:** `OrderController.addItem`
- **Caso de uso:** `OrderUseCasePort.addItem(Long, Long, Integer)`
- **Request JSON de ejemplo:**
  ```json
  {
    "productId": 10,
    "quantity": 2
  }
  ```
- **Response 200 OK:** `OrderResponseDTO` (total recalculado)
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | productId/quantity inválidos | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 403 | Rol no BUYER | `AccessDeniedException` |
  | 404 | Orden o producto inexistente | `ResourceNotFoundException` |
  | 409 | Estado no permite ítems | `InvalidStatusTransitionException` |

---

### DELETE /api/orders/{id}/items/{itemId}
- **Rol requerido:** BUYER
- **Controlador:** `OrderController.removeItem`
- **Caso de uso:** `OrderUseCasePort.removeItem(Long, Long)`
- **Response 200 OK:** `OrderResponseDTO`
- **Tabla de errores:** 401, 403, 404, 409 (estado).

---

### PATCH /api/orders/{id}/checkout
- **Rol requerido:** BUYER
- **Controlador:** `OrderController.checkout`
- **Caso de uso:** `OrderUseCasePort.checkout(Long)`
- **Response 200 OK:** `OrderResponseDTO` (estado PENDING_PAYMENT)
- **Tabla de errores:** 401, 403, 404, 409 (carrito vacío o estado inválido).

---

### PATCH /api/orders/{id}/confirm-payment
- **Rol requerido:** BUYER
- **Controlador:** `OrderController.confirmPayment`
- **Caso de uso:** `OrderUseCasePort.confirmPayment(Long)`
- **Response 200 OK:** `OrderResponseDTO` (estado PAID)
- **Tabla de errores:** 401, 403, 404, 409 (estado no es PENDING_PAYMENT).

---

### PATCH /api/orders/{id}/dispatch
- **Rol requerido:** AUTENTICADO (intención: WAREHOUSE_OPERATOR)
- **Controlador:** `OrderController.dispatch`
- **Caso de uso:** `OrderUseCasePort.dispatch(Long)`
- **Response 200 OK:** `OrderResponseDTO` (estado DISPATCHED)
- **Tabla de errores:** 401, 404, 409 (estado no es PAID).

---

### PATCH /api/orders/{id}/deliver
- **Rol requerido:** AUTENTICADO (intención: WAREHOUSE_OPERATOR)
- **Controlador:** `OrderController.deliver`
- **Caso de uso:** `OrderUseCasePort.deliver(Long)`
- **Response 200 OK:** `OrderResponseDTO` (estado DELIVERED)
- **Tabla de errores:** 401, 404, 409 (estado no es DISPATCHED).

---

### PATCH /api/orders/{id}/finish
- **Rol requerido:** AUTENTICADO (intención: WAREHOUSE_OPERATOR)
- **Controlador:** `OrderController.finish`
- **Caso de uso:** `OrderUseCasePort.finish(Long)`
- **Response 200 OK:** `OrderResponseDTO` (estado FINISHED)
- **Tabla de errores:** 401, 404, 409 (estado no es DELIVERED).

---

### PATCH /api/orders/{id}/cancel
- **Rol requerido:** BUYER
- **Controlador:** `OrderController.cancel`
- **Caso de uso:** `OrderUseCasePort.cancel(Long)`
- **Response 200 OK:** `OrderResponseDTO` (estado CANCELLED)
- **Tabla de errores:** 401, 403, 404, 409 (estado es FINISHED o DELIVERED).

---

## Módulo: Facturación (invoices)

### POST /api/invoices
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InvoiceController.generateInvoice`
- **Caso de uso:** `InvoiceUseCasePort.generateInvoice(Long, String)`
- **Request JSON de ejemplo:**
  ```json
  {
    "orderId": 100,
    "pdfUrl": "https://cdn.nexus.com/invoices/100.pdf"
  }
  ```
- **Response 201 Created:** `InvoiceResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | orderId nulo | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 404 | Orden inexistente | `ResourceNotFoundException` |
  | 409 | Factura ya existe para la orden | `InvoiceAlreadyExistsException` |

---

### GET /api/invoices/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InvoiceController.getInvoiceById`
- **Caso de uso:** `InvoiceUseCasePort.getByIdOrThrow(Long)`
- **Response 200 OK:** `InvoiceResponseDTO`
- **Tabla de errores:** 401, 404.

---

### GET /api/invoices/order/{orderId}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InvoiceController.getInvoiceByOrder`
- **Caso de uso:** `InvoiceUseCasePort.findByOrder(Long)`
- **Response 200 OK:** `InvoiceResponseDTO`
- **Tabla de errores:** 401, 404.

---

### GET /api/invoices
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InvoiceController.getAllInvoices`
- **Caso de uso:** `InvoiceUseCasePort.findAll()`
- **Response 200 OK:** `List<InvoiceResponseDTO>`
- **Tabla de errores:** 401.

---

### PATCH /api/invoices/{id}/void
- **Rol requerido:** AUTENTICADO
- **Controlador:** `InvoiceController.voidInvoice`
- **Caso de uso:** `InvoiceUseCasePort.voidInvoice(Long)`
- **Response 200 OK:** `InvoiceResponseDTO` (anulada)
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 401 | Sin autenticación | `AuthenticationException` |
  | 404 | Factura inexistente | `ResourceNotFoundException` |
  | 422 | Ya pagada o anulada | `InvoiceNotPayableException` |

---

## Módulo: Reembolsos (refunds)

### POST /api/refunds
- **Rol requerido:** AUTENTICADO
- **Controlador:** `RefundController.createRefund`
- **Caso de uso:** `RefundUseCasePort.createRefund(Long, Long, BigDecimal)`
- **Request JSON de ejemplo:**
  ```json
  {
    "invoiceId": 50,
    "returnRequestId": 5,
    "amount": 125000.00
  }
  ```
- **Response 201 Created:** `RefundResponseDTO` (estado PENDING)
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | invoiceId o amount nulos | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 404 | Factura inexistente | `ResourceNotFoundException` |
  | 422 | Monto > facturado | `RefundAmountExceededException` |

---

### GET /api/refunds/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `RefundController.getRefundById`
- **Caso de uso:** `RefundUseCasePort.getByIdOrThrow(Long)`
- **Response 200 OK:** `RefundResponseDTO`
- **Tabla de errores:** 401, 404.

---

### GET /api/refunds/invoice/{invoiceId}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `RefundController.getRefundsByInvoice`
- **Caso de uso:** `RefundUseCasePort.findByInvoice(Long)`
- **Response 200 OK:** `List<RefundResponseDTO>`
- **Tabla de errores:** 401.

---

### GET /api/refunds
- **Rol requerido:** AUTENTICADO
- **Controlador:** `RefundController.getAllRefunds`
- **Caso de uso:** `RefundUseCasePort.findAll()`
- **Response 200 OK:** `List<RefundResponseDTO>`
- **Tabla de errores:** 401.

---

### PATCH /api/refunds/{id}/approve
- **Rol requerido:** AUTENTICADO (intención: SUPERVISOR)
- **Controlador:** `RefundController.approveRefund`
- **Caso de uso:** `RefundUseCasePort.approveRefund(Long)`
- **Response 200 OK:** `RefundResponseDTO` (estado APPROVED)
- **Tabla de errores:** 401, 404, 409 (ya procesado).

---

### PATCH /api/refunds/{id}/reject
- **Rol requerido:** AUTENTICADO (intención: SUPERVISOR)
- **Controlador:** `RefundController.rejectRefund`
- **Caso de uso:** `RefundUseCasePort.rejectRefund(Long)`
- **Response 200 OK:** `RefundResponseDTO` (estado REJECTED)
- **Tabla de errores:** 401, 404, 409.

---

## Módulo: Envíos (shipments)

### POST /api/shipments
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ShipmentController.createShipment`
- **Caso de uso:** `ShipmentUseCasePort.createShipment(Long, Long, String)`
- **Request JSON de ejemplo:**
  ```json
  {
    "orderId": 100,
    "warehouseId": 3,
    "trackingNumber": "TRK-2026-0001"
  }
  ```
- **Response 201 Created:** `ShipmentResponseDTO` (estado PENDING)
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | orderId o warehouseId nulos | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 404 | Orden inexistente | `ResourceNotFoundException` |
  | 409 | Ya existe envío para la orden | `ShipmentAlreadyExistsException` |

---

### GET /api/shipments/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ShipmentController.getShipmentById`
- **Caso de uso:** `ShipmentUseCasePort.getByIdOrThrow(Long)`
- **Response 200 OK:** `ShipmentResponseDTO`
- **Tabla de errores:** 401, 404.

---

### GET /api/shipments/tracking/{trackingNumber}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ShipmentController.getShipmentByTracking`
- **Caso de uso:** `ShipmentUseCasePort.findByTracking(String)`
- **Response 200 OK:** `ShipmentResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 401 | Sin autenticación | `AuthenticationException` |
  | 404 | Tracking inexistente | `TrackingNotFoundException` |

---

### GET /api/shipments/order/{orderId}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ShipmentController.getShipmentByOrder`
- **Caso de uso:** `ShipmentUseCasePort.findByOrder(Long)`
- **Response 200 OK:** `ShipmentResponseDTO`
- **Tabla de errores:** 401, 404.

---

### GET /api/shipments
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ShipmentController.getAllShipments`
- **Caso de uso:** `ShipmentUseCasePort.findAll()`
- **Response 200 OK:** `List<ShipmentResponseDTO>`
- **Tabla de errores:** 401.

---

### PATCH /api/shipments/{id}/status
- **Rol requerido:** AUTENTICADO (intención: WAREHOUSE_OPERATOR)
- **Controlador:** `ShipmentController.updateStatus`
- **Caso de uso:** `ShipmentUseCasePort.updateStatus(Long, ShipmentStatus)`
- **Request JSON de ejemplo:**
  ```json
  {
    "status": "IN_TRANSIT"
  }
  ```
- **Response 200 OK:** `ShipmentResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | status nulo | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 404 | Envío inexistente | `ResourceNotFoundException` |

---

## Módulo: Devoluciones (returns)

### POST /api/returns
- **Rol requerido:** BUYER
- **Controlador:** `ReturnController.createReturn`
- **Caso de uso:** `ReturnUseCasePort.createReturn(Long, String)`
- **Request JSON de ejemplo:**
  ```json
  {
    "orderId": 100,
    "reason": "Producto defectuoso"
  }
  ```
- **Response 201 Created:** `ReturnResponseDTO` (estado REQUESTED)
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 400 | orderId nulo o reason vacío | `MethodArgumentNotValidException` |
  | 401 | Sin autenticación | `AuthenticationException` |
  | 403 | Rol no BUYER | `AccessDeniedException` |
  | 404 | Orden inexistente | `ResourceNotFoundException` |
  | 422 | Fuera de ventana de devolución | `ReturnWindowExpiredException` |

---

### GET /api/returns/{id}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ReturnController.getReturnById`
- **Caso de uso:** `ReturnUseCasePort.getByIdOrThrow(Long)`
- **Response 200 OK:** `ReturnResponseDTO`
- **Tabla de errores:** 401, 404.

---

### GET /api/returns/order/{orderId}
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ReturnController.getReturnsByOrder`
- **Caso de uso:** `ReturnUseCasePort.findByOrder(Long)`
- **Response 200 OK:** `List<ReturnResponseDTO>`
- **Tabla de errores:** 401.

---

### GET /api/returns
- **Rol requerido:** AUTENTICADO
- **Controlador:** `ReturnController.getAllReturns`
- **Caso de uso:** `ReturnUseCasePort.findAll()`
- **Response 200 OK:** `List<ReturnResponseDTO>`
- **Tabla de errores:** 401.

---

### PATCH /api/returns/{id}/approve
- **Rol requerido:** AUTENTICADO (intención: SUPERVISOR)
- **Controlador:** `ReturnController.approveReturn`
- **Caso de uso:** `ReturnUseCasePort.approveReturn(Long)`
- **Response 200 OK:** `ReturnResponseDTO` (estado APPROVED)
- **Tabla de errores:** 401, 404, 409 (ya procesada).

---

### PATCH /api/returns/{id}/reject
- **Rol requerido:** AUTENTICADO (intención: SUPERVISOR)
- **Controlador:** `ReturnController.rejectReturn`
- **Caso de uso:** `ReturnUseCasePort.rejectReturn(Long)`
- **Response 200 OK:** `ReturnResponseDTO` (estado REJECTED)
- **Tabla de errores:** 401, 404, 409.

---

### PATCH /api/returns/{id}/complete
- **Rol requerido:** AUTENTICADO (intención: WAREHOUSE_OPERATOR)
- **Controlador:** `ReturnController.completeReturn`
- **Caso de uso:** `ReturnUseCasePort.completeReturn(Long)`
- **Response 200 OK:** `ReturnResponseDTO` (estado COMPLETED)
- **Tabla de errores:** 401, 404, 409.

---

## Módulo: Auditoría (auditing)

### GET /api/audit?userId=&entityName=&start=&end=
- **Rol requerido:** ADMIN o SUPERVISOR
- **Controlador:** `AuditController.getAllLogs`
- **Caso de uso:** `AuditUseCasePort.findAll()` / `findByUser` / `findByEntity` / `findByDateRange`
- **Request:** query params opcionales `userId` (Long), `entityName` (String), `start`/`end` (LocalDateTime ISO)
- **Response 200 OK:** `List<AuditLogResponseDTO>`
  ```json
  [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "userId": 1,
      "performedBy": "comprador@nexus.com",
      "entityName": "Order",
      "entityId": "100",
      "action": "CREATE",
      "details": "Orden creada con estado CART",
      "timestamp": "2026-01-15T10:30:00"
    }
  ]
  ```
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 401 | Sin autenticación | `AuthenticationException` |
  | 403 | Rol no ADMIN/SUPERVISOR | `AccessDeniedException` |

---

### GET /api/audit/{id}
- **Rol requerido:** ADMIN o SUPERVISOR
- **Controlador:** `AuditController.getLogById`
- **Caso de uso:** `AuditUseCasePort.getByIdOrThrow(String)`
- **Request:** path variable `id` (String/UUID)
- **Response 200 OK:** `AuditLogResponseDTO`
- **Tabla de errores:**
  | Código | Causa | Excepción |
  |--------|-------|-----------|
  | 401 | Sin autenticación | `AuthenticationException` |
  | 403 | Rol no ADMIN/SUPERVISOR | `AccessDeniedException` |
  | 404 | Log inexistente | `ResourceNotFoundException` |

---

## Resumen de cobertura

| Módulo | Endpoints | Controlador |
|--------|-----------|-------------|
| Auth | 2 | AuthController |
| Users | 9 | UserController |
| Buyers | 6 | BuyerController |
| Sellers | 7 | SellerController |
| Productos (catálogo) | 8 | ProductController |
| Categorías | 6 | CategoryController |
| Bodegas | 5 | WarehouseController |
| Catálogo general | 3 | CatalogController |
| Inventario | 9 | InventoryController |
| Órdenes | 12 | OrderController |
| Facturación | 5 | InvoiceController |
| Reembolsos | 6 | RefundController |
| Envíos | 6 | ShipmentController |
| Devoluciones | 7 | ReturnController |
| Auditoría | 2 | AuditController |
| **Total** | **93** | **15 controladores** |
