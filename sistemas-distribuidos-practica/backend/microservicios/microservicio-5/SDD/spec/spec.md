# Especificación — Microservicio 5: Centro de impresión

## 1. Problema

Un centro de impresión tiene tres procesos: **Diseño**, **Revisión** e **Impresión**.
Para cada documento debe cumplirse obligatoriamente el orden:

```text
DISEÑO → REVISIÓN → IMPRESIÓN
```

No se puede revisar un documento que todavía no ha sido diseñado y tampoco se puede
imprimir antes de que haya sido revisado. Es un problema de **sincronización de procesos**:
hay que ordenar la ejecución, no solo excluirla mutuamente.

## 2. Objetivo

Utilizar **semáforos** para imponer un orden determinado de ejecución entre las tres
etapas y repetir el ciclo para **3 documentos**, verificando que el orden sea siempre
correcto.

## 3. Arquitectura MVC

| Capa | Carpeta | Responsabilidad |
|---|---|---|
| Modelo | `modelo/` | Entidades y datos (`Etapa`, `EstadoCentro`, DTOs). |
| Servicio | `servicio/` | Recurso compartido y lógica: `CentroImpresionServicio` (semáforos de turno, contadores, documento actual). |
| Controlador | `controlador/` | `Microservicio5Controller`, endpoints REST. La **Vista** es el JSON que devuelve. |

Flujo: `Cliente → Gateway → Controlador → CentroImpresionServicio (Modelo) → JSON`.

## 4. Requisitos funcionales

- **RF-01** Recurso compartido: el centro de impresión, con su estado en un único servicio.
- **RF-02** Tres semáforos de turno: `turnoDiseno = 1`, `turnoRevision = 0`, `turnoImpresion = 0`.
- **RF-03** Inicialmente solo la etapa DISEÑO puede ejecutarse.
- **RF-04** DISEÑO: `wait(turnoDiseno)` antes de diseñar y `signal(turnoRevision)` al terminar; habilita a REVISIÓN.
- **RF-05** REVISIÓN: `wait(turnoRevision)` antes de revisar y `signal(turnoImpresion)` al terminar; habilita a IMPRESIÓN.
- **RF-06** IMPRESIÓN: `wait(turnoImpresion)` antes de imprimir y `signal(turnoDiseno)` al terminar; habilita de nuevo a DISEÑO.
- **RF-07** Repetir el ciclo para 3 documentos (cantidad configurable).
- **RF-08** Registrar el documento actual, la etapa actual y los contadores por etapa.
- **RF-09** Exponer el estado del centro (`/estado`).
- **RF-10** Reiniciar el estado del centro (`/reset`).
- **RF-11 (análisis)** Analizar qué ocurre si se elimina uno de los `signal()`.
- **RF-12** Exponer todo por REST sin romper `/health` ni `/api`.

## 5. API REST (endpoints)

Acceso directo: `http://localhost:3005`. Vía gateway: `http://localhost:3000/api/microservicio-5`.
> El gateway reenvía `/api/microservicio-5/<ruta>` a `/<ruta>` y solo admite rutas de
> **un segmento**, por eso los endpoints usan rutas simples (`/diseno`, `/revision`, ...) y se
> publican también sin el prefijo `/api`.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/health` | Estado del servicio (se conserva). |
| GET | `/api` | Respuesta base (se conserva). |
| POST | `/diseno` | Ejecuta la etapa de DISEÑO de un documento. |
| POST | `/revision` | Ejecuta la etapa de REVISIÓN del documento diseñado. |
| POST | `/impresion` | Ejecuta la etapa de IMPRESIÓN del documento revisado. |
| GET | `/estado` | Etapa actual, documento actual y contadores por etapa. |
| POST | `/ciclos` | Configura la cantidad de documentos a procesar (por defecto 3). |
| POST | `/reset` | Reinicia los semáforos y contadores. |

Entrada de etapa (opcional): `{ "id": "doc-1", "demoraMs": 300 }`.
Respuesta: `{ "documento", "etapa", "documentosDisenados", "documentosRevisados", "documentosImpresos", "esperoMs", "mensaje" }`.

## 6. Criterios de aceptación

- **CA-01** No se puede revisar sin una señal previa de DISEÑO (la petición de revisión espera).
- **CA-02** No se puede imprimir sin una señal previa de REVISIÓN (la petición de impresión espera).
- **CA-03** El orden observado es siempre `DISEÑO → REVISIÓN → IMPRESIÓN`.
- **CA-04** Cada etapa se ejecuta exactamente una vez por documento.
- **CA-05** Al completar 3 documentos, los contadores valen 3 en cada etapa.
- **CA-06** `/estado` refleja siempre los valores reales.
- **CA-07** `/health` y `/api` siguen respondiendo.
