package cl.duoc.sonidovivo.service;

import cl.duoc.sonidovivo.dto.LoginRequest;
import cl.duoc.sonidovivo.dto.LoginResponse;
import cl.duoc.sonidovivo.dto.UsuarioResponse;
import cl.duoc.sonidovivo.exception.ReglaNegocioException;
import cl.duoc.sonidovivo.model.Usuario;
import cl.duoc.sonidovivo.repository.UsuarioRepository;
import cl.duoc.sonidovivo.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private static final int MAX_INTENTOS = 3;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    // Sin @Transactional a propósito: el contador de intentos fallidos debe guardarse aunque se lance la excepción
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.UNAUTHORIZED,
                        "El correo electrónico no se encuentra registrado."));

        if (usuario.isBloqueado()) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN,
                    "Esta cuenta ha sido BLOQUEADA por exceder los intentos de acceso. Contacta a un administrador.");
        }

        if (passwordEncoder.matches(request.password(), usuario.getPassword())) {
            usuario.setIntentosFallidos(0);
            usuarioRepository.save(usuario);
            return new LoginResponse(jwtService.generarToken(usuario), UsuarioResponse.desde(usuario));
        }

        // Regla de negocio: 3 contraseñas incorrectas seguidas bloquean la cuenta
        int intentos = usuario.getIntentosFallidos() + 1;
        usuario.setIntentosFallidos(intentos);

        if (intentos >= MAX_INTENTOS) {
            usuario.setBloqueado(true);
            usuarioRepository.save(usuario);
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN,
                    "Has superado el límite de intentos. Tu cuenta ha sido BLOQUEADA.");
        }

        usuarioRepository.save(usuario);
        throw new ReglaNegocioException(HttpStatus.UNAUTHORIZED,
                "Contraseña incorrecta. Intentos restantes: " + (MAX_INTENTOS - intentos) + ".");
    }

    // Renovación: con un token todavía válido se entrega uno nuevo (con la expiración reiniciada)
    // junto con los datos actuales del usuario. También sirve para restaurar la sesión al recargar la página.
    public LoginResponse renovar(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.UNAUTHORIZED, "La sesión ya no es válida."));

        if (usuario.isBloqueado()) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "Esta cuenta ha sido BLOQUEADA.");
        }

        return new LoginResponse(jwtService.generarToken(usuario), UsuarioResponse.desde(usuario));
    }
}
