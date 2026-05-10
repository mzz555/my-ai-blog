# 后台 A 批快赢 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在半天内完成后台 3 件快赢功能：封面裁剪接入、用户新建对话框、文章列表行内状态切换。

**Architecture:** 后端按 RBAC 现有模式新增 `POST /api/admin/users` + DTO + Service 方法 + Controller 端点，沿用 BCryptPasswordEncoder 与 `user:list` 权限码；前端接入已有但闲置的 `CoverCropDialog`（先把 cropperjs 从 v2.1.1 降到 v1.6.x 让组件可用）、给 `UserManageView` 加创建对话框、把 `ArticleListView` 的状态徽章改成可点 `<button>` 复用现有 toggle API。

**Tech Stack:** Spring Boot 3.2.4 / MyBatis-Plus 3.5.6 / Spring Security 6 / JUnit 5 + H2 / Vue 3 / Element Plus 2.6 / cropperjs 1.6.x（降级目标）

**Spec 来源：** `docs/superpowers/specs/2026-05-10-admin-batch-A-design.md`

---

## 文件结构总览

| 文件 | 操作 | 责任 |
|------|------|------|
| `blog-frontend/package.json` | 改 | cropperjs 降到 ^1.6.2 |
| `blog-backend/src/main/java/com/blog/dto/user/UserCreateDTO.java` | 创建 | 创建用户的入参 DTO |
| `blog-backend/src/main/java/com/blog/service/UserService.java` | 改 | 新增 `createUser` 接口签名 |
| `blog-backend/src/main/java/com/blog/service/impl/UserServiceImpl.java` | 改 | 注入 PasswordEncoder + 实现 createUser |
| `blog-backend/src/main/java/com/blog/controller/UserController.java` | 改 | 新增 `POST /api/admin/users` |
| `blog-backend/src/test/java/com/blog/service/impl/UserServiceImplTest.java` | 创建 | createUser 三场景单元测试 |
| `blog-backend/src/test/java/com/blog/controller/UserControllerTest.java` | 创建 | POST 端点 200/400/403 集成测试 |
| `blog-frontend/src/api/user.js` | 改 | 新增 createUser 函数 |
| `blog-frontend/src/views/admin/UserManageView.vue` | 改 | 新建用户对话框 + 按钮 |
| `blog-frontend/src/views/admin/ArticleEditView.vue` | 改 | 接入 CoverCropDialog |
| `blog-frontend/src/views/admin/ArticleListView.vue` | 改 | 行内状态切换 |

---

## Task 1: 前端 cropperjs 降级到 v1.6.x

**Files:**
- Modify: `blog-frontend/package.json`
- Modify: `blog-frontend/package-lock.json`（npm 自动重写）

**Why:** 现有 `CoverCropDialog.vue` 用 v1 API（`new Cropper(img, opts)` + `cropper.getCroppedCanvas()`），实际安装的 v2.1.1 是 Web Components 重写，调用会抛错。

- [ ] **Step 1.1: 改 `package.json` 中的 cropperjs 版本**

修改 `blog-frontend/package.json` 的 dependencies：
```diff
-    "cropperjs": "^2.1.1",
+    "cropperjs": "^1.6.2",
```

- [ ] **Step 1.2: 重新安装依赖**

```powershell
cd blog-frontend
npm install
```

- [ ] **Step 1.3: 验证安装版本**

```powershell
npm ls cropperjs
```
预期：`cropperjs@1.6.x`（具体小版本号可能是 1.6.2 或更高）

- [ ] **Step 1.4: 验证 v1 API 在新版本中可用**

```powershell
node -e "const C = require('./node_modules/cropperjs'); const Cls = C.default || C; console.log(typeof Cls.prototype.getCroppedCanvas)"
```
预期输出：`function`（如显示 `undefined`，说明降级未生效，回到 Step 1.1 排查）

- [ ] **Step 1.5: 提交**

```bash
git add blog-frontend/package.json blog-frontend/package-lock.json
git commit -m "chore(frontend): cropperjs 降级到 v1.6.x，恢复 CoverCropDialog 可用性"
```

---

## Task 2: 后端新建 UserCreateDTO

**Files:**
- Create: `blog-backend/src/main/java/com/blog/dto/user/UserCreateDTO.java`

- [ ] **Step 2.1: 创建 DTO 文件**

