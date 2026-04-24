-- V2__seed_admin.sql
-- 默认管理员账号：admin / Admin@2024
-- 密码 BCrypt hash（Spring BCryptPasswordEncoder strength=10）
INSERT INTO users (username, email, password, status) VALUES
('admin', 'admin@blog.com', '$2a$10$ZYpBB4KjKcad8tisoTzhAe9fQ5sqjHnKwNbIcFOryslOaFElW9j8e', 1);

-- 将 admin 用户关联到 ADMIN 角色（roles 表中 id=1 为 ADMIN，V1 已插入）
INSERT INTO user_roles (user_id, role_id)
SELECT id, 1 FROM users WHERE username = 'admin';

-- 种子分类
INSERT INTO categories (name, slug, description, sort_order) VALUES
('前端开发', 'frontend',  '前端技术：Vue、React、CSS、TypeScript', 1),
('后端开发', 'backend',   '后端技术：Java、Spring Boot、数据库', 2),
('DevOps',   'devops',    '运维、CI/CD、Docker、Kubernetes',      3),
('算法',     'algorithm', '数据结构与算法、LeetCode 题解',         4);

-- 种子标签
INSERT INTO tags (name, slug) VALUES
('Vue',         'vue'),
('Spring Boot', 'spring-boot'),
('MyBatis',     'mybatis'),
('MySQL',       'mysql'),
('Redis',       'redis'),
('Docker',      'docker'),
('JavaScript',  'javascript'),
('Java',        'java');

-- 欢迎文章
INSERT INTO articles (title, slug, summary, content, status, is_top, allow_comment, author_id, category_id, published_at)
SELECT
  '欢迎来到技术博客',
  'welcome-to-tech-blog',
  '这是博客的第一篇文章，介绍本博客的技术栈和功能特性。',
  '# 欢迎来到技术博客

本博客基于以下技术栈构建：

## 后端
- **Spring Boot 3.2** — RESTful API
- **MyBatis-Plus** — ORM
- **Spring Security + JWT** — 认证与授权
- **Redis** — 缓存与 Token 存储
- **MySQL 8** — 主数据库
- **MinIO** — 图片存储

## 前端
- **Vue 3** — 前端框架
- **Vite 5** — 构建工具
- **Element Plus** — UI 组件库
- **Pinia** — 状态管理
- **md-editor-v3** — Markdown 编辑器

## 功能
- 文章 CRUD，支持 Markdown 编写
- 分类 + 标签管理
- 评论系统（支持树形结构）
- 基于 RBAC 的权限控制
- 图片上传（MinIO）
- 浏览量统计（Redis 异步同步）

欢迎体验！',
  'PUBLISHED',
  TRUE,
  TRUE,
  u.id,
  c.id,
  NOW()
FROM users u, categories c
WHERE u.username = 'admin' AND c.slug = 'backend'
LIMIT 1;
