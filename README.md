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

## Endpoints

### Franquicias

| Método   | Ruta                      | Descripción                   |
| -------- | ------------------------- | ----------------------------- |
| `POST`   | `/api/v1/franchises`      | Crea una franquicia           |
| `GET`    | `/api/v1/franchises`      | Lista todas las franquicias   |
| `GET`    | `/api/v1/franchises/{id}` | Obtiene una franquicia por id |
| `PATCH`  | `/api/v1/franchises/{id}` | Renombra una franquicia       |
| `DELETE` | `/api/v1/franchises/{id}` | Elimina una franquicia        |

`POST` y `PATCH` reciben `{"name": "..."}`. El nombre es obligatorio, no puede
estar en blanco y admite hasta 120 caracteres. No se puede repetir dentro de la
misma app.

`GET /api/v1/franchises` está paginado con `page` (página, empezando en 0) y
`limit` (elementos por página, máximo 100): `?page=0&limit=20`. Se ordena por
fecha de creación de forma estable. La respuesta es un envoltorio con `items`,
`page`, `limit`, `totalElements` y `totalPages`.

Los errores usan `application/problem+json` (RFC 7807): `400` con el detalle por
campo en `errors`, `404` si no existe el recurso o la ruta, `405` si el método
no está permitido y `409` si el nombre ya existe.

## Pruebas

Postgres y Redis no publican puertos, así que las pruebas corren en un
contenedor unido a la red del stack. Con el stack levantado:

```bash
docker compose -f docker-compose.test.yml run --rm test
```

El contenedor monta el código fuente y ejecuta `./mvnw verify`. El volumen
`maven-cache` conserva las dependencias descargadas entre ejecuciones.

## Estructura

Arquitectura hexagonal por módulos: el dominio es puro (sin Spring ni JPA), la
capa de aplicación usa casos de uso y la infraestructura provee adaptadores.

```
com/accenture/franchises/
├── FranchiseApiApplication.java   # punto de entrada y descripción OpenAPI
├── franchise/                     # módulo de franquicias
│   ├── domain/                    # modelo y puerto del repositorio (sin frameworks)
│   ├── application/               # caso de uso FranchiseService
│   └── infrastructure/
│       ├── persistence/           # entidad JPA, repositorio y adapter
│       └── web/                   # controller REST y DTOs de entrada/salida
└── common/                        # código transversal (no es un módulo de negocio)
    ├── exception/                 # excepciones compartidas entre módulos
    ├── pagination/                # PageQuery y PageResult
    └── infrastructure/web/        # GlobalExceptionHandler y PageResponse
```

A nivel de proyecto:

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
| Esquema       | Hibernate `ddl-auto=update`                     |
| Caché         | Redis 7                                         |
| Documentación | springdoc-openapi (Swagger UI)                  |
| Monitoreo     | Spring Boot Actuator                            |
| Empaquetado   | Docker (imagen multi-stage, usuario no root)    |
| Arquitectura  | Hexagonal por módulos (dominio, aplicación, infra) |
