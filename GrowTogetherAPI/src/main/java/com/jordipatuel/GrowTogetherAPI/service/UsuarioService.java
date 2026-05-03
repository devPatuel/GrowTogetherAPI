package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioAdminDTO;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioCreateDTO;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioDTO;
import com.jordipatuel.GrowTogetherAPI.dto.UsuarioPublicoDTO;
import java.util.Comparator;
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
    /**
     * Inyecta el repositorio de usuarios y el codificador de contraseñas.
     *
     * @param usuarioRepository repositorio de usuarios
     * @param passwordEncoder codificador BCrypt para hashear contraseñas
     */
    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo usuario a partir del DTO recibido: cifra su contraseña,
     * asigna la fecha de registro y el rol STANDARD por defecto.
     *
     * @param dto datos del nuevo usuario
     * @return el usuario registrado
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
     *
     * @return lista completa de usuarios
     */
    public List<UsuarioDTO> obtenerTodos() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve todos los usuarios para uso del panel admin con datos completos
     * (incluye estado de bloqueo). Ordenados primero los activos alfabéticamente,
     * después los bloqueados alfabéticamente.
     *
     * @return lista de usuarios con datos sensibles para admin
     */
    public List<UsuarioAdminDTO> obtenerTodosAdmin() {
        Comparator<Usuario> orden = Comparator
                .comparing(Usuario::isActivo).reversed()
                .thenComparing(u -> u.getNombre() == null ? "" : u.getNombre().toLowerCase());
        return usuarioRepository.findAll().stream()
                .sorted(orden)
                .map(this::toAdminDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo usuario con rol ADMIN. Solo invocable desde el panel admin.
     * Valida email único y aplica la misma política de contraseña que el registro normal.
     *
     * @param dto datos del nuevo administrador
     * @return el admin creado en formato DTO admin
     */
    public UsuarioAdminDTO crearAdmin(UsuarioCreateDTO dto) {
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException(
                    "El email ya está en uso");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setFoto(dto.getFoto());
        usuario.setFechaRegistro(new Date());
        usuario.setRol(Roles.ADMIN);
        return toAdminDTO(usuarioRepository.save(usuario));
    }
    /**
     * Busca un usuario por su ID. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     *
     * @param id ID del usuario
     * @return el usuario encontrado
     */
    public UsuarioDTO obtenerPorId(Long id) {
        return toDTO(obtenerEntidadPorId(id));
    }
    /**
     * Busca usuarios cuyo nombre o email contengan el texto indicado (insensible a mayúsculas).
     *
     * @param query texto a buscar en nombre o email
     * @return lista de usuarios que coinciden
     */
    public List<UsuarioDTO> buscarPorNombreOEmail(String query) {
        return usuarioRepository.findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
    /**
     * Devuelve los datos públicos (sin email ni rol) de cualquier usuario por su ID.
     * Usado desde el buscador de amigos por ID. Lanza {@link com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException} si no existe.
     *
     * @param id ID del usuario
     * @return datos públicos del usuario
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
     *
     * @param email email del usuario
     * @return el usuario encontrado
     */
    public UsuarioDTO obtenerUsuarioPorEmail(String email) {
        return toDTO(usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado con email: " + email)));
    }

    /**
     * Implementación de {@link UserDetailsService} requerida por Spring Security.
     * Carga el usuario por email, verifica que esté activo y construye
     * un {@link AuthUserDetails} con id, rol y tokenVersion para la validación JWT.
     *
     * @param username email del usuario a cargar
     * @return UserDetails del usuario autenticado
     * @throws UsernameNotFoundException si el usuario no existe o está desactivado
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
     *
     * @param idUsuario ID del usuario
     * @param currentPassword contraseña actual en texto plano
     * @param newPassword nueva contraseña en texto plano
     * @return el usuario actualizado
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
     *
     * @param idUsuario ID del usuario
     * @param nombre nuevo nombre (null o vacío para no modificar)
     * @param email nuevo email (null o vacío para no modificar)
     * @param foto nueva foto en base64 (null para no modificar)
     * @return el usuario actualizado
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
     *
     * @param idUsuario ID del usuario
     * @param tema nuevo tema (puede ser null)
     * @param idioma nuevo idioma (puede ser null)
     * @return el usuario actualizado
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
     *
     * @param idUsuario ID del usuario que añade el amigo
     * @param idAmigo ID del amigo a añadir
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
     *
     * @param idUsuario ID del usuario que elimina el amigo
     * @param idAmigo ID del amigo a eliminar
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
     *
     * @param userId ID del usuario al que se le resetea la contraseña
     * @param newPassword nueva contraseña en texto plano
     */
    public void resetearContrasena(Long userId, String newPassword) {
        Usuario usuario = obtenerEntidadPorId(userId);
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setTokenVersion(usuario.getTokenVersion() + 1);
        usuarioRepository.save(usuario);
    }
    /**
     * Devuelve la lista de amigos del usuario indicado.
     *
     * @param idUsuario ID del usuario
     * @return lista de amigos del usuario
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
     *
     * @param usuarioId ID del usuario
     * @param puntos cantidad de puntos a sumar
     */
    public void sumarPuntos(Long usuarioId, int puntos) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        usuario.setPuntosTotales(usuario.getPuntosTotales() + puntos);
        usuarioRepository.save(usuario);
    }
    /**
     * Cuenta los usuarios actualmente activos. Usado en métricas admin.
     *
     * @return número de usuarios activos
     */
    public long contarUsuariosActivos() {
        return usuarioRepository.countByActivoTrue();
    }

    /**
     * Devuelve el usuario activo con la fecha de registro más antigua o vacío
     * si no hay usuarios. Usado para destacar al veterano en el dashboard admin.
     *
     * @return Optional con el usuario más veterano, o vacío si no hay usuarios activos
     */
    public java.util.Optional<UsuarioAdminDTO> obtenerUsuarioMasVeterano() {
        return usuarioRepository.findFirstByActivoTrueOrderByFechaRegistroAsc()
                .map(this::toAdminDTO);
    }

    /**
     * Devuelve el número de usuarios nuevos registrados en los últimos {@code meses} meses
     * agrupados por mes. La clave es "YYYY-MM" y el valor el conteo.
     * El orden de la lista resultante es ascendente (más antiguo primero).
     *
     * @param meses número de meses hacia atrás a contabilizar
     * @return lista de mapas con claves "mes" (YYYY-MM) y "cantidad" (long)
     */
    public java.util.List<java.util.Map<String, Object>> contarUsuariosNuevosPorMes(int meses) {
        java.util.List<java.util.Map<String, Object>> resultado = new java.util.ArrayList<>();
        java.time.YearMonth ahora = java.time.YearMonth.now();
        java.time.format.DateTimeFormatter etiqueta = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");
        for (int i = meses - 1; i >= 0; i--) {
            java.time.YearMonth ym = ahora.minusMonths(i);
            Date desde = Date.from(ym.atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            Date hasta = Date.from(ym.plusMonths(1).atDay(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant());
            long cantidad = usuarioRepository.countByFechaRegistroBetween(desde, hasta);
            java.util.Map<String, Object> punto = new java.util.LinkedHashMap<>();
            punto.put("mes", ym.format(etiqueta));
            punto.put("cantidad", cantidad);
            resultado.add(punto);
        }
        return resultado;
    }

    /**
     * Bloquea (soft delete) un usuario guardando el motivo y la fecha del bloqueo.
     * Incrementa tokenVersion para cerrar todas sus sesiones activas.
     * El motivo es obligatorio: deja constancia de por qué se bloqueó la cuenta.
     *
     * @param id ID del usuario a bloquear
     * @param motivo motivo del bloqueo (obligatorio, máximo 500 caracteres)
     */
    public void eliminarUsuario(Long id, String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException(
                    "El motivo de bloqueo es obligatorio");
        }
        if (motivo.length() > 500) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException(
                    "El motivo de bloqueo no puede superar los 500 caracteres");
        }
        Usuario usuario = obtenerEntidadPorId(id);
        usuario.setActivo(false);
        usuario.setMotivoBloqueo(motivo);
        usuario.setFechaBloqueo(new Date());
        usuario.setTokenVersion(usuario.getTokenVersion() + 1);
        usuarioRepository.save(usuario);
    }

    /**
     * Desbloquea un usuario previamente bloqueado: lo reactiva y limpia
     * el motivo y la fecha de bloqueo. No incrementa tokenVersion porque
     * el usuario tendrá que iniciar sesión de nuevo igualmente.
     *
     * @param id ID del usuario a desbloquear
     */
    public void desbloquearUsuario(Long id) {
        Usuario usuario = obtenerEntidadPorId(id);
        usuario.setActivo(true);
        usuario.setMotivoBloqueo(null);
        usuario.setFechaBloqueo(null);
        usuarioRepository.save(usuario);
    }

    /**
     * Helper interno: recupera la entidad {@link Usuario} para operaciones internas
     * del servicio que necesitan modificar la referencia JPA (edición de perfil,
     * cambio de contraseña, gestión de amigos, etc).
     *
     * @param id ID del usuario
     * @return entidad Usuario encontrada
     */
    private Usuario obtenerEntidadPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }

    /**
     * Convierte la entidad {@link Usuario} al DTO de respuesta.
     * No incluye password, tokenVersion ni activo (detalles internos).
     *
     * @param usuario entidad origen
     * @return DTO público sin datos sensibles
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

    /**
     * Convierte la entidad {@link Usuario} al DTO admin con datos completos
     * (estado de bloqueo). Solo se usa en endpoints /admin.
     *
     * @param usuario entidad origen
     * @return DTO admin con estado de bloqueo
     */
    private UsuarioAdminDTO toAdminDTO(Usuario usuario) {
        return new UsuarioAdminDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol(),
                usuario.getFechaRegistro(),
                usuario.getPuntosTotales(),
                usuario.getFoto(),
                usuario.isActivo(),
                usuario.getMotivoBloqueo(),
                usuario.getFechaBloqueo()
        );
    }
}