```java
package com.blog.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 后台创建用户的入参 DTO
 *
 * @author blog
 * @since 1.0.0
 */
@Data
public class UserCreateDTO {

    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "用户名只能包含字母、数字、下划线、连字符")
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(min = 6, max = 50)
    private String password;

    @Size(max = 50)
    private String nickname;

    private List<Long> roleIds = new ArrayList<>();

    private Integer status;
}
```

- [ ] **Step 2.2: 编译验证**

```powershell
cd blog-backend
mvn compile -q
```
预期：BUILD SUCCESS

- [ ] **Step 2.3: 提交**

```bash
git add blog-backend/src/main/java/com/blog/dto/user/UserCreateDTO.java
git commit -m "feat(backend): 新增 UserCreateDTO 作为后台创建用户入参"
```

---

## Task 3: UserService 接口加签名 + Impl 加 stub（让 TDD 测试可编译）

**Files:**
- Modify: `blog-backend/src/main/java/com/blog/service/UserService.java`
- Modify: `blog-backend/src/main/java/com/blog/service/impl/UserServiceImpl.java`

- [ ] **Step 3.1: 在 `UserService.java` 加入 createUser 方法签名**

修改 `blog-backend/src/main/java/com/blog/service/UserService.java`，在 `import` 块加上：
```java
import com.blog.dto.user.UserCreateDTO;
```
在接口里 `updateUser` 之后加一行：
```java
User createUser(UserCreateDTO dto);
```

完整接口：
```java
package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.common.PageResult;
import com.blog.dto.user.UserCreateDTO;
import com.blog.dto.user.UserUpdateDTO;
import com.blog.entity.User;

public interface UserService extends IService<User> {
    PageResult<User> listUsers(int page, int size, String keyword);
    void updateStatus(Long id, int status);
    User updateUser(Long id, UserUpdateDTO dto);
    User createUser(UserCreateDTO dto);
}
```

- [ ] **Step 3.2: 在 `UserServiceImpl.java` 加 stub 实现（让代码可编译）**

在 `UserServiceImpl.java` 的 `import` 块加上：
```java
import com.blog.dto.user.UserCreateDTO;
```
在类末尾追加：
```java
@Override
public User createUser(UserCreateDTO dto) {
    throw new UnsupportedOperationException("尚未实现");
}
```

- [ ] **Step 3.3: 编译验证**

```powershell
cd blog-backend
mvn compile -q
```
预期：BUILD SUCCESS

- [ ] **Step 3.4: 提交**

```bash
git add blog-backend/src/main/java/com/blog/service/UserService.java blog-backend/src/main/java/com/blog/service/impl/UserServiceImpl.java
git commit -m "feat(backend): UserService 增加 createUser 接口签名（含 stub）"
```

---

## Task 4: 写 UserServiceImpl 单元测试（红色阶段）

**Files:**
- Create: `blog-backend/src/test/java/com/blog/service/impl/UserServiceImplTest.java`

- [ ] **Step 4.1: 创建测试文件**

```java
package com.blog.service.impl;

import com.blog.dto.user.UserCreateDTO;
import com.blog.entity.User;
import com.blog.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {

    @Autowired UserService userService;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void createUser_success_returnsUserWithoutPasswordAndAssignsRoles() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("testuser_" + System.currentTimeMillis());
        dto.setEmail("test_" + System.currentTimeMillis() + "@example.com");
        dto.setPassword("secret123");
        dto.setNickname("测试用户");
        dto.setRoleIds(List.of(2L));  // USER 角色（V2 种子数据）
        dto.setStatus(1);

        User created = userService.createUser(dto);

        assertNotNull(created.getId());
        assertEquals(dto.getUsername(), created.getUsername());
        assertEquals("测试用户", created.getNickname());
        assertNull(created.getPassword(), "返回值的 password 字段必须置空");

        // 直接查 DB 校验密码已加密
        User fromDb = userService.getById(created.getId());
        assertNotNull(fromDb.getPassword());
        assertNotEquals("secret123", fromDb.getPassword(), "密码必须已加密");
        assertTrue(passwordEncoder.matches("secret123", fromDb.getPassword()));
    }

    @Test
    void createUser_duplicateUsername_throwsIllegalArgument() {
        // 先建一个
        String username = "dup_" + System.currentTimeMillis();
        UserCreateDTO first = new UserCreateDTO();
        first.setUsername(username);
        first.setEmail("first_" + System.currentTimeMillis() + "@example.com");
        first.setPassword("secret123");
        userService.createUser(first);

        // 再建同名
        UserCreateDTO dup = new UserCreateDTO();
        dup.setUsername(username);
        dup.setEmail("second_" + System.currentTimeMillis() + "@example.com");
        dup.setPassword("secret123");

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(dup)
        );
        assertTrue(ex.getMessage().contains("用户名"));
    }

    @Test
    void createUser_duplicateEmail_throwsIllegalArgument() {
        String email = "dupemail_" + System.currentTimeMillis() + "@example.com";
        UserCreateDTO first = new UserCreateDTO();
        first.setUsername("u1_" + System.currentTimeMillis());
        first.setEmail(email);
        first.setPassword("secret123");
        userService.createUser(first);

        UserCreateDTO dup = new UserCreateDTO();
        dup.setUsername("u2_" + System.currentTimeMillis());
        dup.setEmail(email);
        dup.setPassword("secret123");

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(dup)
        );
        assertTrue(ex.getMessage().contains("邮箱"));
    }
}
```

