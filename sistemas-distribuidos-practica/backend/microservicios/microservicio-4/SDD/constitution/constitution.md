# Constitución — Microservicio 4 (Cruce de vehículos)

Reglas fijas del proyecto. Todo lo demás (especificación, plan, tareas) debe respetarlas.

## 1. Identidad

- **Proyecto:** `microservicio-4` — Ejercicio "Cruce de vehículos: una sola dirección activa".
- **Puerto:** `3004`. **Gateway:** `http://localhost:3000/api/microservicio-4`.
- **Tecnología:** Java 17 + Spring Boot 3.4 (Spring Web).
- **Arquitectura:** **MVC (Modelo–Vista–Controlador)**.

## 2. Arquitectura MVC y carpetas

Se programa con tres capas y sus carpetas:

| Capa | Carpeta | Qué contiene |
|---|---|---|
| **Modelo** | `modelo/` | Datos y entidades: `Direccion`, `Vehiculo`, `EstadoPuente`, DTOs. |
| **Servicio** | `servicio/` | Lógica y el recurso compartido: `PuenteServicio` (mutex, contadores, dirección). |
| **Controlador** | `controlador/` | `Microservicio4Controller` (`@RestController`), expone los endpoints REST. |

- La **Vista** es la **respuesta JSON** que devuelve la API; en REST no necesita carpeta propia.
- El controlador **no** lleva lógica de concurrencia; solo recibe peticiones y delega en el servicio.

## 3. Principios

1. **La invariante manda:** nunca debe haber vehículos del NORTE y del SUR cruzando a la vez.
2. **El mutex protege el estado:** dirección y contadores solo se tocan bajo el cerrojo.
3. **Simplicidad:** estado en memoria, sin base de datos ni dependencias extra.
4. **Equidad:** el algoritmo debe evitar que una dirección monopolice el puente (reto del enunciado).
5. **No romper la integración:** se conservan `/health`, `/api`, el puerto `3004` y el `Dockerfile`.
6. **API REST:** el consumo es por HTTP a través del gateway.

## 4. Invariantes

- `direccion` solo puede ser `NINGUNA`, `NORTE` o `SUR`.
- Si hay vehículos del NORTE, no puede haber del SUR (y viceversa).
- Los contadores nunca son negativos.
- Sin vehículos cruzando, la dirección vuelve a `NINGUNA`.

## 5. Definición de "terminado"

1. Los endpoints REST responden y respetan las invariantes.
2. Compila y levanta en el puerto `3004`.
3. `/health` y `/api` siguen funcionando.
4. El reto de equidad está implementado.
5. La separación MVC (Modelo / Servicio / Controlador) se respeta.
