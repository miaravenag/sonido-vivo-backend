package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Cada línea de una compra guarda una copia del nombre y del precio al momento de comprar,
// así el historial no cambia si luego se modifica o elimina el producto.
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "detalles_compra")
public class DetalleCompra {
    @Column(name = "id_detalle")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_compra")
    private Compra compra;

    @Column(name = "producto_id", nullable = false)
    private Long productoId;

    @Column(name = "nombre_producto", nullable = false, length = 150)
    private String nombreProducto;

    @Column(name = "precio_unitario", nullable = false)
    private int precioUnitario;

    @Column(nullable = false)
    private int cantidad;
}
