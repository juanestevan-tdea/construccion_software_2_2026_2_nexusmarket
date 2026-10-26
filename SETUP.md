# Guía paso a paso: poner en marcha NexusMarket

Probado en Windows 11 + PowerShell 5.1. Todos los comandos se ejecutan desde la
raíz del repositorio, salvo indicación contraria.

## 0. Prerrequisitos

1. **JDK 17 o superior** (verificado con Eclipse Temurin 17 y 21):
   `java -version`.
2. **Docker Desktop** en ejecución: `docker --version` y `docker compose version`.
3. Puertos libres en el host: **3306** (MySQL), **27017** (MongoDB) y **8080**
   (app). Cómo comprobarlo:
   ```powershell
   Get-NetTCPConnection -LocalPort 27017,3306,8080 -State Listen |
     Select-Object LocalAddress, LocalPort, OwningProcess
   ```
   - Si el 8080 está ocupado, usar `SERVER_PORT=8081` al arrancar (ver paso 3).
   - Si `127.0.0.1:27017` lo ocupa un `mongod` ajeno a Docker, apuntar la app al
     contenedor con `MONGODB_URI=mongodb://[::1]:27017/nexusmarket_audit`.

## 1. Configurar secretos y levantar las bases de datos

Crea el archivo local `.env` a partir del ejemplo y define valores propios.
Compose exige `MYSQL_ROOT_PASSWORD` y `JWT_SECRET`; no hay contraseñas por
defecto en el archivo versionado.

```powershell
Copy-Item .env.example .env
# Edita .env: MYSQL_ROOT_PASSWORD, DB_PASSWORD y JWT_SECRET.
# No reutilices los valores de ejemplo.
```

Opción A — solo MySQL y MongoDB:

```powershell
docker compose up -d mysql mongodb
docker compose ps
docker exec nexusmarket-mongo mongosh --quiet --eval "db.runCommand({ping:1})"
docker exec nexusmarket-mysql mysql -uroot -p"<tu-password>" -e "SELECT VERSION();"
```

Opción B — stack completo (bases + aplicación):

```powershell
docker compose up -d --build
```

El `docker-compose.yml` declara `healthcheck` para MySQL y MongoDB; la app
espera a que ambos estén saludables. La contraseña root se toma de
`MYSQL_ROOT_PASSWORD` en `.env`; no se usa `root/root` por defecto.

## 2. Compilar y correr las pruebas

```powershell
.\mvnw.cmd clean test
```

Resultado esperado:

```
Tests run: 228, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Las pruebas usan Mockito y `@WebMvcTest`; **no necesitan MySQL ni MongoDB**.
No existe una prueba `contextLoads`, por lo que el resultado no depende de que
las bases estén levantadas.

## 3. Empaquetar y arrancar la aplicación

`JWT_SECRET` y `DB_PASSWORD` son obligatorios. Define ambos en la misma sesión
antes de arrancar; el valor de `DB_PASSWORD` debe coincidir con el de MySQL.

```powershell
# Solo si no vienen ya del entorno:
$env:DB_PASSWORD = "<tu-password-local>"
$bytes = New-Object byte[] 64
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)