- [ ] **Step 4.2: 跑测试，预期全部失败**

```powershell
cd blog-backend
mvn test -Dtest=UserServiceImplTest -q
```
预期：3 个测试全部失败，原因为 `UnsupportedOperationException: 尚未实现`

- [ ] **Step 4.3: 提交（红色测试）**

```bash
git add blog-backend/src/test/java/com/blog/service/impl/UserServiceImplTest.java
git commit -m "test(backend): UserServiceImpl.createUser 三场景测试（红色）"
```

---

## Task 5: 实现 UserServiceImpl.createUser（绿色阶段）

**Files:**
- Modify: `blog-backend/src/main/java/com/blog/service/impl/UserServiceImpl.java`

- [ ] **Step 5.1: 注入 PasswordEncoder + 替换 stub 为完整实现**

在 `UserServiceImpl.java` 的 `import` 块加上：
```java
import org.springframework.security.crypto.password.PasswordEncoder;
```

在类的字段区（已有 `@Autowired private RoleMapper roleMapper;` 等之后）加：
```java
@Autowired
private PasswordEncoder passwordEncoder;
```

把 Task 3 加的 stub 方法整体替换为：
```java
@Override
@Transactional
public User createUser(UserCreateDTO dto) {
    if (this.count(Wrappers.<User>lambdaQuery().eq(User::getUsername, dto.getUsername())) > 0) {
        throw new IllegalArgumentException("用户名已存在");
    }
    if (this.count(Wrappers.<User>lambdaQuery().eq(User::getEmail, dto.getEmail())) > 0) {
        throw new IllegalArgumentException("邮箱已被注册");
    }

    User user = new User();
    user.setUsername(dto.getUsername());
    user.setEmail(dto.getEmail());
    user.setPassword(passwordEncoder.encode(dto.getPassword()));
    user.setNickname(
        dto.getNickname() == null || dto.getNickname().isBlank()
            ? dto.getUsername()
            : dto.getNickname()
    );
    user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
    this.save(user);

    if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
        for (Long roleId : dto.getRoleIds()) {
            userRoleMapper.insert(new UserRole(user.getId(), roleId));
        }
    }

    user.setPassword(null);
    return user;
}
```

- [ ] **Step 5.2: 跑测试，预期全部通过**

```powershell
cd blog-backend
mvn test -Dtest=UserServiceImplTest -q
```
预期：3 个测试全部通过（Tests run: 3, Failures: 0, Errors: 0）

- [ ] **Step 5.3: 提交（绿色实现）**

```bash
git add blog-backend/src/main/java/com/blog/service/impl/UserServiceImpl.java
git commit -m "feat(backend): 实现 UserServiceImpl.createUser，含密码加密与角色绑定"
```

---

## Task 6: 写 UserController 集成测试（红色阶段）

**Files:**
- Create: `blog-backend/src/test/java/com/blog/controller/UserControllerTest.java`

- [ ] **Step 6.1: 创建测试文件**

