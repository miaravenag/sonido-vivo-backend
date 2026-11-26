package cl.duoc.sonidovivo.dto;

import cl.duoc.sonidovivo.model.Compra;

import java.util.List;

public record CompraResponse(
        Long id,
        String fecha,
        long total,
        String estado,
        Long clienteId,
        String clienteNombre,
        String clienteEmail,
        List<DetalleCompraResponse> items) {

    public static CompraResponse desde(Compra c) {
        return new CompraResponse(
                c.getId(),
                c.getFecha().toString(),
                c.getTotal(),
                c.getEstado().name(),
                c.getUsuario().getId(),
                c.getUsuario().getNombre() + " " + c.getUsuario().getApellido(),
                c.getUsuario().getEmail(),
                c.getDetalles().stream().map(DetalleCompraResponse::desde).toList());
    }
}
