package cl.duoc.sonidovivo.dto;

import cl.duoc.sonidovivo.model.Usuario;

// Lo que ve el cliente: nunca incluye la contraseña
public record UsuarioResponse(
        Long id,
        String nombre,
        String apellido,
        String fechaNacimiento,
        String email,
        String direccion,
        String region,
        String genero,
        String rol,
        boolean bloqueado) {

    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNombre(),
                u.getApellido(),
                u.getFechaNacimiento().toString(),
                u.getEmail(),
                u.getDireccion(),
                u.getRegion(),
                u.getGenero(),
                u.getRol(),
                u.isBloqueado());
    }
}
