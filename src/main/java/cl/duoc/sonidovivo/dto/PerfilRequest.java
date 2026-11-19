package cl.duoc.sonidovivo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

// Datos que un usuario puede cambiar de su propio perfil (sin rol, sin contraseña, sin bloqueo)
public record PerfilRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 60, message = "El nombre no puede superar los 60 caracteres.")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio.")
        @Size(max = 60, message = "El apellido no puede superar los 60 caracteres.")
        String apellido,

        @NotNull(message = "La fecha de nacimiento es obligatoria.")
        LocalDate fechaNacimiento,

        @NotBlank(message = "El correo es obligatorio.")
        @Pattern(regexp = "(?i)^[\\w.+-]+@duocuc\\.cl$",
                message = "Solo se permiten correos institucionales duocuc.cl (Ejemplo: usuario@duocuc.cl).")
        String email,

        @NotBlank(message = "La dirección es obligatoria.")
        @Size(max = 150, message = "La dirección no puede superar los 150 caracteres.")
        String direccion,

        @NotBlank(message = "La región es obligatoria.")
        String region,

        @NotBlank(message = "El género es obligatorio.")
        @Pattern(regexp = "Masculino|Femenino|Otro", message = "El género no es válido.")
        String genero) {
}
