package cl.duoc.sonidovivo.dto;

import cl.duoc.sonidovivo.model.EstadoCompra;
import jakarta.validation.constraints.NotNull;

public record EstadoCompraRequest(
        @NotNull(message = "El estado es obligatorio.")
        EstadoCompra estado) {
}
