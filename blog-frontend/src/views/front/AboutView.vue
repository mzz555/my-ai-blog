<template>
  <div class="about">
    <!-- Hero -->
    <section class="a-hero">
      <div class="a-hero-inner">
        <div class="a-hero-left">
          <div class="a-badge">全栈开发者</div>
          <h1 class="a-name">你好，我是{{ displayName }} 👋</h1>
          <p class="a-role">全栈开发工程师 · 开源贡献者 · 技术博主</p>
          <p class="a-bio">{{ bio }}</p>
          <div class="a-btns">
            <router-link to="/posts" class="btn-primary">查看我的文章</router-link>
            <a href="mailto:your@email.com" class="btn-ghost">联系我</a>
          </div>
        </div>
        <div class="a-hero-right">
          <div class="a-avatar">
            <span class="a-avatar-text">{{ avatarChar }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- Stats -->
    <section class="a-stats">
      <div class="a-stats-inner">
        <div class="a-stat"><span class="a-stat-num">48</span><span class="a-stat-lab">篇技术文章</span></div>
        <div class="a-sep"></div>
        <div class="a-stat"><span class="a-stat-num">5</span><span class="a-stat-lab">年开发经验</span></div>
        <div class="a-sep"></div>
        <div class="a-stat"><span class="a-stat-num">12.8k</span><span class="a-stat-lab">月活读者</span></div>
        <div class="a-sep"></div>
        <div class="a-stat"><span class="a-stat-num">320</span><span class="a-stat-lab">GitHub Stars</span></div>
      </div>
    </section>

    <!-- Tech Stack -->
    <section class="a-skills">
      <div class="a-section-inner">
        <div class="a-section-head">
          <span class="a-label">TECH STACK</span>
          <h2 class="a-section-title">我的技术栈</h2>
        </div>
        <div class="a-skills-grid">
          <div class="a-skill-col" v-for="(col, ci) in skillCols" :key="ci">
            <div v-for="sk in col" :key="sk.name" class="a-skill-item">
              <div class="a-skill-row">
                <span class="a-skill-name">{{ sk.name }}</span>
                <span class="a-skill-pct">{{ sk.pct }}%</span>
              </div>
              <div class="a-skill-bar"><div class="a-skill-fill" :style="{ width: sk.pct + '%' }"></div></div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Experience -->
    <section class="a-timeline">
      <div class="a-section-inner">
        <div class="a-section-head">
          <span class="a-label">EXPERIENCE</span>
          <h2 class="a-section-title">成长历程</h2>
        </div>
        <div class="a-time-list">
          <div v-for="item in timeline" :key="item.year" class="a-time-item">
            <div class="a-time-dot"></div>
            <div class="a-time-body">
              <div class="a-time-header">
                <span class="a-time-year">{{ item.year }}</span>
                <h3 class="a-time-title">{{ item.title }}</h3>
              </div>
              <p class="a-time-desc">{{ item.desc }}</p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Contact -->
    <section class="a-contact">
      <div class="a-contact-inner">
        <span class="a-label">GET IN TOUCH</span>
        <h2 class="a-contact-title">一起构建有价值的东西</h2>
        <p class="a-contact-sub">无论是技术交流、开源合作还是项目咨询，欢迎随时联系我</p>
        <div class="a-contact-links">
          <a href="mailto:your@email.com" class="a-contact-btn">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><rect x="2" y="4" width="20" height="16" rx="2"/><polyline points="22,4 12,13 2,4"/></svg>
            发送邮件
          </a>
          <a href="https://github.com" target="_blank" rel="noopener" class="a-contact-btn">
            <svg viewBox="0 0 24 24" width="16" height="16" fill="currentColor"><path d="M12 2C6.477 2 2 6.484 2 12.017c0 4.425 2.865 8.18 6.839 9.504.5.092.682-.217.682-.483 0-.237-.008-.868-.013-1.703-2.782.605-3.369-1.343-3.369-1.343-.454-1.158-1.11-1.466-1.11-1.466-.908-.62.069-.608.069-.608 1.003.07 1.531 1.032 1.531 1.032.892 1.53 2.341 1.088 2.91.832.092-.647.35-1.088.636-1.338-2.22-.253-4.555-1.113-4.555-4.951 0-1.093.39-1.988 1.029-2.688-.103-.253-.446-1.272.098-2.65 0 0 .84-.27 2.75 1.026A9.564 9.564 0 0112 6.844c.85.004 1.705.115 2.504.337 1.909-1.296 2.747-1.027 2.747-1.027.546 1.379.202 2.398.1 2.651.64.7 1.028 1.595 1.028 2.688 0 3.848-2.339 4.695-4.566 4.943.359.309.678.92.678 1.855 0 1.338-.012 2.419-.012 2.747 0 .268.18.58.688.482A10.019 10.019 0 0022 12.017C22 6.484 17.522 2 12 2z"/></svg>
            GitHub
          </a>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getMe } from '@/api/auth'

