--liquibase formatted sql

--changeset tondise:049-users-keycloak-user-id
-- users.password/role/enabled/reset_token* (001) datent d'avant le passage à Keycloak :
-- User n'a plus ces champs, il porte keycloakUserId (colonne manquante, requise par
-- CurrentUserResolver/AuthService pour réconcilier le JWT avec la ligne locale).
-- Les colonnes obsolètes ne sont pas supprimées ici (hors scope de ce changeset,
-- ddl-auto=validate ne les vérifie pas) — ménage à faire séparément.
ALTER TABLE users ADD COLUMN keycloak_user_id VARCHAR(255) NOT NULL;
CREATE UNIQUE INDEX idx_users_keycloak_user_id ON users (keycloak_user_id);
