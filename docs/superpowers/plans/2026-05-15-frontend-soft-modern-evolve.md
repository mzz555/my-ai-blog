# 前台 Soft Modern 演化升级 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 在保留现有 Soft Modern 暖米色 + 琥珀金底子的前提下，对前台博客做视觉演化升级——引入 Fraunces 可变 serif 字体作为标题字、JetBrains Mono 用于 meta 信息、加微妙背景质感、改造 Hero / ArticleCard / Section divider 三处关键视觉点，使博客从"通用 sans 模板感"升级到"有个性的技术杂志感"。

**Architecture:** Token 先行（在 `tokens.css` 加 `--font-display` 字体 token + 背景 glow token），然后**全局组件**统一引用新 token（标题用 display、meta 用 mono）；接着对 3 个**单点组件**做差异化改造（Hero 巨大标题 + staggered 入场、ArticleCard hover read-more、Section divider 居中虚线 label）。所有改动**不动颜色系统、不动 admin、不动 dark theme 配色**，只在 light/dark 双主题下增强字体与微动。

**Tech Stack:** Vue 3.4 / Element Plus 2.6 / 现有 tokens.css 双主题 / Vite 5 / Google Fonts CDN（Fraunces, Noto Serif SC — 用 `<link>` 而非 `@import`，让浏览器并行加载）

**Spec 来源：** 用户对话决议（保持现有 Soft Modern 风格，走"演化"而非"重塑"）；与 `docs/design-samples/A-soft-modern.html` 样张为视觉参考。

**前置：** 当前分支 master, 从 master 拉新分支 `feat/frontend-soft-modern-evolve`。

---

## 分支策略

```bash
cd C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog
git checkout master
git checkout -b feat/frontend-soft-modern-evolve
```

每个 Task 一个 commit。最后 `--no-ff` merge 回 master。

---

## 文件结构总览

| 文件 | 操作 | 责任 |
|------|------|------|
| `blog-frontend/index.html` | 改 | 在 `<head>` 加 Google Fonts preconnect + Fraunces / Noto Serif SC / 升级 JetBrains Mono 的 link |
| `blog-frontend/src/styles/tokens.css` | 改 | 新增 `--font-display` token（light + dark）；新增背景 glow token |
| `blog-frontend/src/layouts/FrontLayout.vue` | 改 | `.brand` / `.footer-logo` 改用 `--font-display`；body 背景层叠 glow |
| `blog-frontend/src/views/front/HomeView.vue` | 改 | `.hero-title` 字体 + 字号 + 斜体强调 + staggered 入场；`.section-title` / `.newsletter-title` 改 display；`.section-divider` 重设计；meta 项改 mono |
| `blog-frontend/src/components/front/ArticleCard.vue` | 改 | `.title` 改 display；meta 改 mono；hover 增"阅读 →"渐显 |
| `blog-frontend/src/components/front/ArticleCardGrid.vue` | 改 | `.card-title` 改 display；meta 改 mono；hover 增 read-more |

涉及 6 个文件。共 7 个 Task + 1 个收尾验证 Task。

---

## Task 1: 引入 Fraunces + Noto Serif SC 字体（HTML preconnect）

**Goal:** 让浏览器尽早开始下载 Fraunces 与 Noto Serif SC 字体文件。

**Files:**
- Modify: `blog-frontend/index.html`

**Why:** Google Fonts 通过 `@import` 在 CSS 中加载会**串行**（CSS parse 完才发请求），而 `<link>` 在 HTML `<head>` 里**并行**。`preconnect` 提前建 TCP/TLS 连接，可省 100-300ms。

- [ ] **Step 1.1: 修改 `blog-frontend/index.html`**

把整个 `<head>` 替换为：

```html
<head>
  <meta charset="UTF-8" />
  <link rel="icon" type="image/svg+xml" href="/favicon.ico" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />

  <!-- Fonts: preconnect + 并行加载 -->
  <link rel="preconnect" href="https://fonts.googleapis.com" />
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
  <link href="https://fonts.googleapis.com/css2?family=Fraunces:ital,opsz,wght,SOFT,WONK@0,9..144,300..900,30..100,0..1;1,9..144,300..900,30..100,0..1&family=Noto+Serif+SC:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500;600;700&display=swap" rel="stylesheet" />

  <title>技术博客</title>
</head>
```

