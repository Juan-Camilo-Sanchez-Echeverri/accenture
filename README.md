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

| Método   | Ruta                                   | Descripción                                |
| -------- | -------------------------------------- | ------------------------------------------ |
| `POST`   | `/api/v1/franchises`                   | Crea una franquicia                        |
| `GET`    | `/api/v1/franchises`                   | Lista todas las franquicias                |
| `GET`    | `/api/v1/franchises/{id}`              | Obtiene una franquicia por id              |
| `GET`    | `/api/v1/franchises/{id}/top-products` | El producto con más stock de cada sucursal |
| `PATCH`  | `/api/v1/franchises/{id}`              | Renombra una franquicia                    |
| `DELETE` | `/api/v1/franchises/{id}`              | Elimina una franquicia                     |

`POST` y `PATCH` reciben `{"name": "..."}`. El nombre es obligatorio, no puede
estar en blanco y admite hasta 120 caracteres. No se puede repetir dentro de la
misma app.

`GET /api/v1/franchises` está paginado con `page` (página, empezando en **0**) y
`limit` (elementos por página, máximo 100): `?page=0&limit=20`. Se ordena por
fecha de creación de forma estable. La respuesta es un envoltorio con `items`,
`page`, `limit`, `totalElements` y `totalPages`.

### Sucursales

| Método   | Ruta                                               | Descripción                                                 |
| -------- | -------------------------------------------------- | ----------------------------------------------------------- |
| `POST`   | `/api/v1/franchises/{franchiseId}/branches`        | Crea una sucursal dentro de la franquicia                   |
| `GET`    | `/api/v1/branches`                                 | Lista las sucursales paginadas, sin productos               |
| `GET`    | `/api/v1/branches/{branchId}`                      | Obtiene una sucursal con los productos que oferta           |
| `PATCH`  | `/api/v1/branches/{branchId}`                      | Renombra una sucursal                                       |
| `DELETE` | `/api/v1/branches/{branchId}`                      | Elimina una sucursal                                        |
| `POST`   | `/api/v1/branches/{branchId}/products`             | Vincula un producto del catálogo a la sucursal con su stock |
| `PATCH`  | `/api/v1/branches/{branchId}/products/{productId}` | Cambia el stock del producto en la sucursal                 |
| `DELETE` | `/api/v1/branches/{branchId}/products/{productId}` | Quita el producto de la sucursal (se descarta su stock)     |

`POST` de sucursal recibe `{"name": "..."}`. El nombre es obligatorio, no puede
estar en blanco y admite hasta 100 caracteres. No se puede repetir dentro de la
misma franquicia.

`POST /api/v1/branches/{branchId}/products` recibe
`{"productId": "...", "stock": 10}` (stock mínimo 0): `201` al vincular, `404`
si no existe la franquicia, la sucursal o el producto, y `409` si el producto ya
está vinculado a esa sucursal. `PATCH .../products/{productId}` recibe
`{"stock": 5}` y responde `200` con el stock actualizado. `DELETE` responde `204`
y `404` si el vínculo no existe.

`GET /api/v1/branches` está paginado igual que el de franquicias (`?page=0&limit=20`),
con el mismo envoltorio `items`, `page`, `limit`, `totalElements` y `totalPages`.

### Productos

| Método   | Ruta                    | Descripción                                            |
| -------- | ----------------------- | ------------------------------------------------------ |
| `POST`   | `/api/v1/products`      | Crea un producto en el catálogo global                 |
| `GET`    | `/api/v1/products`      | Lista los productos con su stock por sucursal          |
| `GET`    | `/api/v1/products/{id}` | Obtiene un producto con su stock por sucursal          |
| `PATCH`  | `/api/v1/products/{id}` | Renombra un producto                                   |
| `DELETE` | `/api/v1/products/{id}` | Elimina un producto (lo quita de todas las sucursales) |

`POST` y `PATCH` reciben `{"name": "..."}` (máximo 120 caracteres). El nombre no
se puede repetir entre productos. El catálogo se crea por separado: vincular un
producto a una sucursal se hace con el `POST` de la sección Sucursales.

