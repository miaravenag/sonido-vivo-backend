package cl.duoc.sonidovivo.controller;

import cl.duoc.sonidovivo.dto.LoginRequest;
import cl.duoc.sonidovivo.dto.LoginResponse;
import cl.duoc.sonidovivo.dto.UsuarioRequest;
import cl.duoc.sonidovivo.dto.UsuarioResponse;
import cl.duoc.sonidovivo.service.AuthService;
import cl.duoc.sonidovivo.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "Login y registro (rutas públicas)")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioService usuarioService;

    @Operation(summary = "Iniciar sesión y obtener el token JWT")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Registrar una cuenta de cliente")
    @PostMapping("/registro")
    public ResponseEntity<UsuarioResponse> registro(@Valid @RequestBody UsuarioRequest request) {
        return new ResponseEntity<>(usuarioService.registrarCliente(request), HttpStatus.CREATED);
    }

    @Operation(summary = "Renovar el token JWT (requiere un token válido)")
    @PostMapping("/renovar")
    public ResponseEntity<LoginResponse> renovar(HttpServletRequest request, Authentication authentication) {
        // El filtro JWT ya validó este token; aquí se lee de nuevo para conservar el inicio de la sesión
        String cabecera = request.getHeader("Authorization");
        String token = cabecera != null && cabecera.startsWith("Bearer ") ? cabecera.substring(7) : "";
        return ResponseEntity.ok(authService.renovar(Long.parseLong(authentication.getName()), token));
    }
}
