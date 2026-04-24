# 博客项目收尾 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 补全后端缺失接口、修复损坏的文章编辑功能、对齐 Pencil UI 设计稿，使整个博客系统达到完整可用状态。

**Architecture:** 按 Critical → Important → Polish 三阶段推进：先修复完全断掉的后端接口（菜单/角色管理），再补全不完整功能（文章编辑/邻篇/个人资料等），最后用 Pencil 设计稿中提取的色值和尺寸对齐前端 UI。后端接口契约以前端 api/*.js 已有调用为准，字段差异由后端 DTO/VO 吸收。

**Tech Stack:** Spring Boot 3.2 / MyBatis-Plus / MySQL 8 / Vue 3.4 / Element Plus 2.6 / Pinia

---

## 文件变更总览

### Phase 1 — Critical（后端补全 + 前端接通）

**新建：**
- `blog-backend/src/main/java/com/blog/dto/menu/MenuDTO.java`
- `blog-backend/src/main/java/com/blog/dto/menu/MenuVO.java`
- `blog-backend/src/main/java/com/blog/service/MenuService.java`
- `blog-backend/src/main/java/com/blog/service/impl/MenuServiceImpl.java`
- `blog-backend/src/main/java/com/blog/controller/MenuController.java`
- `blog-backend/src/test/java/com/blog/controller/MenuControllerTest.java`
- `blog-backend/src/main/java/com/blog/dto/role/RoleDTO.java`
- `blog-backend/src/main/java/com/blog/dto/role/RoleVO.java`
- `blog-backend/src/test/java/com/blog/controller/RoleControllerTest.java`

**修改：**
- `blog-backend/src/main/java/com/blog/entity/Menu.java` — 添加 `children` 字段
- `blog-backend/src/main/java/com/blog/service/RoleService.java` — 添加 CRUD 方法
- `blog-backend/src/main/java/com/blog/service/impl/RoleServiceImpl.java` — 实现
- `blog-backend/src/main/java/com/blog/controller/RoleController.java` — 添加 POST/PUT/DELETE
- `blog-frontend/src/views/admin/MenuManageView.vue` — 字段映射修正
- `blog-frontend/src/views/admin/RoleManageView.vue` — 权限码修正

### Phase 2 — Important（功能补全）

**新建：**
- `blog-backend/src/main/java/com/blog/dto/article/ArticleNeighborsResponse.java`
- `blog-backend/src/main/java/com/blog/dto/auth/ProfileUpdateDTO.java`
- `blog-backend/src/main/java/com/blog/dto/user/UserUpdateDTO.java`
- `blog-backend/src/main/resources/db/migration/V4__add_user_nickname.sql`

**修改：**
- `blog-backend/src/main/java/com/blog/controller/ArticleController.java`
- `blog-backend/src/main/java/com/blog/service/ArticleService.java`
- `blog-backend/src/main/java/com/blog/service/impl/ArticleServiceImpl.java`
- `blog-backend/src/main/java/com/blog/controller/AuthController.java`
- `blog-backend/src/main/java/com/blog/service/AuthService.java`
- `blog-backend/src/main/java/com/blog/service/impl/AuthServiceImpl.java`
- `blog-backend/src/main/java/com/blog/controller/TagController.java`
- `blog-backend/src/main/java/com/blog/controller/UserController.java`
- `blog-backend/src/main/java/com/blog/service/UserService.java`
- `blog-backend/src/main/java/com/blog/service/impl/UserServiceImpl.java`
- `blog-backend/src/main/java/com/blog/entity/User.java`
- `blog-frontend/src/api/article.js`
- `blog-frontend/src/api/auth.js`
- `blog-frontend/src/views/admin/ArticleEditView.vue`
- `blog-frontend/src/views/front/PostDetailView.vue`
- `blog-frontend/src/views/admin/ProfileView.vue`

### Phase 3 — Polish（UI 对齐）

**新建：**
- `blog-frontend/src/components/common/EmptyState.vue`
- `blog-frontend/src/components/common/SkeletonCard.vue`
- `blog-frontend/src/components/common/ErrorState.vue`

**修改：**
- `blog-frontend/src/layouts/FrontLayout.vue`
- `blog-frontend/src/components/front/ArticleCard.vue`
- `blog-frontend/src/views/front/PostListView.vue`
- `blog-frontend/src/views/front/PostDetailView.vue`
- `blog-frontend/src/layouts/AdminLayout.vue`
- `blog-frontend/src/components/admin/AdminSidebar.vue`
- `blog-frontend/src/views/admin/ArticleListView.vue`
- `blog-frontend/src/views/admin/CategoryManageView.vue`
- `blog-frontend/src/views/admin/TagManageView.vue`
- `blog-frontend/src/views/admin/CommentManageView.vue`
- `blog-frontend/src/views/admin/UserManageView.vue`

---

## Phase 1：Critical

### Task 1：Menu 实体 + MenuDTO + MenuVO + MenuService

**Files:**
- Modify: `blog-backend/src/main/java/com/blog/entity/Menu.java`
- Create: `blog-backend/src/main/java/com/blog/dto/menu/MenuDTO.java`
- Create: `blog-backend/src/main/java/com/blog/dto/menu/MenuVO.java`
- Create: `blog-backend/src/main/java/com/blog/service/MenuService.java`
- Create: `blog-backend/src/main/java/com/blog/service/impl/MenuServiceImpl.java`

- [ ] **Step 1: 在 Menu 实体中添加 children 字段**

`blog-backend/src/main/java/com/blog/entity/Menu.java` — 在类末尾（`MenuType` 枚举之前）加：

```java
/** 子菜单列表，非数据库字段，由 Service 层组装 */
@TableField(exist = false)
private List<Menu> children;
```

完整 import 补充（文件顶部）：

```java
import java.util.List;
```

- [ ] **Step 2: 创建 MenuDTO**

```java
// blog-backend/src/main/java/com/blog/dto/menu/MenuDTO.java
package com.blog.dto.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MenuDTO {
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50)
    private String name;

    @Size(max = 200)
    private String path;

    @Size(max = 100)
    private String icon;

    /** 前端 sort → 后端 sortOrder */
    private Integer sort = 0;

    /** 前端 visible(boolean) → 后端 status(1/0) */
    private Boolean visible = true;

    private Long parentId;
}
```

- [ ] **Step 3: 创建 MenuVO**

```java
// blog-backend/src/main/java/com/blog/dto/menu/MenuVO.java
package com.blog.dto.menu;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MenuVO {
    private Long id;
    private String name;
    private String path;
    private String icon;
    private Integer sort;
    private Boolean visible;
    private Long parentId;
    private List<MenuVO> children;
}
```

- [ ] **Step 4: 创建 MenuService 接口**

```java
// blog-backend/src/main/java/com/blog/service/MenuService.java
package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.menu.MenuDTO;
import com.blog.dto.menu.MenuVO;
import com.blog.entity.Menu;
import java.util.List;

