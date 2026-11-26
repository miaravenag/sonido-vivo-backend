# API REST de Sonido Vivo

Backend de la tienda de instrumentos musicales **Sonido Vivo**, desarrollado con **Spring Boot**, **Spring Data JPA**,
**Spring Security + JWT** y **MySQL**. Documentación interactiva con **Swagger**.

---

## 🛠️ Tecnologías Utilizadas

- **Java 21**
- **Spring Boot 4**
- **Spring Data JPA** + **MySQL Server**
- **Spring Security** con **JWT** (jjwt)
- **Bean Validation** (`@Valid`)
- **Swagger / OpenAPI** (springdoc)
- **Lombok**
- **Maven**

---

## 📋 Requisitos Previos

1. **JDK 21** configurado en las variables de entorno.
2. **MySQL Server** en ejecución.
3. **Maven** (opcional si usas el wrapper incluido `./mvnw`).

---

## 🗄️ Base de Datos

Crea la base de datos antes de iniciar la aplicación:

```sql
CREATE DATABASE bd_sonidovivo;
```

Las tablas (`usuarios`, `categorias`, `productos`, `compras`, `detalles_compra`) las crea Hibernate automáticamente
(`ddl-auto=update`).

---

## ⚙️ Configuración y secretos

Las contraseñas y la clave JWT **no están en el repositorio**. Antes de ejecutar:

1. Copia `secrets.properties.example` como `secrets.properties` (en la raíz, junto al `pom.xml`).
2. Completa tus valores:

```properties
DB_PASSWORD=la_contraseña_de_tu_mysql
JWT_SECRET=una-clave-larga-y-aleatoria-de-al-menos-32-caracteres
```

`secrets.properties` está en `.gitignore`. También puedes usar variables de entorno con los mismos nombres
(`DB_PASSWORD`, `JWT_SECRET`) y, opcionalmente, `DB_URL`, `DB_USER`, `CORS_ORIGINS`.

Sin `JWT_SECRET` la aplicación no arranca (a propósito).

- El token JWT dura `jwt.expiration-ms` (por defecto 2 horas).
- Aunque el token se renueve, la sesión dura como máximo `jwt.max-session-ms` (por defecto 8 horas); después hay que
  volver a iniciar sesión.

---

## 🚀 Ejecución del Proyecto

```bash
./mvnw spring-boot:run
```

*(En Windows: `mvnw.cmd spring-boot:run`, o ejecutar `SonidoVivoApplication` desde el IDE).*

- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`

En Swagger, ejecuta `POST /auth/login`, copia el `token` de la respuesta y pégalo en el botón **Authorize** para
probar los endpoints protegidos.

---

## 📦 Carga Inicial de Datos

Al iniciar con la base vacía, un `CommandLineRunner` (`DataInitializer`) inserta categorías, productos y estos usuarios
(las contraseñas se guardan encriptadas con BCrypt):

| Rol | Correo | Contraseña |
|---|---|---|
| administrador | `admin@duocuc.cl` | `Admin123` |
| vendedor | `vendedor@duocuc.cl` | `Vendedor123` |
| cliente | `cliente@duocuc.cl` | `Cliente123` |

Si no existe ninguna compra, también se crean **21 compras de ejemplo** del cliente repartidas en el último año
(con distintos estados) para que el dashboard tenga datos en la demostración. No modifican el stock.

---

## 📡 Endpoints de la API

| Método | Endpoint | Descripción | Acceso |
| :--- | :--- | :--- | :--- |
| POST | `/auth/login` | Inicia sesión y entrega el token JWT | Público |
| POST | `/auth/registro` | Registra una cuenta de cliente | Público |
| POST | `/auth/renovar` | Entrega un token nuevo a partir de uno válido | Usuario con sesión |
| GET | `/productos`, `/productos/{id}` | Consulta el catálogo | Público |
| GET | `/categorias` | Lista las categorías | Público |
| POST / PUT / DELETE | `/productos`, `/productos/{id}` | Gestión de productos | Administrador |
| PATCH | `/productos/{id}/stock` | Modifica solo el stock | Administrador, Vendedor |
| POST / PUT / DELETE | `/categorias`, `/categorias/{id}` | Gestión de categorías | Administrador |
| GET | `/usuarios`, `/usuarios/{id}` | Consulta usuarios | Administrador |
| POST | `/usuarios` | Crea un usuario (el vendedor solo clientes) | Administrador, Vendedor |
| PUT / DELETE | `/usuarios/{id}` | Edita o elimina un usuario | Administrador |
| PUT | `/usuarios/perfil` | Edita el perfil propio | Usuario con sesión |
| POST | `/compras` | Registra una compra con el carrito | Usuario con sesión |
| GET | `/compras/mis-compras` | Historial del usuario | Usuario con sesión |
| GET | `/compras` | Historial de todas las compras | Administrador |
| PATCH | `/compras/{id}/estado` | Cambia el estado de una compra | Administrador |
| GET | `/reportes/resumen?periodo=semana\|mes\|anio\|todo` | Indicadores del dashboard | Administrador |

Las rutas protegidas requieren la cabecera `Authorization: Bearer <token>`. Sin token responden `401`; con un rol
insuficiente, `403`. Los errores siempre tienen el formato `{ "mensaje": "..." }`.

### Ejemplos de peticiones JSON

**POST /auth/login**
```json
{ "email": "admin@duocuc.cl", "password": "Admin123" }
```

**POST /compras** (los precios los calcula el servidor)
```json
{ "items": [ { "productoId": 1, "cantidad": 2 }, { "productoId": 4, "cantidad": 1 } ] }
```

**PATCH /compras/{id}/estado**
```json
{ "estado": "EN_PREPARACION" }
```

---

## ✅ Validaciones y reglas de negocio

- Correo institucional `@duocuc.cl`, único; contraseña de mínimo 6 caracteres con una mayúscula.
- Edad entre 14 y 120 años; la fecha de nacimiento no puede ser futura.
- 3 contraseñas incorrectas seguidas **bloquean la cuenta**; el administrador la puede desbloquear.
- El registro público siempre crea clientes; el vendedor solo puede crear clientes.
- El administrador no puede cambiar su propio rol, bloquearse ni eliminarse.
- No se elimina un usuario que tiene compras registradas.
- Productos: precio entre $1 y $1.000.000, precio de oferta menor al precio normal, stock mayor o igual a 0.
- Categorías: nombre único de al menos 3 caracteres; no se elimina una categoría con productos.
- Compras: se valida el stock disponible, el precio lo toma el servidor (oferta si existe), el stock se descuenta y la
  compra se guarda en una sola transacción. El historial conserva el nombre y el precio al momento de comprar.
- Estado de la compra: `PENDIENTE` → `EN_PREPARACION` → `ENVIADA` → `ENTREGADA`. Se puede pasar a `CANCELADA` desde
  `PENDIENTE` o `EN_PREPARACION` (se devuelve el stock). `ENTREGADA` y `CANCELADA` son estados finales; una transición
  no permitida responde `409`.
- Reportes: las compras canceladas no cuentan como venta. Periodos: `semana` (7 días), `mes` (30 días), `anio`
  (12 meses) y `todo`.
- Sesión: el login pone `401` con el mismo mensaje si el correo no existe o la contraseña es incorrecta.