.\mvnw.cmd -q -DskipTests package
$env:SERVER_PORT="8080"   # u "8081" si el 8080 está ocupado
java -jar target\nexus-market-1.0.0-SNAPSHOT.jar
```

Si `JWT_SECRET` falta o tiene menos de 32 bytes, la aplicación falla al
arrancar con un mensaje explícito; no se genera ni se usa una clave fija.

Al arrancar, Hibernate crea/actualiza las tablas en `nexusmarket` y Mongo queda
listo para crear las colecciones de auditoría con el primer evento. Verificar
salud:

```powershell
curl.exe -s http://127.0.0.1:8080/actuator/health
# {"status":"UP"}
```

> Variables de la misma sesión antes de `java -jar`: `DB_PASSWORD` y
> `JWT_SECRET` son obligatorias. También puedes ajustar `DB_USERNAME`, `DB_URL`,
> `MONGODB_URI`, `JWT_EXPIRATION` y `SERVER_PORT`. Ver tabla en `README.md`.

### Secretos, perfiles locales y artefactos

- `.env` está ignorado por Git y `.env.example` solo contiene valores ficticios.
- `application-local*.properties`, `application-dev.properties`, `config/` y
  material de claves están ignorados. Maven excluye los perfiles locales del
  JAR y `.dockerignore` evita que entren en el contexto de Docker.
- No publiques un `target/` ni una imagen construidos antes de estas
  correcciones. Regénéralos con `.\mvnw.cmd clean package` y
  `docker compose build --no-cache app`; el JAR resultante no debe contener
  `application-local.properties` ni `.env`.
- Si una credencial estuvo expuesta en Git, no basta con borrar el archivo:
  rótala en el proveedor y sigue el plan de [SECURITY.md](SECURITY.md). La
  reescritura del historial requiere autorización y aún no se ejecutó.

## 4. Prueba de humo end-to-end (registro → login → uso → logout)

> En PowerShell, pasar JSON inline a `curl.exe` suele corromper las comillas.
> Usar **archivos** para los bodies (`-d "@ruta\file.json"`).

1. Crear los bodies:
   ```powershell
   $tmp = "$env:TEMP\nexusmarket-smoke"
   New-Item -ItemType Directory -Path $tmp -Force
   Set-Content "$tmp\register.json" -Value '{"email":"test@nexus.com","fullName":"Test User","password":"Test1234","role":"BUYER","primaryAddress":"Calle 50 #40-20, Medellin"}' -NoNewline
   Set-Content "$tmp\login.json" -Value '{"email":"test@nexus.com","password":"Test1234"}' -NoNewline
   ```
   (Si repites la prueba, cambia el `email` por uno nuevo.)

2. Registrar usuario → **201**:
   ```powershell
   curl.exe -s -w "`nHTTP:%{http_code}`n" -X POST http://127.0.0.1:8080/api/v1/auth/register -H "Content-Type: application/json" -d "@$tmp\register.json"
   ```

3. Login → **200** con `token`, `role` y `expiresIn`:
   ```powershell
   $login = curl.exe -s -X POST http://127.0.0.1:8080/api/v1/auth/login -H "Content-Type: application/json" -d "@$tmp\login.json"
   $token = ($login | ConvertFrom-Json).token
   ```

4. Endpoint protegido con token → **200** (sin token → **401**):
   ```powershell
   curl.exe -s -w "`nHTTP:%{http_code}`n" http://127.0.0.1:8080/api/v1/admin/users/me -H "Authorization: Bearer $token"
   ```

5. Crear una orden (el comprador sale del token, no del body):
   ```powershell
   curl.exe -s -w "`nHTTP:%{http_code}`n" -X POST http://127.0.0.1:8080/api/v1/buyer/orders -H "Authorization: Bearer $token"
   ```

6. Verificar persistencia:
   ```powershell
   docker exec nexusmarket-mysql mysql -uroot -p"<tu-password>" --batch -e "SHOW TABLES FROM nexusmarket;"
   docker exec nexusmarket-mongo mongosh --quiet nexusmarket_audit --eval "db.audit_logs.countDocuments()"
   ```

## 5. Apagar todo

```powershell
# Detener la app: Ctrl+C en su terminal
docker compose stop       # conserva contenedores y volúmenes
docker compose down       # elimina contenedores; los volúmenes se conservan
docker compose down -v    # elimina también los datos
```

## 6. Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| `Port 8080 was already in use` | Otro proceso ocupa el puerto | Arrancar con `$env:SERVER_PORT="8081"` |
| `Communications link failure` al arrancar | MySQL apagado o aún iniciando | `docker compose up -d mysql` y esperar el healthcheck |
| Auditoría no aparece en `nexusmarket-mongo` | Otro `mongod` nativo ocupa `127.0.0.1:27017` | Usar `$env:MONGODB_URI="mongodb://[::1]:27017/nexusmarket_audit"` o detener el proceso ajeno |
| `401 Unauthorized` en endpoints protegidos | Token ausente o expirado | Enviar `Authorization: Bearer <token>` obtenido en `/api/v1/auth/login` |
| `403 Forbidden` en `/api/v1/buyer/**` | El token no tiene rol BUYER o el recurso es de otro usuario | Verificar `role` y que el recurso pertenezca al usuario autenticado |
| `curl.exe` devuelve 400 con JSON inline | Comillas mutiladas por PowerShell | Usar `-d "@archivo.json"` como en el paso 4 |
| `JWT signature does not match` tras reiniciar | `JWT_SECRET` cambió | Usar el mismo secreto o volver a iniciar sesión |

## 7. Variables de entorno

| Variable | Valor por defecto | Uso |
|---|---|---|
| `SERVER_PORT` | `8080` | Puerto HTTP de Spring Boot |
| `DB_URL` | `jdbc:mysql://localhost:3306/nexusmarket?...` | JDBC URL de MySQL |
| `DB_USERNAME` | `root` | Usuario de MySQL |
| `DB_PASSWORD` | **Sin valor por defecto** | Contraseña de MySQL; obligatoria |
| `MONGODB_URI` | `mongodb://localhost:27017/nexusmarket_audit` | URI de MongoDB |
| `JWT_SECRET` | **Sin valor por defecto** | Firma HMAC-SHA256; obligatoria y de al menos 32 bytes |
| `JWT_EXPIRATION` | `86400000` | Vida del token en milisegundos (24 h) |
| `MYSQL_ROOT_PASSWORD` | **Sin valor por defecto** | Solo Docker Compose; obligatoria |
| `MYSQL_DATABASE` | `nexusmarket` | Solo Docker Compose |

> `jwt.expiration` se puede sobrescribir con `JWT_EXPIRATION`; su valor por
> defecto es 24 h.

## 8. Verificación de la configuración Docker

```powershell
docker compose config --quiet   # requiere .env con las variables obligatorias
docker compose build app        # construye la imagen multi-stage
```

No publiques ni distribuyas imágenes construidas antes de eliminar los
perfiles locales; reconstruye con `--no-cache`.
