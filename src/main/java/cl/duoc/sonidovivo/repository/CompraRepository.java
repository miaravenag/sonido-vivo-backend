package cl.duoc.sonidovivo.repository;

import cl.duoc.sonidovivo.model.Compra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long> {
    List<Compra> findByUsuarioIdOrderByFechaDesc(Long usuarioId);

    List<Compra> findAllByOrderByFechaDesc();

    boolean existsByUsuarioId(Long usuarioId);
}
