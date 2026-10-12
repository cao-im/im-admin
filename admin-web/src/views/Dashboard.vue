<template>
  <div class="dashboard">
    <section class="hero-strip">
      <div>
        <p class="eyebrow">Realtime Pulse</p>
        <h2>会话运行概览</h2>
        <p class="desc">一眼看清用户规模、群聊健康度与今日消息吞吐。</p>
      </div>
      <div class="live-badge">
        <span class="pulse" />
        数据实时刷新
      </div>
    </section>

    <div class="cards">
      <article v-for="card in cards" :key="card.key" class="stat-card" :class="card.tone">
        <div class="stat-top">
          <div class="icon-wrap">
            <el-icon :size="22"><component :is="card.icon" /></el-icon>
          </div>
          <span class="label">{{ card.label }}</span>
        </div>
        <div class="num">{{ formatNum(stats[card.key]) }}</div>
        <div class="foot">
          <span>{{ card.hint }}</span>
          <div class="spark" aria-hidden="true">
            <i v-for="n in 7" :key="n" :style="{ height: sparkHeight(card.key, n) }" />
          </div>
        </div>
      </article>
    </div>

    <section class="insight-grid">
      <div class="panel">
        <h3>运营建议</h3>
        <ul>
          <li>关注今日消息峰值，排查异常刷屏与敏感词命中。</li>
          <li>定期复核禁用用户与已解散群，保持账号体系干净。</li>
          <li>在系统配置中按业务节奏调整注册开关与群容量。</li>
        </ul>
      </div>
      <div class="panel accent">
        <h3>控制台能力</h3>
        <div class="chips">
          <span>用户启停</span>
          <span>群聊治理</span>
          <span>消息回溯</span>
          <span>敏感词库</span>
          <span>审计追踪</span>
          <span>角色权限</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { ChatDotRound, User, UserFilled } from '@element-plus/icons-vue'
import http from '../api/http'

const stats = reactive({ userCount: 0, groupCount: 0, todayMessageCount: 0 })

const cards = [
  { key: 'userCount', label: '注册用户', icon: User, tone: 'teal', hint: '客户端账号总量' },
  { key: 'groupCount', label: '正常群聊', icon: UserFilled, tone: 'sky', hint: '未解散的活跃群' },
  { key: 'todayMessageCount', label: '今日消息', icon: ChatDotRound, tone: 'ink', hint: '当日会话吞吐' }
]

function formatNum(value) {
  const n = Number(value || 0)
  return n.toLocaleString('zh-CN')
}

function sparkHeight(key, index) {
  const base = Number(stats[key] || 0)
  const seed = (base + index * 17) % 37
  return `${28 + seed}%`
}

onMounted(async () => {
  const body = await http.get('/api/im/stats')
  Object.assign(stats, body.data || {})
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.hero-strip {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  padding: 22px 24px;
  border-radius: var(--im-radius);
  background:
    linear-gradient(120deg, rgba(18, 184, 168, 0.14), rgba(56, 189, 248, 0.1) 55%, rgba(255, 255, 255, 0.7)),
    #fff;
  border: 1px solid var(--im-border);
  box-shadow: var(--im-shadow-sm);
}

.eyebrow {
  margin: 0 0 6px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--im-teal-deep);
}

.hero-strip h2 {
  margin: 0;
  font-family: var(--im-display);
  font-size: 26px;
  font-weight: 700;
}

.desc {
  margin: 8px 0 0;
  color: var(--im-text-secondary);
  font-size: 14px;
}

.live-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(15, 23, 42, 0.04);
  color: var(--im-text-secondary);
  font-size: 13px;
  font-weight: 600;
  white-space: nowrap;
}

.pulse {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #14b8a6;
  box-shadow: 0 0 0 0 rgba(20, 184, 166, 0.55);
  animation: ping 1.8s ease-out infinite;
}

.cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  padding: 20px 20px 16px;
  border-radius: var(--im-radius);
  background: #fff;
  border: 1px solid var(--im-border);
  box-shadow: var(--im-shadow-sm);
  animation: im-fade-up 0.4s ease both;
}

.stat-card:nth-child(2) { animation-delay: 0.05s; }
.stat-card:nth-child(3) { animation-delay: 0.1s; }

.stat-card::after {
  content: "";
  position: absolute;
  right: -40px;
  top: -40px;
  width: 140px;
  height: 140px;
  border-radius: 50%;
  opacity: 0.35;
}

.stat-card.teal::after { background: radial-gradient(circle, rgba(20, 184, 166, 0.45), transparent 70%); }
.stat-card.sky::after { background: radial-gradient(circle, rgba(56, 189, 248, 0.45), transparent 70%); }
.stat-card.ink::after { background: radial-gradient(circle, rgba(15, 23, 42, 0.18), transparent 70%); }

.stat-top {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}

.icon-wrap {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  color: #fff;
}

.teal .icon-wrap { background: linear-gradient(135deg, #14b8a6, #0d9488); }
.sky .icon-wrap { background: linear-gradient(135deg, #38bdf8, #0284c7); }
.ink .icon-wrap { background: linear-gradient(135deg, #1e293b, #0f172a); }

.label {
  font-size: 14px;
  font-weight: 600;
  color: var(--im-text-secondary);
}

.num {
  font-family: var(--im-display);
  font-size: 40px;
  font-weight: 700;
  letter-spacing: -0.03em;
  line-height: 1;
  margin-bottom: 14px;
}

.foot {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  color: var(--im-text-muted);
  font-size: 12px;
}

.spark {
  display: flex;
  align-items: flex-end;
  gap: 3px;
  height: 28px;
}

.spark i {
  width: 4px;
  border-radius: 999px;
  background: linear-gradient(180deg, #2dd4bf, rgba(45, 212, 191, 0.25));
  display: block;
}

.insight-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 16px;
}

.panel {
  padding: 20px 22px;
  border-radius: var(--im-radius);
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid var(--im-border);
  box-shadow: var(--im-shadow-sm);
}

.panel.accent {
  background:
    linear-gradient(160deg, rgba(15, 23, 42, 0.96), #0b1220 70%),
    #0b1220;
  color: #e8eef8;
  border-color: transparent;
}

.panel h3 {
  margin: 0 0 14px;
  font-family: var(--im-display);
  font-size: 16px;
}

.panel ul {
  margin: 0;
  padding-left: 18px;
  color: var(--im-text-secondary);
  line-height: 1.7;
  font-size: 14px;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.chips span {
  padding: 7px 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.1);
  font-size: 12px;
  font-weight: 600;
}

@keyframes ping {
  0% { box-shadow: 0 0 0 0 rgba(20, 184, 166, 0.45); }
  70% { box-shadow: 0 0 0 10px rgba(20, 184, 166, 0); }
  100% { box-shadow: 0 0 0 0 rgba(20, 184, 166, 0); }
}

@media (max-width: 960px) {
  .cards,
  .insight-grid {
    grid-template-columns: 1fr;
  }
  .hero-strip {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
