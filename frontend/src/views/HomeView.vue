<!--
  HomeView.vue - 首页/山河卷视图
  
  功能说明：
  1. 展示地图界面，用户可以浏览和探索文化遗产景点
  2. 集成 AI 助手面板，用户可以与"文迹书生"进行对话
  3. 点击景点后显示文物详情卡片（AiGuideCard）
  4. 底部导航栏：山河卷、AR鉴赏、藏经阁
  
  主要组件：
  - CityMap: 地图组件，显示景点标记和城市切换
  - AiGuideCard: 文物详情卡片，包含 AI 讲解功能
  - AI Panel: AI 对话面板，支持多会话管理
  
  数据流：
  - defaultNodes: 预设的文物节点数据
  - userStore: 用户状态管理（阅历、收藏等）
  - backendSites: 从后端获取的附近景点数据
-->
<template>
  <div class="home-view">
    <div class="view-header">
      <h1 class="view-title font-serif">山河卷</h1>
      <div class="header-actions">
        <button class="ai-btn" @click="openAiPanel">
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2z"/>
            <path d="M12 16v-4"/>
            <path d="M12 8h.01"/>
          </svg>
          <span>AI助手</span>
        </button>
        <div class="user-stats">
          <span class="stat-item">
            <span class="stat-label">阅历</span>
            <span class="stat-value">{{ userStore.experience }}</span>
          </span>
        </div>
      </div>
    </div>
    
    <div class="map-container">
      <CityMap @explore="handleSiteExplore" />
    </div>
    
    <nav class="bottom-nav">
      <router-link to="/" class="nav-item active">
        <span class="nav-icon">山</span>
        <span class="nav-label">山河卷</span>
      </router-link>
      <router-link to="/ar" class="nav-item">
        <span class="nav-icon">鉴</span>
        <span class="nav-label">AR鉴赏</span>
      </router-link>
      <router-link to="/profile" class="nav-item">
        <span class="nav-icon">阁</span>
        <span class="nav-label">藏经阁</span>
      </router-link>
    </nav>
    
    <AiGuideCard
      :visible="showAiCard"
      :artifact="selectedArtifact"
      @close="closeAiCard"
      @like="handleLike"
      @share="handleShare"
      @ar-experience="handleArExperience"
      @ai-complete="handleAiComplete"
    />
    
    <transition name="slide-up">
      <div v-if="showAiPanel" class="ai-panel">
        <div class="ai-panel-header">
          <div class="header-left">
            <button class="menu-btn" @click="showSidebar = !showSidebar" :class="{ active: showSidebar }">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="3" y1="12" x2="21" y2="12"></line>
                <line x1="3" y1="6" x2="21" y2="6"></line>
                <line x1="3" y1="18" x2="21" y2="18"></line>
              </svg>
            </button>
            <span class="ai-title font-serif">文迹书生</span>
          </div>
          <button class="close-btn" @click="showAiPanel = false">×</button>
        </div>
        
        <div class="ai-panel-body">
          <transition name="slide-left">
            <div v-if="showSidebar" class="ai-sidebar">
              <button class="new-chat-btn" @click="createNewChat">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="12" y1="5" x2="12" y2="19"></line>
                  <line x1="5" y1="12" x2="19" y2="12"></line>
                </svg>
                新对话
              </button>
              
              <div class="chat-list">
                <div 
                  v-for="session in chatSessions" 
                  :key="session.id"
                  class="chat-item"
                  :class="{ active: currentSessionId === session.id }"
                  @click="loadSession(session.id)"
                >
                  <div class="chat-item-info">
                    <span class="chat-item-title">{{ session.title || '新对话' }}</span>
                    <span class="chat-item-time">{{ formatTime(session.updateTime) }}</span>
                  </div>
                  <button class="delete-btn" @click.stop="deleteSession(session.id)">
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <polyline points="3 6 5 6 21 6"></polyline>
                      <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
                    </svg>
                  </button>
                </div>
                
                <div v-if="chatSessions.length === 0" class="empty-tip">
                  暂无对话记录
                </div>
              </div>
            </div>
          </transition>
          
          <div class="ai-main">
            <div class="ai-panel-messages" ref="messagesContainer">
              <div 
                v-for="(msg, idx) in aiMessages" 
                :key="idx" 
                class="message"
                :class="{ 'from-user': msg.fromUser }"
              >
                <div class="message-content">{{ msg.text }}</div>
              </div>
              <div v-if="isAiTyping" class="message typing">
                <div class="typing-indicator">
                  <span></span><span></span><span></span>
                </div>
              </div>
            </div>
            
            <div class="ai-panel-input">
              <div class="ai-usage-info">
                <span class="usage-text">
                  今日剩余: <strong>{{ authStore.aiRemaining }}</strong> 次
                  <span v-if="authStore.extraAiCount > 0" class="extra-count">(+{{ authStore.extraAiCount }}兑换)</span>
                </span>
                <button v-if="userStore.experience >= authStore.EXPERIENCE_COST_PER_AI" 
                        class="exchange-btn" 
                        @click="showExchangeDialog = true"
                        :title="`使用 ${authStore.EXPERIENCE_COST_PER_AI} 阅历兑换 1 次 AI 对话`">
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
                  </svg>
                  兑换
                </button>
              </div>
              <div class="input-row">
                <input 
                  v-model="aiInput" 
                  placeholder="问我任何关于传统文化的问题..."
                  @keyup.enter="sendAiMessage"
                />
                <button class="send-btn" @click="sendAiMessage" :disabled="!aiInput.trim() || isAiTyping">
                  发送
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>
    
    <transition name="fade">
      <div v-if="showDeleteConfirm" class="confirm-overlay" @click.self="cancelDelete">
        <div class="confirm-dialog">
          <div class="confirm-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#E63946" stroke-width="2">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
          </div>
          <h3 class="confirm-title">确认删除</h3>
          <p class="confirm-message">确定要删除这个对话吗？删除后将无法恢复。</p>
          <div class="confirm-actions">
            <button class="confirm-btn cancel" @click="cancelDelete">取消</button>
            <button class="confirm-btn delete" @click="confirmDelete" :disabled="isDeleting">
              {{ isDeleting ? '删除中...' : '确认删除' }}
            </button>
          </div>
        </div>
      </div>
    </transition>
    
    <transition name="fade">
      <div v-if="showExchangeDialog" class="confirm-overlay" @click.self="showExchangeDialog = false">
        <div class="confirm-dialog exchange-dialog">
          <div class="confirm-icon">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#C9A227" stroke-width="2">
              <path d="M12 2v20M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6"/>
            </svg>
          </div>
          <h3 class="confirm-title">兑换 AI 对话次数</h3>
          <p class="confirm-message">
            当前阅历: <strong>{{ userStore.experience }}</strong><br>
            兑换消耗: <strong>{{ authStore.EXPERIENCE_COST_PER_AI }}</strong> 阅历/次<br>
            可兑换: <strong>{{ Math.floor(userStore.experience / authStore.EXPERIENCE_COST_PER_AI) }}</strong> 次
          </p>
          <div class="exchange-count-selector">
            <button class="count-btn" @click="exchangeCount = Math.max(1, exchangeCount - 1)">-</button>
            <span class="count-display">{{ exchangeCount }}</span>
            <button class="count-btn" @click="exchangeCount = Math.min(Math.floor(userStore.experience / authStore.EXPERIENCE_COST_PER_AI), exchangeCount + 1)">+</button>
          </div>
          <p class="exchange-cost">消耗: {{ exchangeCount * authStore.EXPERIENCE_COST_PER_AI }} 阅历</p>
          <div class="confirm-actions">
            <button class="confirm-btn cancel" @click="showExchangeDialog = false">取消</button>
            <button class="confirm-btn confirm" @click="confirmExchange" :disabled="isExchanging">
              {{ isExchanging ? '兑换中...' : '确认兑换' }}
            </button>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
