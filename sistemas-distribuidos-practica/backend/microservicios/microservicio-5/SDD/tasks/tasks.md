# Tareas — Microservicio 5 (Centro de impresión, MVC)

`[ ]` pendiente · `[x]` hecha. Arquitectura MVC: `modelo/`, `servicio/`, `controlador/`.

## 1. Modelo — `modelo/`

- [x] **T-01** `Etapa.java` (enum `NINGUNA, DISENO, REVISION, IMPRESION`). — RF-03
- [x] **T-02** `dto/SolicitudEtapa.java` (`id`, `demoraMs`). — RF-12
- [x] **T-03** `dto/RespuestaEtapa.java`. — RF-12
- [x] **T-04** `dto/SolicitudCiclos.java` (`ciclos`). — RF-07
- [x] **T-05** `EstadoCentro.java` (snapshot del estado). — RF-08, RF-09

## 2. Servicio — `servicio/`

- [x] **T-06** `CentroImpresionServicio.java` con tres `Semaphore` (equitativos). — RF-01, RF-02
- [x] **T-07** Semáforos `turnoDiseno=1`, `turnoRevision=0`, `turnoImpresion=0`. — RF-02, RF-03
- [x] **T-08** DISEÑO: `wait(turnoDiseno)`, crear documento, `signal(turnoRevision)`. — RF-04
- [x] **T-09** REVISIÓN: `wait(turnoRevision)`, revisar, `signal(turnoImpresion)`. — RF-05
- [x] **T-10** IMPRESIÓN: `wait(turnoImpresion)`, imprimir, `signal(turnoDiseno)`. — RF-06
- [x] **T-11** Contadores `documentosDisenados`, `documentosRevisados`, `documentosImpresos`. — RF-08
- [x] **T-12** Ciclo repetible para `ciclosTotales` documentos (por defecto 3). — RF-07
- [x] **T-13** Etapa simulada (`Thread.sleep`) sin retener el recurso compartido. — RNF
- [x] **T-14** Métodos `estado()`, `setCiclos()`, `reset()`. — RF-09, RF-10

## 3. Controlador — `controlador/`

- [x] **T-15** `Microservicio5Controller` inyectando `CentroImpresionServicio`. — RF-12
- [x] **T-16** `GET /health` y `GET /api` (se conservan). — CA-07
- [x] **T-17** `POST /diseno`, `POST /revision` y `POST /impresion`. — RF-12
- [x] **T-18** `GET /estado`. — CA-06
- [x] **T-19** `POST /ciclos` y `POST /reset`. — RF-07, RF-10
- [x] **T-20** `@CrossOrigin` y rutas con/sin `/api`. — RNF
- [x] **T-21** El controlador no contiene lógica de sincronización. — MVC

## 4. Análisis del `signal()`

- [x] **T-22** Documentar el efecto de eliminar `signal(turnoRevision)`. — RF-11
- [x] **T-23** Documentar el efecto de eliminar `signal(turnoImpresion)` y `signal(turnoDiseno)`. — RF-11
- [x] **T-24** Verificar con una prueba que sin el `signal()` el ciclo se detiene. — CA-01, CA-02

## 5. Pruebas y cierre

- [x] **T-25** Prueba unitaria de orden e invariantes. — CA-01..CA-04
- [x] **T-26** Prueba de concurrencia de varias etapas/documentos. — CA-03, CA-04
- [x] **T-27** Prueba del ciclo completo de 3 documentos. — CA-05
- [x] **T-28** Prueba de los endpoints REST. — CA-06, CA-07
- [x] **T-29** `mvn test` y `mvn package` sin errores. — DoD
- [x] **T-30** Verificar por gateway `/api/microservicio-5`. — CA-07
