# Guía paso a paso: poner en marcha el proyecto

Probado en Windows 11 + PowerShell 5.1. Todos los comandos se ejecutan desde la
raíz del repositorio, salvo indicación contraria.

## 0. Prerrequisitos

1. **JDK 17 o superior** (verificado con Eclipse Temurin 21):
   `java -version`.
2. **Docker Desktop** en ejecución: `docker --version` y `docker compose version`.
3. Puertos libres en el host: **3306** (MySQL), **27017** (MongoDB) y **8080**
   (app). Cómo comprobarlo:
   ```powershell
   Get-NetTCPConnection -LocalPort 27017,3306,8080 -State Listen |
     Select-Object LocalAddress, LocalPort, OwningProcess
   ```
   - Si el 8080 está ocupado (frecuente por Docker Desktop u otras apps), usar
     `SERVER_PORT=8081` al arrancar (ver paso 3).
   - Si `127.0.0.1:27017` lo ocupa un `mongod` ajeno a Docker (ver
     [solución de problemas](#6-solución-de-problemas)), apuntar la app al
     contenedor con `MONGODB_URI=mongodb://[::1]:27017/nexusmarket_audit`.

## 1. Levantar las bases de datos

```powershell
docker compose up -d
docker ps --format "{{.Names}}|{{.Status}}"
docker exec nexusmarket-mongo mongosh --quiet --eval "db.runCommand({ping:1})"
docker exec nexusmarket-mysql mysql -uroot -proot -e "SELECT VERSION();"
```

Esto crea `nexusmarket-mysql` (MySQL 8, `nexusmarket`) y `nexusmarket-mongo`
(MongoDB 7, `nexusmarket_audit`). Credenciales por defecto: usuario `root` / clave
`root` (sobrescribibles con `MYSQL_ROOT_PASSWORD`, `MYSQL_PASSWORD`).

## 2. Compilar y correr las pruebas

```powershell
.\mvnw.cmd test
```

Resultado esperado: `Tests run: 94, Failures: 0, Errors: 0` y
`BUILD SUCCESS`. La prueba `NexusMarketApplicationTests.contextLoads` necesita
MySQL arriba (paso 1); sin base de datos ese test falla por conexión, el resto
pasa.

## 3. Empaquetar y arrancar la aplicación

```powershell
.\mvnw.cmd -q -DskipTests package
$env:SERVER_PORT="8080"   # u "8081" si el 8080 está ocupado
java -jar target\nexus-market-1.0.0-SNAPSHOT.jar
```

Al arrancar, Hibernate crea/actualiza las tablas en `nexusmarket` y Mongo queda
listo para crear `audit_logs` con el primer evento. Verificar salud:

```powershell
curl.exe -s http://127.0.0.1:8080/actuator/health
# {"status":"UP"}
```

> Variables opcionales en la misma sesión antes de `java -jar`:
> `$env:DB_USERNAME`, `$env:DB_PASSWORD`, `$env:MONGODB_URI`,
> `$env:JWT_SECRET`, `$env:SERVER_PORT`. Ver tabla en `README.md`.

## 4. Prueba de humo end-to-end (registro → login → uso → logout)

> En PowerShell, pasar JSON inline a `curl.exe` suele corromper las comillas.
> Usar **archivos** para los bodies (`-d "@ruta\file.json"`).

1. Crear los bodies (una sola vez):
   ```powershell
   $tmp = "$env:TEMP\nexusmarket-smoke"
   New-Item -ItemType Directory -Path $tmp -Force
   Set-Content "$tmp\register.json" -Value '{"email":"test@nexus.com","fullName":"Test User","password":"Test123!","role":"BUYER","primaryAddress":"Calle 50 #40-20, Medellin"}' -NoNewline
   Set-Content "$tmp\login.json" -Value '{"email":"test@nexus.com","password":"Test123!"}' -NoNewline
   ```
   (Si repites la prueba, cambia el `email` por uno nuevo.)

2. Registrar usuario → **201**:
   ```powershell
   curl.exe -s -w "`nHTTP:%{http_code}`n" -X POST http://127.0.0.1:8080/api/auth/register -H "Content-Type: application/json" -d "@$tmp\register.json"
   ```

3. Login → **200** con `token`, `role` y `expiresIn`:
   ```powershell
   $login = curl.exe -s -X POST http://127.0.0.1:8080/api/auth/login -H "Content-Type: application/json" -d "@$tmp\login.json"
   $token = ($login | ConvertFrom-Json).token
   ```

4. Endpoint protegido con token → **200** (sin token → **401**):
   ```powershell
   curl.exe -s -w "`nHTTP:%{http_code}`n" http://127.0.0.1:8080/api/users/me -H "Authorization: Bearer $token"
   ```

5. Verificar persistencia:
   ```powershell
   docker exec nexusmarket-mysql mysql -uroot -proot --batch -e "SHOW TABLES FROM nexusmarket; SELECT id, email, role, status FROM nexusmarket.usuarios;"
   docker exec nexusmarket-mongo mongosh --quiet nexusmarket_audit --eval "db.audit_logs.countDocuments()"
   ```

## 5. Apagar todo

```powershell
# Detener la app: Ctrl+C en su terminal
docker compose stop        # o `docker compose down` para además borrar contenedores (los volúmenes con datos se conservan)
```

## 6. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Port 8080 was already in use` | Otro proceso ocupa el puerto | Arrancar con `$env:SERVER_PORT="8081"` |
| `Tests ... NexusMarketApplicationTests ... Communications link failure` | MySQL apagado | `docker compose up -d` y repetir |
| Auditoría no aparece en `nexusmarket-mongo` | Otro `mongod` nativo ocupa `127.0.0.1:27017` | Arrancar con `$env:MONGODB_URI="mongodb://[::1]:27017/nexusmarket_audit"` o detener el proceso ajeno (requiere admin) |
| `401 Unauthorized` en endpoints protegidos | Token ausente o expirado | Enviar `Authorization: Bearer <token>` obtenido en `/api/auth/login` |
| `curl.exe` devuelve 400 con JSON inline en PowerShell | Comillas mutiladas por la shell | Usar `-d "@archivo.json"` como en el paso 4 |

## 7. Variables de entorno

| Variable | Valor por defecto | Uso |
|---|---|---|
| `SERVER_PORT` | `8080` | Puerto HTTP de Spring Boot |
| `DB_USERNAME` | `root` | Usuario de MySQL |
| `DB_PASSWORD` | `root` | Contraseña de MySQL |
| `MONGODB_URI` | `mongodb://localhost:27017/nexusmarket_audit` | URI de MongoDB |
| `JWT_SECRET` | Clave por defecto | Firma HMAC-SHA256 para JWT |
| `JWT_EXPIRATION` | `86400000` | Expiración del token en milisegundos (24h) |