**关键点：**
- `Fraunces` 是 variable font，含 `opsz`（光学尺寸）/ `wght`（字重）/ `SOFT`（柔度）/ `WONK`（怪诞度）四个轴，体积约 50KB。
- `Noto Serif SC` 是中文 serif，与 Fraunces 笔形相配。
- `JetBrains Mono` 现有项目已用，这里把 weight 范围列出来。
- `display=swap` 确保字体加载时先用 fallback 显示文本，不阻塞渲染。

- [ ] **Step 1.2: 启 dev server 验证字体加载**

```powershell
cd C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog/blog-frontend
npm run dev
```

打开浏览器 `http://localhost:3000` (或 3001)，F12 → Network → Filter "font"，应看到：
- `fraunces.googleapis.com/...woff2` 200
- `noto-serif-sc.../woff2` 200
- `jetbrains-mono.../woff2` 200

字体此时还没在页面上"用"，所以视觉上没变化——这是正常的。**不需要 commit**，与 Task 2 一起验证后合并 commit。

---

## Task 2: tokens.css 加 `--font-display` 与背景 glow token

**Goal:** 把"标题专用 serif 字体栈"与"背景暖光点"作为 token 加入设计系统，让后续组件统一引用。

**Files:**
- Modify: `blog-frontend/src/styles/tokens.css`

- [ ] **Step 2.1: 在 light 主题（`:root {`）的 `--font-sans` 后加 `--font-display` 与 `--font-mono`**

打开 `blog-frontend/src/styles/tokens.css`，找到第 53-54 行（`--font-sans` 与 `--font-mono`），改为：

```css
  --font-sans:    'Inter', 'PingFang SC', 'Noto Sans SC', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  --font-display: 'Fraunces', 'Noto Serif SC', 'Songti SC', 'STSong', serif;
  --font-mono:    'JetBrains Mono', 'Fira Code', 'Cascadia Code', Consolas, monospace;
```

**关键点：**
- `--font-display` 仅在标题用，body 仍保留 `--font-sans` 不变。
- 中文 fallback：`Noto Serif SC` 在线 + `Songti SC` (macOS) + `STSong` (Windows) 兜底。

- [ ] **Step 2.2: 在 light 主题末尾（第 82 行 `--shadow-card-elevate` 后、`}` 闭合前）加背景 glow token**

把 `}` 前一行加入：

```css
  /* 背景质感（B 批 + 演化新增） */
  --bg-glow-1: radial-gradient(ellipse 60% 50% at 30% 20%, rgba(232,168,56,.05) 0%, transparent 60%);
  --bg-glow-2: radial-gradient(ellipse 50% 40% at 80% 80%, rgba(232,168,56,.04) 0%, transparent 60%);
```

完整对照应是：

```css
  --shadow-card-elevate:      0 8px 32px rgba(0,0,0,.06);

  /* 背景质感（B 批 + 演化新增） */
  --bg-glow-1: radial-gradient(ellipse 60% 50% at 30% 20%, rgba(232,168,56,.05) 0%, transparent 60%);
  --bg-glow-2: radial-gradient(ellipse 50% 40% at 80% 80%, rgba(232,168,56,.04) 0%, transparent 60%);
}
```

- [ ] **Step 2.3: 在 dark 主题（`html[data-theme='dark']`）末尾加暗模版 glow（更低饱和度）**

在 dark 主题块的最后 `}` 闭合前，加：

```css
  /* 背景质感（暗主题：紫蓝偏） */
  --bg-glow-1: radial-gradient(ellipse 60% 50% at 30% 20%, rgba(232,168,56,.03) 0%, transparent 60%);
  --bg-glow-2: radial-gradient(ellipse 50% 40% at 80% 80%, rgba(94, 88, 192, .04) 0%, transparent 60%);
```

- [ ] **Step 2.4: Commit Task 1 + Task 2**

```bash
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog add blog-frontend/index.html blog-frontend/src/styles/tokens.css
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog commit -m "feat(frontend): 引入 Fraunces + Noto Serif SC 字体并加 design token

- index.html 加 Google Fonts preconnect 与 Fraunces / Noto Serif SC / JetBrains Mono link
- tokens.css 新增 --font-display（标题专用 serif）
- tokens.css 新增 --bg-glow-1 / --bg-glow-2（双主题各一组）

此为视觉演化的基础设施，后续 task 引用这些 token 实现 UI 升级。"
```

