# GrowTogether — API REST

Backend del proyecto final de 2º DAM — Jordi Patuel Pons (2025/2026).

API REST para la app de seguimiento de hábitos GrowTogether: autenticación JWT, gestión de hábitos con rachas, historial de registros, desafíos entre usuarios y panel de administración.

---

## Stack

| Capa | Tecnología |
|------|-----------|
| Framework | Spring Boot 3.3.4 + Java 17 |
| Seguridad | Spring Security + JWT (jjwt 0.11.5) |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | PostgreSQL (puerto 5433) |
| Build | Gradle (Kotlin DSL) |
| Documentación | SpringDoc OpenAPI 2.6.0 (Swagger UI) |

---

## Requisitos previos

- Java 17+
- PostgreSQL corriendo en el puerto **5433** (configurable via `DB_URL`)
- Gradle (incluido en el wrapper, no hace falta instalarlo)

---

## Instalación y primer arranque

### 1. Crear la base de datos

```sql
CREATE DATABASE "GrowTogether_DB";
```

### 2. Variables de entorno

El proyecto requiere estas variables antes de arrancar. Sin ellas la aplicación no inicia.

| Variable | Descripción | Ejemplo |
|----------|-------------|---------|
| `JWT_SECRET` | Clave para firmar tokens JWT (mín. 32 caracteres hex) | `404E635266556A586E3272...` |
| `DB_USERNAME` | Usuario de PostgreSQL | `postgres` |
| `DB_PASSWORD` | Contraseña de PostgreSQL | `tu_password` |
| `DB_URL` | URL JDBC (opcional, hay valor por defecto) | `jdbc:postgresql://localhost:5433/GrowTogether_DB` |

**En IntelliJ**: `Run → Edit Configurations → Environment variables`

**En CLI** (perfil dev con fichero `application-dev.properties`):

```bash
# application-dev.properties (NO subir al repo — está en .gitignore)
spring.datasource.username=tu_usuario
spring.datasource.password=tu_password
JWT_SECRET=tu_clave_secreta_larga
```

```bash
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

### 3. Sembrar datos iniciales (solo primera vez)

El fichero `data.sql` contiene dos usuarios de prueba y hábitos de ejemplo. Para cargarlo:

1. En `application.properties`, cambiar temporalmente `spring.sql.init.mode=never` → `always`
2. Arrancar la API una vez — los datos se insertan automáticamente
3. Volver a `mode=never` para arranques posteriores

> Con `ddl-auto=update` las tablas y datos se **conservan entre reinicios**. No es necesario repetir este paso.

---

## Arrancar el proyecto

### Opción A — Docker Compose (recomendado para validar la topología completa)

Desde la raíz del repo padre `GrowTogether/`:

```bash
docker compose -f docker-compose.local.yml up -d --build
```

Levanta API + Postgres + nginx en una red interna. La API queda en `http://localhost:8081` y todos los healthchecks definidos. Es la misma topología que se despliega en EC2.

### Opción B — Gradle directo

```bash
# Con perfil dev (credenciales en application-dev.properties)
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

# Sin perfil (las variables de entorno deben estar definidas en el sistema)
./gradlew bootRun
```

La API arranca en `http://localhost:8081`. El base path de todos los endpoints es `/api/v1`.

### Arranque desde CLI sin IntelliJ

```bash
# Ver si hay algo corriendo en el puerto 8081
netstat -ano | grep ":8081"

# Arrancar en segundo plano con log a fichero
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun --no-daemon > /tmp/api_log.txt 2>&1 &
```

---

## Credenciales de prueba

| Rol | Email | Contraseña |
|-----|-------|-----------|
| Admin | admin@growtogether.com | Prueba123 |
| Usuario estándar | usuario@growtogether.com | Prueba123 |

---

## Documentación de la API

### Decisiones de arquitectura

Las decisiones técnicas (ADRs) se mantienen en
[`docs/DECISIONS.md`](docs/DECISIONS.md). Las migraciones manuales que
hay que aplicar contra la BD antes de pushear código (porque producción
usa `ddl-auto=validate`) viven en `docs/migrations/`.

### Swagger UI (interactiva)

Con la aplicación en marcha, la documentación Swagger está disponible en:

```
http://localhost:8081/swagger-ui.html
```

Para probar endpoints protegidos:
1. `POST /api/v1/auth/login` con `{ "email": "...", "password": "..." }`
2. Copiar el `token` de la respuesta
3. En Swagger: botón **Authorize** → `Bearer <token>`

### Javadoc (referencia HTML estática)

Las clases (controladores, servicios, modelos, DTOs) están comentadas con
Javadoc. Para generar la documentación:

```bash
./gradlew javadoc
```

Salida en `build/docs/javadoc/index.html`. Por defecto está bajo `build/`,
ignorada por git.

---

## Estructura del proyecto

```
src/main/java/com/jordipatuel/GrowTogetherAPI/
├── controller/     # 6 controladores REST (Auth, Usuario, Habito, Desafio, Notificacion, Admin)
├── service/        # 11 servicios con la lógica de negocio
├── repository/     # 10 repositorios Spring Data JPA
├── model/          # 10 entidades JPA + enums (DiaSemana, EstadoHabito, Frecuencia, Roles...)
├── dto/            # DTOs de entrada (CreateDTO) y salida (ResponseDTO, AdminDTO)
├── config/         # SecurityConfig, JwtFilter, RateLimitFilter, PasswordEncoderConfig
└── exception/      # GlobalExceptionHandler + excepciones tipadas
```

