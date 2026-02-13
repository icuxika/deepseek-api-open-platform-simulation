# DeepSeek API 开放平台模拟系统

一个模拟 DeepSeek API 开放平台的完整项目，包含用户管理、API Key 管理、计费系统和 AI 模型调用等功能。

## 项目概述

本项目模拟了一个 AI API 开放平台，用户可以：
- 注册账号并登录管理
- 创建和管理 API Keys
- 使用 API Key 调用 AI 模型接口
- 充值账户和查看账单
- 查看用量统计

## 技术栈

### 前端
- **Vue 3** - 渐进式 JavaScript 框架
- **TypeScript** - 类型安全
- **Vite** - 下一代前端构建工具
- **Naive UI** - Vue 3 组件库
- **Pinia** - Vue 状态管理
- **Vue Router** - 路由管理

### 后端
- **Spring Boot 3.2** - Java Web 框架
- **Spring Security** - 安全框架
- **Spring Data JPA** - 数据持久化
- **SQLite** - 嵌入式数据库
- **JWT** - JSON Web Token 认证

## 项目结构

```
deepseek-api-open-platform-simulation/
├── src/                          # 前端源码
│   ├── api/                      # API 请求封装
│   ├── components/               # 公共组件
│   ├── layouts/                  # 布局组件
│   ├── router/                   # 路由配置
│   ├── stores/                   # Pinia 状态管理
│   ├── types/                    # TypeScript 类型定义
│   └── views/                    # 页面组件
│       ├── api-keys/             # API Keys 管理
│       ├── billing/              # 账单
│       ├── dashboard/            # 仪表板
│       ├── docs/                 # 接口文档
│       ├── login/                # 登录
│       ├── pricing/              # 产品定价
│       ├── profile/              # 个人设置
│       ├── recharge/             # 充值
│       └── register/             # 注册
│
├── backend/                      # 后端源码
│   └── src/main/java/com/deepseek/apiplatform/
│       ├── config/               # 配置类
│       ├── controller/           # 控制器
│       ├── dto/                  # 数据传输对象
│       ├── entity/               # 实体类
│       ├── exception/            # 异常处理
│       ├── repository/           # 数据访问层
│       ├── security/             # 安全认证
│       └── service/              # 业务逻辑层
│
└── README.md
```

## 快速开始

### 环境要求
- Node.js 18+
- Java 21+
- Maven 3.8+

### 安装依赖

```bash
# 安装前端依赖
pnpm install

# 安装后端依赖
cd backend
mvn install
```

### 启动项目

```bash
# 启动后端服务 (端口 8080)
cd backend
mvn spring-boot:run

# 启动前端服务 (端口 5173)
pnpm run dev
```

### 访问应用

打开浏览器访问 http://localhost:5173

## API 接口

### 用户认证接口 (`/api/auth/*`)

| 接口 | 方法 | 认证 | 说明 |
|------|------|------|------|
| `/api/auth/register` | POST | 无 | 用户注册 |
| `/api/auth/login` | POST | 无 | 用户登录 |
| `/api/auth/me` | GET | JWT | 获取当前用户信息 |

### API Keys 接口 (`/api/api-keys`)

| 接口 | 方法 | 认证 | 说明 |
|------|------|------|------|
| `/api/api-keys` | GET | JWT | 获取 API Keys 列表 |
| `/api/api-keys` | POST | JWT | 创建 API Key |
| `/api/api-keys/{id}` | DELETE | JWT | 删除 API Key |

### 计费接口 (`/api/billing/*`)

| 接口 | 方法 | 认证 | 说明 |
|------|------|------|------|
| `/api/billing/usage` | GET | JWT | 获取用量统计 |
| `/api/billing/records` | GET | JWT | 获取账单记录 |
| `/api/billing/recharge` | POST | JWT | 充值 |

### AI 模型接口 (`/v1/*`)

| 接口 | 方法 | 认证 | 说明 |
|------|------|------|------|
| `/v1/models` | GET | 无 | 获取可用模型列表 |
| `/v1/chat/completions` | POST | API Key | 聊天补全 |

