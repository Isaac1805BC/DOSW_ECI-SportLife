# ECI-SportLife

## MVP SportLife — Tienda Virtual de Productos Deportivos

SportLife es una aplicación web REST API desarrollada con Spring Boot que gestiona usuarios, productos, carrito de compras y pagos para una tienda virtual de artículos deportivos.

---

# PARTE TEORICA

## 1. Matriz de Trazabilidad de Funcionalidades

| ID  | Funcionalidad                  | Prioridad | Tipo          | Depende de    | Bloquea         |
|-----|-------------------------------|-----------|---------------|---------------|-----------------|
| F1  | Registrar Usuario              | Alta      | Independiente | —             | F2              |
| F2  | Autenticar Usuario (Login)     | Alta      | Bloqueante    | F1            | F6, F7, F8      |
| F3  | Listar Catalogo de Productos   | Alta      | Independiente | —             | F4, F5          |
| F4  | Filtrar/Buscar Productos       | Media     | Derivada      | F3            | —               |
| F5  | Ver Detalle de Producto        | Alta      | Derivada      | F3            | F6              |
| F6  | Agregar Producto al Carrito    | Alta      | Bloqueada     | F2, F5        | F7              |
| F7  | Ver Resumen del Carrito        | Alta      | Derivada      | F6            | F8              |
| F8  | Iniciar Pago / Procesar Orden  | Alta      | Bloqueada     | F7            | —               |

**Analisis de prioridad:**
- F1 y F2 son la base de todo el sistema; sin registro y autenticacion no existe flujo de compra.
- F3 es el punto de entrada para los productos y no requiere autenticacion, lo que lo hace independiente y de alta prioridad para la experiencia de usuario.
- F6, F7 y F8 conforman el flujo principal de compra y deben implementarse en orden estricto.
- F4 es la unica funcionalidad de prioridad media porque es una mejora de busqueda sobre F3, que ya retorna todos los productos.

---

## 2. Definicion Detallada de Funcionalidades

---

### F1 — Registrar Usuario

| Propiedad         | Valor                                    |
|-------------------|------------------------------------------|
| **Verbo HTTP**    | `POST`                                   |
| **Endpoint**      | `/api/auth/register`                     |
| **Idempotente**   | No                                       |
| **Razon tecnica** | Cada llamada crea una nueva entidad en la base de datos. Dos llamadas con los mismos datos produciran un error por correo duplicado o crearan duplicados, por lo tanto el estado del servidor cambia con cada invocacion. |

#### Datos de Entrada

| Campo      | Tipo     | Obligatorio | Descripcion                                          |
|------------|----------|-------------|------------------------------------------------------|
| `name`     | `String` | Si          | Nombre completo del usuario                          |
| `email`    | `String` | Si          | Correo electronico unico                             |
| `password` | `String` | Si          | Contrasena en texto plano (se hashea internamente)   |

#### Datos de Salida

| Campo     | Tipo     | Descripcion                      |
|-----------|----------|----------------------------------|
| `message` | `String` | Confirmacion de registro         |
| `userId`  | `String` | Identificador del nuevo usuario  |

#### Ejemplo de Entrada

```json
{
  "name": "Isaac Burgos",
  "email": "isaac@correo.com",
  "password": "Abc123!@#"
}
```

#### Ejemplo de Salida

