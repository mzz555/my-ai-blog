# 博客项目收尾路线图设计

**日期：** 2026-04-25  
**项目：** my-ai-blog（Spring Boot 3 + Vue 3 前后端分离博客）  
**目标：** 全面收尾——补全后端缺口、修复损坏功能、对齐 Pencil UI 设计稿

---

## 现状评估

### 技术栈

| 层次 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2.4 + MyBatis-Plus + MySQL 8 + Redis + MinIO + JWT |
| 前端 | Vue 3.4 + Vite 5 + Element Plus 2.6 + Pinia + Vue Router 4 |
| 部署 | Docker Compose + Nginx |

### 完成度

| 模块 | 状态 |
|------|------|
| 后端整体 | ~75% 完成，6 类接口缺失 |
| 前端路由 | 100% 配置完毕 |
| 前台页面 | 文件齐全，部分 UI 未对齐设计稿 |
| 后台页面 | 文件齐全，部分页面功能损坏或未联调 |

### 后端缺口清单

| 缺失接口 | 影响页面 |
|----------|----------|
| `GET/POST/PUT/DELETE /api/admin/menus` | 菜单管理（完全 404） |
| `POST/PUT/DELETE /api/admin/roles` | 角色管理（只有 GET） |
| `GET /api/articles/admin/{id}` | 文章编辑（编辑模式表单永远空白） |
| `GET /api/articles/{slug}/neighbors` | 文章详情（上/下篇导航） |
| `PATCH /api/users/profile` | 个人资料（保存按钮 disabled） |
| `PUT /api/tags/{id}` | 标签管理（只有 GET/POST/DELETE） |
| `PUT /api/admin/users/{id}` | 用户管理（只有 GET + status） |

---

## 设计系统（来自 Pencil myBlog.pen）

### 暗色主题色板

| 用途 | 色值 |
|------|------|
| 前台页面背景 | `#0C0C10` |
| 后台页面背景 | `#0A0A12` |
| 侧边栏背景 | `#111119` |
| 卡片背景 | `#13131E` |
| 次级背景 | `#16162A` |
| 搜索框/输入框 | `#1A1A28` |
| 激活菜单项背景 | `#1C1C30` / `#1E1E30` |
| 分割线 | `#1C1C2C` / `#1E1E2C` |
| **主色调（amber）** | `#E8A838` |
| 主文字 | `#F0F0F8` / `#F9FAFB` |
| 次文字 | `#8A8A9E` / `#9CA3AF` |
| 禁用/图标 | `#6E6E82` / `#6B7280` |
| 文字最淡 | `#4A4A6A` / `#3A3A5C` |

### 亮色主题

| 用途 | 色值 |
|------|------|
| 前台背景 | `#F7F7F2` |
| 导航栏 | `#FFFFFF`（带 shadow） |
| 后台背景 | `#F3F4F6` |

### 字体规格

| 场景 | 大小 / 字重 |
|------|------------|
| Hero 标题 | 50px / 700 |
| 文章详情标题 | 44px / 700 |
| 区块标题 | 30px / 700 |
| 页面标题（前台） | 32px / 700 |
| 后台 Topbar 标题 | 18px / 700 |
| 卡片标题 | 16px / 600 |
| 导航链接（激活） | 14px / 600 / `#E8A838` |
| 导航链接（正常） | 14px / normal / `#9CA3AF` |
| 侧边栏菜单（激活） | 13px / 600 / `#E8A838` |
| 侧边栏菜单（正常） | 13px / normal / `#6E6E82` |
| 正文 | 15px / normal / `#A0A0B8` |
| 辅助/Footer | 12px / normal / `#4A4A6A` |

### 布局规格

**前台：**
- 画布宽：1440px
- 导航栏高：72px，水平内边距：48px，导航链接间距：gap 32px
- 分割线：`#1C1C2C` / 1px

