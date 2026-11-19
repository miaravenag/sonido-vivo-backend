package cl.duoc.sonidovivo.service;

import cl.duoc.sonidovivo.dto.PerfilRequest;
import cl.duoc.sonidovivo.dto.UsuarioRequest;
import cl.duoc.sonidovivo.dto.UsuarioResponse;
import cl.duoc.sonidovivo.exception.ReglaNegocioException;
import cl.duoc.sonidovivo.exception.RecursoNoEncontradoException;
import cl.duoc.sonidovivo.model.Usuario;
import cl.duoc.sonidovivo.repository.CompraRepository;
import cl.duoc.sonidovivo.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

@Service
public class UsuarioService {
    private static final int EDAD_MINIMA = 14;
    private static final int EDAD_MAXIMA = 120;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Listar todos
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(UsuarioResponse::desde).toList();
    }

    // 2. Buscar por ID
    public UsuarioResponse buscarPorId(Long id) {
        return UsuarioResponse.desde(obtener(id));
    }

    // 3. Registro público: siempre crea un cliente activo
    public UsuarioResponse registrarCliente(UsuarioRequest request) {
        return guardarNuevo(request, "cliente", false);
    }

    // 4. Crear desde el panel: el administrador crea cualquier rol; el vendedor solo clientes
    public UsuarioResponse crear(UsuarioRequest request, Long creadorId) {
        Usuario creador = obtener(creadorId);
        boolean esAdministrador = creador.getRol().equals("administrador");
        String rol = request.rol() == null ? "cliente" : request.rol();

        if (!esAdministrador && !rol.equals("cliente")) {
            throw new ReglaNegocioException(HttpStatus.FORBIDDEN, "Tu rol solo permite crear usuarios con rol cliente.");
        }

        boolean bloqueado = esAdministrador && Boolean.TRUE.equals(request.bloqueado());
        return guardarNuevo(request, rol, bloqueado);
    }

    // 5. Actualizar (solo administrador)
    public UsuarioResponse actualizar(Long id, UsuarioRequest request, Long actualId) {
        Usuario usuario = obtener(id);
        String rol = request.rol() == null ? usuario.getRol() : request.rol();
        boolean bloqueado = Boolean.TRUE.equals(request.bloqueado());

        // Regla de negocio: el administrador no puede quitarse su propio rol ni bloquearse
        if (id.equals(actualId) && (!rol.equals(usuario.getRol()) || bloqueado)) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                    "No puedes cambiar tu propio rol ni bloquear tu propia cuenta.");
        }

        validarFechaNacimiento(request.fechaNacimiento());
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "El correo ingresado ya se encuentra registrado.");
        }

        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setFechaNacimiento(request.fechaNacimiento());
        usuario.setEmail(email);
        usuario.setDireccion(request.direccion().trim());
        usuario.setRegion(request.region());
        usuario.setGenero(request.genero());
        usuario.setRol(rol);

        // Contraseña vacía = no se cambia
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }

        // Al desbloquear una cuenta se reinicia el contador de intentos fallidos
        if (usuario.isBloqueado() && !bloqueado) {
            usuario.setIntentosFallidos(0);
        }
        usuario.setBloqueado(bloqueado);

        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    // 6. Borrar (solo administrador)
    public void eliminar(Long id, Long actualId) {
        Usuario usuario = obtener(id);

        if (id.equals(actualId)) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "No puedes eliminar tu propia cuenta.");
        }
        // Regla de negocio: no se borra a quien tiene compras, para no perder el historial
        if (compraRepository.existsByUsuarioId(id)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "No se puede eliminar a este usuario porque tiene compras registradas. Puedes bloquear su cuenta.");
        }

        usuarioRepository.delete(usuario);
    }

    // 7. Cada usuario edita su propio perfil
    public UsuarioResponse actualizarPerfil(Long actualId, PerfilRequest request) {
        Usuario usuario = obtener(actualId);

        validarFechaNacimiento(request.fechaNacimiento());
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCaseAndIdNot(email, actualId)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "El correo ingresado ya se encuentra registrado.");
        }

        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setFechaNacimiento(request.fechaNacimiento());
        usuario.setEmail(email);
        usuario.setDireccion(request.direccion().trim());
        usuario.setRegion(request.region());
        usuario.setGenero(request.genero());

        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    private UsuarioResponse guardarNuevo(UsuarioRequest request, String rol, boolean bloqueado) {
        if (request.password() == null || request.password().isBlank()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "La contraseña es obligatoria.");
        }

        validarFechaNacimiento(request.fechaNacimiento());
        String email = request.email().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "El correo ingresado ya se encuentra registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(request.nombre().trim());
        usuario.setApellido(request.apellido().trim());
        usuario.setFechaNacimiento(request.fechaNacimiento());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(request.password()));
        usuario.setDireccion(request.direccion().trim());
        usuario.setRegion(request.region());
        usuario.setGenero(request.genero());
        usuario.setRol(rol);
        usuario.setBloqueado(bloqueado);
        usuario.setIntentosFallidos(0);

        return UsuarioResponse.desde(usuarioRepository.save(usuario));
    }

    // Regla de negocio: edad entre 14 y 120 años, y la fecha no puede ser futura
    private void validarFechaNacimiento(LocalDate fecha) {
        LocalDate hoy = LocalDate.now();
        if (fecha.isAfter(hoy)) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "La fecha de nacimiento no es válida.");
        }
        int edad = Period.between(fecha, hoy).getYears();
        if (edad > EDAD_MAXIMA) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "La fecha de nacimiento no es válida.");
        }
        if (edad < EDAD_MINIMA) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "Debes tener al menos " + EDAD_MINIMA + " años de edad.");
        }
    }

    private Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario no existe."));
    }
}