const userInfo = ref(null)
onMounted(async () => {
  try { const res = await getMe(); userInfo.value = res.data } catch {}
})

const displayName = computed(() => userInfo.value?.nickname || userInfo.value?.username || '博主')
const avatarChar = computed(() => displayName.value.charAt(0))
const bio = computed(() =>
  userInfo.value?.bio ||
  '专注于 Spring Boot、Vue 3 与云原生架构，致力于把复杂的技术概念用简单清晰的语言表达出来。这个博客记录了我过去 5 年的技术成长历程。'
)

const allSkills = [
  { name: 'Spring Boot', pct: 95 },
  { name: 'MyBatis / Nuxt', pct: 80 },
  { name: 'Vue 3 / Vite', pct: 80 },
  { name: 'TypeScript', pct: 82 },
  { name: 'Docker / K8s', pct: 75 },
  { name: 'MySQL / OSS', pct: 85 },
]
const skillCols = [allSkills.slice(0,2), allSkills.slice(2,4), allSkills.slice(4,6)]

const timeline = [
  { year: '2024', title: '独立博客平台上线，单月访问破万', desc: '完成从 0 到 1 的产品构建，掌握全栈开发的完整工程流程，包含 CI/CD 自动部署。' },
  { year: '2021', title: '加入现任公司，负责后端架构设计', desc: '主导微服务拆分项目，将系统响应时间降低 37%，带领 5 人后端团队。' },
  { year: '2019', title: '研发工程师，互联网头部公司', desc: '参与亿级用户系统的维护与迭代，深入了解高并发场景下的 Java 性能调优。' },
]
</script>

<style scoped>
.about { display: flex; flex-direction: column; }

.a-hero { background: var(--color-bg); }
.a-hero-inner {
  max-width: var(--content-max-width);
  margin: 0 auto;
  padding: 80px 100px;
  display: flex;
  align-items: center;
  gap: 64px;
}
.a-hero-left { flex: 1; display: flex; flex-direction: column; gap: 24px; }

