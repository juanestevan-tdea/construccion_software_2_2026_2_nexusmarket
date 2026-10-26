# Despliegue — Diagrama de Contenedores (Docker Compose)

Diagrama de despliegue basado en el `docker-compose.yml` real del proyecto. Levanta tres servicios sobre una red bridge (`nexusmarket-net`) con volúmenes persistentes para MySQL y MongoDB.

```mermaid
C4Deployment
    title Despliegue - NexusMarket (docker-compose)

    Deployment_Node(host, "Host de desarrollo / servidor", "Linux (Docker Engine)") {
        Container_Boundary(docker_host, "Docker Host") {

            Container_Boundary(net, "Red bridge: nexusmarket-net") {

                Container(mysql, "MySQL 8.0", "mysql:8.0", "Puerto 3306", "BD relacional transacional") {
                    ContainerDb(mysql_db, "nexusmarket", "MySQL", "3306", "Volumen: mysql_data")
                }

                Container(mongo, "MongoDB 7", "mongo:7", "Puerto 27017", "BD documental de auditoría") {
                    ContainerDb(mongo_db, "nexusmarket_audit", "MongoDB", "27017", "Volumen: mongodb_data")
                }

                Container(app, "NexusMarket App", "Dockerfile multi-stage", "Puerto 8080", "Spring Boot 4.0.0 + Java 17")
            }
        }
    }

    Rel_User_App(user, app, "Llamadas REST autenticadas (JWT Bearer)", "HTTPS :8080")

    Rel(app, mysql, "JPA / Hibernate (JDBC)", "mysql:3306")
    Rel(app, mongo, "Spring Data MongoDB", "mongodb:27017")

    Rel(mysql, mysql_db, "Almacena", "Volumen persistente")
    Rel(mongo, mongo_db, "Almacena", "Volumen persistente")
```

## Servicios del `docker-compose.yml`

| Servicio | Imagen | Puerto host → contenedor | Volúmenes | Variables de entorno clave |
|----------|--------|--------------------------|-----------|----------------------------|
| `mysql` | `mysql:8.0` | 3306 → 3306 | `mysql_data` → `/var/lib/mysql` | `MYSQL_ROOT_PASSWORD` (obligatoria, desde `.env`), `MYSQL_DATABASE=nexusmarket` |
| `mongodb` | `mongo:7` | 27017 → 27017 | `mongodb_data` → `/data/db` | — |
| `app` | build (Dockerfile) | 8080 → 8080 | — | `SPRING_PROFILES_ACTIVE=prod`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `MONGODB_URI`, `JWT_SECRET` (obligatoria) |

## Red y volúmenes

- **Red:** `nexusmarket-net` (driver `bridge`). Los tres servicios están en ella; la app se conecta a `mysql:3306` y `mongodb:27017` por nombre de servicio.
- **Volúmenes:** `mysql_data` y `mongodb_data` (nombrados, persistentes entre reinicios).
- **Restart policy:** `unless-stopped` en los tres servicios.
- **Healthchecks:** MySQL (`mysqladmin ping`) y MongoDB (`db.adminCommand('ping')`); la app usa `depends_on: condition: service_healthy`.
- **Orden de arranque:** `app` depende de `mysql` y `mongodb` (`depends_on`).

## Endpoints expuestos

| Puerto | Servicio | Uso |
|--------|----------|-----|
| 8080 | app | API REST NexusMarket (`http://localhost:8080/api`) |
| 3306 | mysql | MySQL JDBC |
| 27017 | mongodb | MongoDB auditoría |

## Cómo desplegar

```bash
# 1. Crear base de datos en MySQL (si no usa Docker para la BD)
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS nexusmarket CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 2. Levantar toda la pila
docker-compose up -d

# 3. Ver estado
docker-compose ps

# 4. Logs de la app
docker-compose logs -f app
```

La aplicación queda disponible en `http://localhost:8080`. En desarrollo, la documentación interactiva está expuesta de forma pública por `springdoc-openapi`: Swagger UI en `http://localhost:8080/swagger-ui.html` y el contrato generado en `http://localhost:8080/v3/api-docs` (JSON) o `/v3/api-docs.yaml`. El perfil `prod`, activo en Compose, desactiva Swagger y los endpoints OpenAPI; `SDD/04-api/openapi.yaml` se conserva como contrato de referencia curado a mano.
