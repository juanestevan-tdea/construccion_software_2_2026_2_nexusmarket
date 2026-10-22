# Seguridad y gestión de secretos

Este documento registra el estado de la Misión 2 y el plan de saneamiento que
**todavía no se ha ejecutado**. No contiene valores de credenciales.

## Estado confirmado

- El historial de Git incluye credenciales de MongoDB con forma de URI real en
  commits antiguos (`910929b`, `d245062` y `c24a81c`). Son ancestros de
  `origin/main`.
- El commit `96224ae` eliminó esas cadenas del árbol, pero no del historial. Por
  tanto, siguen siendo recuperables desde el repositorio público.
- No se accedió a MongoDB Atlas ni a ningún proveedor externo para comprobar si
  las credenciales siguen activas. Se deben tratar como comprometidas.
- La copia local `src/main/resources/application-local.properties` contenía una
  tercera credencial. Fue eliminada del árbol de trabajo; el archivo estaba
  ignorado por Git. No se debe conservar ni publicar ninguna copia de ese
  archivo.
- El árbol actual usa `MONGODB_URI`, `JWT_SECRET` y `DB_PASSWORD` desde el
  entorno. Maven excluye perfiles locales del JAR y `.dockerignore` los excluye
  del contexto de Docker.

## Acción obligatoria antes de publicar

1. **Rotar/revocar** en MongoDB Atlas las credenciales históricas y cualquier
   credencial local que haya estado en el árbol. Crear un usuario de base de
   datos nuevo con privilegios mínimos para la aplicación.
2. Rotar también `JWT_SECRET` en cualquier entorno que haya usado un valor
   publicado o compartido.
3. No publicar ni distribuir JARs, imágenes Docker, capturas, logs o archivos
   `target/` generados antes de estas correcciones.
4. Regenerar artefactos después de la rotación:

   ```powershell
   .\mvnw.cmd clean package
   docker compose build --no-cache app
   ```

5. Verificar que el JAR no contiene `application-local.properties`, `.env` ni
   URIs con credenciales embebidas.

## Plan de saneamiento del historial (requiere autorización)

No se debe reescribir el historial sin autorización explícita. El orden seguro
es:

1. Rotar las credenciales primero. El saneamiento no protege una credencial que
   ya fue copiada o usada.
2. Crear un espejo de respaldo del repositorio y coordinar a todos los
   colaboradores.
3. Instalar `git filter-repo` y preparar un archivo de reemplazos **fuera del
   repositorio**, con las cadenas antiguas y sus reemplazos ficticios. Ese
   archivo no debe commitearse, imprimirse ni compartirse.
4. Ejecutar el reemplazo sobre todas las ramas y etiquetas, por ejemplo:

   ```text
   git filter-repo --replace-text <archivo-de-reemplazos> --force
   ```

5. Verificar de forma enmascarada que las cadenas ya no aparecen en ningún
   commit, rama, etiqueta ni archivo del historial.
6. Revisar los hashes reescritos, las referencias y la configuración de
   protecciones del repositorio.
7. Solo entonces, y con autorización, actualizar el remoto con
   `git push --force-with-lease` (nunca con `--force` a ciegas).
8. Avisar a colaboradores para que vuelvan a clonar; los clones antiguos siguen
   conteniendo los secretos y no deben seguir usándose.

### Riesgos de la reescritura

- Cambian todos los hashes de los commits afectados y sus descendientes.
- Se rompen clones existentes, ramas de colaboradores, pull requests abiertos y
  enlaces a commits.
- Los forks y cachés de GitHub pueden conservar objetos antiguos; la rotación de
  credenciales sigue siendo obligatoria.
- Un `force-push` mal coordinado puede perder trabajo. Requiere respaldo,
  ventana de mantenimiento y autorización.

## Actualización de la Misión 3 (auditoría final)

Cambios de código verificados en el árbol de trabajo, **sin commit ni push**:

- Se aplicó CORS dentro de la cadena de Spring Security (`SecurityConfig` con
  `.cors(...)`). El preflight ya no recibe `401` antes de llegar a la
  configuración MVC; se añadió una prueba de regresión.
- `PasswordPolicy` centraliza la regla de 8 a 72 caracteres, al menos una letra
  y un número. Se aplica en el alta pública, el alta administrativa y el cambio
  de contraseña, además de las anotaciones de los DTO.
- Las lecturas de productos por ID y por `sellerId` en la ruta de vendedor
  verifican la propiedad; un vendedor que cambia el identificador recibe `403`.
- La consulta administrativa de eventos de sesión ya no interpreta filtros
  opcionales como `null` de MongoDB: cada combinación usa una consulta derivada
  distinta.
- La tolerancia a fallos de escritura de auditoría quedó en
  `SessionAuditService`; el adaptador MongoDB ya no registra el mismo error dos
  veces.
- El login audita también fallos de autenticación distintos de credenciales
  inválidas (por ejemplo, cuenta deshabilitada), sin revelar el estado al
  cliente.
- Las transiciones de devolución, reembolso y envío respetan el estado previo: devolución `REQUESTED`, reembolso `PENDING`; los envíos en `DELIVERED`/`CANCELLED` no se pueden reabrir.

Resultado local de la suite: **211 pruebas, 0 fallos, 0 errores, 0 omitidas**
(`mvnw clean package`). No se ejecutaron pruebas contra MySQL/MongoDB reales ni
contra Docker.

Siguen pendientes y **no se consideran resueltos**:

1. La rotación/revocación externa de credenciales históricas y el saneamiento
   del historial de Git descritos en las secciones anteriores.
2. La validación del stack Docker: el motor Linux de Docker Desktop no estaba
   disponible en esta sesión.
3. Pruebas de integración reales (Testcontainers o equivalente) para JPA,
   consultas derivadas de MongoDB y migraciones.
4. Endurecimiento de producción: usuario MySQL con privilegios mínimos,
   MongoDB con autenticación, CORS por entorno, `ddl-auto=validate` con Flyway
   y mantener los logs SQL/MongoDB en `INFO`/`false` salvo depuración local.

## Estado de publicación

Mientras existan credenciales activas expuestas en el historial, en un artefacto
o en un fork, el repositorio **no debe declararse seguro para publicar**. La
rotación externa y el saneamiento del historial son pendientes separados de los
cambios de código de esta misión.
