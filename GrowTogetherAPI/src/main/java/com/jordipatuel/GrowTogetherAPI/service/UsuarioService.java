package com.jordipatuel.GrowTogetherAPI.service;
import com.jordipatuel.GrowTogetherAPI.model.Usuario;
import com.jordipatuel.GrowTogetherAPI.model.enums.Roles;
import com.jordipatuel.GrowTogetherAPI.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Collections;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.jordipatuel.GrowTogetherAPI.config.AuthUserDetails;

@Service
public class UsuarioService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); 
    }
    public Usuario registrarUsuario(Usuario usuario) {
        String contrasenaEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(contrasenaEncriptada);
        usuario.setFechaRegistro(new Date());
        if(usuario.getRol() == null) {
            usuario.setRol(Roles.STANDARD);
        }
        return usuarioRepository.save(usuario);
    }
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado con ID: " + id));
    }
    public List<Usuario> buscarPorNombreOEmail(String query) {
        return usuarioRepository.findByNombreContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }
    public Usuario obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado con email: " + email));
    }

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
    public Usuario cambiarContrasena(Long idUsuario, String currentPassword, String newPassword) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado"));
        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
            throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("La contraseña actual es incorrecta");
        }
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setTokenVersion(usuario.getTokenVersion() + 1);
        return usuarioRepository.save(usuario);
    }
    public Usuario editarPerfil(Long idUsuario, String nombre, String email, String foto) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new com.jordipatuel.GrowTogetherAPI.exception.ResourceNotFoundException("Usuario no encontrado"));
        if(nombre != null && !nombre.trim().isEmpty()) usuario.setNombre(nombre);
        if(email != null && !email.trim().isEmpty()) {
            Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(email);
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(idUsuario)) {
                throw new com.jordipatuel.GrowTogetherAPI.exception.BadRequestException("El email ya está en uso");
            }
            usuario.setEmail(email);
        }
        if(foto != null) usuario.setFoto(foto);
        return usuarioRepository.save(usuario);
    }
    public void agregarAmigo(Long idUsuario, Long idAmigo) {
        Usuario usuario = obtenerPorId(idUsuario);
        Usuario amigo = obtenerPorId(idAmigo);
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
    public void eliminarAmigo(Long idUsuario, Long idAmigo) {
        Usuario usuario = obtenerPorId(idUsuario);
        Usuario amigo = obtenerPorId(idAmigo);
        usuario.getAmigos().remove(amigo);
        amigo.getAmigos().remove(usuario);
        usuarioRepository.save(usuario);
        usuarioRepository.save(amigo);
    }
    public void resetearContrasena(Long userId, String newPassword) {
        Usuario usuario = obtenerPorId(userId);
        usuario.setPassword(passwordEncoder.encode(newPassword));
        usuario.setTokenVersion(usuario.getTokenVersion() + 1);
        usuarioRepository.save(usuario);
    }
    public List<Usuario> obtenerAmigos(Long idUsuario) {
        Usuario usuario = obtenerPorId(idUsuario);
        return usuario.getAmigos();
    }
    public void sumarPuntos(Long usuarioId, int puntos) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElseThrow();
        usuario.setPuntosTotales(usuario.getPuntosTotales() + puntos); 
        usuarioRepository.save(usuario);
    }
    public void eliminarUsuario(Long id) {
        Usuario usuario = obtenerPorId(id);
        usuario.setActivo(false);
        usuario.setTokenVersion(usuario.getTokenVersion() + 1);
        usuarioRepository.save(usuario);
    }
}
