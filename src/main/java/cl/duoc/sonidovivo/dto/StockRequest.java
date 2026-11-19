package cl.duoc.sonidovivo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockRequest(
        @NotNull(message = "El stock es obligatorio.")
        @Min(value = 0, message = "El stock debe ser un entero mayor o igual a 0.")
        Integer stock) {
}
