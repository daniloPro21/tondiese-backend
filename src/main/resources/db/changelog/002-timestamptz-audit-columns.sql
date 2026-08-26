--liquibase formatted sql

--changeset tondise:019-users-timestamptz
-- AbstractEntity (tondise-util) type created/updated en Instant et mappe leurs colonnes en timestamptz ;
-- 001-initial-schema.sql les avait créées en timestamp (sans fuseau). Nouveau changeset plutôt que
-- modifier 001 déjà appliqué ailleurs (voir README : "tout changement de schéma passe par un nouveau changelog").
ALTER TABLE users ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE users ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:020-categories-timestamptz
ALTER TABLE categories ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE categories ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:021-products-timestamptz
ALTER TABLE products ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE products ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:022-product-options-timestamptz
ALTER TABLE product_options ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE product_options ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:023-product-option-values-timestamptz
ALTER TABLE product_option_values ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE product_option_values ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:024-pricing-tiers-timestamptz
ALTER TABLE pricing_tiers ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE pricing_tiers ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:025-promo-codes-timestamptz
ALTER TABLE promo_codes ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE promo_codes ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:026-carts-timestamptz
ALTER TABLE carts ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE carts ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:027-cart-items-timestamptz
ALTER TABLE cart_items ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE cart_items ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:028-favorites-timestamptz
ALTER TABLE favorites ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE favorites ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:029-addresses-timestamptz
ALTER TABLE addresses ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE addresses ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:030-orders-timestamptz
ALTER TABLE orders ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE orders ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:031-order-items-timestamptz
ALTER TABLE order_items ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE order_items ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:032-payment-transactions-timestamptz
ALTER TABLE payment_transactions ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE payment_transactions ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';

--changeset tondise:033-payment-gateway-configs-timestamptz
ALTER TABLE payment_gateway_configs ALTER COLUMN created_at TYPE timestamptz USING created_at AT TIME ZONE 'UTC';
ALTER TABLE payment_gateway_configs ALTER COLUMN updated_at TYPE timestamptz USING updated_at AT TIME ZONE 'UTC';