/**
 * HomeView - 首页/山河卷视图
 * 
 * 核心功能：
 * 1. 地图展示与景点探索
 * 2. AI 助手对话功能
 * 3. 文物详情展示与收藏
 */

import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/userStore'
import { useAuthStore } from '@/stores/authStore'
import { heritageApi, aiApi } from '@/api'
import { CityMap, AiGuideCard } from '@/components'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const authStore = useAuthStore()

// ==================== 文物详情卡片相关状态 ====================
const showAiCard = ref(false)
const selectedArtifact = ref(null)
const backendSites = ref([])

// ==================== AI 对话面板相关状态 ====================
const showAiPanel = ref(false)
const showSidebar = ref(false)
const aiMessages = ref([])
const aiInput = ref('')
const isAiTyping = ref(false)
const messagesContainer = ref(null)

// ==================== 对话会话管理 ====================
const chatSessions = ref([])
const currentSessionId = ref(null)

// ==================== 删除确认弹窗 ====================
const showDeleteConfirm = ref(false)
const deleteTargetId = ref(null)
const isDeleting = ref(false)

// ==================== 阅历兑换弹窗 ====================
const showExchangeDialog = ref(false)
const exchangeCount = ref(1)
const isExchanging = ref(false)

/**
 * 预设文物节点数据
 * 用于在没有后端数据时展示默认景点
 * 每个节点包含：id、名称、朝代、位置、图片、分类、地点、博物馆、诗词、描述等
 */
