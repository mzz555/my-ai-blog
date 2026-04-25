-- H2-compatible schema for integration tests

CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    email       VARCHAR(100) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    avatar      VARCHAR(255),
    bio         TEXT,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    code        VARCHAR(50)  NOT NULL,
    description VARCHAR(200),
    status      TINYINT      NOT NULL DEFAULT 1,
    sort_order  INT          NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS menus (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    code        VARCHAR(100),
    type        VARCHAR(10)  NOT NULL DEFAULT 'BUTTON',
    parent_id   BIGINT,
    path        VARCHAR(200),
    component   VARCHAR(200),
    icon        VARCHAR(50),
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS role_menus (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id)
);

CREATE TABLE IF NOT EXISTS categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    slug        VARCHAR(50)  NOT NULL,
    description VARCHAR(200),
    sort_order  INT          NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS tags (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    slug VARCHAR(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS articles (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    slug          VARCHAR(200) NOT NULL,
    summary       VARCHAR(500),
    content       CLOB         NOT NULL,
    cover_image   VARCHAR(255),
    status        VARCHAR(20)  NOT NULL DEFAULT 'DRAFT',
    view_count    INT          NOT NULL DEFAULT 0,
    is_top        BOOLEAN      NOT NULL DEFAULT FALSE,
    allow_comment BOOLEAN      NOT NULL DEFAULT TRUE,
    author_id     BIGINT       NOT NULL,
    category_id   BIGINT,
    published_at  DATETIME,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS article_tags (
    article_id BIGINT NOT NULL,
    tag_id     BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id)
);

CREATE TABLE IF NOT EXISTS comments (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    content    TEXT         NOT NULL,
    article_id BIGINT       NOT NULL,
    user_id    BIGINT,
    parent_id  BIGINT,
    nickname   VARCHAR(50),
    email      VARCHAR(100),
    status     VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);