```java
package com.blog.controller;

import com.blog.dto.user.UserCreateDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "user:list")
    void create_withValidDto_shouldReturn200() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("ctrl_" + System.currentTimeMillis());
        dto.setEmail("ctrl_" + System.currentTimeMillis() + "@example.com");
        dto.setPassword("secret123");

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    @WithMockUser(authorities = "user:list")
    void create_withBlankUsername_shouldReturn400() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("");
        dto.setEmail("blank@example.com");
        dto.setPassword("secret123");

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "other:permission")
    void create_withoutUserListAuthority_shouldReturn403() throws Exception {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("noauth_" + System.currentTimeMillis());
        dto.setEmail("noauth_" + System.currentTimeMillis() + "@example.com");
        dto.setPassword("secret123");

        mockMvc.perform(post("/api/admin/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}
```

- [ ] **Step 6.2: 跑测试，预期全部失败**

```powershell
cd blog-backend
mvn test -Dtest=UserControllerTest -q
```
预期：3 个测试都失败，因为 `POST /api/admin/users` 还不存在（404）。

- [ ] **Step 6.3: 提交（红色测试）**

```bash
git add blog-backend/src/test/java/com/blog/controller/UserControllerTest.java
git commit -m "test(backend): UserController.create 三场景集成测试（红色）"
```

---

## Task 7: 实现 UserController.create（绿色阶段）

**Files:**
- Modify: `blog-backend/src/main/java/com/blog/controller/UserController.java`

- [ ] **Step 7.1: 增加 POST 端点**

在 `UserController.java` 的 `import` 块加：
```java
import com.blog.dto.user.UserCreateDTO;
```

在 `update` 方法之后追加：
```java
@PostMapping
@PreAuthorize("hasAuthority('user:list')")
public Result<User> create(@Valid @RequestBody UserCreateDTO dto) {
    return Result.success(userService.createUser(dto));
}
```

- [ ] **Step 7.2: 跑测试，预期全部通过**

```powershell
cd blog-backend
mvn test -Dtest=UserControllerTest -q
```
预期：3 个测试全部通过。

- [ ] **Step 7.3: 跑全量后端测试，确保没破坏其他东西**

```powershell
cd blog-backend
mvn test -q
```
预期：BUILD SUCCESS。

- [ ] **Step 7.4: 提交（绿色实现）**

```bash
git add blog-backend/src/main/java/com/blog/controller/UserController.java
git commit -m "feat(backend): 新增 POST /api/admin/users 创建用户接口"
```

---

## Task 8: 前端 api/user.js 加 createUser

**Files:**
- Modify: `blog-frontend/src/api/user.js`

- [ ] **Step 8.1: 追加 createUser**

在 `blog-frontend/src/api/user.js` 末尾追加一行：
```js
export const createUser = (data) => request.post('/admin/users', data)
```

完整文件应为：
```js
import request from './request'

export const getUsers = (params) => request.get('/admin/users', { params })
export const updateUserStatus = (id, status) => request.put(`/admin/users/${id}/status`, null, { params: { status } })
export const updateUser = (id, data) => request.put(`/admin/users/${id}`, data)
export const createUser = (data) => request.post('/admin/users', data)
```

- [ ] **Step 8.2: 提交**

```bash
git add blog-frontend/src/api/user.js
git commit -m "feat(frontend): api/user.js 新增 createUser 函数"
```

---

## Task 9: 前端 UserManageView 新建用户对话框

**Files:**
- Modify: `blog-frontend/src/views/admin/UserManageView.vue`

- [ ] **Step 9.1: import + state 加新建相关**

修改 `UserManageView.vue` 的 `<script setup>` 块：

把 import 行：
```js
import { getUsers, updateUserStatus, updateUser } from '@/api/user'
```
改为：
```js
import { getUsers, updateUserStatus, updateUser, createUser } from '@/api/user'
```

把 import 图标行：
```js
import { Search, Edit } from '@element-plus/icons-vue'
```
改为：
```js
import { Search, Edit, Plus } from '@element-plus/icons-vue'
```

在 `editForm` 定义之后追加：
```js
const createVisible = ref(false)
const createSaving = ref(false)
const createFormRef = ref(null)
const createForm = reactive({
  username: '',
  email: '',
  password: '',
  nickname: '',
  roleIds: [],
  status: 1,
})
const createRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名 3-50 位', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '只能包含字母、数字、下划线、连字符', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码 6-50 位', trigger: 'blur' },
  ],
}

function openCreate() {
  createForm.username = ''
  createForm.email = ''
  createForm.password = ''
  createForm.nickname = ''
  createForm.roleIds = []
  createForm.status = 1
  createVisible.value = true
}

async function handleCreate() {
  const valid = await createFormRef.value?.validate().catch(() => false)
  if (!valid) return
  createSaving.value = true
  try {
    await createUser({
      username: createForm.username.trim(),
      email: createForm.email.trim(),
      password: createForm.password,
      nickname: createForm.nickname.trim() || null,
      roleIds: createForm.roleIds,
      status: createForm.status,
    })
    ElMessage.success('用户已创建')
    createVisible.value = false
    fetchUsers(1)
  } finally {
    createSaving.value = false
  }
}
```

