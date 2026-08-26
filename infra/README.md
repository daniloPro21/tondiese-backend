# Infra locale — Redis, Keycloak, MinIO

Ce dossier compose les trois dépendances externes de `tondise-backend` pour le
développement local : cache/sessions (Redis), identité (Keycloak), stockage
fichiers (MinIO). Le Postgres applicatif reste dans le `docker-compose.yml` à
la racine du projet — celui-ci ne gère que l'infra d'identité/stockage.

## Démarrer

```bash
docker compose -f infra/docker-compose.yml up -d
```

Au premier démarrage, Keycloak importe automatiquement `keycloak/realms/tondise-realm.json`
(realm `tondise`, rôles `USER`/`ADMIN`, clients `tondise-backend` et `tondise-web`,
un compte `admin@tondise.local` / `admin123` avec le rôle `ADMIN` pour tester
les routes `/admin/**`).

## Accès

- Keycloak admin console : http://localhost:8080 (admin / admin)
- Realm applicatif : http://localhost:8080/realms/tondise
- MinIO console : http://localhost:9001 (tondise / tondise123)
- Redis : localhost:6379

## Correspondance avec `application-dev.yml`

Les variables d'environnement de `application-dev.yml` doivent correspondre au
realm importé (valeurs déjà alignées par défaut) :

| Variable | Valeur locale |
|---|---|
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8080/realms/tondise` |
| `KEYCLOAK_REALM` | `tondise` |
| `KEYCLOAK_AUTH_SERVER_URL` | `http://localhost:8080` |
| `KEYCLOAK_CLIENT_ID` | `tondise-backend` |
| `KEYCLOAK_CLIENT_SECRET` | `changeme-dev-secret` (doit matcher le `secret` du client dans `tondise-realm.json`) |
| `KEYCLOAK_PUBLIC_CLIENT_ID` | `tondise-web` |
| `MINIO_URL` | `http://localhost:9000` |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | `tondise` / `tondise123` |
| `REDIS_HOST` / `REDIS_PORT` | `localhost` / `6379` |

## Notes

- `tondise-backend` (client confidentiel) a un compte de service avec le rôle
  `manage-users` sur `realm-management`, nécessaire pour que
  `KeycloakService.createKeycloakUser` / `resetPassword` (tondise-util)
  fonctionnent via l'API admin Keycloak.
- Le secret client `changeme-dev-secret` est acceptable en développement local
  uniquement — à régénérer et injecter par variable d'environnement en
  production (jamais commité).
- Pour repartir de zéro : `docker compose -f infra/docker-compose.yml down -v`
  (supprime aussi les volumes Postgres-Keycloak et MinIO).
