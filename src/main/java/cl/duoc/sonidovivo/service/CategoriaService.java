package cl.duoc.sonidovivo.service;

import cl.duoc.sonidovivo.dto.CategoriaResponse;
import cl.duoc.sonidovivo.exception.RecursoNoEncontradoException;
import cl.duoc.sonidovivo.exception.ReglaNegocioException;
import cl.duoc.sonidovivo.model.Categoria;
import cl.duoc.sonidovivo.repository.CategoriaRepository;
import cl.duoc.sonidovivo.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    public List<CategoriaResponse> listar() {
        return categoriaRepository.findAll().stream()
                .map(c -> CategoriaResponse.desde(c, productoRepository.countByCategoriaId(c.getId())))
                .toList();
    }

    public CategoriaResponse crear(String nombre) {
        String limpio = validarNombre(nombre);
        if (categoriaRepository.existsByNombreIgnoreCase(limpio)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre.");
        }

        Categoria categoria = categoriaRepository.save(new Categoria(null, limpio));
        return CategoriaResponse.desde(categoria, 0);
    }

    public CategoriaResponse renombrar(Long id, String nombre) {
        Categoria categoria = obtener(id);
        String limpio = validarNombre(nombre);
        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(limpio, id)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe una categoría con ese nombre.");
        }

        categoria.setNombre(limpio);
        categoriaRepository.save(categoria);
        return CategoriaResponse.desde(categoria, productoRepository.countByCategoriaId(id));
    }

    public void eliminar(Long id) {
        Categoria categoria = obtener(id);

        // Regla de negocio: no se elimina una categoría que todavía tiene productos
        long cantidad = productoRepository.countByCategoriaId(id);
        if (cantidad > 0) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT,
                    "No se puede eliminar \"" + categoria.getNombre() + "\": tiene " + cantidad
                            + " producto(s) asociado(s). Reasígnalos o elimínalos primero.");
        }

        categoriaRepository.delete(categoria);
    }

    private String validarNombre(String nombre) {
        String limpio = nombre.trim();
        if (limpio.length() < 3) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST, "El nombre de la categoría debe tener al menos 3 caracteres.");
        }
        return limpio;
    }

    private Categoria obtener(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La categoría no existe."));
    }
}
