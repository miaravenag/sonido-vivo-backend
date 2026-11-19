package cl.duoc.sonidovivo.exception;

import org.springframework.http.HttpStatus;

// Error esperado de la aplicación (validación o regla de negocio) con su código HTTP
public class ReglaNegocioException extends RuntimeException {
    private final HttpStatus status;

    public ReglaNegocioException(HttpStatus status, String mensaje) {
        super(mensaje);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