```json
{
  "message": "Usuario registrado exitosamente",
  "userId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

#### Validaciones

**Input:**
- `name`: no puede estar vacio, minimo 2 caracteres, maximo 100.
- `email`: formato valido (regex), no puede estar vacio.
- `password`: minimo 8 caracteres, al menos una mayuscula, un numero y un caracter especial.

**Negocio:**
- El correo electronico debe ser unico en el sistema; si ya existe retornar error.

#### Codigos HTTP

| Escenario              | Codigo | Mensaje                                      |
|------------------------|--------|----------------------------------------------|
| Registro exitoso       | `201`  | `"Usuario registrado exitosamente"`          |
| Correo ya registrado   | `409`  | `"El correo electronico ya esta en uso"`     |
| Datos invalidos        | `400`  | `"Datos de entrada invalidos: [detalle]"`    |
| Error interno          | `500`  | `"Error interno del servidor"`               |

---

### F2 — Autenticar Usuario (Login)

| Propiedad         | Valor                                    |
|-------------------|------------------------------------------|
| **Verbo HTTP**    | `POST`                                   |
| **Endpoint**      | `/api/auth/login`                        |
| **Idempotente**   | No                                       |
| **Razon tecnica** | Aunque no modifica la entidad `User` en si, cada invocacion genera un nuevo token JWT con una fecha de expiracion distinta (`iat`, `exp`), lo que significa que el estado del sistema cambia con cada llamada. Ademas puede registrar un evento de login en auditoria. |

#### Datos de Entrada

| Campo      | Tipo     | Obligatorio | Descripcion               |
|------------|----------|-------------|---------------------------|
| `email`    | `String` | Si          | Correo del usuario        |
| `password` | `String` | Si          | Contrasena del usuario    |

#### Datos de Salida

| Campo   | Tipo     | Descripcion                  |
|---------|----------|------------------------------|
| `token` | `String` | JWT para peticiones futuras  |
| `type`  | `String` | Tipo de token (`Bearer`)     |

#### Ejemplo de Entrada

```json
{
  "email": "isaac@correo.com",
  "password": "Abc123!@#"
}
```

#### Ejemplo de Salida

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer"
}
```

#### Validaciones

**Input:**
- `email`: formato valido, no vacio.
- `password`: no vacio.

**Negocio:**
- El correo debe existir en el sistema.
- La contrasena debe coincidir con el hash almacenado (BCrypt).
- No revelar si el error es por correo o contrasena (mensaje generico por seguridad).

#### Codigos HTTP

| Escenario               | Codigo | Mensaje                            |
|-------------------------|--------|------------------------------------|
| Login exitoso           | `200`  | — (retorna token)                  |
| Credenciales invalidas  | `401`  | `"Credenciales incorrectas"`       |
| Datos invalidos         | `400`  | `"Datos de entrada invalidos"`     |
| Error interno           | `500`  | `"Error interno del servidor"`     |

---

### F3 — Listar Catalogo de Productos

| Propiedad         | Valor                                    |
|-------------------|------------------------------------------|
| **Verbo HTTP**    | `GET`                                    |
| **Endpoint**      | `/api/products`                          |
| **Idempotente**   | Si                                       |
| **Razon tecnica** | Es una operacion de solo lectura. Puede ejecutarse N veces sin modificar el estado del servidor. El mismo request siempre produce el mismo conjunto de datos en el mismo instante de tiempo. GET es inherentemente idempotente por especificacion HTTP (RFC 7231). |

#### Datos de Entrada (Query Params opcionales)

| Parametro    | Tipo     | Obligatorio | Descripcion                         |
|--------------|----------|-------------|-------------------------------------|
| `category`   | `String` | No          | Filtrar por categoria               |
| `name`       | `String` | No          | Buscar por nombre (like)            |
| `page`       | `int`    | No          | Numero de pagina (default: 0)       |
| `size`       | `int`    | No          | Elementos por pagina (default: 10)  |

#### Datos de Salida

| Campo         | Tipo               | Descripcion               |
|---------------|--------------------|---------------------------|
| `products`    | `Array<Product>`   | Lista de productos activos |
| `totalItems`  | `long`             | Total de productos        |
| `totalPages`  | `int`              | Total de paginas          |
| `currentPage` | `int`              | Pagina actual             |

#### Ejemplo de Salida

```json
{
  "products": [
    {
      "id": "prod-001",
      "name": "Tenis Running Pro",
      "description": "Tenis ligeros para running urbano",
      "category": "running",
      "price": 250000,
      "stock": 15,
      "images": ["url1.jpg", "url2.jpg"],
      "status": "ACTIVE"
    }
  ],
  "totalItems": 1,
  "totalPages": 1,
  "currentPage": 0
}
```