const defaultNodes = [
  {
    id: 'qinghuaci',
    name: '青花瓷瓶',
    dynasty: '明朝',
    positionX: 8,
    positionY: 42,
    imageUrl: '/images/qinghuaci.jpg',
    category: '陶瓷艺术',
    location: '景德镇',
    museum: '故宫博物院',
    poem: '素胚勾勒出青花笔锋浓转淡',
    description: '青花瓷是中国陶瓷艺术的瑰宝，以其典雅的蓝白配色闻名于世。'
  },
  {
    id: 'fencaici',
    name: '粉彩瓷瓶',
    dynasty: '清朝',
    positionX: 18,
    positionY: 35,
    imageUrl: '/images/fencaici.jpg',
    category: '陶瓷艺术',
    location: '景德镇',
    museum: '南京博物院',
    poem: '粉黛轻施画中仙',
    description: '粉彩瓷是清代康熙年间创烧的釉上彩新品种，色彩柔和粉润。'
  },
  {
    id: 'qinghuacimianju',
    name: '青花瓷面具',
    dynasty: '明代',
    positionX: 28,
    positionY: 48,
    imageUrl: '/images/qinghuacimianju.jpg',
    category: '陶瓷艺术',
    location: '景德镇',
    museum: '故宫博物院',
    poem: '面具藏真，青花传神',
    description: '青花瓷面具是明代陶瓷艺术的珍品，展现了精湛的青花绘制工艺。',
    special: true,
    icon: '🎭'
  },
  {
    id: 'beijing1',
    name: '故宫博物院',
    dynasty: '明清',
    positionX: 35,
    positionY: 38,
    imageUrl: '/images/gugong-taihedian.jpg',
    category: '皇家建筑',
    location: '北京',
    museum: '故宫博物院',
    poem: '紫禁城中，皇权象征',
    description: '故宫是明清两代的皇家宫殿，是世界上现存规模最大、保存最为完整的木质结构古建筑之一。'
  },
  {
    id: 'beijing2',
    name: '天坛',
    dynasty: '明代',
    positionX: 42,
    positionY: 45,
    imageUrl: '/images/tiantan-qiniandian.jpg',
    category: '皇家建筑',
    location: '北京',
    museum: '天坛公园',
    poem: '祭天祈谷，天人合一',
    description: '天坛是明清两代皇帝祭天祈谷的场所，是中国现存最大的古代祭祀性建筑群。',
    special: true,
    icon: '🏛️'
  },
  {
    id: 'beijing3',
    name: '颐和园',
    dynasty: '清代',
    positionX: 50,
    positionY: 32,
    imageUrl: '/images/yiheyuan-changlang.jpg',
    category: '皇家园林',
    location: '北京',
    museum: '颐和园',
    poem: '山水之间，皇家园林',
    description: '颐和园是中国现存规模最大、保存最完整的皇家园林，被誉为"皇家园林博物馆"。'
  },
  {
    id: 'beijing4',
    name: '长城',
    dynasty: '明代',
    positionX: 58,
    positionY: 40,
    imageUrl: '/images/changcheng-fenghuotai.jpg',
    category: '军事建筑',
    location: '北京',
    museum: '八达岭长城',
    poem: '万里长城，雄关漫道',
    description: '长城是中国古代的军事防御工程，是世界上最伟大的人工建筑之一。',
    special: true,
    icon: '�'
  }
]

/**
 * 计算属性：地图节点数据
 * 为每个节点添加解锁状态和收藏状态
 */
