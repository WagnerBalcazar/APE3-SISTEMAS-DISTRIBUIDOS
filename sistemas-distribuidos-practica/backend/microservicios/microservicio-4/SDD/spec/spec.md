# Especificación — Microservicio 4: Cruce de vehículos

## 1. Problema

Un puente permite circular en ambos sentidos, pero **solo una dirección puede estar
activa a la vez**. Varios vehículos pueden cruzar en el mismo sentido, pero nunca
vehículos del NORTE y del SUR al mismo tiempo. Es un problema de procesos que
comparten un recurso, con exclusión mutua por dirección.

## 2. Objetivo

Coordinar los cruces para que dos grupos incompatibles no accedan a la vez, y evitar
que una dirección monopolice el puente (inanición).

## 3. Arquitectura MVC

| Capa | Carpeta | Responsabilidad |
|---|---|---|
| Modelo | `modelo/` | Entidades y datos (`Direccion`, `Vehiculo`, `EstadoPuente`, DTOs). |
| Servicio | `servicio/` | Recurso compartido y lógica: `PuenteServicio` (dirección, contadores, mutex). |
| Controlador | `controlador/` | `Microservicio4Controller`, endpoints REST. La **Vista** es el JSON que devuelve. |

Flujo: `Cliente → Gateway → Controlador → PuenteServicio (Modelo) → JSON`.

## 4. Requisitos funcionales

- **RF-01** Recurso compartido: el puente, con su estado en un único servicio.
- **RF-02** Variable `direccion`: `NINGUNA | NORTE | SUR`.
- **RF-03** Contadores `vehiculosNorte` y `vehiculosSur`.
- **RF-04** Proteger el estado con un mutex.
- **RF-05** Verificar la dirección antes de entrar.
- **RF-06** Si hay vehículos de la dirección contraria, esperar.
- **RF-07** Al entrar, incrementar el contador de su dirección.
- **RF-08** Al salir, decrementar el contador.
- **RF-09** Cuando no queden vehículos, la dirección vuelve a `NINGUNA`.
- **RF-10** Implementar el cruce hacia el SUR (igual que el NORTE).
- **RF-11 (reto)** Evitar que una dirección monopolice el puente.
- **RF-12** Exponer todo por REST sin romper `/health` ni `/api`.

## 5. API REST (endpoints)

Acceso directo: `http://localhost:3004`. Vía gateway: `http://localhost:3000/api/microservicio-4`.
> El gateway reenvía `/api/microservicio-4/<ruta>` a `/<ruta>` y solo admite rutas de
> **un segmento**, por eso los endpoints usan rutas simples (`/norte`, `/sur`, ...) y se
> publican también sin el prefijo `/api`.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/health` | Estado del servicio (se conserva). |
| GET | `/api` | Respuesta base (se conserva). |
| POST | `/norte` | Un vehículo cruza hacia el NORTE. |
| POST | `/sur` | Un vehículo cruza hacia el SUR. |
| GET | `/estado` | Dirección, contadores y modo de equidad. |
| POST | `/equidad` | Activa/desactiva la equidad anti-inanición. |
| POST | `/reset` | Reinicia el estado. |

Entrada del vehículo (opcional): `{ "id": "auto-1", "demoraMs": 500 }`.
Respuesta: `{ "vehiculo", "direccion", "vehiculosNorte", "vehiculosSur", "esperoMs", "mensaje" }`.

## 6. Criterios de aceptación

- **CA-01** Con vehículos del NORTE cruzando, un vehículo del SUR espera su turno.
- **CA-02** Varios vehículos de la misma dirección cruzan a la vez.
- **CA-03** Al quedar vacío, la dirección cambia a `NINGUNA` y admite la contraria.
- **CA-04** Los contadores nunca son negativos.
- **CA-05** Con equidad activa, la dirección contraria entra en tiempo acotado.
- **CA-06** `/estado` refleja siempre los valores reales.
- **CA-07** `/health` y `/api` siguen respondiendo.
