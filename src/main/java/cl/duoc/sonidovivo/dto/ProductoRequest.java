package cl.duoc.sonidovivo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductoRequest(
        @NotBlank(message = "El nombre del producto es obligatorio.")
        @Size(max = 150, message = "El nombre del producto no puede superar los 150 caracteres.")
        String nombre,

        // Nombre de una categoría existente
        @NotBlank(message = "Selecciona una categoría.")
        String categoria,

        @NotNull(message = "El precio es obligatorio.")
        @Min(value = 1, message = "El precio debe ser un entero mayor a 0.")
        @Max(value = 1000000, message = "El precio no puede superar $1.000.000.")
        Integer precio,

        @Min(value = 1, message = "El precio de oferta debe ser un entero mayor a 0.")
        Integer precioOferta,

        @NotNull(message = "El stock es obligatorio.")
        @Min(value = 0, message = "El stock debe ser un entero mayor o igual a 0.")
        Integer stock,

        @NotBlank(message = "La URL de la imagen es obligatoria.")
        @Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres.")
        String imagen,

        @NotBlank(message = "La descripción es obligatoria.")
        @Size(max = 1000, message = "La descripción no puede superar los 1000 caracteres.")
        String descripcion) {
}