**后台：**
- 侧边栏宽：240px
- Logo 区高：64px，gap 10px，图标方块：32×32 / `#E8A838`
- 菜单项高：40px，padding [0,8]，gap 10px
- Topbar 高：64px，背景 `#111119`
- 工具栏高：56px

---

## 推进策略：按缺口严重程度分三轮

```
Critical → Important → Polish
```

---

## Phase 1：Critical（当前页面打开直接 404）

### 1.1 菜单管理后端（MenuController 全新）

**新建文件：**
- `MenuController.java`
- `MenuService.java` / `MenuServiceImpl.java`
- `dto/menu/MenuDTO.java`

> `MenuMapper.java` 已存在，无需新建。

**API 端点：**

```
GET    /api/admin/menus        → 返回树状结构列表（递归组装父子）
POST   /api/admin/menus        → 新建菜单节点
PUT    /api/admin/menus/{id}   → 更新菜单节点
DELETE /api/admin/menus/{id}   → 删除（含级联删除子菜单）
```

**字段映射（前端 ↔ 后端）：**

| 前端字段 | 后端字段 | 转换规则 |
|----------|----------|----------|
| `visible: boolean` | `status: int` | true→1, false→0 |
| `sort: number` | `sortOrder: int` | DTO 映射 |
| `children: Menu[]` | `@TableField(exist=false)` | Service 层递归组装 |
| `parentId: null\|number` | `parentId: Long` | 直接映射 |

`listTree()` 逻辑：查全部 menu → 内存中按 parentId 递归组装 → 返回根节点列表。

删除：Service 层递归删除子菜单（避免外键约束报错）。

**前端调整（MenuManageView.vue）：**
- `sort` ↔ `sortOrder` 字段名统一
- `visible` ↔ `status` 布尔转换
- 加载失败时显示空状态（非空白）
- 按设计稿调整样式（#13131E 卡片，#E8A838 操作按钮）

### 1.2 角色管理后端（RoleController 补全）

**补充端点：**

```
GET    /api/admin/roles        → 已存在，改为返回 List<RoleVO>
POST   /api/admin/roles        → 新建角色
PUT    /api/admin/roles/{id}   → 更新角色 + 重分配权限
DELETE /api/admin/roles/{id}   → 删除角色
```

**数据结构对齐：**

```java
// 请求体 RoleDTO
{ name, description, permissionCodes: List<String> }

// 响应体 RoleVO（修正 Role.menus → permissions）
{ id, name, code, description, status, permissions: [{id, code}] }
```

**权限分配逻辑（POST/PUT）：**
1. 根据 `permissionCodes` 在 menus 表中查 `code IN (...)` 得到 menu id 列表
2. 删除 `role_menus WHERE role_id = ?`
3. 批量插入新的 `role_menus` 记录

**权限码不匹配（需修正前端）：** 前端 `allPermissions` 硬编码了 6 个粗粒度码，与数据库实际 13 个细粒度码完全不同：

| 前端（错误） | 数据库实际（V1__init.sql） |
|-------------|--------------------------|
| `article:read` | `article:list` |
| `article:write` | `article:create` / `article:update` / `article:publish` |
| `article:delete` | `article:delete` ✅ |
| `comment:manage` | `comment:list` / `comment:approve` / `comment:delete` |
| `user:manage` | `user:list` |
| `role:manage` | `role:manage` ✅ |
| ——（缺失） | `category:manage` / `tag:manage` / `menu:manage` |

**决策：** 将前端 `RoleManageView.vue` 中的 `allPermissions` 替换为与数据库一致的 13 个权限码，同时调整显示名称（中文标签）。后端 `@PreAuthorize` 注解中使用的权限码保持不变。

**前端调整（RoleManageView.vue）：**
- GET 响应改用 `permissions` 字段（已匹配 RoleVO）
- POST/PUT 发送 `permissionCodes` 数组（已实现）
- 权限 tag 颜色统一为 `#E8A838` amber
- 删除角色时检查用户绑定（提示，允许删除）

---

## Phase 2：Important（功能存在但不完整）

