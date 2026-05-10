# Architecture Decision Records — GrowTogetherAPI

Decisiones de arquitectura del backend Spring Boot de GrowTogether.

**Proyecto**: GrowTogether — DAM 2026
**Autor**: Jordi Patuel Pons

> Este archivo cubre solo decisiones del backend. Las del paquete de
> datos compartido están en `GrowTogetherDATA/docs/DECISIONS.md`, las
> de la app móvil en `GrowTogetherAPP/docs/DECISIONS.md` y las del
> panel admin en `GrowTogetherADMIN/docs/DECISIONS.md`.

---

## Índice

| # | Decisión | Estado |
|---|----------|--------|
| [ADR-001](#adr-001-autenticacin-jwt-stateless) | Autenticación JWT stateless | Aceptado |
| [ADR-002](#adr-002-revocacin-de-tokens-token-version) | Revocación de tokens con tokenVersion | Aceptado |
| [ADR-003](#adr-003-cifrado-de-contraseas-bcrypt) | Cifrado de contraseñas con BCrypt | Aceptado |
| [ADR-004](#adr-004-borrado-lgico-soft-delete) | Borrado lógico (soft delete) | Aceptado |
| [ADR-005](#adr-005-dtos-separados-de-las-entidades) | DTOs separados de las entidades | Aceptado |
| [ADR-006](#adr-006-manejo-centralizado-de-errores-globalexceptionhandler) | Manejo centralizado de errores | Aceptado |
| [ADR-007](#adr-007-rate-limiting-propio-en-el-filtro) | Rate limiting propio en el filtro | Aceptado |
| [ADR-008](#adr-008-historial-de-hbitos-relleno-lazy-de-no_completado) | Relleno lazy de NO_COMPLETADO | Aceptado |
| [ADR-009](#adr-009-autorizacin-por-propietario-preauthorize-con-spel) | Autorización por propietario (`@PreAuthorize` + SpEL) | Aceptado |
| [ADR-010](#adr-010-roles-enumerated-string) | Roles `@Enumerated(STRING)` | Aceptado |
| [ADR-011](#adr-011-cors-configuracin-manual-por-origen) | CORS manual por origen | Aceptado |
| [ADR-012](#adr-012-consejos-un-consejo-por-fecha) | Un consejo por fecha | Aceptado |
| [ADR-013](#adr-013-auditora-sin-relacin-jpa) | Auditoría sin relación JPA | Aceptado |
| [ADR-014](#adr-014-swagger-ui-pblico) | Swagger UI público | Aceptado (académico) |
| [ADR-015](#adr-015-eliminacin-de-notificacionfrecuencia) | Eliminación de `Notificacion.frecuencia` | Aceptado |

---

## ADR-001: Autenticación JWT stateless

**Fecha**: 2026-03-01

### Contexto y decisión

El sistema de autenticación usa tokens JWT firmados con HMAC-SHA256
(HS256). El servidor no guarda estado de sesión: cada petición incluye
el token en el header `Authorization: Bearer <token>`.

### Alternativas descartadas

- **Sesiones HTTP (`HttpSession`)**: requieren estado en servidor.
  Obligan a sticky sessions o estado compartido si se escala.
- **OAuth2 / SSO externo**: válido en producción empresarial pero añade
  dependencia de un proveedor y complejidad innecesaria para el TFG.
- **API Keys simples**: no permiten incluir información del usuario
  (rol, id) ni gestionan expiración de forma estándar.

### Por qué JWT

- Stateless: el token se valida sin consulta a BD en cada petición
  (excepto la verificación de `tokenVersion`, ver ADR-002).
- Portable: el cliente Flutter lo gestiona en `flutter_secure_storage`.
- Estándar adoptado, librería madura (`jjwt 0.11.5`).
- Claims personalizados (`tv`, `sub`) extienden funcionalidad sin
  romper compatibilidad.

**Implementación**: `JwtService.java`, `JwtAuthenticationFilter.java`,
`SecurityConfig.java`.

---

## ADR-002: Revocación de tokens (token version)

**Fecha**: 2026-03-10

### Contexto y decisión

Cada usuario tiene un campo `tokenVersion` (int) en BD. Al generar el
JWT se incluye el valor como claim `tv`. El filtro de cada petición
compara el `tv` del token con el `tokenVersion` actual del usuario; si
no coinciden, el token se rechaza. Cambiar contraseña o desactivar la
cuenta incrementa `tokenVersion`, invalidando todos los tokens
anteriores.

### Alternativas descartadas

- **Blacklist en BD/Redis**: añade consulta por petición y necesita
  limpieza periódica. Rompe el stateless de JWT.
- **No revocar (dejar expirar)**: si un usuario cambia contraseña
  porque cree que está comprometida, el atacante mantiene acceso 24 h
  hasta la expiración.
- **Access + refresh tokens**: más complejo de implementar y de
  gestionar en el cliente. Válido en producción real, sobreingeniería
  aquí.

### Por qué tokenVersion

- Solo un entero en `usuarios`, sin tabla extra.
- Revocación inmediata.
- Mantiene el modelo stateless: la versión viaja en el propio token.

**Implementación**: `Usuario.java` (campo `tokenVersion`),
`JwtService.java` (claim `tv`), `JwtAuthenticationFilter.java`
(validación), `UsuarioService.java` (incremento).

---

## ADR-003: Cifrado de contraseñas BCrypt

**Fecha**: 2026-03-01

### Contexto y decisión

Las contraseñas se cifran con BCrypt antes de persistirse. Nunca se
guardan en texto plano.

### Alternativas descartadas

- **SHA-256 / MD5**: rápidas — vulnerables a fuerza bruta y rainbow
  tables.
- **Argon2**: más moderno, pero Spring Security incluye BCrypt out of
  the box. Argon2 exigía dependencia adicional sin ventaja práctica.
- **Texto plano**: descartado de plano.

### Por qué BCrypt

- Incluido en Spring Security.
- Salt automático (anti rainbow tables).
- Factor de coste configurable.
- Estándar de facto.

### Nota de implementación

`PasswordEncoder` vive en `PasswordEncoderConfig.java` separado de
`SecurityConfig.java` para evitar dependencias circulares: si el
encoder estuviera dentro de `SecurityConfig`, el ciclo
`SecurityConfig → UsuarioService → PasswordEncoder → SecurityConfig`
impide arrancar Spring. Patrón recomendado por Spring para este caso.

**Implementación**: `PasswordEncoderConfig.java`, `UsuarioService.java`.

---

## ADR-004: Borrado lógico (soft delete)

**Fecha**: 2026-03-05

### Contexto y decisión

Las entidades `Usuario`, `Habito` y `Desafio` no se borran físicamente
de la BD. Tienen un campo `activo` (boolean, default `true`). Al
"borrar" se pone `activo = false` y los servicios filtran por
`activo = true`.

### Alternativas descartadas

- **Hard delete (`DELETE` SQL)**: pierde historial y rompe integridad
  referencial con registros relacionados (RegistroHabito,
  ParticipacionDesafio). Irreversible.
- **Tabla de archivo separada**: más complejo y sin beneficio real
  aquí.

### Por qué soft delete

- Preserva el historial: `RegistroHabito` de un hábito borrado siguen
  existiendo para estadísticas.
- Permite reactivar registros (`PUT /admin/usuarios/{id}/desbloquear`).
- Audit log claro: se ve qué usuarios estuvieron activos.

**Implementación**: `Usuario.java`, `Habito.java`, `Desafio.java`
(campo `activo`); servicios filtran.

---

## ADR-005: DTOs separados de las entidades

**Fecha**: 2026-03-01

### Contexto y decisión

Existen DTOs separados de las entidades JPA. Hay DTOs de entrada
(`*CreateDTO`) y de salida (`*DTO`).

### Alternativas descartadas

- **Exponer entidades directamente**: filtra password y `tokenVersion`,
  causa serialización infinita por relaciones JPA y acopla la API al
  modelo de BD.
- **Un solo DTO por entidad**: los campos de entrada y salida no
  coinciden (el password va en `Create` pero nunca en `Response`).

### Por qué DTOs separados

- Control total sobre qué se expone.
- Password nunca viaja en una respuesta.
- Validaciones de entrada (`@NotBlank`, `@Email`, `@Size`) que no tienen
  sentido en la entidad.
- Desacopla BD y API.

**Implementación**: paquete `dto/` (~20 clases).

---

## ADR-006: Manejo centralizado de errores (`GlobalExceptionHandler`)

**Fecha**: 2026-03-01

### Contexto y decisión

Una clase `@RestControllerAdvice` intercepta todas las excepciones y
las transforma en `ErrorResponseDTO` JSON.

### Alternativas descartadas

- **try/catch en cada controller**: duplicación masiva.
- **Errores por defecto de Spring**: incluyen stack traces y mensajes
  internos de JPA — riesgo de filtrar información sensible.

### Por qué un handler global

- Único punto de control de la información que sale al cliente.
- Errores no controlados (`RuntimeException`) se loguean en servidor
  con stack trace y devuelven "Error interno del servidor" al cliente.
- Respuestas de error consistentes.
- Servicios lanzan excepciones semánticas
  (`ResourceNotFoundException`, `BadRequestException`); el handler
  traduce a HTTP.

**Implementación**: `GlobalExceptionHandler.java`,
`BadRequestException.java`, `ResourceNotFoundException.java`.

---

## ADR-007: Rate limiting propio en el filtro

**Fecha**: 2026-03-15

### Contexto y decisión

`RateLimitFilter` (Servlet Filter) limita a 10 peticiones/minuto por
IP en login y registro. Usa un `ConcurrentHashMap` en memoria.

### Alternativas descartadas

- **API Gateway externo (AWS, Kong, Nginx)**: robusto pero requiere
  infraestructura adicional fuera del scope del TFG.
- **Spring Cloud Gateway / Bucket4j**: dependencias especializadas que
  no aportan ventaja para dos endpoints.
- **Sin rate limiting**: los endpoints de auth son objetivo principal
  de fuerza bruta.

### Por qué implementación propia

- Sin dependencias adicionales.
- Suficiente para el scope.
- `ConcurrentHashMap` thread-safe.
- Ventana deslizante simple (60 s).

### Limitación conocida

El conteo se pierde al reiniciar y no es compartido entre instancias:
con dos servidores cada uno lleva su contador, un atacante podría
hacer 10×N peticiones. Producción real → Redis o API Gateway.

**Implementación**: `RateLimitFilter.java`.

---

## ADR-008: Historial de hábitos: relleno lazy de NO_COMPLETADO

**Fecha**: 2026-03-15

### Contexto y decisión

Los días que un usuario no interactúa con un hábito no generan
registro. Cuando se consulta el historial, el servicio detecta los
huecos y los rellena con `NO_COMPLETADO` en ese momento. Además, una
tarea programada (`@Scheduled` cada día a las 00:01) hace ese relleno
de forma automática para que las estadísticas del día anterior estén
listas.

### Alternativas descartadas

- **Generar todos los registros al crear el hábito**: miles de filas
  vacías para fechas futuras, sin sentido.
- **Trigger en BD**: mueve lógica de negocio fuera de la app,
  difícil de testear.
- **Inferirlos en el cliente**: lógica de servidor en el cliente.

### Por qué relleno lazy

- BD solo guarda registros con información real.
- Historial siempre consistente al consultarse.
- Tarea nocturna garantiza estadísticas correctas al día siguiente.

### Por qué `@Scheduled` y no un trigger

- La lógica de qué días debe tener un hábito PERSONALIZADO depende del
  modelo Java (`enum DiaSemana`, `Set<DiaSemana> diasSemana`).
  Replicarla en SQL sería complejo y frágil.
- `@Scheduled` vive con el resto del dominio, fácil de depurar.
- Spring activa el scheduler con una sola anotación
  (`@EnableScheduling`).

**Implementación**: `RegistroHabitoService.java`,
`HabitoScheduledService.java`.

---

## ADR-009: Autorización por propietario (`@PreAuthorize` con SpEL)

**Fecha**: 2026-03-10

### Contexto y decisión

Endpoints que modifican recursos de un usuario (hábitos,
notificaciones, desafíos) usan `@PreAuthorize` con expresiones SpEL
para verificar que el autenticado es propietario.

### Alternativas descartadas

- **Verificación manual en cada servicio**: duplicación.
- **Sin verificación**: cualquier usuario puede tocar recursos de
  otro conociendo el id.

### Por qué `@PreAuthorize`

- La verificación queda declarada en el controller, visible.
- Spring Security la evalúa antes de entrar al método.
- Se integra con el `SecurityContext` ya cargado.

**Implementación**: `HabitoController`, `DesafioController`,
`NotificacionController`.

---

## ADR-010: Roles `@Enumerated(STRING)`

**Fecha**: 2026-03-01

### Contexto y decisión

`Usuario.rol` usa `@Enumerated(EnumType.STRING)`, guardando el valor
como texto (`"STANDARD"`, `"ADMIN"`).

### Alternativas descartadas

- **`EnumType.ORDINAL`**: guarda el índice numérico (0, 1, 2…). Si se
  reordena o se añade un valor, los datos históricos se corrompen
  silenciosamente.

### Por qué STRING

- Datos legibles en BD sin conocer el código.
- Añadir nuevos roles no corrompe nada.
- Coste de espacio mínimo.

**Implementación**: `Usuario.java`, `Roles.java`.

---

## ADR-011: CORS configuración manual por origen

**Fecha**: 2026-03-15

### Contexto y decisión

CORS permite los orígenes:

- `localhost:*` — desarrollo en máquina local.
- `10.0.2.2:*` — emulador Android (IP del host).
- `192.168.*.*:*` — móvil físico en la misma red local.

Configurado en `SecurityConfig` para que Spring lo aplique antes que el
filtro JWT.

### Producción

Restringir a los dominios reales (`growtogether.jordipatuel.com` y
similares).

**Implementación**: `SecurityConfig.corsConfigurationSource()`.

---

## ADR-012: Consejos: un consejo por fecha

**Fecha**: 2026-04-01

### Contexto y decisión

Cada `Consejo` tiene una `fechaPublicacion` (día en que se mostrará).
**No puede haber dos consejos programados para el mismo día.**

### Estado de implementación

Restricción aplicada en el servicio (`ConsejoService` valida antes de
guardar). **Pendiente:** añadir `@UniqueConstraint` en la entidad para
duplicar la garantía a nivel de BD.

---

## ADR-013: Auditoría sin relación JPA

**Fecha**: 2026-04-05

### Contexto y decisión

`AuditLog` guarda `usuarioId` (Long) y `usuarioEmail` (String) como
campos simples, sin relación `@ManyToOne` con `Usuario`.

### Alternativas descartadas

- **Relación JPA con `Usuario`**: si el usuario se desactiva o cambia,
  el log podría reflejar el estado actual del usuario en lugar del
  estado del momento del evento.

### Por qué sin relación

- El audit log es un registro inmutable e independiente del ciclo de
  vida del usuario.
- Si el email del usuario cambia, el log mantiene el email del momento.
- Las consultas de audit no necesitan joins.

**Implementación**: `AuditLog.java`, `AuditService.java`.

---

## ADR-014: Swagger UI público

**Fecha**: 2026-03-01
**Estado**: Aceptado (justificado por contexto académico)

### Contexto y decisión

`/swagger-ui/**` y `/v3/api-docs/**` son públicos en `SecurityConfig`,
sin token.

### Alternativas descartadas

- **Swagger con auth básica**: dos mecanismos de auth conviviendo,
  complejidad sin beneficio aquí.
- **Eliminar SpringDoc en producción**: válido en prod real, pero el
  proyecto académico requiere que el evaluador pueda explorar la API.

### Por qué público

- Proyecto académico: el evaluador debe poder explorar y probar la API
  sin implementar un cliente.
- Swagger muestra todos los endpoints, parámetros y respuestas
  visualmente.
- Agiliza pruebas durante desarrollo.

### Producción

Si la API se desplegara públicamente, Swagger debería protegerse con
auth básica o desactivarse: `/v3/api-docs/**` expone la estructura
completa, lo que facilita el reconocimiento a un atacante.

**Implementación**: `SecurityConfig.java`.

---

## ADR-015: Eliminación de `Notificacion.frecuencia`

**Fecha**: 2026-05-10

### Contexto

El campo `frecuencia` de la entidad `Notificacion` (y de su DTO)
duplicaba la información del hábito asociado. La app móvil no lo usaba
para decidir cuándo disparar la noti: lo hacía consultando
`habito.frecuencia` y `habito.diasSemana`.

### Decisión

**Eliminar el campo `frecuencia` de la entidad, los DTOs y la columna
de la tabla `notificaciones`.** La frecuencia con la que la noti se
dispara la decide el cliente derivándola del hábito asociado.

### Migración

`ddl-auto=validate` en producción exige aplicar la migración antes de
desplegar el código:

```sql
ALTER TABLE notificaciones DROP COLUMN frecuencia;
```

Documentada en `docs/migrations/2026-05-10-drop-frecuencia.sql`.

### Coordinación con DATA

El paquete `growtogether_data` ya eliminó el campo en `v0.4.0`. El
repositorio cliente sigue enviando `frecuencia: 'DIARIO'` hardcoded
**solo hasta** que esta migración se aplique. Tras la migración, hay
que quitar las dos líneas hardcoded en
`GrowTogetherDATA/lib/src/repositories/notificacion_repository.dart` y
bumpear DATA otra vez.

---

## Estado actual del proyecto (mayo 2026)

Los módulos de **hábitos**, **autenticación**, **usuarios** (perfil,
búsqueda) y **notificaciones** (CRUD + integración con
`flutter_local_notifications` en cliente) están funcionando.

Pendientes de pulir:

- **Desafíos** — creación, listado y ranking implementados, sin
  validación end-to-end con el cliente.
- **Participación en desafíos** — lógica de unirse y ranking pendiente
  de pruebas.
- **Sistema de puntos** — la integración con desafíos
  (`puntosGanadosEnDesafio`) está pendiente de definición final.
- **`Consejo.fechaPublicacion`** — validada en servicio, pendiente
  añadir `@UniqueConstraint` en la entidad.
- **`@UniqueConstraint` en migración manual** — al aplicarse en BD
  debe coordinarse con `ddl-auto=validate`.
- **Tabla `habito_dias` a escala** — los días de la semana de hábitos
  PERSONALIZADO se guardan en una tabla secundaria (una fila por día
  por hábito). Alternativas a evaluar: bitmask entero (7 bits) o
  string separado por comas en la propia tabla `habitos`.

*Última actualización: mayo 2026 — Jordi Patuel Pons*
