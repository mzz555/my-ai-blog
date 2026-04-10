# 技术博客系统设计规范

**日期：** 2026-04-10
**状态：** 已确认
**作者：** 新毕业程序员个人项目

---

## 一、项目背景与目标

### 背景
刚毕业的 Java 后端开发者，希望通过搭建个人技术博客：
- 驱动持续学习与知识沉淀
- 扩大技术影响力，建立个人品牌
- 在职业发展中形成差异化优势

### 目标用户
- **访客**：潜在雇主 / HR、同行开发者、初学者——全覆盖
- **管理员**：博主本人（唯一管理员）

### 内容定位
混合型技术博客：学习笔记 + 技术教程 + 项目实战案例

---

## 二、技术选型（方案 B）

| 层级 | 技术 | 版本 | 说明 |
|------|------|------|------|
| 前端框架 | Vue.js | 3.x | Composition API，现代主流 |
| 构建工具 | Vite | 5.x | 快速热重载，生产构建高效 |
| UI 组件库 | Element Plus | 2.x | 成熟的 Vue3 企业组件库 |
| 状态管理 | Pinia | 2.x | Vue3 官方推荐，轻量 |
| 路由 | Vue Router | 4.x | 支持动态路由注册 |
| Markdown 编辑器 | md-editor-v3 | — | 支持实时预览 + 图片上传 |
| HTTP 请求 | Axios | — | 统一封装拦截器 |
| 后端框架 | Spring Boot | 3.x | Java 17+，主流企业级框架 |
| 安全框架 | Spring Security + JWT | — | 无状态认证，前后端分离标配 |
| ORM | Spring Data JPA + Hibernate | — | 实体管理，复杂查询补充 JPQL |
| 缓存 | Spring Cache + Redis | 7.x | 热文列表、权限、访问计数 |
| 数据库 | MySQL | 8.0 | 主数据存储 |
| 对象存储 | MinIO（自托管） | — | 图片/文件上传，可替换为阿里云 OSS |
| DB 迁移 | Flyway | — | SQL 脚本版本化管理 |
| 接口文档 | Swagger / OpenAPI | 3.x | 自动生成 API 文档 |
| 反向代理 | Nginx | alpine | SSL 卸载 + 静态文件服务 |
| 容器化 | Docker + Docker Compose | — | 一键启动全部服务 |

---

## 三、系统架构

```
用户（浏览器）
      │ HTTPS
      ▼
  Nginx（反向代理 + SSL）
  ├── /           → Vue.js 静态文件（dist/）
  └── /api/**     → Spring Boot（8080 端口）
      ├── MySQL（数据持久化）
      ├── Redis（缓存 + Token）
      └── MinIO（文件存储）

所有服务由 Docker Compose 统一编排，部署于阿里云/腾讯云 ECS
```

---

## 四、数据库设计（10 张核心表）

### 4.1 业务表

#### users（用户表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键，自增 |
| username | VARCHAR(50) UNIQUE | 用户名 |
| email | VARCHAR(100) UNIQUE | 邮箱 |
| password | VARCHAR(255) | BCrypt 加密 |
| avatar | VARCHAR(255) | 头像 URL |
| bio | TEXT | 个人简介 |
| status | TINYINT | 0 禁用 / 1 启用 |
| created_at | DATETIME | 注册时间 |
| updated_at | DATETIME | 更新时间 |

#### articles（文章表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| title | VARCHAR(200) | 文章标题 |
| slug | VARCHAR(200) UNIQUE | SEO 友好 URL 标识 |
| summary | VARCHAR(500) | 摘要（SEO meta description）|
| content | LONGTEXT | Markdown 正文 |
| cover_image | VARCHAR(255) | 封面图 URL |
| status | ENUM('DRAFT','PUBLISHED') | 发布状态 |
| view_count | INT DEFAULT 0 | 阅读量（异步同步自 Redis）|
| is_top | BOOLEAN DEFAULT FALSE | 是否置顶 |
| allow_comment | BOOLEAN DEFAULT TRUE | 是否允许评论 |
| author_id | BIGINT FK → users.id | 作者 |
| category_id | BIGINT FK → categories.id | 分类 |
| published_at | DATETIME | 发布时间 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

