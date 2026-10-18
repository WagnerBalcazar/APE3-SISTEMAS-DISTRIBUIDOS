# Plan — Microservicio 4 (Cruce de vehículos, MVC)

## 1. Estructura del proyecto

```text
src/main/java/edu/ape3/microservicio4
├── Microservicio4Application.java      arranque Spring Boot
├── modelo/                             [MODELO]
│   ├── Direccion.java                  enum NINGUNA, NORTE, SUR
│   ├── EstadoPuente.java               snapshot del estado (respuesta)
│   └── dto/
│       ├── SolicitudVehiculo.java      id, demoraMs
│       ├── SolicitudEquidad.java       activa
│       └── RespuestaVehiculo.java      respuesta del cruce
├── servicio/                           [SERVICIO] (recurso compartido)
│   └── PuenteServicio.java             mutex + direccion + contadores + equidad
└── controlador/                        [CONTROLADOR]
    └── Microservicio4Controller.java   endpoints REST (@RestController)

src/test/java/edu/ape3/microservicio4
├── PuenteServicioTest.java             concurrencia e invariantes
└── Microservicio4ControllerTest.java   endpoints
```

- **Vista:** el JSON que devuelve el controlador (no necesita carpeta).
- El **Controlador** solo traduce HTTP ↔ llamadas al **Servicio**. La lógica y el mutex
  viven en **PuenteServicio**.

## 2. Servicio: estado del puente

```text
lock (ReentrantLock) + condicion (Condition)
direccion = NINGUNA
vehiculosNorte = 0
vehiculosSur = 0
equidadActiva = false
esperandoNorte = 0
esperandoSur = 0
```

## 3. Algoritmo de entrada (NORTE; SUR es igual)

```text
lock
MIENTRAS no pueda entrar: esperar()
SI direccion == NINGUNA: direccion = NORTE
vehiculosNorte++
unlock

cruzar()          // fuera del mutex (sleep simulado)

lock
vehiculosNorte--
SI vehiculosNorte == 0 Y vehiculosSur == 0: direccion = NINGUNA ; avisar a todos
unlock
```

## 4. Reto anti-inanición (equidad)

- Problema: si una dirección llega sin parar, la contraria nunca entra.
- Solución: cuando la dirección contraria tiene vehículos esperando, se deja de
  admitir nuevos de la dirección actual; al vaciarse el puente, se cede el **turno**
  a la dirección que espera. Así la espera queda acotada.
- Se activa con `POST /equidad` y se refleja en `/estado` (`equidadActiva`, `esperando*`).

## 5. Controlador (REST)

| Método | Ruta | Acción |
|---|---|---|
| GET | `/health`, `/api` | Conservados. |
| POST | `/norte`, `/api/norte` | Delega al servicio. |
| POST | `/sur`, `/api/sur` | Delega al servicio. |
| GET | `/estado`, `/api/estado` | Devuelve el snapshot (Vista). |
| POST | `/equidad`, `/api/equidad` | Activa/desactiva equidad. |
| POST | `/reset`, `/api/reset` | Reinicia el estado. |

- `@CrossOrigin(origins = "*")` para el frontend.
- Se publican rutas con y sin `/api`; son de un solo segmento porque el gateway
  reenvía `/api/microservicio-4/<ruta>` a `/<ruta>` y no cruza `/`.

## 6. Pruebas

- **Unitarias:** transiciones e invariantes sin concurrencia.
- **Concurrencia:** muchos vehículos NORTE y SUR; nunca ambos a la vez.
- **Equidad:** la dirección contraria entra en tiempo acotado.
- **API:** todos los endpoints devuelven el JSON esperado.
- Comando: `mvn -f backend/microservicios/microservicio-4/pom.xml test`.

## 7. Orden de trabajo

1. `modelo/` (Direccion, DTOs, EstadoPuente).
2. `servicio/PuenteServicio` (algoritmo básico NORTE/SUR).
3. `controlador/Microservicio4Controller` (endpoints REST).
4. Equidad (reto).
5. Pruebas y verificación.
