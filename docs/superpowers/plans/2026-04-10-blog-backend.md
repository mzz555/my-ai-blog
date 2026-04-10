# 技术博客系统 — 后端实施计划（Spring Boot）

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标：** 实现技术博客系统完整后端 REST API，涵盖 RBAC 权限体系、文章/评论/分类/标签管理、文件上传、访问统计。

**架构：** Spring Boot 3 + Spring Security 6 + JWT 无状态认证；RBAC 用户-角色-菜单三表权限模型；Redis 缓存热数据和权限码；Flyway 管理数据库迁移。

**技术栈：** Java 17 · Spring Boot 3.2 · Spring Security 6 · Spring Data JPA · MySQL 8 · Redis 7 · MinIO · Flyway · Lombok · JUnit 5 · H2（测试用）

---

## 文件清单

```
blog-backend/
├── pom.xml
├── src/main/java/com/blog/
│   ├── BlogApplication.java
│   ├── common/
│   │   ├── Result.java
│   │   ├── PageResult.java
│   │   ├── PageRequest.java
│   │   └── GlobalExceptionHandler.java
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── RedisConfig.java
│   │   └── MinioConfig.java
│   ├── entity/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Menu.java
│   │   ├── Article.java
│   │   ├── Category.java
│   │   ├── Tag.java
│   │   └── Comment.java
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── RoleRepository.java
│   │   ├── MenuRepository.java
│   │   ├── ArticleRepository.java
│   │   ├── CategoryRepository.java
│   │   ├── TagRepository.java
│   │   └── CommentRepository.java
│   ├── dto/
│   │   ├── auth/LoginRequest.java
│   │   ├── auth/RegisterRequest.java
│   │   ├── auth/AuthResponse.java
│   │   ├── auth/UserInfoResponse.java
│   │   ├── article/ArticleCreateRequest.java
│   │   ├── article/ArticleUpdateRequest.java
│   │   ├── article/ArticleListResponse.java
│   │   ├── article/ArticleDetailResponse.java
│   │   ├── comment/CommentCreateRequest.java
│   │   ├── comment/CommentResponse.java
│   │   ├── category/CategoryRequest.java
│   │   └── tag/TagRequest.java
│   ├── security/
│   │   ├── JwtUtil.java
│   │   ├── JwtAuthFilter.java
│   │   └── UserDetailsServiceImpl.java
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── ArticleService.java
│   │   ├── CommentService.java
│   │   ├── CategoryService.java
│   │   ├── TagService.java
│   │   ├── UploadService.java
│   │   ├── StatsService.java
│   │   └── impl/
│   │       ├── AuthServiceImpl.java
│   │       ├── ArticleServiceImpl.java
│   │       ├── CommentServiceImpl.java
│   │       ├── CategoryServiceImpl.java
│   │       ├── TagServiceImpl.java
│   │       ├── UploadServiceImpl.java
│   │       └── StatsServiceImpl.java
│   └── controller/
│       ├── AuthController.java
│       ├── ArticleController.java
│       ├── CommentController.java
│       ├── CategoryController.java
│       ├── TagController.java
│       ├── UploadController.java
│       └── StatsController.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/V1__init.sql
└── src/test/java/com/blog/
    ├── security/JwtUtilTest.java
    ├── service/AuthServiceTest.java
    ├── service/ArticleServiceTest.java
    └── controller/AuthControllerTest.java
```

---

## Task 1: 项目初始化

**Files:**
- Create: `blog-backend/pom.xml`
- Create: `blog-backend/src/main/java/com/blog/BlogApplication.java`
- Create: `blog-backend/src/main/resources/application.yml`
- Create: `blog-backend/src/main/resources/application-dev.yml`
- Create: `blog-backend/src/main/resources/application-prod.yml`

- [ ] **Step 1: 创建 pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.4</version>
    </parent>
    <groupId>com.blog</groupId>
    <artifactId>blog-backend</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>blog-backend</name>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-redis</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-cache</artifactId></dependency>
        <dependency><groupId>com.mysql</groupId><artifactId>mysql-connector-j</artifactId><scope>runtime</scope></dependency>
        <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-mysql</artifactId></dependency>
        <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
        <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>0.12.3</version></dependency>
        <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>0.12.3</version><scope>runtime</scope></dependency>
        <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>0.12.3</version><scope>runtime</scope></dependency>
        <dependency><groupId>io.minio</groupId><artifactId>minio</artifactId><version>8.5.9</version></dependency>
        <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId><version>2.3.0</version></dependency>
        <dependency><groupId>com.github.slugify</groupId><artifactId>slugify</artifactId><version>3.0.6</version></dependency>
        <!-- Test -->
        <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
        <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
        <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>test</scope></dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin><groupId>org.springframework.boot</groupId><artifactId>spring-boot-maven-plugin</artifactId></plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: 创建 BlogApplication.java**

```java
// src/main/java/com/blog/BlogApplication.java
package com.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class BlogApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlogApplication.class, args);
    }
}
```

- [ ] **Step 3: 创建 application.yml**

```yaml
# src/main/resources/application.yml
spring:
  profiles:
    active: dev
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    open-in-view: false
  flyway:
    locations: classpath:db/migration
    baseline-on-migrate: true

jwt:
  secret: ${JWT_SECRET:dev-secret-key-at-least-256-bits-long-for-hs256-algorithm}
  access-token-expiry: 604800   # 7 days in seconds
  refresh-token-expiry: 2592000 # 30 days in seconds

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

- [ ] **Step 4: 创建 application-dev.yml**

```yaml
# src/main/resources/application-dev.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Shanghai
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: localhost
      port: 6379
  jpa:
    show-sql: true

minio:
  endpoint: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: blog
```

- [ ] **Step 5: 创建 application-prod.yml**

```yaml
# src/main/resources/application-prod.yml
spring:
  datasource:
    url: ${DB_URL}
    username: root
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  data:
    redis:
      host: ${REDIS_HOST:redis}
      port: 6379

minio:
  endpoint: ${MINIO_ENDPOINT}
  access-key: ${MINIO_ACCESS_KEY}
  secret-key: ${MINIO_SECRET_KEY}
  bucket: blog
```

- [ ] **Step 6: 验证项目能编译**

```bash
cd blog-backend
mvn compile
```

预期输出：`BUILD SUCCESS`

- [ ] **Step 7: 提交**

```bash
git add blog-backend/
git commit -m "初始化：Spring Boot 后端项目骨架"
```

---

## Task 2: 公共层（Result / 异常处理）

**Files:**
- Create: `src/main/java/com/blog/common/Result.java`
- Create: `src/main/java/com/blog/common/PageResult.java`
- Create: `src/main/java/com/blog/common/GlobalExceptionHandler.java`
- Create: `src/test/java/com/blog/common/ResultTest.java`

- [ ] **Step 1: 写失败测试**

```java
// src/test/java/com/blog/common/ResultTest.java
package com.blog.common;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResultTest {
    @Test
    void success_shouldReturnCode200AndData() {
        Result<String> result = Result.success("hello");
        assertEquals(200, result.getCode());
        assertEquals("hello", result.getData());
        assertNull(result.getMessage());
    }

    @Test
    void error_shouldReturnCodeAndMessage() {
        Result<Void> result = Result.error(400, "参数错误");
        assertEquals(400, result.getCode());
        assertEquals("参数错误", result.getMessage());
        assertNull(result.getData());
    }
}
```

- [ ] **Step 2: 运行测试，确认失败**

```bash
mvn test -pl blog-backend -Dtest=ResultTest
```

预期：`FAILED` — `Result cannot be resolved`

- [ ] **Step 3: 实现 Result.java**

```java
// src/main/java/com/blog/common/Result.java
package com.blog.common;

