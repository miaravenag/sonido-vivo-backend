package cl.duoc.sonidovivo.service;

import cl.duoc.sonidovivo.dto.ReporteResumen;
import cl.duoc.sonidovivo.dto.ReporteResumen.ProductoMasVendido;
import cl.duoc.sonidovivo.dto.ReporteResumen.PuntoVentas;
import cl.duoc.sonidovivo.exception.ReglaNegocioException;
import cl.duoc.sonidovivo.model.Compra;
import cl.duoc.sonidovivo.model.DetalleCompra;
import cl.duoc.sonidovivo.model.EstadoCompra;
import cl.duoc.sonidovivo.repository.CompraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ReporteService {

    private static final int TOP_PRODUCTOS = 5;

    @Autowired
    private CompraRepository compraRepository;

    // Acumulador interno para sumar unidades y monto por producto
    private static class Acumulado {
        String nombre;
        long unidades;
        long monto;
    }

    @Transactional(readOnly = true)
    public ReporteResumen resumen(String periodo) {
        String p = periodo == null ? "mes" : periodo.trim().toLowerCase();
        LocalDate hoy = LocalDate.now();

        // semana = últimos 7 días, mes = últimos 30 días, anio = últimos 12 meses, todo = sin límite
        LocalDate desde = switch (p) {
            case "semana" -> hoy.minusDays(6);
            case "mes" -> hoy.minusDays(29);
            case "anio" -> hoy.withDayOfMonth(1).minusMonths(11);
            case "todo" -> null;
            default -> throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                    "Periodo no válido. Usa: semana, mes, anio o todo.");
        };

        List<Compra> compras = desde == null
                ? compraRepository.findAll()
                : compraRepository.findByFechaGreaterThanEqual(desde.atStartOfDay());

        // Conteo por estado (todas las compras del periodo)
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (EstadoCompra estado : EstadoCompra.values()) {
            porEstado.put(estado.name(), 0L);
        }
        for (Compra c : compras) {
            porEstado.merge(c.getEstado().name(), 1L, Long::sum);
        }

        // Las compras canceladas no cuentan como venta
        List<Compra> vendidas = compras.stream()
                .filter(c -> c.getEstado() != EstadoCompra.CANCELADA)
                .toList();

        long montoTotal = 0;
        long unidades = 0;
        Map<Long, Acumulado> porProducto = new LinkedHashMap<>();
        boolean porMes = p.equals("anio") || p.equals("todo");
        TreeMap<String, long[]> serie = new TreeMap<>();
        prepararSerie(serie, p, desde, hoy);

        for (Compra c : vendidas) {
            montoTotal += c.getTotal();

            for (DetalleCompra d : c.getDetalles()) {
                unidades += d.getCantidad();
                Acumulado a = porProducto.computeIfAbsent(d.getProductoId(), id -> new Acumulado());
                a.nombre = d.getNombreProducto();
                a.unidades += d.getCantidad();
                a.monto += (long) d.getPrecioUnitario() * d.getCantidad();
            }

            String etiqueta = porMes
                    ? YearMonth.from(c.getFecha()).toString()
                    : c.getFecha().toLocalDate().toString();
            long[] punto = serie.computeIfAbsent(etiqueta, k -> new long[2]);
            punto[0]++;
            punto[1] += c.getTotal();
        }

        List<ProductoMasVendido> masVendidos = porProducto.entrySet().stream()
                .map(e -> new ProductoMasVendido(e.getKey(), e.getValue().nombre,
                        e.getValue().unidades, e.getValue().monto))
                .sorted(Comparator.comparingLong(ProductoMasVendido::unidades).reversed()
                        .thenComparing(Comparator.comparingLong(ProductoMasVendido::monto).reversed()))
                .limit(TOP_PRODUCTOS)
                .toList();

        List<PuntoVentas> puntos = new ArrayList<>();
        serie.forEach((etiqueta, v) -> puntos.add(new PuntoVentas(etiqueta, v[0], v[1])));

        long total = vendidas.size();
        return new ReporteResumen(
                p,
                desde == null ? null : desde.toString(),
                hoy.toString(),
                total,
                montoTotal,
                unidades,
                total == 0 ? 0 : montoTotal / total,
                porEstado.get(EstadoCompra.CANCELADA.name()),
                porEstado,
                masVendidos,
                puntos);
    }

    // Rellena con ceros los días/meses sin ventas para que el gráfico no tenga huecos
    private void prepararSerie(TreeMap<String, long[]> serie, String periodo, LocalDate desde, LocalDate hoy) {
        if (periodo.equals("semana") || periodo.equals("mes")) {
            for (LocalDate dia = desde; !dia.isAfter(hoy); dia = dia.plusDays(1)) {
                serie.put(dia.toString(), new long[2]);
            }
        } else if (periodo.equals("anio")) {
            YearMonth fin = YearMonth.from(hoy);
            for (YearMonth mes = YearMonth.from(desde); !mes.isAfter(fin); mes = mes.plusMonths(1)) {
                serie.put(mes.toString(), new long[2]);
            }
        }
    }
}
