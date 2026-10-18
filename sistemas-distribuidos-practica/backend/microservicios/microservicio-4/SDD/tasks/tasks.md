# Tareas — Microservicio 4 (Cruce de vehículos, MVC)

`[ ]` pendiente · `[x]` hecha. Arquitectura MVC: `modelo/`, `servicio/`, `controlador/`.

## 1. Modelo — `modelo/`

- [x] **T-01** `Direccion.java` (enum `NINGUNA, NORTE, SUR`). — RF-02
- [x] **T-02** `dto/SolicitudVehiculo.java` (`id`, `demoraMs`). — RF-12
- [x] **T-03** `dto/RespuestaVehiculo.java`. — RF-12
- [x] **T-04** `dto/SolicitudEquidad.java` (`activa`). — RF-11
- [x] **T-05** `EstadoPuente.java` (snapshot del estado). — RF-02, RF-03

## 2. Servicio — `servicio/`

- [x] **T-06** `PuenteServicio.java` con `ReentrantLock` + `Condition`. — RF-01, RF-04
- [x] **T-07** Variables `direccion`, `vehiculosNorte`, `vehiculosSur`. — RF-02, RF-03
- [x] **T-08** Entrada NORTE: verificar dirección, esperar, incrementar. — RF-05, RF-06, RF-07
- [x] **T-09** Salida NORTE: decrementar y volver a `NINGUNA` al vaciar. — RF-08, RF-09
- [x] **T-10** Cruce SUR con la misma lógica. — RF-10
- [x] **T-11** Cruce simulado (`Thread.sleep`) fuera del mutex. — RNF
- [x] **T-12** Métodos `estado()`, `setEquidad()`, `reset()`. — RF-12

## 3. Controlador — `controlador/`

- [x] **T-13** `Microservicio4Controller` inyectando `PuenteServicio`. — RF-12
- [x] **T-14** `GET /health` y `GET /api` (se conservan). — CA-07
- [x] **T-15** `POST /norte` y `POST /sur`. — RF-12
- [x] **T-16** `GET /estado`. — CA-06
- [x] **T-17** `POST /equidad` y `POST /reset`. — RF-11
- [x] **T-18** `@CrossOrigin` y rutas con/sin `/api`. — RNF
- [x] **T-19** El controlador no contiene lógica de concurrencia. — MVC

## 4. Reto anti-inanición

- [x] **T-20** Contar vehículos en espera por dirección. — RF-11
- [x] **T-21** Ceder el turno a la dirección contraria cuando espera. — CA-05
- [x] **T-22** Exponer `equidadActiva` y esperas en `/estado`. — RF-11

## 5. Pruebas y cierre

- [x] **T-23** Prueba unitaria de invariantes. — CA-01..CA-04
- [x] **T-24** Prueba de concurrencia NORTE/SUR. — CA
- [x] **T-25** Prueba de equidad. — CA-05
- [x] **T-26** Prueba de los endpoints REST. — CA-06, CA-07
- [x] **T-27** `mvn test` y `mvn package` sin errores. — DoD
- [x] **T-28** Verificar por gateway `/api/microservicio-4`. — CA-07