import lombok.Data;

@Data
public class Result<T> {
    private int code;
    private String message;
    private T data;

    private Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(200, null, data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, null, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
```

- [ ] **Step 4: 实现 PageResult.java**

```java
// src/main/java/com/blog/common/PageResult.java
package com.blog.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private long total;
    private int page;
    private int size;

    public static <T> PageResult<T> of(List<T> list, long total, int page, int size) {
        PageResult<T> r = new PageResult<>();
        r.setList(list);
        r.setTotal(total);
        r.setPage(page);
        r.setSize(size);
        return r;
    }
}
```

- [ ] **Step 5: 实现 GlobalExceptionHandler.java**

```java
// src/main/java/com/blog/common/GlobalExceptionHandler.java
package com.blog.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException ex) {
        FieldError field = ex.getBindingResult().getFieldErrors().get(0);
        return Result.error(400, field.getDefaultMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return Result.error(400, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException ex) {
        return Result.error(403, "没有权限");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex) {
        log.error("未处理异常", ex);
        return Result.error(500, "服务器内部错误");
    }
}
```

- [ ] **Step 6: 运行测试，确认通过**

```bash
mvn test -pl blog-backend -Dtest=ResultTest
```

预期：`PASSED`

- [ ] **Step 7: 提交**

```bash
git add blog-backend/src/
git commit -m "新增：公共响应封装 Result、PageResult、全局异常处理"
```

---

## Task 3: 数据库迁移脚本（Flyway）

**Files:**
- Create: `src/main/resources/db/migration/V1__init.sql`

- [ ] **Step 1: 创建 V1__init.sql**

```sql
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
    path        VARCHAR(200),
    component   VARCHAR(200),
    icon        VARCHAR(50),
    parent_id   BIGINT REFERENCES menus(id),
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
```

- [ ] **Step 2: 启动 MySQL（本地开发）并验证迁移**

```bash
# 确保 MySQL 已运行，数据库 blog 已存在
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS blog CHARACTER SET utf8mb4;"
cd blog-backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

预期：控制台出现 `Flyway` 迁移成功日志，无报错

- [ ] **Step 3: 提交**

```bash
git add blog-backend/src/main/resources/db/
git commit -m "新增：Flyway V1 数据库初始化脚本（10 张表 + 初始角色/权限数据）"
```

---

## Task 4: JPA 实体类

**Files:**
- Create: `src/main/java/com/blog/entity/User.java`
- Create: `src/main/java/com/blog/entity/Role.java`
- Create: `src/main/java/com/blog/entity/Menu.java`
- Create: `src/main/java/com/blog/entity/Article.java`
- Create: `src/main/java/com/blog/entity/Category.java`
- Create: `src/main/java/com/blog/entity/Tag.java`
- Create: `src/main/java/com/blog/entity/Comment.java`

- [ ] **Step 1: 创建 User.java**

```java
// src/main/java/com/blog/entity/User.java
package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data @Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    @Column(nullable = false)
    private String password;
    private String avatar;
    @Column(columnDefinition = "TEXT")
    private String bio;
    @Column(nullable = false)
    private Integer status = 1;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @PrePersist
    void prePersist() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }
}
```

- [ ] **Step 2: 创建 Role.java**

```java
// src/main/java/com/blog/entity/Role.java
package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Data @Entity @Table(name = "roles")
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(length = 200)
    private String description;
    @Column(nullable = false)
    private Integer status = 1;
    @Column(nullable = false)
    private Integer sortOrder = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_menus",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "menu_id"))
    private Set<Menu> menus;
}
```

- [ ] **Step 3: 创建 Menu.java**

```java
// src/main/java/com/blog/entity/Menu.java
package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "menus")
public class Menu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(unique = true, length = 100)
    private String code;  // nullable for pure nav menus
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuType type;
    @Column(length = 200)
    private String path;
    @Column(length = 200)
    private String component;
    @Column(length = 50)
    private String icon;
    private Long parentId;
    @Column(nullable = false)
    private Integer sortOrder = 0;
    @Column(nullable = false)
    private Integer status = 1;

    public enum MenuType { MENU, BUTTON, API }
}
```

- [ ] **Step 4: 创建 Category.java**

```java
// src/main/java/com/blog/entity/Category.java
package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false, unique = true, length = 50)
    private String slug;
    @Column(length = 200)
    private String description;
    @Column(nullable = false)
    private Integer sortOrder = 0;
}
```

- [ ] **Step 5: 创建 Tag.java**

```java
// src/main/java/com/blog/entity/Tag.java
package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "tags")
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String name;
    @Column(nullable = false, unique = true, length = 30)
    private String slug;
}
```

- [ ] **Step 6: 创建 Article.java**

```java
// src/main/java/com/blog/entity/Article.java
package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data @Entity @Table(name = "articles")
public class Article {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, unique = true, length = 200)
    private String slug;
    @Column(length = 500)
    private String summary;
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;
    @Column(length = 255)
    private String coverImage;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleStatus status = ArticleStatus.DRAFT;
    @Column(nullable = false)
    private Integer viewCount = 0;
    @Column(nullable = false)
    private Boolean isTop = false;
    @Column(nullable = false)
    private Boolean allowComment = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "article_tags",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;

    private LocalDateTime publishedAt;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist void prePersist() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  void preUpdate()  { updatedAt = LocalDateTime.now(); }

    public enum ArticleStatus { DRAFT, PUBLISHED }
}
```

- [ ] **Step 7: 创建 Comment.java**

```java
// src/main/java/com/blog/entity/Comment.java
package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "comments")
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // null = guest

    private Long parentId;
    @Column(length = 50)
    private String nickname;
    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status = CommentStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }

    public enum CommentStatus { PENDING, APPROVED, REJECTED }
}
```

- [ ] **Step 8: 验证实体映射**

```bash
# 启动应用，Flyway 验证 schema 与实体是否匹配（ddl-auto: validate）
cd blog-backend && mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

预期：应用正常启动，无 SchemaValidationException

- [ ] **Step 9: 提交**

```bash
git add blog-backend/src/main/java/com/blog/entity/
git commit -m "新增：JPA 实体类（User/Role/Menu/Article/Category/Tag/Comment）"
```

---

## Task 5: Repository 层

**Files:**
- Create: `src/main/java/com/blog/repository/UserRepository.java`
- Create: `src/main/java/com/blog/repository/RoleRepository.java`
- Create: `src/main/java/com/blog/repository/MenuRepository.java`
- Create: `src/main/java/com/blog/repository/ArticleRepository.java`
- Create: `src/main/java/com/blog/repository/CategoryRepository.java`
- Create: `src/main/java/com/blog/repository/TagRepository.java`
- Create: `src/main/java/com/blog/repository/CommentRepository.java`

- [ ] **Step 1: 创建 UserRepository.java**

```java
// src/main/java/com/blog/repository/UserRepository.java
package com.blog.repository;

import com.blog.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.menus WHERE u.username = :username")
    Optional<User> findByUsernameWithRolesAndMenus(String username);
}
```

- [ ] **Step 2: 创建 ArticleRepository.java**

```java
// src/main/java/com/blog/repository/ArticleRepository.java
package com.blog.repository;

