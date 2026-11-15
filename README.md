# cms-m2 — CMS 内容模块（M2）

企业级 CMS 课程项目的 **M2 内容模块**完整工程。在小组正式 M1/M3 交付前，内置了**可连真实 MySQL 读取的伪 M1（账号权限）、伪 M3（分类）契约实现**，保证 M2 可以独立启动、联调、演示 4 项验收用例；正式模块到位后按契约替换即可无缝接入。

> 模块边界严格遵守《分工说明》：M2 只调用 M1/M3 的 Service、DTO/VO，禁止跨模块 import 对方的 mapper/entity/controller/config，由 `ModuleBoundaryTest` 强制校验。

---

## 一、技术栈

| 组件 | 版本 | 说明 |
|---|---|---|
| Java | 17（release） | 需 JDK 17+，本机验证用 JDK 24 编译运行 |
| Spring Boot | 3.3.5 | 启动模块聚合全部业务模块 |
| MyBatis-Plus | 3.5.7 | 分页插件、乐观锁插件、逻辑删除、字段自动填充 |
| Lombok | 1.18.38 | 父 pom 显式覆盖默认 1.18.34（JDK23+ 必须，否则注解不生效） |
| MySQL | 8.0+（验证用 9.7.0） | 库名 `cms`，连接 `root/123456@127.0.0.1:3306` |
| Maven | 3.9.x | 多模块构建 |

## 二、工程结构（Maven 多模块）

```
cms/
├── pom.xml                  # 父 pom：模块声明、Lombok 版本覆盖、JDK17、编译插件 proc=full
├── common/                  # 公共层（按分工由 M1 维护）：Result/ResultCode/BusinessException/
│                            #   UserContext/LoginUser/ArticleAuthorCounter/@RequirePermission/@GuideCard
├── config/                  # 公共配置（M1 维护）：MybatisPlusConfig(分页+乐观锁)/
│                            #   GlobalExceptionHandler/MyMetaObjectHandler
├── security/                # 【伪 M1】账号与权限契约：登录、用户 CRUD、
│                            #   角色/权限真实查库、@RequirePermission 切面、删除账号守卫(ArticleAuthorCounter)
├── catalog/                 # 【伪 M3】分类契约：分类树、启用校验、子分类展开
├── content/                 # 【M2 内容模块】文章状态机/审核/乐观锁/逻辑删除/分页/工作台
├── ops/                     # M4 预留模块（占位）
├── startup/                 # 启动模块：CmsApplication、WebConfig(登录拦截)、application.yml、
│                            #   ModuleBoundaryTest（边界校验测试）
├── sql/schema.sql           # 全表 DDL + 种子数据（建库后执行一次）
├── db/init.bat, init.sh     # 一键初始化数据库脚本
├── docs/                    # api.md 接口文档、M2验收记录.md（实测结果存档）
├── apifox/cms-openapi.json  # OpenAPI 规范（可导入 Apifox/Postman）
└── cms-m2-test.http         # IDEA HTTP Client 验收脚本（点 ▶ 即可跑）
```

**模块依赖方向**（只允许下行引用）：

```
startup → security(伪M1) → common/config
       ↘  catalog(伪M3)  ↗
       ↘  content(M2)    ↗
```

## 三、环境准备

1. **JDK 17+**（IDEA 里 Project Structure → SDK 选本机 JDK，如 temurin-24）
2. **Maven 3.9+**（IDEA 自带或独立安装）
3. **MySQL**：本机服务运行中，账号 `root`，密码 `123456`（如不同，改 `startup/src/main/resources/application.yml`）
4. **IDEA**（社区版即可）：打开工程根目录 `pom.xml`，等待依赖下载完成

> Lombok 版本已在父 pom 固定为 1.18.38，并在 maven-compiler-plugin 显式声明 `proc=full` + annotationProcessorPaths，**无需手动装 Lombok 插件也能编译**（JDK 23+ 下默认识别注解处理器）。

## 四、快速开始（4 步）

### 第 1 步：初始化数据库（一次性）

双击运行 `db/init.bat`，或在命令行执行：

```bash
mysql -uroot -p123456 < sql/schema.sql
```

脚本会创建 `cms` 库、8 张表并写入种子数据（管理员 admin/admin123、张三 zhangsan/123456、4 个分类、6 项权限）。

### 第 2 步：构建打包

```bash
mvn -B -pl startup -am package -DskipTests
```

产出可执行 jar：`startup/target/cms-startup-1.0.0.jar`

### 第 3 步：启动应用

方式一（命令行）：

```bash
java -jar startup/target/cms-startup-1.0.0.jar
```

方式二（IDEA）：打开 `startup/src/main/java/com/ylf/cms/CmsApplication.java` → 右键 → Run。

启动成功标志：控制台出现 `Started CmsApplication in 2.x seconds`，服务监听 **8080**。

### 第 4 步：跑验收脚本

IDEA 中双击打开根目录 **`cms-m2-test.http`**，按顺序点击每段左侧绿色 ▶：

- 第 1、2 段登录 admin / zhangsan（自动保存 token）
- 第 3 段新建文章（自动保存文章 id）
- 其余段落自动引用，无需改任何参数

## 五、4 项验收用例（答辩演示）

