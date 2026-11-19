package cl.duoc.sonidovivo.service;

import cl.duoc.sonidovivo.dto.ProductoRequest;
import cl.duoc.sonidovivo.dto.ProductoResponse;
import cl.duoc.sonidovivo.exception.RecursoNoEncontradoException;
import cl.duoc.sonidovivo.exception.ReglaNegocioException;
import cl.duoc.sonidovivo.model.Categoria;
import cl.duoc.sonidovivo.model.Producto;
import cl.duoc.sonidovivo.repository.CategoriaRepository;
import cl.duoc.sonidovivo.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<ProductoResponse> listar() {
        return productoRepository.findAll(Sort.by("id")).stream().map(ProductoResponse::desde).toList();
    }

    public ProductoResponse buscarPorId(Long id) {
        return ProductoResponse.desde(obtener(id));
    }

    public ProductoResponse crear(ProductoRequest request) {
        Producto producto = new Producto();
        aplicarDatos(producto, request);
        return ProductoResponse.desde(productoRepository.save(producto));
    }

    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = obtener(id);
        aplicarDatos(producto, request);
        return ProductoResponse.desde(productoRepository.save(producto));
    }

    public void eliminar(Long id) {
        productoRepository.delete(obtener(id));
    }

    // Administrador y vendedor pueden ajustar el stock
    public ProductoResponse actualizarStock(Long id, int stock) {
        Producto producto = obtener(id);
        producto.setStock(stock);
        return ProductoResponse.desde(productoRepository.save(producto));
    }

    private void aplicarDatos(Producto producto, ProductoRequest request) {
        Categoria categoria = categoriaRepository.findByNombreIgnoreCase(request.categoria().trim())
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.BAD_REQUEST, "Selecciona una categoría existente."));

        // Regla de negocio: la oferta debe ser menor que el precio normal
        if (request.precioOferta() != null && request.precioOferta() >= request.precio()) {
            throw new ReglaNegocioException(HttpStatus.BAD_REQUEST,
                    "El precio de oferta debe ser un entero mayor a 0 y menor al precio normal.");
        }

        producto.setNombre(request.nombre().trim());
        producto.setCategoria(categoria);
        producto.setPrecio(request.precio());
        producto.setPrecioOferta(request.precioOferta());
        producto.setStock(request.stock());
        producto.setImagen(request.imagen().trim());
        producto.setDescripcion(request.descripcion().trim());
    }

    private Producto obtener(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El producto solicitado no existe o fue removido."));
    }
}