---

## Task 3: FrontLayout 应用新 token — brand / footer / 背景 glow

**Goal:** 把站点标识（brand / footer logo）改成 Fraunces serif；把背景 glow 应用到 layout body。

**Files:**
- Modify: `blog-frontend/src/layouts/FrontLayout.vue`

- [ ] **Step 3.1: 改 `.brand` 与 `.footer-logo` 字体（template 不变，仅 style）**

打开 `blog-frontend/src/layouts/FrontLayout.vue`，找到第 134-143 行 `.brand` 块，把：

```css
.brand {
  font-size: 24px;
  font-weight: 700;
  color: var(--color-accent);
  text-decoration: none;
  letter-spacing: -0.3px;
  flex-shrink: 0;
  transition: opacity var(--transition-fast);
}
```

改为：

```css
.brand {
  font-family: var(--font-display);
  font-variation-settings: "opsz" 100, "wght" 600, "SOFT" 50;
  font-size: 26px;
  font-weight: 600;
  color: var(--color-accent);
  text-decoration: none;
  letter-spacing: -0.015em;
  flex-shrink: 0;
  transition: opacity var(--transition-fast);
}
```

然后找到第 258-263 行 `.footer-logo` 块，把：

```css
.footer-logo {
  font-size: 20px;
  font-weight: 700;
  color: #E8A838;
  text-decoration: none;
}
```

改为：

```css
.footer-logo {
  font-family: var(--font-display);
  font-variation-settings: "opsz" 100, "wght" 600, "SOFT" 50;
  font-size: 22px;
  font-weight: 600;
  color: #E8A838;
  text-decoration: none;
  letter-spacing: -0.012em;
}
```

- [ ] **Step 3.2: 给 `.layout` 加背景 glow**

找到第 104-109 行 `.layout` 块，把：

```css
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg);
}
```

改为：

```css
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg);
  background-image: var(--bg-glow-1), var(--bg-glow-2);
  background-attachment: fixed;
  background-repeat: no-repeat;
}
```

**关键点：** 用 `background-attachment: fixed` 让 glow 跟视口而非内容滚动，避免长页面下 glow 被推走。

- [ ] **Step 3.3: 启 dev server 验证**

```powershell
cd C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog/blog-frontend
npm run dev
```

打开 `/`，应看到：
- 左上 brand 字体由 sans 切换为有衬线 serif（"DevLog." 字形明显改变）
- 背景米色上有非常微妙的暖光点（在 30% 20% 与 80% 80%）—— 要在白色 surface 卡片旁边对比才看得到。

- [ ] **Step 3.4: Commit**

```bash
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog add blog-frontend/src/layouts/FrontLayout.vue
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog commit -m "feat(frontend): FrontLayout brand/footer 改 display 字体 + 背景加 glow"
```

---

## Task 4: HomeView 全局标题与 meta 应用 display / mono

**Goal:** HomeView 中的 `.section-title` / `.newsletter-title` 改用 `--font-display`；`.hero-meta-item` / `.meta-date` 等 meta 类改 `--font-mono`。（hero-title 单独留到 Task 5 做大改造）

**Files:**
- Modify: `blog-frontend/src/views/front/HomeView.vue`

- [ ] **Step 4.1: 改 `.section-title`（第 273 行）**

把：

```css
.section-title { margin: 0; font-size: 24px; font-weight: 700; color: var(--color-text-primary); }
```

改为：

```css
.section-title {
  margin: 0;
  font-family: var(--font-display);
  font-variation-settings: "opsz" 60, "wght" 500, "SOFT" 50;
  font-size: 30px;
  font-weight: 500;
  color: var(--color-text-primary);
  letter-spacing: -0.015em;
}
```

- [ ] **Step 4.2: 改 `.newsletter-title`（第 323-330 行）**

把：

```css
.newsletter-title {
  margin: 0;
  font-size: 30px;
  font-weight: 700;
  color: #F0F0F8;
  text-align: center;
}
:root:not([data-theme='dark']) .newsletter-title { color: #111827; }
```

改为：

```css
.newsletter-title {
  margin: 0;
  font-family: var(--font-display);
  font-variation-settings: "opsz" 100, "wght" 500, "SOFT" 50;
  font-size: 38px;
  font-weight: 500;
  color: #F0F0F8;
  text-align: center;
  letter-spacing: -0.02em;
}
:root:not([data-theme='dark']) .newsletter-title { color: #111827; }
```

