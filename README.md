# 技术博客系统

基于 Spring Boot 3 + Vue 3 构建的全栈技术博客，支持 Markdown 写作、文章管理、评论审核、图片上传和 RBAC 权限控制。

---

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3.2.4 |
| ORM | MyBatis-Plus 3.5.6 |
| 数据库 | MySQL 8.0 |
| 缓存 | Redis 7 |
| 安全 | Spring Security 6 + JWT (jjwt 0.12.3) |
| 数据库迁移 | Flyway |
| 对象存储 | MinIO |
| 前端框架 | Vue 3.4 + Vite 5 |
| UI 组件库 | Element Plus 2.6 |
| 状态管理 | Pinia 2 |
| 路由 | Vue Router 4 |
| Markdown | md-editor-v3 |
| 部署 | Docker Compose + Nginx |

---

## 功能特性

- **前台博客**：文章列表、详情、分类/标签过滤、全文搜索、评论
- **管理后台**：文章 CRUD、Markdown 编辑器、分类/标签管理、评论审核、图片上传
- **用户系统**：注册/登录、JWT Token 刷新、个人资料
- **权限控制**：RBAC（用户→角色→菜单/权限码）、前端路由动态加载
- **SEO**：动态 title/meta（og 标签）、slug URL
- **性能**：浏览量 Redis 计数 + 定期同步、MyBatis-Plus 分页插件

---

## 快速开始（本地开发）

### 前提条件

- Java 17+
- Maven 3.6+
- Node.js 18+ (Windows 安装在 `D:\node\`)
- MySQL 8.0
- Redis 7
- MinIO（可选，用于图片上传）

### 1. 克隆并配置

```bash
git clone <repo-url>
cd tech-blog
cp blog-backend/src/main/resources/application-dev.yml.example blog-backend/src/main/resources/application-dev.yml
```

修改 `application-dev.yml` 中的数据库、Redis、MinIO 连接信息。

### 2. 启动后端

```bash
cd blog-backend
mvn spring-boot:run
```

首次启动 Flyway 会自动初始化数据库表和初始数据（ADMIN/USER 角色 + 13 个权限码）。

### 3. 启动前端

```bash
cd blog-frontend
npm install
npm run dev
```

> Windows 用户如果 npm 找不到 node，双击运行根目录的 `build_frontend.bat`。

前端开发服务器默认运行在 `http://localhost:5173`，通过 Vite 代理转发 `/api` 请求到后端 8080 端口。

---

## 环境变量

后端通过环境变量或 `application-prod.yml` 配置。关键配置项：

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| `DB_URL` | MySQL JDBC URL | `jdbc:mysql://localhost:3306/techblog` |
| `DB_USERNAME` | 数据库用户名 | `root` |
| `DB_PASSWORD` | 数据库密码 | — |
| `REDIS_HOST` | Redis 主机 | `localhost` |
| `REDIS_PORT` | Redis 端口 | `6379` |
| `JWT_SECRET` | JWT 签名密钥（≥256 bit）| — |
| `MINIO_ENDPOINT` | MinIO 地址 | `http://localhost:9000` |
| `MINIO_ACCESS_KEY` | MinIO Access Key | — |
| `MINIO_SECRET_KEY` | MinIO Secret Key | — |
| `MINIO_BUCKET` | MinIO 存储桶名 | `blog` |

创建 `.env` 文件（参考 `.env.example`）或直接在 `docker-compose.yml` 中配置。

---

## 生产部署（Docker Compose）

```bash
# 构建前端
cd blog-frontend && npm run build

# 复制构建产物到 Nginx 目录（docker-compose 已挂载）
# 启动所有服务
cd ..
docker-compose up -d
```

服务列表：
- `blog-backend` — Spring Boot API（8080）
- `mysql` — MySQL 8.0（3306）
- `redis` — Redis 7（6379）
- `minio` — MinIO 对象存储（9000/9001）
- `nginx` — 反向代理（80/443）

默认管理员账户：`admin / admin123`（首次登录后请立即修改密码）

---

## 目录结构

```
tech-blog/
├── blog-backend/              # Spring Boot 后端
│   ├── src/main/java/com/blog/
│   │   ├── config/            # MyBatis-Plus、Redis、MinIO、Security 配置
│   │   ├── common/            # Result、PageResult、GlobalExceptionHandler、异常类
│   │   ├── controller/        # REST 控制器
│   │   ├── service/           # 业务接口（extends IService<T>）
│   │   │   └── impl/          # 业务实现（extends ServiceImpl<M,T>）
│   │   ├── mapper/            # MyBatis-Plus Mapper（extends BaseMapper<T>）
│   │   ├── entity/            # 数据库实体类（@TableName）
│   │   ├── dto/               # 请求/响应 DTO
│   │   └── security/          # JWT 工具、Spring Security 配置
│   └── src/main/resources/
│       ├── application.yml    # 基础配置（激活 profile）
│       ├── application-dev.yml  # 开发环境配置
│       └── db/migration/      # Flyway 数据库迁移脚本
├── blog-frontend/             # Vue 3 前端
│   ├── src/
│   │   ├── api/               # Axios 封装 + 各模块 API 函数
│   │   ├── components/        # 可复用组件
│   │   ├── directives/        # 自定义指令（v-permission）
│   │   ├── layouts/           # 前台/后台布局
│   │   ├── router/            # Vue Router 配置（含动态路由）
│   │   ├── stores/            # Pinia 状态（用户信息）
│   │   ├── utils/             # 工具函数（auth、format）
│   │   └── views/             # 页面组件
│   └── package.json
├── nginx/                     # Nginx 配置
├── docker-compose.yml
└── README.md
```

---

## API 文档

启动后端后访问：`http://localhost:8080/swagger-ui.html`

---

## 开发规范

- **Java 注释**：所有公开类和方法添加 JavaDoc（`@param`、`@return`）
- **JS 注释**：API 函数、复杂逻辑添加 JSDoc
- **Git 提交**：提交信息使用中文，格式：`feat: 新增文章标签功能`
- **代码格式**：后端用 IntelliJ 默认格式化，前端用 Prettier（`npm run format`）
