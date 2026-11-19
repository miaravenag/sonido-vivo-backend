package cl.duoc.sonidovivo.config;

import cl.duoc.sonidovivo.model.Categoria;
import cl.duoc.sonidovivo.model.Producto;
import cl.duoc.sonidovivo.model.Usuario;
import cl.duoc.sonidovivo.repository.CategoriaRepository;
import cl.duoc.sonidovivo.repository.ProductoRepository;
import cl.duoc.sonidovivo.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner cargarDatosDemo(UsuarioRepository usuarioRepository,
                                      CategoriaRepository categoriaRepository,
                                      ProductoRepository productoRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.count() == 0) {
                usuarioRepository.saveAll(List.of(
                        usuario("ProfeChristian", "Administrador", LocalDate.of(1990, 5, 10), "admin@duocuc.cl",
                                passwordEncoder.encode("Admin123"), "Av. Concha y Toro 1234", "Metropolitana", "Masculino", "administrador"),
                        usuario("Víctor", "Vendedor", LocalDate.of(1995, 8, 21), "vendedor@duocuc.cl",
                                passwordEncoder.encode("Vendedor123"), "Calle Los Olivos 456", "Valparaíso", "Masculino", "vendedor"),
                        usuario("Camila", "Cliente", LocalDate.of(2000, 1, 15), "cliente@duocuc.cl",
                                passwordEncoder.encode("Cliente123"), "Pasaje Las Rosas 789", "Biobío", "Otro", "cliente")));
            }

            if (categoriaRepository.count() == 0) {
                for (String nombre : List.of("Guitarras", "Bajos", "Baterías", "Teclados",
                        "Amplificadores", "Micrófonos", "Pedales", "Accesorios")) {
                    categoriaRepository.save(new Categoria(null, nombre));
                }
            }

            if (productoRepository.count() == 0) {
                productoRepository.saveAll(List.of(
                        producto(categoriaRepository, "Guitarra Eléctrica Fender Player Stratocaster", "Guitarras", 799900, 749900, 5,
                                "https://images.unsplash.com/photo-1550291652-6ea9114a47b1?w=500",
                                "Guitarra eléctrica legendaria con cuerpo de aliso, mástil de arce y 3 cápsulas single-coil."),
                        producto(categoriaRepository, "Bajo Eléctrico Ibanez SR300E", "Bajos", 420000, null, 3,
                                "https://images.unsplash.com/photo-1564186763535-ebb21ef5277f?w=500",
                                "Bajo eléctrico de 4 cuerdas, cuerpo ligero, mástil cómodo de 5 piezas de arce/nogal."),
                        producto(categoriaRepository, "Batería Acústica Pearl Export EXX", "Baterías", 890000, 820000, 2,
                                "https://images.unsplash.com/photo-1519892300165-cb5542fb47c7?w=500",
                                "Batería completa de 5 piezas construida en álamo y caoba asiática con herrajes de alta durabilidad."),
                        producto(categoriaRepository, "Teclado Sintetizador Yamaha PSR-E373", "Teclados", 260000, null, 8,
                                "https://images.unsplash.com/photo-1520523839897-bd0b52f945a0?w=500",
                                "Teclado portátil de 61 teclas sensibles a la pulsación, con 622 voces de alta calidad."),
                        producto(categoriaRepository, "Amplificador de Guitarra Boss Katana 50 MkII", "Amplificadores", 310000, 289900, 4,
                                "https://images.unsplash.com/photo-1543443374-b6fe10a6ab7b?w=500",
                                "Amplificador combo de 50W con parlante de 12 pulgadas y múltiples efectos integrados BOSS."),
                        producto(categoriaRepository, "Micrófono Dinámico Shure SM58", "Micrófonos", 125000, null, 12,
                                "https://images.unsplash.com/photo-1590602847861-f357a9332bbc?w=500",
                                "El micrófono dinámico para voz más popular del mundo. Respuesta de frecuencia optimizada."),
                        producto(categoriaRepository, "Pedal de Efecto Distortion Boss DS-1", "Pedales", 65000, 58000, 10,
                                "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=500",
                                "Pedal clásico de distorsión utilizado por guitarristas icónicos en todo el mundo."),
                        producto(categoriaRepository, "Guitarra Acústica Taylor GS Mini", "Guitarras", 690000, null, 2,
                                "https://images.unsplash.com/photo-1510915361894-db8b60106cb1?w=500",
                                "Guitarra acústica de tamaño compacto pero sonido potente y cálido, tapa de abeto macizo."),
                        producto(categoriaRepository, "Audífonos de Estudio Audio-Technica ATH-M50x", "Accesorios", 169000, 149900, 6,
                                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500",
                                "Audífonos profesionales de monitoreo con respuesta precisa y aislamiento pasivo de sonido."),
                        // Sin stock a propósito, para probar el estado AGOTADO
                        producto(categoriaRepository, "Interfaz de Audio Focusrite Scarlett 2i2 4th Gen", "Accesorios", 210000, null, 0,
                                "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=500",
                                "Interfaz de audio USB de 2 entradas / 2 salidas con preamplificadores de ultra bajo ruido.")));
            }
        };
    }

    private Usuario usuario(String nombre, String apellido, LocalDate nacimiento, String email, String passwordEncriptada,
                            String direccion, String region, String genero, String rol) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setFechaNacimiento(nacimiento);
        u.setEmail(email);
        u.setPassword(passwordEncriptada);
        u.setDireccion(direccion);
        u.setRegion(region);
        u.setGenero(genero);
        u.setRol(rol);
        u.setBloqueado(false);
        u.setIntentosFallidos(0);
        return u;
    }

    private Producto producto(CategoriaRepository categoriaRepository, String nombre, String categoria, int precio,
                              Integer precioOferta, int stock, String imagen, String descripcion) {
        Producto p = new Producto();
        p.setNombre(nombre);
        p.setCategoria(categoriaRepository.findByNombreIgnoreCase(categoria).orElseThrow());
        p.setPrecio(precio);
        p.setPrecioOferta(precioOferta);
        p.setStock(stock);
        p.setImagen(imagen);
        p.setDescripcion(descripcion);
        return p;
    }
}