`GET /api/v1/products` está paginado igual que las franquicias. Cada producto
incluye `stocks`, la lista de sucursales donde está presente con su nombre y la
cantidad en stock en cada una (se consulta en una sola pasada, sin perder
rendimiento por cantidad de productos).

Los errores usan `application/problem+json` (RFC 7807): `400` con el detalle por
campo en `errors`, `404` si no existe el recurso o la ruta, `405` si el método
no está permitido y `409` si el nombre ya existe o el vínculo ya fue creado.

## Caché

El producto con más stock por sucursal (top-products) se cachea en Redis con
**Jedis** usando el cliente `RedisClient`

- **Key**: `franchise:top-products:{franchiseId}`
- **TTL**: 60 segundos por defecto (`top-products.cache.ttl-seconds`)
- **Invalidación**: cualquier mutación que afecte a las sucursales de la
  franquicia la borra en Redis para que el siguiente GET regenere el valor:
  alta/renombrado/baja de sucursal, vincular o desvincular producto y cambio de
  stock.
- **Tolerancia a fallos**: si Redis no responde, el endpoint calcula el resultado
  contra la base de datos y la API sigue funcionando (se registra un warning).

Configuración vía variables de entorno:

| Variable         | Local (docker compose) | Redis Cloud (redis.io)                                                    |
| ---------------- | ---------------------- | ------------------------------------------------------------------------- |
| `REDIS_HOST`     | `redis`                | el host público de tu instancia, p. ej. `redis-XXXXX.cloud.redislabs.com` |
| `REDIS_PORT`     | `6379`                 | el puerto de la instancia                                                 |
| `REDIS_PASSWORD` | vacío                  | la contraseña de la instancia (autentica como el usuario `default`)       |

`redis` es el nombre del servicio dentro de la red de Docker y solo se resuelve
desde otro contenedor del stack; si corres la app en tu máquina, usa
`localhost` (o el host de la nube). Cuando `REDIS_PASSWORD` trae valor, el
cliente autentica como el usuario `default`, igual que en el quickstart de
redis.io.

## Pruebas

Postgres y Redis no publican puertos, así que las pruebas corren en un
contenedor unido a la red del stack. Con el stack levantado:

```bash
docker compose up -d --build --wait
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
│       ├── redis/                 # TopProductCache (JSON en Redis)
│       └── web/                   # controller REST y DTOs de entrada/salida
├── branch/                        # módulo de sucursales (incluye sucursal-producto + stock)
│   ├── domain/                    # Branch y BranchProduct con sus puertos
│   ├── application/               # caso de uso BranchService
│   └── infrastructure/
│       ├── persistence/           # entidades JPA y adapter
│       └── web/                   # controller REST y DTOs
├── product/                       # módulo de productos (catálogo global)
│   ├── domain/                    # Product, ProductStock y puerto del repositorio
│   ├── application/               # caso de uso ProductService
│   └── infrastructure/
│       ├── persistence/           # entidad JPA y adapter
│       └── web/                   # controller REST y DTOs
└── common/                        # código transversal (no es un módulo de negocio)
    ├── cache/                     # CachePort y CacheKeys (API de caché compartida)
    ├── exception/                 # excepciones compartidas entre módulos
    ├── pagination/                # PageQuery y PageResult
    └── infrastructure/
        ├── redis/                 # JedisConfig (RedisClient) y JedisCache
        └── web/                   # GlobalExceptionHandler y PageResponse
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

| Pieza         | Tecnología                                         |
| ------------- | -------------------------------------------------- |
| Lenguaje      | Java 21                                            |
| Framework     | Spring Boot 4.1.1 (Spring MVC)                     |
| Build         | Maven 3.9 con Maven Wrapper                        |
| Persistencia  | PostgreSQL 17 con Spring Data JPA e Hibernate 6    |
| Esquema       | Hibernate `ddl-auto=update`                        |
| Caché         | Redis 7 con Jedis (RedisClient)                    |
| Documentación | springdoc-openapi (Swagger UI)                     |
| Monitoreo     | Spring Boot Actuator                               |
| Empaquetado   | Docker (imagen multi-stage, usuario no root)       |
| Arquitectura  | Hexagonal por módulos (dominio, aplicación, infra) |
