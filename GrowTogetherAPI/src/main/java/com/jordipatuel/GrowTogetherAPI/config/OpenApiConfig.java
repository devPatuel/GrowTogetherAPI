package com.jordipatuel.GrowTogetherAPI.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI / Swagger UI.
 *
 * Declara la metainformación de la API (título, versión, contacto, servidores)
 * y registra el esquema de seguridad "bearerAuth" para que Swagger UI muestre
 * el botón "Authorize" y permita adjuntar el JWT en las peticiones de prueba.
 *
 * IMPORTANTE: Esta clase es puramente documental. No afecta a la seguridad real
 * de la API, que está definida en {@link SecurityConfig} mediante Spring Security
 * y el filtro {@link JwtAuthenticationFilter}. Quitar o añadir anotaciones aquí
 * no abre ni cierra endpoints; solo cambia lo que ve Swagger UI.
 *
 * El requisito de seguridad se aplica de forma global a todos los endpoints
 * documentados. Los endpoints públicos de {@code /api/v1/auth/**} seguirán
 * funcionando sin token porque {@link SecurityConfig} los marca como
 * {@code permitAll()}; el candado que aparece en Swagger sobre ellos es solo visual.
 *
 * Swagger UI queda accesible en:
 * <ul>
 *     <li>http://localhost:8081/swagger-ui.html</li>
 *     <li>http://localhost:8081/v3/api-docs (especificación OpenAPI en JSON)</li>
 * </ul>
 *
 * Flujo típico de uso desde Swagger UI:
 * <ol>
 *     <li>Llamar a {@code POST /api/v1/auth/registrar} o {@code /login}.</li>
 *     <li>Copiar el token devuelto en el campo "token" de la respuesta.</li>
 *     <li>Pulsar el botón "Authorize" y pegar el token (sin el prefijo "Bearer ").</li>
 *     <li>Lanzar el resto de endpoints protegidos; Swagger añade el header
 *         {@code Authorization: Bearer &lt;token&gt;} automáticamente.</li>
 * </ol>
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GrowTogether API",
                version = "v1",
                description = "API REST de GrowTogether. Gestiona usuarios, hábitos, desafíos y administración. " +
                        "La mayoría de endpoints requieren autenticación JWT obtenida en /api/v1/auth/login.",
                contact = @Contact(name = "Jordi Patuel Pons", email = "trivaraassetventures@gmail.com"),
                license = @License(name = "Uso académico - Proyecto Final 2DAM")
        ),
        servers = {
                @Server(url = "http://localhost:8081", description = "Entorno local de desarrollo")
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Token JWT obtenido al llamar a POST /api/v1/auth/login. " +
                "Pegar solo el valor del token, sin el prefijo 'Bearer '.",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {
}
