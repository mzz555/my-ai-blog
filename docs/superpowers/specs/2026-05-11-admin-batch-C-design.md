# 后台 C 批 · 列表批量操作 设计文档

**日期：** 2026-05-11
**前置批次：** A 批（admin 快赢，已 merge）、B 批（整体视觉 + 列表抽象，已 merge）
**Spec 来源：** B 批 spec §10「不在范围」明标的下一批

---

## 1. 目标与背景

B 批已经把 5 个 list view 接入 `AdminPageCard` + `DataTable` 通用骨架，删除流程统一为 `ElMessageBox.confirm`。但**所有动作仍是单行操作**——管理员处理一批待审评论或一批要清理的文章时只能逐条点删除/通过，效率与现代管理后台不符。

C 批补齐**批量操作**能力，使后台具备"多选一批 → 一键执行"的标准管理体验。

## 2. 范围

### 2.1 In Scope

| 列表 | 批量动作 |
|------|---------|
| 文章管理 `/admin/articles` | 批量删除 |
| 评论审核 `/admin/comments` | 批量通过、批量拒绝、批量删除 |

### 2.2 Out of Scope（推到后续批次）

- ❌ 用户列表批量启用/禁用（用户管理误操作风险高，单行更安全）
- ❌ 批量修改文章状态（发布/撤稿）—— 涉及 `article:publish` 权限分支，本批先不引入
- ❌ 跨页选择保留 / 全选所有符合 filter 的项—— el-table 默认行为已够用
- ❌ Category / Tag 列表（B 批已确定它们是卡片网格，不适合多选）
- ❌ 操作日志 / 审计（无现有基础设施，独立批次）

## 3. Backend 接口设计

新增 3 个 endpoint，遵守现有 Controller / Service / Mapper 三层 + Spring Security 注解风格。

### 3.1 文章批量删除

```
POST /api/articles/batch-delete
Content-Type: application/json
Authorization: Bearer <token>
权限：article:delete

Body:
{
  "ids": [12, 15, 23]
}

Response 200:
{
  "code": 200,
  "msg": "success",
  "data": { "deleted": 3 }
}

Response 4xx:
- 400 ids 为空 / 超过 100 条
- 403 无 article:delete 权限
- 401 未登录
```

**Service 实现：**
```java
@Transactional
public int batchDelete(List<Long> ids) {
    if (ids == null || ids.isEmpty()) throw new IllegalArgumentException("ids 不能为空");
    if (ids.size() > 100) throw new IllegalArgumentException("批量不能超过 100 条");
    // 1. 删关联表（article_tag）：DELETE FROM article_tag WHERE article_id IN (...)
    // 2. 删主表：DELETE FROM articles WHERE id IN (...)
    return articleMapper.deleteBatch(ids);
}
```

事务原子：要么全删，要么不动数据库。

### 3.2 评论批量删除

```
POST /api/comments/batch-delete
权限：comment:delete（沿用现有单行 DELETE /api/comments/{id} 的权限注解）
Body: { "ids": [Long...] }
Response: { "deleted": N }
```

同样事务性 `DELETE WHERE id IN (...)`。注意：评论无 cascade 子表，可直接删主表。

### 3.3 评论批量改状态（通过 / 拒绝）

```
POST /api/comments/batch-status
权限：comment:approve（沿用现有单行 PUT /api/comments/{id}/status 的权限注解）
Body:
{
  "ids": [Long...],
  "status": "APPROVED" | "REJECTED"
}

Response:
{
  "code": 200,
  "data": { "updated": N }
}
```

后端校验 `status` 只允许 `APPROVED` / `REJECTED`，禁止用此接口把状态改成 `PENDING`（应通过另外路径，本批不开放）。

### 3.4 DTO 命名

复用现有 `dto.common`（如有），否则新建：
```java
public class BatchIdsDTO {
    @NotEmpty private List<Long> ids;
}

public class BatchStatusDTO extends BatchIdsDTO {
    @NotNull private CommentStatus status;
}
```

