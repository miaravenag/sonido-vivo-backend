package cl.duoc.sonidovivo.controller;

import cl.duoc.sonidovivo.dto.PerfilRequest;
import cl.duoc.sonidovivo.dto.UsuarioRequest;
import cl.duoc.sonidovivo.dto.UsuarioResponse;
import cl.duoc.sonidovivo.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios (requiere token)")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    // El filtro JWT deja el id del usuario autenticado como "name" de la autenticación
    private Long idActual(Authentication authentication) {
        return Long.parseLong(authentication.getName());
    }

    // 1. GET: Listar todos
    @Operation(summary = "Listar usuarios (administrador)")
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listar());
    }

    // 2. GET: Buscar por ID
    @Operation(summary = "Buscar usuario por ID (administrador)")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    // 3. POST: Crear (administrador: cualquier rol; vendedor: solo clientes)
    @Operation(summary = "Crear usuario (administrador o vendedor)")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR','VENDEDOR')")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest request, Authentication authentication) {
        UsuarioResponse nuevo = usuarioService.crear(request, idActual(authentication));
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    // 4. PUT: Editar el propio perfil (cualquier usuario con sesión)
    @Operation(summary = "Actualizar mi perfil")
    @PutMapping("/perfil")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(@Valid @RequestBody PerfilRequest request, Authentication authentication) {
        return ResponseEntity.ok(usuarioService.actualizarPerfil(idActual(authentication), request));
    }

    // 5. PUT: Actualizar
    @Operation(summary = "Actualizar usuario (administrador)")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioRequest request,
                                                      Authentication authentication) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request, idActual(authentication)));
    }

    // 6. DELETE: Borrar
    @Operation(summary = "Eliminar usuario (administrador)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> borrar(@PathVariable Long id, Authentication authentication) {
        usuarioService.eliminar(id, idActual(authentication));
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}