---

## Endpoints principales

### Públicos (sin autenticación)
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/v1/auth/registrar` | Registrar nuevo usuario |
| POST | `/api/v1/auth/login` | Login → devuelve JWT + userId |

### Hábitos (requiere JWT)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/habitos/usuario/{id}` | Listar hábitos activos del usuario |
| POST | `/api/v1/habitos` | Crear hábito |
| PUT | `/api/v1/habitos/{id}` | Editar hábito |
| DELETE | `/api/v1/habitos/{id}` | Eliminar (soft delete) |
| POST | `/api/v1/habitos/{id}/completar?fecha=` | Marcar como completado |
| POST | `/api/v1/habitos/{id}/descompletar?fecha=` | Desmarcar |
| GET | `/api/v1/habitos/{id}/historial?fechaInicio=&fechaFin=` | Historial por rango |

### Admin (rol ADMIN)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/admin/metricas` | Snapshot global: usuarios activos, totales, hábitos creados, completados hoy, desafíos activos, usuario más veterano y nuevos por mes (6 meses) |
| GET | `/api/v1/admin/usuarios` | Lista todos los usuarios para el panel (UsuarioAdminDTO con estado de bloqueo). Activos primero, alfabético |
| POST | `/api/v1/admin/usuarios` | Crea un nuevo administrador |
| PUT | `/api/v1/admin/usuarios/{id}/resetear-contrasena` | Reset sin verificar la actual |
| PUT | `/api/v1/admin/usuarios/{id}/desbloquear` | Reactiva un usuario bloqueado y limpia motivo/fecha |
| DELETE | `/api/v1/admin/usuarios/{id}` | Bloqueo (soft delete) con motivo obligatorio en el body. Se rechaza si el admin intenta bloquearse a sí mismo |
| GET | `/api/v1/admin/recursos` | Lista todos los consejos (incluye inactivos y futuros) |
| POST | `/api/v1/admin/recursos` | Crea un consejo. La fecha es opcional pero única si se asigna |
| PUT | `/api/v1/admin/recursos/{id}` | Edita un consejo |
| DELETE | `/api/v1/admin/recursos/{id}` | Elimina físicamente un consejo |
| GET | `/api/v1/admin/audit` | Últimos 100 registros de auditoría globales |
| GET | `/api/v1/admin/audit/usuario/{id}` | Audit log filtrado por admin |

### Consejos del día (cualquier rol autenticado)
| Método | Ruta | Descripción |
|--------|------|-------------|
| GET | `/api/v1/usuarios/consejos` | Lista de consejos activos publicados hasta hoy |
| GET | `/api/v1/usuarios/consejo/hoy` | Consejo asignado a la fecha de hoy. 204 si no hay |

Ver el Swagger para la lista completa de endpoints con parámetros y respuestas.

---

## Lógica de negocio clave

- **Rachas**: se recalculan en cada completar/descompletar. Los hábitos `PERSONALIZADO` (con días de la semana específicos) solo cuentan los días programados; los no programados se saltan sin romper la racha.
- **Historial NO_COMPLETADO**: un job nocturno (`HabitoScheduledService`, 00:01) marca como `NO_COMPLETADO` todos los registros `PENDIENTE` del día anterior. También se aplica de forma lazy al consultar el historial.
- **Puntos**: completar un hábito suma 10 puntos al usuario (idempotente — no suma doble si ya estaba completado). Desmarcar resta 10 puntos.
- **Revocación de tokens**: el campo `tokenVersion` en `Usuario` se incrementa al cambiar contraseña o desactivar cuenta, invalidando todos los JWT anteriores del usuario.
- **Bloqueo con motivo**: el bloqueo de un usuario (`DELETE /admin/usuarios/{id}`) exige `motivo` en el body. Se guarda en `motivo_bloqueo` y `fecha_bloqueo` y queda en el audit log. Un admin no puede bloquearse a sí mismo (validación en controller).
- **Consejos diarios**: los consejos pueden no tener fecha asignada (consejos "de reserva"). Cuando se asigna, la fecha es única (constraint UNIQUE + validación en `ConsejoService`). El endpoint `GET /usuarios/consejo/hoy` devuelve el consejo activo para el día actual o 204 si no hay.

---

## Seguridad

- JWT stateless con expiración de 24h. Secret obligatorio via variable de entorno.
- BCrypt para contraseñas con política de complejidad (mínimo 8 caracteres, mayúscula, minúscula, dígito y carácter especial).
- Rate limiting: 10 requests/minuto por IP en los endpoints de login y registro.
- Control de acceso por rol (`STANDARD` / `ADMIN`) y por propietario (`@PreAuthorize` con SpEL).
- Soft delete en usuarios, hábitos y desafíos — los datos nunca se borran físicamente.

---

## Notas para producción

- Cambiar `spring.profiles.active` a un perfil de producción con `ddl-auto=validate` o `none`.
- Proteger o eliminar Swagger UI (`/swagger-ui.html`).
- Configurar CORS con el dominio real en lugar de `localhost` y `192.168.*`.
- Revisar el rate limiting: la implementación actual con `ConcurrentHashMap` no se limpia automáticamente (posible memory leak a largo plazo).
- Migrar el almacenamiento de fotos de Base64 en BD a un servicio cloud (S3 o equivalente).
