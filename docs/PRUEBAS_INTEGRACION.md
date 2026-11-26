# Pruebas de integración — Sonido Vivo

**Fecha de ejecución:** ____ / ____ / ______  **Probado por:** ____________________

**Entorno:** Backend Spring Boot (`http://localhost:8080`) · Frontend React/Vite (`http://localhost:5173`) · MySQL `bd_sonidovivo`

**Cuentas usadas:** admin `admin@duocuc.cl` · vendedor `vendedor@duocuc.cl` · cliente `cliente@duocuc.cl`

Marcar el resultado con ✅ (correcto) o ❌ (falla) y anotar la evidencia (captura, código HTTP, fila de la BD).

## 1. Conexión y persistencia

| N° | Prueba | Pasos | Resultado esperado | Resultado obtenido | ✅/❌ | Evidencia |
|---|---|---|---|---|---|---|
| 1.1 | Conexión front–back–BD | Abrir el catálogo en el frontend | Los productos se cargan desde la API (no hay datos fijos) | | | |
| 1.2 | Persistencia tras reinicio | Crear un producto, detener y volver a iniciar el backend, recargar el catálogo | El producto sigue existiendo | | | |
| 1.3 | Datos en la BD | `SELECT * FROM productos;` en MySQL | Coincide con lo que muestra la aplicación | | | |

## 2. CRUD (Swagger y panel de administración)

| N° | Prueba | Pasos | Resultado esperado | Resultado obtenido | ✅/❌ | Evidencia |
|---|---|---|---|---|---|---|
| 2.1 | Crear producto | `POST /productos` (admin) | 201 y aparece en el catálogo | | | |
| 2.2 | Consultar producto | `GET /productos/{id}` | 200 con el JSON del producto | | | |
| 2.3 | Actualizar producto | `PUT /productos/{id}` | 200 y el cambio se ve en la interfaz | | | |
| 2.4 | Eliminar producto | `DELETE /productos/{id}` | 204 y desaparece del catálogo | | | |
| 2.5 | CRUD de usuarios | Crear, editar, bloquear/desbloquear y eliminar un usuario en `/gestion/usuarios` | Cada operación se refleja en la BD | | | |
| 2.6 | CRUD de categorías | Crear, editar y eliminar una categoría | Cada operación se refleja en la BD | | | |
| 2.7 | Validación de datos | `POST /productos` con precio negativo | 400 con `{"mensaje": "..."}` | | | |

## 3. Autenticación y sesión

| N° | Prueba | Pasos | Resultado esperado | Resultado obtenido | ✅/❌ | Evidencia |
|---|---|---|---|---|---|---|
| 3.1 | Login correcto | Ingresar con credenciales válidas | Sesión iniciada; se recibe token JWT | | | |
| 3.2 | Login incorrecto | Contraseña errónea | 401 "Contraseña incorrecta. Intentos restantes: 2." | | | |
| 3.3 | Correo inexistente | Correo no registrado | 401 "Correo o contraseña incorrectos." | | | |
| 3.4 | Bloqueo de cuenta | 3 contraseñas incorrectas seguidas (usar el cliente) | Cuenta bloqueada; el admin puede desbloquearla | | | |
| 3.5 | Contraseña con hash | `SELECT password FROM usuarios;` | Valores BCrypt (`$2a$...`), nunca texto plano | | | |
| 3.6 | Persistencia al recargar | Iniciar sesión y presionar F5 | Sigue con la sesión iniciada | | | |
| 3.7 | Cierre de sesión | Pulsar "Cerrar sesión" | Vuelve al login; `sessionStorage` sin token; F5 no restaura la sesión | | | |
| 3.8 | Expiración del token | Arrancar con `JWT_EXPIRATION_MS=60000`, esperar 1 minuto y usar la app | 401 y la sesión se cierra | | | |
| 3.9 | Sesión máxima | Arrancar con `JWT_MAX_SESSION_MS=60000`, esperar 1 minuto y recargar | `POST /auth/renovar` responde 401 y se pide iniciar sesión | | | |
| 3.10 | Token alterado | Cambiar un carácter del token y llamar a `GET /compras/mis-compras` | 401 | | | |

## 4. Roles y permisos