### 2.1 文章编辑修复（最高优先）

**Bug：** 进入 `/admin/articles/:id/edit` 时，`onMounted` 只加载分类/标签，不加载文章数据，表单永远空白。

**后端新增：**
```
GET /api/articles/admin/{id}
```
- 需要 `article:write` 权限
- 返回完整文章字段：title, slug, summary, content, coverImage, status, categoryId, tagNames[], isTop, allowComment

**前端修复（ArticleEditView.vue）：**
- `api/article.js` 新增 `getArticleById(id)`
- `onMounted` 中 `isEdit.value` 为真时调用，填充 form 所有字段
- 新增封面图上传 UI（复用已有 `uploadImage`）
- slug 从标题自动生成，可手动覆盖
- 按 Pencil Article Edit — Admin 设计稿重构布局

### 2.2 文章邻篇导航

**后端新增：**
```
GET /api/articles/{slug}/neighbors
```

响应结构：
```json
{ "prev": { "title": "...", "slug": "..." }, "next": { "title": "...", "slug": "..." } }
```

查询逻辑：
- prev：状态 PUBLISHED，id < 当前，DESC LIMIT 1
- next：状态 PUBLISHED，id > 当前，ASC LIMIT 1
- 任一方向无结果返回 null

**前端（PostDetailView.vue）：**
- `getArticleNeighbors(slug)` 已存在，直接调用
- 文章内容底部渲染上/下篇卡片（#13131E 背景，←/→ 方向标）
- 任一方向为 null 时隐藏对应卡片

### 2.3 个人资料保存

**后端新增（AuthController 中）：**
```
PATCH /api/users/profile
```
- 从 JWT 获取当前用户 ID
- 可更新字段：`bio`
- 返回更新后的用户对象
- 成功后前端刷新 Pinia userStore

**前端（ProfileView.vue）：**
- `api/auth.js` 新增 `updateProfile(data)`
- 去掉保存按钮 disabled，加 loading 状态

### 2.4 标签编辑

**后端（TagController 新增）：**
```
PUT /api/tags/{id}
```
- 调用 `tagService.updateById(tag)`
- 需要 `tag:manage` 权限

**前端：** TagManageView.vue 已完整实现，无需改动。

### 2.5 用户编辑 + 角色分配

**后端（UserController 新增）：**
```
PUT /api/admin/users/{id}
```
请求体：`{ nickname: String, roleIds: Long[] }`

Service 逻辑：
1. 更新 users.nickname
2. 删除 `user_roles WHERE user_id = ?`
3. 批量插入新的 `user_roles` 记录
4. 响应包含 roles 数组（UserManageView 用于回显）

**前端：** UserManageView.vue 已完整实现，无需改动。

---

## Phase 3：Polish（UI 对齐 Pencil 设计稿）

### 3.1 前台公共组件

**FrontLayout 导航栏：**
- 高度 72px，水平内边距 48px，导航间距 gap: 32px
- 激活链接：`#E8A838` / 600；非激活：`#9CA3AF` / normal
- Logo：22-24px / 700 / `#E8A838`
- 底部分割线：`#1C1C2C` / 1px
- ThemeToggle 图标：`#6E6E82` / 18px

**ArticleCard 组件：**
- 卡片背景 `#13131E`，圆角 8px
- 封面图高度：140px（大卡）/ 130px（小卡）
- 卡片内边距：padding [20,20,16,20]
- 3 列网格，列间距 gap: 24px，区域 padding [40,64]

**PostListView 筛选栏：**
- FilterBar 背景 `#13131E`，padding [16,64]，gap 10px
- 激活分类按钮：`#E8A838` 背景 / `#000` 文字
- 非激活按钮：`#1E2030` 背景 / `#D1D5DB` 文字
- 分页：当前页 `#E8A838` 背景 / `#000` 文字，其余 `#13131E`

### 3.2 文章详情页（PostDetailView）

