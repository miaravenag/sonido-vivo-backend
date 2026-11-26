package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "compras")
public class Compra {
    @Column(name = "id_compra")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false)
    private long total;

    // columnDefinition con DEFAULT permite que ddl-auto=update agregue la columna aunque ya existan compras
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'PENDIENTE'")
    private EstadoCompra estado = EstadoCompra.PENDIENTE;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleCompra> detalles = new ArrayList<>();

    public void agregarDetalle(DetalleCompra detalle) {
        detalle.setCompra(this);
        detalles.add(detalle);
    }
}
