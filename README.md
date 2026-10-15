# Estudiante: Rimberth Villca Maiza

# 🎬 Sistema para la Reserva de asientos en el Cine — Proyecto Final 

Esta entrega contiene la implementación completa del proyecto final para el curso **Spring Boot Cloud (Spring Boot 4 & Java 21/25)**. 

> [!IMPORTANT]
> **Modo de Entrega:** Este proyecto se entrega directamente en un **archivo comprimido (`.zip` / `.rar`)**. **No se utilizará Git ni control de versiones** para esta entrega. Basta con descomprimir la carpeta en cualquier PC y seguir los pasos indicados a continuación.

El sistema modela una plataforma de reservas de cine distribuida en **microservicios**, aplicando los patrones clave aprendidos durante el curso: **Arquitectura Hexagonal**, **Domain-Driven Design (DDD)**, el patrón de transacciones distribuidas **SAGA** de 2 pasos, **Seguridad OAuth2 / JWT Resource Server** y documentación viva con **OpenAPI / Swagger UI**.

---

## 🏛️ 1. Arquitectura del Sistema

El sistema está dividido en dos microservicios de negocio independientes y un servicio emisor de seguridad:

```text
 ┌────────────────────────────────────────────────────────────────────────┐
 │                      Cliente / Postman / Swagger UI                    │
 └───────────────────────────────────┬────────────────────────────────────┘
                                     │ Bearer JWT
                       ┌─────────────▼─────────────┐
                       │        auth-service       │ (Puerto: 50003)
                       │  POST /oauth2/token → JWT │
                       └─────────────┬─────────────┘
                                     │ Bearer JWT
          ┌──────────────────────────┴──────────────────────────┐
          │                                                     │ Bearer JWT
┌─────────▼──────────────┐   RestClient (@LoadBalanced)   ┌─────▼──────────────────┐
│     movie-service      │ ◄───────────────────────────── │    booking-service     │
│  - Catálogo Películas  │           Llamadas SAGA        │  - Orquestador Reservas│
│  - Funciones / Salas   │                                │  - SAGA: reserva/cancel│
│  Puerto: 51100         │                                │  Puerto: 51200         │
└─────────┬──────────────┘                                └─────────┬──────────────┘
          │                                                         │
   ┌──────▼──────┐                                           ┌──────▼──────┐
   │   moviedb   │                                           │  bookingdb  │
   │  PostgreSQL │                                           │  PostgreSQL │
   └─────────────┘                                           └─────────────┘
```

### Conceptos Clave Implementados:
1. **Arquitectura Hexagonal:** Separación rigurosa de capas (`api/dto`, `application`, `domain`, `infrastructure`, `exception`). El dominio (`domain`) es puro y está libre de dependencias de Spring o JPA.
2. **Entidades con Dominio Rico (DDD):** La lógica de negocio y las invariantes se protegen en la propia entidad (ej. `Screening.reserve()` valida que `availableSeats > 0` arrojando `NoSeatsAvailableException` y `Booking.cancel()` valida el estado).
3. **Flujo SAGA Orquestado:** `booking-service` coordina la transacción usando variables de estado locales (`bookingId`, `seatReserved`), transacciones aisladas por paso y compensaciones *best-effort* con absorción de errores.
4. **Snapshot de Datos:** Al crear una reserva se guarda una copia del título de la película (`movieTitle`), evitando dependencias innecesarias de lectura hacia `movie-service` en consultas posteriores.
5**Estándar RFC 7807:** Todas las respuestas de error utilizan el formato estándar `ProblemDetail`.

---

## 💻 2. Prerrequisitos para Ejecutar

Antes de comenzar, asegúrate de tener instalado en tu máquina:

1. **Herramienta de descompresión:** Utilidad nativa de Windows/Linux/macOS, WinRAR, 7-Zip, etc., para descomprimir el archivo `.zip` del proyecto.
2. **Java JDK 21** o superior instalado y configurado en tu variable `PATH` (comprueba con `java -version`).
3. **Docker Desktop** (o Docker Engine en Linux) corriendo para levantar PostgreSQL.
4. *(Opcional)* Maven instalado. Si no tienes Maven global, ¡no te preocupes! El proyecto incluye el Maven Wrapper (`./mvnw` en Linux/Mac o `mvnw.cmd` en Windows) en todos los módulos.

