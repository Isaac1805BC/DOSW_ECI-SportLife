# SportLife MVP — ECI-SportLife

**Empresa:** DOSW Company  
**Proyecto:** SportLife — Tienda Virtual de Productos Deportivos  
**Entrega:** Pre Parcial Corte #2  
**Autor:** Isaac Burgos  

---

## Descripción General

SportLife es una aplicación web que expone servicios REST para la comercialización de productos deportivos (ropa, accesorios, implementos de entrenamiento, entre otros). El objetivo del MVP es cubrir el flujo completo desde el registro de un usuario hasta el pago de su compra, garantizando una experiencia básica pero funcional.

---

## Tabla de Contenidos

1. [Matriz de Trazabilidad](#1-matriz-de-trazabilidad)
2. [Especificación de Funcionalidades (Endpoints)](#2-especificación-de-funcionalidades-endpoints)
3. [Diagrama de Componentes General](#3-diagrama-de-componentes-general)
4. [Diagrama de Componentes Específico](#4-diagrama-de-componentes-específico)
5. [Diagrama de Clases](#5-diagrama-de-clases)
6. [Diagrama de Base de Datos](#6-diagrama-de-base-de-datos)
7. [Seguridad](#7-seguridad)
8. [Roles y Permisos](#8-roles-y-permisos)
9. [TLS/SSL en API REST](#9-tlsssl-en-api-rest)
10. [CORS en API REST](#10-cors-en-api-rest)
11. [Diseño de Pantallas (Figma)](#11-diseño-de-pantallas-figma)
12. [Parte Práctica](#12-parte-práctica)

---

## 1. Matriz de Trazabilidad

| Código | Nombre Requerimiento | Historia de Usuario | Tareas Principales |
|--------|----------------------|---------------------|--------------------|
| **REQ-001** | Registro de Usuario | Como usuario nuevo, quiero crear una cuenta con mi nombre, correo electrónico y contraseña, para poder acceder a la plataforma SportLife. | - Crear endpoint `POST /api/users/register` - Validar campos obligatorios (nombre, email, contraseña) - Validar formato de email único - Encriptar contraseña - Persistir usuario en BD - Retornar confirmación de registro |
| **REQ-002** | Autenticación de Usuario | Como usuario registrado, quiero iniciar sesión con mi correo y contraseña, para acceder a las funcionalidades de la plataforma. | - Crear endpoint `POST /api/auth/login` - Validar credenciales contra BD - Generar token JWT - Retornar token al cliente - Manejar error de credenciales inválidas |
| **REQ-003** | Listar Productos | Como usuario autenticado, quiero ver el catálogo de productos disponibles con opciones de filtro por categoría y búsqueda por nombre, para encontrar fácilmente lo que necesito. | - Crear endpoint `GET /api/products` - Implementar filtro por categoría (query param) - Implementar búsqueda por nombre (query param) - Retornar solo productos ACTIVOS - Retornar lista paginada con: id, nombre, precio, categoría, stock, imágenes |
| **REQ-004** | Ver Detalle de Producto | Como usuario autenticado, quiero ver la información completa de un producto específico, para conocer su precio, descripción, disponibilidad y características antes de comprarlo. | - Crear endpoint `GET /api/products/{id}` - Validar que el producto existe - Retornar: id, nombre, descripción, categoría, precio, stock, imágenes, estado - Manejar error 404 si no existe |
| **REQ-005** | Agregar Producto al Carrito | Como usuario autenticado, quiero agregar productos a mi carrito de compras definiendo la cantidad deseada, para preparar mi pedido. | - Crear endpoint `POST /api/cart/items` - Validar que el producto existe y está activo - Validar stock disponible al momento de agregar - Asociar ítem al carrito del usuario autenticado - Actualizar cantidad si el producto ya está en el carrito - Retornar carrito actualizado |
| **REQ-006** | Ver Resumen del Carrito | Como usuario autenticado, quiero ver el resumen de mi carrito con la lista de productos, cantidades, subtotales y total, para revisar mi pedido antes de pagar. | - Crear endpoint `GET /api/cart` - Obtener carrito activo del usuario autenticado - Calcular subtotal por ítem (precio × cantidad) - Calcular total general - Retornar: lista de productos, cantidades, subtotales, total |
| **REQ-007** | Procesar Pago / Generar Orden | Como usuario autenticado, quiero iniciar el proceso de pago de mi carrito, para que el sistema valide el contenido, calcule el total y genere una orden de compra. | - Crear endpoint `POST /api/orders/checkout` - Validar que el carrito no esté vacío - Validar stock de todos los productos - Calcular total a pagar - Generar orden con estado PENDING - Invocar pasarela de pago - Retornar resultado |
| **REQ-008** | Confirmación de Pago Aprobado | Como usuario autenticado, quiero recibir una confirmación cuando mi pago sea aprobado, para saber que mi compra fue exitosa y tener el resumen de la transacción. | - Actualizar estado de la orden a PAID - Descontar stock de cada producto comprado - Generar ID único de transacción - Retornar confirmación con resumen (productos, cantidades, total, ID transacción) |
| **REQ-009** | Manejo de Pago Rechazado | Como usuario autenticado, quiero ser notificado cuando mi pago sea rechazado, para conocer el fallo y poder reintentar el proceso de pago. | - Actualizar estado de la orden a REJECTED - NO modificar el stock - Notificar al usuario el motivo del rechazo - Permitir reintento del pago - Mantener el carrito intacto |

### Dependencias entre Requerimientos

```
REQ-001 (Registro)
    └── REQ-002 (Login) ← BLOQUEA a todos los siguientes
            ├── REQ-003 (Listar Productos)
            │       └── REQ-004 (Ver Detalle)
            │               └── REQ-005 (Agregar al Carrito)
            │                       └── REQ-006 (Ver Resumen)
            │                               └── REQ-007 (Procesar Pago)
            │                                       ├── REQ-008 (Pago Aprobado)
            │                                       └── REQ-009 (Pago Rechazado)
```

> **REQ-001** y **REQ-002** son los requerimientos base. Sin registro y autenticación, ninguna otra funcionalidad es accesible.

---

## 2. Especificación de Funcionalidades (Endpoints)

> _Pendiente de desarrollo — se completará en la parte teórica del parcial._

---

## 3. Diagrama de Componentes General

> _Pendiente de desarrollo._

---

## 4. Diagrama de Componentes Específico

> _Pendiente de desarrollo._

---

## 5. Diagrama de Clases

> _Pendiente de desarrollo._

---

## 6. Diagrama de Base de Datos

### 6a. Modelo Relacional
> _Pendiente de desarrollo._

### 6b. Modelo No Relacional
> _Pendiente de desarrollo._

---

## 7. Seguridad

> _Pendiente de desarrollo._

---

## 8. Roles y Permisos

> _Pendiente de desarrollo._

---

## 9. TLS/SSL en API REST

> _Pendiente de desarrollo._

---

## 10. CORS en API REST

> _Pendiente de desarrollo._

---

## 11. Diseño de Pantallas (Figma)

> _Pendiente de desarrollo._

---

## 12. Parte Práctica

### Tecnologías utilizadas

| Tecnología | Uso |
|------------|-----|
| Java + Spring Boot | Framework principal del backend |
| Maven | Gestión de dependencias y build |
| Spring Data JPA | Persistencia relacional |
| Spring Data MongoDB | Persistencia no relacional |
| Spring Security + JWT | Autenticación y autorización |
| Lombok | Reducción de código boilerplate |
| Mockito + JUnit | Pruebas unitarias |
| Swagger / OpenAPI | Documentación de la API |
| SonarQube | Análisis de calidad de código |
| GitHub Actions | CI/CD Pipeline |
| Azure | Despliegue en producción |

### Estructura del Proyecto (MVC)

```
ECI-SportLife/
├── src/
│   ├── main/
│   │   ├── java/com/dosw/sportlife/
│   │   │   ├── controller/
│   │   │   ├── service/
│   │   │   ├── repository/
│   │   │   ├── model/
│   │   │   ├── dto/
│   │   │   ├── config/
│   │   │   └── exception/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

### Pipeline CI/CD (GitHub Actions)

- Se ejecuta al hacer merge de `feature/*` → `develop`
- Se ejecuta al hacer merge de `develop` → `master`
- Etapas: **build → test → analysis (SonarQube) → deploy (Azure)**

### URL de Producción

> _Pendiente de despliegue._

### Video de Pruebas Funcionales

> _Pendiente de grabación._

---

## Tiempo de realización del parcial teórico

| Actividad | Tiempo |
|-----------|--------|
| Matriz de Trazabilidad | — |
| Especificación de Endpoints | — |
| Diagramas (Componentes, Clases, BD) | — |
| Seguridad, Roles, TLS, CORS | — |
| Diseño Figma | — |
| **Total** | — |
