# 📘 Documentación Técnica - NexusMarket

## 🏗️ Proceso de Construcción del Software

### 1. Configuración Inicial del Proyecto

**1.1 Creación del Repositorio**
- Se creó un repositorio público en GitHub.
- Se clonó localmente usando git clone.
- Se estableció la rama main como rama principal.

**1.2 Estructura de Carpetas**
Se siguió el estándar de Maven para proyectos Spring Boot:
- src/main/java: Código fuente.
- src/main/resources: Archivos de configuración.
- src/test/java: Pruebas.
- .mvn/wrapper: Maven Wrapper.

**1.3 Configuración de Dependencias (pom.xml)**
Se incluyeron las siguientes dependencias:
- spring-boot-starter-web: Para la API REST.
- spring-boot-starter-data-jpa: Para persistencia con MySQL.
- spring-boot-starter-data-mongodb: Para persistencia con MongoDB.
- lombok: Para reducir código boilerplate.
- spring-boot-starter-validation: Para validaciones.
- spring-boot-starter-test: Para pruebas.

**1.4 Configuración de Bases de Datos (application.yml)**
Se configuraron dos fuentes de datos:
- **MySQL**: Para datos relacionales (usuarios, pedidos, inventario).
- **MongoDB**: Para datos documentales (catálogo enriquecido, carritos, logs).

**1.5 Maven Wrapper**
Se generó con mvn wrapper:wrapper para garantizar reproducibilidad del build.

---

### 2. Modelo de Dominio (Construcción de Entidades)

**2.1 Entidad User**
- **Propósito**: Representa a todos los usuarios del sistema.
- **Atributos**:
  - id: Identificador único.
  - email: Correo electrónico (único).
  - ullName: Nombre completo.
  - password: Contraseña (encriptada en producción).
  - ole: Enum con los roles (BUYER, SELLER, WAREHOUSE_OPERATOR, ADMIN, SUPERVISOR).
  - status: Enum con estados (ACTIVE, BLOCKED).
  - createdAt, updatedAt: Fechas de creación y actualización.
- **Métodos de negocio**:
  - lock(): Cambia el estado a BLOCKED.
  - ctivate(): Cambia el estado a ACTIVE.
  - isActive(): Verifica si el usuario está activo.

**2.2 Repositorio UserRepository**
- Extiende JpaRepository para operaciones CRUD.
- Métodos personalizados:
  - indByEmail(String email): Busca un usuario por correo.
  - indByRole(UserRole role): Lista usuarios por rol.
  - indByStatus(UserStatus status): Lista usuarios por estado.
  - existsByEmail(String email): Verifica si un correo ya está registrado.

---

### 3. Convenciones de Código (Construcción Limpia)
- **Lenguaje**: Todo el código está en inglés.
- **Nombres de clases**: PascalCase (ej. User, UserRepository).
- **Nombres de métodos**: camelCase (ej. indByEmail, ctivate).
- **Anotaciones de Lombok**: @Data, @NoArgsConstructor, @AllArgsConstructor.
- **Anotaciones de JPA**: @Entity, @Table, @Column, @Enumerated, @Id, @GeneratedValue.

---

### 4. Comandos Git Utilizados (Control de Versiones)
| Comando | Propósito |
|---------|-----------|
| git clone | Descargar el repositorio remoto. |
| git status | Ver el estado de los archivos. |
| git add . | Agregar todos los archivos al staging. |
| git commit -m "mensaje" | Guardar los cambios en el historial. |
| git push origin main | Subir los cambios al repositorio remoto. |

---

### 5. Próximos Pasos en la Construcción
- Crear las entidades Buyer, Seller, Product, Inventory, Order, etc.
- Implementar los servicios de dominio.
- Desarrollar los endpoints REST.
- Integrar la persistencia con MySQL y MongoDB.

---
**Fecha de creación**: 25 de agosto de 2026  
**Autor**: Juan Esteban T-DEA  
**Curso**: Construcción de Software 2 - 2026-2