## 4. Frontend 架构

### 4.1 改 `DataTable.vue`

新增 prop：
```js
selectable: { type: Boolean, default: false }
```

模板内条件渲染 selection column：
```vue
<el-table ref="tableRef" ... @selection-change="onSelectionChange">
  <el-table-column v-if="selectable" type="selection" width="48" />
  <slot />
</el-table>
```

新增 emit：`update:selection` 和 `selection-change`，从 `<el-table>` 透传。

通过 `defineExpose` 暴露 `clearSelection()` 方法，内部调用 `tableRef.value.clearSelection()`，让 view 可以通过 DataTable 的 ref 调用：

```js
const tableRef = ref(null)
defineExpose({
  clearSelection: () => tableRef.value?.clearSelection(),
})
```

### 4.2 新建 `BulkActionBar.vue`

```vue
<BulkActionBar
  :count="selectedRows.length"
  @cancel="clearSelection"
>
  <button class="bba-action bba-action--del" @click="handleBatchDelete">
    <el-icon><Delete /></el-icon> 批量删除
  </button>
</BulkActionBar>
```

视觉：选中后在 AdminPageCard 的 filter 行下方滑出一条横条，左侧 "✓ 已选 N 项" 文案 + 右侧动作 slot + "取消"按钮。

CSS：使用 B 批的 `--color-card-border` / `--color-card-surface` 系列 token；琥珀色高亮背景 `rgba(232,168,56,.08)` 与 border `rgba(232,168,56,.2)`。

slot：
- 默认 slot：动作按钮（view 自定义）

props：
- `count: number` — 已选条数
- 隐藏自身的条件：`count === 0`（v-if 在内部，不需要 view 处理）

events：
- `@cancel` — 用户点取消时触发，view 应清空 selection（调用 el-table.clearSelection()）

### 4.3 View 改造

#### ArticleListView

```vue
<DataTable selectable @selection-change="handleSelectionChange" ...>
```

添加 state：
```js
const selectedRows = ref([])
const dataTableRef = ref(null)
function handleSelectionChange(rows) { selectedRows.value = rows }
function clearSelection() { dataTableRef.value?.clearSelection() }

async function handleBatchDelete() {
  const count = selectedRows.value.length
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${count} 篇文章？此操作不可撤销。`,
      '批量删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消', confirmButtonClass: 'el-button--danger' }
    )
  } catch { return }
  const ids = selectedRows.value.map(r => r.id)
  await batchDeleteArticles({ ids })
  ElMessage.success(`已删除 ${count} 篇文章`)
  selectedRows.value = []
  loadArticles(page.value)
}
```

BulkActionBar 在模板里 mount 在 DataTable 之前：
```vue
<BulkActionBar :count="selectedRows.length" @cancel="clearSelection">
  <button class="bba-action bba-action--del" @click="handleBatchDelete">
    <el-icon><Delete /></el-icon> 批量删除
  </button>
</BulkActionBar>
<DataTable ref="dataTableRef" selectable @selection-change="handleSelectionChange" ... />
```

#### CommentManageView

同思路，BulkActionBar 内 3 个 action 按钮：通过 / 拒绝 / 删除。

```js
async function handleBatchApprove() {
  // 无需 ElMessageBox（通过是正向动作，与单行通过一致）
  await batchUpdateCommentStatus({ ids, status: 'APPROVED' })
  ...
}
async function handleBatchReject() {
  await ElMessageBox.confirm('确认拒绝选中评论？', '批量拒绝', { type: 'warning' })
  await batchUpdateCommentStatus({ ids, status: 'REJECTED' })
}
async function handleBatchDelete() {
  await ElMessageBox.confirm(`确认永久删除选中的 ${count} 条评论？`, '批量删除', { type: 'warning' })
  await batchDeleteComments({ ids })
}
```

### 4.4 API 层（`src/api/`）

新增 3 个函数：

```js
// api/article.js
export const batchDeleteArticles = (data) =>
  request.post('/articles/batch-delete', data)