> [!NOTE]
> **Puertos requeridos libres:** Asegúrate de que los puertos `5432` (Postgres), `50003` (Auth), `51100` (Movie) y `51400` (Booking) no estén ocupados por otras aplicaciones.
En mi caso se tenia problemas con el puerto 51200 para Booking service por lo que se trabajo con en el puerto 51400.
---

## 🚀 3. Paso a Paso para Levantar el Proyecto

Sigue este orden exacto abriendo terminales independientes para cada componente.

### Paso 3.1: Descomprimir el proyecto y ubicarse en la carpeta raíz
1. Haz clic derecho sobre el archivo `.zip` recibido y selecciona **"Extraer aquí"** (o en la carpeta de tu preferencia, ej. en el Escritorio).
2. Abre una terminal (PowerShell, CMD o terminal de Linux/macOS).
3. Navega hacia la carpeta raíz que acabas de descomprimir:
```bash
cd <ruta_de_la_carpeta_descomprimida>
```
*(Verifica que al hacer `dir` o `ls` veas las carpetas `auth-service`, `movie-service`, `booking-service`, `docker`, etc.)*

---

### Paso 3.2: Iniciar la Base de Datos con Docker
Desde la raíz del proyecto, ejecuta:
```bash
make infra-up 
```
Esto descargará la imagen oficial de PostgreSQL 15 y ejecutará automáticamente el script `docker/init-db/create-databases.sql`, creando las dos bases de datos necesarias: `moviedb` y `bookingdb`.

**¿Cómo verificar que levantó bien?**
```bash
docker ps
```
Deberías ver el contenedor `postgres-ms` en estado *Up* y saludable (*healthy*).

---

### Paso 3.3: Iniciar `auth-service` (Terminal 1)
El servicio de autenticación emite los tokens JWT y expone la clave pública JWKS. **Debe arrancarse primero** para que los demás microservicios puedan validar su emisor (`issuer-uri`).

Abre la **Terminal 1**:
* **En Windows:**
run AuthServiceApplication