.a-badge {
  display: inline-flex;
  align-items: center;
  padding: 6px 14px;
  background: rgba(232,168,56,.12);
  border: 1px solid rgba(232,168,56,.25);
  border-radius: var(--radius-full);
  font-size: 12px;
  font-weight: 600;
  color: #E8A838;
  width: fit-content;
}
.a-name { margin: 0; font-size: 42px; font-weight: 700; color: var(--color-text-primary); line-height: 1.2; }
.a-role { margin: 0; font-size: 18px; font-weight: 500; color: #E8A838; }
.a-bio { margin: 0; font-size: 15px; line-height: 1.8; color: var(--color-text-secondary); }
.a-btns { display: flex; gap: 16px; }

.btn-primary {
  display: inline-flex; align-items: center;
  padding: 12px 24px; background: #E8A838; color: #0C0C10;
  border-radius: var(--radius-md); font-size: 14px; font-weight: 600;
  text-decoration: none; transition: background var(--transition-fast);
}
.btn-primary:hover { background: #F5BC50; }

.btn-ghost {
  display: inline-flex; align-items: center;
  padding: 12px 24px; border: 1px solid var(--color-border); color: var(--color-text-secondary);
  border-radius: var(--radius-md); font-size: 14px; font-weight: 500;
  text-decoration: none; transition: border-color var(--transition-fast), color var(--transition-fast);
}
.btn-ghost:hover { border-color: #E8A838; color: #E8A838; }

.a-hero-right { flex-shrink: 0; }
.a-avatar {
  width: 220px; height: 220px; border-radius: 50%;
  background: #E8A838; display: flex; align-items: center; justify-content: center;
}
.a-avatar-text { font-size: 80px; font-weight: 700; color: #0C0C10; line-height: 1; }

.a-stats {
  background: var(--color-bg-tertiary);
  border-top: 1px solid var(--color-border);
  border-bottom: 1px solid var(--color-border);
}
[data-theme='dark'] .a-stats { background: #0D0D18; }
.a-stats-inner {
  max-width: var(--content-max-width); margin: 0 auto;
  display: flex; justify-content: center;
}
.a-stat {
  display: flex; flex-direction: column; align-items: center;
  gap: 6px; padding: 36px 64px;
}
.a-stat-num { font-size: 40px; font-weight: 700; color: #E8A838; line-height: 1; }
.a-stat-lab { font-size: 14px; color: var(--color-text-tertiary); }
.a-sep { width: 1px; height: 80px; background: var(--color-border); align-self: center; }

.a-skills { background: var(--color-bg); }
.a-timeline { background: var(--color-bg-tertiary); }
[data-theme='dark'] .a-timeline { background: #0D0D18; }

.a-section-inner {
  max-width: var(--content-max-width); margin: 0 auto;
  padding: 64px 100px; display: flex; flex-direction: column; gap: 32px;
}
.a-section-head { display: flex; flex-direction: column; gap: 8px; }
.a-label { font-size: 12px; font-weight: 600; color: #E8A838; letter-spacing: 2px; }
.a-section-title { margin: 0; font-size: 28px; font-weight: 700; color: var(--color-text-primary); }

.a-skills-grid { display: grid; grid-template-columns: repeat(3,1fr); gap: 16px; }
.a-skill-col { display: flex; flex-direction: column; gap: 16px; }
.a-skill-item { display: flex; flex-direction: column; gap: 8px; }
.a-skill-row { display: flex; justify-content: space-between; }
.a-skill-name { font-size: 14px; color: var(--color-text-primary); }
.a-skill-pct { font-size: 13px; color: var(--color-text-tertiary); }
.a-skill-bar { height: 6px; background: var(--color-border); border-radius: var(--radius-full); overflow: hidden; }
.a-skill-fill { height: 100%; background: #E8A838; border-radius: var(--radius-full); }

.a-time-list { display: flex; flex-direction: column; }
.a-time-item { display: flex; gap: 24px; padding-bottom: 32px; position: relative; }
.a-time-item:not(:last-child)::before {
  content: ''; position: absolute; left: 7px; top: 16px; bottom: 0;
  width: 1px; background: var(--color-border);
}
.a-time-dot {
  width: 16px; height: 16px; border-radius: 50%; background: #E8A838;
  flex-shrink: 0; margin-top: 4px; position: relative; z-index: 1;
}
.a-time-body { flex: 1; }
.a-time-header { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.a-time-year {
  font-size: 12px; font-weight: 600; color: #E8A838;
  background: rgba(232,168,56,.12); border: 1px solid rgba(232,168,56,.25);
  padding: 2px 8px; border-radius: var(--radius-full);
}
.a-time-title { margin: 0; font-size: 16px; font-weight: 600; color: var(--color-text-primary); }
.a-time-desc { margin: 0; font-size: 14px; color: var(--color-text-secondary); line-height: 1.7; }

.a-contact { background: var(--color-bg); }
.a-contact-inner {
  max-width: var(--content-max-width); margin: 0 auto;
  padding: 64px 100px; display: flex; flex-direction: column; align-items: center; gap: 32px; text-align: center;
}
.a-contact-title { margin: 0; font-size: 32px; font-weight: 700; color: var(--color-text-primary); }
.a-contact-sub { margin: 0; font-size: 15px; color: var(--color-text-secondary); }
.a-contact-links { display: flex; gap: 16px; }
.a-contact-btn {
  display: inline-flex; align-items: center; gap: 10px;
  padding: 14px 24px; background: var(--color-surface);
  border: 1px solid var(--color-border); border-radius: var(--radius-md);
  font-size: 14px; color: var(--color-text-primary); text-decoration: none;
  transition: border-color var(--transition-fast), color var(--transition-fast);
}
.a-contact-btn:hover { border-color: #E8A838; color: #E8A838; }
</style>
