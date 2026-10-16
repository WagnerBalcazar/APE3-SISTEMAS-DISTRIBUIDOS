# Plan — Microservicio 5 (Centro de impresión, MVC)

## 1. Estructura del proyecto

```text
src/main/java/edu/ape3/microservicio5
├── Microservicio5Application.java        arranque Spring Boot
├── modelo/                               [MODELO]
│   ├── Etapa.java                        enum NINGUNA, DISENO, REVISION, IMPRESION
│   ├── EstadoCentro.java                 snapshot del estado (respuesta)
│   └── dto/
│       ├── SolicitudEtapa.java           id, demoraMs
│       ├── RespuestaEtapa.java           respuesta de una etapa
│       └── SolicitudCiclos.java          ciclos (documentos a procesar)
├── servicio/                             [SERVICIO] (recurso compartido)
│   └── CentroImpresionServicio.java      semáforos de turno + contadores + documento actual
└── controlador/                          [CONTROLADOR]
    └── Microservicio5Controller.java     endpoints REST (@RestController)

src/test/java/edu/ape3/microservicio5
├── CentroImpresionServicioTest.java      orden, invariantes y concurrencia
└── Microservicio5ControllerTest.java     endpoints
```

- **Vista:** el JSON que devuelve el controlador (no necesita carpeta).
- El **Controlador** solo traduce HTTP ↔ llamadas al **Servicio**. La sincronización
  (semáforos) y el estado viven en **CentroImpresionServicio**.

## 2. Servicio: estado del centro

```text
SEMÁFORO turnoDiseno    = 1     // DISEÑO habilitado inicialmente
SEMÁFORO turnoRevision  = 0     // bloqueado hasta que DISEÑO haga signal
SEMÁFORO turnoImpresion = 0     // bloqueado hasta que REVISIÓN haga signal

etapaActual = NINGUNA
documentoActual = ""
documentosDisenados = 0
documentosRevisados = 0
documentosImpresos  = 0
ciclosTotales = 3
```

## 3. Algoritmo por etapa (pseudocódigo → método)

```text
disenar(id):                       revisar(id):                        imprimir(id):
  wait(turnoDiseno)                  wait(turnoRevision)                 wait(turnoImpresion)
  crear_documento()                  revisar_documento()                 imprimir_documento()
  documentosDisenados++              documentosRevisados++               documentosImpresos++
  etapaActual = DISENO               etapaActual = REVISION              etapaActual = IMPRESION
  signal(turnoRevision)              signal(turnoImpresion)              signal(turnoDiseno)
```

- Cada etapa hace `wait` en **su** semáforo (arranca en `0`, salvo DISEÑO en `1`) y `signal`
  en el semáforo de la **etapa siguiente**, cerrando el ciclo.
- La demora simulada (`Thread.sleep`) ocurre **fuera** del punto de sincronización lógico,
  sin retener el recurso compartido.
- `wait`/`signal` se implementan con `java.util.concurrent.Semaphore` justo o equitativo (`fair = true`).
- Si el documento ya alcanzó `ciclosTotales`, la petición responde sin ejecutar otra vez.

## 4. Análisis: eliminar un `signal()`

- **Quitar `signal(turnoRevision)`:** REVISIÓN nunca despierta; como IMPRESIÓN depende de
  REVISIÓN, el ciclo se detiene en el primer documento. Bloqueo permanente (inanición).
- **Quitar `signal(turnoImpresion)`:** IMPRESIÓN nunca despierta y DISEÑO tampoco recibe su
  turno de vuelta, así que solo se diseña un documento.
- **Quitar `signal(turnoDiseno)`:** DISEÑO nunca vuelve a habilitarse, por lo que solo se
  procesa un documento y el resto queda bloqueado.

Conclusión: cada `signal()` es imprescindible; quitarlo rompe el orden o detiene el ciclo.
Esto se documenta y se verifica con pruebas.

## 5. Controlador (REST)

| Método | Ruta | Acción |
|---|---|---|
| GET | `/health`, `/api` | Conservados. |
| POST | `/diseno`, `/api/diseno` | Delega al servicio. |
| POST | `/revision`, `/api/revision` | Delega al servicio. |
| POST | `/impresion`, `/api/impresion` | Delega al servicio. |
| GET | `/estado`, `/api/estado` | Devuelve el snapshot (Vista). |
| POST | `/ciclos`, `/api/ciclos` | Configura los ciclos (documentos). |
| POST | `/reset`, `/api/reset` | Reinicia el estado. |

- `@CrossOrigin(origins = "*")` para el frontend.
- Se publican rutas con y sin `/api`; son de un solo segmento porque el gateway
  reenvía `/api/microservicio-5/<ruta>` a `/<ruta>` y no cruza `/`.

## 6. Pruebas

- **Unitarias:** el orden DISEÑO → REVISIÓN → IMPRESIÓN y las invariantes sin concurrencia.
- **Concurrencia:** varios documentos en paralelo; nunca hay revisión antes de diseño ni
  impresión antes de revisión.
- **Análisis:** con un `signal()` eliminado el ciclo se detiene (verificación del bloqueo).
- **API:** todos los endpoints devuelven el JSON esperado.
- Comando: `mvn -f backend/microservicios/microservicio-5/pom.xml test`.

## 7. Orden de trabajo

1. `modelo/` (Etapa, DTOs, EstadoCentro).
2. `servicio/CentroImpresionServicio` (tres semáforos y contadores).
3. `controlador/Microservicio5Controller` (endpoints REST).
4. Análisis de eliminar un `signal()`.
5. Pruebas y verificación.
