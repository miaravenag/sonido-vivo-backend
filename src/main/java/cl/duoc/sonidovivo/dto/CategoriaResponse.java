package cl.duoc.sonidovivo.dto;

import cl.duoc.sonidovivo.model.Categoria;

public record CategoriaResponse(Long id, String nombre, long cantidadProductos) {

    public static CategoriaResponse desde(Categoria c, long cantidadProductos) {
        return new CategoriaResponse(c.getId(), c.getNombre(), cantidadProductos);
    }
}