## 认证机制

本项目使用两种认证方式，分别用于不同的场景：

### JWT Token 认证

- **用途**: 用户登录后访问用户管理接口 (`/api/*`)
- **格式**: 标准 JWT 格式 (如 `eyJhbGciOiJIUzI1NiJ9...`)
- **获取**: 登录或注册成功后返回
- **有效期**: 24 小时

### API Key 认证

- **用途**: 调用 AI 模型接口 (`/v1/*`)
- **格式**: `sk-` 开头的字符串 (如 `sk-xxxxxxxxxxxxxxxxxxxxxxxx`)
- **获取**: 在控制台创建
- **有效期**: 永久有效，直到手动删除

### 权限对照表

| 接口路径 | API Key (sk-xxx) | JWT Token |
|----------|------------------|-----------|
| `/v1/chat/completions` | ✅ 允许 | ❌ 拒绝 |
| `/v1/models` | ✅ 允许 | ✅ 允许 |
| `/api/auth/me` | ❌ 拒绝 | ✅ 允许 |
| `/api/api-keys` | ❌ 拒绝 | ✅ 允许 |
| `/api/billing/*` | ❌ 拒绝 | ✅ 允许 |

## 使用示例

### 调用 Chat Completion API

```bash
curl -X POST http://localhost:8080/v1/chat/completions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer sk-your-api-key" \
  -d '{
    "model": "deepseek-chat",
    "messages": [
      {"role": "user", "content": "Hello, how are you?"}
    ]
  }'
```

### 响应示例

```json
{
  "id": "chatcmpl-xxxxxxxx",
  "object": "chat.completion",
  "created": 1234567890,
  "model": "deepseek-chat",
  "choices": [{
    "index": 0,
    "message": {
      "role": "assistant",
      "content": "Hello! I'm DeepSeek AI assistant. How can I help you today?"
    },
    "finishReason": "stop"
  }],
  "usage": {
    "promptTokens": 14,
    "completionTokens": 15,
    "totalTokens": 29
  }
}
```

### Python SDK 示例

```python
import openai

client = openai.OpenAI(
    api_key="sk-your-api-key",
    base_url="http://localhost:8080/v1"
)

response = client.chat.completions.create(
    model="deepseek-chat",
    messages=[
        {"role": "user", "content": "Hello!"}
    ]
)

print(response.choices[0].message.content)
```

## 可用模型

| 模型名称 | 说明 | 输入价格 | 输出价格 |
|----------|------|----------|----------|
| deepseek-chat | 通用对话模型 | ¥0.001/1K tokens | ¥0.002/1K tokens |
| deepseek-coder | 代码生成模型 | ¥0.0015/1K tokens | ¥0.003/1K tokens |
| deepseek-reasoner | 推理增强模型 | ¥0.002/1K tokens | ¥0.004/1K tokens |

## 测试

### 运行后端测试

```bash
cd backend
mvn test
```

### 测试覆盖

- **单元测试**: Service 层业务逻辑测试
- **集成测试**: Controller 层 API 测试

测试文件位于 `backend/src/test/java/com/deepseek/apiplatform/`

## 开发命令

```bash
# 前端开发
pnpm run dev          # 启动开发服务器
pnpm run build        # 构建生产版本
pnpm run preview      # 预览生产构建
pnpm run type-check   # TypeScript 类型检查

# 后端开发
cd backend
mvn spring-boot:run   # 启动开发服务器
mvn test              # 运行测试
mvn package           # 打包
```

## 项目特性

- ✅ 用户注册/登录
- ✅ JWT Token 认证
- ✅ API Key 管理
- ✅ API Key 认证
- ✅ Chat Completion API
- ✅ 用量统计
- ✅ 账户充值
- ✅ 账单记录
- ✅ 接口文档
- ✅ 产品定价
- ✅ 单元测试和集成测试
- ✅ CORS 跨域支持

## 许可证

MIT License