#### Validaciones

**Input:**
- `page` y `size` deben ser enteros positivos.
- `category` debe ser un valor permitido si se valida contra un enum.

**Negocio:**
- Solo retornar productos con estado `ACTIVE`.
- No requiere autenticacion.

#### Codigos HTTP

| Escenario         | Codigo | Mensaje                         |
|-------------------|--------|---------------------------------|
| Listado exitoso   | `200`  | — (retorna lista)               |
| Error interno     | `500`  | `"Error interno del servidor"`  |

---

### F4 — Filtrar / Buscar Productos

| Propiedad         | Valor                                         |
|-------------------|-----------------------------------------------|
| **Verbo HTTP**    | `GET`                                         |
| **Endpoint**      | `/api/products?category=running&name=tenis`   |
| **Idempotente**   | Si                                            |
| **Razon tecnica** | Es una lectura parametrizada, no modifica estado. Reutiliza el mismo endpoint de F3 con parametros de filtrado. |

Esta funcionalidad se implementa como extension de F3 mediante query params opcionales. Ver F3 para datos de entrada, salida, validaciones y codigos HTTP.

---

### F5 — Ver Detalle de Producto

| Propiedad         | Valor                                    |
|-------------------|------------------------------------------|
| **Verbo HTTP**    | `GET`                                    |
| **Endpoint**      | `/api/products/{id}`                     |
| **Idempotente**   | Si                                       |
| **Razon tecnica** | Lectura de un recurso por su identificador. Repetir la misma peticion siempre retorna el mismo producto mientras no sea modificado. |

#### Datos de Entrada

| Campo | Tipo     | Obligatorio | Descripcion                |
|-------|----------|-------------|----------------------------|
| `id`  | `String` | Si (path)   | Identificador del producto |

#### Datos de Salida

| Campo         | Tipo            | Descripcion               |
|---------------|-----------------|---------------------------|
| `id`          | `String`        | Identificador unico       |
| `name`        | `String`        | Nombre del producto       |
| `description` | `String`        | Descripcion completa      |
| `category`    | `String`        | Categoria                 |
| `price`       | `BigDecimal`    | Precio                    |
| `stock`       | `int`           | Unidades disponibles      |
| `images`      | `List<String>`  | URLs de imagenes          |
| `status`      | `String`        | `ACTIVE` / `INACTIVE`     |

#### Ejemplo de Salida

```json
{
  "id": "prod-001",
  "name": "Tenis Running Pro",
  "description": "Tenis ligeros para running urbano, suela de caucho antideslizante",
  "category": "running",
  "price": 250000.00,
  "stock": 15,
  "images": ["https://cdn.sportlife.com/prod-001-a.jpg"],
  "status": "ACTIVE"
}
```

#### Validaciones

**Input:**
- `id` no puede estar vacio.

**Negocio:**
- Si el producto no existe, retornar 404.
- Si el producto esta inactivo, retornar 404 (no visible para clientes).

#### Codigos HTTP

| Escenario                 | Codigo | Mensaje                              |
|---------------------------|--------|--------------------------------------|
| Producto encontrado       | `200`  | — (retorna producto)                 |
| Producto no encontrado    | `404`  | `"Producto no encontrado"`           |
| ID con formato invalido   | `400`  | `"ID de producto invalido"`          |

---

### F6 — Agregar Producto al Carrito

| Propiedad         | Valor                                    |
|-------------------|------------------------------------------|
| **Verbo HTTP**    | `POST`                                   |
| **Endpoint**      | `/api/cart/items`                        |
| **Idempotente**   | No                                       |
| **Razon tecnica** | Cada llamada puede incrementar la cantidad del producto en el carrito o anadir un nuevo item. El estado del carrito cambia con cada invocacion, por lo que no es seguro repetirla sin consecuencias. |

#### Datos de Entrada

