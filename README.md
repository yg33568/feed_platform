# 高并发 Feed 流推送平台（仿小红书/微博）

基于 Spring Boot + Redis + RabbitMQ 构建的高并发 Feed 流系统，支持**推拉结合 + 分级推送**，解决大 V 场景下的写扩散性能瓶颈。

## ✨ 核心亮点

- 🚀 **推拉结合**：小V全推，大V只推活跃粉丝，写量降低 **90%**
- 📊 **高性能**：推模式 QPS **1857** / 5ms，拉模式 QPS **886** / 83ms
- 💪 **消息可靠性**：本地消息表 + 生产者确认 + 定时重试 + 死信队列
- 🛡️ **系统防护**：布隆过滤器防穿透 + 用户级限流（10秒5次）
- 🔥 **缓存预热**：启动时自动加载大V收件箱

## 📦 技术栈

| 技术 | 用途 |
|------|------|
| Spring Boot 2.7 | 项目框架 |
| MyBatis-Plus | ORM |
| MySQL 8.0 | 存储用户、文章、关注关系 |
| Redis | 收件箱(ZSet) + 限流 + 布隆过滤器 |
| RabbitMQ | 异步推送 + 死信队列 |
| 雪花算法 | 分布式ID |
| JMeter | 压测验证 |

## 🏗️ 系统架构
发文章 → MySQL → MQ → 消费者 → 查询粉丝 → 判断活跃度 → 写入 Redis ZSet
↓
粉丝刷首页 ← 批量查文章详情 ← Redis ZSet 分页 ← 推/拉模式切换 ← 判断活跃度

## 🚀 核心功能

| 功能 | 说明 |
|------|------|
| 用户关注/取关 | MySQL 存关系，Redis Set 缓存 |
| 发文章 + MQ 推送 | 异步解耦，主流程不阻塞 |
| 大V分级推送 | 粉丝数 ≥ 1 万 → 只推活跃粉丝 |
| Feed 流拉取 | Redis ZSet 分页，按时间倒序 |
| 推拉自动切换 | 活跃用户读收件箱，不活跃用户实时查 MySQL |
| 接口限流 | 用户级限流，10秒5次，返回 429 |
| 布隆过滤器 | 防缓存穿透，拦截不存在的 userId |
| 缓存预热 | 启动时预加载大V收件箱 |

## 📊 压测数据

| 接口 | QPS | 平均响应 | 说明 |
|------|-----|----------|------|
| `/feed` 推模式 | **1857** | **5ms** | 读 Redis 收件箱 |
| `/feed` 拉模式 | 886 | 83ms | 实时查 MySQL |
| `/article/publish` | 474 | 162ms | 写 DB + MQ 确认 |

## 🔁 消息可靠性设计
保存文章 + 消息日志(PENDING)
↓
发送 MQ + ConfirmCallback
↓ ↓
成功 → SUCCESS 失败 → FAILED → 定时重试(最多3次,指数退避)
↓
消费失败 → 死信队列 → 告警

text

## 📁 项目结构
com.feed/
├── controller/ # 控制层
├── service/ # 业务层
├── mq/ # MQ 生产者和消费者
├── mapper/ # 数据访问层
├── entity/ # 实体类
├── config/ # 配置类（Redis、MQ、Redisson）
├── util/ # 工具类（雪花算法、布隆过滤器、限流）
├── task/ # 定时任务（大V更新、消息重试）
├── common/ # 统一响应 + 全局异常
└── startup/ # 启动初始化（布隆过滤器、缓存预热）

text

## 🔧 快速启动

### 1. 环境要求

- JDK 17
- MySQL 8.0
- Redis 6.x
- RabbitMQ 3.x

### 2. 启动 RabbitMQ（WSL Ubuntu）

```bash
wsl -d Ubuntu-24.04
sudo service rabbitmq-server start
3. 修改配置
application.yml 中修改数据库、Redis、RabbitMQ 连接信息。

4. 初始化数据库
执行 docs/schema.sql 中的建表语句。

5. 运行项目
IDEA 中运行 FeedApplication，访问 http://localhost:8080

6. 接口测试
bash
# 关注用户
GET /follow?userId=1&followId=2

# 发文章
GET /article/publish?authorId=2&title=你好&content=测试

# 拉取 Feed 流
GET /feed?userId=1
📈 后续优化方向
用户登录 + JWT 鉴权，主动更新 last_login_time

全局限流 + 令牌桶平滑限流

Redis 收件箱 TTL + 定时清理

粉丝分片 + 并发推送

🙋 常见问题
Q：大V发文时判断每个粉丝是否活跃，不会很慢吗？

A：会，但这是异步操作，且换来了 90% 的 Redis 写入量下降。如需进一步优化，可改用批量查询或活跃标记缓存。

Q：用户长期不登录，收件箱会一直占内存吗？

A：目前没有 TTL，设计上是“活跃用户才写入”。后续可增加定时清理任务或 Redis TTL 兜底。

Q：为什么推模式比拉模式快这么多？

A：推模式把“写扩散”提前到发文时，用户读时只需从 Redis ZSet 按 rank 取数据，复杂度 O(log N)。拉模式需要实时 JOIN 关注表 + 文章表，每次都是复杂查询。

📝 压测结论
✅ 推模式 QPS 1857，响应时间 5ms

✅ 发文章接口在保证消息可靠的前提下，稳定 474 QPS

✅ 限流在 10 秒 5 次时准确触发，无异常

📄 License
MIT

🔗 相关链接
[掘金/CSDN 博客]（https://blog.csdn.net/yg33568）