- [ ] **Step 9.2: 模板加按钮 + 对话框**

在 `<template>` 的 `.page-head` 内（搜索框前面）加按钮：

把：
```html
    <div class="page-head">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-sub">共 {{ total }} 位用户</p>
      </div>
      <el-input
        v-model="keyword"
        placeholder="搜索用户名 / 昵称"
        clearable
        class="search-input"
        @input="handleSearch"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>
```
改成：
```html
    <div class="page-head">
      <div>
        <h2 class="page-title">用户管理</h2>
        <p class="page-sub">共 {{ total }} 位用户</p>
      </div>
      <div class="head-right">
        <el-input
          v-model="keyword"
          placeholder="搜索用户名 / 昵称"
          clearable
          class="search-input"
          @input="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" @click="openCreate">
          <el-icon><Plus /></el-icon> 新建用户
        </el-button>
      </div>
    </div>
```

在现有"编辑用户" `<el-dialog>` 之后追加新建对话框：
```html
    <el-dialog v-model="createVisible" title="新建用户" width="480px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-position="top">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="createForm.username" placeholder="3-50 位字母/数字/下划线/连字符" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" placeholder="user@example.com" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="createForm.password" type="password" show-password placeholder="6-50 位" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="createForm.nickname" placeholder="留空时使用用户名" />
        </el-form-item>
        <el-form-item label="分配角色">
          <el-checkbox-group v-model="createForm.roleIds" class="role-check-group">
            <el-checkbox v-for="r in allRoles" :key="r.id" :label="r.id">{{ r.name }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch
            v-model="createForm.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createVisible = false">取消</el-button>
          <el-button type="primary" :loading="createSaving" @click="handleCreate">创建</el-button>
        </div>
      </template>
    </el-dialog>
```

- [ ] **Step 9.3: 样式追加 head-right 横排**

在 `<style scoped>` 末尾追加：
```css
.head-right { display: flex; align-items: center; gap: 12px; }
```

- [ ] **Step 9.4: 启动前后端，手测**

启动后端：
```powershell
cd blog-backend
mvn spring-boot:run
```

新窗口启动前端：
```powershell
cd blog-frontend
npm run dev
```

浏览器打开 `http://localhost:5173/login`，用 `admin / admin123` 登录后访问 `/admin/users`。
1. 点"新建用户" → 弹出对话框
2. 用户名留空 → 红字提示
3. 用户名 `ab` → 提示长度不足
4. 用户名 `t1` → 邮箱填错 `notmail` → 提示格式
5. 全部填正确（如 `qa01 / qa01@test.com / pass123 / 测试 / 勾选 USER 角色`）→ 创建成功 → 列表多一行
6. 用 `qa01 / pass123` 登录前台 → 应能登录成功
7. 再次创建同名 → 提示"用户名已存在"

- [ ] **Step 9.5: 提交**

```bash
git add blog-frontend/src/views/admin/UserManageView.vue
git commit -m "feat(frontend): 用户管理页新增'新建用户'对话框"
```

---

## Task 10: 前端 ArticleEditView 接入 CoverCropDialog

**Files:**
- Modify: `blog-frontend/src/views/admin/ArticleEditView.vue`

- [ ] **Step 10.1: import + state 修改**

在 `<script setup>` 的 import 块加：
```js
import CoverCropDialog from '@/components/admin/CoverCropDialog.vue'
```

把现有：
```js
const coverUploading = ref(false)
const coverInput = ref(null)
```
改为：
```js
const coverInput = ref(null)
const cropDialogVisible = ref(false)
const pendingFile = ref(null)
```
（删掉 `coverUploading`）

- [ ] **Step 10.2: 替换 handleCoverChange 函数**

