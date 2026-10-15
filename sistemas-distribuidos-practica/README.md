# APE 3 - Sistemas Distribuidos

Monorepo académico con un frontend React, un API Gateway y cinco microservicios.

## Estructura

```text
frontend/                 Plantilla React de la práctica
gateway/                  Punto único de entrada de la API
microservicios/           Cinco servicios independientes
shared/                   Código común mínimo de los servicios
docker-compose.yml        Orquestación de todo el sistema
```

## Ejecutar en Visual Studio Code

1. Instala Node.js LTS desde https://nodejs.org si todavía no está instalado.
2. Abre esta carpeta en Visual Studio Code.
3. Abre una terminal con **Terminal > New Terminal**.
4. Instala las dependencias del frontend y ejecútalo:

```bash
npm --prefix frontend install
npm run dev:frontend
```

5. En otra terminal ejecuta el Gateway y los microservicios:

```bash
npm run start:services
npm run start:gateway
```

6. Abre `http://localhost:5173`. El Gateway queda disponible en `http://localhost:3000`.

## Rutas de integración

```text
GET http://localhost:3000/health
GET http://localhost:3000/api/services
GET http://localhost:3000/api/microservicio-1
GET http://localhost:3000/api/microservicio-2
GET http://localhost:3000/api/microservicio-3
GET http://localhost:3000/api/microservicio-4
GET http://localhost:3000/api/microservicio-5
```

## Ejecución con Docker

Requiere Docker Desktop instalado:

```bash
docker compose up --build
```

El frontend estará en `http://localhost:5173`.

## Personalizar la guía

Edita `frontend/src/data.js` para reemplazar los textos `TODO` por el contenido real de la práctica.

Las respuestas del estudiante se guardan automáticamente en `localStorage`. El botón **Imprimir / PDF** permite imprimir la pestaña actual o guardarla como PDF desde el diálogo del navegador.