public interface MenuService extends IService<Menu> {
    List<MenuVO> listTree();
    MenuVO create(MenuDTO dto);
    MenuVO update(Long id, MenuDTO dto);
    void delete(Long id);
}
```

- [ ] **Step 5: 创建 MenuServiceImpl**

```java
// blog-backend/src/main/java/com/blog/service/impl/MenuServiceImpl.java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.exception.NotFoundException;
import com.blog.dto.menu.MenuDTO;
import com.blog.dto.menu.MenuVO;
import com.blog.entity.Menu;
import com.blog.mapper.MenuMapper;
import com.blog.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Override
    public List<MenuVO> listTree() {
        List<Menu> all = this.list(
            Wrappers.<Menu>lambdaQuery().orderByAsc(Menu::getSortOrder));
        return buildTree(all, null);
    }

    private List<MenuVO> buildTree(List<Menu> all, Long parentId) {
        return all.stream()
            .filter(m -> Objects.equals(m.getParentId(), parentId))
            .map(m -> MenuVO.builder()
                .id(m.getId())
                .name(m.getName())
                .path(m.getPath())
                .icon(m.getIcon())
                .sort(m.getSortOrder())
                .visible(m.getStatus() != null && m.getStatus() == 1)
                .parentId(m.getParentId())
                .children(buildTree(all, m.getId()))
                .build())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MenuVO create(MenuDTO dto) {
        Menu menu = toEntity(dto);
        this.save(menu);
        return toVO(menu);
    }

    @Override
    @Transactional
    public MenuVO update(Long id, MenuDTO dto) {
        Menu menu = this.getById(id);
        if (menu == null) throw new NotFoundException("菜单不存在");
        menu.setName(dto.getName());
        menu.setPath(dto.getPath());
        menu.setIcon(dto.getIcon());
        menu.setSortOrder(dto.getSort() != null ? dto.getSort() : 0);
        menu.setStatus(Boolean.TRUE.equals(dto.getVisible()) ? 1 : 0);
        menu.setParentId(dto.getParentId());
        this.updateById(menu);
        return toVO(menu);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        deleteRecursive(id);
    }

    private void deleteRecursive(Long id) {
        List<Menu> children = this.list(
            Wrappers.<Menu>lambdaQuery().eq(Menu::getParentId, id));
        children.forEach(c -> deleteRecursive(c.getId()));
        this.removeById(id);
    }

    private Menu toEntity(MenuDTO dto) {
        Menu menu = new Menu();
        menu.setName(dto.getName());
        menu.setPath(dto.getPath());
        menu.setIcon(dto.getIcon());
        menu.setSortOrder(dto.getSort() != null ? dto.getSort() : 0);
        menu.setStatus(Boolean.TRUE.equals(dto.getVisible()) ? 1 : 0);
        menu.setParentId(dto.getParentId());
        menu.setType(Menu.MenuType.MENU);
        return menu;
    }

    private MenuVO toVO(Menu menu) {
        return MenuVO.builder()
            .id(menu.getId())
            .name(menu.getName())
            .path(menu.getPath())
            .icon(menu.getIcon())
            .sort(menu.getSortOrder())
            .visible(menu.getStatus() != null && menu.getStatus() == 1)
            .parentId(menu.getParentId())
            .children(Collections.emptyList())
            .build();
    }
}
```

- [ ] **Step 6: 提交**

```bash
cd blog-backend
git add src/main/java/com/blog/entity/Menu.java \
        src/main/java/com/blog/dto/menu/ \
        src/main/java/com/blog/service/MenuService.java \
        src/main/java/com/blog/service/impl/MenuServiceImpl.java
git commit -m "feat: 新增 MenuService 及 DTO/VO（菜单管理后端第一步）"
```

---

### Task 2：MenuController + 测试

**Files:**
- Create: `blog-backend/src/main/java/com/blog/controller/MenuController.java`
- Create: `blog-backend/src/test/java/com/blog/controller/MenuControllerTest.java`

- [ ] **Step 1: 编写测试（先写，先跑失败）**

```java
// blog-backend/src/test/java/com/blog/controller/MenuControllerTest.java
package com.blog.controller;