const mapNodes = computed(() => {
  return defaultNodes.map(node => ({
    ...node,
    unlocked: userStore.visitedSites.some(s => s.id === node.id),
    collected: userStore.artifacts.some(a => a.id === node.id)
  }))
})

/**
 * 处理景点探索事件
 * 当用户点击地图上的景点标记时触发
 * @param {Object} site - 景点数据对象
 */
const handleSiteExplore = (site) => {
  selectedArtifact.value = {
    id: site.id,
    name: site.name,
    dynasty: site.dynasty || site.type,
    category: site.category || site.type,
    location: site.address,
    museum: site.name,
    description: site.description,
    imageUrl: site.coverImage || site.image
  }
  showAiCard.value = true
  userStore.visitSite(site)
}

/**
 * 处理节点点击事件
 * 用于预设节点的点击交互
 * @param {Object} node - 节点数据对象
 */
const handleNodeClick = (node) => {
  userStore.visitSite(node)
  selectedArtifact.value = node
  showAiCard.value = true
}

/**
 * 处理节点收藏事件
 * 收藏文物并增加阅历值
 * @param {Object} node - 节点数据对象
 */
const handleNodeCollect = (node) => {
  userStore.collectArtifact(node)
  userStore.addExperience(10)
  userStore.saveToStorage()
}

/** 关闭文物详情卡片 */
const closeAiCard = () => {
  showAiCard.value = false
  selectedArtifact.value = null
}

/** 处理点赞事件 */
const handleLike = ({ artifact, liked }) => {
}

/**
 * 处理分享事件
 * 使用 Web Share API 进行原生分享
 * @param {Object} artifact - 文物数据对象
 */
const handleShare = (artifact) => {
  if (navigator.share) {
    navigator.share({
      title: artifact.name,
      text: `来看看这件${artifact.dynasty}的${artifact.name}`,
      url: window.location.href
    })
  }
}

/**
 * 处理 AR 体验事件
 * 跳转到 AR 鉴赏页面
 * @param {Object} artifact - 文物数据对象
 */
const handleArExperience = (artifact) => {
  router.push({ path: '/ar', query: { id: artifact.id } })
}

/**
 * 处理 AI 讲解完成事件
 * 答对问题后增加阅历值
 * @param {Object} param0 - 包含 artifact 和 correct 的对象
 */
const handleAiComplete = ({ artifact, correct }) => {
  if (correct) {
    userStore.addExperience(5)
    userStore.saveToStorage()
  }
}

// ==================== AI 对话面板功能 ====================

/**
 * 打开 AI 对话面板
 * 检查登录状态，加载历史会话或创建新会话
 */
const openAiPanel = async () => {
  showAiPanel.value = true
  
  if (!authStore.isLoggedIn) {
    aiMessages.value = [
      { text: '您好！我是小游。请先登录后即可与我对话~', fromUser: false }
    ]
    return
  }
  
  await loadChatSessions()
  
  if (chatSessions.value.length > 0) {
    await loadSession(chatSessions.value[0].id)
  } else {
    createNewChat()
  }
}

/**
 * 加载用户的对话会话列表
 * 从后端获取所有会话 ID
 */
const loadChatSessions = async () => {
  if (!authStore.isLoggedIn) return
  const result = await aiApi.getChatIds('chat')
  if (result.success && result.data) {
    chatSessions.value = result.data.map(id => ({
      id,
      title: '对话 ' + id.substring(0, 6),
      updateTime: new Date()
    }))
  }
}

/**
 * 创建新的对话会话
 * 初始化会话 ID 和欢迎消息
 */
const createNewChat = () => {
  currentSessionId.value = 'session_' + Date.now()
  aiMessages.value = [
    { text: '您好！我是文迹书生，有什么关于传统文化的问题可以问我~', fromUser: false }
  ]
  showSidebar.value = false
}

/**
 * 加载指定会话的历史消息
 * @param {string} sessionId - 会话 ID
 */
const loadSession = async (sessionId) => {
  if (!authStore.isLoggedIn) return
  currentSessionId.value = sessionId
  const result = await aiApi.getChatHistory('chat', sessionId)
  if (result.success && result.data && result.data[0]) {
    const sessionData = result.data[0]
    aiMessages.value = sessionData.messages.map(msg => ({
      text: msg.content,
      fromUser: msg.role === 'user'
    }))
    await nextTick()
    scrollToBottom()
  }
  showSidebar.value = false
}

