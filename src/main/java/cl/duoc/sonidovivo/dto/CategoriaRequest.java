package cl.duoc.sonidovivo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(
        @NotNull(message = "El nombre de la categoría es obligatorio.")
        @Size(min = 3, max = 50, message = "El nombre de la categoría debe tener entre 3 y 50 caracteres.")
        String nombre) {
}
