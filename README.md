<div align="center">

# 文迹 (WenJi) - 文化遗产探索App


![Version](https://img.shields.io/badge/version-1.0.0--beta-blue)
![Vue](https://img.shields.io/badge/Vue-3.5-brightgreen)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen)
![JDK](https://img.shields.io/badge/JDK-21-orange)
![LangChain4j](https://img.shields.io/badge/LangChain4j-1.0.1--beta6-orange)
![License](https://img.shields.io/badge/license-MIT-green)

**基于地理位置的传统文化探索应用 · AI 智能导游「智游」**

[功能特性](#功能特性) • [快速开始](#快速开始) • [API 接口](#api-接口) • [配置说明](#配置说明) • [技术架构](#技术架构) • [AI 能力](#ai-能力说明)

</div>

---

## 项目简介

文迹 App 是一款融合 **AR 增强现实**、**LangChain4j AI 智能讲解**、**RAG 知识库检索**、**地理定位** 技术的文化遗产探索应用。用户可通过手机探索周边文化地标，体验 AR 文物展示，与 AI 导游「智游」进行智能对话，在行走中感受传统文化的魅力。

### 核心亮点

| 亮点 | 描述 |
|------|------|
| 🗺️ **文化地图点亮** | 到访文化地标即可点亮地图，形成个人文化足迹 |
| 📱 **WebAR 展示** | 无需下载 App，浏览器即可体验 AR 文物展示 |
| 🤖 **AI 导游「智游」** | LangChain4j + 通义千问驱动的个性化文化讲解 |
| 📚 **RAG 知识库** | 向量数据库存储非遗知识，精准检索增强回答 |
| 🎮 **游戏化激励** | 经验值、徽章、等级体系，提升用户粘性 |

---

## 功能特性

| 模块 | 功能 | 描述 |
|------|------|------|
| 🗺️ 地图探索 | 周边景点 / 地图点亮 | 基于 GPS 定位展示周边文化遗产，进入范围自动点亮 |
| 📱 AR 体验 | 文物识别 / 3D 模型 | 扫描文物图片触发 AR 展示，Three.js 渲染 3D 模型 |
| 🤖 AI 对话 | 智能对话 / 流式输出 / 多模态 | SSE 流式响应，支持文本+图片多模态输入 |
| 📚 RAG 检索 | 知识库问答 / 文档入库 | Redis 向量存储非遗知识，语义检索增强回答 |
| 👤 用户系统 | 登录注册 / 个人中心 | 手机号验证码登录，经验值/等级/徽章管理 |
| 📝 游记系统 | 发布游记 / 幂等防重 | 记录文化探索心得，唯一索引 + 原子更新保证积分不重不漏 |

---

## 快速开始

### 环境要求

| 软件 | 版本要求 |
|------|---------|
| JDK | 21+ |
| MySQL | 8.0+ |
| Redis | 7.x（需支持向量搜索） |
| Node.js | 20.0+（前端开发） |

### 方式一：本地开发

#### 1. 启动基础设施

```bash
# 启动 MySQL 和 Redis（确保端口 3306、6379 可用）
```

#### 2. 配置环境变量

```bash
# 设置通义千问 API Key（必需，否则 AI 功能不可用）
export QWEN_API_KEY=sk-xxxxxxxxxxxxxxxx
```

或直接修改 `backend/src/main/resources/application.yml`：

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: sk-你的密钥
    streaming-chat-model:
      api-key: sk-你的密钥
    embedding-model:
      api-key: sk-你的密钥
```

#### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
# 或 IDE 直接运行 WenjiApplication.java
```

后端启动成功后访问：http://localhost:8080/doc.html （Knife4j API 文档）

#### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端地址：http://localhost:5173

---

## API 接口

> 除标注「公开」外，所有接口需在请求头携带 `Authorization: Bearer <token>`。

### 用户模块 `/user`

| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| GET | `/user/captcha` | 获取图形验证码 | 公开 |
| POST | `/user/login` | 登录（用户不存在时自动注册） | 公开 |
| POST | `/user/logout` | 退出登录（清除 Redis 白名单） | 必需 |
| GET | `/user/currentUserInfo` | 获取当前用户信息 | 必需 |
| PUT | `/user/updateUserInfo` | 更新用户信息 | 必需 |
| DELETE | `/user/deleteUserInfo` | 注销账户 | 必需 |

### AI 对话 `/user/ai`

| 方法 | 路径 | 说明 | 特性 |
|------|------|------|------|
| POST | `/user/ai/chat` | AI 对话 | SSE 流式、RAG 增强、多模态 |

**参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| prompt | String | ✅ | 用户问题 |
| chatId | String | ❌ | 会话 ID（不传则新建） |
| images | MultipartFile[] | ❌ | 图片文件（传图时走多模态分支） |

**响应：`text/event-stream`，每个分片是一个纯文本片段**

```
data: 景泰蓝又称
data: 铜胎掐丝珐琅
data: ，制作需经六道工序
```

### 会话历史 `/ai/history`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/ai/history/{type}/list` | 会话列表（type 目前为 `chat`） |
| GET | `/ai/history/{type}/{sessionId}` | 指定会话的消息详情 |
| DELETE | `/ai/history/session/{sessionId}` | 删除会话（Redis + MySQL 双删） |

### 景点地图 `/map`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/map/initial?lng={经度}&lat={纬度}` | 获取附近景点 |
| GET | `/map/{id}` | 景点详情 |

### 游记 `/travel`

| 方法 | 路径 | 说明 | 特性 |
|------|------|------|------|
| POST | `/travel/publish` | 发布游记 | 唯一索引防重 + 原子积分更新 |
| GET | `/travel/myBlogs` | 我的游记列表 | — |
| DELETE | `/travel/delete/{blogId}` | 删除游记 | 校验归属 |

### 徽章 `/badge`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/badge/myBadges` | 当前用户已获得的徽章 |

### RAG 知识库 `/admin/rag`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/admin/rag/ingest` | 单条知识入库 |
| POST | `/admin/rag/batch-ingest` | 批量导入示例知识 |
| POST | `/admin/rag/import-from-file` | 从文件导入（未实现） |

### 文件 `/common`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/common/upload` | 文件上传 |

---

## 配置说明

### 后端主配置 `application.yml`

```yaml
spring:
  data:
    redis:
      host: localhost        # Redis 地址
      port: 6379             # Redis 端口
      password: ""           # Redis 密码
      database: 3            # 业务缓存 DB
  datasource:
    url: jdbc:mysql://localhost:3306/wenji?...
    username: root
    password: 123456

langchain4j:
  # ========== AI 聊天模型 ==========
  open-ai:
    chat-model:
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: ${QWEN_API_KEY}     # 通义千问 API Key
      model-name: qwen-plus         # 模型名称
      temperature: 0.8              # 温度参数
      max-tokens: 200               # 最大 token 数
    streaming-chat-model:           # 流式模型（同上配置）
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: ${QWEN_API_KEY}
      model-name: qwen-plus
      temperature: 0.8
    embedding-model:                 # 向量嵌入模型
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
      api-key: ${QWEN_API_KEY}
      model-name: text-embedding-v3
      dimension: 1024                # 向量维度

  # ========== Redis 向量存储 ==========
  community:
    redis:
      host: localhost
      port: 6379
      password: ""
      database: 4                    # 向量 DB（与业务隔离）
```

### 配置项速查

| 变量 | 必填 | 默认值 | 说明 |
|------|------|--------|------|
| `QWEN_API_KEY` | ✅ | - | 阿里云通义千问 API Key |
| `spring.data.redis.host` | ✅ | localhost | Redis 地址 |
| `spring.datasource.password` | ✅ | 123456 | 数据库密码 |

---

## 项目结构

```
WenJi/
├── backend/
│   ├── src/main/java/com/example/
│   │   ├── WenjiApplication.java            # 启动类（@MapperScan com.example.mapper）
│   │   ├── common/
│   │   │   ├── CommonConfiguration.java     # LangChain4j Bean 装配（记忆/检索器）
│   │   │   ├── Result.java · PageResult.java
│   │   │   ├── constants/RedisConstants.java    # Redis Key、TTL、相似度阈值
│   │   │   └── utils/                       # JWT / MD5 / 验证码 / ThreadLocal / 向量工具
│   │   ├── config/                          # Web、MyBatis、定时任务、等级、Swagger
│   │   ├── controller/                      # 9 个 Controller
│   │   │   ├── ChatController.java          # AI 对话（SSE）
│   │   │   ├── ChatHistoryController.java   # 会话历史
│   │   │   ├── RagKnowledgeController.java  # 知识库入库
│   │   │   ├── UserController.java · CaptchaController.java
│   │   │   ├── HeritageController.java      # 景点地图
│   │   │   ├── TravelBlogController.java    # 游记
│   │   │   ├── BadgeController.java
│   │   │   └── CommonController.java        # 文件上传
│   │   ├── exception/                       # BusinessException + 全局异常处理
│   │   ├── interceptor/                     # RefreshToken（认证+续期）、Login（准入）
│   │   ├── mapper/                          # MyBatis-Plus Mapper 接口
│   │   ├── repository/
│   │   │   ├── RedisChatMemoryStore.java    # ChatMemoryStore 实现（List 结构）
│   │   │   ├── RedisChatMemory.java         # 归档侧读取实现
│   │   │   └── impl/DatabaseChatHistoryRepository.java
│   │   ├── service/
│   │   │   ├── aiservice/                   # ⭐ AI 服务层（核心）
│   │   │   │   ├── ConsultantService.java       # @AiService 声明式接口
│   │   │   │   ├── RagChatService.java          # 基础检索聊天
│   │   │   │   └── RagEnhancedChatService.java  # 增强版（相似度分级 + 三层降级）
│   │   │   └── impl/                        # 业务实现（User/TravelBlog/Point/Badge...）
│   │   └── po/
│   │       ├── dto/                         # 入参对象
│   │       ├── pojo/                        # 数据库实体
│   │       └── vo/                          # 出参对象
│   │
│   ├── src/main/resources/
│   │   ├── application.yml                  # 主配置
│   │   ├── application-test.yml
│   │   ├── System.txt                       # AI 导游人设提示词
│   │   ├── com/example/mapper/*.xml         # MyBatis XML（namespace 需与接口全限定名一致）
│   │   └── db/                              # 建表与初始化脚本
│   │
│   └── Dockerfile                           # eclipse-temurin:21-jre-alpine
│
├── frontend/src/
│   ├── views/                               # HomeView / ArExperience / Profile /
│   │                                        # Reward / TargetCompiler / WelcomeScreen
│   ├── api/                                 # 请求封装
│   ├── components/                          # atoms / molecules / organisms
│   ├── stores/                              # Pinia
│   └── router/
│
├── .github/workflows/                       # CI：后端 Maven 构建 + 前端 Vite 构建
├── docs/                                    # 设计与复盘文档
├── docker-compose.yml                       # MySQL + Redis + Backend + Frontend
└── CLAUDE.md
```

---

## 技术架构

### 技术栈

| 层级 | 技术 | 版本 | 用途 |
|------|------|------|------|
| **前端框架** | Vue 3 + Vite | 3.5 / 7 | SPA 应用 |
| **状态管理** | Pinia | 3.x | 全局状态 |
| **UI 组件** | Element Plus + Tailwind CSS | - | 界面组件 |
| **地图服务** | 高德地图 API + Leaflet | - | 地理定位与展示 |
| **AR 引擎** | MindAR.js + Three.js | - | WebAR 3D 展示 |
| **后端框架** | Spring Boot | 3.3.5 | RESTful API |
| **运行时** | OpenJDK | 21 (LTS) | 编译与运行版本 |
| **AI 引擎** | **LangChain4j** | **1.0.1-beta6** | LLM 集成、RAG、声明式 AI Service |
| **LLM 模型** | 阿里云通义千问 | qwen-plus / text-embedding-v3 | 对话 + 向量嵌入 |
| **ORM** | MyBatis-Plus | 3.5.7 | 数据库操作 |
| **数据库** | MySQL | 8.0 | 业务数据存储 |
| **缓存** | Redis | 7.x | 缓存 + 向量存储 + 聊天记忆 |
| **认证** | JWT (jjwt) | 0.11.5 | 无状态认证 |
| **响应式** | Project Reactor | - | SSE 流式响应 (Flux\<String\>) |
| **文档** | Knife4j (OpenAPI 3) | 4.5.0 | API 文档 |

### 系统架构图

```
┌─────────────────────────────────────────────────────────────┐
│                      用户层 (Client)                         │
│               H5 / PWA / 浏览器直接访问                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                     前端 (Vue 3 + Vite)                      │
│   HomeView │ ArView │ Profile │ Reward │ WelcomeScreen      │
└─────────────────────────────────────────────────────────────┘
                              │ HTTP / SSE
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                  后端 (Spring Boot 3.3.5)                    │
│                                                             │
│  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │ Controller  │→ │   Service    │→ │    Repository     │   │
│  │   (REST)    │  │   (业务逻辑) │  │  (数据持久化)     │   │
│  └─────────────┘  └──────────────┘  └──────────────────┘   │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              LangChain4j AI 引擎层                   │   │
│  │                                                     │   │
│  │  ConsultantService (@AiService)                     │   │
│  │    ├─ chat()           同步对话                     │   │
│  │    ├─ chatStream()     Flux<String> 流式对话        │   │
│  │    └─ chatWithImage()  多模态(图文)对话             │   │
│  │                                                     │   │
│  │  RagEnhancedChatService                            │   │
│  │    ├─ 向量化用户问题                                │   │
│  │    ├─ Redis EmbeddingStore 语义检索                 │   │
│  │    ├─ 组装增强 Prompt                               │   │
│  │    └─ 降级机制: 检索失败 → 普通 LLM 回答            │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
  ┌──────────┐         ┌──────────┐          ┌──────────────┐
  │  MySQL   │         │  Redis   │          │  阿里云千问   │
  │   8.0    │         │    7     │          │  (qwen-plus)  │
  │          │         │          │          │              │
  │ 业务数据  │         │ DB3: 缓存 │          │  LLM 对话    │
  │ 会话/消息 │         │ DB4: 向量 │          │  Embedding   │
  │ 用户/游记 │         │ 聊天记忆  │          │              │
  └──────────┘         └──────────┘          └──────────────┘
```

---

## AI 能力说明

### 1. AI 导游「智游」

基于 **LangChain4j @AiService** 声明式接口实现，角色设定为活泼亲切的导游：

- **同步对话**: `ConsultantService.chat(message)` → 返回完整字符串
- **流式对话**: `ConsultantService.chatStream(chatId, message)` → 返回 `Flux<String>` (SSE)
- **多模态**: `ConsultantService.chatWithImage(chatId, contents)` → 支持图片输入

### 2. RAG 知识库检索增强

```
用户提问 "景泰蓝是什么？"
       ↓
  向量嵌入 (text-embedding-v3 → 1024维)
       ↓
  Redis EmbeddingStore 相似度搜索 (Top-5, minScore=0.6)
       ↓
  召回相关非遗知识片段
       ↓
  组装增强 Prompt (参考资料 + 用户问题)
       ↓
  通义千问生成回答 (Flux<String> 流式输出)
       ↓
 降级兜底: 检索无结果时自动退化为普通 LLM 回答
```

### 3. 聊天记忆管理

| 存储 | 用途 | 实现 |
|------|------|------|
| **Redis (DB3)** | 实时对话上下文 | `RedisChatMemoryStore` (ChatMemoryStore 接口) |
| **MySQL** | 持久化归档 | 定时任务每 5 分钟归档活跃会话 |
| **消息窗口** | 上下文长度控制 | `MessageWindowChatMemory(maxMessages=20)` |

### 4. 多线程能力

| 技术 | 应用场景 | 所在文件 |
|------|----------|----------|
| **ThreadLocal** | 请求级用户上下文传递 | `ThreadLocalUtil.java` |
| **线程池** | 定时任务独立执行 | `ScheduleConfig.java` (5线程守护线程池) |
| **CountDownLatch** | 并发测试协调 | `TravelBlogTest.java` |
| **AtomicInteger** | 并发计数统计 | `TravelBlogTest.java` |
| **Reactor Flux** | AI 流式响应 (SSE) | `ChatController.java` |

---

## 常见问题

### Q: 启动报错 Redis host 为空？

检查 `application.yml` 中 `langchain4j.community.redis` 的配置格式：

```yaml
# ✅ 正确
langchain4j:
  community:
    redis:
      host: localhost
      port: 6379

# ❌ 错误（多了 embedding-store 一层）
langchain4j:
  community:
    redis:
      embedding-store:    # ← 这层多余
        host: localhost
```

### Q: AI 对话返回 401？

获取验证码等公开接口被登录拦截器拦截了。检查 `WebConfig.java` 中拦截器的 `excludePathPatterns` 是否包含该路径。

### Q: 如何申请通义千问 API Key？

1. 登录 [阿里云百炼平台](https://bailian.console.aliyun.com/)
2. 开通通义千问服务
3. 创建 API Key
4. 设置环境变量 `QWEN_API_KEY=sk-xxx`

### Q: RAG 知识库如何使用？

```bash
# 1. 先批量导入示例知识
curl -X POST http://localhost:8080/admin/rag/batch-ingest

# 2. 单条入库
curl -X POST "http://localhost:8080/admin/rag/ingest?content=xxx&title=xxx&source=xxx"
```

---

## 版本信息

| 项目 | 信息 |
|------|------|
| 版本号 | v1.0.0-beta |
| 运行时 | OpenJDK 21 (LTS) |
| AI 框架 | Spring AI 1.0.0-M6 → **LangChain4j 1.0.1-beta6** |
| LLM | 阿里云通义千问 (qwen-plus) |
| 更新日期 | 2026-10-26 |

---

<div align="center">

**文迹 - 让传统文化触手可及 · AI 导游「智游」伴你同行**

</div>
