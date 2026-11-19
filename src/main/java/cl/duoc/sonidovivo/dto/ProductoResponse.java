package cl.duoc.sonidovivo.dto;

import cl.duoc.sonidovivo.model.Producto;

public record ProductoResponse(
        Long id,
        String nombre,
        String categoria,
        int precio,
        Integer precioOferta,
        int stock,
        String imagen,
        String descripcion) {

    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(
                p.getId(),
                p.getNombre(),
                p.getCategoria().getNombre(),
                p.getPrecio(),
                p.getPrecioOferta(),
                p.getStock(),
                p.getImagen(),
                p.getDescripcion());
    }
}