**Hero 区域：**
- 封面大图全宽，高度 420px
- 内容叠层宽 860px，padding [60,0,48,64]，gap 16px
- 面包屑：12px / `#6E6E82`，激活项 `#E8A838`
- 标签 chip：`#1C1C2E` 背景，padding [4,12]
- 标题：44px / 700 / `#F0F0F8`，宽 780px
- 元数据 gap: 20px，分割竖线 `#2A2A40`

**正文区域：**
- 内边距：padding [48,64]，gap 40px
- 正文字号：15px / `#A0A0B8`
- 代码块：`#0F0F1C` 背景，标题栏 `#16162A`
- 引用块：`#111120` 背景，左边框 3px `#E8A838`，padding [20,24]
- 点赞栏：`#13131E`，padding [20,24]

### 3.3 后台通用样式

**AdminLayout 侧边栏：**
- 宽度 240px，背景 `#111119`，边框 `#1E1E2C`
- Logo 区：高 64px，图标方块 32×32 / `#E8A838`，gap 10px
- 菜单项：高 40px，padding [0,8]，gap 10px
- 激活项：背景 `#1E1E30`，图标/文字 `#E8A838` / 600
- 非激活：图标/文字 `#6E6E82` / normal
- 底部用户栏：头像 32px ellipse / `#E8A838`

**所有后台列表页：**
- Topbar：高 64px，背景 `#111119`，页面标题 18px / 700 / `#F9FAFB`
- 工具栏：高 56px，搜索框/下拉 `#1A1A28`，高 36px
- 表头：`#111119` 背景，高 44px，padding [0,24]
- 数据行：高 60px，交替行背景 `#0D0D1A`
- 主色按钮：`#E8A838` 背景，`#000`/`#0C0C10` 文字，高 36px，padding [0,16]
- 状态 badge：发布 = `#1A2010` + `#6FCF97`；草稿 = `#1A1028` + `#9CA3AF`

### 3.4 公共 UI 组件（新建）

| 组件 | 说明 |
|------|------|
| `EmptyState.vue` | 图标 + 主标题 + 副标题 + 可选操作按钮 |
| `SkeletonCard.vue` | `#1C1C2C` 闪烁动画，替代 el-loading 菊花 |
| `ErrorState.vue` | 红色提示框 + 重试按钮，替代静默失败 |

---

## 工作量估算

| 阶段 | 任务 | 估算 |
|------|------|------|
| Critical | MenuController（后端全新） | 1 天 |
| Critical | RoleController 补全 + RoleVO | 0.5 天 |
| Critical | 前端菜单/角色视图对齐 | 0.5 天 |
| Important | 文章编辑修复（后端+前端） | 1 天 |
| Important | 文章邻篇导航 | 0.5 天 |
| Important | 个人资料 + 标签 + 用户编辑 | 0.5 天 |
| Polish | 前台 UI 对齐（导航栏/卡片/列表） | 1 天 |
| Polish | 文章详情 UI 对齐 | 1 天 |
| Polish | 后台通用样式统一 | 1 天 |
| Polish | 公共组件（空状态/骨架屏/错误状态） | 0.5 天 |
| **合计** | | **~7 天** |

---

## 约束与决策记录

1. **不新增功能**：本轮目标是收尾现有设计，不引入新功能（点赞统计图表、邮件通知等留待下一阶段）。
2. **UI 必须遵循 Pencil 设计**：所有颜色、字号、间距直接取自 `pencil/myBlog.pen`，不自行发明。
3. **后端接口设计以前端已有 API 调用为准**：前端 api/*.js 文件定义了接口契约，后端实现与之对齐，不改前端（除 ArticleEditView 需补充调用外）。
4. **前端数据字段差异由后端 DTO/VO 吸收**：如 `visible↔status`、`sort↔sortOrder`、`permissions↔menus` 等，不在前端做额外转换。
5. **权限码以数据库为准**：menus 表（V1__init.sql）定义了 13 个细粒度 BUTTON 权限，前端 `allPermissions` 需全部替换为这 13 个码，不修改数据库。
