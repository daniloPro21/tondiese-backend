--liquibase formatted sql

--changeset tondise:034-users-deleted-on
-- AbstractEntity (tondise-util) porte deletedOn (Instant, colonne deleted_on timestamptz),
-- posé par le hook @PreDestroy au soft-delete. 001-initial-schema.sql ne créait que `deleted`
-- (booléen) sans cette colonne. Nouveau changeset plutôt que modifier 001 déjà appliqué ailleurs.
ALTER TABLE users ADD COLUMN deleted_on timestamptz;

--changeset tondise:035-categories-deleted-on
ALTER TABLE categories ADD COLUMN deleted_on timestamptz;

--changeset tondise:036-products-deleted-on
ALTER TABLE products ADD COLUMN deleted_on timestamptz;

--changeset tondise:037-product-options-deleted-on
ALTER TABLE product_options ADD COLUMN deleted_on timestamptz;

--changeset tondise:038-product-option-values-deleted-on
ALTER TABLE product_option_values ADD COLUMN deleted_on timestamptz;

--changeset tondise:039-pricing-tiers-deleted-on
ALTER TABLE pricing_tiers ADD COLUMN deleted_on timestamptz;

--changeset tondise:040-promo-codes-deleted-on
ALTER TABLE promo_codes ADD COLUMN deleted_on timestamptz;

--changeset tondise:041-carts-deleted-on
ALTER TABLE carts ADD COLUMN deleted_on timestamptz;

--changeset tondise:042-cart-items-deleted-on
ALTER TABLE cart_items ADD COLUMN deleted_on timestamptz;

--changeset tondise:043-favorites-deleted-on
ALTER TABLE favorites ADD COLUMN deleted_on timestamptz;

--changeset tondise:044-addresses-deleted-on
ALTER TABLE addresses ADD COLUMN deleted_on timestamptz;

--changeset tondise:045-orders-deleted-on
ALTER TABLE orders ADD COLUMN deleted_on timestamptz;

--changeset tondise:046-order-items-deleted-on
ALTER TABLE order_items ADD COLUMN deleted_on timestamptz;

--changeset tondise:047-payment-transactions-deleted-on
ALTER TABLE payment_transactions ADD COLUMN deleted_on timestamptz;

--changeset tondise:048-payment-gateway-configs-deleted-on
ALTER TABLE payment_gateway_configs ADD COLUMN deleted_on timestamptz;
