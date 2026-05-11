# 后台 C 批 · 列表批量操作 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 给后台「文章管理」「评论审核」加批量操作能力（文章批删除；评论批通过/拒绝/删除）。Backend 走 TDD 红→绿，Frontend 新增 BulkActionBar 通用组件并接入。

**Architecture:** Backend 加 3 个 batch endpoint（事务原子，权限沿用单行接口），Mapper 用现有注解风格（非 XML）。Frontend 把 DataTable 升级为可选模式（v-if selection column），新建 BulkActionBar 顶部内联条，两 list view 接入即可。

**Tech Stack:** Spring Boot 3.2 / MyBatis-Plus 3.5 (BaseMapper) / Vue 3.4 / Element Plus 2.6

**Spec 来源：** `docs/superpowers/specs/2026-05-11-admin-batch-C-design.md`

**前置：** 当前在 master 分支，A 批 + B 批已合并。

**Mapper 风格说明：** 项目现有 ArticleMapper/CommentMapper 用 `@Select`/`@Update` 注解 + `BaseMapper<T>` (mybatis-plus)，**无 XML**。本批新增的批量 SQL 统一用注解 + MyBatis `<foreach>` 的字符串拼接版本，或用 mybatis-plus 提供的 `BaseMapper#deleteBatchIds(Collection)`。Spec §9 表里"`*.xml`"的措辞按此修正——不创建 XML 文件。

**Backend mvn 调用前缀**（mvn 不在 PATH，看 memory `build_env.md`）：
```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn <command>
```

---

## 分支策略

```bash
git checkout -b feat/admin-batch-C-bulk-actions
```
所有 task commit 在这条分支上，最后合 master。

---

## 文件结构总览

| 文件 | 操作 | 责任 |
|------|------|------|
| `blog-backend/src/main/java/com/blog/dto/common/BatchIdsDTO.java` | 新建 | 通用批量 ids 入参 |
| `blog-backend/src/main/java/com/blog/dto/comment/BatchStatusDTO.java` | 新建 | 评论批改状态入参（含 status 字段） |
| `blog-backend/src/main/java/com/blog/service/ArticleService.java` | 改 | 加 `batchDelete(List<Long> ids)` 签名 |
| `blog-backend/src/main/java/com/blog/service/impl/ArticleServiceImpl.java` | 改 | 实现 batchDelete（含 article_tags 清理） |
| `blog-backend/src/main/java/com/blog/controller/ArticleController.java` | 改 | 加 `POST /api/articles/batch-delete` |
| `blog-backend/src/main/java/com/blog/service/CommentService.java` | 改 | 加 `batchDelete` + `batchUpdateStatus` 签名 |
| `blog-backend/src/main/java/com/blog/service/impl/CommentServiceImpl.java` | 改 | 实现两方法 |
| `blog-backend/src/main/java/com/blog/mapper/CommentMapper.java` | 改 | 加 `updateStatusBatch` 注解方法 |
| `blog-backend/src/main/java/com/blog/controller/CommentController.java` | 改 | 加两 endpoint |
| `blog-backend/src/test/java/com/blog/service/impl/ArticleServiceImplTest.java` | 新建 | batchDelete 单测 |
| `blog-backend/src/test/java/com/blog/controller/ArticleControllerTest.java` | 新建 | batchDelete 5 场景 |
| `blog-backend/src/test/java/com/blog/service/impl/CommentServiceImplTest.java` | 新建 | batchDelete + batchUpdateStatus 单测 |
| `blog-backend/src/test/java/com/blog/controller/CommentControllerTest.java` | 新建 | 两 endpoint × 5 场景 |
| `blog-frontend/src/components/admin/DataTable.vue` | 改 | 加 `selectable` prop + defineExpose clearSelection |
| `blog-frontend/src/components/admin/BulkActionBar.vue` | 新建 | 顶部内联批量动作条 |
| `blog-frontend/src/api/article.js` | 改 | 加 `batchDeleteArticles` |
| `blog-frontend/src/api/comment.js` | 改 | 加 `batchDeleteComments` + `batchUpdateCommentStatus` |
| `blog-frontend/src/views/admin/ArticleListView.vue` | 改 | 接入 selection + BulkActionBar |
| `blog-frontend/src/views/admin/CommentManageView.vue` | 改 | 接入 selection + BulkActionBar |

---

## Task 1: 公共 DTO

**Files:**
- Create: `blog-backend/src/main/java/com/blog/dto/common/BatchIdsDTO.java`
- Create: `blog-backend/src/main/java/com/blog/dto/comment/BatchStatusDTO.java`

- [ ] **Step 1.1: 新建 `BatchIdsDTO.java`**

```java
package com.blog.dto.common;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

/**
 * 通用批量 ids 入参。
 * 最多 100 条，防止误操作和性能问题。
 */
@Data
public class BatchIdsDTO {
    @NotEmpty(message = "ids 不能为空")
    @Size(max = 100, message = "批量不能超过 100 条")
    private List<Long> ids;
}
```

- [ ] **Step 1.2: 新建 `BatchStatusDTO.java`**

```java
package com.blog.dto.comment;

import com.blog.entity.enums.CommentStatus;
import com.blog.dto.common.BatchIdsDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 评论批量改状态入参。
 * 仅支持改为 APPROVED 或 REJECTED；想改为 PENDING 不开放（业务规则）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BatchStatusDTO extends BatchIdsDTO {
    @NotNull(message = "status 不能为空")
    private CommentStatus status;
}
```

**注意：** `CommentStatus` 枚举已存在（看 `CommentController.updateStatus` 的 `@RequestParam CommentStatus status`）。如包名是 `com.blog.entity` 而非 `com.blog.entity.enums`，import 改成实际位置。**实施时先确认枚举的实际包路径再粘代码。**