把现有的整个 `handleCoverChange` 函数：
```js
async function handleCoverChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  coverUploading.value = true
  try {
    const res = await uploadImage(file)
    form.coverImage = res.data
  } catch {
    ElMessage.error('封面上传失败，请重试')
  } finally {
    coverUploading.value = false
    e.target.value = ''
  }
}
```
替换为：
```js
function handleCoverChange(e) {
  const file = e.target.files?.[0]
  e.target.value = ''  // 允许同图重选
  if (!file) return
  pendingFile.value = file
  cropDialogVisible.value = true
}

function handleCropDone(url) {
  form.coverImage = url
}
```

由于现在 `uploadImage` 不再被本文件直接调用（CoverCropDialog 内部调用），可保留 import（编辑器 MD 上传 `handleUpload` 还在用 `uploadImage`，所以 import 必须保留）。

- [ ] **Step 10.3: 模板修改：删 loading 属性 + 加裁剪框**

模板里的封面表单项：
```html
<el-button size="small" :loading="coverUploading" @click="coverInput.click()">
```
改为：
```html
<el-button size="small" @click="coverInput.click()">
```

在 `<template>` 末尾的 `</div>` 之前（也就是最外层 `.edit-wrap` 内）追加：
```html
<CoverCropDialog
  v-model:visible="cropDialogVisible"
  :file="pendingFile"
  @done="handleCropDone"
/>
```

- [ ] **Step 10.4: 手测**

如果上一个 dev server 还开着就直接访问；否则重启 `npm run dev`。

浏览器访问 `/admin/articles/new`：
1. 点"上传封面" → 选一张大图 → 弹裁剪框
2. 在裁剪框内拖动 → 调整选区
3. 点"取消" → 弹框关闭、`form.coverImage` 仍为空
4. 再点"上传封面" → 选同一张图 → 应再次弹框（验证 `e.target.value = ''` 生效）
5. 点"确认裁剪" → loading → 关闭 → 缩略图显示 16:9
6. 点"更换封面" → 走相同流程
7. 编辑已存在的文章 → 点"更换封面" → 同样走裁剪

- [ ] **Step 10.5: 提交**

```bash
git add blog-frontend/src/views/admin/ArticleEditView.vue
git commit -m "feat(frontend): 文章编辑页接入封面裁剪（强制 16:9）"
```

---

## Task 11: 前端 ArticleListView 行内状态切换

**Files:**
- Modify: `blog-frontend/src/views/admin/ArticleListView.vue`

- [ ] **Step 11.1: import + state 增加**

在 `<script setup>` 的 import 块加：

把：
```js
import { getAdminArticles, deleteArticle } from '@/api/article'
```
改为：
```js
import { getAdminArticles, deleteArticle, togglePublish } from '@/api/article'
```

把：
```js
import { ElMessage } from 'element-plus'
```
改为：
```js
import { ElMessage, ElMessageBox } from 'element-plus'
```

在 import 行追加：
```js
import { useUserStore } from '@/stores/user'
```

在已有 `const deleting = ref(false)` 之后追加：
```js
const userStore = useUserStore()
const canToggle = computed(() => userStore.hasPermission('article:publish'))
const togglingId = ref(null)
```

- [ ] **Step 11.2: 增加 handleToggle 函数**