import com.blog.entity.Article;
import com.blog.entity.Article.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findBySlugAndStatus(String slug, ArticleStatus status);
    Page<Article> findByStatus(ArticleStatus status, Pageable pageable);
    Page<Article> findByStatusAndCategoryId(ArticleStatus status, Long categoryId, Pageable pageable);

    @Query("SELECT a FROM Article a JOIN a.tags t WHERE a.status = 'PUBLISHED' AND t.slug = :tagSlug")
    Page<Article> findPublishedByTagSlug(String tagSlug, Pageable pageable);

    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + :count WHERE a.id = :id")
    void incrementViewCount(Long id, int count);
}
```

- [ ] **Step 3: 创建其余 Repository（一次性写完）**

```java
// src/main/java/com/blog/repository/RoleRepository.java
package com.blog.repository;
import com.blog.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByCode(String code);
}
```

```java
// src/main/java/com/blog/repository/MenuRepository.java
package com.blog.repository;
import com.blog.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByStatusOrderBySortOrder(Integer status);
}
```

```java
// src/main/java/com/blog/repository/CategoryRepository.java
package com.blog.repository;
import com.blog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
```

```java
// src/main/java/com/blog/repository/TagRepository.java
package com.blog.repository;
import com.blog.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findBySlug(String slug);
    List<Tag> findByNameIn(List<String> names);
}
```

```java
// src/main/java/com/blog/repository/CommentRepository.java
package com.blog.repository;
import com.blog.entity.Comment;
import com.blog.entity.Comment.CommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticleIdAndStatusOrderByCreatedAtAsc(Long articleId, CommentStatus status);
    Page<Comment> findByStatus(CommentStatus status, Pageable pageable);
}
```

- [ ] **Step 4: 提交**

```bash
git add blog-backend/src/main/java/com/blog/repository/
git commit -m "新增：Repository 层（7 个数据访问接口）"
```

---

## Task 6: JWT 工具类

**Files:**
- Create: `src/main/java/com/blog/security/JwtUtil.java`
- Create: `src/test/java/com/blog/security/JwtUtilTest.java`
- Create: `src/test/resources/application.yml`（测试配置）

- [ ] **Step 1: 创建测试配置**

```yaml
# src/test/resources/application.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    database-platform: org.hibernate.dialect.H2Dialect
  flyway:
    enabled: false  # 测试用 ddl-auto 建表

jwt:
  secret: test-secret-key-at-least-256-bits-long-for-hs256-algorithm-testing
  access-token-expiry: 604800
  refresh-token-expiry: 2592000
```

- [ ] **Step 2: 写失败测试**

```java
// src/test/java/com/blog/security/JwtUtilTest.java
package com.blog.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateAndValidateAccessToken() {
        String token = jwtUtil.generateAccessToken(1L, "testuser");
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(1L, jwtUtil.getUserId(token));
        assertEquals("testuser", jwtUtil.getUsername(token));
    }

    @Test
    void expiredTokenShouldBeInvalid() {
        String token = jwtUtil.generateTokenWithExpiry(1L, "testuser", -1000L);
        assertFalse(jwtUtil.validateToken(token));
    }
}
```

- [ ] **Step 3: 运行测试，确认失败**

```bash
mvn test -pl blog-backend -Dtest=JwtUtilTest
```

预期：`FAILED` — `JwtUtil` 未找到

- [ ] **Step 4: 实现 JwtUtil.java**

```java
// src/main/java/com/blog/security/JwtUtil.java
package com.blog.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {

    private final SecretKey key;
    private final long accessTokenExpiry;
    private final long refreshTokenExpiry;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiry}") long accessTokenExpiry,
            @Value("${jwt.refresh-token-expiry}") long refreshTokenExpiry) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiry = accessTokenExpiry * 1000;
        this.refreshTokenExpiry = refreshTokenExpiry * 1000;
    }

    public String generateAccessToken(Long userId, String username) {
        return generateTokenWithExpiry(userId, username, accessTokenExpiry);
    }

    public String generateRefreshToken(Long userId, String username) {
        return generateTokenWithExpiry(userId, username, refreshTokenExpiry);
    }

    public String generateTokenWithExpiry(Long userId, String username, long expiryMs) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expiryMs))
                .signWith(key)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("JWT 验证失败: {}", e.getMessage());
            return false;
        }
    }

    public String getUsername(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(key).build()
                .parseSignedClaims(token).getPayload();
    }
}
```

- [ ] **Step 5: 运行测试，确认通过**

```bash
mvn test -pl blog-backend -Dtest=JwtUtilTest
```

预期：`PASSED`

- [ ] **Step 6: 提交**

```bash
git add blog-backend/src/
git commit -m "新增：JWT 工具类，含 access/refresh token 生成与验证"
```

---

## Task 7: Spring Security 配置 + JWT 过滤器

**Files:**
- Create: `src/main/java/com/blog/security/UserDetailsServiceImpl.java`
- Create: `src/main/java/com/blog/security/JwtAuthFilter.java`
- Create: `src/main/java/com/blog/config/SecurityConfig.java`
- Create: `src/main/java/com/blog/config/RedisConfig.java`

- [ ] **Step 1: 创建 UserDetailsServiceImpl.java**

```java
// src/main/java/com/blog/security/UserDetailsServiceImpl.java
package com.blog.security;

