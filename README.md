# GrowTogether API

> Backend REST del ecosistema **GrowTogether**: autenticación JWT, hábitos con rachas, desafíos entre usuarios y panel de administración.

![Java](https://img.shields.io/badge/Java-17-007396?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-JWT-6DB33F?logo=springsecurity&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-BD-4169E1?logo=postgresql&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-Kotlin%20DSL-02303A?logo=gradle&logoColor=white)
![OpenAPI](https://img.shields.io/badge/OpenAPI-Swagger%20UI-85EA2D?logo=swagger&logoColor=black)

---

## Sobre el proyecto

**GrowTogether** es una aplicación de seguimiento de hábitos con componente social, inspirada en *Atomic Habits* de James Clear: construye hábitos consistentes, visualiza tu progreso y compite con amigos en desafíos.

Es el **Trabajo Final de Grado de DAM** (Desarrollo de Aplicaciones Multiplataforma, 2025/2026) de **Jordi Patuel Pons**.

### Papel de este repositorio

Este repo contiene la **API REST** que consumen la app móvil y el panel de administración: autenticación JWT, gestión de hábitos con rachas, historial de registros, desafíos entre usuarios, consejos diarios y endpoints de administración con auditoría.

## Ecosistema GrowTogether

| Repositorio | Descripción |
|---|---|
| **[GrowTogetherAPI](https://github.com/devPatuel/GrowTogetherAPI)** ← estás aquí | Backend REST (Java 17 + Spring Boot) |
| [GrowTogetherAPP](https://github.com/devPatuel/GrowTogetherAPP) | App móvil de hábitos (Flutter) |
| [GrowTogetherADMIN](https://github.com/devPatuel/GrowTogetherADMIN) | Panel de administración web (Flutter Web) |
| [GrowTogetherDATA](https://github.com/devPatuel/GrowTogetherDATA) | Paquete Dart compartido: modelos, cliente HTTP y repositorios |

---

## Stack técnico

| Capa | Tecnología |
|------|-----------|
| Framework | Spring Boot 3.3.4 + Java 17 |
| Seguridad | Spring Security + JWT (jjwt 0.11.5) |
| Persistencia | Spring Data JPA + Hibernate |
| Base de datos | PostgreSQL (puerto 5433) |
| Build | Gradle (Kotlin DSL) |
| Documentación | SpringDoc OpenAPI 2.6.0 (Swagger UI) |
| Contenedores | Dockerfile multi-stage (Temurin 17, imagen final solo JRE) |

---

## Cómo ejecutarlo en local

> El código vive en el subdirectorio `GrowTogetherAPI/` del repo; ejecuta los comandos de Gradle desde ahí.

### Requisitos previos

- Java 17+
- PostgreSQL corriendo en el puerto **5433** (configurable vía `DB_URL`)
- Gradle (incluido en el wrapper, no hace falta instalarlo)

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

**En CLI** (perfil dev con fichero `application-dev.properties`, hay un `application-dev.properties.example` de plantilla):

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

### 4. Arrancar

```bash
# Con perfil dev (credenciales en application-dev.properties)
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun

# Sin perfil (las variables de entorno deben estar definidas en el sistema)
./gradlew bootRun
```

La API arranca en `http://localhost:8081`. El base path de todos los endpoints es `/api/v1`.

### Docker (opcional)

El repo incluye un `Dockerfile` multi-stage (build con Gradle → imagen final solo con el JRE sobre Alpine), el mismo que se usa en el despliegue en EC2. Necesita un PostgreSQL accesible y las mismas variables de entorno:

```bash
docker build -t growtogether-api ./GrowTogetherAPI
docker run -p 8081:8081 \
  -e JWT_SECRET=... -e DB_USERNAME=... -e DB_PASSWORD=... \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5433/GrowTogether_DB \
  growtogether-api
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

Las decisiones técnicas (ADRs) se mantienen en [`docs/DECISIONS.md`](docs/DECISIONS.md).

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

Las clases (controladores, servicios, modelos, DTOs) están comentadas con Javadoc. Para generar la documentación:

```bash
./gradlew javadoc
```

Salida en `build/docs/javadoc/index.html`. Por defecto está bajo `build/`, ignorada por git.

---

## Estructura del proyecto

```
src/main/java/com/jordipatuel/GrowTogetherAPI/
├── controller/     # 7 controladores REST (Auth, Usuario, Habito, Desafio, Notificacion, Admin, Version)
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
- **Puntos**: los puntos se ganan **solo completando días de un desafío** (no por completar hábitos). El cálculo lo hace `Scoring.java` con bonus de racha: 10 puntos base, +10% por cada día consecutivo, con tope x20. Los puntos del usuario se reflejan en `puntosTotales` y se recalculan al marcar/desmarcar días del desafío.
- **Revocación de tokens**: el campo `tokenVersion` en `Usuario` se incrementa al cambiar contraseña o desactivar cuenta, invalidando todos los JWT anteriores del usuario.
- **Bloqueo con motivo**: el bloqueo de un usuario (`DELETE /admin/usuarios/{id}`) exige `motivo` en el body. Se guarda en `motivo_bloqueo` y `fecha_bloqueo` y queda en el audit log. Un admin no puede bloquearse a sí mismo (validación en controller).
- **Consejos diarios**: los consejos pueden no tener fecha asignada (consejos "de reserva"). Cuando se asigna, la fecha es única (constraint UNIQUE + validación en `ConsejoService`). El endpoint `GET /usuarios/consejo/hoy` devuelve el consejo activo para el día actual o 204 si no hay.

---

## Seguridad

- JWT stateless con expiración de 24h. Secret obligatorio vía variable de entorno.
- BCrypt para contraseñas con política de complejidad (mínimo 8 caracteres, mayúscula, minúscula, dígito y carácter especial).
- Rate limiting: 10 requests/minuto por IP en los endpoints de login y registro.
- Control de acceso por rol (`STANDARD` / `ADMIN`) y por propietario (`@PreAuthorize` con SpEL).
- Soft delete en usuarios, hábitos y desafíos — los datos nunca se borran físicamente.

---

## Notas para producción

- Cambiar `spring.profiles.active` a un perfil de producción con `ddl-auto=validate` o `none`.
- Configurar CORS con el dominio real en lugar de `localhost` y `192.168.*` (variable `CORS_ORIGINS`).
- Revisar el rate limiting: la implementación actual con `ConcurrentHashMap` no se limpia automáticamente (posible memory leak a largo plazo).
- Migrar el almacenamiento de fotos de Base64 en BD a un servicio cloud (S3 o equivalente).

> Sobre Swagger UI: se mantiene público a propósito incluso en producción para que el tribunal pueda explorar la API desplegada sin acceso al repo. Decisión documentada en [ADR-014](docs/DECISIONS.md#adr-014-swagger-ui-pblico).

---

## Licencia

Proyecto académico — Trabajo Final de Grado de DAM · GrowTogether · Jordi Patuel Pons.
