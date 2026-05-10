# 后台 A 批快赢设计

**日期**：2026-05-10
**范围**：后台三件快赢功能 —— 封面裁剪接入、用户新建、文章列表行内状态切换
**预估工作量**：半天

---

## 1. 背景

后台审计发现三处可在半天内补齐的体验缺口：

1. `components/admin/CoverCropDialog.vue` 已写好但未接入文章编辑，封面比例参差
2. `UserManageView` 只能编辑、不能新建用户，新用户必须走前台 register
3. `ArticleListView` 列表中改"草稿/已发布"必须进入编辑页改 `status` 字段

本设计落实"A 批"修复，明确不在范围内的改动列在 §7。

---

## 2. 决策记录

| # | 决策 | 选项 |
|---|------|------|
| D1 | 用户新建的密码流程 | **管理员手填**（与 `register` 一致） |
| D2 | 封面上传是否强制裁剪 | **强制**（保证列表/卡片视觉整齐） |
| D3 | 行内状态切换的视觉形态 | **状态徽章本身可点**（hover 态强调可点性） |
| D4 | 撤稿（已发布→草稿）是否需要二次确认 | **需要**（影响前台可见性） |
| D5 | cropperjs 版本路线 | **降级到 v1.6.x**（与现有 `CoverCropDialog.vue` 的 v1 API 匹配） |

---

## 3. 功能 1：封面裁剪接入

### 3.1 前置修复

**修改 `blog-frontend/package.json`**：
```diff
- "cropperjs": "^2.1.1",
+ "cropperjs": "^1.6.2",
```
然后 `npm install` 锁定到 v1.6.x。

**理由**：现有 `CoverCropDialog.vue` 用的是 v1 API（`new Cropper(img, opts)` + `cropper.getCroppedCanvas()`），但当前安装的 v2 是基于 Web Components 的完全重写，不存在 `getCroppedCanvas` 方法。组件目前是坏死代码（仓库内 0 引用）。降级是最低成本路径。

### 3.2 ArticleEditView.vue 改动

**新增状态：**
```js
const cropDialogVisible = ref(false)
const pendingFile = ref(null)
```

**改写 `handleCoverChange`：**
```js
function handleCoverChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  pendingFile.value = file
  cropDialogVisible.value = true
  e.target.value = ''  // 允许同图重选
}
```

**模板中增加：**
```html
<CoverCropDialog
  v-model:visible="cropDialogVisible"
  :file="pendingFile"
  @done="handleCropDone"
/>
```

**新增 done 回调：**
```js
function handleCropDone(url) {
  form.coverImage = url
}
```

**删除：** 原有 `coverUploading` 状态（裁剪框自带 loading）。

### 3.3 交互流程

```
点"上传/更换封面"
  → 文件选择器
  → 选完图自动弹裁剪框（16:9 固定）
  → [取消] coverImage 不变
  → [确认裁剪] cropper 输出 JPEG 0.9 → uploadImage → form.coverImage = url
```

### 3.4 边缘情况

- 同图重选：`coverInput.value = ''` 已存在
- 文件类型限制：`accept="image/*"` 已存在
- cropperjs 实例销毁：CoverCropDialog 已在 `@closed` 处理
- 未选文件：`if (!file) return` 早退

---

## 4. 功能 2：用户新建

### 4.1 后端

**新增文件 `dto/user/UserCreateDTO.java`：**
```java
@Data
public class UserCreateDTO {
    @NotBlank @Size(min=3, max=50)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$")
    private String username;

    @NotBlank @Email @Size(max=100)
    private String email;

    @NotBlank @Size(min=6, max=50)
    private String password;

    @Size(max=50)
    private String nickname;

    private List<Long> roleIds;

    private Integer status;  // 默认 1
}
```

**修改 `service/UserService.java`：** 新增方法签名
```java
User createUser(UserCreateDTO dto);
```

**修改 `service/impl/UserServiceImpl.java`：**

新增字段注入：
```java
@Autowired
private PasswordEncoder passwordEncoder;
```

新增方法：
```java
@Override
@Transactional
public User createUser(UserCreateDTO dto) {
    if (this.count(Wrappers.<User>lambdaQuery().eq(User::getUsername, dto.getUsername())) > 0)
        throw new IllegalArgumentException("用户名已存在");
    if (this.count(Wrappers.<User>lambdaQuery().eq(User::getEmail, dto.getEmail())) > 0)
        throw new IllegalArgumentException("邮箱已被注册");

    User user = new User();
    user.setUsername(dto.getUsername());
    user.setEmail(dto.getEmail());
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setNickname(dto.getNickname() == null || dto.getNickname().isBlank()
        ? dto.getUsername() : dto.getNickname());
    user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
    this.save(user);

    if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
        dto.getRoleIds().forEach(roleId ->
            userRoleMapper.insert(new UserRole(user.getId(), roleId)));
    }

    user.setPassword(null);
    return user;
}
```