- [ ] **Step 4.3: 改 `.hero-meta-item` 与 `.hero-tag` 为 mono（第 205-206 行）**

把：

```css
.hero-meta-item { display: flex; align-items: center; gap: 4px; font-size: 13px; color: #6E6E82; }
.hero-tag { font-size: 13px; color: #E8A838; }
```

改为：

```css
.hero-meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-family: var(--font-mono);
  font-size: 12px;
  color: #6E6E82;
  letter-spacing: 0;
}
.hero-tag {
  font-family: var(--font-mono);
  font-size: 12px;
  color: #E8A838;
  letter-spacing: 0;
}
```

- [ ] **Step 4.4: Commit**

```bash
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog add blog-frontend/src/views/front/HomeView.vue
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog commit -m "feat(frontend): HomeView 标题改 display 字体 / hero meta 改 mono"
```

---

## Task 5: Hero 改造 — 巨大标题 + 斜体强调 + staggered 入场

**Goal:** Hero 的 `.hero-title` 改为 Fraunces 大字号 + 一处斜体强调字 + 整体 4 步 staggered 入场（kicker → title → desc → btns）。这是首屏视觉重塑的核心。

**Files:**
- Modify: `blog-frontend/src/views/front/HomeView.vue`

- [ ] **Step 5.1: template 改 — 在 `.hero-tag-row` 上方加 `.hero-kicker`，`.hero-title` 内加 `<em>` 斜体段**

找到第 4-35 行 hero template 块。改 `.hero-inner` 内的 `.hero-left` 部分：

把：

```vue
<div class="hero-left">
  <div class="hero-tag-row">
    <span class="hero-badge">{{ featured.categoryName || '技术' }}</span>
    <span v-if="featured.isTop" class="hero-badge hero-badge-top">置顶</span>
  </div>
  <h1 class="hero-title">{{ featured.title }}</h1>
  <p class="hero-desc">{{ featured.summary }}</p>
  ...
</div>
```

改为：

```vue
<div class="hero-left">
  <div class="hero-kicker">— 最新一期 / {{ formatKickerDate(featured.publishedAt) }}</div>
  <div class="hero-tag-row">
    <span class="hero-badge">{{ featured.categoryName || '技术' }}</span>
    <span v-if="featured.isTop" class="hero-badge hero-badge-top">置顶</span>
  </div>
  <h1 class="hero-title">{{ featured.title }}</h1>
  <p class="hero-desc">{{ featured.summary }}</p>
  ...
</div>
```

- [ ] **Step 5.2: script 加 `formatKickerDate` 工具**

在 `<script setup>` 块底部（第 129 行 `onMounted` 之前）加：

```js
function formatKickerDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec']
  return `${months[d.getMonth()]} ${d.getFullYear()}`
}
```

- [ ] **Step 5.3: 改 `.hero-title` 字体与字号（第 185-193 行）**

把：

```css
.hero-title {
  margin: 0;
  font-size: 50px;
  font-weight: 700;
  line-height: 1.2;
  color: #F0F0F8;
  letter-spacing: -1px;
}
:root:not([data-theme='dark']) .hero-title { color: #111827; }
```

改为：

```css
.hero-title {
  margin: 0;
  font-family: var(--font-display);
  font-variation-settings: "opsz" 144, "wght" 400, "SOFT" 30, "WONK" 1;
  font-size: clamp(40px, 6.5vw, 72px);
  font-weight: 400;
  line-height: 1.05;
  color: #F0F0F8;
  letter-spacing: -0.025em;
}
:root:not([data-theme='dark']) .hero-title { color: #111827; }
```

- [ ] **Step 5.4: 加 `.hero-kicker` 样式与 staggered 入场动画**

在 `.hero-title` 块后插入：

```css
.hero-kicker {
  font-family: var(--font-display);
  font-style: italic;
  font-variation-settings: "opsz" 14, "wght" 400;
  font-size: 14px;
  color: var(--color-accent);
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

@keyframes hero-fade-up {
  from { opacity: 0; transform: translateY(14px); }
  to   { opacity: 1; transform: translateY(0); }
}

.hero-kicker,
.hero-tag-row,
.hero-title,
.hero-desc,
.hero-meta,
.hero-btns,
.hero-right {
  opacity: 0;
  animation: hero-fade-up .8s ease forwards;
}
.hero-kicker  { animation-delay: 0.05s; }
.hero-tag-row { animation-delay: 0.15s; }
.hero-title   { animation-delay: 0.25s; }
.hero-desc    { animation-delay: 0.40s; }
.hero-meta    { animation-delay: 0.55s; }
.hero-btns    { animation-delay: 0.70s; }
.hero-right   { animation-delay: 0.30s; }
```

