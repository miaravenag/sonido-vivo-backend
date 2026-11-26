package cl.duoc.sonidovivo.dto;

import java.util.List;
import java.util.Map;

// Indicadores del dashboard administrativo, calculados desde la base de datos
public record ReporteResumen(
        String periodo,
        String desde,              // null cuando el periodo es "todo"
        String hasta,
        long totalOperaciones,     // compras no canceladas
        long montoTotal,
        long unidadesVendidas,
        long ticketPromedio,
        long operacionesCanceladas,
        Map<String, Long> operacionesPorEstado,
        List<ProductoMasVendido> masVendidos,
        List<PuntoVentas> serie) {

    public record ProductoMasVendido(Long productoId, String nombre, long unidades, long monto) {
    }

    // Un punto del gráfico: un día (yyyy-MM-dd) o un mes (yyyy-MM)
    public record PuntoVentas(String etiqueta, long operaciones, long monto) {
    }
}