**修改 `controller/UserController.java`：**
```java
@PostMapping
@PreAuthorize("hasAuthority('user:list')")
public Result<User> create(@Valid @RequestBody UserCreateDTO dto) {
    return Result.success(userService.createUser(dto));
}
```

### 4.2 前端

**修改 `api/user.js`：** 新增
```js
export const createUser = (data) => request.post('/admin/users', data)
```

**修改 `views/admin/UserManageView.vue`：**

1. 页头加按钮：
```html
<el-button type="primary" @click="openCreate">
  <el-icon><Plus /></el-icon> 新建用户
</el-button>
```

2. 新增对话框，字段：username / email / password（带 `show-password`）/ nickname / 角色 checkbox-group / 状态 switch

3. 状态：
```js
const createVisible = ref(false)
const createSaving = ref(false)
const createForm = reactive({
  username: '', email: '', password: '', nickname: '',
  roleIds: [], status: 1,
})
const createRules = {
  username: [{ required: true, min: 3, max: 50, message: '3-50 位用户名' }],
  email: [{ required: true, type: 'email', message: '邮箱格式不正确' }],
  password: [{ required: true, min: 6, max: 50, message: '密码 6-50 位' }],
}
```

4. 提交：
```js
async function handleCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  createSaving.value = true
  try {
    await createUser(createForm)
    ElMessage.success('用户已创建')
    createVisible.value = false
    fetchUsers(page.value)
  } finally {
    createSaving.value = false
  }
}
```

### 4.3 校验与错误处理

- 前端：Element Plus form rules（同 LoginView 风格）
- 后端：抛 `IllegalArgumentException` → `GlobalExceptionHandler` 转 400 + 错误消息
- 密码字段返回前置 `null`（沿用 `listUsers` 的处理方式）

---

## 5. 功能 3：行内状态切换

### 5.1 改动单文件 `views/admin/ArticleListView.vue`

**模板**（替换第 102-108 行的 `<span class="status-dot">`）：
```html
<el-table-column label="状态" width="86" align="center">
  <template #default="{ row }">
    <button
      class="status-dot"
      :class="[
        row.status === 'PUBLISHED' ? 'status-dot--pub' : 'status-dot--draft',
        { 'status-dot--clickable': canToggle }
      ]"
      :disabled="!canToggle || togglingId === row.id"
      @click="handleToggle(row)"
    >
      {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
    </button>
  </template>
</el-table-column>
```

**Script 新增：**
```js
import { togglePublish } from '@/api/article'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const canToggle = computed(() => userStore.hasPermission('article:publish'))
const togglingId = ref(null)

async function handleToggle(row) {
  if (!canToggle.value) return
  if (row.status === 'PUBLISHED') {
    try {
      await ElMessageBox.confirm(
        `确认将文章「${row.title}」撤回为草稿？前台将不再可见。`,
        '撤回文章',
        { type: 'warning', confirmButtonText: '确认撤回', cancelButtonText: '取消' }
      )
    } catch { return }
  }
  togglingId.value = row.id
  try {
    await togglePublish(row.id)
    row.status = row.status === 'PUBLISHED' ? 'DRAFT' : 'PUBLISHED'
    if (row.status === 'PUBLISHED' && !row.publishedAt) {
      row.publishedAt = new Date().toISOString()
    }
    ElMessage.success(row.status === 'PUBLISHED' ? '已发布' : '已撤回为草稿')
  } finally {
    togglingId.value = null
  }
}
```

**样式增量：**
```css
.status-dot {
  font-family: inherit;
  border: 1px solid;
  cursor: default;
  transition: opacity .15s, transform .15s;
}
.status-dot--clickable { cursor: pointer; }
.status-dot--clickable:hover { opacity: .8; transform: scale(1.04); }
.status-dot--clickable:disabled { cursor: wait; opacity: .6; transform: none; }
```

### 5.2 行为细节

- 草稿 → 已发布：直接 toggle，无确认
- 已发布 → 草稿：弹 ElMessageBox 二次确认
- 无 `article:publish` 权限：保持只读（不显 hover 态）
- **乐观更新**：只改单行 `status / publishedAt`，不重新 `loadArticles()`，避免分页跳回
- 失败处理：`request.js` 全局拦截弹错；状态字段在 await 之后才修改，失败时 UI 自动复原

