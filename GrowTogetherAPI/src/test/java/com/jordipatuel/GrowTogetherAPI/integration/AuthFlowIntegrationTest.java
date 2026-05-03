package com.jordipatuel.GrowTogetherAPI.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jordipatuel.GrowTogetherAPI.dto.HabitoCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioCreateDTO;
import com.jordipatuel.GrowTogetherAPI.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test de integración E2E con H2 in-memory.
 *
 * Recorre el flujo real registro → login → llamada autenticada a un recurso
 * protegido. Demuestra que la cadena Spring Security + JWT + JPA funciona.
 *
 * Usa el perfil 'test' (application-test.properties) que configura H2 + JWT_SECRET dummy.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UsuarioRepository usuarioRepository;

    @BeforeEach
    void limpiarBaseDeDatos() {
        usuarioRepository.deleteAll();
    }

    private String registrar(String nombre, String email, String password) throws Exception {
        UsuarioCreateDTO dto = new UsuarioCreateDTO(nombre, email, password, null);
        return objectMapper.writeValueAsString(dto);
    }

    /**
     * Hace el login y devuelve el JWT.
     */
    private String login(String email, String password) throws Exception {
        String body = "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isOk())
            .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("token").asText();
    }

    @Test
    void registrar_inserta_usuario_en_BD_y_devuelve_201() throws Exception {
        mockMvc.perform(post("/api/v1/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrar("Jordi", "jordi@example.com", "Prueba12")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("jordi@example.com"));

        assertThat(usuarioRepository.findByEmail("jordi@example.com")).isPresent();
    }

    @Test
    void registrar_con_email_duplicado_devuelve_409() throws Exception {
        mockMvc.perform(post("/api/v1/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrar("Jordi", "duplicado@example.com", "Prueba12")))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrar("Otro", "duplicado@example.com", "Prueba12")))
            .andExpect(status().isConflict());
    }

    @Test
    void registrar_con_dto_invalido_devuelve_400() throws Exception {
        // Email mal formado
        mockMvc.perform(post("/api/v1/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrar("Jordi", "no-es-email", "Prueba12")))
            .andExpect(status().isBadRequest());
    }

    @Test
    void login_con_credenciales_correctas_devuelve_token() throws Exception {
        mockMvc.perform(post("/api/v1/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrar("Jordi", "login@example.com", "Prueba12")))
            .andExpect(status().isCreated());

        String token = login("login@example.com", "Prueba12");

        assertThat(token).isNotBlank();
        // JWT tiene 3 partes separadas por puntos
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    void login_con_email_no_registrado_devuelve_401() throws Exception {
        String body = "{\"email\":\"nadie@example.com\",\"password\":\"Prueba12\"}";

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void login_con_password_incorrecta_devuelve_401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrar("Jordi", "fail@example.com", "Prueba12")))
            .andExpect(status().isCreated());

        String body = "{\"email\":\"fail@example.com\",\"password\":\"OtraPass99\"}";
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void recurso_protegido_sin_token_devuelve_401() throws Exception {
        // El JwtAuthenticationEntryPoint custom mapea ausencia de auth a 401,
        // para que la app móvil pueda detectar la expiración y borrar el token.
        mockMvc.perform(get("/api/v1/habitos/usuario/1"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.status").value(401))
            .andExpect(jsonPath("$.message").value("No autenticado"));
    }

    @Test
    void recurso_protegido_con_token_invalido_devuelve_401() throws Exception {
        mockMvc.perform(get("/api/v1/habitos/usuario/1")
                .header("Authorization", "Bearer token-falso-no-vale"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void flujo_completo_registro_login_y_crear_habito_devuelve_201() throws Exception {
        // 1. Registrar
        MvcResult registroResult = mockMvc.perform(post("/api/v1/auth/registrar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrar("Jordi", "flujo@example.com", "Prueba12")))
            .andExpect(status().isCreated())
            .andReturn();
        Long usuarioId = objectMapper.readTree(registroResult.getResponse().getContentAsString())
                .get("id").asLong();

        // 2. Login
        String token = login("flujo@example.com", "Prueba12");

        // 3. Crear hábito con el JWT
        HabitoCreateDTO habito = new HabitoCreateDTO("Leer", "20 min", "DIARIO", null, "POSITIVO", null);
        mockMvc.perform(post("/api/v1/habitos")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(habito)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Leer"))
            .andExpect(jsonPath("$.usuarioId").value(usuarioId));

        // 4. Listar hábitos del usuario
        mockMvc.perform(get("/api/v1/habitos/usuario/" + usuarioId)
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }
}
