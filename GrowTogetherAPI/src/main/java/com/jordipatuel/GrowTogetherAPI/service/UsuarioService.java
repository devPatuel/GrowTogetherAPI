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

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // Inicializamos el encoder aquí
    }

    public Usuario registrarUsuario(Usuario usuario) {
        // Encriptar la contraseña usando BCryptPasswordEncoder
        String contrasenaEncriptada = passwordEncoder.encode(usuario.getPassword());
        usuario.setPassword(contrasenaEncriptada);
        
        // Asignar fecha de registro actual
        usuario.setFechaRegistro(new Date());
        
        // Asignar rol por defecto si no lo tiene
        if(usuario.getRol() == null) {
            usuario.setRol(Roles.STANDARD);
        }
        
        // Guardar el usuario en base de datos
        return usuarioRepository.save(usuario);
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public boolean iniciarSesion(String email, String rawPassword) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isPresent()) {
            return passwordEncoder.matches(rawPassword, usuarioOpt.get().getPassword());
        }
        return false;
    }

    public Usuario cambiarContrasena(Long idUsuario, String currentPassword, String newPassword) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        if (!passwordEncoder.matches(currentPassword, usuario.getPassword())) {
            throw new RuntimeException("La contraseña actual es incorrecta");
        }
        
        usuario.setPassword(passwordEncoder.encode(newPassword));
        return usuarioRepository.save(usuario);
    }
    
    public Usuario editarPerfil(Long idUsuario, String nombre, String email, String foto) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
                
        if(nombre != null && !nombre.trim().isEmpty()) usuario.setNombre(nombre);
        if(email != null && !email.trim().isEmpty()) {
            Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(email);
            if (usuarioExistente.isPresent() && !usuarioExistente.get().getId().equals(idUsuario)) {
                throw new RuntimeException("El email ya está en uso");
            }
            usuario.setEmail(email);
        }
        if(foto != null) usuario.setFoto(foto);
        
        return usuarioRepository.save(usuario);
    }

    public void agregarAmigo(Long idUsuario, Long idAmigo) {
        // TODO: En el futuro enviará una petición de amistad, de momento se queda vacío
    }
}