import com.blog.dto.auth.LoginRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.JsonNode;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MenuControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    private String adminToken;

    @BeforeEach
    void setup() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("Admin@2024");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        adminToken = body.path("data").path("accessToken").asText();
    }

    @Test
    void listMenus_withAdminToken_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/api/admin/menus")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    void listMenus_withoutToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/admin/menus"))
                .andExpect(status().isForbidden());
    }

    @Test
    void createAndDeleteMenu_shouldWork() throws Exception {
        String body = """
            {"name":"测试菜单","path":"/test","icon":"","sort":99,"visible":true,"parentId":null}
            """;
        MvcResult created = mockMvc.perform(post("/api/admin/menus")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("测试菜单"))
                .andReturn();
        Long id = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("id").asLong();

        mockMvc.perform(delete("/api/admin/menus/" + id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 2: 运行测试（确认失败，因为 MenuController 不存在）**

```bash
cd blog-backend
mvn test -pl . -Dtest=MenuControllerTest -q 2>&1 | tail -5
```

预期：`ERRORS` 或编译失败

- [ ] **Step 3: 创建 MenuController**

```java
// blog-backend/src/main/java/com/blog/controller/MenuController.java
package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.menu.MenuDTO;
import com.blog.dto.menu.MenuVO;
import com.blog.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @PreAuthorize("hasAuthority('menu:manage')")
    public Result<List<MenuVO>> list() {
        return Result.success(menuService.listTree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('menu:manage')")
    public Result<MenuVO> create(@Valid @RequestBody MenuDTO dto) {
        return Result.success(menuService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('menu:manage')")
    public Result<MenuVO> update(@PathVariable Long id, @Valid @RequestBody MenuDTO dto) {
        return Result.success(menuService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('menu:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}
```

- [ ] **Step 4: 运行测试（确认通过）**

```bash
cd blog-backend
mvn test -pl . -Dtest=MenuControllerTest -q 2>&1 | tail -5
```

预期：`BUILD SUCCESS`

- [ ] **Step 5: 提交**

```bash
git add src/main/java/com/blog/controller/MenuController.java \
        src/test/java/com/blog/controller/MenuControllerTest.java
git commit -m "feat: 新增 MenuController（菜单 CRUD 完整实现）"
```

---

### Task 3：RoleDTO + RoleVO + RoleService + RoleController 补全

**Files:**
- Create: `blog-backend/src/main/java/com/blog/dto/role/RoleDTO.java`
- Create: `blog-backend/src/main/java/com/blog/dto/role/RoleVO.java`
- Modify: `blog-backend/src/main/java/com/blog/service/RoleService.java`
- Modify: `blog-backend/src/main/java/com/blog/service/impl/RoleServiceImpl.java`
- Modify: `blog-backend/src/main/java/com/blog/controller/RoleController.java`
- Create: `blog-backend/src/test/java/com/blog/controller/RoleControllerTest.java`

- [ ] **Step 1: 创建 RoleDTO**

```java
// blog-backend/src/main/java/com/blog/dto/role/RoleDTO.java
package com.blog.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class RoleDTO {
    @NotBlank(message = "角色名不能为空")
    @Size(max = 50)
    private String name;

    @Size(max = 200)
    private String description;

    /** 权限码列表，对应 menus 表中 BUTTON 类型的 code 字段 */
    private List<String> permissionCodes = new ArrayList<>();
}
```

- [ ] **Step 2: 创建 RoleVO**

```java
// blog-backend/src/main/java/com/blog/dto/role/RoleVO.java
package com.blog.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RoleVO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer status;

    /** 前端用 permissions[].code 展示权限 tag */
    private List<PermissionItem> permissions;

    @Data
    @AllArgsConstructor
    public static class PermissionItem {
        private Long id;
        private String code;
    }
}
```

- [ ] **Step 3: 更新 RoleService 接口**

```java
// blog-backend/src/main/java/com/blog/service/RoleService.java
package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.role.RoleDTO;
import com.blog.dto.role.RoleVO;
import com.blog.entity.Role;
import java.util.List;

public interface RoleService extends IService<Role> {
    List<RoleVO> listAllVO();
    RoleVO createRole(RoleDTO dto);
    RoleVO updateRole(Long id, RoleDTO dto);
    void deleteRole(Long id);
}
```

- [ ] **Step 4: 实现 RoleServiceImpl**

```java
// blog-backend/src/main/java/com/blog/service/impl/RoleServiceImpl.java
package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.exception.NotFoundException;
import com.blog.dto.role.RoleDTO;
import com.blog.dto.role.RoleVO;
import com.blog.entity.Menu;
import com.blog.entity.Role;
import com.blog.entity.RoleMenu;
import com.blog.mapper.MenuMapper;
import com.blog.mapper.RoleMapper;
import com.blog.mapper.RoleMenuMapper;
import com.blog.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;

    @Override
    public List<RoleVO> listAllVO() {
        return this.list().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleVO createRole(RoleDTO dto) {
        Role role = new Role();
        role.setName(dto.getName());
        role.setCode(dto.getName().toUpperCase().replace(" ", "_"));
        role.setDescription(dto.getDescription());
        this.save(role);
        assignPermissions(role.getId(), dto.getPermissionCodes());
        return toVO(role);
    }

    @Override
    @Transactional
    public RoleVO updateRole(Long id, RoleDTO dto) {
        Role role = this.getById(id);
        if (role == null) throw new NotFoundException("角色不存在");
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        this.updateById(role);
        assignPermissions(id, dto.getPermissionCodes());
        return toVO(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        roleMenuMapper.delete(Wrappers.<RoleMenu>lambdaQuery().eq(RoleMenu::getRoleId, id));
        this.removeById(id);
    }

    private void assignPermissions(Long roleId, List<String> codes) {
        roleMenuMapper.delete(Wrappers.<RoleMenu>lambdaQuery().eq(RoleMenu::getRoleId, roleId));
        if (codes == null || codes.isEmpty()) return;
        List<Menu> menus = menuMapper.selectList(
            Wrappers.<Menu>lambdaQuery().in(Menu::getCode, codes));
        List<RoleMenu> relations = menus.stream()
            .map(m -> new RoleMenu(roleId, m.getId()))
            .collect(Collectors.toList());
        if (!relations.isEmpty()) {
            relations.forEach(roleMenuMapper::insert);
        }
    }

    private RoleVO toVO(Role role) {
        List<Menu> menus = menuMapper.selectList(
            Wrappers.<Menu>lambdaQuery()
                .inSql(Menu::getId,
                    "SELECT menu_id FROM role_menus WHERE role_id = " + role.getId()));
        List<RoleVO.PermissionItem> permissions = menus.stream()
            .filter(m -> m.getCode() != null)
            .map(m -> new RoleVO.PermissionItem(m.getId(), m.getCode()))
            .collect(Collectors.toList());
        return RoleVO.builder()
            .id(role.getId())
            .name(role.getName())
            .code(role.getCode())
            .description(role.getDescription())
            .status(role.getStatus())
            .permissions(permissions)
            .build();
    }
}
```

- [ ] **Step 5: 编写 RoleController 测试**

```java
// blog-backend/src/test/java/com/blog/controller/RoleControllerTest.java
package com.blog.controller;

import com.blog.dto.auth.LoginRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RoleControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    private String adminToken;

    @BeforeEach
    void setup() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("Admin@2024");
        MvcResult res = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andReturn();
        adminToken = objectMapper.readTree(res.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();
    }

    @Test
    void listRoles_shouldIncludePermissionsField() throws Exception {
        mockMvc.perform(get("/api/admin/roles")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    void createAndDeleteRole_shouldWork() throws Exception {
        String body = """
            {"name":"测试角色","description":"仅测试用","permissionCodes":["article:list"]}
            """;
        MvcResult created = mockMvc.perform(post("/api/admin/roles")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("测试角色"))
                .andReturn();
        Long id = objectMapper.readTree(created.getResponse().getContentAsString())
                .path("data").path("id").asLong();
        mockMvc.perform(delete("/api/admin/roles/" + id)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
```

- [ ] **Step 6: 运行测试（先失败）**

```bash
mvn test -pl . -Dtest=RoleControllerTest -q 2>&1 | tail -5
```

预期：编译错误（RoleService 接口方法不匹配）

- [ ] **Step 7: 更新 RoleController**

```java
// blog-backend/src/main/java/com/blog/controller/RoleController.java
package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.role.RoleDTO;
import com.blog.dto.role.RoleVO;
import com.blog.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<List<RoleVO>> list() {
        return Result.success(roleService.listAllVO());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<RoleVO> create(@Valid @RequestBody RoleDTO dto) {
        return Result.success(roleService.createRole(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<RoleVO> update(@PathVariable Long id, @Valid @RequestBody RoleDTO dto) {
        return Result.success(roleService.updateRole(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }
}
```

- [ ] **Step 8: 运行测试（确认通过）**

```bash
mvn test -pl . -Dtest=RoleControllerTest -q 2>&1 | tail -5
```

预期：`BUILD SUCCESS`

- [ ] **Step 9: 提交**

```bash
git add src/main/java/com/blog/dto/role/ \
        src/main/java/com/blog/service/RoleService.java \
        src/main/java/com/blog/service/impl/RoleServiceImpl.java \
        src/main/java/com/blog/controller/RoleController.java \
        src/test/java/com/blog/controller/RoleControllerTest.java
git commit -m "feat: 补全角色管理 CRUD 接口（RoleVO + 权限分配）"
```

---

### Task 4：前端 — MenuManageView 字段修正 + RoleManageView 权限码修正

**Files:**
- Modify: `blog-frontend/src/views/admin/MenuManageView.vue`
- Modify: `blog-frontend/src/views/admin/RoleManageView.vue`

- [ ] **Step 1: 修正 MenuManageView 字段映射**

在 `MenuManageView.vue` 的 `<script setup>` 中找到 `openEdit` 函数，将：

```js
form.sort = row.sort || 0
form.visible = row.visible !== false
form.parentId = row.parentId || null
```

确认这些字段名与后端返回的 MenuVO 一致（MenuVO 返回 `sort` 和 `visible`，已对齐）。

找到加载菜单的函数（通常是 `loadMenus`），确认它将响应 `res.data` 直接赋给 `menus`：

```js
async function loadMenus() {
  loading.value = true
  try {
    const res = await getMenus()
    menus.value = res.data
  } catch {
    ElMessage.error('加载菜单失败')
  } finally {
    loading.value = false
  }
}
```

如果加载错误时只是静默失败（无提示），加上 `ElMessage.error('加载菜单失败')`。

- [ ] **Step 2: 修正 RoleManageView 的 allPermissions**

找到 `allPermissions` 数组，将整个数组替换为与数据库一致的 13 个权限码（来自 V1__init.sql）：

```js
const allPermissions = [
  { code: 'article:list',    label: '文章列表' },
  { code: 'article:create',  label: '文章创建' },
  { code: 'article:update',  label: '文章编辑' },
  { code: 'article:delete',  label: '文章删除' },
  { code: 'article:publish', label: '文章发布' },
  { code: 'comment:list',    label: '评论列表' },
  { code: 'comment:approve', label: '评论审核' },
  { code: 'comment:delete',  label: '评论删除' },
  { code: 'category:manage', label: '分类管理' },
  { code: 'tag:manage',      label: '标签管理' },
  { code: 'user:list',       label: '用户列表' },
  { code: 'role:manage',     label: '角色管理' },
  { code: 'menu:manage',     label: '菜单管理' },
]
```

同时将模板中的 checkbox 从：

```html
<el-checkbox v-for="perm in allPermissions" :key="perm" :label="perm">{{ perm }}</el-checkbox>
```

改为：

```html
<el-checkbox v-for="perm in allPermissions" :key="perm.code" :label="perm.code">{{ perm.label }}</el-checkbox>
```

- [ ] **Step 3: 验证**

启动后端（确保 DB 连接正常），启动前端 `npm run dev`，访问 `http://localhost:5173/admin/menus` 和 `/admin/roles`，确认：
- 菜单管理页加载树形数据，新建/编辑/删除正常
- 角色管理页权限列表显示 13 个中文标签

- [ ] **Step 4: 提交**

```bash
cd blog-frontend
git add src/views/admin/MenuManageView.vue src/views/admin/RoleManageView.vue
git commit -m "fix: 修正菜单管理字段映射，修正角色管理权限码列表（13个）"
```

---

## Phase 2：Important

### Task 5：文章编辑修复（后端 admin/{id} + 前端加载 + 封面 + slug）

**Files:**
- Modify: `blog-backend/src/main/java/com/blog/controller/ArticleController.java`
- Modify: `blog-backend/src/main/java/com/blog/service/ArticleService.java`
- Modify: `blog-backend/src/main/java/com/blog/service/impl/ArticleServiceImpl.java`
- Modify: `blog-frontend/src/api/article.js`
- Modify: `blog-frontend/src/views/admin/ArticleEditView.vue`

- [ ] **Step 1: 在 ArticleService 接口添加方法**

在 `blog-backend/src/main/java/com/blog/service/ArticleService.java` 末尾（`like()` 之后）添加：

```java
/**
 * 根据 ID 查询文章详情（管理端编辑用）
 *
 * @param id 文章 ID
 * @return 文章详情 DTO
 */
ArticleDetailResponse getByIdForAdmin(Long id);
```

- [ ] **Step 2: 在 ArticleServiceImpl 实现方法**

在 `ArticleServiceImpl` 中添加（参考已有的 `getBySlug` 实现方式）：

```java
@Override
public ArticleDetailResponse getByIdForAdmin(Long id) {
    Article article = this.getById(id);
    if (article == null) throw new NotFoundException("文章不存在");
    return buildDetailResponse(article);
}
```

在同类中找到 `getBySlug` 方法，其中调用了 `buildDetailResponse` 或直接组装 DTO。若没有单独的 `buildDetailResponse` 方法，则提取一个：

```java
private ArticleDetailResponse buildDetailResponse(Article article) {
    ArticleDetailResponse resp = new ArticleDetailResponse();
    resp.setId(article.getId());
    resp.setTitle(article.getTitle());
    resp.setSlug(article.getSlug());
    resp.setSummary(article.getSummary());
    resp.setContent(article.getContent());
    resp.setCoverImage(article.getCoverImage());
    resp.setViewCount(article.getViewCount());
    resp.setIsTop(article.getIsTop());
    resp.setAllowComment(article.getAllowComment());
    resp.setPublishedAt(article.getPublishedAt());
    resp.setCreatedAt(article.getCreatedAt());
    resp.setCategoryId(article.getCategoryId());
    if (article.getCategoryId() != null) {
        Category cat = categoryMapper.selectById(article.getCategoryId());
        if (cat != null) resp.setCategoryName(cat.getName());
    }
    List<Tag> tags = tagMapper.selectList(
        Wrappers.<Tag>lambdaQuery().inSql(Tag::getId,
            "SELECT tag_id FROM article_tags WHERE article_id = " + article.getId()));
    resp.setTagNames(tags.stream().map(Tag::getName).collect(Collectors.toList()));
    if (article.getAuthorId() != null) {
        User author = userMapper.selectById(article.getAuthorId());
        if (author != null) resp.setAuthorName(author.getUsername());
    }
    resp.setStatus(article.getStatus() != null ? article.getStatus().name() : "DRAFT");
    return resp;
}
```

> 注意：`ArticleDetailResponse` 需要 `status` 字段。检查该类是否已有，若没有则添加：`private String status;`

- [ ] **Step 3: 在 ArticleController 添加端点**

在 `ArticleController` 中（`@GetMapping("/{slug}")` 之后）添加：

```java
/**
 * 根据 ID 查询文章（管理端编辑用），需要 article:update 权限
 * GET /api/articles/admin/{id}
 */
@GetMapping("/admin/{id}")
@PreAuthorize("hasAuthority('article:update')")
public Result<ArticleDetailResponse> adminDetail(@PathVariable Long id) {
    return Result.success(articleService.getByIdForAdmin(id));
}
```

- [ ] **Step 4: 运行后端服务，用 curl 验证接口**

```bash
# 先获取 token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@2024"}' \
  | grep -o '"accessToken":"[^"]*"' | cut -d'"' -f4)

# 查询文章 ID=1
curl -s http://localhost:8080/api/articles/admin/1 \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool | head -20
```

预期：返回包含 `title`、`content`、`tagNames` 的完整 JSON

- [ ] **Step 5: 在前端 api/article.js 添加函数**

```js
export const getArticleById = (id) => request.get(`/articles/admin/${id}`)
```

- [ ] **Step 6: 完善 ArticleEditView.vue**

用以下完整 `<script setup>` 替换现有：

```js
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { createArticle, updateArticle, getArticleById } from '@/api/article'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'
import { uploadImage } from '@/api/upload'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const isEdit = computed(() => !!route.params.id)
const saving = ref(false)
const categories = ref([])
const tags = ref([])
const coverUploading = ref(false)

const form = reactive({
  title: '', slug: '', summary: '', content: '', coverImage: '',
  status: 'DRAFT', categoryId: null, tagNames: [], isTop: false, allowComment: true,
})

// slug 从标题自动生成（仅新建时自动，编辑时保持原 slug）
watch(() => form.title, (val) => {
  if (!isEdit.value && !form.slug) {
    form.slug = val.toLowerCase()
      .replace(/[\s_]+/g, '-')
      .replace(/[^\w\-一-龥]/g, '')
      .slice(0, 100)
  }
})

async function handleUpload(files, callback) {
  const results = await Promise.all(files.map(async f => {
    const res = await uploadImage(f)
    return res.data
  }))
  callback(results)
}

async function handleCoverUpload(file) {
  coverUploading.value = true
  try {
    const res = await uploadImage(file.raw || file)
    form.coverImage = res.data
  } catch {
    ElMessage.error('封面上传失败')
  } finally {
    coverUploading.value = false
  }
  return false  // 阻止 el-upload 默认行为
}

async function handleSave() {
  if (!form.title) return ElMessage.warning('请输入标题')
  if (!form.content) return ElMessage.warning('请输入内容')
  saving.value = true
  try {
    if (isEdit.value) {
      await updateArticle(route.params.id, form)
      ElMessage.success('保存成功')
    } else {
      await createArticle(form)
      ElMessage.success('发布成功')
      router.push('/admin/articles')
    }
  } catch (e) {
    ElMessage.error(e?.response?.data?.message || '保存失败，请重试')
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  const [catRes, tagRes] = await Promise.all([getCategories(), getTags()])
  categories.value = catRes.data
  tags.value = tagRes.data

  if (isEdit.value) {
    try {
      const res = await getArticleById(route.params.id)
      const a = res.data
      Object.assign(form, {
        title: a.title,
        slug: a.slug,
        summary: a.summary || '',
        content: a.content || '',
        coverImage: a.coverImage || '',
        status: a.status || 'DRAFT',
        categoryId: a.categoryId || null,
        tagNames: a.tagNames || [],
        isTop: a.isTop || false,
        allowComment: a.allowComment !== false,
      })
    } catch {
      ElMessage.error('加载文章失败')
    }
  }
})
```

同时在 `<template>` 的封面图区域（`<el-form-item label="摘要">` 之前）添加：

```html
<el-form-item label="封面图">
  <div style="display:flex;align-items:center;gap:12px">
    <img v-if="form.coverImage" :src="form.coverImage"
      style="width:120px;height:70px;object-fit:cover;border-radius:4px;border:1px solid var(--color-border)" />
    <el-upload :before-upload="handleCoverUpload" :show-file-list="false" accept="image/*">
      <el-button :loading="coverUploading" size="small">
        {{ form.coverImage ? '更换封面' : '上传封面' }}
      </el-button>
    </el-upload>
    <el-button v-if="form.coverImage" size="small" @click="form.coverImage = ''">移除</el-button>
  </div>
</el-form-item>
<el-form-item label="Slug">
  <el-input v-model="form.slug" placeholder="URL 路径（自动生成，可手动修改）" />
</el-form-item>
```

- [ ] **Step 7: 验证**

在前端访问文章列表，点击任意文章的"编辑"按钮，确认表单加载了正确的标题、内容、分类、标签等数据。

- [ ] **Step 8: 提交**

```bash
cd ..  # 回到 my-ai-blog 根目录
git add blog-backend/src/main/java/com/blog/controller/ArticleController.java \
        blog-backend/src/main/java/com/blog/service/ArticleService.java \
        blog-backend/src/main/java/com/blog/service/impl/ArticleServiceImpl.java \
        blog-frontend/src/api/article.js \
        blog-frontend/src/views/admin/ArticleEditView.vue
git commit -m "fix: 修复文章编辑模式空白问题，新增封面图上传和 slug 自动生成"
```

---

### Task 6：文章邻篇导航

**Files:**
- Create: `blog-backend/src/main/java/com/blog/dto/article/ArticleNeighborsResponse.java`
- Modify: `blog-backend/src/main/java/com/blog/service/ArticleService.java`
- Modify: `blog-backend/src/main/java/com/blog/service/impl/ArticleServiceImpl.java`
- Modify: `blog-backend/src/main/java/com/blog/controller/ArticleController.java`
- Modify: `blog-frontend/src/views/front/PostDetailView.vue`

- [ ] **Step 1: 创建响应 DTO**

```java
// blog-backend/src/main/java/com/blog/dto/article/ArticleNeighborsResponse.java
package com.blog.dto.article;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArticleNeighborsResponse {
    private NeighborItem prev;
    private NeighborItem next;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NeighborItem {
        private String title;
        private String slug;
    }
}
```

- [ ] **Step 2: 在 ArticleService 添加接口方法**

```java
ArticleNeighborsResponse getNeighbors(String slug);
```

- [ ] **Step 3: 实现 getNeighbors**

在 `ArticleServiceImpl` 中添加：

```java
@Override
public ArticleNeighborsResponse getNeighbors(String slug) {
    Article current = this.getOne(
        Wrappers.<Article>lambdaQuery().eq(Article::getSlug, slug));
    if (current == null) throw new NotFoundException("文章不存在");

    Article prev = this.getOne(
        Wrappers.<Article>lambdaQuery()
            .eq(Article::getStatus, Article.ArticleStatus.PUBLISHED)
            .lt(Article::getId, current.getId())
            .orderByDesc(Article::getId)
            .last("LIMIT 1"));

    Article next = this.getOne(
        Wrappers.<Article>lambdaQuery()
            .eq(Article::getStatus, Article.ArticleStatus.PUBLISHED)
            .gt(Article::getId, current.getId())
            .orderByAsc(Article::getId)
            .last("LIMIT 1"));

    return new ArticleNeighborsResponse(
        prev == null ? null : new ArticleNeighborsResponse.NeighborItem(prev.getTitle(), prev.getSlug()),
        next == null ? null : new ArticleNeighborsResponse.NeighborItem(next.getTitle(), next.getSlug())
    );
}
```

- [ ] **Step 4: 在 ArticleController 添加端点**

```java
/**
 * 获取上一篇/下一篇文章（前台）
 * GET /api/articles/{slug}/neighbors
 */
@GetMapping("/{slug}/neighbors")
public Result<ArticleNeighborsResponse> neighbors(@PathVariable String slug) {
    return Result.success(articleService.getNeighbors(slug));
}
```

- [ ] **Step 5: 在 PostDetailView.vue 添加邻篇区域**

在 `<script setup>` 中添加：

```js
import { getArticleNeighbors } from '@/api/article'
const neighbors = ref({ prev: null, next: null })

// 在加载文章详情的函数末尾（获取 slug 后）调用：
const nRes = await getArticleNeighbors(slug)
neighbors.value = nRes.data
```

在模板文章内容区域底部（评论区上方）添加：

```html
<!-- 上/下篇导航 -->
<div v-if="neighbors.prev || neighbors.next" class="neighbors-nav">
  <router-link v-if="neighbors.prev" :to="`/posts/${neighbors.prev.slug}`" class="neighbor-card neighbor-prev">
    <span class="neighbor-dir">← 上一篇</span>
    <span class="neighbor-title">{{ neighbors.prev.title }}</span>
  </router-link>
  <router-link v-if="neighbors.next" :to="`/posts/${neighbors.next.slug}`" class="neighbor-card neighbor-next">
    <span class="neighbor-dir">下一篇 →</span>
    <span class="neighbor-title">{{ neighbors.next.title }}</span>
  </router-link>
</div>
```

在 `<style scoped>` 添加：

```css
.neighbors-nav {
  display: flex;
  gap: 16px;
  margin: 40px 0;
}
.neighbor-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 16px 20px;
  background: #13131E;
  border-radius: 8px;
  text-decoration: none;
  border: 1px solid #1C1C2C;
  transition: border-color 0.2s;
}
.neighbor-card:hover { border-color: #E8A838; }
.neighbor-next { text-align: right; }
.neighbor-dir { font-size: 12px; color: #6E6E82; }
.neighbor-title { font-size: 14px; font-weight: 600; color: #F0F0F8; }
```

- [ ] **Step 6: 提交**

```bash
git add blog-backend/src/main/java/com/blog/dto/article/ArticleNeighborsResponse.java \
        blog-backend/src/main/java/com/blog/service/ArticleService.java \
        blog-backend/src/main/java/com/blog/service/impl/ArticleServiceImpl.java \
        blog-backend/src/main/java/com/blog/controller/ArticleController.java \
        blog-frontend/src/views/front/PostDetailView.vue
git commit -m "feat: 文章邻篇导航（上一篇/下一篇）"
```

---

### Task 7：个人资料保存 + 标签编辑 + 用户编辑 + Nickname 字段

**Files:**
- Create: `blog-backend/src/main/resources/db/migration/V4__add_user_nickname.sql`
- Create: `blog-backend/src/main/java/com/blog/dto/auth/ProfileUpdateDTO.java`
- Create: `blog-backend/src/main/java/com/blog/dto/user/UserUpdateDTO.java`
- Modify: `blog-backend/src/main/java/com/blog/entity/User.java`
- Modify: `blog-backend/src/main/java/com/blog/controller/AuthController.java`
- Modify: `blog-backend/src/main/java/com/blog/service/AuthService.java`
- Modify: `blog-backend/src/main/java/com/blog/service/impl/AuthServiceImpl.java`
- Modify: `blog-backend/src/main/java/com/blog/controller/TagController.java`
- Modify: `blog-backend/src/main/java/com/blog/controller/UserController.java`
- Modify: `blog-backend/src/main/java/com/blog/service/UserService.java`
- Modify: `blog-backend/src/main/java/com/blog/service/impl/UserServiceImpl.java`
- Modify: `blog-frontend/src/api/auth.js`
- Modify: `blog-frontend/src/views/admin/ProfileView.vue`

- [ ] **Step 1: Flyway 迁移 — 添加 nickname 列**

```sql
-- blog-backend/src/main/resources/db/migration/V4__add_user_nickname.sql
ALTER TABLE users ADD COLUMN nickname VARCHAR(50) NULL COMMENT '用户昵称' AFTER bio;
```

- [ ] **Step 2: User 实体添加 nickname + roles 字段**

在 `User.java` 中 `bio` 字段后添加：

```java
/** 用户昵称，显示名称 */
private String nickname;

/** 用户角色列表，非数据库字段，由 Service 层填充 */
@TableField(exist = false)
private List<Role> roles;
```

同时添加 import：

```java
import com.blog.entity.Role;
import java.util.List;
```

- [ ] **Step 3: 创建 ProfileUpdateDTO**

```java
// blog-backend/src/main/java/com/blog/dto/auth/ProfileUpdateDTO.java
package com.blog.dto.auth;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateDTO {
    @Size(max = 500, message = "简介不能超过 500 字")
    private String bio;
}
```

- [ ] **Step 4: 在 AuthService 添加接口方法**

```java
UserInfoResponse updateProfile(String username, ProfileUpdateDTO dto);
```

- [ ] **Step 5: 在 AuthServiceImpl 实现**

```java
@Override
@Transactional
public UserInfoResponse updateProfile(String username, ProfileUpdateDTO dto) {
    User user = this.getOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
    if (user == null) throw new NotFoundException("用户不存在");
    if (dto.getBio() != null) user.setBio(dto.getBio());
    this.updateById(user);
    return getCurrentUser(username);
}
```

- [ ] **Step 6: 在 AuthController 添加端点**

```java
@PatchMapping("/profile")
public Result<UserInfoResponse> updateProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @Valid @RequestBody ProfileUpdateDTO dto) {
    return Result.success(authService.updateProfile(userDetails.getUsername(), dto));
}
```

同时在 AuthController 顶部添加 import：

```java
import com.blog.dto.auth.ProfileUpdateDTO;
```

- [ ] **Step 7: 在 TagController 添加 PUT /{id}**

在 `@PostMapping` 方法之后添加：

```java
@PutMapping("/{id}")
@PreAuthorize("hasAuthority('tag:manage')")
public Result<Tag> update(@PathVariable Long id, @Valid @RequestBody TagRequest req) {
    Tag tag = tagService.getById(id);
    if (tag == null) return Result.error(404, "标签不存在");
    tag.setName(req.getName());
    tag.setSlug(req.getSlug() != null ? req.getSlug() : tag.getSlug());
    tagService.updateById(tag);
    return Result.success(tag);
}
```

- [ ] **Step 8: 创建 UserUpdateDTO**

```java
// blog-backend/src/main/java/com/blog/dto/user/UserUpdateDTO.java
package com.blog.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserUpdateDTO {
    @Size(max = 50)
    private String nickname;

    private List<Long> roleIds = new ArrayList<>();
}
```

- [ ] **Step 9: 在 UserService 添加接口方法**

```java
import com.blog.dto.user.UserUpdateDTO;
// ...
User updateUser(Long id, UserUpdateDTO dto);
```

- [ ] **Step 10: 在 UserServiceImpl 实现**

```java
@Override
@Transactional
public User updateUser(Long id, UserUpdateDTO dto) {
    User user = this.getById(id);
    if (user == null) throw new NotFoundException("用户不存在");
    if (dto.getNickname() != null) user.setNickname(dto.getNickname());
    this.updateById(user);
    userRoleMapper.deleteByUserId(id);
    if (dto.getRoleIds() != null) {
        dto.getRoleIds().forEach(roleId ->
            userRoleMapper.insert(new UserRole(id, roleId)));
    }
    // 填充 roles 用于前端回显
    List<Role> roles = roleMapper.selectList(
        Wrappers.<Role>lambdaQuery().inSql(Role::getId,
            "SELECT role_id FROM user_roles WHERE user_id = " + id));
    user.setRoles(roles);
    return user;
}
```

需要在 UserServiceImpl 注入：

```java
private final RoleMapper roleMapper;
private final UserRoleMapper userRoleMapper;
```

- [ ] **Step 11: 在 UserController 添加 PUT /{id}**

```java
@PutMapping("/{id}")
@PreAuthorize("hasAuthority('user:list')")
public Result<User> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
    return Result.success(userService.updateUser(id, dto));
}
```

添加 import：

```java
import com.blog.dto.user.UserUpdateDTO;
```

- [ ] **Step 12: 更新 UserService.listUsers — 返回时携带 roles**

在 UserServiceImpl 的 `listUsers` 方法中，分页查询完用户后，批量填充 roles：

```java
// 在 PageResult 返回之前
List<User> records = page.getRecords();
records.forEach(u -> {
    List<Role> roles = roleMapper.selectList(
        Wrappers.<Role>lambdaQuery().inSql(Role::getId,
            "SELECT role_id FROM user_roles WHERE user_id = " + u.getId()));
    u.setRoles(roles);
});
```

- [ ] **Step 13: 前端 — api/auth.js 添加 updateProfile**

```js
export const updateProfile = (data) => request.patch('/auth/profile', data)
```

- [ ] **Step 14: 前端 — ProfileView.vue 启用保存**

将 `<el-button type="primary" disabled>保存（需后端 PATCH /api/users/profile 接口）</el-button>` 替换为：

```html
<el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
```

在 `<script setup>` 中添加：

```js
import { updateProfile } from '@/api/auth'
const saving = ref(false)

async function handleSave() {
  saving.value = true
  try {
    await updateProfile({ bio: form.bio })
    ElMessage.success('保存成功')
    userStore.fetchUserInfo()
  } catch {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}
```

补充 import：

```js
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
```

- [ ] **Step 15: 重启后端（Flyway 会自动执行 V4 迁移），验证**

```bash
cd blog-backend && mvn spring-boot:run
```

Flyway 日志应出现：`Successfully applied 1 migration to schema 'techblog' (V4__add_user_nickname.sql)`

访问 `/admin/profile`，修改简介，点保存，确认弹出"保存成功"。

- [ ] **Step 16: 提交**

```bash
git add blog-backend/src/main/resources/db/migration/V4__add_user_nickname.sql \
        blog-backend/src/main/java/com/blog/dto/auth/ProfileUpdateDTO.java \
        blog-backend/src/main/java/com/blog/dto/user/UserUpdateDTO.java \
        blog-backend/src/main/java/com/blog/entity/User.java \
        blog-backend/src/main/java/com/blog/controller/AuthController.java \
        blog-backend/src/main/java/com/blog/service/AuthService.java \
        blog-backend/src/main/java/com/blog/service/impl/AuthServiceImpl.java \
        blog-backend/src/main/java/com/blog/controller/TagController.java \
        blog-backend/src/main/java/com/blog/controller/UserController.java \
        blog-backend/src/main/java/com/blog/service/UserService.java \
        blog-backend/src/main/java/com/blog/service/impl/UserServiceImpl.java \
        blog-frontend/src/api/auth.js \
        blog-frontend/src/views/admin/ProfileView.vue
git commit -m "feat: 个人资料保存、标签编辑、用户编辑（含昵称字段 V4 迁移）"
```

---

## Phase 3：Polish

### Task 8：公共 UI 组件（EmptyState / SkeletonCard / ErrorState）

**Files:**
- Create: `blog-frontend/src/components/common/EmptyState.vue`
- Create: `blog-frontend/src/components/common/SkeletonCard.vue`
- Create: `blog-frontend/src/components/common/ErrorState.vue`

- [ ] **Step 1: 创建 EmptyState.vue**

```vue
<!-- blog-frontend/src/components/common/EmptyState.vue -->
<template>
  <div class="empty-state">
    <div class="empty-icon">{{ icon }}</div>
    <div class="empty-title">{{ title }}</div>
    <div v-if="subtitle" class="empty-subtitle">{{ subtitle }}</div>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '📭' },
  title: { type: String, default: '暂无数据' },
  subtitle: { type: String, default: '' },
})
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 10px;
  color: var(--color-text-secondary, #8A8A9E);
}
.empty-icon { font-size: 40px; }
.empty-title { font-size: 16px; font-weight: 600; color: var(--color-text, #F0F0F8); }
.empty-subtitle { font-size: 13px; }
</style>
```

- [ ] **Step 2: 创建 SkeletonCard.vue**

```vue
<!-- blog-frontend/src/components/common/SkeletonCard.vue -->
<template>
  <div class="skeleton-card">
    <div class="sk-image" />
    <div class="sk-body">
      <div class="sk-line sk-title" />
      <div class="sk-line sk-subtitle" />
      <div class="sk-line sk-meta" />
    </div>
  </div>
</template>

<style scoped>
@keyframes shimmer {
  0% { background-position: -400px 0; }
  100% { background-position: 400px 0; }
}
.skeleton-card {
  background: #13131E;
  border-radius: 8px;
  overflow: hidden;
}
.sk-image {
  height: 140px;
  background: linear-gradient(90deg, #1C1C2C 25%, #252545 50%, #1C1C2C 75%);
  background-size: 400px 100%;
  animation: shimmer 1.4s infinite linear;
}
.sk-body { padding: 16px; display: flex; flex-direction: column; gap: 10px; }
.sk-line {
  border-radius: 4px;
  background: linear-gradient(90deg, #1C1C2C 25%, #252545 50%, #1C1C2C 75%);
  background-size: 400px 100%;
  animation: shimmer 1.4s infinite linear;
}
.sk-title { height: 16px; width: 80%; }
.sk-subtitle { height: 12px; width: 60%; }
.sk-meta { height: 10px; width: 40%; }
</style>
```

- [ ] **Step 3: 创建 ErrorState.vue**

```vue
<!-- blog-frontend/src/components/common/ErrorState.vue -->
<template>
  <div class="error-state">
    <div class="error-box">
      <span class="error-icon">⚠</span>
      <span class="error-msg">{{ message }}</span>
    </div>
    <button v-if="onRetry" class="retry-btn" @click="onRetry">重试</button>
  </div>
</template>

<script setup>
defineProps({
  message: { type: String, default: '加载失败，请刷新重试' },
  onRetry: { type: Function, default: null },
})
</script>

<style scoped>
.error-state { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 40px; }
.error-box {
  display: flex; align-items: center; gap: 8px;
  background: #2A1010; border: 1px solid #5C1010; border-radius: 6px;
  padding: 12px 20px; color: #F87171; font-size: 13px;
}
.error-icon { font-size: 16px; }
.retry-btn {
  background: #1A1A28; border: 1px solid #3A3A5C; color: #F0F0F8;
  padding: 6px 16px; border-radius: 6px; cursor: pointer; font-size: 13px;
}
.retry-btn:hover { border-color: #E8A838; }
</style>
```

- [ ] **Step 4: 提交**

```bash
cd blog-frontend
git add src/components/common/EmptyState.vue \
        src/components/common/SkeletonCard.vue \
        src/components/common/ErrorState.vue
git commit -m "feat: 新增公共 UI 组件（EmptyState/SkeletonCard/ErrorState）"
```

---

### Task 9：FrontLayout 导航栏 + ArticleCard 对齐 Pencil

**Files:**
- Modify: `blog-frontend/src/layouts/FrontLayout.vue`
- Modify: `blog-frontend/src/components/front/ArticleCard.vue`

- [ ] **Step 1: 修正 FrontLayout 导航栏 CSS**

在 `FrontLayout.vue` 的 `<style>` 中，找到 `.header-inner` 和 `.header`，修改为：

```css
.header {
  height: 72px;
  background: var(--color-bg, #0C0C10);
  border-bottom: 1px solid #1C1C2C;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1440px;
  margin: 0 auto;
  height: 100%;
  display: flex;
  align-items: center;
  padding: 0 48px;
}
.brand {
  font-size: 24px;
  font-weight: 700;
  color: #E8A838;
  text-decoration: none;
  margin-right: auto;
}
.nav {
  display: flex;
  gap: 32px;
  margin-right: 20px;
}
.nav-link {
  font-size: 14px;
  color: #9CA3AF;
  text-decoration: none;
  font-weight: normal;
  transition: color 0.2s;
}
.nav-link.router-link-active,
.nav-link.router-link-exact-active {
  color: #E8A838;
  font-weight: 600;
}
.nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}
.search-icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #6E6E82;
  display: flex;
  align-items: center;
}
.nav-cta {
  background: #E8A838;
  color: #0C0C10;
  font-size: 13px;
  font-weight: 600;
  padding: 7px 18px;
  border-radius: 6px;
  text-decoration: none;
}
```

- [ ] **Step 2: 修正 ArticleCard 样式**

在 `ArticleCard.vue` 的 `<style scoped>` 中：

```css
.article-card {
  background: #13131E;
  border: 1px solid #1C1C2C;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  transition: border-color 0.2s;
}
.article-card:hover { border-color: rgba(232, 168, 56, 0.4); }

.cover {
  width: 100%;
  height: 140px;
  overflow: hidden;
  background: #1C1C2E;
}
.cover img { width: 100%; height: 100%; object-fit: cover; }

.body {
  padding: 20px 20px 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-top { display: flex; gap: 6px; flex-wrap: wrap; }
.badge {
  font-size: 11px;
  font-weight: 600;
  padding: 2px 8px;
  border-radius: 3px;
}
.badge-top { background: #1C1C2E; color: #E8A838; }
.badge-cat { background: #1C1C2E; color: #8A8A9E; }

.title {
  font-size: 16px;
  font-weight: 600;
  color: #F0F0F8;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.summary {
  font-size: 13px;
  color: #8A8A9E;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.meta-bottom { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.meta-item { font-size: 12px; color: #6E6E82; display: flex; align-items: center; gap: 4px; }
.meta-icon { width: 12px; height: 12px; }
.tag-link { font-size: 11px; color: #6E6E82; cursor: pointer; }
.tag-link:hover { color: #E8A838; }
```

- [ ] **Step 3: 验证**

访问 `http://localhost:5173`，确认：
- 导航栏高度 72px，品牌名 amber 色，激活链接 amber 色
- 文章卡片深色背景，封面图高度一致

- [ ] **Step 4: 提交**

```bash
git add src/layouts/FrontLayout.vue src/components/front/ArticleCard.vue
git commit -m "polish: 对齐 Pencil 设计——前台导航栏和文章卡片"
```

---

### Task 10：PostListView 筛选栏 + 分页

**Files:**
- Modify: `blog-frontend/src/views/front/PostListView.vue`

- [ ] **Step 1: 找到 FilterBar 区域**

在 `PostListView.vue` 模板中找到分类筛选区域（通常是一排分类按钮），确认其有对应的样式类（如 `.filter-bar`），修改样式：

```css
.filter-bar {
  background: #13131E;
  padding: 16px 64px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.filter-label {
  font-size: 13px;
  color: #6B7280;
}
.filter-btn {
  height: 32px;
  padding: 0 16px;
  border-radius: 6px;
  border: none;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s, color 0.2s;
}
.filter-btn--active {
  background: #E8A838;
  color: #000;
  font-weight: 600;
}
.filter-btn--inactive {
  background: #1E2030;
  color: #D1D5DB;
}
.filter-btn--inactive:hover {
  background: #2A2A40;
}
```

- [ ] **Step 2: 修正分页样式**

找到 `<el-pagination>` 或自定义分页组件，如使用 Element Plus 分页，添加样式覆盖：

```css
/* 覆盖 Element Plus 分页按钮颜色 */
:deep(.el-pagination .el-pager li.is-active) {
  background: #E8A838;
  color: #000;
  border-radius: 6px;
}
:deep(.el-pagination .el-pager li) {
  background: #13131E;
  color: #9CA3AF;
  border-radius: 6px;
}
:deep(.el-pagination button) {
  background: #13131E;
  color: #9CA3AF;
}
```

- [ ] **Step 3: 确保文章网格布局为 3 列**

```css
.article-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  padding: 40px 64px;
}
@media (max-width: 1024px) {
  .article-grid { grid-template-columns: repeat(2, 1fr); }
}
@media (max-width: 640px) {
  .article-grid { grid-template-columns: 1fr; }
}
```

- [ ] **Step 4: 提交**

```bash
git add src/views/front/PostListView.vue
git commit -m "polish: 文章列表筛选栏和分页样式对齐 Pencil"
```

---

### Task 11：AdminLayout 侧边栏 + Topbar

**Files:**
- Modify: `blog-frontend/src/layouts/AdminLayout.vue`
- Modify: `blog-frontend/src/components/admin/AdminSidebar.vue`

- [ ] **Step 1: 修正 AdminLayout 样式**

在 `AdminLayout.vue` 的 `<style>` 中：

```css
.admin-layout {
  display: flex;
  height: 100vh;
  background: #0A0A12;
}
.sidebar {
  width: 240px;
  flex-shrink: 0;
  background: #111119;
  border-right: 1px solid #1E1E2C;
  display: flex;
  flex-direction: column;
  transition: width 0.2s;
}
.sidebar.collapsed { width: 64px; }
.admin-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
.admin-header {
  height: 64px;
  background: #111119;
  border-bottom: 1px solid #1E1E2C;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
}
.admin-main {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: #0A0A12;
}
.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #E8A838;
  color: #000;
  font-size: 14px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.username { font-size: 13px; color: #F0F0F8; }
```

- [ ] **Step 2: 修正 AdminSidebar 菜单项样式**

在 `AdminSidebar.vue` 的 `<style scoped>` 中：

```css
.sidebar-logo {
  height: 64px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 10px;
  border-bottom: 1px solid #1E1E2C;
}
.logo-icon {
  width: 32px;
  height: 32px;
  background: #E8A838;
  border-radius: 6px;
  flex-shrink: 0;
}
.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #F9FAFB;
}
.sidebar-nav {
  flex: 1;
  padding: 16px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 40px;
  padding: 0 8px;
  border-radius: 6px;
  cursor: pointer;
  text-decoration: none;
  transition: background 0.15s;
  font-size: 13px;
  color: #6E6E82;
}
.nav-item.router-link-active,
.nav-item.active {
  background: #1E1E30;
  color: #E8A838;
  font-weight: 600;
}
.nav-item:hover:not(.router-link-active) {
  background: #16162A;
  color: #9CA3AF;
}
.nav-icon { width: 16px; height: 16px; flex-shrink: 0; }
.sidebar-user {
  padding: 12px 20px;
  border-top: 1px solid #1E1E2C;
  display: flex;
  align-items: center;
  gap: 8px;
}
.user-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #E8A838;
  color: #000;
  font-size: 13px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
}
.user-info .user-name { font-size: 13px; font-weight: 600; color: #F0F0F8; }
.user-info .user-role { font-size: 11px; color: #3A3A5C; }
```

- [ ] **Step 3: 验证**

访问 `/admin`，确认：
- 侧边栏宽 240px，深色背景，激活项 amber 色
- 顶栏高 64px，用户头像 amber 圆形
- 主内容区深色背景

- [ ] **Step 4: 提交**

```bash
git add src/layouts/AdminLayout.vue src/components/admin/AdminSidebar.vue
git commit -m "polish: 对齐 Pencil 设计——后台侧边栏和 Topbar"
```

---

### Task 12：后台列表页通用表格样式

**Files:**
- Modify: `blog-frontend/src/views/admin/ArticleListView.vue`
- Modify: `blog-frontend/src/views/admin/CategoryManageView.vue`
- Modify: `blog-frontend/src/views/admin/TagManageView.vue`
- Modify: `blog-frontend/src/views/admin/CommentManageView.vue`
- Modify: `blog-frontend/src/views/admin/UserManageView.vue`

- [ ] **Step 1: 为每个管理页面添加统一样式**

以下 CSS 添加到每个管理页面的 `<style scoped>` 中（各页面已有类名不同，按实际调整）：

```css
/* 页面头部 */
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
}
.page-title {
  font-size: 18px;
  font-weight: 700;
  color: #F9FAFB;
}

/* 工具栏 */
.toolbar {
  height: 56px;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

/* 搜索框 / 筛选下拉 覆盖 Element Plus */
:deep(.el-input__wrapper) {
  background: #1A1A28 !important;
  border-color: #2A2A3C !important;
  box-shadow: none !important;
}
:deep(.el-input__inner) {
  color: #F0F0F8 !important;
}
:deep(.el-select .el-input__wrapper) {
  background: #1A1A28 !important;
  border-color: #2A2A3C !important;
}

/* 主色按钮 */
:deep(.el-button--primary) {
  background: #E8A838 !important;
  border-color: #E8A838 !important;
  color: #000 !important;
  font-weight: 600;
}
:deep(.el-button--primary:hover) {
  background: #F0B840 !important;
}

/* 表格 */
:deep(.el-table) {
  background: transparent !important;
  color: #F0F0F8;
}
:deep(.el-table__header-wrapper th) {
  background: #111119 !important;
  color: #6E6E82;
  font-weight: 500;
  height: 44px;
}
:deep(.el-table__row) {
  background: transparent !important;
  height: 60px;
}
:deep(.el-table__row:nth-child(even)) {
  background: #0D0D1A !important;
}
:deep(.el-table__row:hover td) {
  background: #16162A !important;
}
:deep(.el-table td) {
  border-bottom: 1px solid #1C1C2C !important;
}
:deep(.el-table th) {
  border-bottom: 1px solid #1E1E2C !important;
}

/* 状态 badge */
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}
.status-published { background: #1A2010; color: #6FCF97; }
.status-draft { background: #1A1028; color: #9CA3AF; }
```

- [ ] **Step 2: 将已有的状态显示替换为 badge 格式**

在每个列表页中，找到状态列的 `<template>` 插槽，替换为：

```html
<template #default="{ row }">
  <span :class="['status-badge', row.status === 'PUBLISHED' ? 'status-published' : 'status-draft']">
    {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
  </span>
</template>
```

- [ ] **Step 3: 验证所有后台页面外观一致**

逐一访问 `/admin/articles`、`/admin/categories`、`/admin/tags`、`/admin/comments`、`/admin/users`，确认表头、行高、颜色统一。

- [ ] **Step 4: 提交**

```bash
git add src/views/admin/ArticleListView.vue \
        src/views/admin/CategoryManageView.vue \
        src/views/admin/TagManageView.vue \
        src/views/admin/CommentManageView.vue \
        src/views/admin/UserManageView.vue
git commit -m "polish: 统一后台列表页表格样式（深色主题对齐 Pencil）"
```

---

## 自检清单

- [ ] Phase 1 所有后端测试通过：`mvn test -Dtest="MenuControllerTest,RoleControllerTest" -q`
- [ ] Phase 1 前端：菜单/角色管理页无 404
- [ ] Phase 2：文章编辑模式加载现有数据
- [ ] Phase 2：文章详情显示上/下篇卡片
- [ ] Phase 2：个人资料保存返回 200
- [ ] Phase 2：标签编辑和用户编辑后端返回 200
- [ ] Phase 3：导航栏高度 72px，品牌名 #E8A838
- [ ] Phase 3：文章卡片 #13131E 背景，封面 140px
- [ ] Phase 3：后台侧边栏 240px，激活项 amber
- [ ] Phase 3：后台表格行高 60px，表头 #111119
