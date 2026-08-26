--liquibase formatted sql

--changeset tondise:001-users
CREATE TABLE users (
    id                     UUID PRIMARY KEY,
    email                  VARCHAR(255) NOT NULL UNIQUE,
    password               VARCHAR(255) NOT NULL,
    first_name             VARCHAR(100) NOT NULL,
    last_name              VARCHAR(100) NOT NULL,
    phone                  VARCHAR(30),
    avatar                 VARCHAR(500),
    email_verified_at      TIMESTAMP,
    role                   VARCHAR(20) NOT NULL DEFAULT 'USER',
    enabled                BOOLEAN NOT NULL DEFAULT TRUE,
    reset_token            VARCHAR(255),
    reset_token_expires_at TIMESTAMP,
    created_at             TIMESTAMP NOT NULL,
    updated_at             TIMESTAMP NOT NULL,
    deleted                BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_users_email ON users (email);

--changeset tondise:002-categories
CREATE TABLE categories (
    id          UUID PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    slug        VARCHAR(150) NOT NULL UNIQUE,
    description TEXT,
    image       VARCHAR(500),
    parent_id   UUID REFERENCES categories (id),
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    deleted     BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_categories_slug ON categories (slug);
CREATE INDEX idx_categories_parent_id ON categories (parent_id);

--changeset tondise:003-products
CREATE TABLE products (
    id                 UUID PRIMARY KEY,
    name               VARCHAR(200) NOT NULL,
    slug               VARCHAR(200) NOT NULL UNIQUE,
    short_description  VARCHAR(500),
    description        TEXT,
    base_price         NUMERIC(12, 2) NOT NULL,
    main_image         VARCHAR(500),
    category_id        UUID REFERENCES categories (id),
    is_featured        BOOLEAN NOT NULL DEFAULT FALSE,
    stock_quantity     INTEGER NOT NULL DEFAULT 0,
    supports_design    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP NOT NULL,
    updated_at         TIMESTAMP NOT NULL,
    deleted            BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_products_slug ON products (slug);
CREATE INDEX idx_products_category_id ON products (category_id);

--changeset tondise:004-product-images
CREATE TABLE product_images (
    product_id UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    position   INTEGER NOT NULL,
    image_url  VARCHAR(500) NOT NULL,
    PRIMARY KEY (product_id, position)
);

--changeset tondise:005-product-options
CREATE TABLE product_options (
    id          UUID PRIMARY KEY,
    product_id  UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    name        VARCHAR(150) NOT NULL,
    type        VARCHAR(20) NOT NULL,
    is_required BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    deleted     BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_product_options_product_id ON product_options (product_id);

--changeset tondise:006-product-option-values
CREATE TABLE product_option_values (
    id             UUID PRIMARY KEY,
    option_id      UUID NOT NULL REFERENCES product_options (id) ON DELETE CASCADE,
    label          VARCHAR(150) NOT NULL,
    value          VARCHAR(150) NOT NULL,
    price_modifier NUMERIC(12, 2) NOT NULL DEFAULT 0,
    is_default     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at     TIMESTAMP NOT NULL,
    updated_at     TIMESTAMP NOT NULL,
    deleted        BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_product_option_values_option_id ON product_option_values (option_id);

--changeset tondise:007-pricing-tiers
CREATE TABLE pricing_tiers (
    id         UUID PRIMARY KEY,
    product_id UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    quantity   INTEGER NOT NULL,
    unit_price NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted    BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_pricing_tiers_product_id ON pricing_tiers (product_id);

--changeset tondise:008-promo-codes
CREATE TABLE promo_codes (
    id         UUID PRIMARY KEY,
    code       VARCHAR(50) NOT NULL UNIQUE,
    type       VARCHAR(20) NOT NULL,
    discount   NUMERIC(12, 2) NOT NULL,
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted    BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_promo_codes_code ON promo_codes (code);

--changeset tondise:009-carts
CREATE TABLE carts (
    id            UUID PRIMARY KEY,
    user_id       UUID NOT NULL UNIQUE REFERENCES users (id) ON DELETE CASCADE,
    promo_code_id UUID REFERENCES promo_codes (id),
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP NOT NULL,
    deleted       BOOLEAN NOT NULL DEFAULT FALSE
);

--changeset tondise:010-cart-items
CREATE TABLE cart_items (
    id          UUID PRIMARY KEY,
    cart_id     UUID NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    product_id  UUID NOT NULL REFERENCES products (id),
    quantity    INTEGER NOT NULL,
    design_id   UUID,
    unit_price  NUMERIC(12, 2) NOT NULL,
    total_price NUMERIC(12, 2) NOT NULL,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    deleted     BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);

--changeset tondise:011-cart-item-selected-options
CREATE TABLE cart_item_selected_options (
    cart_item_id    UUID NOT NULL REFERENCES cart_items (id) ON DELETE CASCADE,
    option_id       UUID NOT NULL,
    option_value_id UUID NOT NULL,
    PRIMARY KEY (cart_item_id, option_id)
);

--changeset tondise:012-favorites
CREATE TABLE favorites (
    id         UUID PRIMARY KEY,
    user_id    UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products (id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted    BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE UNIQUE INDEX idx_favorites_user_product ON favorites (user_id, product_id);

--changeset tondise:013-addresses
CREATE TABLE addresses (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    address     VARCHAR(255) NOT NULL,
    city        VARCHAR(100) NOT NULL,
    postal_code VARCHAR(20),
    country     VARCHAR(100) NOT NULL,
    phone       VARCHAR(30) NOT NULL,
    is_default  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,
    deleted     BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_addresses_user_id ON addresses (user_id);

--changeset tondise:014-orders
CREATE TABLE orders (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL REFERENCES users (id),
    order_number        VARCHAR(50) NOT NULL UNIQUE,
    status              VARCHAR(20) NOT NULL,
    payment_status      VARCHAR(20) NOT NULL,
    shipping_method     VARCHAR(100) NOT NULL,
    shipping_cost       NUMERIC(12, 2) NOT NULL,
    shipping_address_id UUID NOT NULL REFERENCES addresses (id),
    billing_address_id  UUID REFERENCES addresses (id),
    subtotal            NUMERIC(12, 2) NOT NULL,
    discount            NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total               NUMERIC(12, 2) NOT NULL,
    promo_code          VARCHAR(50),
    tracking_number     VARCHAR(100),
    notes               TEXT,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_orders_order_number ON orders (order_number);
CREATE INDEX idx_orders_user_id ON orders (user_id);

--changeset tondise:015-order-items
CREATE TABLE order_items (
    id               UUID PRIMARY KEY,
    order_id         UUID NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id       UUID REFERENCES products (id),
    product_name     VARCHAR(200) NOT NULL,
    quantity         INTEGER NOT NULL,
    unit_price       NUMERIC(12, 2) NOT NULL,
    total_price      NUMERIC(12, 2) NOT NULL,
    selected_options TEXT,
    created_at       TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP NOT NULL,
    deleted          BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);

--changeset tondise:016-payment-transactions
CREATE TABLE payment_transactions (
    id                  UUID PRIMARY KEY,
    order_id            UUID NOT NULL REFERENCES orders (id),
    gateway             VARCHAR(30) NOT NULL,
    provider_payment_id VARCHAR(255),
    client_secret       VARCHAR(255),
    amount              NUMERIC(12, 2) NOT NULL,
    currency            VARCHAR(10) NOT NULL DEFAULT 'XAF',
    status              VARCHAR(20) NOT NULL,
    phone_number        VARCHAR(30),
    failure_message     TEXT,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    deleted             BOOLEAN NOT NULL DEFAULT FALSE
);
CREATE INDEX idx_payment_tx_order_id ON payment_transactions (order_id);

--changeset tondise:017-payment-gateway-configs
CREATE TABLE payment_gateway_configs (
    id         UUID PRIMARY KEY,
    gateway    VARCHAR(30) NOT NULL UNIQUE,
    enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    config     TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted    BOOLEAN NOT NULL DEFAULT FALSE
);

--changeset tondise:018-payment-gateway-methods
CREATE TABLE payment_gateway_methods (
    gateway_config_id UUID NOT NULL REFERENCES payment_gateway_configs (id) ON DELETE CASCADE,
    method            VARCHAR(50) NOT NULL,
    PRIMARY KEY (gateway_config_id, method)
);
