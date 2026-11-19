package cl.duoc.sonidovivo.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuarios")
public class Usuario {
    @Column(name = "id_usuario")
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 60)
    private String apellido;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Se guarda encriptada con BCrypt, nunca en texto plano
    @Column(nullable = false, length = 100)
    private String password;

    @Column(nullable = false, length = 150)
    private String direccion;

    @Column(nullable = false, length = 50)
    private String region;

    @Column(nullable = false, length = 20)
    private String genero;

    // administrador | vendedor | cliente
    @Column(nullable = false, length = 15)
    private String rol;

    @Column(nullable = false)
    private boolean bloqueado;

    @Column(name = "intentos_fallidos", nullable = false)
    private int intentosFallidos;
}