// api/comment.js
export const batchDeleteComments = (data) =>
  request.post('/comments/batch-delete', data)

export const batchUpdateCommentStatus = (data) =>
  request.post('/comments/batch-status', data)
```

## 5. UX 流程

```
[用户在 list view] → 勾选行 checkbox
  → DataTable emit selection-change
  → view 更新 selectedRows
  → BulkActionBar v-if (count > 0) → 滑出
[用户点动作按钮]
  → ElMessageBox.confirm（删除/拒绝必弹；通过可省）
    取消 → return
    确认 → call batch API
  → 成功:
    - ElMessage.success
    - selectedRows = []
    - BulkActionBar 自动消失（count = 0）
    - reload 当前页
  → 失败:
    - request.js 全局拦截弹错（已有）
    - selection 不清空（用户可重试）
[用户点"取消"]
  → @cancel → clearSelection → BulkActionBar 消失
```

## 6. 数据流图

```
   ┌────────────────┐    selection-change    ┌──────────────┐
   │  el-table      │ ─────────────────────> │   View       │
   │  (内含 checkbox)│                        │ selectedRows │
   └────────────────┘                        └──────┬───────┘
          ▲                                         │
          │ clearSelection()                        │ :count
          │                                         ▼
   ┌──────┴─────────┐                        ┌─────────────────┐
   │  DataTable.vue │                        │ BulkActionBar   │
   │ (selectable    │ <─── @cancel ────────  │ (v-if count>0)  │
   │  prop)         │                        │   slot: actions │
   └────────────────┘                        └──────┬──────────┘
                                                    │
                                                    │ click action
                                                    ▼
                                          ┌────────────────────┐
                                          │ ElMessageBox.confirm│
                                          └─────────┬──────────┘
                                                    │ ok
                                                    ▼
                                          ┌─────────────────────┐
                                          │ batch* API (axios)  │
                                          │  request.js 拦截    │
                                          └─────────┬──────────┘
                                                    │ 2xx
                                                    ▼
                                          ┌─────────────────────┐
                                          │ ElMessage.success   │
                                          │ selectedRows = []   │
                                          │ load(page)          │
                                          └─────────────────────┘