| Campo       | Tipo     | Obligatorio | Descripcion                        |
|-------------|----------|-------------|------------------------------------|
| `productId` | `String` | Si          | ID del producto a agregar          |
| `quantity`  | `int`    | Si          | Cantidad a agregar (minimo 1)      |

> Requiere header: `Authorization: Bearer <token>`

#### Datos de Salida

| Campo     | Tipo     | Descripcion                         |
|-----------|----------|-------------------------------------|
| `message` | `String` | Confirmacion                        |
| `cartId`  | `String` | ID del carrito actualizado          |
| `items`   | `Array`  | Items actuales del carrito          |

#### Ejemplo de Entrada

```json
{
  "productId": "prod-001",
  "quantity": 2
}
```

#### Ejemplo de Salida

```json
{
  "message": "Producto agregado al carrito",
  "cartId": "cart-xyz-789",
  "items": [
    {
      "productId": "prod-001",
      "name": "Tenis Running Pro",
      "quantity": 2,
      "unitPrice": 250000.00,
      "subtotal": 500000.00
    }
  ]
}
```

#### Validaciones

**Input:**
- `productId` no puede estar vacio.
- `quantity` debe ser un entero mayor o igual a 1.

**Negocio:**
- El producto debe existir y estar activo.
- El stock disponible debe ser mayor o igual a la cantidad solicitada.
- Si el producto ya esta en el carrito, sumar la cantidad y validar stock total.
- El usuario debe estar autenticado.

#### Codigos HTTP

| Escenario                   | Codigo | Mensaje                                             |
|-----------------------------|--------|-----------------------------------------------------|
| Producto agregado           | `200`  | `"Producto agregado al carrito"`                    |
| Stock insuficiente          | `409`  | `"Stock insuficiente para la cantidad solicitada"`  |
| Producto no encontrado      | `404`  | `"Producto no encontrado"`                          |
| No autenticado              | `401`  | `"No autorizado"`                                   |
| Datos invalidos             | `400`  | `"Datos de entrada invalidos"`                      |

---

### F7 — Ver Resumen del Carrito

| Propiedad         | Valor                                    |
|-------------------|------------------------------------------|
| **Verbo HTTP**    | `GET`                                    |
| **Endpoint**      | `/api/cart`                              |
| **Idempotente**   | Si                                       |
| **Razon tecnica** | Es una operacion de solo lectura del estado actual del carrito del usuario autenticado. No modifica datos. |

#### Datos de Entrada

> Solo requiere header: `Authorization: Bearer <token>`

#### Datos de Salida

| Campo     | Tipo               | Descripcion                        |
|-----------|--------------------|------------------------------------|
| `cartId`  | `String`           | Identificador del carrito          |
| `items`   | `Array<CartItem>`  | Lista de productos en el carrito   |
| `total`   | `BigDecimal`       | Total de la compra                 |

#### Ejemplo de Salida

```json
{
  "cartId": "cart-xyz-789",
  "items": [
    {
      "productId": "prod-001",
      "name": "Tenis Running Pro",
      "quantity": 2,
      "unitPrice": 250000.00,
      "subtotal": 500000.00
    },
    {
      "productId": "prod-002",
      "name": "Camiseta Gym Dry-Fit",
      "quantity": 1,
      "unitPrice": 85000.00,
      "subtotal": 85000.00
    }
  ],
  "total": 585000.00
}
```

#### Validaciones

**Negocio:**
- El usuario debe estar autenticado.
- Si el carrito esta vacio, retornar lista vacia con total 0.

#### Codigos HTTP

| Escenario            | Codigo | Mensaje                         |
|----------------------|--------|---------------------------------|
| Carrito obtenido     | `200`  | — (retorna resumen)             |
| No autenticado       | `401`  | `"No autorizado"`               |
| Error interno        | `500`  | `"Error interno del servidor"`  |

---

### F8 — Iniciar Pago / Procesar Orden

