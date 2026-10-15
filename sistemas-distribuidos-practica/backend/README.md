# Backend

El backend está compuesto por un API Gateway y cinco instancias independientes de una aplicación Spring Boot.

## Componentes

- `gateway`: entrada única de la API en el puerto `3000`.
- `microservicio-template`: aplicación base configurable para los servicios `1` a `5`.

Cada instancia recibe `SERVER_PORT`, `SERVICE_NUMBER` y `SERVICE_EXERCISE` mediante Docker Compose. Aunque comparten código base, se ejecutan como procesos y contenedores separados.

## Compilar

```bash
mvn -f gateway/pom.xml clean package
mvn -f microservicio-template/pom.xml clean package
```

## Ejecutar

```bash
java -jar microservicio-template/target/microservicio-template-1.0.0.jar --server.port=3001 --service.number=1
java -jar gateway/target/gateway-1.0.0.jar
```

Para levantar todas las instancias se recomienda ejecutar desde la raíz:

```bash
docker compose up --build
```