#### categories（分类表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| name | VARCHAR(50) | 分类名 |
| slug | VARCHAR(50) UNIQUE | URL 标识 |
| description | VARCHAR(200) | 描述 |
| sort_order | INT | 排序 |

#### tags（标签表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| name | VARCHAR(30) UNIQUE | 标签名 |
| slug | VARCHAR(30) UNIQUE | URL 标识 |

#### article_tags（文章-标签关联，多对多）
| 字段 | 类型 | 说明 |
|------|------|------|
| article_id | BIGINT FK | → articles.id |
| tag_id | BIGINT FK | → tags.id |

#### comments（评论表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| content | TEXT | 评论内容 |
| article_id | BIGINT FK | → articles.id |
| user_id | BIGINT FK NULL | → users.id（NULL = 游客）|
| parent_id | BIGINT FK NULL | → comments.id（楼层回复）|
| nickname | VARCHAR(50) | 游客昵称 |
| email | VARCHAR(100) | 游客邮箱（通知用）|
| status | ENUM('PENDING','APPROVED','REJECTED') | 审核状态 |
| created_at | DATETIME | 评论时间 |

### 4.2 权限表（RBAC 用户-角色-菜单）

#### roles（角色表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| name | VARCHAR(50) | 角色名（超级管理员）|
| code | VARCHAR(50) UNIQUE | 角色标识（ADMIN/USER）|
| description | VARCHAR(200) | 描述 |
| status | TINYINT | 0 禁用 / 1 启用 |
| sort_order | INT | 排序 |

#### menus（菜单权限表）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 主键 |
| name | VARCHAR(50) | 菜单/按钮名称 |
| code | VARCHAR(100) UNIQUE NULL | 权限码（article:create）；纯导航菜单可为 NULL |
| type | ENUM('MENU','BUTTON','API') | 类型 |
| path | VARCHAR(200) | 前端路由 / 接口路径 |
| component | VARCHAR(200) | Vue 组件路径 |
| icon | VARCHAR(50) | 图标 |
| parent_id | BIGINT FK NULL | → menus.id（树形结构）|
| sort_order | INT | 排序 |
| status | TINYINT | 0 禁用 / 1 启用 |

#### user_roles（用户-角色关联，多对多）
| 字段 | 说明 |
|------|------|
| user_id FK | → users.id |
| role_id FK | → roles.id |

#### role_menus（角色-菜单关联，多对多）
| 字段 | 说明 |
|------|------|
| role_id FK | → roles.id |
| menu_id FK | → menus.id |

### 4.3 权限码规范

格式：`模块:操作`

| 模块 | 权限码 |
|------|------|
| 文章 | `article:list` `article:create` `article:update` `article:delete` `article:publish` |
| 评论 | `comment:list` `comment:approve` `comment:reject` `comment:delete` |
| 分类标签 | `category:manage` `tag:manage` |
| 用户 | `user:list` `user:create` `user:update` |
| 角色菜单 | `role:manage` `menu:manage` |
| 系统 | `system:config` |

---

## 五、API 设计

### 认证模块 `/api/auth`
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | /api/auth/login | 登录，返回 JWT Token | 公开 |
| POST | /api/auth/register | 注册 | 公开 |
| GET | /api/auth/me | 获取当前用户信息 + 菜单树 | 登录 |
| POST | /api/auth/refresh | 刷新 Token | 登录 |
| POST | /api/auth/logout | 登出（Redis 注销 Token）| 登录 |

### 文章模块 `/api/articles`
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/articles | 文章列表（分页+筛选） | 公开 |
| GET | /api/articles/{slug} | 文章详情 | 公开 |
| POST | /api/articles | 创建文章 | article:create |
| PUT | /api/articles/{id} | 更新文章 | article:update |
| DELETE | /api/articles/{id} | 删除文章 | article:delete |
| PUT | /api/articles/{id}/publish | 发布/撤回 | article:publish |

