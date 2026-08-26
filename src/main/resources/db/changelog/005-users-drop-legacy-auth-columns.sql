--liquibase formatted sql

--changeset tondise:050-users-drop-legacy-auth-columns
-- users.password/role/enabled/reset_token/reset_token_expires_at (001) datent d'avant Keycloak.
-- User n'a plus aucun de ces champs (confirmé : aucune référence dans dao/models/User.java ni
-- AuthService.java en dehors de RegisterRequest.getPassword()) ; password NOT NULL sans défaut
-- fait échouer tout INSERT réel (voir AuthService.register), pas seulement ddl-auto=validate.
ALTER TABLE users DROP COLUMN password;
ALTER TABLE users DROP COLUMN role;
ALTER TABLE users DROP COLUMN enabled;
ALTER TABLE users DROP COLUMN reset_token;
ALTER TABLE users DROP COLUMN reset_token_expires_at;