| Propiedad         | Valor                                    |
|-------------------|------------------------------------------|
| **Verbo HTTP**    | `POST`                                   |
| **Endpoint**      | `/api/orders/checkout`                   |
| **Idempotente**   | No                                       |
| **Razon tecnica** | Cada llamada crea una nueva orden y, si el pago es aprobado, descuenta el stock de los productos y genera un ID de transaccion unico. El estado del sistema cambia irreversiblemente con cada invocacion exitosa. |

#### Datos de Entrada

| Campo           | Tipo     | Obligatorio | Descripcion                           |
|-----------------|----------|-------------|---------------------------------------|
| `paymentMethod` | `String` | Si          | Metodo de pago (`CARD`, `PSE`, etc.)  |
| `cardNumber`    | `String` | Cond.       | Ultimos 4 digitos (si es tarjeta)     |

> Requiere header: `Authorization: Bearer <token>`

#### Datos de Salida

| Campo           | Tipo         | Descripcion                              |
|-----------------|--------------|------------------------------------------|
| `orderId`       | `String`     | ID de la orden creada                    |
| `status`        | `String`     | `PAID` o `REJECTED`                      |
| `transactionId` | `String`     | ID unico de la transaccion (si pagado)   |
| `total`         | `BigDecimal` | Total pagado                             |
| `message`       | `String`     | Mensaje al usuario                       |
| `items`         | `Array`      | Resumen de productos comprados           |

#### Ejemplo de Salida — Pago Aprobado

```json
{
  "orderId": "order-abc-123",
  "status": "PAID",
  "transactionId": "txn-20260410-xyz",
  "total": 585000.00,
  "message": "Pago procesado exitosamente. Gracias por tu compra.",
  "items": [
    { "productId": "prod-001", "name": "Tenis Running Pro", "quantity": 2, "subtotal": 500000.00 },
    { "productId": "prod-002", "name": "Camiseta Gym Dry-Fit", "quantity": 1, "subtotal": 85000.00 }
  ]
}
```

#### Ejemplo de Salida — Pago Rechazado

```json
{
  "orderId": "order-abc-124",
  "status": "REJECTED",
  "transactionId": null,
  "total": 585000.00,
  "message": "Pago rechazado. Por favor verifique sus datos e intente nuevamente."
}
```

#### Validaciones

**Input:**
- `paymentMethod` no puede estar vacio y debe ser un valor permitido.

**Negocio:**
- El carrito no puede estar vacio al momento del pago.
- Verificar nuevamente el stock de todos los productos en el carrito antes de procesar (el stock puede haber cambiado desde que se agrego).
- Si el pago es aprobado: descontar stock, marcar orden como `PAID`, vaciar el carrito.
- Si el pago es rechazado: no modificar stock, marcar orden como `REJECTED`, permitir reintento.
- El usuario debe estar autenticado.

#### Codigos HTTP

| Escenario                   | Codigo | Mensaje                                         |
|-----------------------------|--------|-------------------------------------------------|
| Pago aprobado               | `201`  | `"Pago procesado exitosamente"`                 |
| Pago rechazado              | `402`  | `"Pago rechazado. Intente nuevamente"`          |
| Carrito vacio               | `400`  | `"El carrito esta vacio"`                       |
| Stock insuficiente          | `409`  | `"Stock insuficiente al momento del pago"`      |
| No autenticado              | `401`  | `"No autorizado"`                               |
| Error interno               | `500`  | `"Error interno del servidor"`                  |

---

## 7. Seguridad en SportLife

### Tipo de Seguridad: JWT + Spring Security + BCrypt

**Implementacion:**
- **BCrypt** para hashear contrasenas antes de almacenarlas. Nunca se guarda la contrasena en texto plano.
- **JWT (JSON Web Token)** para autenticacion stateless. El cliente recibe un token firmado al hacer login y lo envia en el header `Authorization: Bearer <token>` en cada peticion protegida.
- **Spring Security** como framework de seguridad que intercepta todas las peticiones, valida el token JWT en un filtro (`JwtFilter`) y establece el contexto de seguridad.

**Ventajas:**

