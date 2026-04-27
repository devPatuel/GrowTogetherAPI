package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioDTO;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioPublicoDTO;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import com.jordipatuel.GrowTogetherAPI.model.enums.Roles;
import com.jordipatuel.GrowTogetherAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;

/**
 * Servicio principal de gestión de usuarios.
 *
 * Implementa {@link UserDetailsService} para integrarse con Spring Security:
 * cuando llega una petición con JWT, el filtro llama a {@code loadUserByUsername}
 * para cargar el usuario y verificar que existe, está activo y su tokenVersion es válida.
 *
 * También centraliza el registro, edición de perfil, contraseñas,
 * preferencias, sistema de amigos, puntos y baja de usuarios.
 *
 * Expone una API basada en DTOs: los Controllers nunca ven la entidad {@link Usuario}.
 * {@code loadUserByUsername} se deja intacto porque es el contrato de Spring Security
 * y necesita devolver un {@link UserDetails}.
 */
@Service
public class UsuarioService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo usuario a partir del DTO recibido: cifra su contraseña,
     * asigna la fecha de registro y el rol STANDARD por defecto.
     */
    public UsuarioDTO registrarUsuario(UsuarioCreateDTO dto) {
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setFoto(dto.getFoto());
        usuario.setFechaRegistro(new Date());
        usuario.setRol(Roles.STANDARD);
        return toDTO(usuarioRepository.save(usuario));
    }
    /**
     * Devuelve todos los usuarios de la base de datos sin filtrar.
     */
    public List<UsuarioDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Busca un usuario por su ID. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     */
    public UsuarioDTO obtenerPorId(Long id) {
        return toDTO(obtenerEntidadPorId(id));
    }
    /**
     * Busca usuarios cuyo nombre o email contengan el texto indicado (insensible a mayúsculas).
     */
    public List<UsuarioDTO> buscarPorNombreOEmail(String query) {
        return usuarioRepository.findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Devuelve los datos públicos (sin email ni rol) de cualquier usuario por su ID.
     * Usado desde el buscador de amigos por ID. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     */
    public UsuarioPublicoDTO obtenerPublicoPorId(Long id) {
        Usuario usuario = obtenerEntidadPorId(id);
        return new UsuarioPublicoDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getFoto(),
                usuario.getPuntosTotales()
        );
    }
    /**
     * Busca un usuario por su email exacto. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     */
    public UsuarioDTO obtenerUsuarioPorEmail(String email) {
        return toDTO(usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado con email: " + email)));
    }

    /**
     * Implementación de {@link UserDetailsService} requerida por Spring Security.
     * Carga el usuario por email, verifica que esté activo y construye
     * un {@link AuthUserDetails} con id, rol y tokenVersion para la validación JWT.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + username));
        if (!usuario.isActivo()) {
            throw new UsernameNotFoundException("La cuenta está desactivada");
        }
        return new AuthUserDetails(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name())),
                usuario.getTokenVersion()
        );
    }
    /**
     * Cambia la contraseña del usuario verificando primero la actual.
     * Incrementa tokenVersion para invalidar todos los JWT activos del usuario.
     */
    public UsuarioDTO cambiarContrasena(Long idUsuario, String currentPassword, String newPassword) {
        Usuario usuario = obtenerEntidadPorId(idUsuario);
        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("La contraseña actual es incorrecta");
        }
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setTokenVersion(usuario.getTokenVersion() + 1);
        return toDTO(usuarioRepository.save(usuario));
    }
    /**
     * Actualiza nombre, email y/o foto del perfil.
     * Valida que el nuevo email no esté ya en uso por otro usuario.
     */
    public UsuarioDTO editarPerfil(Long idUsuario, String nombre, String email, String foto) {
        Usuario usuario = obtenerEntidadPorId(idUsuario);
        if(nombre != null && !nombre.trim().isEmpty()) usuario.setNombre(nombre);
        if(email != null && !email.trim().isEmpty()) {
            Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(email);
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(idUsuario)) {
                throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("El email ya está en uso");
            }
            usuario.setEmail(email);
        }
        if(foto != null) usuario.setFoto(foto);
        return toDTO(usuarioRepository.save(usuario));
    }
    private static final List<String> TEMAS_VALIDOS = Arrays.asList("CLARO", "OSCURO", "MORADO", "NATURALEZA");
    private static final List<String> IDIOMAS_VALIDOS = Arrays.asList("es", "en", "ca");

    /**
     * Actualiza las preferencias de tema (CLARO/OSCURO/MORADO/NATURALEZA)
     * e idioma (es/en/ca). Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.BadRequestException} si el valor no es válido.
     */
    public UsuarioDTO actualizarPreferencias(Long idUsuario, String tema, String idioma) {
        Usuario usuario = obtenerEntidadPorId(idUsuario);

        if (tema != null) {
            String temaUpper = tema.toUpperCase();
            if (!TEMAS_VALIDOS.contains(temaUpper)) {
                throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException(
                        "Tema no válido. Valores permitidos: " + TEMAS_VALIDOS);
            }
            usuario.setTema(temaUpper);
        }

        if (idioma != null) {
            String idiomaLower = idioma.toLowerCase();
            if (!IDIOMAS_VALIDOS.contains(idiomaLower)) {
                throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException(
                        "Idioma no válido. Valores permitidos: " + IDIOMAS_VALIDOS);
            }
            usuario.setIdioma(idiomaLower);
        }

        return toDTO(usuarioRepository.save(usuario));
    }

    /**
     * Añade una relación de amistad bidireccional entre dos usuarios.
     * Solo guarda si la relación no existía ya en alguna de las dos direcciones.
     */
    public void agregarAmigo(Long idUsuario, Long idAmigo) {
        Usuario usuario = obtenerEntidadPorId(idUsuario);
        Usuario amigo = obtenerEntidadPorId(idAmigo);
        boolean changed = false;
        if (!usuario.getAmigos().contains(amigo)) {
            usuario.getAmigos().add(amigo);
            changed = true;
        }
        if (!amigo.getAmigos().contains(usuario)) {
            amigo.getAmigos().add(usuario);
            changed = true;
        }
        if (changed) {
            usuarioRepository.save(usuario);
            usuarioRepository.save(amigo);
        }
    }
    /**
     * Elimina la relación de amistad en ambas direcciones.
     */
    public void eliminarAmigo(Long idUsuario, Long idAmigo) {
        Usuario usuario = obtenerEntidadPorId(idUsuario);
        Usuario amigo = obtenerEntidadPorId(idAmigo);
        usuario.getAmigos().remove(amigo);
        amigo.getAmigos().remove(usuario);
        usuarioRepository.save(usuario);
        usuarioRepository.save(amigo);
    }
    /**
     * Resetea la contraseña de un usuario sin verificar la actual (uso exclusivo del admin).
     * Incrementa tokenVersion para invalidar sesiones activas.
     */
    public void resetearContrasena(Long userId, String newPassword) {
        Usuario usuario = obtenerEntidadPorId(userId);
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setTokenVersion(usuario.getTokenVersion() + 1);
        usuarioRepository.save(usuario);
    }
    /**
     * Devuelve la lista de amigos del usuario indicado.
     */
    public List<UsuarioDTO> obtenerAmigos(Long idUsuario) {
        Usuario usuario = obtenerEntidadPorId(idUsuario);
        return usuario.getAmigos().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Suma puntos al total del usuario. Llamado desde {@link HabitoService}
     * al completar un hábito.
     */
    public void sumarPuntos(Long usuarioId, int puntos) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        usuario.setPuntosTotales(usuario.getPuntosTotales() + puntos);
        usuarioRepository.save(usuario);
    }
    /**
     * Desactiva el usuario (soft delete) e incrementa tokenVersion
     * para cerrar todas sus sesiones activas.
     */
    public void eliminarUsuario(Long id) {
        Usuario usuario = obtenerEntidadPorId(id);
        usuario.setActivo(false);
        usuario.setTokenVersion(usuario.getTokenVersion() + 1);
        usuarioRepository.save(usuario);
    }

    /**
     * Helper interno: recupera la entidad {@link Usuario} para operaciones internas
     * del servicio que necesitan modificar la referencia JPA (edición de perfil,
     * cambio de contraseña, gestión de amigos, etc).
     */
    private Usuario obtenerEntidadPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Convierte la entidad {@link Usuario} al DTO de respuesta.
     * No incluye password, tokenVersion ni activo (detalles internos).
     */
    private UsuarioDTO toDTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getFechaRegistro(),
                usuario.getPuntosTotales(),
                usuario.getFoto(),
                usuario.getTema(),
                usuario.getIdioma()
        );
    }
}
