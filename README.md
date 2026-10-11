# IM Admin - 曹操IM后台管理

> 独立的运营后台。管理员登录、用户与群管理、审计日志放在这里。IM 消息和长连接仍由 im-server 负责。

## 和其他模块的关系

```
flutter-demo ── im-sdk-flutter ──► im-server:8080   消息 / 群 / 好友 / WebSocket
                                        ▲
app-server:8081  ── Feign + X-Internal-Token ──────┘   C 端登录、注册、换 Token
im-admin:8082    ── Feign + X-Internal-Token ──────┘   运营管理
```

| 项目 | 职责 | 端口 | 数据库 |
|------|------|------|--------|
| im-server | IM 核心 | 8080 | `cao_im_db` |
| app-server | C 端认证 | 8081 | `cao_im_app_server` |
| **im-admin** | 运营后台 | 8082 | `cao_im_admin` |

im-admin 不连接 `cao_im_db`，也不把页面放进 im-boot。改用户、改群、查消息，都通过 im-server 的内部接口完成，请求头带 `X-Internal-Token`，和 app-server 调用 im-core 的方式一致。

## 目录

```
im-admin/
├── admin-server/    # Spring Boot 3.2.5 / Java 17，管理员 API
└── admin-web/       # Vue 3 + Vite + Element Plus
```

`admin-server` 依赖 `com.caoim:im-core:1.0.0`，只用其中的实体和调用契约。管理员账号和审计日志存在 `cao_im_admin`，Token 与 C 端用户分开签发。

## 一期

- 管理员登录、退出（密码 BCrypt 存储，独立 JWT）
- 用户分页、搜索
- 禁用 / 启用账号
- 群分页、搜索、解散
- 操作审计（谁在什么时间做了什么）

一期先做单个超级管理员，角色和菜单权限放到后面。

## 二期

- 概览：用户数、群数、当日消息量
- 消息检索（按用户、群、时间，只读）
- 敏感词

## 三期

- 角色、菜单、多管理员
- 系统配置

## im-server 需要补的内部接口

这些接口只接受 `X-Internal-Token`，不给客户端用。实现放在 im-boot，Feign 契约放在 im-core，本仓库只调用。

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/user/page` | 用户分页、按用户名/昵称搜索 |
| PUT | `/admin/user/{id}/enabled` | 禁用或启用账号 |
| GET | `/admin/group/page` | 群分页、搜索 |
| PUT | `/admin/group/{id}/dismiss` | 解散群 |
| GET | `/admin/message/page` | 消息检索（二期） |
| GET | `/admin/stats/overview` | 概览计数（二期） |

`im_user.status` 目前表示在线状态，不能拿来做封禁。账号启停要单独加字段，并在登录、建连时拒绝已禁用用户。

## 技术栈

| 模块 | 技术 |
|------|------|
| admin-server | Java 17、Spring Boot 3.2.5、MyBatis-Plus 3.5.6、MySQL 8、JWT 0.12.5 |
| admin-web | Vue 3、Vite、Element Plus |

## 仓库

- GitHub：https://github.com/cao-im/im-admin
- Gitee：https://gitee.com/cao-im/im-admin
- 默认分支：`master`

## 状态

一期到三期已经落地：管理员登录和权限、用户启停、群解散、消息检索、概览、敏感词、系统配置、多管理员、角色菜单、审计日志。

## 启动

先安装 `im-core`，并保证 MySQL 里有 `cao_im_db` 和 `cao_im_admin`。

```bash
cd im-server
mvn clean install -DskipTests

cd ../im-admin/admin-server
mvn spring-boot:run
```

管理端 API 端口 8082。前端：

```bash
cd im-admin/admin-web
npm install
npm run dev
```

打开 http://localhost:5173 ，初始账号 `admin` / `admin123`。

`im-server` 启动时会补上 `enabled`、敏感词表和系统配置。管理端通过 `X-Internal-Token` 调用 `http://localhost:8080/api`。

## License

MIT License