/**
 * 显示删除会话确认弹窗
 * @param {string} sessionId - 要删除的会话 ID
 */
const deleteSession = (sessionId) => {
  if (!authStore.isLoggedIn) return
  deleteTargetId.value = sessionId
  showDeleteConfirm.value = true
}

/** 取消删除操作 */
const cancelDelete = () => {
  showDeleteConfirm.value = false
  deleteTargetId.value = null
}

/**
 * 确认删除会话
 * 调用 API 删除会话，更新本地列表
 */
const confirmDelete = async () => {
  if (!deleteTargetId.value || isDeleting.value) return
  
  isDeleting.value = true
  
  try {
    const result = await aiApi.deleteSession(deleteTargetId.value)
    
    if (result.success) {
      chatSessions.value = chatSessions.value.filter(s => s.id !== deleteTargetId.value)
      
      if (currentSessionId.value === deleteTargetId.value) {
        createNewChat()
      }
    } else {
      console.error('删除失败:', result.message)
    }
  } catch (error) {
    console.error('删除对话失败')
  } finally {
    isDeleting.value = false
    showDeleteConfirm.value = false
    deleteTargetId.value = null
  }
}

const confirmExchange = () => {
  if (isExchanging.value) return
  
  isExchanging.value = true
  
  try {
    const result = authStore.exchangeExperienceForAi(userStore, exchangeCount.value)
    
    if (result.success) {
      ElMessage.success(result.message)
      showExchangeDialog.value = false
      exchangeCount.value = 1
    } else {
      ElMessage.warning(result.message)
    }
  } finally {
    isExchanging.value = false
  }
}

/**
 * 格式化时间显示
 * 将时间戳转换为友好的相对时间格式
 * @param {Date|string} time - 时间对象或时间戳
 * @returns {string} 格式化后的时间字符串
 */
const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  return date.toLocaleDateString()
}

/**
 * 发送 AI 消息
 * 将用户消息发送到后端，使用流式响应接收 AI 回复
 */
const sendAiMessage = async () => {
  if (!aiInput.value.trim() || isAiTyping.value) return
  
  if (!authStore.canUseAi) {
    aiMessages.value.push({ 
      text: `今日AI对话次数已用完（${authStore.aiLimit}次），请明天再来或登录获取更多次数`, 
      fromUser: false 
    })
    authStore.showLoginModal = true
    return
  }
  
  const userMessage = aiInput.value.trim()
  aiMessages.value.push({ text: userMessage, fromUser: true })
  aiInput.value = ''
  isAiTyping.value = true
  
  await nextTick()
  scrollToBottom()
  
  const aiMessageIndex = aiMessages.value.length
  aiMessages.value.push({ text: '', fromUser: false })
  
  try {
    // 调用 AI API，使用流式响应
    const response = await aiApi.chat(userMessage, currentSessionId.value)
    
    if (!response) {
      aiMessages.value[aiMessageIndex].text = '请先登录后再与小游进行沟通~'
      return
    }
    
    if (response.limitExceeded) {
      aiMessages.value[aiMessageIndex].text = response.message
      authStore.showLoginModal = true
      return
    }
    
    if (response.status === 401) {
      aiMessages.value[aiMessageIndex].text = '登录已过期，请重新登录'
      return
    }
    
    // 读取流式响应
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      
      for (const line of lines) {
        if (line.startsWith('data:')) {
          const content = line.slice(5)
          if (content && content !== '[DONE]') {
            aiMessages.value[aiMessageIndex].text += content
            await nextTick()
            scrollToBottom()
          }
        }
      }
    }
    
    if (!aiMessages.value[aiMessageIndex].text.trim()) {
      aiMessages.value[aiMessageIndex].text = '抱歉，我暂时无法回答这个问题。'
    }
  } catch (error) {
    console.error('AI回复失败')
    aiMessages.value[aiMessageIndex].text = '抱歉，我暂时无法回答这个问题，请稍后再试。'
  } finally {
    isAiTyping.value = false
  }
}

/** 滚动消息容器到底部 */
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

/**
 * 组件挂载时的初始化
 * 加载用户存储数据，获取用户信息，获取附近景点
 */
onMounted(() => {
  userStore.loadFromStorage()
  
  if (authStore.isLoggedIn) {
    userStore.fetchUserInfo()
  }
  
  fetchNearbySites()
})

