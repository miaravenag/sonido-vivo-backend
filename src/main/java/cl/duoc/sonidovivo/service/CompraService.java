package cl.duoc.sonidovivo.service;

import cl.duoc.sonidovivo.dto.CompraRequest;
import cl.duoc.sonidovivo.dto.CompraResponse;
import cl.duoc.sonidovivo.dto.ItemCompraRequest;
import cl.duoc.sonidovivo.exception.RecursoNoEncontradoException;
import cl.duoc.sonidovivo.exception.ReglaNegocioException;
import cl.duoc.sonidovivo.model.Compra;
import cl.duoc.sonidovivo.model.DetalleCompra;
import cl.duoc.sonidovivo.model.EstadoCompra;
import cl.duoc.sonidovivo.model.Producto;
import cl.duoc.sonidovivo.model.Usuario;
import cl.duoc.sonidovivo.repository.CompraRepository;
import cl.duoc.sonidovivo.repository.ProductoRepository;
import cl.duoc.sonidovivo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // Todo ocurre en una sola transacción: si algo falla, no se descuenta stock ni se guarda la compra
    @Transactional
    public CompraResponse crear(CompraRequest request, Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe."));

        // Si el mismo producto viene repetido, se suman sus cantidades
        Map<Long, Integer> cantidades = new LinkedHashMap<>();
        for (ItemCompraRequest item : request.items()) {
            cantidades.merge(item.productoId(), item.cantidad(), Integer::sum);
        }

        Compra compra = new Compra();
        compra.setUsuario(usuario);
        compra.setFecha(LocalDateTime.now());

        long total = 0;
        for (Map.Entry<Long, Integer> entrada : cantidades.entrySet()) {
            Producto producto = productoRepository.findById(entrada.getKey())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "Uno de los productos del carrito ya no existe."));
            int cantidad = entrada.getValue();

            // Regla de negocio: no se puede comprar más unidades que el stock disponible
            if (cantidad > producto.getStock()) {
                throw new ReglaNegocioException(HttpStatus.CONFLICT,
                        "Stock insuficiente de \"" + producto.getNombre() + "\" (disponible: "
                                + producto.getStock() + "). Ajusta la cantidad e intenta nuevamente.");
            }

            // El precio lo define el servidor (oferta si existe), nunca el cliente
            int precioUnitario = producto.getPrecioOferta() != null ? producto.getPrecioOferta() : producto.getPrecio();
            producto.setStock(producto.getStock() - cantidad);

            DetalleCompra detalle = new DetalleCompra();
            detalle.setProductoId(producto.getId());
            detalle.setNombreProducto(producto.getNombre());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setCantidad(cantidad);
            compra.agregarDetalle(detalle);

            total += (long) precioUnitario * cantidad;
        }

        compra.setTotal(total);
        return CompraResponse.desde(compraRepository.save(compra));
    }

    // Historial del usuario que inició sesión
    @Transactional(readOnly = true)
    public List<CompraResponse> listarDeUsuario(Long usuarioId) {
        return compraRepository.findByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(CompraResponse::desde).toList();
    }

    // Historial completo (panel de administración)
    @Transactional(readOnly = true)
    public List<CompraResponse> listarTodas() {
        return compraRepository.findAllByOrderByFechaDesc().stream()
                .map(CompraResponse::desde).toList();
    }

    // Administrador: avanza o cancela una compra. Al cancelar se devuelve el stock.
    @Transactional
    public CompraResponse cambiarEstado(Long compraId, EstadoCompra nuevo) {
        Compra compra = compraRepository.findById(compraId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La compra no existe."));
        EstadoCompra actual = compra.getEstado();

        if (!actual.puedePasarA(nuevo)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "No se puede cambiar una compra de " + actual + " a " + nuevo + ".");
        }

        if (nuevo == EstadoCompra.CANCELADA) {
            for (DetalleCompra detalle : compra.getDetalles()) {
                int cantidad = detalle.getCantidad();
                // Si el producto fue eliminado después de la compra, no hay stock que devolver
                productoRepository.findById(detalle.getProductoId())
                        .ifPresent(producto -> producto.setStock(producto.getStock() + cantidad));
            }
        }

        compra.setEstado(nuevo);
        return CompraResponse.desde(compra);
    }
}
