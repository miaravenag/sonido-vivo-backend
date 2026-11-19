package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "productos")
public class Producto {
    @Column(name = "id_producto")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @Column(nullable = false)
    private int precio;

    @Column(name = "precio_oferta")
    private Integer precioOferta;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false, length = 500)
    private String imagen;

    @Column(nullable = false, length = 1000)
    private String descripcion;
}