/**
 * 获取附近的遗产景点
 * 使用浏览器定位或默认位置（北京）获取景点数据
 */
const fetchNearbySites = async () => {
  try {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const { longitude, latitude } = position.coords
          const result = await heritageApi.getNearbySites(longitude, latitude)
          if (result.success && result.data) {
            backendSites.value = result.data
          }
        },
        async () => {
          // 定位失败时使用默认位置（北京）
          const result = await heritageApi.getNearbySites(116.4074, 39.9042)
          if (result.success && result.data) {
            backendSites.value = result.data
          }
        }
      )
    } else {
      // 不支持定位时使用默认位置（北京）
      const result = await heritageApi.getNearbySites(116.4074, 39.9042)
      if (result.success && result.data) {
        backendSites.value = result.data
      }
    }
  } catch (error) {
    // 定位失败或接口异常时降级：保留页面默认状态，不阻断渲染，但要留下可追溯的痕迹
    console.warn('[HomeView] 加载附近景点失败:', error?.message ?? error)
  }
}

watch(() => authStore.isLoggedIn, (isLoggedIn) => {
  if (isLoggedIn) {
    userStore.fetchUserInfo()
  }
})
</script>

<style scoped>
.home-view {
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  background: #F5E6D3;
  overflow: hidden;
}

.view-header {
  flex-shrink: 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  padding-top: calc(env(safe-area-inset-top) + 12px);
  background: rgba(245, 230, 211, 0.95);
  z-index: 100;
}

.view-title {
  font-size: 20px;
  color: #2D4059;
  letter-spacing: 0.15em;
  margin: 0;
}

.user-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: rgba(45, 64, 89, 0.06);
  border-radius: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stat-label {
  font-size: 10px;
  color: #8B9A9C;
}

.stat-value {
  font-size: 12px;
  color: #2D4059;
  font-weight: 500;
}

.stat-divider {
  color: #8B9A9C;
  opacity: 0.5;
}

.map-container {
  flex: 1;
  min-height: 0;
  position: relative;
}

.bottom-nav {
  flex-shrink: 0;
  display: flex;
  justify-content: space-around;
  padding: 10px 16px;
  padding-bottom: calc(env(safe-area-inset-bottom) + 10px);
  background: rgba(245, 242, 235, 0.98);
  border-top: 1px solid rgba(45, 64, 89, 0.08);
  z-index: 100;
}

.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  text-decoration: none;
  color: #8B9A9C;
  transition: color 0.3s ease;
}

.nav-item.active {
  color: #2D4059;
}

.nav-icon {
  font-size: 20px;
  font-family: 'Noto Serif SC', serif;
}

.nav-label {
  font-size: 10px;
}

@media (max-width: 428px) {
  .view-header {
    padding: 12px 16px;
  }
  
  .view-title {
    font-size: 20px;
  }
  
  .user-stats {
    padding: 6px 10px;
  }
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: linear-gradient(135deg, #2D4059 0%, #3D5A80 100%);
  color: #F5F2EB;
  border: none;
  border-radius: 16px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.ai-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(45, 64, 89, 0.3);
}

.ai-panel {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #F5F2EB;
  z-index: 1000;
  display: flex;
  flex-direction: column;
}

.ai-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  padding-top: calc(env(safe-area-inset-top) + 16px);
  background: #2D4059;
  color: #F5F2EB;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.menu-btn {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: #F5F2EB;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.menu-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.menu-btn.active {
  background: rgba(255, 255, 255, 0.25);
}

.ai-title {
  font-size: 18px;
  letter-spacing: 0.1em;
}

.close-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: #F5F2EB;
  font-size: 20px;
  cursor: pointer;
}

.ai-panel-body {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.ai-sidebar {
  width: 260px;
  background: #E8ECEF;
  border-right: 1px solid rgba(45, 64, 89, 0.1);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.new-chat-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin: 12px;
  padding: 10px 16px;
  background: #2D4059;
  color: #F5F2EB;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.new-chat-btn:hover {
  background: #3D5A80;
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 0 8px;
}

.chat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  margin-bottom: 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.2s ease;
}

.chat-item:hover {
  background: rgba(45, 64, 89, 0.08);
}

.chat-item.active {
  background: rgba(45, 64, 89, 0.15);
}

.chat-item-info {
  flex: 1;
  min-width: 0;
}

