package cl.duoc.sonidovivo.security;

import cl.duoc.sonidovivo.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expiracionMs;

    // Duración máxima de una sesión, aunque el token se renueve varias veces
    @Value("${jwt.max-session-ms}")
    private long maxSesionMs;

    private SecretKey clave() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // El "subject" del token es el id del usuario (así sigue siendo válido si cambia su correo)
    public String generarToken(Usuario usuario) {
        return generarToken(usuario, System.currentTimeMillis());
    }

    // "inicio" guarda cuándo comenzó la sesión (login); al renovar se conserva ese valor
    public String generarToken(Usuario usuario, long inicioSesionMs) {
        Date ahora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("email", usuario.getEmail())
                .claim("rol", usuario.getRol())
                .claim("inicio", inicioSesionMs)
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave())
                .compact();
    }

    public long inicioSesion(Claims claims) {
        Object inicio = claims.get("inicio");
        if (inicio instanceof Number numero) {
            return numero.longValue();
        }
        return claims.getIssuedAt().getTime(); // tokens antiguos sin el claim
    }

    public long getMaxSesionMs() {
        return maxSesionMs;
    }

    // Lanza JwtException si el token es inválido, fue alterado o expiró
    public Claims leer(String token) {
        return Jwts.parser()
                .verifyWith(clave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
