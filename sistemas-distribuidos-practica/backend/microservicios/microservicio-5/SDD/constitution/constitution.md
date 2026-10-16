# Constitución — Microservicio 5 (Centro de impresión)

Reglas fijas del proyecto. Todo lo demás (especificación, plan, tareas) debe respetarlas.

## 1. Identidad

- **Proyecto:** `microservicio-5` — Ejercicio "Centro de impresión: sincronización de procesos".
- **Puerto:** `3005`. **Gateway:** `http://localhost:3000/api/microservicio-5`.
- **Tecnología:** Java 17 + Spring Boot 3.4 (Spring Web).
- **Arquitectura:** **MVC (Modelo–Vista–Controlador)**.

## 2. Arquitectura MVC y carpetas

Se programa con tres capas y sus carpetas:

| Capa | Carpeta | Qué contiene |
|---|---|---|
| **Modelo** | `modelo/` | Datos y entidades: `Etapa`, `EstadoCentro`, DTOs. |
| **Servicio** | `servicio/` | Lógica y el recurso compartido: `CentroImpresionServicio` (semáforos de turno, contadores, documento actual). |
| **Controlador** | `controlador/` | `Microservicio5Controller` (`@RestController`), expone los endpoints REST. |

- La **Vista** es la **respuesta JSON** que devuelve la API; en REST no necesita carpeta propia.
- El controlador **no** lleva lógica de sincronización; solo recibe peticiones y delega en el servicio.

## 3. Principios

1. **La invariante manda:** nunca se revisa un documento sin diseño previo ni se imprime sin revisión previa.
2. **Los semáforos ordenan la ejecución:** cada etapa debe hacer `wait` en su semáforo de turno antes de actuar y `signal` en el semáforo de la etapa siguiente al terminar.
3. **Simplicidad:** estado en memoria, sin base de datos ni dependencias extra.
4. **Sin bloqueo ni inanición:** el ciclo siempre avanza y las tres etapas se ejecutan en orden para cada documento.
5. **No romper la integración:** se conservan `/health`, `/api`, el puerto `3005` y el `Dockerfile`.
6. **API REST:** el consumo es por HTTP a través del gateway.

## 4. Invariantes

- El orden de ejecución es siempre `DISEÑO → REVISIÓN → IMPRESIÓN`.
- Para un mismo documento no puede haber `revisión` sin `diseño` ni `impresión` sin `revisión`.
- `documentosRevisados <= documentosDisenados` y `documentosImpresos <= documentosRevisados`.
- Los contadores nunca son negativos ni superan los ciclos configurados.
- Una sola etapa es la activa a la vez; las demás permanecen bloqueadas en su semáforo.
- Al menos un semáforo permanece habilitado (`> 0`) para que el ciclo no se detenga.

## 5. Definición de "terminado"

1. Los endpoints REST responden y respetan las invariantes de orden.
2. Compila y levanta en el puerto `3005`.
3. `/health` y `/api` siguen funcionando.
4. El ciclo se repite para 3 documentos por defecto (configurable).
5. El análisis de eliminar un `signal()` está documentado.
6. La separación MVC (Modelo / Servicio / Controlador) se respeta.