.chat-item-title {
  display: block;
  font-size: 14px;
  color: #2D4059;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-item-time {
  display: block;
  font-size: 11px;
  color: #8B9A9C;
  margin-top: 2px;
}

.delete-btn {
  padding: 4px;
  background: transparent;
  border: none;
  color: #8B9A9C;
  cursor: pointer;
  opacity: 0;
  transition: all 0.2s ease;
}

.chat-item:hover .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  color: #E63946;
}

.empty-tip {
  text-align: center;
  padding: 24px;
  color: #8B9A9C;
  font-size: 13px;
}

.ai-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.ai-panel-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 16px;
  background: #E8ECEF;
  color: #2D4059;
}

.message.from-user {
  align-self: flex-end;
  background: #2D4059;
  color: #F5F2EB;
}

.message-content {
  font-size: 14px;
  line-height: 1.5;
}

.typing-indicator {
  display: flex;
  gap: 4px;
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #8B9A9C;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes typing {
  0%, 60%, 100% { transform: translateY(0); }
  30% { transform: translateY(-4px); }
}

.ai-panel-input {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px 16px;
  padding-bottom: calc(env(safe-area-inset-bottom) + 12px);
  background: #F5F2EB;
  border-top: 1px solid rgba(45, 64, 89, 0.1);
}

.ai-usage-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #8B9A9C;
}

.usage-text strong {
  color: #C9A227;
  font-weight: 600;
}

.extra-count {
  color: #C9A227;
  font-size: 11px;
  margin-left: 4px;
}

.exchange-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: linear-gradient(135deg, #C9A227, #D4AF37);
  color: #fff;
  border: none;
  border-radius: 12px;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.exchange-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(201, 162, 39, 0.4);
}

.input-row {
  display: flex;
  gap: 12px;
}

.ai-panel-input input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid rgba(45, 64, 89, 0.2);
  border-radius: 24px;
  font-size: 14px;
  background: #fff;
  outline: none;
}

.ai-panel-input input:focus {
  border-color: #2D4059;
}

.send-btn {
  padding: 12px 24px;
  background: #2D4059;
  color: #F5F2EB;
  border: none;
  border-radius: 24px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(100%);
}

.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.3s ease;
}

.slide-left-enter-from,
.slide-left-leave-to {
  opacity: 0;
  transform: translateX(-100%);
}

.confirm-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

.confirm-dialog {
  background: #F5F2EB;
  border-radius: 16px;
  padding: 24px;
  max-width: 320px;
  width: 90%;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.confirm-icon {
  margin-bottom: 16px;
}

.confirm-title {
  font-size: 18px;
  color: #2D4059;
  margin: 0 0 8px 0;
}

.confirm-message {
  font-size: 14px;
  color: #8B9A9C;
  margin: 0 0 24px 0;
  line-height: 1.5;
}

.confirm-actions {
  display: flex;
  gap: 12px;
}

.confirm-btn {
  flex: 1;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
}

.confirm-btn.cancel {
  background: #E8ECEF;
  color: #2D4059;
}

.confirm-btn.cancel:hover {
  background: #D8DCE0;
}

.confirm-btn.delete {
  background: #E63946;
  color: #fff;
}

.confirm-btn.delete:hover {
  background: #D62836;
}

.confirm-btn.delete:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.confirm-btn.confirm {
  background: linear-gradient(135deg, #C9A227, #D4AF37);
  color: #fff;
}

.confirm-btn.confirm:hover {
  box-shadow: 0 4px 12px rgba(201, 162, 39, 0.4);
}

.confirm-btn.confirm:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.exchange-dialog .confirm-message {
  text-align: left;
  padding: 12px;
  background: rgba(45, 64, 89, 0.05);
  border-radius: 8px;
}

.exchange-dialog .confirm-message strong {
  color: #C9A227;
}

.exchange-count-selector {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin: 16px 0;
}

.count-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 2px solid #C9A227;
  background: transparent;
  color: #C9A227;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.count-btn:hover {
  background: #C9A227;
  color: #fff;
}

.count-display {
  font-size: 24px;
  font-weight: 600;
  color: #2D4059;
  min-width: 40px;
  text-align: center;
}

.exchange-cost {
  font-size: 14px;
  color: #C9A227;
  margin: 0 0 16px 0;
  font-weight: 500;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