**¿Cómo saber que está listo?**
Verás en consola `Started AuthServiceApplication in ... seconds` en el puerto `50003`.
Puedes verificar abriendo en tu navegador:
[http://localhost:50003/.well-known/openid-configuration](http://localhost:50003/.well-known/openid-configuration)

---

### Paso 3.4: Iniciar `movie-service` (Terminal 2)
Este servicio gestiona películas y funciones. Al arrancar, Flyway creará las tablas automáticamente y poblará la base de datos con los datos de muestra iniciales (2 películas y 3 funciones).

Abre la **Terminal 2**:
* **En Windows:**
run MovieServiceApplication

**¿Cómo saber que está listo?**
Verás `Started MovieServiceApplication in ... seconds` en el puerto `51100`.
Abre Swagger UI en tu navegador para comprobarlo:
👉 [http://localhost:51100/api/v1/swagger-ui.html](http://localhost:51100/api/v1/swagger-ui.html) *(o [http://localhost:51100/swagger-ui.html](http://localhost:51100/swagger-ui.html))*

---

### Paso 3.5: Iniciar `booking-service` (Terminal 3)
Este es el orquestador del SAGA que administra las reservas de los clientes.

Abre la **Terminal 3**:
run BoolingServiceApplication

**¿Cómo saber que está listo?**
Verás `Started BookingServiceApplication in ... seconds` en el puerto `51200`.
Abre su Swagger UI en tu navegador:
👉 [http://localhost:51200/api/v1/swagger-ui.html](http://localhost:51200/api/v1/swagger-ui.html) *(o [http://localhost:51200/swagger-ui.html](http://localhost:51200/swagger-ui.html))*

---

## 🔑 4. Obtención del Token JWT

Los endpoints de consulta (`GET`) son públicos, pero los endpoints de escritura (`POST`, `PUT`, `DELETE`, `PATCH`) requieren un token Bearer JWT.

Puedes obtenerlo fácilmente abriendo una cuarta terminal y ejecutando:

### Opción A: Mediante Postman

Ejecutamos los endpoints de crear usuario y login usuario.


Copia el valor de `access_token`. En las interfaces de Swagger UI puedes hacer clic en el botón verde **Authorize** arriba a la derecha y escribir:
```text
Bearer <tu_token_aqui>
```

Similar a lo avanzado en clases.

---

## 🧪 5. Guía de Demostración del Flujo SAGA (Para Evaluar)

Para demostrar en vivo que todo funciona y presentar el proyecto al profesor o revisores, sigue este flujo paso a paso:

### 1. Consultar la Cartelera Inicial (Sin Token)
Consulta las películas y funciones cargadas automáticamente por Flyway:
* **GET** `http://localhost:51100/movies`
* **GET** `http://localhost:51100/screenings`

Anota el `id` de una función con asientos disponibles (por ejemplo, la función en IMAX con ID `73d2a76f-005a-4b96-b072-cd414e5b22b6` que inicia con 45 asientos).

---

### 2. Camino Feliz: Reserva Exitosa (SAGA Completo)
Realiza una reserva enviando el token en el header `Authorization: Bearer <TOKEN>`:

* **POST** `http://localhost:51400/bookings`
```json
{
  "screeningId": "73d2a76f-005a-4b96-b072-cd414e5b22b6",
  "customerId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "totalAmount": 12.50
}
```

**Resultado esperado:**
* **Status:** `201 Created`
* **Header:** `Location: http://localhost:51400/bookings/{bookingId}`
* **Body:** Estado `"status": "CONFIRMED"`, con el `movieTitle` copiado como snapshot.
* **Verificación:** Si consultas de nuevo `GET http://localhost:51100/screenings/73d2a76f-005a-4b96-b072-cd414e5b22b6`, verás que `availableSeats` se redujo a **44**.

---

### 3. Caso de Negocio: Intento de Reserva sin Asientos (422 Inmediato)
Crea una función con 0 asientos o reserva en una que ya no tenga cupos disponibles.

* **POST** `http://localhost:51400/bookings` hacia una función agotada.

**Resultado esperado:**
* **Status:** `422 Unprocessable Entity`
* **Formato RFC 7807:**
  ```json
  {
    "type": "about:blank",
    "title": "Invalid Booking State",
    "status": 422,
    "detail": "Función sin asientos disponibles"
  }
  ```
* **Comportamiento SAGA:** No se crea la reserva en la base de datos ni se descuentan asientos.

---

### 4. SAGA Inverso: Cancelación de Reserva y Compensación
Cancela la reserva que creaste en el paso 2:

* **PATCH** `http://localhost:51200/bookings/{bookingId}/cancel`
  *(Con header `Authorization: Bearer <TOKEN>`)*

**Resultado esperado:**
* **Status:** `200 OK`
* **Body:** Estado `"status": "CANCELLED"`.
* **Verificación:** Vuelve a consultar la función en `movie-service` (`GET /screenings/{id}`). Comprobarás que el asiento volvió a liberarse y `availableSeats` regresó a **45**.

---

## 📮 6. Colección de Postman Incluida

Si prefieres no usar Swagger o cURL manualmente, en la carpeta `postman/` tienes todo listo para importar:
1. `postman/MovieBookingEnvironment.postman_environment.json`: Contiene las variables `movieServiceUrl`, `bookingServiceUrl`, `authServiceUrl` y guarda automáticamente el token en `TOKEN`.
2. `postman/MovieBookingCollection.postman_collection.json`: Incluye todas las peticiones organizadas por servicio y ordenadas secuencialmente para ejecutar la demo del SAGA en un clic.

---

## ⚠️ 7. Formato de Errores Estándar (RFC 7807)

Todos los errores del sistema cumplen con la especificación `application/problem+json`:

| Código HTTP | Causa |
| :--- | :--- |
| **`400 Bad Request`** | Error de validación `@Valid` (campos obligatorios nulos, números negativos) o JSON malformado. |
| **`401 Unauthorized`** | Token JWT no enviado o inválido en endpoints de escritura. |
| **`404 Not Found`** | Identificador UUID de película, función o reserva inexistente. |
| **`422 Unprocessable Entity`** | Violación de invariante de negocio: función sin asientos disponibles o reserva ya cancelada. |
| **`502 Bad Gateway`** | Fallo de comunicación entre servicios tras agotar los 3 reintentos de resiliencia (`@Retry`). |

---

## 🛠️ 8. Preguntas Frecuentes y Solución de Problemas

* **¿Error de conexión a PostgreSQL (`Connection refused`)?**  
  Asegúrate de que Docker Desktop esté encendido y que hayas ejecutado `docker compose up -d`.
* **¿Error de autenticación al arrancar los servicios (`Could not obtain connection to issuer`)?**  
  Recuerda que `auth-service` debe estar levantado en el puerto `50003` antes de arrancar `movie-service` y `booking-service`.

---

¡Gracias! 🎉