- [ ] **Step 5.5: 启 dev server 验证**

```powershell
npm run dev
```

打开 `/`：
- 进入页面时 hero 各元素**依次淡入上浮**（kicker → title → desc → 按钮）
- hero 标题字体明显从 sans 切到 Fraunces serif，字号更大（72px on desktop）
- hero 上方多出"— 最新一期 / Jun 2026"小字 kicker

- [ ] **Step 5.6: Commit**

```bash
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog add blog-frontend/src/views/front/HomeView.vue
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog commit -m "feat(frontend): Hero 巨标题 + kicker + 4 步 staggered 入场动画"
```

---

## Task 6: ArticleCardGrid 升级 — serif 标题 + mono meta + hover read-more

**Goal:** HomeView 用到的 ArticleCardGrid 卡片标题改 display，meta 改 mono，hover 时在卡片底部渐显"阅读 →"提示。

**Files:**
- Modify: `blog-frontend/src/components/front/ArticleCardGrid.vue`

- [ ] **Step 6.1: template 加 hover 时显示的 "阅读 →" 节点**

打开 `blog-frontend/src/components/front/ArticleCardGrid.vue`，在第 17 行 `</article>` 之前（紧贴 `</div>` 卡片 body 之后）加：

把：

```vue
    </div>
  </article>
</template>
```

改为：

```vue
    </div>
    <div class="card-read-hint">
      <span>阅读</span>
      <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
    </div>
  </article>
</template>
```

- [ ] **Step 6.2: 改 `.card-title` 用 display（第 71-82 行）**

把：

```css
.card-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color var(--transition-fast);
}
```

改为：

```css
.card-title {
  margin: 0;
  font-family: var(--font-display);
  font-variation-settings: "opsz" 36, "wght" 500, "SOFT" 50;
  font-size: 19px;
  font-weight: 500;
  color: var(--color-text-primary);
  line-height: 1.25;
  letter-spacing: -0.012em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color var(--transition-fast);
}
```

- [ ] **Step 6.3: 改 `.card-meta` 用 mono（第 97-105 行）**

把：

```css
.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: var(--color-text-tertiary);
  padding-top: 8px;
  border-top: 1px solid var(--color-border);
}
```

改为：

```css
.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-family: var(--font-mono);
  font-size: 11px;
  color: var(--color-text-tertiary);
  letter-spacing: .02em;
  padding-top: 10px;
  border-top: 1px solid var(--color-border);
}
```

- [ ] **Step 6.4: 加 `.card-read-hint` 样式与 hover 显隐**

在 `</style>` 闭合前（第 107 行 `.meta-views` 之后）插入：

```css
.card-read-hint {
  position: absolute;
  right: 16px;
  bottom: 16px;
  display: flex;
  align-items: center;
  gap: 4px;
  font-family: var(--font-display);
  font-style: italic;
  font-size: 13px;
  color: var(--color-accent);
  opacity: 0;
  transform: translateX(-6px);
  transition: opacity 200ms ease, transform 200ms ease;
  pointer-events: none;
}
.grid-card { position: relative; }
.grid-card:hover .card-read-hint {
  opacity: 1;
  transform: translateX(0);
}
```

- [ ] **Step 6.5: 启 dev server 验证**

打开 `/`，hover 文章卡片：
- 标题字体由 sans 改 serif（更优雅）
- meta 数字用 mono（专业感）
- 右下角渐显"阅读 →"

- [ ] **Step 6.6: Commit**

```bash
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog add blog-frontend/src/components/front/ArticleCardGrid.vue
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog commit -m "feat(frontend): ArticleCardGrid 标题改 display / meta 改 mono / hover 渐显阅读"
```

---

## Task 7: ArticleCard 同步升级（用在 PostList / Tag / Category 页）

**Goal:** ArticleCard（带封面的卡片，用于 PostListView / TagView / CategoryView）做同样的字体升级。保持与 ArticleCardGrid 视觉一致。

