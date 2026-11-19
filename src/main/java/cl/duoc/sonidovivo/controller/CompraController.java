package cl.duoc.sonidovivo.controller;

import cl.duoc.sonidovivo.dto.CompraRequest;
import cl.duoc.sonidovivo.dto.CompraResponse;
import cl.duoc.sonidovivo.service.CompraService;
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
@RequestMapping("/compras")
@Tag(name = "Compras", description = "Registro e historial de compras (requiere token)")
public class CompraController {

    @Autowired
    private CompraService compraService;

    @Operation(summary = "Registrar una compra con los productos del carrito")
    @PostMapping
    public ResponseEntity<CompraResponse> crear(@Valid @RequestBody CompraRequest request, Authentication authentication) {
        CompraResponse compra = compraService.crear(request, Long.parseLong(authentication.getName()));
        return new ResponseEntity<>(compra, HttpStatus.CREATED);
    }

    @Operation(summary = "Historial de compras del usuario con sesión")
    @GetMapping("/mis-compras")
    public ResponseEntity<List<CompraResponse>> misCompras(Authentication authentication) {
        return ResponseEntity.ok(compraService.listarDeUsuario(Long.parseLong(authentication.getName())));
    }

    @Operation(summary = "Historial de todas las compras (administrador)")
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<CompraResponse>> listarTodas() {
        return ResponseEntity.ok(compraService.listarTodas());
    }
}
