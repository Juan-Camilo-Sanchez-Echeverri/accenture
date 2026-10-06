# Franchise API

API de inventario de franquicias: franquicias, sucursales y productos con su stock.
Spring Boot 4.1.1 sobre PostgreSQL, con Redis como caché.

## Requisitos

- Docker Desktop (o Docker Engine + Compose v2)

## Puesta en marcha

```bash
cp .env.example .env
docker compose up -d --build --wait
```

El primer arranque descarga las imágenes, compila el jar y levanta los tres
servicios. `--wait` termina cuando todos están healthy.

Para detener:

```bash
docker compose down
```

Para borrar también los datos de Postgres y Redis:

```bash
docker compose down -v
```

## Servicios

| Servicio   | Puerto en el host | Notas                                         |
| ---------- | ----------------- | --------------------------------------------- |
| `api`      | 8080              | Único servicio con puerto publicado           |
| `postgres` | ninguno           | Accesible solo desde la red interna de Docker |
| `redis`    | ninguno           | Accesible solo desde la red interna de Docker |

Las credenciales viven en `.env`, que está en `.gitignore`. `.env.example` tiene
los valores por defecto para desarrollo.

## Verificación

```bash
curl http://localhost:8080/actuator/health
```

```json
{
  "status": "UP",
  "components": {
    "db": { "status": "UP" },
    "ping": { "status": "UP" },
    "redis": { "status": "UP" }
  }
}
```

Documentación interactiva de la API:

- <http://localhost:8080/swagger-ui.html>
- <http://localhost:8080/v3/api-docs>

## Pruebas

Postgres y Redis no publican puertos, así que las pruebas corren en un
contenedor unido a la red del stack. Con el stack levantado:

```bash
docker compose -f docker-compose.test.yml run --rm test
```

El contenedor monta el código fuente y ejecuta `./mvnw verify`. El volumen
`maven-cache` conserva las dependencias descargadas entre ejecuciones.

## Estructura

```
.
├── Dockerfile               # build multi-stage: compila, imagen final solo con el JRE
├── docker-compose.yml       # postgres, redis y api
├── docker-compose.test.yml  # suite de pruebas de Maven
├── .env.example             # plantilla de variables de entorno
├── mvnw / pom.xml           # Maven Wrapper y build
├── data/                    # datos locales de postgres y redis (bind mounts, no se versiona)
└── src
    ├── main
    │   ├── java/com/accenture/franchises/
    │   └── resources/application.properties
    └── test/java/com/accenture/franchises/
```

## Stack

| Pieza         | Tecnología                                      |
| ------------- | ----------------------------------------------- |
| Lenguaje      | Java 21                                         |
| Framework     | Spring Boot 4.1.1 (Spring MVC)                  |
| Build         | Maven 3.9 con Maven Wrapper                     |
| Persistencia  | PostgreSQL 17 con Spring Data JPA e Hibernate 6 |
| Migraciones   | Flyway                                          |
| Caché         | Redis 7                                         |
| Documentación | springdoc-openapi (Swagger UI)                  |
| Monitoreo     | Spring Boot Actuator                            |
| Empaquetado   | Docker (imagen multi-stage, usuario no root)    |