**Files:**
- Modify: `blog-frontend/src/components/front/ArticleCard.vue`

- [ ] **Step 7.1: 改 `.title`（第 106-117 行）**

把：

```css
.title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--color-text-primary);
  line-height: 1.45;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color var(--transition-fast);
}
```

改为：

```css
.title {
  margin: 0;
  font-family: var(--font-display);
  font-variation-settings: "opsz" 36, "wght" 500, "SOFT" 50;
  font-size: 19px;
  font-weight: 500;
  color: var(--color-text-primary);
  line-height: 1.25;
  letter-spacing: -0.012em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color var(--transition-fast);
}
```

- [ ] **Step 7.2: 改 `.meta-bottom` / `.meta-item` / `.tag-link` 用 mono（第 132-157 行）**

把：

```css
.meta-bottom {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 4px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: var(--color-text-tertiary);
}

.meta-icon { width: 12px; height: 12px; }

.tag-link {
  font-size: 11px;
  color: var(--color-text-tertiary);
  cursor: pointer;
  transition: color var(--transition-fast);
  padding: 1px 0;
}
.tag-link:hover { color: var(--color-accent); }
```

改为：

```css
.meta-bottom {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 6px;
  font-family: var(--font-mono);
  letter-spacing: .02em;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 11px;
  color: var(--color-text-tertiary);
}

.meta-icon { width: 12px; height: 12px; }

.tag-link {
  font-size: 11px;
  color: var(--color-text-tertiary);
  cursor: pointer;
  transition: color var(--transition-fast);
  padding: 1px 0;
}
.tag-link:hover { color: var(--color-accent); }
```

- [ ] **Step 7.3: Commit**

```bash
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog add blog-frontend/src/components/front/ArticleCard.vue
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog commit -m "feat(frontend): ArticleCard 标题改 display / meta 改 mono（与 Grid 对齐）"
```

---

## Task 8: Section divider 重设计 — 虚线 + 居中 label

**Goal:** 现在 HomeView 的 `.section-divider` 是一根硬实线。改为带居中文字 label 的虚线分隔（如 `── 最近的字 ──`），刊物感、节奏感更强。

**Files:**
- Modify: `blog-frontend/src/views/front/HomeView.vue`

- [ ] **Step 8.1: template 改 — 把空的 `<div class="section-divider"></div>` 改为带 label 的 figure**

找到第 36 行 `<div class="section-divider"></div>` 与第 64 行同样的一行。把它们都改为：

第 36 行（在 hero 与文章列表之间）：

```vue
<div class="section-divider">
  <span>最近的字</span>
</div>
```

第 64 行（在文章列表与 newsletter 之间）：

```vue
<div class="section-divider">
  <span>· · ·</span>
</div>
```

并删掉 articles-section 内原来的 `.section-header` 区块的 `.section-title`（第 41-47 行）—— 因为 "最近的字" 已经在 divider 上呈现了。把：

```vue
<div class="section-header">
  <h2 class="section-title">最新文章</h2>
  <router-link to="/posts" class="section-link">
    查看全部
    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
  </router-link>
</div>
```

改为：

```vue
<div class="section-header">
  <router-link to="/posts" class="section-link">
    查看全部
    <svg viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" stroke-width="2"><polyline points="9 18 15 12 9 6"/></svg>
  </router-link>
</div>
```

注意 `.section-header` 内只剩一个 link，需要把它对齐到右边。（已经是 `justify-content: space-between`，单元素时它自动靠右——但为更明显，下一 step 微调）。

- [ ] **Step 8.2: 改 `.section-divider` 样式（第 259 行）**

把：

```css
.section-divider { height: 1px; background: var(--color-border); }
```

改为：

```css
.section-divider {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18px;
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 0 64px;
  position: relative;
  height: 56px;
}
.section-divider::before,
.section-divider::after {
  content: '';
  flex: 1;
  height: 0;
  border-top: 1px dashed var(--color-border);
}
.section-divider span {
  font-family: var(--font-mono);
  font-size: 11px;
  letter-spacing: .2em;
  text-transform: uppercase;
  color: var(--color-text-tertiary);
}
```

- [ ] **Step 8.3: 调 `.section-header` 让单 link 靠右**

找到第 272 行 `.section-header`，把：

```css
.section-header { display: flex; justify-content: space-between; align-items: center; }
```

