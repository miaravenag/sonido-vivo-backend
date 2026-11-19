package cl.duoc.sonidovivo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

// El cliente solo envía qué productos y cuántas unidades; los precios los toma el servidor desde la base de datos
public record CompraRequest(
        @NotEmpty(message = "El carrito está vacío.")
        List<@Valid ItemCompraRequest> items) {
}