| # | 验收点 | 操作 | 预期 |
|---|---|---|---|
| 1 | 草稿直接审核被拒 | 新建文章后直接调 `POST /api/article/{id}/passAudit` | `code=40901 当前状态不允许该操作` |
| 2 | 乐观锁并发冲突 | 两个请求用同一 `version` 修改同一文章 | 第 1 个成功(version+1)，第 2 个 `code=40900 数据已被他人修改` |
| 3 | 删除有稿件账号被拦（M1→M2 契约） | zhangsan 建稿后，admin 调 `DELETE /api/users/2` | `code=40903 该账号下存在稿件，无法删除` |
| 4 | 分页无 N+1 | 调 `GET /api/article/page`，看应用控制台 SQL | 仅 2 条 SQL：`COUNT(*)` 统计 + 一次 `LEFT JOIN` 联查 `LIMIT` |

> 验收 4 观察点：控制台打印的 SQL 中，分页请求只有 `SELECT COUNT(*) AS total ...` 和 `SELECT a.id,... JOIN sys_user ... JOIN cms_category ... LIMIT ?` 两条，中间没有任何逐行查询。

**验收 3 注意事项**：账号删除是逻辑删除，复现需先重置：重新执行 `sql/schema.sql`（或 `db/init.bat`）。

## 六、接口清单

### 认证（伪 M1）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/login | 登录，body `{username,password}`，返回 `data.token`（即 userId） |
| DELETE | /api/users/{id} | 删除账号（守卫：有稿件返回 40903） |

### 内容（M2）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/article | 新建文章（默认草稿） |
| GET | /api/article/{id} | 文章详情（联查作者昵称、分类名） |
| PUT | /api/article | 修改文章（body 带 `version`，冲突返回 40900） |
| DELETE | /api/article/{id} | 逻辑删除文章 |
| GET | /api/article/page | 分页列表（支持 `pageNum/pageSize/title/status/categoryId/authorId`） |
| POST | /api/article/{id}/submitAudit | 提交审核（草稿→待审核） |
| POST | /api/article/{id}/passAudit | 审核通过（待审核→已发布） |
| POST | /api/article/{id}/rejectAudit | 审核驳回（待审核→草稿） |
| POST | /api/article/{id}/offline | 下架（已发布→已下架） |
| GET | /api/content/dashboard | 工作台统计（账号数+分类数+各状态稿件数） |

所有接口（除登录）请求头需带 `Authorization: Bearer {token}`。admin 拥有全部权限；zhangsan 仅有 article:create / article:update。

**状态机**：

```
草稿(0) --提交审核--> 待审核(1) --审核通过--> 已发布(2) --下架--> 已下架(3)
待审核(1) --审核驳回--> 草稿(0)
禁止：草稿直接审核通过（40901）
```

## 七、错误码约定（common/ResultCode）

| code | 含义 |
|---|---|
| 0 | 成功 |
| 40100 | 未登录或会话失效 |
| 40101 | 账号不存在 |
| 40102 | 账号或密码错误 |
| 40300 | 无操作权限 |
| 40302 | 权限不足，拒绝访问 |
| 40401 | 文章不存在或已删除 |
| 40402 | 分类不存在 |
| 40900 | 并发冲突（数据已被他人修改） |
| 40901 | 当前状态不允许该操作 |
| 40902 | 分类不存在或已停用 |
| 40903 | 该账号下存在稿件，无法删除 |
| 50000 | 系统异常 |

## 八、边界校验（模块归属 + 越界引用）

```bash
cd startup
mvn -o test -Dtest=ModuleBoundaryTest
```

- **归属检查**：每个 `.java` 必须落在 common/config/security/catalog/content/ops/startup 之一
- **越界检查**：扫描全部 import，跨模块引用对方 mapper/entity/controller/config 即失败，并打印模块名、包名、文件

## 九、常见问题（FAQ）

| 问题 | 解决 |
|---|---|
| 启动报端口被占用 | 8080 被占用时，先结束旧 java 进程，或改 `application.yml` 的 `server.port` |
| `Invalid bound statement` | 重新 `mvn package`（mapper XML 在 `content/src/main/resources/mapper/`） |
| Lombok 报错（找不到构造器） | 确认父 pom `lombok.version=1.18.38`，且 IDEA 里 JDK 级别 ≥17 |
| 登录返回 50000 | 检查 MySQL 服务是否运行、`application.yml` 账号密码是否正确 |
| 验收3 复现不了（删了张三） | 重跑 `sql/schema.sql` 重置数据 |
| `spring-boot:run` 找不到主类 | 改用 `mvn -B -pl startup -am package` + `java -jar` |

## 十、协作约定（摘自《分工说明》）

- 提交信息带模块前缀：`[M2] feat: ...` / `[M2] fix: ...`，每人用自己的账号提交（git log 即分工证据）
- 接口变更先改 `docs/api.md` 与 `apifox/cms-openapi.json`（一对，双向校验）
- 改表只改 `sql/schema.sql`，统一走 `db/init.sh`，不手改库
- 改 common 前先知会全员，改完跑全量测试
- 正式 M1/M3 到位后：替换 security/catalog 模块实现，重跑 ModuleBoundaryTest 与 4 项验收即可