- [ ] **Step 1.3: 编译通过**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend compile -DskipTests
```
预期：BUILD SUCCESS。

- [ ] **Step 1.4: 提交**

```bash
git add blog-backend/src/main/java/com/blog/dto/common/BatchIdsDTO.java \
        blog-backend/src/main/java/com/blog/dto/comment/BatchStatusDTO.java
git commit -m "feat(backend): 新增批量操作通用 DTO（BatchIdsDTO + BatchStatusDTO）"
```

---

## Task 2: ArticleService.batchDelete TDD

**Files:**
- Modify: `blog-backend/src/main/java/com/blog/service/ArticleService.java`
- Modify: `blog-backend/src/main/java/com/blog/service/impl/ArticleServiceImpl.java`
- Create: `blog-backend/src/test/java/com/blog/service/impl/ArticleServiceImplTest.java`

**Why:** 先用红色测试锁定行为契约（事务删除、article_tags 级联清理、不存在 id 静默忽略），再写实现。

- [ ] **Step 2.1: 给 `ArticleService` 加签名**

打开 `blog-backend/src/main/java/com/blog/service/ArticleService.java`，在 `void delete(Long id);` 之后插入：

```java
    /**
     * 批量删除文章（同时清理 article_tags 关联）。
     *
     * @param ids 文章 ID 列表，最多 100 条
     * @return 实际删除的主表行数（不存在的 id 不计入）
     */
    int batchDelete(java.util.List<Long> ids);
```

- [ ] **Step 2.2: `ArticleServiceImpl` 加 stub 让编译过**

打开 `blog-backend/src/main/java/com/blog/service/impl/ArticleServiceImpl.java`，在类内任意位置加：

```java
    @Override
    public int batchDelete(java.util.List<Long> ids) {
        throw new UnsupportedOperationException("not implemented yet");
    }
```

- [ ] **Step 2.3: 写失败 ServiceTest**

新建 `blog-backend/src/test/java/com/blog/service/impl/ArticleServiceImplTest.java`：

```java
package com.blog.service.impl;

import com.blog.dto.article.ArticleCreateRequest;
import com.blog.entity.Article;
import com.blog.mapper.ArticleMapper;
import com.blog.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ArticleServiceImplTest {

    @Autowired ArticleService articleService;
    @Autowired ArticleMapper articleMapper;

    @Test
    void batchDelete_withValidIds_shouldDeleteAll() {
        // 准备 3 篇文章
        Long id1 = createTestArticle("批删测试 1");
        Long id2 = createTestArticle("批删测试 2");
        Long id3 = createTestArticle("批删测试 3");

        int deleted = articleService.batchDelete(Arrays.asList(id1, id2, id3));

        assertEquals(3, deleted);
        assertNull(articleMapper.selectById(id1));
        assertNull(articleMapper.selectById(id2));
        assertNull(articleMapper.selectById(id3));
    }

    @Test
    void batchDelete_withNonexistentIds_shouldReturnZero() {
        int deleted = articleService.batchDelete(Arrays.asList(999_999_999L, 999_999_998L));
        assertEquals(0, deleted);
    }

    @Test
    void batchDelete_withMixedIds_shouldDeleteExistingOnly() {
        Long realId = createTestArticle("混合 id 测试");

        int deleted = articleService.batchDelete(Arrays.asList(realId, 999_999_999L));

        assertEquals(1, deleted);
        assertNull(articleMapper.selectById(realId));
    }

    private Long createTestArticle(String title) {
        Article a = new Article();
        a.setTitle(title);
        a.setSlug(title.replace(" ", "-").toLowerCase() + "-" + System.nanoTime());
        a.setStatus("DRAFT");
        a.setContent("test body");
        a.setAuthorId(1L); // admin 假设存在
        articleMapper.insert(a);
        return a.getId();
    }
}
```

**注意：** 测试用 `@Transactional` 让每个测试结束后自动回滚，互不干扰。`createTestArticle` 假设 author_id=1 的 admin 用户存在（看 `test/resources/schema.sql` 与 `data.sql` 现状确认；若不存在则改用 setUp 创建一个）。

- [ ] **Step 2.4: 跑测试，确认红**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend test -Dtest=ArticleServiceImplTest
```
预期：3 个测试全失败，`UnsupportedOperationException: not implemented yet`。

- [ ] **Step 2.5: 实现 batchDelete**

替换 `ArticleServiceImpl.batchDelete` 的 stub：

```java
    @Override
    @org.springframework.transaction.annotation.Transactional
    public int batchDelete(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        // 1. 先清 article_tags 关联（外键约束保护）
        articleTagMapper.deleteByArticleIds(ids);
        // 2. 删主表
        return articleMapper.deleteBatchIds(ids);
    }
```

**注意：** 需要 `articleTagMapper` 字段。如类中已 inject 则直接用；若没有则在类顶部加：

```java
    @org.springframework.beans.factory.annotation.Autowired
    private com.blog.mapper.ArticleTagMapper articleTagMapper;
```

**ArticleTagMapper.deleteByArticleIds 不一定存在**，先检查 `blog-backend/src/main/java/com/blog/mapper/ArticleTagMapper.java`：
- 如已有 `deleteByArticleIds(List<Long>)` → 直接用
- 如没有 → 在该 Mapper 加一个新方法：

```java
@org.apache.ibatis.annotations.Delete({
    "<script>",
    "DELETE FROM article_tags WHERE article_id IN ",
    "<foreach collection='articleIds' item='aid' open='(' separator=',' close=')'>#{aid}</foreach>",
    "</script>"
})
int deleteByArticleIds(@org.apache.ibatis.annotations.Param("articleIds") java.util.List<Long> articleIds);
```

