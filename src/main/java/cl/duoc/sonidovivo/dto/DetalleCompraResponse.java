package cl.duoc.sonidovivo.dto;

import cl.duoc.sonidovivo.model.DetalleCompra;

public record DetalleCompraResponse(
        Long productoId,
        String nombreProducto,
        int precioUnitario,
        int cantidad,
        long subtotal) {

    public static DetalleCompraResponse desde(DetalleCompra d) {
        return new DetalleCompraResponse(
                d.getProductoId(),
                d.getNombreProducto(),
                d.getPrecioUnitario(),
                d.getCantidad(),
                (long) d.getPrecioUnitario() * d.getCantidad());
    }
}
