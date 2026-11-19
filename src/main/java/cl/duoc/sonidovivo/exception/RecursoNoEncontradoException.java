package cl.duoc.sonidovivo.exception;

import org.springframework.http.HttpStatus;

public class RecursoNoEncontradoException extends ReglaNegocioException {
    public RecursoNoEncontradoException(String mensaje) {
        super(HttpStatus.NOT_FOUND, mensaje);
    }
}