- [ ] **Step 2.6: 跑测试，确认绿**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend test -Dtest=ArticleServiceImplTest
```
预期：3 个测试全通过。

- [ ] **Step 2.7: 提交**

```bash
git add blog-backend/src/main/java/com/blog/service/ArticleService.java \
        blog-backend/src/main/java/com/blog/service/impl/ArticleServiceImpl.java \
        blog-backend/src/main/java/com/blog/mapper/ArticleTagMapper.java \
        blog-backend/src/test/java/com/blog/service/impl/ArticleServiceImplTest.java
git commit -m "feat(backend): ArticleService.batchDelete + 3 场景单测（TDD）"
```

---

## Task 3: ArticleController.batchDelete + ControllerTest

**Files:**
- Modify: `blog-backend/src/main/java/com/blog/controller/ArticleController.java`
- Create: `blog-backend/src/test/java/com/blog/controller/ArticleControllerTest.java`

- [ ] **Step 3.1: 写失败 ControllerTest（5 场景）**

新建 `blog-backend/src/test/java/com/blog/controller/ArticleControllerTest.java`：

```java
package com.blog.controller;

import com.blog.dto.common.BatchIdsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ArticleControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "article:delete")
    void batchDelete_withValidIds_shouldReturn200() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        dto.setIds(Arrays.asList(999_999_999L, 999_999_998L)); // 不存在的 id，验证不报错且返回 0

        mockMvc.perform(post("/api/articles/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.deleted").value(0));
    }

    @Test
    @WithMockUser(authorities = "article:delete")
    void batchDelete_withEmptyIds_shouldReturn400() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        dto.setIds(Collections.emptyList());

        mockMvc.perform(post("/api/articles/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "article:delete")
    void batchDelete_withOver100Ids_shouldReturn400() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        List<Long> ids = LongStream.rangeClosed(1, 101).boxed().collect(Collectors.toList());
        dto.setIds(ids);

        mockMvc.perform(post("/api/articles/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "other:permission")
    void batchDelete_withoutAuthority_shouldReturn403() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        dto.setIds(Arrays.asList(1L, 2L));

        mockMvc.perform(post("/api/articles/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void batchDelete_unauthenticated_shouldReturn401() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        dto.setIds(Arrays.asList(1L));

        mockMvc.perform(post("/api/articles/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
```

- [ ] **Step 3.2: 跑测试，确认红**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend test -Dtest=ArticleControllerTest
```
预期：5 个测试中至少 1 个失败（404 not found，因为 endpoint 不存在）。

- [ ] **Step 3.3: 实现 Controller 方法**

打开 `blog-backend/src/main/java/com/blog/controller/ArticleController.java`，在 `delete(@PathVariable Long id)` 方法（约 154 行）之后插入：

```java
    /**
     * 批量删除文章。
     */
    @PostMapping("/batch-delete")
    @PreAuthorize("hasAuthority('article:delete')")
    public Result<java.util.Map<String, Integer>> batchDelete(
            @Valid @RequestBody com.blog.dto.common.BatchIdsDTO dto) {
        int deleted = articleService.batchDelete(dto.getIds());
        return Result.success(java.util.Map.of("deleted", deleted));
    }
```

**注意：**
- 类已 import 了 `Result` / `@PreAuthorize` / `@PostMapping`（看现有 create 方法）。`@Valid` 也已 import。
- 若 `Result.success(Object)` 签名不存在，看现有 `Result` 类用对应工厂方法（如 `Result.ok(data)` 或 `new Result<>(200, "success", data)`）。

- [ ] **Step 3.4: 跑测试，确认绿**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend test -Dtest=ArticleControllerTest
```
预期：5 个测试全过。

- [ ] **Step 3.5: 提交**

```bash
git add blog-backend/src/main/java/com/blog/controller/ArticleController.java \
        blog-backend/src/test/java/com/blog/controller/ArticleControllerTest.java
git commit -m "feat(backend): POST /api/articles/batch-delete + 5 场景集成测试"
```

---

## Task 4: CommentService.batchDelete TDD

**Files:**
- Modify: `blog-backend/src/main/java/com/blog/service/CommentService.java`
- Modify: `blog-backend/src/main/java/com/blog/service/impl/CommentServiceImpl.java`
- Create: `blog-backend/src/test/java/com/blog/service/impl/CommentServiceImplTest.java`

- [ ] **Step 4.1: `CommentService` 加签名**

打开 `blog-backend/src/main/java/com/blog/service/CommentService.java`，加：

```java
    /**
     * 批量删除评论。
     *
     * @return 实际删除的行数
     */
    int batchDelete(java.util.List<Long> ids);
```

- [ ] **Step 4.2: `CommentServiceImpl` 加 stub**

```java
    @Override
    public int batchDelete(java.util.List<Long> ids) {
        throw new UnsupportedOperationException("not implemented yet");
    }
```

- [ ] **Step 4.3: 写失败 ServiceTest**

新建 `blog-backend/src/test/java/com/blog/service/impl/CommentServiceImplTest.java`：

```java
package com.blog.service.impl;

import com.blog.entity.Comment;
import com.blog.mapper.CommentMapper;
import com.blog.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CommentServiceImplTest {

    @Autowired CommentService commentService;
    @Autowired CommentMapper commentMapper;

    @Test
    void batchDelete_withValidIds_shouldDeleteAll() {
        Long id1 = createTestComment("PENDING");
        Long id2 = createTestComment("PENDING");

        int deleted = commentService.batchDelete(Arrays.asList(id1, id2));

        assertEquals(2, deleted);
        assertNull(commentMapper.selectById(id1));
        assertNull(commentMapper.selectById(id2));
    }

    @Test
    void batchDelete_withNonexistentIds_shouldReturnZero() {
        int deleted = commentService.batchDelete(Arrays.asList(999_999_999L));
        assertEquals(0, deleted);
    }

    private Long createTestComment(String status) {
        Comment c = new Comment();
        c.setArticleId(1L); // 假设 article id=1 在 schema.sql/data.sql 存在
        c.setNickname("test_" + System.nanoTime());
        c.setContent("batch test");
        c.setStatus(status);
        commentMapper.insert(c);
        return c.getId();
    }
}
```

**注意：** `Comment` 实体的字段名按现状确认。如 `status` 不是 String 而是枚举/byte，调整赋值方式。

- [ ] **Step 4.4: 跑测试，确认红**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend test -Dtest=CommentServiceImplTest
```
预期：2 个测试都失败。

- [ ] **Step 4.5: 实现 batchDelete**

替换 `CommentServiceImpl.batchDelete` 的 stub：

```java
    @Override
    @org.springframework.transaction.annotation.Transactional
    public int batchDelete(java.util.List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        return commentMapper.deleteBatchIds(ids);
    }
```

**说明：** mybatis-plus `BaseMapper#deleteBatchIds` 自动接受 `Collection<? extends Serializable>`，会生成 `DELETE FROM comments WHERE id IN (...)`。评论无 cascade 子表，直接删主表。

- [ ] **Step 4.6: 跑测试，确认绿**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend test -Dtest=CommentServiceImplTest
```

- [ ] **Step 4.7: 提交**

```bash
git add blog-backend/src/main/java/com/blog/service/CommentService.java \
        blog-backend/src/main/java/com/blog/service/impl/CommentServiceImpl.java \
        blog-backend/src/test/java/com/blog/service/impl/CommentServiceImplTest.java
git commit -m "feat(backend): CommentService.batchDelete + 单测（TDD）"
```

---

## Task 5: CommentController.batchDelete + ControllerTest

**Files:**
- Modify: `blog-backend/src/main/java/com/blog/controller/CommentController.java`
- Create: `blog-backend/src/test/java/com/blog/controller/CommentControllerTest.java`

- [ ] **Step 5.1: 写失败 ControllerTest（5 场景）**

新建 `blog-backend/src/test/java/com/blog/controller/CommentControllerTest.java`：

```java
package com.blog.controller;

import com.blog.dto.common.BatchIdsDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "comment:delete")
    void batchDelete_withValidIds_shouldReturn200() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        dto.setIds(Arrays.asList(999_999_999L));

        mockMvc.perform(post("/api/comments/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.deleted").value(0));
    }

    @Test
    @WithMockUser(authorities = "comment:delete")
    void batchDelete_withEmptyIds_shouldReturn400() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        dto.setIds(Collections.emptyList());

        mockMvc.perform(post("/api/comments/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "comment:delete")
    void batchDelete_withOver100Ids_shouldReturn400() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        List<Long> ids = LongStream.rangeClosed(1, 101).boxed().collect(Collectors.toList());
        dto.setIds(ids);

        mockMvc.perform(post("/api/comments/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "other:permission")
    void batchDelete_withoutAuthority_shouldReturn403() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        dto.setIds(Arrays.asList(1L));

        mockMvc.perform(post("/api/comments/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void batchDelete_unauthenticated_shouldReturn401() throws Exception {
        BatchIdsDTO dto = new BatchIdsDTO();
        dto.setIds(Arrays.asList(1L));

        mockMvc.perform(post("/api/comments/batch-delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
```

- [ ] **Step 5.2: 跑测试，确认红**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend test -Dtest=CommentControllerTest
```

- [ ] **Step 5.3: 实现 Controller 方法**

在 `CommentController.java` 的 `delete(@PathVariable Long id)` 方法（约 99 行）之后插入：

```java
    @PostMapping("/api/comments/batch-delete")
    @PreAuthorize("hasAuthority('comment:delete')")
    public Result<java.util.Map<String, Integer>> batchDelete(
            @Valid @RequestBody com.blog.dto.common.BatchIdsDTO dto) {
        int deleted = commentService.batchDelete(dto.getIds());
        return Result.success(java.util.Map.of("deleted", deleted));
    }
```

**注意：** CommentController 无 class-level `@RequestMapping`，每方法写全路径。

- [ ] **Step 5.4: 跑测试，确认绿**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend test -Dtest=CommentControllerTest
```

- [ ] **Step 5.5: 提交**

```bash
git add blog-backend/src/main/java/com/blog/controller/CommentController.java \
        blog-backend/src/test/java/com/blog/controller/CommentControllerTest.java
git commit -m "feat(backend): POST /api/comments/batch-delete + 5 场景集成测试"
```

---

## Task 6: CommentService.batchUpdateStatus TDD

**Files:**
- Modify: `blog-backend/src/main/java/com/blog/service/CommentService.java`
- Modify: `blog-backend/src/main/java/com/blog/service/impl/CommentServiceImpl.java`
- Modify: `blog-backend/src/main/java/com/blog/mapper/CommentMapper.java`
- Modify: `blog-backend/src/test/java/com/blog/service/impl/CommentServiceImplTest.java`

- [ ] **Step 6.1: `CommentService` 加签名**

```java
    /**
     * 批量改评论状态（仅允许 APPROVED 或 REJECTED）。
     */
    int batchUpdateStatus(java.util.List<Long> ids, com.blog.entity.enums.CommentStatus status);
```

（包路径以 `CommentStatus` 实际位置为准；与 Task 1 一致。）

- [ ] **Step 6.2: `CommentServiceImpl` 加 stub**

```java
    @Override
    public int batchUpdateStatus(java.util.List<Long> ids, com.blog.entity.enums.CommentStatus status) {
        throw new UnsupportedOperationException("not implemented yet");
    }
```

- [ ] **Step 6.3: 在 `CommentMapper.java` 加批量 update 方法**

打开 `blog-backend/src/main/java/com/blog/mapper/CommentMapper.java`，在 `countByStatus` 方法之前或之后插入：

```java
    /**
     * 批量更新评论状态。
     *
     * @param ids    评论 ID 列表
     * @param status 目标状态字符串
     * @return 实际更新行数
     */
    @org.apache.ibatis.annotations.Update({
        "<script>",
        "UPDATE comments SET status = #{status} WHERE id IN ",
        "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
        "</script>"
    })
    int updateStatusBatch(@org.apache.ibatis.annotations.Param("ids") java.util.List<Long> ids,
                          @org.apache.ibatis.annotations.Param("status") String status);
```

- [ ] **Step 6.4: 在 `CommentServiceImplTest` 追加测试**

打开已有的 `CommentServiceImplTest.java`，加：

```java
    @Test
    void batchUpdateStatus_toApproved_shouldUpdateAll() {
        Long id1 = createTestComment("PENDING");
        Long id2 = createTestComment("PENDING");

        int updated = commentService.batchUpdateStatus(
            Arrays.asList(id1, id2),
            com.blog.entity.enums.CommentStatus.APPROVED
        );

        assertEquals(2, updated);
        assertEquals("APPROVED", commentMapper.selectById(id1).getStatus());
        assertEquals("APPROVED", commentMapper.selectById(id2).getStatus());
    }

    @Test
    void batchUpdateStatus_toRejected_shouldUpdateAll() {
        Long id1 = createTestComment("PENDING");

        int updated = commentService.batchUpdateStatus(
            Arrays.asList(id1),
            com.blog.entity.enums.CommentStatus.REJECTED
        );

        assertEquals(1, updated);
        assertEquals("REJECTED", commentMapper.selectById(id1).getStatus());
    }
```

（如 `Comment.status` 返回类型不是 String 而是枚举，断言改为 `assertEquals(CommentStatus.APPROVED, ...)`。）

- [ ] **Step 6.5: 跑测试，确认红**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend test -Dtest=CommentServiceImplTest
```
预期：新增 2 个测试失败。

- [ ] **Step 6.6: 实现 batchUpdateStatus**

替换 stub：

```java
    @Override
    @org.springframework.transaction.annotation.Transactional
    public int batchUpdateStatus(java.util.List<Long> ids, com.blog.entity.enums.CommentStatus status) {
        if (ids == null || ids.isEmpty()) return 0;
        if (status != com.blog.entity.enums.CommentStatus.APPROVED
            && status != com.blog.entity.enums.CommentStatus.REJECTED) {
            throw new IllegalArgumentException("status 只允许 APPROVED 或 REJECTED");
        }
        return commentMapper.updateStatusBatch(ids, status.name());
    }
```

- [ ] **Step 6.7: 跑测试，确认绿**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend test -Dtest=CommentServiceImplTest
```

- [ ] **Step 6.8: 提交**

```bash
git add blog-backend/src/main/java/com/blog/service/CommentService.java \
        blog-backend/src/main/java/com/blog/service/impl/CommentServiceImpl.java \
        blog-backend/src/main/java/com/blog/mapper/CommentMapper.java \
        blog-backend/src/test/java/com/blog/service/impl/CommentServiceImplTest.java
git commit -m "feat(backend): CommentService.batchUpdateStatus + Mapper + 2 个单测"
```

---

## Task 7: CommentController.batchStatus + ControllerTest

**Files:**
- Modify: `blog-backend/src/main/java/com/blog/controller/CommentController.java`
- Modify: `blog-backend/src/test/java/com/blog/controller/CommentControllerTest.java`

- [ ] **Step 7.1: 在 `CommentControllerTest` 追加 5 场景**

```java
    @Test
    @WithMockUser(authorities = "comment:approve")
    void batchStatus_toApproved_shouldReturn200() throws Exception {
        com.blog.dto.comment.BatchStatusDTO dto = new com.blog.dto.comment.BatchStatusDTO();
        dto.setIds(Arrays.asList(999_999_999L)); // 不存在的 id
        dto.setStatus(com.blog.entity.enums.CommentStatus.APPROVED);

        mockMvc.perform(post("/api/comments/batch-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updated").value(0));
    }

    @Test
    @WithMockUser(authorities = "comment:approve")
    void batchStatus_withEmptyIds_shouldReturn400() throws Exception {
        com.blog.dto.comment.BatchStatusDTO dto = new com.blog.dto.comment.BatchStatusDTO();
        dto.setIds(Collections.emptyList());
        dto.setStatus(com.blog.entity.enums.CommentStatus.APPROVED);

        mockMvc.perform(post("/api/comments/batch-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "comment:approve")
    void batchStatus_withNullStatus_shouldReturn400() throws Exception {
        com.blog.dto.comment.BatchStatusDTO dto = new com.blog.dto.comment.BatchStatusDTO();
        dto.setIds(Arrays.asList(1L));
        dto.setStatus(null);

        mockMvc.perform(post("/api/comments/batch-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "other:permission")
    void batchStatus_withoutAuthority_shouldReturn403() throws Exception {
        com.blog.dto.comment.BatchStatusDTO dto = new com.blog.dto.comment.BatchStatusDTO();
        dto.setIds(Arrays.asList(1L));
        dto.setStatus(com.blog.entity.enums.CommentStatus.APPROVED);

        mockMvc.perform(post("/api/comments/batch-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void batchStatus_unauthenticated_shouldReturn401() throws Exception {
        com.blog.dto.comment.BatchStatusDTO dto = new com.blog.dto.comment.BatchStatusDTO();
        dto.setIds(Arrays.asList(1L));
        dto.setStatus(com.blog.entity.enums.CommentStatus.APPROVED);

        mockMvc.perform(post("/api/comments/batch-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
```

- [ ] **Step 7.2: 跑测试，确认红**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend test -Dtest=CommentControllerTest
```

- [ ] **Step 7.3: 实现 Controller 方法**

在 `CommentController` 的 `batchDelete` 方法之后插入：

```java
    @PostMapping("/api/comments/batch-status")
    @PreAuthorize("hasAuthority('comment:approve')")
    public Result<java.util.Map<String, Integer>> batchStatus(
            @Valid @RequestBody com.blog.dto.comment.BatchStatusDTO dto) {
        int updated = commentService.batchUpdateStatus(dto.getIds(), dto.getStatus());
        return Result.success(java.util.Map.of("updated", updated));
    }
```

- [ ] **Step 7.4: 跑测试，确认绿**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend test -Dtest=CommentControllerTest
```

- [ ] **Step 7.5: 提交**

```bash
git add blog-backend/src/main/java/com/blog/controller/CommentController.java \
        blog-backend/src/test/java/com/blog/controller/CommentControllerTest.java
git commit -m "feat(backend): POST /api/comments/batch-status + 5 场景集成测试"
```

---

## Task 8: DataTable.vue 加 selectable + defineExpose

**Files:**
- Modify: `blog-frontend/src/components/admin/DataTable.vue`

- [ ] **Step 8.1: 修改 `<el-table>` 部分**

打开 `blog-frontend/src/components/admin/DataTable.vue`。

把现有 `<el-table ...>...<slot/></el-table>` 改为：

```vue
<el-table
  ref="tableRef"
  :data="data"
  v-loading="loading"
  :row-key="rowKey"
  class="data-table"
  :empty-text="emptyText"
  @selection-change="onSelectionChange"
>
  <el-table-column v-if="selectable" type="selection" width="48" />
  <slot />
</el-table>
```

- [ ] **Step 8.2: 修改 `<script setup>` 部分**

把现有 script 块替换为：

```vue
<script setup>
import { ref } from 'vue'

const props = defineProps({
  data:       { type: Array,   default: () => [] },
  loading:    { type: Boolean, default: false },
  total:      { type: Number,  default: 0 },
  page:       { type: Number,  default: 1 },
  pageSize:   { type: Number,  default: 10 },
  rowKey:     { type: String,  default: 'id' },
  emptyText:  { type: String,  default: '暂无数据' },
  selectable: { type: Boolean, default: false },
})

const emit = defineEmits(['update:page', 'page-change', 'selection-change'])

const tableRef = ref(null)

function handlePageChange(p) {
  emit('update:page', p)
  emit('page-change', p)
}

function onSelectionChange(rows) {
  emit('selection-change', rows)
}

defineExpose({
  clearSelection: () => tableRef.value?.clearSelection(),
})
</script>
```

- [ ] **Step 8.3: 验证 vite build**

```bash
cd C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog/blog-frontend && npx vite build 2>&1 | tail -3
```
预期：BUILD SUCCESS。

- [ ] **Step 8.4: 提交**

```bash
git add blog-frontend/src/components/admin/DataTable.vue
git commit -m "feat(frontend): DataTable 加 selectable prop + 暴露 clearSelection"
```

---

## Task 9: 新建 BulkActionBar.vue

**Files:**
- Create: `blog-frontend/src/components/admin/BulkActionBar.vue`

- [ ] **Step 9.1: 新建组件**

新建 `blog-frontend/src/components/admin/BulkActionBar.vue`：

```vue
<template>
  <Transition name="bba-slide">
    <div v-if="count > 0" class="bulk-action-bar">
      <div class="bba-left">
        <span class="bba-check-icon">✓</span>
        <span class="bba-count">已选 {{ count }} 项</span>
      </div>
      <div class="bba-right">
        <slot />
        <button class="bba-cancel" @click="$emit('cancel')">取消</button>
      </div>
    </div>
  </Transition>
</template>

<script setup>
defineProps({
  count: { type: Number, required: true, default: 0 },
})

defineEmits(['cancel'])
</script>

<style scoped>
.bulk-action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 16px;
  background: rgba(232, 168, 56, 0.08);
  border: 1px solid rgba(232, 168, 56, 0.25);
  border-radius: var(--radius-md);
  transition: background var(--transition-base), border-color var(--transition-base);
}

.bba-left {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #E8A838;
  font-weight: 600;
}

.bba-check-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(232, 168, 56, 0.2);
  font-size: 11px;
}

.bba-right {
  display: flex;
  gap: 8px;
  align-items: center;
}

.bba-cancel {
  display: inline-flex;
  align-items: center;
  height: 30px;
  padding: 0 14px;
  border-radius: 6px;
  background: var(--color-card-surface);
  border: 1px solid var(--color-card-border);
  color: var(--color-text-secondary);
  cursor: pointer;
  font-size: 12px;
  font-weight: 500;
  transition: opacity var(--transition-fast), border-color var(--transition-fast);
}
.bba-cancel:hover { border-color: var(--color-text-tertiary); }

/* Slide animation */
.bba-slide-enter-active, .bba-slide-leave-active {
  transition: opacity 200ms ease, transform 200ms ease, max-height 200ms ease;
  overflow: hidden;
}
.bba-slide-enter-from, .bba-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
  border-width: 0;
}
.bba-slide-enter-to, .bba-slide-leave-from {
  opacity: 1;
  transform: translateY(0);
  max-height: 60px;
}

/* Action button styles to use in slot */
:slotted(.bba-action) {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  height: 30px;
  padding: 0 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  border: 1px solid;
  transition: opacity var(--transition-fast);
}
:slotted(.bba-action:hover) { opacity: 0.8; }
:slotted(.bba-action--del) {
  background: rgba(239, 68, 68, 0.12);
  border-color: rgba(239, 68, 68, 0.35);
  color: #EF4444;
}
:slotted(.bba-action--approve) {
  background: rgba(34, 197, 94, 0.12);
  border-color: rgba(34, 197, 94, 0.35);
  color: #22C55E;
}
:slotted(.bba-action--reject) {
  background: rgba(107, 114, 128, 0.1);
  border-color: rgba(107, 114, 128, 0.3);
  color: var(--color-text-secondary);
}
</style>
```

- [ ] **Step 9.2: 验证 vite build**

```bash
cd C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog/blog-frontend && npx vite build 2>&1 | tail -3
```

- [ ] **Step 9.3: 提交**

```bash
git add blog-frontend/src/components/admin/BulkActionBar.vue
git commit -m "feat(frontend): 新增 BulkActionBar 通用批量动作条"
```

---

## Task 10: ArticleListView 接入批量删除

**Files:**
- Modify: `blog-frontend/src/api/article.js`
- Modify: `blog-frontend/src/views/admin/ArticleListView.vue`

- [ ] **Step 10.1: 在 `api/article.js` 末尾追加函数**

打开 `blog-frontend/src/api/article.js`，在最后一行 `export const getArticleById = ...` 之后追加：

```js
export const batchDeleteArticles = (data) => request.post('/articles/batch-delete', data)
```

- [ ] **Step 10.2: `ArticleListView.vue` script 加 import**

打开 `blog-frontend/src/views/admin/ArticleListView.vue`，在 `<script setup>` 块的 import 区追加：

```js
import BulkActionBar from '@/components/admin/BulkActionBar.vue'
```

并把：
```js
import { getAdminArticles, deleteArticle, togglePublish } from '@/api/article'
```
改为：
```js
import { getAdminArticles, deleteArticle, togglePublish, batchDeleteArticles } from '@/api/article'
```

- [ ] **Step 10.3: script 加 state + handler**

在 `<script setup>` 内（建议放在 `const togglingId = ref(null)` 之后）追加：

```js
const dataTableRef = ref(null)
const selectedRows = ref([])

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function clearSelection() {
  dataTableRef.value?.clearSelection()
}

async function handleBatchDelete() {
  const count = selectedRows.value.length
  if (count === 0) return
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${count} 篇文章？此操作不可撤销。`,
      '批量删除确认',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
      }
    )
  } catch { return }
  try {
    const ids = selectedRows.value.map(r => r.id)
    await batchDeleteArticles({ ids })
    ElMessage.success(`已删除 ${count} 篇文章`)
    selectedRows.value = []
    loadArticles(page.value)
  } catch {
    /* request.js 全局拦截弹错 */
  }
}
```

- [ ] **Step 10.4: 修改 template**

在 `<DataTable ...>` 标签前插入 `<BulkActionBar>`，并改 `<DataTable>` 标签加 ref + selectable + selection-change：

把：
```vue
    <DataTable
      :data="articles"
      :loading="loading"
      :total="total"
      :page="page"
      :page-size="pageSize"
      @page-change="loadArticles"
    >
```
改为：
```vue
    <BulkActionBar :count="selectedRows.length" @cancel="clearSelection">
      <button class="bba-action bba-action--del" @click="handleBatchDelete">
        <el-icon><Delete /></el-icon> 批量删除
      </button>
    </BulkActionBar>

    <DataTable
      ref="dataTableRef"
      selectable
      :data="articles"
      :loading="loading"
      :total="total"
      :page="page"
      :page-size="pageSize"
      @page-change="loadArticles"
      @selection-change="handleSelectionChange"
    >
```

- [ ] **Step 10.5: 验证 vite build**

```bash
cd C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog/blog-frontend && npx vite build 2>&1 | tail -3
```

- [ ] **Step 10.6: 提交**

```bash
git add blog-frontend/src/api/article.js \
        blog-frontend/src/views/admin/ArticleListView.vue
git commit -m "feat(frontend): ArticleListView 接入批量删除"
```

---

## Task 11: CommentManageView 接入 3 批量动作

**Files:**
- Modify: `blog-frontend/src/api/comment.js`
- Modify: `blog-frontend/src/views/admin/CommentManageView.vue`

- [ ] **Step 11.1: 在 `api/comment.js` 末尾追加函数**

```js
export const batchDeleteComments = (data) => request.post('/comments/batch-delete', data)
export const batchUpdateCommentStatus = (data) => request.post('/comments/batch-status', data)
```

- [ ] **Step 11.2: `CommentManageView.vue` script 加 import**

`<script setup>` 块的 import 区：
- 加：`import BulkActionBar from '@/components/admin/BulkActionBar.vue'`
- 加：`import { ElMessage, ElMessageBox } from 'element-plus'`（原本只有 ElMessage，需补 ElMessageBox）
- 修改：`import { getAdminComments, updateCommentStatus, deleteComment, batchDeleteComments, batchUpdateCommentStatus } from '@/api/comment'`

- [ ] **Step 11.3: script 加 state + 3 个 handler**

在 `<script setup>` 内（建议放在 `const detailRow = ref(null)` 之后）追加：

```js
const dataTableRef = ref(null)
const selectedRows = ref([])

function handleSelectionChange(rows) {
  selectedRows.value = rows
}

function clearSelection() {
  dataTableRef.value?.clearSelection()
}

async function handleBatchApprove() {
  const ids = selectedRows.value.map(r => r.id)
  if (!ids.length) return
  try {
    await batchUpdateCommentStatus({ ids, status: 'APPROVED' })
    ElMessage.success(`已通过 ${ids.length} 条评论`)
    selectedRows.value = []
    load(page.value)
    loadPendingCount()
  } catch { /* 全局拦截 */ }
}

async function handleBatchReject() {
  const count = selectedRows.value.length
  if (count === 0) return
  try {
    await ElMessageBox.confirm(
      `确认将选中的 ${count} 条评论标记为已拒绝？`,
      '批量拒绝',
      { type: 'warning', confirmButtonText: '确认拒绝', cancelButtonText: '取消' }
    )
  } catch { return }
  try {
    const ids = selectedRows.value.map(r => r.id)
    await batchUpdateCommentStatus({ ids, status: 'REJECTED' })
    ElMessage.success(`已拒绝 ${count} 条评论`)
    selectedRows.value = []
    load(page.value)
    loadPendingCount()
  } catch { /* 全局拦截 */ }
}

async function handleBatchDelete() {
  const count = selectedRows.value.length
  if (count === 0) return
  try {
    await ElMessageBox.confirm(
      `确认永久删除选中的 ${count} 条评论？此操作不可撤销。`,
      '批量删除',
      {
        type: 'warning',
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        confirmButtonClass: 'el-button--danger',
      }
    )
  } catch { return }
  try {
    const ids = selectedRows.value.map(r => r.id)
    await batchDeleteComments({ ids })
    ElMessage.success(`已删除 ${count} 条评论`)
    selectedRows.value = []
    load(page.value)
    loadPendingCount()
  } catch { /* 全局拦截 */ }
}
```

- [ ] **Step 11.4: 修改 template**

把 `<DataTable ...>` 改为：

```vue
    <BulkActionBar :count="selectedRows.length" @cancel="clearSelection">
      <button class="bba-action bba-action--approve" @click="handleBatchApprove">✓ 批量通过</button>
      <button class="bba-action bba-action--reject" @click="handleBatchReject">✕ 批量拒绝</button>
      <button class="bba-action bba-action--del" @click="handleBatchDelete">🗑 批量删除</button>
    </BulkActionBar>

    <DataTable
      ref="dataTableRef"
      selectable
      :data="comments"
      :loading="loading"
      :total="total"
      :page="page"
      :page-size="pageSize"
      @page-change="load"
      @selection-change="handleSelectionChange"
    >
```

- [ ] **Step 11.5: 验证 vite build**

```bash
cd C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog/blog-frontend && npx vite build 2>&1 | tail -3
```

- [ ] **Step 11.6: 提交**

```bash
git add blog-frontend/src/api/comment.js \
        blog-frontend/src/views/admin/CommentManageView.vue
git commit -m "feat(frontend): CommentManageView 接入批量通过/拒绝/删除"
```

---

## Task 12: 跨页面手测 + 完工汇报

**Files:** 无新文件（如手测发现问题再 patch）

- [ ] **Step 12.1: backend 全量 test 兜底**

```bash
JAVA_HOME="/d/environment/jdk-21.0.11" \
PATH="/d/environment/jdk-21.0.11/bin:/d/environment/apache-maven-3.6.3/bin:$PATH" \
mvn -q -pl blog-backend test
```
预期：BUILD SUCCESS，所有 test（含 C 批新增 ~17 个）全绿。

- [ ] **Step 12.2: frontend 全量 build 兜底**

```bash
cd C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog/blog-frontend && npx vite build 2>&1 | tail -5
```
预期：BUILD SUCCESS。

- [ ] **Step 12.3: 启 dev server 手测**

```powershell
$env:UPLOAD_DIR='D:\blog-data\uploads'
cd C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog/blog-frontend
npm run dev
```

（后端需用户单独在 IntelliJ 启动；UPLOAD_DIR 详见 memory `upload_dir.md`。）

打开 `/admin/articles` 与 `/admin/comments`，验：

| # | 操作 | 预期 |
|---|------|------|
| 1 | 文章页：勾 3 条 | BulkActionBar 顶部滑出，显示「✓ 已选 3 项 + 批量删除 + 取消」 |
| 2 | 点"批量删除" | ElMessageBox 确认 → 取消无变化 / 确认成功删除 3 条 + 列表刷新 + bar 消失 |
| 3 | 点"取消" | bar 消失 + checkbox 清空 |
| 4 | 翻页时 selection 清空 | bar 自动消失（el-table 默认行为） |
| 5 | 评论页：勾 N 条 | bar 显示 3 个动作按钮 |
| 6 | 点"批量通过" | 无 confirm（正向动作），状态变 APPROVED + 刷新 |
| 7 | 点"批量拒绝" | ElMessageBox 确认 → 状态变 REJECTED |
| 8 | 点"批量删除" | ElMessageBox 确认 → 永久删除 |
| 9 | 暗/亮主题切换 | bar 视觉两种主题对比度都正常 |
| 10 | 不触发 batch 时其他原有功能 | A/B 批所有功能（单行删除/状态切换/popconfirm/详情 dialog）不受影响 |

如手测发现走样：patch 后单独 commit `fix(frontend): C 批 手测发现的视觉问题修补`。

- [ ] **Step 12.4: 检查 git 树并汇报**

```bash
cd C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog && git status && echo "---" && git log --oneline master..HEAD
```
预期：
- 无未提交改动
- 列出 C 批所有 commits（约 11-12 个）

向用户报告：
- Backend：3 个新 endpoint + ≥15 个测试用例全绿
- Frontend：新增 BulkActionBar、ArticleListView 与 CommentManageView 已接入
- 10 项手测通过情况
- 已知不在范围（spec §10）

---

## 实施完成后的下一步

C 批完成后：
- 走 `superpowers:finishing-a-development-branch` 决定合并方式
- 或继续 spec §10 未做项：Dashboard 改造 / Role-Menu 视图改造 / 移动端响应式