| Ventaja                       | Descripcion                                                                                                                                    |
|-------------------------------|------------------------------------------------------------------------------------------------------------------------------------------------|
| Stateless                     | El servidor no almacena sesiones. El token contiene toda la informacion necesaria (userId, rol). Escala horizontalmente sin sesiones compartidas. |
| Estandar abierto              | JWT (RFC 7519) es compatible con cualquier cliente: web, movil, microservicios.                                                                |
| Seguridad de contrasenas      | BCrypt aplica salt automaticamente y es resistente a ataques de fuerza bruta y rainbow tables.                                                 |
| Control de acceso             | Spring Security permite proteger endpoints por rol con `@PreAuthorize` de forma declarativa.                                                   |
| Expiracion de tokens          | Los JWT tienen tiempo de expiracion (`exp`), limitando la ventana de ataque si un token es robado.                                             |

---

## 8. Roles e Identificacion de Permisos

### Roles Identificados

| Rol     | Descripcion                                              |
|---------|----------------------------------------------------------|
| `USER`  | Cliente registrado que puede comprar productos           |
| `ADMIN` | Administrador que gestiona el catalogo y el inventario   |

### Matriz de Permisos por Funcionalidad

| Funcionalidad                   | Sin Autenticar | USER | ADMIN |
|---------------------------------|:--------------:|:----:|:-----:|
| Registrar usuario               | Si             | Si   | Si    |
| Autenticar usuario              | Si             | Si   | Si    |
| Listar catalogo de productos    | Si             | Si   | Si    |
| Filtrar/Buscar productos        | Si             | Si   | Si    |
| Ver detalle de producto         | Si             | Si   | Si    |
| Agregar producto al carrito     | No             | Si   | No    |
| Ver resumen del carrito         | No             | Si   | No    |
| Iniciar pago / Procesar orden   | No             | Si   | No    |
| Crear producto (CRUD admin)     | No             | No   | Si    |
| Editar producto (CRUD admin)    | No             | No   | Si    |
| Desactivar producto             | No             | No   | Si    |
| Ver todas las ordenes           | No             | No   | Si    |

---

## 9. TLS/SSL en una API REST

### Como se implementa

TLS puede implementarse en dos niveles:

**Nivel de infraestructura (recomendado para produccion):**
El certificado SSL se instala en el API Gateway (Azure Application Gateway o Nginx). El trafico entre el cliente y el gateway viaja cifrado via HTTPS. Internamente, la comunicacion puede ser HTTP simple.

```
Cliente -- HTTPS --> Azure App Gateway (TLS Termination) -- HTTP --> Spring Boot API
```

**Nivel de aplicacion (Spring Boot):**
```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=secreto
server.ssl.key-store-type=PKCS12
server.port=8443
```

### Ventajas para SportLife

| Ventaja                   | Descripcion                                                                                                                                                  |
|---------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Cifrado en transito       | Los datos (credenciales, datos de pago, tokens JWT) viajan cifrados. Un atacante que intercepte el trafico solo vera datos ilegibles.                         |
| Autenticidad del servidor | El certificado SSL garantiza al cliente que esta hablando con el servidor legitimo de SportLife, previniendo ataques de Man-in-the-Middle.                    |
| Integridad de los datos   | TLS garantiza que los datos no fueron modificados durante el transito.                                                                                       |
| Confianza del usuario     | El candado verde en el navegador aumenta la confianza del usuario para ingresar datos de pago.                                                               |
| Cumplimiento normativo    | Normativas como PCI-DSS para pagos exigen comunicaciones cifradas para proteger datos sensibles.                                                             |

---

## 10. Importancia de CORS en una API REST

CORS (Cross-Origin Resource Sharing) es un mecanismo de seguridad implementado por los navegadores web que restringe las peticiones HTTP realizadas desde un origen (dominio + puerto) hacia un origen diferente.

### Por que es importante para SportLife

La API corre en `https://api.sportlife.com` mientras que la aplicacion web del cliente puede estar en `https://app.sportlife.com`. Sin CORS configurado, el navegador bloqueara todas las peticiones desde el frontend hacia la API.