```

## 7. 错误处理

| 场景 | 处理 |
|------|------|
| 全选 0 条点动作 | BulkActionBar 不出现，无入口（前端不需要额外校验） |
| ids 数组为空到后端 | 后端 400，request.js 弹错；前端入口已防住，几乎不会到这里 |
| 批量超 100 条 | 后端 400 + 中文 msg；前端不预防（暂无 page-size > 100 入口） |
| 网络失败 | request.js 全局 toast；selection 保留，用户可重试 |
| 部分 id 不存在 | SQL `WHERE id IN (...)` 自然忽略不存在的 id，返回实际 deleted 数。前端 toast 用后端返回的 count |
| 权限不足 | 后端 403，request.js 弹错 |

**不做的事**：前端不维护"部分失败重试列表"，事务原子保证要么全成功要么全失败。

## 8. 测试策略

### 8.1 Backend（TDD 红→绿）

每个新 endpoint 至少 3 个 controller integration test 场景（沿用 `UserControllerTest` 模板）：

**ArticleController.batchDelete 测试：**
1. ✅ 正常路径：3 条有效 id → 200 + `{deleted: 3}`，数据库验证 3 条已删
2. ❌ 未登录：401
3. ❌ 无 `article:delete` 权限：403
4. ❌ 空 ids：400
5. ❌ ids 超 100：400

**CommentController.batchDelete / batchStatus 测试：** 类似 3-5 场景。

**Service 单测：**
- 事务回滚（中间抛异常时数据不动）
- 不存在的 id 被静默忽略

### 8.2 Frontend

- `vite build` 兜底（与 B 批一致）
- 手测验收：
  1. ArticleListView 选 3 条 → 批量删除 → ElMessageBox 确认 → 列表刷新少 3 条
  2. CommentManageView 选 N 条 → 批量通过 → 列表中那 N 条状态变 APPROVED
  3. CommentManageView 跨页：第 1 页选 2 条 → 翻第 2 页 → 选 3 条 → 第 1 页那 2 条被清空（仅 3 条 selected）
  4. Toolbar 出现/消失动画顺滑
  5. 暗/亮主题下 toolbar 视觉对比度都正常
  6. 点 toolbar "取消" → toolbar 消失 + checkbox 清空

## 9. 文件清单

### Backend（新增 / 修改）
| 文件 | 操作 | 说明 |
|------|------|------|
| `dto/common/BatchIdsDTO.java` | 新建 | 通用批量 id 入参 DTO |
| `dto/comment/BatchStatusDTO.java` | 新建 | 含 status 字段 |
| `controller/ArticleController.java` | 改 | 加 `batchDelete` 方法 |
| `controller/CommentController.java` | 改 | 加 `batchDelete` + `batchStatus` 方法 |
| `service/ArticleService.java` + Impl | 改 | 加 `batchDelete(List<Long>)` 签名 + 实现 |
| `service/CommentService.java` + Impl | 改 | 加 `batchDelete` + `batchUpdateStatus` 签名 + 实现 |
| `mapper/ArticleMapper.xml` | 改 | 加 `deleteBatch` SQL（`<foreach>`） |
| `mapper/CommentMapper.xml` | 改 | 加 `deleteBatch` + `updateStatusBatch` SQL |
| `test/controller/ArticleControllerTest.java` | 新建 | 批量删除 5 场景测试（首次为该 controller 写测试） |
| `test/controller/CommentControllerTest.java` | 新建 | 批量两 endpoint × 5 场景测试（首次） |
| `test/service/impl/ArticleServiceImplTest.java` | 新建 | 批量删除单测（事务） |
| `test/service/impl/CommentServiceImplTest.java` | 新建 | 同上 |

### Frontend（新增 / 修改）
| 文件 | 操作 | 说明 |
|------|------|------|
| `components/admin/DataTable.vue` | 改 | 加 `selectable` prop + selection 透传 |
| `components/admin/BulkActionBar.vue` | 新建 | 顶部内联 toolbar |
| `views/admin/ArticleListView.vue` | 改 | 启用 selection + BulkActionBar + handleBatchDelete |
| `views/admin/CommentManageView.vue` | 改 | 启用 selection + BulkActionBar + 3 个 batch handler |
| `api/article.js` | 改 | 加 `batchDeleteArticles` |
| `api/comment.js` | 改 | 加 `batchDeleteComments` + `batchUpdateCommentStatus` |

## 10. 不在范围（明示拒绝项）

- ❌ 用户列表批量操作（删除用户风险大，状态切换价值小）
- ❌ 文章批量状态变更（发布/撤稿，权限分支复杂）
- ❌ 跨页选择保留（页面 size=10 时 valued less）
- ❌ "全选所有符合 filter 的"按钮（需要后端按 filter 删的全新接口模式）
- ❌ 撤销 / undo（架构复杂）
- ❌ 批量操作的操作日志 / 审计
- ❌ 进度条 / 大批量分段提交（事务保证下一次 commit 内完成）

## 11. 验收清单

实施完成的判定标准：

- [ ] Backend：3 个新 endpoint 全部 `@PreAuthorize` 注解 + 红→绿 TDD 流程 commit
- [ ] Backend：mvn test 全绿（含新增 ≥15 个 test 用例）
- [ ] Frontend：DataTable selectable 模式可用 + 5 个旧 view 不受影响
- [ ] Frontend：vite build 无错
- [ ] 手测 6 项全过（见 §8.2）
- [ ] 提交历史清晰：每个 task 一个 commit，commit message 中文
- [ ] 不变更 A 批 / B 批已交付组件的对外 API