| N° | Prueba | Pasos | Resultado esperado | Resultado obtenido | ✅/❌ | Evidencia |
|---|---|---|---|---|---|---|
| 4.1 | Sin token | `GET /compras/mis-compras` sin `Authorization` | 401 | | | |
| 4.2 | Cliente en endpoint de admin | Token de cliente en `GET /usuarios` | 403 | | | |
| 4.3 | Vendedor en endpoint de admin | Token de vendedor en `POST /categorias` | 403 | | | |
| 4.4 | Cliente en `/reportes/resumen` | Token de cliente | 403 | | | |
| 4.5 | Cliente en `PATCH /compras/{id}/estado` | Token de cliente | 403 | | | |
| 4.6 | URL escrita a mano (cliente) | Con sesión de cliente, escribir `/gestion/usuarios` y `/gestion/reportes` | "Acceso denegado" | | | |
| 4.7 | URL escrita a mano (sin sesión) | Sin sesión, escribir `/historial` | Redirige al login | | | |
| 4.8 | Menú por rol | Iniciar sesión con cada rol | Cada uno ve solo las opciones de su rol | | | |

## 5. Compras, estados e historial

| N° | Prueba | Pasos | Resultado esperado | Resultado obtenido | ✅/❌ | Evidencia |
|---|---|---|---|---|---|---|
| 5.1 | Registrar una compra | Cliente agrega productos y finaliza la compra | Compra creada en estado **Pendiente**; stock descontado | | | |
| 5.2 | Aparece en el historial | Ir a `/historial` | Aparece con N°, fecha, estado, total y detalle | | | |
| 5.3 | Historial en otro dispositivo | Cerrar sesión e iniciar de nuevo (o usar otro navegador) | El historial se mantiene | | | |
| 5.4 | Stock insuficiente | Comprar más unidades que el stock | 409 con mensaje claro; no se crea la compra | | | |
| 5.5 | Aislamiento del historial | Con el token del cliente, llamar a `GET /compras` | 403; `/compras/mis-compras` solo devuelve sus compras | | | |
| 5.6 | Cambiar estado | Admin pasa la compra a "En preparación" en `/gestion/compras` | Mensaje de éxito; el cliente ve el nuevo estado al recargar su historial | | | |
| 5.7 | Transición no permitida | `PATCH /compras/{id}/estado` con `ENTREGADA` sobre una compra `PENDIENTE` | 409 | | | |
| 5.8 | Cancelar devuelve stock | Admin cancela una compra pendiente | Estado "Cancelada"; el stock de sus productos aumenta | | | |
| 5.9 | Estado final | Intentar cambiar una compra cancelada o entregada | 409 / la interfaz no ofrece cambios | | | |

## 6. Reportes

| N° | Prueba | Pasos | Resultado esperado | Resultado obtenido | ✅/❌ | Evidencia |
|---|---|---|---|---|---|---|
| 6.1 | Dashboard carga | Admin abre `/gestion/reportes` | Tarjetas, gráfico, más vendidos y estados | | | |
| 6.2 | Cálculo real | Comparar con `SELECT COUNT(*), SUM(total) FROM compras WHERE estado <> 'CANCELADA';` (periodo "Todo") | Las cifras coinciden | | | |
| 6.3 | Periodos | Cambiar entre Semana, Mes, Año y Todo | Los indicadores cambian según el periodo | | | |
| 6.4 | Se actualiza con nuevas compras | Anotar el total, hacer una compra como cliente y pulsar "Actualizar" | Operaciones y monto aumentan | | | |
| 6.5 | Cancelación | Cancelar una compra y actualizar el dashboard | Operaciones y monto bajan; sube "canceladas" | | | |
| 6.6 | Periodo inválido | `GET /reportes/resumen?periodo=xyz` (admin) | 400 | | | |

## 7. Seguridad del repositorio

| N° | Prueba | Pasos | Resultado esperado | Resultado obtenido | ✅/❌ | Evidencia |
|---|---|---|---|---|---|---|
| 7.1 | Sin secretos en GitHub | `git ls-files \| grep secrets.properties` y revisar `application.properties` | `secrets.properties` no está versionado; no hay claves ni contraseñas | | | |
| 7.2 | Arranque sin clave JWT | Ejecutar sin `JWT_SECRET` | La aplicación no arranca | | | |

## Conclusiones

_Resumen de los resultados, errores encontrados y cómo se corrigieron._
