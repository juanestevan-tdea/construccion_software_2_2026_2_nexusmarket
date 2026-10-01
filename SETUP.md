# Guía de Puesta en Marcha: NexusMarket

Guía paso a paso para configurar, compilar, probar y ejecutar **NexusMarket** en entornos locales de desarrollo.

---

## 1. Requisitos Previos

Asegúrate de contar con las siguientes herramientas instaladas en tu sistema:

- **JDK 17 o superior** (OpenJDK o Eclipse Temurin):
  ```bash
  java -version
  ```
- **Apache Maven 3.9+** (o usar el wrapper `./mvnw` / `mvnw.cmd`):
  ```bash
  mvn -version
  ```
- **MySQL 8.0+** (local o vía Docker) en el puerto `3306`.
- **MongoDB 7.0+** (local o clúster MongoDB Atlas) en el puerto `27017`.
- **Docker & Docker Compose** (opcional, recomendado para levantar la infraestructura de datos).

---

## 2. Pasos para Ejecutar NexusMarket

### Paso a) Clonar el repositorio
```bash
git clone https://github.com/juanestevan-tdea/construccion_software_2_2026_2_nexusmarket.git
cd construccion_software_2_2026_2_nexusmarket
```

### Paso b) Crear la Base de Datos MySQL
Accede a tu cliente de MySQL (CLI, Workbench, DBeaver) y ejecuta:
```sql
CREATE DATABASE nexusmarket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Paso c) Configurar Variables de Entorno o `application.properties`
El archivo `src/main/resources/application.properties` está preparado con valores por defecto y variables de entorno externas. Puedes configurarlas en tu terminal o sistema antes de iniciar:

| Variable | Descripción | Valor por Defecto |
|---|---|---|
| `DB_USERNAME` | Usuario de MySQL | `root` |
| `DB_PASSWORD` | Contraseña de MySQL | `root` |
| `MONGODB_URI` | Cadena de conexión de MongoDB | URI de MongoDB Atlas (o `mongodb://localhost:27017/nexusmarket_audit`) |
| `JWT_SECRET` | Clave secreta HMAC-SHA256 (min 256 bits) | Clave base preconfigurada en propiedades |
| `SERVER_PORT` | Puerto HTTP del servidor Spring Boot | `8080` |

#### En Windows PowerShell:
```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="root"
$env:MONGODB_URI="mongodb://localhost:27017/nexusmarket_audit"
```

#### En Linux / macOS / Bash:
```bash
export DB_USERNAME="root"
export DB_PASSWORD="root"
export MONGODB_URI="mongodb://localhost:27017/nexusmarket_audit"
```

### Paso d) Compilar el Proyecto
Para descargar dependencias y compilar el código saltando los tests:
```bash
mvn clean install -DskipTests
```
*(O en Windows usando el wrapper: `.\mvnw.cmd clean install -DskipTests`)*

### Paso e) Ejecutar la Aplicación
Inicia el servidor Spring Boot:
```bash
mvn spring-boot:run
```
O ejecutando el artefacto `.jar` empaquetado:
```bash
java -jar target/nexus-market-1.0.0-SNAPSHOT.jar
```

La aplicación arrancará en `http://localhost:8080`.

---

## 3. Endpoints Públicos de Ejemplo

Los siguientes endpoints están expuestos públicamente bajo `/api/auth/**`:

### 3.1. Registro de Usuario (`POST /api/auth/register`)

**Petición HTTP:**
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "juan.perez@example.com",
  "fullName": "Juan Perez",
  "password": "Password123!",
  "role": "BUYER",
  "primaryAddress": "Calle 50 # 40-20, Medellin",
  "taxId": "1017123456",
  "companyName": "Perez Enterprises"
}
```

> **Roles válidos:** `BUYER`, `SELLER`, `INVENTORY_MANAGER`, `LOGISTICS_OPERATOR`, `AUDITOR`, `SUPERVISOR`, `ADMIN`.

**Respuesta Exitosa (`201 Created`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "juan.perez@example.com",
  "fullName": "Juan Perez",
  "role": "BUYER"
}
```

### 3.2. Inicio de Sesión (`POST /api/auth/login`)

**Petición HTTP:**
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "juan.perez@example.com",
  "password": "Password123!"
}
```

**Respuesta Exitosa (`200 OK`):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": 1,
  "email": "juan.perez@example.com",
  "fullName": "Juan Perez",
  "role": "BUYER"
}
```

Para consumir los endpoints protegidos, añade la cabecera:
```http
Authorization: Bearer <TOKEN_RECIBIDO>
```

---

## 4. Ejecución de Pruebas Unitarias y de Integración

Para ejecutar la suite completa de pruebas automatizadas:
```bash
mvn clean test
```

**Resultado esperado:**
```text
Tests run: 94, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 5. Solución de Problemas (Troubleshooting)

| Problema / Error | Causa Probable | Solución Recomendada |
|---|---|---|
| `Port 8080 was already in use` | Hay otro proceso (Tomcat, Docker Desktop, otra app) usando el puerto 8080. | Inicia la app especificando un puerto alternativo: `mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"` o establece `$env:SERVER_PORT="8081"`. |
| `Communications link failure / Access denied for user` (MySQL) | MySQL no está corriendo, las credenciales son incorrectas o la base de datos `nexusmarket` no ha sido creada. | 1. Verifica que MySQL esté activo en el puerto 3306.<br>2. Confirma `DB_USERNAME` y `DB_PASSWORD`.<br>3. Ejecuta `CREATE DATABASE nexusmarket;` en tu motor MySQL. |
| `MongoSocketOpenException / MongoTimeoutException` (MongoDB) | MongoDB no se encuentra activo localmente o no hay acceso a internet para MongoDB Atlas. | 1. Si usas MongoDB local, confirma que el servicio esté corriendo en el puerto 27017.<br>2. Si usas MongoDB Atlas, verifica tu conexión a internet o exporta la URI correcta en `MONGODB_URI`. |
| `401 Unauthorized` / `403 Forbidden` en endpoints protegidos | El token JWT no fue incluido o expiró. | Asegúrate de enviar la cabecera `Authorization: Bearer <token>` obtenida en `/api/auth/login`. |