import com.blog.entity.Menu;
import com.blog.entity.Role;
import com.blog.entity.User;
import com.blog.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameWithRolesAndMenus(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        Set<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(role -> role.getMenus().stream())
                .map(Menu::getCode)
                .filter(code -> code != null && !code.isBlank())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.getStatus() == 0)
                .credentialsExpired(false)
                .build();
    }
}
```

- [ ] **Step 2: 创建 JwtAuthFilter.java**

```java
// src/main/java/com/blog/security/JwtAuthFilter.java
package com.blog.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token != null && jwtUtil.validateToken(token)) {
            String username = jwtUtil.getUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        chain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
```

- [ ] **Step 3: 创建 SecurityConfig.java**

```java
// src/main/java/com/blog/config/SecurityConfig.java
package com.blog.config;

import com.blog.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/articles/**", "/api/categories",
                                 "/api/tags", "/api/articles/*/comments",
                                 "/sitemap.xml", "/robots.txt").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/articles/*/comments").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

- [ ] **Step 4: 创建 RedisConfig.java**

```java
// src/main/java/com/blog/config/RedisConfig.java
package com.blog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        return template;
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add blog-backend/src/main/java/com/blog/
git commit -m "新增：Spring Security 配置、JWT 过滤器、UserDetailsService（RBAC 权限加载）"
```

---

## Task 8: 认证 API（登录 / 注册 / 个人信息）

**Files:**
- Create: `src/main/java/com/blog/dto/auth/` （全部 DTO）
- Create: `src/main/java/com/blog/service/AuthService.java`
- Create: `src/main/java/com/blog/service/impl/AuthServiceImpl.java`
- Create: `src/main/java/com/blog/controller/AuthController.java`
- Create: `src/test/java/com/blog/controller/AuthControllerTest.java`

- [ ] **Step 1: 创建 DTO 类**

```java
// src/main/java/com/blog/dto/auth/LoginRequest.java
package com.blog.dto.auth;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data public class LoginRequest {
    @NotBlank(message = "用户名不能为空") private String username;
    @NotBlank(message = "密码不能为空")  private String password;
}
```

```java
// src/main/java/com/blog/dto/auth/RegisterRequest.java
package com.blog.dto.auth;
import jakarta.validation.constraints.*;
import lombok.Data;
@Data public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    @Size(min=3, max=50, message = "用户名长度 3-50 位")
    private String username;
    @NotBlank @Email(message = "邮箱格式不正确")
    private String email;
    @NotBlank
    @Size(min=6, max=100, message = "密码至少 6 位")
    private String password;
}
```

```java
// src/main/java/com/blog/dto/auth/AuthResponse.java
package com.blog.dto.auth;
import lombok.Builder;
import lombok.Data;
@Data @Builder public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
}
```

```java
// src/main/java/com/blog/dto/auth/UserInfoResponse.java
package com.blog.dto.auth;
import lombok.Builder;
import lombok.Data;
import java.util.List;
@Data @Builder public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    private List<String> roles;
    private List<String> permissions;
    private List<MenuNode> menus;

    @Data @Builder public static class MenuNode {
        private Long id;
        private String name;
        private String code;
        private String type;
        private String path;
        private String component;
        private String icon;
        private Long parentId;
        private Integer sortOrder;
    }
}
```

- [ ] **Step 2: 写 AuthService 接口**

```java
// src/main/java/com/blog/service/AuthService.java
package com.blog.service;

import com.blog.dto.auth.*;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    void register(RegisterRequest request);
    UserInfoResponse getCurrentUser(String username);
    AuthResponse refreshToken(String refreshToken);
}
```

- [ ] **Step 3: 写失败测试**

```java
// src/test/java/com/blog/controller/AuthControllerTest.java
package com.blog.controller;

import com.blog.dto.auth.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void login_withInvalidCredentials_shouldReturn400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("nobody");
        req.setPassword("wrongpass");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withBlankUsername_shouldReturn400() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("");
        req.setPassword("password");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }
}
```

- [ ] **Step 4: 运行测试，确认失败**

```bash
mvn test -pl blog-backend -Dtest=AuthControllerTest
```

预期：`FAILED` — `AuthController` 未找到

- [ ] **Step 5: 实现 AuthServiceImpl.java**

```java
// src/main/java/com/blog/service/impl/AuthServiceImpl.java
package com.blog.service.impl;

import com.blog.dto.auth.*;
import com.blog.entity.*;
import com.blog.repository.*;
import com.blog.security.JwtUtil;
import com.blog.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(user.getId(), user.getUsername()))
                .refreshToken(jwtUtil.generateRefreshToken(user.getId(), user.getUsername()))
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new IllegalArgumentException("用户名已存在");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("邮箱已被注册");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role userRole = roleRepository.findByCode("USER")
                .orElseThrow(() -> new IllegalStateException("USER 角色不存在，请检查初始化数据"));
        user.setRoles(Set.of(userRole));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser(String username) {
        User user = userRepository.findByUsernameWithRolesAndMenus(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        List<String> roles = user.getRoles().stream().map(Role::getCode).toList();
        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getMenus().stream())
                .map(Menu::getCode).filter(Objects::nonNull).distinct().toList();
        List<UserInfoResponse.MenuNode> menus = user.getRoles().stream()
                .flatMap(r -> r.getMenus().stream())
                .filter(m -> m.getStatus() == 1)
                .distinct()
                .map(m -> UserInfoResponse.MenuNode.builder()
                        .id(m.getId()).name(m.getName()).code(m.getCode())
                        .type(m.getType().name()).path(m.getPath())
                        .component(m.getComponent()).icon(m.getIcon())
                        .parentId(m.getParentId()).sortOrder(m.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        return UserInfoResponse.builder()
                .id(user.getId()).username(user.getUsername())
                .email(user.getEmail()).avatar(user.getAvatar())
                .bio(user.getBio()).roles(roles)
                .permissions(permissions).menus(menus)
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken))
            throw new IllegalArgumentException("refresh token 无效或已过期");
        String username = jwtUtil.getUsername(refreshToken);
        Long userId = jwtUtil.getUserId(refreshToken);
        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(userId, username))
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
}
```

- [ ] **Step 6: 实现 AuthController.java**

```java
// src/main/java/com/blog/controller/AuthController.java
package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.auth.*;
import com.blog.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<UserInfoResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(authService.getCurrentUser(userDetails.getUsername()));
    }

    @PostMapping("/refresh")
    public Result<AuthResponse> refresh(@RequestParam String refreshToken) {
        return Result.success(authService.refreshToken(refreshToken));
    }
}
```

- [ ] **Step 7: 运行测试，确认通过**

```bash
mvn test -pl blog-backend -Dtest=AuthControllerTest
```

预期：`PASSED`

- [ ] **Step 8: 提交**

```bash
git add blog-backend/src/
git commit -m "新增：认证模块（登录/注册/获取当前用户/刷新 Token）"
```

---

## Task 9: 分类 & 标签 API

**Files:**
- Create: `src/main/java/com/blog/dto/category/CategoryRequest.java`
- Create: `src/main/java/com/blog/dto/tag/TagRequest.java`
- Create: `src/main/java/com/blog/service/CategoryService.java` + impl
- Create: `src/main/java/com/blog/service/TagService.java` + impl
- Create: `src/main/java/com/blog/controller/CategoryController.java`
- Create: `src/main/java/com/blog/controller/TagController.java`

- [ ] **Step 1: 创建 DTO**

```java
// src/main/java/com/blog/dto/category/CategoryRequest.java
package com.blog.dto.category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data public class CategoryRequest {
    @NotBlank(message = "分类名不能为空") @Size(max = 50) private String name;
    @Size(max = 50) private String slug;     // 空时自动生成
    @Size(max = 200) private String description;
    private Integer sortOrder = 0;
}
```

```java
// src/main/java/com/blog/dto/tag/TagRequest.java
package com.blog.dto.tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data public class TagRequest {
    @NotBlank(message = "标签名不能为空") @Size(max = 30) private String name;
    @Size(max = 30) private String slug;
}
```

- [ ] **Step 2: 实现 CategoryService 接口 + 实现类**

```java
// src/main/java/com/blog/service/CategoryService.java
package com.blog.service;
import com.blog.dto.category.CategoryRequest;
import com.blog.entity.Category;
import java.util.List;
public interface CategoryService {
    List<Category> listAll();
    Category create(CategoryRequest request);
    Category update(Long id, CategoryRequest request);
    void delete(Long id);
}
```

```java
// src/main/java/com/blog/service/impl/CategoryServiceImpl.java
package com.blog.service.impl;

import com.blog.dto.category.CategoryRequest;
import com.blog.entity.Category;
import com.blog.repository.CategoryRepository;
import com.blog.service.CategoryService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service @RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final Slugify slugify = Slugify.builder().build();

    @Override public List<Category> listAll() {
        return categoryRepository.findAll();
    }

    @Override public Category create(CategoryRequest req) {
        String slug = resolveSlug(req.getSlug(), req.getName());
        if (categoryRepository.existsBySlug(slug))
            throw new IllegalArgumentException("分类 slug 已存在: " + slug);
        Category c = new Category();
        c.setName(req.getName()); c.setSlug(slug);
        c.setDescription(req.getDescription());
        c.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        return categoryRepository.save(c);
    }

    @Override public Category update(Long id, CategoryRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));
        c.setName(req.getName());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        if (req.getSortOrder() != null) c.setSortOrder(req.getSortOrder());
        return categoryRepository.save(c);
    }

    @Override public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    private String resolveSlug(String slug, String name) {
        return (slug != null && !slug.isBlank()) ? slug : slugify.slugify(name);
    }
}
```

- [ ] **Step 3: 实现 TagService + TagServiceImpl（同上模式）**

```java
// src/main/java/com/blog/service/TagService.java
package com.blog.service;
import com.blog.dto.tag.TagRequest;
import com.blog.entity.Tag;
import java.util.List;
public interface TagService {
    List<Tag> listAll();
    Tag create(TagRequest request);
    void delete(Long id);
}
```

```java
// src/main/java/com/blog/service/impl/TagServiceImpl.java
package com.blog.service.impl;

import com.blog.dto.tag.TagRequest;
import com.blog.entity.Tag;
import com.blog.repository.TagRepository;
import com.blog.service.TagService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service @RequiredArgsConstructor
public class TagServiceImpl implements TagService {
    private final TagRepository tagRepository;
    private final Slugify slugify = Slugify.builder().build();

    @Override public List<Tag> listAll() { return tagRepository.findAll(); }

    @Override public Tag create(TagRequest req) {
        String slug = (req.getSlug() != null && !req.getSlug().isBlank())
                ? req.getSlug() : slugify.slugify(req.getName());
        Tag t = new Tag();
        t.setName(req.getName()); t.setSlug(slug);
        return tagRepository.save(t);
    }

    @Override public void delete(Long id) { tagRepository.deleteById(id); }
}
```

- [ ] **Step 4: 实现 Controller**

```java
// src/main/java/com/blog/controller/CategoryController.java
package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.category.CategoryRequest;
import com.blog.entity.Category;
import com.blog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/categories") @RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> list() { return Result.success(categoryService.listAll()); }

    @PostMapping
    @PreAuthorize("hasAuthority('category:manage')")
    public Result<Category> create(@Valid @RequestBody CategoryRequest req) {
        return Result.success(categoryService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('category:manage')")
    public Result<Category> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        return Result.success(categoryService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id); return Result.success();
    }
}
```

```java
// src/main/java/com/blog/controller/TagController.java
package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.tag.TagRequest;
import com.blog.entity.Tag;
import com.blog.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/tags") @RequiredArgsConstructor
public class TagController {
    private final TagService tagService;

    @GetMapping
    public Result<List<Tag>> list() { return Result.success(tagService.listAll()); }

    @PostMapping
    @PreAuthorize("hasAuthority('tag:manage')")
    public Result<Tag> create(@Valid @RequestBody TagRequest req) {
        return Result.success(tagService.create(req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('tag:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id); return Result.success();
    }
}
```

- [ ] **Step 5: 提交**

```bash
git add blog-backend/src/
git commit -m "新增：分类和标签 CRUD API（含 slug 自动生成）"
```

---

## Task 10: 文章 API

**Files:**
- Create: `src/main/java/com/blog/dto/article/` （4 个 DTO）
- Create: `src/main/java/com/blog/service/ArticleService.java` + impl
- Create: `src/main/java/com/blog/controller/ArticleController.java`
- Create: `src/test/java/com/blog/service/ArticleServiceTest.java`

- [ ] **Step 1: 创建文章 DTO**

```java
// src/main/java/com/blog/dto/article/ArticleCreateRequest.java
package com.blog.dto.article;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;
@Data public class ArticleCreateRequest {
    @NotBlank(message = "标题不能为空") @Size(max = 200) private String title;
    @Size(max = 200) private String slug;
    @Size(max = 500) private String summary;
    @NotBlank(message = "内容不能为空") private String content;
    private String coverImage;
    private String status = "DRAFT";
    private Long categoryId;
    private List<String> tagNames;
    private Boolean isTop = false;
    private Boolean allowComment = true;
}
```

```java
// src/main/java/com/blog/dto/article/ArticleUpdateRequest.java
package com.blog.dto.article;
import lombok.Data;
import java.util.List;
@Data public class ArticleUpdateRequest {
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private Long categoryId;
    private List<String> tagNames;
    private Boolean isTop;
    private Boolean allowComment;
}
```

```java
// src/main/java/com/blog/dto/article/ArticleListResponse.java
package com.blog.dto.article;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data public class ArticleListResponse {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String coverImage;
    private Integer viewCount;
    private Boolean isTop;
    private LocalDateTime publishedAt;
    private String categoryName;
    private List<String> tagNames;
    private String authorName;
}
```

```java
// src/main/java/com/blog/dto/article/ArticleDetailResponse.java
package com.blog.dto.article;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data public class ArticleDetailResponse {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String coverImage;
    private Integer viewCount;
    private Boolean isTop;
    private Boolean allowComment;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private String categoryName;
    private Long categoryId;
    private List<String> tagNames;
    private String authorName;
    private String authorAvatar;
}
```

- [ ] **Step 2: 写失败测试**

```java
// src/test/java/com/blog/service/ArticleServiceTest.java
package com.blog.service;

import com.blog.dto.article.ArticleCreateRequest;
import com.blog.entity.*;
import com.blog.repository.*;
import com.blog.service.impl.ArticleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock ArticleRepository articleRepository;
    @Mock UserRepository userRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock TagRepository tagRepository;
    @InjectMocks ArticleServiceImpl articleService;

    private User testUser;

    @BeforeEach void setup() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
    }

    @Test
    void create_shouldSaveArticleWithSlugGenerated() {
        ArticleCreateRequest req = new ArticleCreateRequest();
        req.setTitle("Spring Boot 入门指南");
        req.setContent("内容...");

        Article saved = new Article();
        saved.setId(1L);
        saved.setTitle(req.getTitle());
        saved.setSlug("spring-boot-ru-men-zhi-nan");
        saved.setAuthor(testUser);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(articleRepository.save(any())).thenReturn(saved);

        var result = articleService.create(req, "testuser");
        assertNotNull(result);
        assertEquals("Spring Boot 入门指南", result.getTitle());
        verify(articleRepository).save(any(Article.class));
    }
}
```

- [ ] **Step 3: 运行测试，确认失败**

```bash
mvn test -pl blog-backend -Dtest=ArticleServiceTest
```

预期：`FAILED`

- [ ] **Step 4: 实现 ArticleService 接口**

```java
// src/main/java/com/blog/service/ArticleService.java
package com.blog.service;

import com.blog.common.PageResult;
import com.blog.dto.article.*;
import com.blog.entity.Article;

public interface ArticleService {
    Article create(ArticleCreateRequest request, String authorUsername);
    Article update(Long id, ArticleUpdateRequest request);
    void delete(Long id);
    void togglePublish(Long id);
    ArticleDetailResponse getBySlug(String slug);
    PageResult<ArticleListResponse> listPublished(int page, int size, Long categoryId, String tagSlug);
    PageResult<ArticleListResponse> listAll(int page, int size);  // admin
}
```

- [ ] **Step 5: 实现 ArticleServiceImpl.java**

```java
// src/main/java/com/blog/service/impl/ArticleServiceImpl.java
package com.blog.service.impl;

import com.blog.common.PageResult;
import com.blog.dto.article.*;
import com.blog.entity.*;
import com.blog.entity.Article.ArticleStatus;
import com.blog.repository.*;
import com.blog.service.ArticleService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final Slugify slugify = Slugify.builder().build();

    @Override @Transactional
    public Article create(ArticleCreateRequest req, String authorUsername) {
        User author = userRepository.findByUsername(authorUsername)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        Article article = new Article();
        article.setTitle(req.getTitle());
        article.setSlug(resolveSlug(req.getSlug(), req.getTitle()));
        article.setContent(req.getContent());
        article.setSummary(req.getSummary());
        article.setCoverImage(req.getCoverImage());
        article.setAuthor(author);
        article.setIsTop(req.getIsTop() != null ? req.getIsTop() : false);
        article.setAllowComment(req.getAllowComment() != null ? req.getAllowComment() : true);

        ArticleStatus status = "PUBLISHED".equals(req.getStatus())
                ? ArticleStatus.PUBLISHED : ArticleStatus.DRAFT;
        article.setStatus(status);
        if (status == ArticleStatus.PUBLISHED) article.setPublishedAt(LocalDateTime.now());

        if (req.getCategoryId() != null) {
            categoryRepository.findById(req.getCategoryId()).ifPresent(article::setCategory);
        }
        if (req.getTagNames() != null && !req.getTagNames().isEmpty()) {
            article.setTags(resolveTags(req.getTagNames()));
        }
        return articleRepository.save(article);
    }

    @Override @Transactional
    public Article update(Long id, ArticleUpdateRequest req) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        if (req.getTitle() != null) article.setTitle(req.getTitle());
        if (req.getSummary() != null) article.setSummary(req.getSummary());
        if (req.getContent() != null) article.setContent(req.getContent());
        if (req.getCoverImage() != null) article.setCoverImage(req.getCoverImage());
        if (req.getIsTop() != null) article.setIsTop(req.getIsTop());
        if (req.getAllowComment() != null) article.setAllowComment(req.getAllowComment());
        if (req.getCategoryId() != null)
            categoryRepository.findById(req.getCategoryId()).ifPresent(article::setCategory);
        if (req.getTagNames() != null)
            article.setTags(resolveTags(req.getTagNames()));
        return articleRepository.save(article);
    }

    @Override @Transactional
    public void delete(Long id) { articleRepository.deleteById(id); }

    @Override @Transactional
    public void togglePublish(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        if (article.getStatus() == ArticleStatus.DRAFT) {
            article.setStatus(ArticleStatus.PUBLISHED);
            if (article.getPublishedAt() == null) article.setPublishedAt(LocalDateTime.now());
        } else {
            article.setStatus(ArticleStatus.DRAFT);
        }
        articleRepository.save(article);
    }

    @Override @Transactional(readOnly = true)
    public ArticleDetailResponse getBySlug(String slug) {
        Article a = articleRepository.findBySlugAndStatus(slug, ArticleStatus.PUBLISHED)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        return toDetailResponse(a);
    }

    @Override @Transactional(readOnly = true)
    public PageResult<ArticleListResponse> listPublished(int page, int size, Long categoryId, String tagSlug) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("isTop").descending().and(Sort.by("publishedAt").descending()));
        Page<Article> pageData;
        if (tagSlug != null) {
            pageData = articleRepository.findPublishedByTagSlug(tagSlug, pageable);
        } else if (categoryId != null) {
            pageData = articleRepository.findByStatusAndCategoryId(ArticleStatus.PUBLISHED, categoryId, pageable);
        } else {
            pageData = articleRepository.findByStatus(ArticleStatus.PUBLISHED, pageable);
        }
        List<ArticleListResponse> list = pageData.getContent().stream().map(this::toListResponse).toList();
        return PageResult.of(list, pageData.getTotalElements(), page, size);
    }

    @Override @Transactional(readOnly = true)
    public PageResult<ArticleListResponse> listAll(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Article> pageData = articleRepository.findAll(pageable);
        return PageResult.of(pageData.getContent().stream().map(this::toListResponse).toList(),
                pageData.getTotalElements(), page, size);
    }

    private ArticleListResponse toListResponse(Article a) {
        ArticleListResponse r = new ArticleListResponse();
        r.setId(a.getId()); r.setTitle(a.getTitle()); r.setSlug(a.getSlug());
        r.setSummary(a.getSummary()); r.setCoverImage(a.getCoverImage());
        r.setViewCount(a.getViewCount()); r.setIsTop(a.getIsTop());
        r.setPublishedAt(a.getPublishedAt());
        r.setAuthorName(a.getAuthor().getUsername());
        if (a.getCategory() != null) r.setCategoryName(a.getCategory().getName());
        if (a.getTags() != null) r.setTagNames(a.getTags().stream().map(Tag::getName).toList());
        return r;
    }

    private ArticleDetailResponse toDetailResponse(Article a) {
        ArticleDetailResponse r = new ArticleDetailResponse();
        r.setId(a.getId()); r.setTitle(a.getTitle()); r.setSlug(a.getSlug());
        r.setSummary(a.getSummary()); r.setContent(a.getContent());
        r.setCoverImage(a.getCoverImage()); r.setViewCount(a.getViewCount());
        r.setIsTop(a.getIsTop()); r.setAllowComment(a.getAllowComment());
        r.setPublishedAt(a.getPublishedAt()); r.setCreatedAt(a.getCreatedAt());
        r.setAuthorName(a.getAuthor().getUsername());
        r.setAuthorAvatar(a.getAuthor().getAvatar());
        if (a.getCategory() != null) {
            r.setCategoryName(a.getCategory().getName());
            r.setCategoryId(a.getCategory().getId());
        }
        if (a.getTags() != null) r.setTagNames(a.getTags().stream().map(Tag::getName).toList());
        return r;
    }

    private Set<Tag> resolveTags(List<String> names) {
        List<Tag> existing = tagRepository.findByNameIn(names);
        Set<String> existingNames = existing.stream().map(Tag::getName).collect(Collectors.toSet());
        List<Tag> newTags = names.stream()
                .filter(n -> !existingNames.contains(n))
                .map(n -> { Tag t = new Tag(); t.setName(n); t.setSlug(slugify.slugify(n)); return t; })
                .map(tagRepository::save)
                .toList();
        Set<Tag> all = new HashSet<>(existing);
        all.addAll(newTags);
        return all;
    }

    private String resolveSlug(String slug, String title) {
        return (slug != null && !slug.isBlank()) ? slug : slugify.slugify(title);
    }
}
```

- [ ] **Step 6: 实现 ArticleController.java**

```java
// src/main/java/com/blog/controller/ArticleController.java
package com.blog.controller;

import com.blog.common.*;
import com.blog.dto.article.*;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/articles") @RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public Result<PageResult<ArticleListResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tagSlug) {
        return Result.success(articleService.listPublished(page, size, categoryId, tagSlug));
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasAuthority('article:list')")
    public Result<PageResult<ArticleListResponse>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(articleService.listAll(page, size));
    }

    @GetMapping("/{slug}")
    public Result<ArticleDetailResponse> detail(@PathVariable String slug) {
        return Result.success(articleService.getBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('article:create')")
    public Result<Article> create(@Valid @RequestBody ArticleCreateRequest req,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(articleService.create(req, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('article:update')")
    public Result<Article> update(@PathVariable Long id, @RequestBody ArticleUpdateRequest req) {
        return Result.success(articleService.update(id, req));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('article:publish')")
    public Result<Void> publish(@PathVariable Long id) {
        articleService.togglePublish(id); return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('article:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id); return Result.success();
    }
}
```

- [ ] **Step 7: 运行测试，确认通过**

```bash
mvn test -pl blog-backend -Dtest=ArticleServiceTest
```

预期：`PASSED`

- [ ] **Step 8: 提交**

```bash
git add blog-backend/src/
git commit -m "新增：文章 CRUD API（列表/详情/创建/编辑/发布/删除）"
```

---

## Task 11: 评论 API

**Files:**
- Create: `src/main/java/com/blog/dto/comment/CommentCreateRequest.java`
- Create: `src/main/java/com/blog/dto/comment/CommentResponse.java`
- Create: `src/main/java/com/blog/service/CommentService.java` + impl
- Create: `src/main/java/com/blog/controller/CommentController.java`

- [ ] **Step 1: 创建 DTO**

```java
// src/main/java/com/blog/dto/comment/CommentCreateRequest.java
package com.blog.dto.comment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data public class CommentCreateRequest {
    @NotBlank(message = "评论内容不能为空") @Size(max = 2000) private String content;
    private Long parentId;
    private String nickname;   // 游客填写
    private String email;      // 游客填写
}
```

```java
// src/main/java/com/blog/dto/comment/CommentResponse.java
package com.blog.dto.comment;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
@Data public class CommentResponse {
    private Long id;
    private String content;
    private String nickname;
    private String avatar;
    private Long parentId;
    private String status;
    private LocalDateTime createdAt;
    private List<CommentResponse> children;
}
```

- [ ] **Step 2: 实现 CommentService + CommentServiceImpl**

```java
// src/main/java/com/blog/service/CommentService.java
package com.blog.service;
import com.blog.common.PageResult;
import com.blog.dto.comment.*;
import com.blog.entity.Comment.CommentStatus;
import java.util.List;
public interface CommentService {
    void create(Long articleId, CommentCreateRequest request, String username);
    List<CommentResponse> listApprovedByArticle(Long articleId);
    PageResult<CommentResponse> listAllForAdmin(int page, int size);
    void updateStatus(Long id, CommentStatus status);
    void delete(Long id);
}
```

```java
// src/main/java/com/blog/service/impl/CommentServiceImpl.java
package com.blog.service.impl;

import com.blog.common.PageResult;
import com.blog.dto.comment.*;
import com.blog.entity.*;
import com.blog.entity.Comment.CommentStatus;
import com.blog.repository.*;
import com.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Override @Transactional
    public void create(Long articleId, CommentCreateRequest req, String username) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        if (!article.getAllowComment()) throw new IllegalArgumentException("该文章已关闭评论");

        Comment c = new Comment();
        c.setContent(req.getContent());
        c.setArticle(article);
        c.setParentId(req.getParentId());

        if (username != null) {
            userRepository.findByUsername(username).ifPresent(c::setUser);
        } else {
            c.setNickname(req.getNickname());
            c.setEmail(req.getEmail());
        }
        commentRepository.save(c);
    }

    @Override @Transactional(readOnly = true)
    public List<CommentResponse> listApprovedByArticle(Long articleId) {
        List<Comment> comments = commentRepository
                .findByArticleIdAndStatusOrderByCreatedAtAsc(articleId, CommentStatus.APPROVED);
        return buildTree(comments);
    }

    @Override @Transactional(readOnly = true)
    public PageResult<CommentResponse> listAllForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Comment> pageData = commentRepository.findAll(pageable);
        List<CommentResponse> list = pageData.getContent().stream().map(this::toResponse).toList();
        return PageResult.of(list, pageData.getTotalElements(), page, size);
    }

    @Override @Transactional
    public void updateStatus(Long id, CommentStatus status) {
        Comment c = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        c.setStatus(status);
        commentRepository.save(c);
    }

    @Override @Transactional
    public void delete(Long id) { commentRepository.deleteById(id); }

    private List<CommentResponse> buildTree(List<Comment> all) {
        Map<Long, CommentResponse> map = new LinkedHashMap<>();
        all.forEach(c -> map.put(c.getId(), toResponse(c)));
        List<CommentResponse> roots = new ArrayList<>();
        map.values().forEach(r -> {
            if (r.getParentId() == null) roots.add(r);
            else if (map.containsKey(r.getParentId())) {
                CommentResponse parent = map.get(r.getParentId());
                if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                parent.getChildren().add(r);
            }
        });
        return roots;
    }

    private CommentResponse toResponse(Comment c) {
        CommentResponse r = new CommentResponse();
        r.setId(c.getId()); r.setContent(c.getContent());
        r.setParentId(c.getParentId());
        r.setStatus(c.getStatus().name());
        r.setCreatedAt(c.getCreatedAt());
        if (c.getUser() != null) {
            r.setNickname(c.getUser().getUsername());
            r.setAvatar(c.getUser().getAvatar());
        } else {
            r.setNickname(c.getNickname());
        }
        return r;
    }
}
```

- [ ] **Step 3: 实现 CommentController.java**

```java
// src/main/java/com/blog/controller/CommentController.java
package com.blog.controller;

import com.blog.common.*;
import com.blog.dto.comment.*;
import com.blog.entity.Comment.CommentStatus;
import com.blog.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/articles/{articleId}/comments")
    public Result<List<CommentResponse>> list(@PathVariable Long articleId) {
        return Result.success(commentService.listApprovedByArticle(articleId));
    }

    @PostMapping("/api/articles/{articleId}/comments")
    public Result<Void> create(@PathVariable Long articleId,
                               @Valid @RequestBody CommentCreateRequest req,
                               @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        commentService.create(articleId, req, username);
        return Result.success();
    }

    @GetMapping("/api/comments/admin")
    @PreAuthorize("hasAuthority('comment:list')")
    public Result<PageResult<CommentResponse>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.listAllForAdmin(page, size));
    }

    @PutMapping("/api/comments/{id}/status")
    @PreAuthorize("hasAuthority('comment:approve')")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam CommentStatus status) {
        commentService.updateStatus(id, status); return Result.success();
    }

    @DeleteMapping("/api/comments/{id}")
    @PreAuthorize("hasAuthority('comment:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id); return Result.success();
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add blog-backend/src/
git commit -m "新增：评论 API（游客/登录评论、树形结构、管理员审核）"
```

---

## Task 12: 文件上传 API（MinIO）

**Files:**
- Create: `src/main/java/com/blog/config/MinioConfig.java`
- Create: `src/main/java/com/blog/service/UploadService.java` + impl
- Create: `src/main/java/com/blog/controller/UploadController.java`

- [ ] **Step 1: 创建 MinioConfig.java**

```java
// src/main/java/com/blog/config/MinioConfig.java
package com.blog.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {
    @Value("${minio.endpoint}") private String endpoint;
    @Value("${minio.access-key}") private String accessKey;
    @Value("${minio.secret-key}") private String secretKey;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
```

- [ ] **Step 2: 实现 UploadService + UploadServiceImpl**

```java
// src/main/java/com/blog/service/UploadService.java
package com.blog.service;
import org.springframework.web.multipart.MultipartFile;
public interface UploadService {
    String uploadImage(MultipartFile file);
}
```

```java
// src/main/java/com/blog/service/impl/UploadServiceImpl.java
package com.blog.service.impl;

import com.blog.service.UploadService;
import io.minio.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Slf4j @Service @RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final MinioClient minioClient;
    @Value("${minio.bucket}") private String bucket;
    @Value("${minio.endpoint}") private String endpoint;

    @Override
    public String uploadImage(MultipartFile file) {
        validateImage(file);
        String ext = getExtension(file.getOriginalFilename());
        String objectName = "images/" + UUID.randomUUID() + "." + ext;
        try {
            ensureBucketExists();
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build());
            return endpoint + "/" + bucket + "/" + objectName;
        } catch (Exception e) {
            log.error("MinIO 上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("文件不能为空");
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/"))
            throw new IllegalArgumentException("只允许上传图片文件");
        if (file.getSize() > 5 * 1024 * 1024)
            throw new IllegalArgumentException("图片大小不能超过 5MB");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private void ensureBucketExists() throws Exception {
        boolean exists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
        if (!exists) minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
    }
}
```

- [ ] **Step 3: 实现 UploadController.java**

```java
// src/main/java/com/blog/controller/UploadController.java
package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/upload") @RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/image")
    @PreAuthorize("isAuthenticated()")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        return Result.success(uploadService.uploadImage(file));
    }
}
```

- [ ] **Step 4: 提交**

```bash
git add blog-backend/src/
git commit -m "新增：图片上传 API（MinIO 存储，5MB 限制）"
```

---

## Task 13: 访问统计 API（Redis 计数器）

**Files:**
- Create: `src/main/java/com/blog/service/StatsService.java` + impl
- Create: `src/main/java/com/blog/controller/StatsController.java`

- [ ] **Step 1: 实现 StatsService**

```java
// src/main/java/com/blog/service/StatsService.java
package com.blog.service;
import java.util.Map;
public interface StatsService {
    void incrementViewCount(Long articleId);
    void syncViewCountsToDb();  // 定时任务调用
    Map<String, Object> getOverview();
}
```

```java
// src/main/java/com/blog/service/impl/StatsServiceImpl.java
package com.blog.service.impl;

import com.blog.repository.*;
import com.blog.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    private static final String VIEW_KEY_PREFIX = "article:view:";

    @Override
    public void incrementViewCount(Long articleId) {
        redisTemplate.opsForValue().increment(VIEW_KEY_PREFIX + articleId);
    }

    @Override
    @Scheduled(fixedDelay = 300_000) // 每 5 分钟同步一次
    @Transactional
    public void syncViewCountsToDb() {
        Set<String> keys = redisTemplate.keys(VIEW_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return;
        keys.forEach(key -> {
            Object val = redisTemplate.opsForValue().get(key);
            if (val != null) {
                Long articleId = Long.parseLong(key.replace(VIEW_KEY_PREFIX, ""));
                int count = Integer.parseInt(val.toString());
                articleRepository.incrementViewCount(articleId, count);
                redisTemplate.delete(key);
            }
        });
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalArticles", articleRepository.count());
        stats.put("totalComments", commentRepository.count());
        stats.put("totalUsers",    userRepository.count());
        return stats;
    }
}
```

- [ ] **Step 2: 实现 StatsController.java**

```java
// src/main/java/com/blog/controller/StatsController.java
package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController @RequestMapping("/api/stats") @RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> overview() {
        return Result.success(statsService.getOverview());
    }

    @PostMapping("/articles/{id}/view")
    public Result<Void> recordView(@PathVariable Long id) {
        statsService.incrementViewCount(id); return Result.success();
    }
}
```

- [ ] **Step 3: 在 BlogApplication 启用定时任务**

```java
// 在 BlogApplication.java 的 @SpringBootApplication 上方添加
@EnableScheduling
```

- [ ] **Step 4: 提交**

```bash
git add blog-backend/src/
git commit -m "新增：访问量统计 API（Redis 计数 + 定时同步至 MySQL）"
```

---

## Task 14: 全量集成测试 & 打包验证

- [ ] **Step 1: 运行全部测试**

```bash
cd blog-backend && mvn test
```

预期：所有测试 `PASSED`，无编译错误

- [ ] **Step 2: 打包**

```bash
mvn package -DskipTests
```

预期：`BUILD SUCCESS`，生成 `target/blog-backend-0.0.1-SNAPSHOT.jar`

- [ ] **Step 3: 验证 JAR 能启动**

```bash
java -jar target/blog-backend-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

预期：`Started BlogApplication`，访问 http://localhost:8080/swagger-ui.html 能看到 Swagger UI

- [ ] **Step 4: 最终提交**

```bash
git add .
git commit -m "后端完成：全量集成测试通过，JAR 打包验证成功"
```

---

## 自检结果

**规范覆盖检查：**
- ✅ RBAC 用户-角色-菜单 → Task 3（SQL）+ Task 4（实体）+ Task 7（Security）+ Task 8（login/me）
- ✅ 文章 CRUD + 发布/草稿 → Task 10
- ✅ 评论（游客+审核+树形）→ Task 11
- ✅ 分类/标签管理 → Task 9
- ✅ 文件上传 → Task 12
- ✅ 访问统计 → Task 13
- ✅ SEO slug → Task 10（`ArticleServiceImpl.resolveSlug`）
- ✅ JWT 认证 → Task 6 + Task 7
- ✅ Flyway 迁移 → Task 3
- ✅ Docker 就绪（Dockerfile 留给 Plan C）

**类型一致性检查：**
- `JwtUtil` 方法签名在 Task 6 定义，Task 7 和 Task 8 使用一致 ✅
- `ArticleService.create` 签名在接口 Task 10 定义，Controller 调用一致 ✅
- `Result<T>` 在 Task 2 定义，所有 Controller 使用一致 ✅
