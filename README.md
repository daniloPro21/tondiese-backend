# Tondise E-commerce — Backend

Backend Spring Boot 3.5.4 / Java 21 pour le site e-commerce Tondise.

## Prérequis

- Java 21
- Maven 3.9+
- Docker (pour Postgres et MinIO en local, via `docker-compose.yml`)

## Démarrage rapide

```bash
docker compose up -d          # démarre Postgres + MinIO
export TONDISE_PROFILE=dev
export JWT_SECRET=change-me-in-dev-32-bytes-minimum
mvn spring-boot:run
```

L'API démarre sur `http://localhost:8080`. Documentation Swagger : `http://localhost:8080/swagger-ui.html`.

## Variables d'environnement

| Variable | Description | Profil |
|---|---|---|
| `TONDISE_PROFILE` | `dev` ou `prod` | tous |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | Connexion Postgres | tous |
| `JWT_SECRET` | Clé de signature JWT (HMAC, 32+ octets) | tous |
| `CORS_ALLOWED_ORIGINS` | Origines autorisées (front) | prod |
| `MINIO_URL`, `MINIO_ACCESS_KEY`, `MINIO_SECRET_KEY`, `MINIO_BUCKET` | Stockage fichiers | prod (dev utilise le stockage local) |
| `STRIPE_PUBLISHABLE_KEY`, `STRIPE_SECRET_KEY` | Paiement Stripe | tous |

En profil `dev`, le stockage de fichiers est local (`./uploads`) — pas besoin de MinIO pour développer.

## Base de données

Le schéma est géré par Liquibase (`src/main/resources/db/changelog`). Hibernate est en `ddl-auto: validate` — aucune migration automatique, tout changement de schéma passe par un nouveau changelog.

## Build & tests

```bash
mvn clean compile
mvn test          # nécessite une base Postgres accessible (voir docker-compose.yml)
```