### 5.3 后端无改动

`PUT /api/articles/{id}/publish` 已存在，`togglePublish` 实现已覆盖：
- 草稿→发布：自动填 `publishedAt = now()` 仅在为 null 时
- 发布→草稿：保留原 `publishedAt`（再次发布维持原时间）

---

## 6. 测试策略

### 6.1 后端

| 测试 | 工具 | 场景 |
|------|------|------|
| `UserServiceImplTest.createUser_success` | JUnit + H2 | 创建成功 + 角色绑定 |
| `UserServiceImplTest.createUser_duplicateUsername` | JUnit + H2 | 抛 IllegalArgumentException |
| `UserServiceImplTest.createUser_duplicateEmail` | JUnit + H2 | 抛 IllegalArgumentException |
| `UserControllerTest.create_200` | MockMvc + spring-security-test | 有 `user:list` 权限返回 200 |
| `UserControllerTest.create_400_validation` | MockMvc | 无 username/email 返回 400 |
| `UserControllerTest.create_403_unauthorized` | MockMvc | 无 `user:list` 权限返回 403 |

### 6.2 前端

无 vitest 配置，以**手测清单**替代：

1. 写新文章：选大图 → 弹裁剪框 → 拖拽确认 → 封面 16:9 显示
2. 编辑文章：换封面 → 同样走裁剪
3. 取消裁剪：`coverImage` 不变
4. 用户管理：新建用户成功 → 列表多一行 → 用新账号能登录
5. 用户管理：重名/重邮箱 → 红色错误提示
6. 文章列表：草稿点徽章 → 直接发布、徽章变绿、`publishedAt` 出现
7. 文章列表：已发布点徽章 → 弹确认 → 取消无变化 / 确认变草稿
8. 切换无 `article:publish` 权限的账号 → 徽章无 hover 态、不可点
9. 暗色主题下三处 UI 视觉正常

---

## 7. 不在范围

为避免范围蔓延，以下显式排除：

- ❌ 批量操作（属 B 批）
- ❌ menus 表概念分离（属 D 批）
- ❌ 角色权限按模块分组（属 C 批）
- ❌ 修复 `UserServiceImpl.updateUser` 中"`roleIds=null` 时仍清空角色"的预存 bug
- ❌ 草稿恢复 UI 提示（属 C 批）
- ❌ Slug 中文支持
- ❌ ECharts tooltip 颜色硬编码（属 E 批）

---

## 8. 文件清单

### 后端
- 新建 `blog-backend/src/main/java/com/blog/dto/user/UserCreateDTO.java`
- 改 `blog-backend/src/main/java/com/blog/service/UserService.java`
- 改 `blog-backend/src/main/java/com/blog/service/impl/UserServiceImpl.java`
- 改 `blog-backend/src/main/java/com/blog/controller/UserController.java`
- 新建 `blog-backend/src/test/java/com/blog/service/impl/UserServiceImplTest.java`
- 新建 `blog-backend/src/test/java/com/blog/controller/UserControllerCreateTest.java`

### 前端
- 改 `blog-frontend/package.json`（cropperjs 降级）
- 改 `blog-frontend/src/views/admin/ArticleEditView.vue`（封面裁剪 wiring）
- 改 `blog-frontend/src/views/admin/UserManageView.vue`（新建用户对话框）
- 改 `blog-frontend/src/views/admin/ArticleListView.vue`（行内状态切换）
- 改 `blog-frontend/src/api/user.js`（加 createUser）

---

## 9. 实施顺序

```
1. 前端先降级 cropperjs（npm install）
2. 后端 user 创建（DTO → Service 接口/实现 → Controller → 测试）
3. 前端 user 创建对话框（依赖 2）
4. 前端封面裁剪接入（独立，可与 2/3 并行）
5. 前端行内状态切换（独立）
6. 跑一遍手测清单（§6.2）
```

---

## 10. 风险

| 风险 | 缓解 |
|------|------|
| cropperjs 降级后其他依赖（Element Plus 内部裁剪、md-editor-v3）冲突 | 两者均不依赖 cropperjs；npm ls cropperjs 验证只剩一个版本 |
| 乐观更新与并发删除冲突 | 极低概率，可接受；下次刷新即解 |
| 创建用户接口缺独立权限码（用了 `user:list`） | 当前 RBAC 设计 13 码已固定；A 批不引入新权限码，沿用 `user:list` 作为"用户管理"总入口；后续如需细化可单独扩展 |
