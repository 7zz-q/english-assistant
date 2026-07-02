# English Assistant Server (Spring Boot)

> 从 Node.js + Express + SQLite 迁移到 Spring Boot 3 + MySQL + MyBatis-Plus + Redis

## 技术栈

| 组件 | 技术 |
|------|------|
| 框架 | Spring Boot 3.3.x (Java 21) |
| 数据库 | MySQL 8.0 |
| ORM | MyBatis-Plus 3.5.x |
| 缓存 | Redis |
| 认证 | Spring Security 6 + JWT (jjwt 0.12.x) |
| API 文档 | Knife4j / Swagger |
| 测试 | JUnit 5 + H2 |

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- MySQL 8.0+
- Redis 7.0+

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS english_assistant
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;
```

### 2. 配置环境变量

```bash
# 阿里云 DashScope API Key（通义千问）
export DASHSCOPE_API_KEY=sk-xxxxxxxx

# JWT 密钥（生产环境请更换为强随机字符串）
export JWT_SECRET=english-assistant-jwt-secret-2024

# 数据库
export DB_USERNAME=root
export DB_PASSWORD=yourpassword

# Redis（可选，默认 localhost:6379）
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

### 3. 编译运行

```bash
# 开发模式
mvn spring-boot:run

# 生产模式
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# 打包
mvn clean package -DskipTests
java -jar target/english-assistant-server-1.0.0.jar
```

服务启动在 `http://localhost:3000`，首次启动会自动建表。

### 4. API 文档

开发环境下访问：`http://localhost:3000/doc.html`

### 5. 数据迁移

原始 SQLite 数据在 `../english-assistant/server/data.db`，可通过以下方式迁移：

```bash
# 方案 A: 导出 SQLite 为 SQL 文件后导入 MySQL
sqlite3 ../english-assistant/server/data.db .dump > data.sql
# 手动编辑 data.sql 适配 MySQL 语法后：
mysql -u root -p english_assistant < data.sql

# 方案 B: 使用 Node.js 脚本逐表迁移
node migrate.js
```

## 项目结构

```
src/main/java/com/englishassistant/
├── EnglishAssistantApplication.java    # 启动类
├── config/                             # Spring 配置
│   ├── SecurityConfig.java             # Security + JWT 无状态
│   ├── JwtAuthenticationFilter.java    # JWT 拦截器
│   ├── CorsConfig.java                 # 跨域
│   └── MyBatisPlusConfig.java          # 分页插件
├── common/                             # 公共类
│   ├── Result.java                     # 统一响应
│   ├── BusinessException.java          # 业务异常
│   └── GlobalExceptionHandler.java     # 全局异常处理
├── entity/                             # 数据实体（8个）
├── dto/request/                        # 请求 DTO
├── mapper/                             # MyBatis Mapper（8个）
├── service/                            # 业务接口（9个）
│   └── impl/                           # 业务实现
├── controller/                         # REST 控制器（9个）
└── util/
    ├── JwtUtil.java                    # JWT 工具
    └── EbbinghausUtil.java             # 艾宾浩斯算法
```

## API 端点 (~25个)

详见 `controller/` 目录，完全兼容原 Node.js 版本。
前端无需任何修改，Vue 3 配置指向本服务即可。

## 与原版对比

| 维度 | Node.js 原版 | Spring Boot 新版 |
|------|-------------|-----------------|
| 框架 | Express 4 | Spring Boot 3.3 |
| 数据库 | SQLite | MySQL 8.0 |
| 缓存 | SQLite 表 | Redis + MySQL 冷备 |
| AI 调用 | OpenAI SDK | RestTemplate |
| API 文档 | 无 | Knife4j/Swagger |
| 测试 | 无 | JUnit 5 |
| 性能 | 单线程 | 多线程连接池 |