### 评论模块 `/api/comments`
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/articles/{id}/comments | 文章评论（树形）| 公开 |
| POST | /api/articles/{id}/comments | 发布评论（游客/登录）| 公开 |
| PUT | /api/comments/{id}/status | 审核评论 | comment:approve |
| DELETE | /api/comments/{id} | 删除评论 | comment:delete |

### 其他模块
| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | /api/categories | 分类列表 | 公开 |
| GET | /api/tags | 标签列表 | 公开 |
| POST | /api/upload/image | 上传图片 | 登录 |
| GET | /api/stats/overview | 仪表盘统计 | 登录 |
| GET | /api/menus/tree | 获取菜单树 | 登录 |

---

## 六、前端页面结构

### 前台（访客可见）
| 路由 | 页面 | 说明 |
|------|------|------|
| `/` | 首页 | 最新文章列表 + 侧边栏（分类/标签/关于）|
| `/posts` | 文章列表 | 分页 + 分类/标签筛选 |
| `/posts/:slug` | 文章详情 | Markdown 渲染 + 目录 + 评论区 |
| `/category/:slug` | 分类页 | 该分类下的文章列表 |
| `/tag/:slug` | 标签页 | 该标签下的文章列表 |
| `/archive` | 归档页 | 按年月时间轴展示所有文章 |
| `/about` | 关于我 | 个人介绍、技术栈、联系方式 |

### 后台（需登录 + 权限）
| 路由 | 页面 | 所需权限 |
|------|------|------|
| `/admin` | 仪表盘（文章/评论/访问量统计）| 登录 |
| `/admin/articles` | 文章管理列表 | article:list |
| `/admin/articles/new` | 写文章（Markdown 编辑器）| article:create |
| `/admin/articles/:id/edit` | 编辑文章 | article:update |
| `/admin/comments` | 评论审核 | comment:list |
| `/admin/categories` | 分类管理 | category:manage |
| `/admin/tags` | 标签管理 | tag:manage |
| `/admin/users` | 用户管理 | user:list |
| `/admin/roles` | 角色管理 | role:manage |
| `/admin/menus` | 菜单权限配置 | menu:manage |
| `/admin/profile` | 个人资料设置 | 登录 |

### 动态菜单实现
1. 登录后调用 `/api/auth/me` 返回用户菜单树
2. Vue Router 调用 `addRoute()` 动态注册后台路由
3. 侧边栏菜单由接口数据驱动渲染
4. 按钮级权限用 `v-permission` 自定义指令控制显隐

---

## 七、权限认证流程

```
登录请求
  → 验证用户名密码（BCrypt）
  → 查询用户角色 → 角色关联权限码列表
  → 生成 JWT（payload 含 userId + 权限码列表）
  → 权限码列表同步缓存至 Redis（key: user:{id}:perms，TTL 7 天）
  → 返回 accessToken（7 天）+ refreshToken（30 天）

接口请求
  → JWT Filter 解析 Token
  → 从 Redis 加载权限码列表（避免每次查库）
  → Spring Security @PreAuthorize("hasAuthority('xxx')") 鉴权
```

---

## 八、SEO 策略

| 策略 | 实现方式 |
|------|------|
| 语义化 URL | `/posts/spring-boot-intro` 替代 `/posts/123` |
| 动态 Meta | `useHead()` 为每篇文章注入 title / description |
| Open Graph | 微信/微博分享预览卡片 |
| sitemap.xml | 后端定时生成，搜索引擎主动推送 |
| robots.txt | 引导爬虫，屏蔽后台路由 |
| JSON-LD | 文章页结构化数据（Article Schema）|

---

## 九、项目目录结构