### Configuracion en Spring Boot

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://app.sportlife.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

### Razones para configurar CORS correctamente

| Razon                          | Explicacion                                                                                                                                                          |
|-------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Seguridad                     | Limitar los origenes permitidos evita que sitios maliciosos hagan peticiones a la API en nombre de usuarios autenticados (ataques CSRF).                              |
| Control de acceso granular    | Se puede definir exactamente que metodos HTTP, cabeceras y origenes tienen acceso, reduciendo la superficie de ataque.                                               |
| Compatibilidad con navegadores| Sin CORS la aplicacion web no funciona en navegadores modernos, que bloquean peticiones cross-origin por defecto.                                                    |
| Separacion de dominios        | Permite mantener la API y el frontend como servicios independientes desplegados en diferentes dominios sin romper la comunicacion.                                   |

---

## 11. Diseno de Pantallas — Flujo de Compra

El diseno de las pantallas para el flujo de compra de SportLife esta disponible en Figma e incluye las siguientes vistas:

**Pantalla 1 — Lista de Productos**

Muestra el catalogo de productos activos con imagen, nombre y precio. Incluye barra de busqueda por nombre y selector de categoria. Cada producto tiene un boton para ver su detalle.

**Pantalla 2 — Detalle de Producto**

Muestra la informacion completa del producto: imagenes, nombre, categoria, precio, stock disponible y descripcion. Incluye selector de cantidad con validacion contra stock y boton para agregar al carrito.

**Pantalla 3 — Carrito de Compras**

Lista los productos agregados con su cantidad, precio unitario y subtotal por producto. Permite modificar cantidades o eliminar items. Muestra el total de la compra y un boton para proceder al pago.

**Pantalla 4 — Pasarela de Pago**

Muestra el resumen de la orden antes de pagar. Incluye seleccion del metodo de pago (tarjeta o PSE), formulario de datos de la tarjeta y boton de pago con el monto total.

**Pantalla 5 — Confirmacion de Compra (Pago Aprobado)**

Muestra mensaje de exito con el ID de la orden, ID de transaccion, lista de productos comprados y total pagado. Incluye boton para regresar al catalogo.

**Pantalla 6 — Pago Rechazado**

Muestra mensaje de error informando que el pago no fue procesado. Ofrece opciones para reintentar el pago o volver al carrito.

---

> **Tiempo de realizacion parte teorica:** 2.5 horas

---

# PARTE PRACTICA

## Estructura del Proyecto (Scaffolding MVC)

```
ECI-SportLife/
src/
  main/
    java/com/dosw/sportlife/
      SportLifeApplication.java
      config/
        SecurityConfig.java
        JwtUtil.java
        JwtFilter.java
      controller/
        AuthController.java
        ProductController.java
        CartController.java
        OrderController.java
      dto/
        request/
          RegisterRequest.java
          LoginRequest.java
          AddToCartRequest.java
        response/
          AuthResponse.java
      exception/
        GlobalExceptionHandler.java
      model/
        User.java
        Product.java
        Cart.java
        CartItem.java
        Order.java
        OrderItem.java
      repository/
        UserRepository.java
        ProductRepository.java
        CartRepository.java
        OrderRepository.java
      service/
        impl/
          AuthServiceImpl.java
          ProductServiceImpl.java
          CartServiceImpl.java
          OrderServiceImpl.java
          UserDetailsServiceImpl.java
    resources/
      application.properties
.github/
  workflows/
    pipeline.yml
pom.xml
```

## Pipeline CI/CD (GitHub Actions)

El pipeline definido en `.github/workflows/pipeline.yml` automatiza build, test, analisis con SonarQube y deploy en Azure.

Se ejecuta en:
- Cada push o merge de rama `feature/**` a `develop`
- Cada push o merge de `develop` a `main`

## Flujo de Ramas Git

```
main <- develop <- feature/registro-usuario
                <- feature/autenticacion
                <- feature/catalogo-productos
                <- feature/carrito-compras
                <- feature/proceso-pago
```