改为：

```css
.section-header { display: flex; justify-content: flex-end; align-items: center; }
```

- [ ] **Step 8.4: 启 dev server 验证**

打开 `/`，hero 与文章列表之间应看到：
- 一条横跨视口的虚线（左右各一段）
- 中间居中显示 `最近的字`（mono 字体、灰色、字母间距宽）
- 文章列表区域 `查看全部` 链接靠右

- [ ] **Step 8.5: Commit**

```bash
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog add blog-frontend/src/views/front/HomeView.vue
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog commit -m "feat(frontend): Section divider 改虚线 + 居中 mono label"
```

---

## Task 9: 跨页面验证 + 合并

**Goal:** 检查所有改动在主要 view 上不破坏其它页面（PostList / TagView / CategoryView / SearchView 用 ArticleCard，应该自动跟随升级），双主题切换 OK，移动端响应不破。最后 merge 回 master。

**Files:** 无新文件。

- [ ] **Step 9.1: frontend build 兜底**

```bash
cd C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog/blog-frontend
npx vite build 2>&1 | tail -10
```

预期：BUILD SUCCESS。如果有 lint / build error 在这里暴露。

- [ ] **Step 9.2: 启 dev server 手测清单**

```bash
npm run dev
```

打开浏览器，逐项验证：

| # | 页面 | 预期 |
|---|------|------|
| 1 | `/` 首页 | hero staggered 入场、巨大 serif 标题、kicker、meta mono、divider 虚线+label、卡片 hover read-more |
| 2 | `/posts` 文章列表 | ArticleCard 标题 serif、meta mono、卡片 hover lift（已有） |
| 3 | `/category/xxx` 分类页 | ArticleCard 同上 |
| 4 | `/tag/xxx` 标签页 | ArticleCard 同上 |
| 5 | `/search?q=xxx` 搜索页 | ArticleCard 同上 |
| 6 | `/posts/xxx` 文章详情页 | header brand serif；body 字体保持 sans（本次不动）|
| 7 | header brand | "DevLog." 用 Fraunces 显示 |
| 8 | footer logo | "DevLog." 用 Fraunces 显示 |
| 9 | 暗/亮主题切换 | 切到暗模时所有 serif 字体仍正常显示，不发糊 |
| 10 | 浏览器调小窗口（< 768px） | hero 不溢出、卡片成单列、divider label 仍居中 |

- [ ] **Step 9.3: 检查 git 状态**

```bash
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog status
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog log --oneline master..HEAD
```

预期：无未提交改动；列出本批所有 commits（应该 7 个，对应 Task 1+2 合并、Task 3、Task 4、Task 5、Task 6、Task 7、Task 8）。

- [ ] **Step 9.4: 合并到 master**

```bash
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog checkout master
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog merge --no-ff feat/frontend-soft-modern-evolve -m "Merge: 前台 Soft Modern 演化升级

合并 feat/frontend-soft-modern-evolve 到 master。

引入 Fraunces 可变 serif + Noto Serif SC 作为标题字体，
JetBrains Mono 用于 meta 信息；Hero 加 staggered 入场 +
kicker + 巨标题；ArticleCard 加 serif 标题 + mono meta +
hover 渐显阅读提示；Section divider 改虚线 + 居中 label。

frontend vite build 通过；10 项手测全过。
"
git -C C:/Users/Administrator/Desktop/cc-project/cc-blog/my-ai-blog branch -d feat/frontend-soft-modern-evolve
```

- [ ] **Step 9.5: 向用户汇报**

报告：
- 字体改造：Fraunces 加入设计系统，3 处标题用例 + 2 处 brand 用例已迁移
- 视觉改造：Hero + ArticleCard + ArticleCardGrid + Section divider 共 4 处
- 改动总量：6 文件 / 约 +XXX -XX 行
- 手测：10 项通过情况
- 已知不在范围（档 3/4 的 T8-T13）：PostDetail drop-cap、阅读进度、代码块字体、Logo SVG、404/About、暗主题 letter-spacing 微调

---

## 实施完成后的下一步

档 2 完成后：
- 走 `superpowers:finishing-a-development-branch` 决定 push / PR / 保留
- 或继续档 3（T8-T10）：PostDetailView 深度优化
- 或继续档 4（T11-T13）：Logo / 404 / About / 暗主题微调
