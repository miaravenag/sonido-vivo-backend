package cl.duoc.sonidovivo.controller;

import cl.duoc.sonidovivo.dto.ReporteResumen;
import cl.duoc.sonidovivo.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reportes")
@Tag(name = "Reportes", description = "Indicadores del dashboard (solo administrador)")
public class ReporteController {

    @Autowired
    private ReporteService reporteService;

    @Operation(summary = "Resumen de ventas por periodo",
            description = "periodo = semana (7 días), mes (30 días), anio (12 meses) o todo")
    @GetMapping("/resumen")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ReporteResumen> resumen(@RequestParam(defaultValue = "mes") String periodo) {
        return ResponseEntity.ok(reporteService.resumen(periodo));
    }
}