在 `confirmDelete` 函数之后追加：
```js
async function handleToggle(row) {
  if (!canToggle.value) return
  if (row.status === 'PUBLISHED') {
    try {
      await ElMessageBox.confirm(
        `确认将文章「${row.title}」撤回为草稿？前台将不再可见。`,
        '撤回文章',
        { type: 'warning', confirmButtonText: '确认撤回', cancelButtonText: '取消' }
      )
    } catch {
      return  // 用户点了取消
    }
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

- [ ] **Step 11.3: 模板把状态徽章改为按钮**

把现有的：
```html
      <!-- 状态 -->
      <el-table-column label="状态" width="86" align="center">
        <template #default="{ row }">
          <span :class="['status-dot', row.status === 'PUBLISHED' ? 'status-dot--pub' : 'status-dot--draft']">
            {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </span>
        </template>
      </el-table-column>
```
替换为：
```html
      <!-- 状态 -->
      <el-table-column label="状态" width="86" align="center">
        <template #default="{ row }">
          <button
            class="status-dot"
            :class="[
              row.status === 'PUBLISHED' ? 'status-dot--pub' : 'status-dot--draft',
              { 'status-dot--clickable': canToggle }
            ]"
            :disabled="!canToggle || togglingId === row.id"
            :title="canToggle ? (row.status === 'PUBLISHED' ? '点击撤回为草稿' : '点击发布') : '无发布权限'"
            @click="handleToggle(row)"
          >
            {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
          </button>
        </template>
      </el-table-column>
```

- [ ] **Step 11.4: 样式增量**

在 `<style scoped>` 中找到现有 `.status-dot` 块（约第 395-405 行），在它之后追加：
```css
/* 行内状态切换的可点态 */
.status-dot {
  font-family: inherit;
  cursor: default;
  transition: opacity .15s, transform .15s;
}
.status-dot--clickable { cursor: pointer; }
.status-dot--clickable:hover { opacity: .8; transform: scale(1.04); }
.status-dot--clickable:disabled { cursor: wait; opacity: .6; transform: none; }
```

注：原 `.status-dot` 已有完整定义；上述 `font-family: inherit; cursor: default; transition: ...` 是在原定义基础上**追加**新规则（CSS 后定义覆盖前定义、其他属性按级联合并）。

- [ ] **Step 11.5: 手测**

浏览器访问 `/admin/articles`（admin 账号已有 `article:publish` 权限）：
1. 找到一篇草稿 → 鼠标悬停徽章 → 应有放大微动 + 工具提示
2. 点击草稿徽章 → 直接发布、徽章变绿、`已发布`、提示"已发布"、`publishedAt` 列显示当前时间
3. 点击已发布徽章 → 弹确认框 → 取消 → 无变化
4. 再点已发布徽章 → 确认 → 变草稿、提示"已撤回为草稿"
5. （可选）切换到无 `article:publish` 权限的账号 → 徽章保持只读、无 hover 微动

- [ ] **Step 11.6: 提交**

```bash
git add blog-frontend/src/views/admin/ArticleListView.vue
git commit -m "feat(frontend): 文章列表行内切换发布状态（撤稿需确认）"
```

---

## Task 12: 跑全量手测清单 + 完工汇报

**Files:** 无新文件

- [ ] **Step 12.1: 跑全量手测 9 项**

确保后端、前端 dev server 都在运行。打开浏览器，按以下顺序操作并标记：

| # | 操作 | 预期 | 通过 |
|---|------|------|------|
| 1 | 写新文章 → 选大图 → 弹裁剪框 → 拖拽确认 | 封面 16:9 显示 | ☐ |
| 2 | 编辑文章 → 换封面 | 同样走裁剪 | ☐ |
| 3 | 选图后点取消 | `coverImage` 不变 | ☐ |
| 4 | 用户管理 → 新建用户成功 | 列表多一行；新账号能登录 | ☐ |
| 5 | 重名用户 | 红色"用户名已存在"提示 | ☐ |
| 6 | 重邮箱用户 | 红色"邮箱已被注册"提示 | ☐ |
| 7 | 文章列表 → 草稿点徽章 | 直接变绿、`publishedAt` 出现 | ☐ |
| 8 | 文章列表 → 已发布点徽章 → 取消 | 状态不变 | ☐ |
| 9 | 文章列表 → 已发布点徽章 → 确认 | 变草稿、提示成功 | ☐ |

- [ ] **Step 12.2: 切换暗色主题再跑一遍 1/4/7**

点右上角主题切换 → 黑底显示 → 重做表中第 1、4、7 项 → 视觉无白色穿帮、无对比度问题。

- [ ] **Step 12.3: 跑后端全量测试一遍兜底**

```powershell
cd blog-backend
mvn test -q
```
预期：BUILD SUCCESS。

- [ ] **Step 12.4: 全量提交收尾（如果有零散改动）**

```bash
git status
# 若有遗漏文件
git add <files>
git commit -m "chore: A 批快赢实施收尾"
```

- [ ] **Step 12.5: 汇报**

向用户报告：
- 完成的 11 个文件改动
- 通过的 9 项手测项
- 后端 mvn test 总测试数（含新增的 6 个）
- 已知未触及但待办的项（来自 spec §7 不在范围列表）

---

## 实施完成后的下一步

A 批完成后可考虑：
- 检视一下 cropperjs 降级是否影响其他依赖（`npm ls`）
- 若用户反馈良好，按 spec §7 的剩余清单决定 B/C/D/E 批顺序
