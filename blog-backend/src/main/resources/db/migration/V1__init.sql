-- src/main/resources/db/migration/V1__init.sql
SET NAMES utf8mb4;

-- 用户表
CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,
    email       VARCHAR(100) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    avatar      VARCHAR(255),
    bio         TEXT,
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 角色表
CREATE TABLE roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    code        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(200),
    status      TINYINT      NOT NULL DEFAULT 1,
    sort_order  INT          NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 菜单权限表
CREATE TABLE menus (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    code        VARCHAR(100) UNIQUE,
    type        ENUM('MENU','BUTTON','API') NOT NULL,
    parent_id   BIGINT REFERENCES menus(id),
    path        VARCHAR(200),
    component   VARCHAR(200),
    icon        VARCHAR(50),
    sort_order  INT          NOT NULL DEFAULT 0,
    status      TINYINT      NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户角色关联
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 角色菜单关联
CREATE TABLE role_menus (
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, menu_id),
    FOREIGN KEY (role_id) REFERENCES roles(id),
    FOREIGN KEY (menu_id) REFERENCES menus(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 分类表
CREATE TABLE categories (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)  NOT NULL,
    slug        VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(200),
    sort_order  INT          NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文章表
CREATE TABLE articles (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(200) NOT NULL,
    slug          VARCHAR(200) NOT NULL UNIQUE,
    summary       VARCHAR(500),
    content       LONGTEXT     NOT NULL,
    cover_image   VARCHAR(255),
    status        ENUM('DRAFT','PUBLISHED') NOT NULL DEFAULT 'DRAFT',
    view_count    INT          NOT NULL DEFAULT 0,
    is_top        BOOLEAN      NOT NULL DEFAULT FALSE,
    allow_comment BOOLEAN      NOT NULL DEFAULT TRUE,
    author_id     BIGINT       NOT NULL,
    category_id   BIGINT,
    published_at  DATETIME,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (author_id)   REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_status_published (status, published_at),
    INDEX idx_category (category_id),
    FULLTEXT INDEX ft_title_content (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 标签表
CREATE TABLE tags (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL UNIQUE,
    slug VARCHAR(30) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文章标签关联
CREATE TABLE article_tags (
    article_id BIGINT NOT NULL,
    tag_id     BIGINT NOT NULL,
    PRIMARY KEY (article_id, tag_id),
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id)     REFERENCES tags(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 评论表
CREATE TABLE comments (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    content    TEXT         NOT NULL,
    article_id BIGINT       NOT NULL,
    user_id    BIGINT,
    parent_id  BIGINT,
    nickname   VARCHAR(50),
    email      VARCHAR(100),
    status     ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (article_id) REFERENCES articles(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id)    REFERENCES users(id),
    FOREIGN KEY (parent_id)  REFERENCES comments(id),
    INDEX idx_article_status (article_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始数据：角色
INSERT INTO roles (name, code, description, sort_order) VALUES
('超级管理员', 'ADMIN', '拥有所有权限', 1),
('普通用户',   'USER',  '注册用户',    2);

-- 初始数据：菜单权限
INSERT INTO menus (name, code, type, sort_order) VALUES
('文章列表',   'article:list',    'BUTTON', 1),
('文章创建',   'article:create',  'BUTTON', 2),
('文章编辑',   'article:update',  'BUTTON', 3),
('文章删除',   'article:delete',  'BUTTON', 4),
('文章发布',   'article:publish', 'BUTTON', 5),
('评论列表',   'comment:list',    'BUTTON', 6),
('评论审核',   'comment:approve', 'BUTTON', 7),
('评论删除',   'comment:delete',  'BUTTON', 8),
('分类管理',   'category:manage', 'BUTTON', 9),
('标签管理',   'tag:manage',      'BUTTON', 10),
('用户列表',   'user:list',       'BUTTON', 11),
('角色管理',   'role:manage',     'BUTTON', 12),
('菜单管理',   'menu:manage',     'BUTTON', 13);

-- ADMIN 角色获得所有权限
INSERT INTO role_menus (role_id, menu_id)
SELECT 1, id FROM menus;