```
tech-blog/
├── blog-backend/                    # Spring Boot 项目
│   ├── src/main/java/com/blog/
│   │   ├── BlogApplication.java
│   │   ├── config/                  # SecurityConfig / RedisConfig / SwaggerConfig
│   │   ├── controller/              # REST 接口层
│   │   ├── service/impl/            # 业务逻辑实现
│   │   ├── repository/              # JPA Repository
│   │   ├── entity/                  # 数据库实体
│   │   ├── dto/                     # 请求/响应 DTO
│   │   ├── security/                # JwtFilter / UserDetailsServiceImpl
│   │   └── common/                  # Result / GlobalExceptionHandler / PageResult
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   ├── application-prod.yml
│   │   └── db/migration/            # Flyway SQL 脚本（V1__init.sql ...）
│   └── Dockerfile
├── blog-frontend/                   # Vue.js 3 项目
│   ├── src/
│   │   ├── router/                  # 路由配置 + 导航守卫
│   │   ├── stores/                  # Pinia（userStore / appStore）
│   │   ├── api/                     # Axios 封装（request.js + 各模块 api）
│   │   ├── views/front/             # 前台页面
│   │   ├── views/admin/             # 后台管理页面
│   │   ├── components/              # 公共组件
│   │   ├── directives/              # v-permission 指令
│   │   └── utils/                   # auth.js / format.js
│   ├── .env.development
│   ├── .env.production
│   ├── vite.config.js
│   └── Dockerfile
├── nginx/
│   └── nginx.conf                   # 反向代理 + SSL 配置
├── docker-compose.yml               # 开发/生产编排
├── .env.example                     # 敏感配置模板（不提交 .env 本身）
└── docs/
    └── superpowers/specs/
        └── 2026-04-10-blog-design.md  # 本文件
```

---

## 十、Docker Compose 部署

```yaml
version: '3.8'
services:
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./blog-frontend/dist:/usr/share/nginx/html
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - backend

  backend:
    build: ./blog-backend
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_URL=jdbc:mysql://mysql:3306/blog
      - DB_PASSWORD=${DB_PASSWORD}
      - REDIS_HOST=redis
      - JWT_SECRET=${JWT_SECRET}
      - MINIO_ENDPOINT=http://minio:9000
      - MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY}
      - MINIO_SECRET_KEY=${MINIO_SECRET_KEY}
    depends_on:
      - mysql
      - redis
    restart: unless-stopped

  mysql:
    image: mysql:8.0
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
      MYSQL_DATABASE: blog
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    volumes:
      - redis_data:/data

  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: ${MINIO_ACCESS_KEY}
      MINIO_ROOT_PASSWORD: ${MINIO_SECRET_KEY}
    volumes:
      - minio_data:/data

volumes:
  mysql_data:
  redis_data:
  minio_data:
```

### 上线步骤
1. 购买 ECS（2核4G 起步），安装 Docker + Docker Compose
2. 域名解析到服务器 IP，申请 SSL 证书（Let's Encrypt / 阿里云免费证书）
3. `git clone` 项目，复制 `.env.example` 为 `.env` 并填写密码/密钥
4. `npm run build`（前端）+ `mvn package -DskipTests`（后端）
5. `docker compose up -d` — 完成上线

---

## 十一、MVP 开发优先级

### Phase 1（核心功能，先上线）
- [ ] Spring Boot 项目初始化，数据库表创建（Flyway）
- [ ] RBAC 权限体系（用户/角色/菜单表 + Spring Security + JWT）
- [ ] 文章 CRUD + 发布/草稿状态
- [ ] 分类、标签管理
- [ ] Vue 前台：首页、文章列表、文章详情
- [ ] Vue 后台：登录、文章管理、Markdown 编辑器
- [ ] Docker Compose 部署上线

### Phase 2（完善体验）
- [ ] 评论系统（游客 + 审核）
- [ ] 图片上传（MinIO）
- [ ] SEO 优化（slug URL / sitemap / Open Graph）
- [ ] 访问量统计（Redis 计数器）
- [ ] 归档页、关于页

### Phase 3（提升影响力）
- [ ] 全文搜索（MySQL FULLTEXT 或引入 Elasticsearch）
- [ ] RSS 订阅
- [ ] 文章点赞/收藏
- [ ] 邮件通知（评论回复通知）
- [ ] 数据仪表盘优化
