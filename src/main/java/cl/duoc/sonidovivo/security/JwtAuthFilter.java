package cl.duoc.sonidovivo.security;

import cl.duoc.sonidovivo.model.Usuario;
import cl.duoc.sonidovivo.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Se ejecuta en cada petición: si trae "Authorization: Bearer <token>" válido, deja autenticado al usuario
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String cabecera = request.getHeader("Authorization");

        if (cabecera != null && cabecera.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.leer(cabecera.substring(7));
                Usuario usuario = usuarioRepository.findById(Long.parseLong(claims.getSubject())).orElse(null);

                // El rol se lee de la base de datos: si el administrador lo cambia o bloquea la cuenta, se aplica de inmediato
                if (usuario != null && !usuario.isBloqueado()) {
                    var autenticacion = new UsernamePasswordAuthenticationToken(
                            String.valueOf(usuario.getId()),
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().toUpperCase())));
                    SecurityContextHolder.getContext().setAuthentication(autenticacion);
                }
            } catch (JwtException | IllegalArgumentException e) {
                // Token inválido o vencido: la petición sigue sin autenticar y Spring Security responderá 401
            }
        }

        chain.doFilter(request, response);
    }
}
