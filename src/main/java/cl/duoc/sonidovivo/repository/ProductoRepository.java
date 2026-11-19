package cl.duoc.sonidovivo.repository;

import cl.duoc.sonidovivo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    long countByCategoriaId(Long categoriaId);
}
