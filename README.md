# 光谷启航 · 新生一站式服务平台

面向高校迎新季的一站式服务平台，覆盖新生从 **录取到报到** 全流程：信息填报、费用查询、在线选宿、分班管理、迎新任务、报到扫码签到、公告/手册/校园地图/FAQ 内容服务、新生群管理，以及志愿者的钥匙与军训服装发放等。

平台采用 **前后端分离** 架构，一套后端同时支撑三类 Web 端（管理员 / 辅导员 / 学生）和一套移动端（学生 / 志愿者 H5）。

---

## 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [接口文档](#接口文档)
- [角色与路由](#角色与路由)
- [数据库表概览](#数据库表概览)
- [常见问题](#常见问题)

---

## 功能特性

### 管理员端（`fronted` → `/admin`）

| 模块 | 说明 |
| --- | --- |
| 工作台 | 迎新数据概览、关键指标统计 |
| 数据大屏 | 大屏可视化展示（`/admin/dashboard-screen`） |
| 组织架构管理 | 学院、专业、班级的增删改查，班级分配 |
| 人员管理 | 辅导员管理、志愿者管理、管理员管理、**新生数据 Excel 批量导入/导出** |
| 费用配置 | 费用项目、按专业的收费标准配置 |
| 宿舍管理 | 楼栋/房间数据、按专业分配宿舍、选房时间段控制 |
| 内容管理 | 迎新手册（富文本）、校园地图、公告通知 |
| 新生群管理 | 迎新 QQ/微信群二维码的统一配置与分发 |
| FAQ 知识库 | 问题分类 + 知识条目维护，支持批量导入 |

> `views/admin/ProcessConfig.vue`（报到流程节点配置，对应 `/api/process`）已开发完成，但目前尚未挂载到侧边菜单，可按需在 `AdminLayout.vue` 中加入菜单项。

### 辅导员端（`fronted` → `/counselor`）

- **我的班级**：所带班级信息与学生名单
- **我的学生**：学生详细档案、报到状态跟踪
- **扫码签到**：扫码核验新生报到（配合 `stu_report_code` 报到码）
- **群管理**：所带班级新生群维护
- 个人中心

### 学生端（`fronted` → `/student-web`，移动端 `app` → `/student`）

- **首页 / 迎新任务**：信息填报、学费查看、宿舍选择等任务清单与完成状态
- **扫码报到**：出示报到二维码 / 扫码报到
- **信息填报**：个人基础信息、联系方式、家庭资料、军训服装尺码等
- **学费查看**：费用明细与缴费状态
- **宿舍选择**：在开放时间段内在线选宿舍、选床位
- **发现**：迎新手册、校园地图、新生群、FAQ 知识库、常见问题
- **个人中心**：个人信息、修改密码

### 志愿者端（`app` → `/volunteer`）

- **钥匙发放**：扫码发放宿舍钥匙，实时统计发放进度
- **军训服装发放**：扫码发放服装，统计发放进度

### 平台通用能力

- JWT 登录鉴权（`JwtInterceptor` 拦截 `/api/**`，登录/注册等接口放行）
- 角色权限路由守卫（按 `userType` 重定向到对应端）
- S3 对象存储（图片 / Excel 上传下载，`S3Service`）
- EasyExcel 批量导入导出（新生、辅导员、班级、费用、宿舍分配、FAQ 等）
- 统一响应结构 `Result` / `PageResult` / `ImportResult`
- SpringDoc + Knife4j 接口文档
- 跨域配置（CORS 全开放）

---

## 技术栈

### 后端 `backend/`

| 技术 | 版本 / 说明 |
| --- | --- |
| Java | 21 |
| Spring Boot | 3.5.14（`spring-boot-starter-web`） |
| MyBatis | mybatis-spring-boot-starter 3.0.5 |
| MySQL | MySQL 8.x（`mysql-connector-j`） |
| JWT | jjwt 0.12.6 |
| 密码加密 | spring-security-crypto |
| Excel | EasyExcel 4.0.3 |
| 对象存储 | AWS SDK S3 2.28.6 |
| 接口文档 | springdoc-openapi 2.8.5 + Knife4j |
| 构建 | Maven Wrapper（`mvnw`） |

### 前端 `fronted/`（Web）与 `app/`（移动端 H5）

| 技术 | 说明 |
| --- | --- |
| Vue 3 | `<script setup>` + TypeScript |
| Vite 8 | 开发服务器 + 构建，`/api` 代理到 `http://localhost:8080` |
| Vue Router 5 | history 模式，全局路由守卫做角色校验 |
| Pinia | 用户态（token / userInfo）持久化到 `localStorage` |
| Axios | 统一封装 `src/utils/request.ts`，自动附加 `Authorization: Bearer` |
| ECharts（仅 `fronted`） | 数据大屏与统计图表 |
| html5-qrcode / qrcode | 扫码与二维码生成 |

---

## 项目结构

```
新生一站式服务平台/
├── backend/                  # Spring Boot 后端（主工程）
│   └── src/main/java/com/example/backend/
│       ├── controller/       # 14 个 Controller，统一 /api/** 前缀
│       ├── service/          # 业务接口 + impl 实现
│       ├── mapper/           # MyBatis Mapper 接口
│       ├── entity/           # 数据库实体 + 导入导出行对象
│       ├── config/           # CORS / S3 / Swagger / WebMvc 配置
│       ├── interceptor/      # JWT 拦截器
│       ├── common/           # Result / PageResult / ImportResult
│       └── util/             # JwtUtil / PasswordUtil
│
├── fronted/                  # Web 端（管理员 / 辅导员 / 学生 Web 版）
│   └── src/
│       ├── api/              # 按模块拆分的接口封装
│       ├── views/admin/      # 管理员端页面
│       ├── views/counselor/  # 辅导员端页面
│       ├── views/student/    # 学生端页面（含 Web 版 *Web*.vue）
│       ├── views/volunteer/  # 志愿者发放页面
│       ├── router/           # 路由 + 角色守卫
│       ├── stores/           # Pinia（user / counter）
│       └── utils/            # axios 封装、省市区数据
│
├── app/                      # 移动端 H5（学生 / 辅导员 / 志愿者手机端）
│   └── src/                  # 结构同 fronted，页面为移动端布局
│
├── demo/                     # 独立实验工程（JPA + Spring Security）
│   └── .../service/PasswordMigrationService.java   # 密码迁移方案验证
│
├── freshman_service.sql      # 数据库建库建表脚本（Navicat 导出）
└── README.md
```

> 说明：`fronted/` 目录名沿用了历史拼写（fronted = frontend），请勿随意改名以免影响已有配置。

---

## 环境要求

| 依赖 | 版本 |
| --- | --- |
| JDK | 21+ |
| Maven | 使用工程内 `mvnw` 即可（或本机 Maven 3.8+） |
| MySQL | 8.x（脚本导出自 8.4.9） |
| Node.js | `^20.19.0` 或 `>=22.12.0` |
| npm | 随 Node 安装即可 |

---

## 快速开始

### 1. 初始化数据库

创建数据库并导入表结构：

```bash
mysql -u root -p -e "CREATE DATABASE freshman_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p freshman_service < freshman_service.sql
```

也可以用 Navicat 等工具直接运行 `freshman_service.sql`。

### 2. 修改后端配置

编辑 `backend/src/main/resources/application.properties`，按本地环境修改：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/freshman_service?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=******          # 数据库密码

s3.endpoint=${S3_ENDPOINT:...}                  # 对象存储，密钥通过环境变量注入
s3.access-key=${S3_ACCESS_KEY:...}
s3.secret-key=${S3_SECRET_KEY:...}
s3.bucket=${S3_BUCKET:...}

jwt.secret=******                          # 建议更换为自己的 256 位密钥
jwt.expiration=86400000
```

### 3. 启动后端（端口 8080）

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

### 4. 启动 Web 前端（端口 5173）

```bash
cd fronted
npm install
npm run dev
```

浏览器访问 <http://localhost:5173>，Vite 已将 `/api` 代理到 `http://localhost:8080`。

### 5. 启动移动端 H5（可选）

```bash
cd app
npm install
npm run dev
```

`app` 与 `fronted` 均监听 5173 端口，同一时间只需启动其中一个；需要同时调试时可修改各自 `vite.config.ts` 中的 `server.port`。

### 6. 生产构建

```bash
# Web 端（含 TypeScript 类型检查）
cd fronted
npm run build        # 产物输出到 dist/

# 后端打包
cd backend
.\mvnw.cmd clean package
java -jar target\backend-0.0.1-SNAPSHOT.jar
```

---

## 接口文档

后端启动后访问：

- Knife4j 聚合文档：<http://localhost:8080/doc.html>
- Swagger UI：<http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON：<http://localhost:8080/v3/api-docs>

所有业务接口统一前缀 `/api`，除白名单外均需携带 `Authorization: Bearer <token>`。

### 接口模块一览

| Controller | 路径前缀 | 主要职责 |
| --- | --- | --- |
| AuthController | `/api/auth` | 登录、获取/修改个人信息、修改密码 |
| OrgController | `/api/org` | 学院 / 专业 / 班级 / 班级分配 |
| PeopleController | `/api/people` | 管理员、辅导员、志愿者、新生导入导出 |
| StudentController | `/api/student` | 新生信息填报、报到码、状态查询 |
| FeeController | `/api/fee` | 费用项目、专业费用配置、缴费状态 |
| DormController | `/api/dorm` | 楼栋房间、选房配置、分配、学生选宿 |
| ProcessController | `/api/process` | 报到流程节点配置 |
| CmsController | `/api/cms` | 迎新手册、校园地图、公告 |
| FaqController | `/api/faq` | FAQ 分类与知识条目 |
| GroupController | `/api/group` | 新生群配置 |
| CounselorController | `/api/counselor` | 辅导员视角的班级 / 学生 / 签到 |
| VolunteerController | `/api/volunteer` | 钥匙、军训服装扫码发放与统计 |
| DashboardController | `/api/dashboard` | 工作台与数据大屏统计 |
| FileController | `/api/file` | 文件上传（S3） |

---

## 角色与路由

`sys_user.user_type` 定义角色，前端路由守卫按角色重定向：

| userType | 角色 | 入口路由 | 所在端 |
| --- | --- | --- | --- |
| 1 | 新生 | `/student-web`（Web）、`/student`（移动） | `fronted` / `app` |
| 2 | 辅导员 | `/counselor` | `fronted` / `app` |
| 3 | 管理员 | `/admin`、`/admin/dashboard-screen` | `fronted` |
| 4 | 志愿者（钥匙） | `/volunteer` | `app` |
| 5 | 志愿者（服装） | `/volunteer/uniform` | `app` |

- 未登录访问受保护页面 → 跳转 `/login?redirect=...`
- 401 响应 → 自动登出并回登录页
- 志愿者（4/5）访问 Web 端 → 跳转移动端地址

---

## 数据库表概览

`freshman_service` 库共 28 张表：

| 分组 | 表名 |
| --- | --- |
| 系统与组织 | `sys_user`、`sys_college`、`sys_major`、`sys_class`、`sys_counselor`、`sys_counselor_class`、`sys_volunteer`、`sys_operation_log` |
| 新生数据 | `stu_info`、`stu_document`、`stu_report_code`、`stu_uniform_size` |
| 报到流程 | `report_flow_config`、`key_distribution`、`uniform_distribution` |
| 费用 | `fee_item`、`fee_major_config` |
| 宿舍 | `dorm_building`、`dorm_room`、`dorm_choose_config`、`dorm_major_alloc`、`stu_dorm_choice` |
| 内容（CMS） | `cms_notice`、`cms_handbook`、`cms_campus_map` |
| 知识与社群 | `faq_category`、`faq_knowledge`、`group_config` |

---

## 常见问题

**1. 启动后端报数据库连接失败？**
确认 MySQL 已启动、`freshman_service` 库已创建并导入 SQL，且 `application.properties` 中的账号密码正确。

**2. 前端登录 401？**
确认后端已在 8080 端口运行；Vite 代理只在 `npm run dev` 下生效，构建产物需由 Nginx 等配置 `/api` 反向代理。

**3. 图片/Excel 上传失败？**
文件走 S3 对象存储，需先设置环境变量 `S3_ENDPOINT`、`S3_ACCESS_KEY`、`S3_SECRET_KEY`、`S3_BUCKET`（未设置时服务可正常启动，仅上传不可用）；单文件大小上限 10MB（`spring.servlet.multipart.max-file-size`）。

**4. `demo/` 目录是做什么的？**
独立的 Spring Boot 实验工程（JPA + Spring Security），用于验证 `PasswordMigrationService` 密码迁移方案，**非运行时依赖**，可忽略。

**5. 志愿者扫码页面打不开？**
志愿者（4/5）在 Web 端会被重定向到移动端地址，需使用 `app/` 移动端访问。
