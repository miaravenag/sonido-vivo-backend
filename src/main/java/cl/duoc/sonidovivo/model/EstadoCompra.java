package cl.duoc.sonidovivo.model;

// Ciclo de vida de una compra. Se guarda como texto en la base de datos.
public enum EstadoCompra {
    PENDIENTE,
    EN_PREPARACION,
    ENVIADA,
    ENTREGADA,
    CANCELADA;

    // Transiciones permitidas: ENTREGADA y CANCELADA son estados finales
    public boolean puedePasarA(EstadoCompra nuevo) {
        return switch (this) {
            case PENDIENTE -> nuevo == EN_PREPARACION || nuevo == CANCELADA;
            case EN_PREPARACION -> nuevo == ENVIADA || nuevo == CANCELADA;
            case ENVIADA -> nuevo == ENTREGADA;
            case ENTREGADA, CANCELADA -> false;
        };
    }
}
