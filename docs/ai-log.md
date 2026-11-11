# AI 使用日志

按天记录：做了什么、为什么这么做、卡在哪里、怎么解决的。

## D1 · 2026-11-01 · 仓库骨架

**做了什么**：搭建多模块 Maven 工程骨架，创建父 pom 与 common/config/security/catalog/content/ops/startup 七个模块，各模块只留空包与占位类；写了初版 README 与 15 天计划表；配置 .gitignore 与 application.yml（端口 8080、MySQL 连接串）。
**为什么**：M2 是内容模块，但要能独立运行和验收，必须把公共层、伪 M1（登录）、伪 M3（分类）都作为模块放进来，形成完整可启动的工程；startup 只做装配和启动，保证模块边界清晰。
**卡点**：第一次写多模块 pom 时子模块互相依赖顺序搞错，`cms-content` 依赖 `cms-catalog` 但 catalog 还没建，Maven 编译直接失败；解决办法是先建全模块骨架再填内容。

## D2 · 2026-11-02 · 公共层：统一结果与全局异常

**做了什么**：在 common 模块实现统一响应 `Result<T>`（code/message/data）、错误码 `ResultCode`、业务异常 `BusinessException`；在 config 模块实现 `GlobalExceptionHandler`，用 `@RestControllerAdvice` 把业务异常、参数校验异常、兜底异常统一转成 `Result` 结构。
**为什么**：前后端交互需要一个稳定的响应契约，验收工具（HTTP 脚本）才能断言 code 是否为 40900/40901 这类错误码；没有统一异常处理时，MyBatis 异常会以 500 裸错误返回。
**卡点**：一开始把 `Result` 的泛型写法搞成 `Result<T> { T data; }` 但没写 getter，Jackson 序列化时字段全丢了，返回体只有 code 没有 data；加上 `@Data` 后正常。

## D3 · 2026-11-03 · 公共层：登录态与跨模块契约注解

**做了什么**：实现 `UserContext`（ThreadLocal 保存当前登录用户）、`LoginUser`（用户ID/角色/权限列表）、`ArticleAuthorCounter`（供 M1 删除用户时查稿件的计数接口）、`@RequirePermission` 与 `@GuideCard` 注解。
**为什么**：M2 接口要校验"当前登录人是不是作者本人""有没有某权限"，而 M1 还没实现，先用公共层把登录态和跨模块契约定下来，M1/M3 同学照注解实现即可。
**卡点**：注解的 `@Target` 写漏了 `ElementType.METHOD`，切面扫描不到方法上的注解，权限校验一直不生效；补上 METHOD 后正常。

## D4 · 2026-11-04 · 数据层：建表脚本与 MyBatis-Plus 配置

**做了什么**：写 `sql/schema.sql`（sys_user、sys_role、sys_permission、sys_user_role、cms_category、cms_article 等 8 张表 + 种子数据），提供 `db/init.bat`/`init.sh` 一键初始化；在 config 模块配置 MyBatis-Plus 分页插件、乐观锁插件、字段自动填充（create_time/update_time/deleted）。
**为什么**：伪 M1 要连真实 MySQL 读用户表，M2 的文章表、分类表也需要真实表结构；乐观锁和逻辑删除是验收点，必须在插件层配好。
**卡点**：MyBatis-Plus 乐观锁要实体字段标 `@Version` 且插件里 new OptimisticLockerInnerInterceptor，否则 update 不会带 version 条件；漏配插件时并发编辑两个请求都成功，验收用例 2 不通过。

## D5 · 2026-11-05 · 伪 M1 登录：连接真实 MySQL 读取用户

**做了什么**：实现 security 模块的 `SysUser` 实体、`SysUserMapper`（BaseMapper）、`AuthController`（POST /api/login 返回 token=userId）、`LoginInterceptor`（从 Authorization 头解析 token 写入 UserContext）、`UserService/UserServiceImpl`（loadLoginUser 查库）；在 startup 注册拦截器，放行 /api/login。
**为什么**：小组 M1 还没交付，但 M2 所有接口都要登录态；按分工说明的契约做一个能连接真实 MySQL 读取 sys_user 的伪 M1，先让 M2 跑起来。
**卡点**：登录接口用 curl 测试时 `-d "username=admin&password=admin123"` 中文参数乱码，请求体编码不对；解决方法是 HTTP 测试文件里明确 Content-Type 与 UTF-8，密码用英文/数字。

## D6 · 2026-11-06 · 伪 M1 权限：角色权限真实查库与切面校验

**做了什么**：升级 `SysUserMapper`，新增 `findRoleCodes`、`findPermissionCodes` 两张注解 @Select 查询（联表 sys_user_role/sys_role/sys_permission）；实现 `PermissionAspect`，在 `@RequirePermission` 注解的方法上切面校验当前用户权限码，不通过返回 403 风格错误。
**为什么**：验收要"权限不足时拒绝"，伪 M1 的权限不能写死，必须真实查库；切面校验让 M2 的 controller 只标注解、不写校验逻辑。
**卡点**：权限表种子数据里 zhangsan 的账号 deleted 被误置为 1，导致登录后查不到角色权限，联表结果为空；把 zhangsan 恢复为 deleted=0 后权限查询正常。

## D7 · 2026-11-07 · 伪 M1 用户管理：删除守卫接 M2 计数

**做了什么**：实现 `UserAdminController`（DELETE /api/users/{id}、列表查询等用户管理接口）；`UserService` 新增 `countAll`（统计用户数）与删除守卫逻辑——删除用户前调用 M2 的 `ArticleAuthorCounter.countArticlesByAuthor`，稿件数大于 0 返回 40903。
**为什么**：分工说明要求"账号下有稿件不允许删除"，这是 M2 与 M1 的协作点；M1 没实现，就在伪 M1 里通过公共层接口接上 M2 的计数能力。
**卡点**：`countArticlesByAuthor` 的返回类型一开始写成了 `int`，跨模块接口约束不统一；统一改成 `long` 后编译通过。

## D8 · 2026-11-08 · 伪 M3 分类：分类树与启用校验

**做了什么**：实现 catalog 模块的 `CmsCategory` 实体、`CmsCategoryMapper`、`CategoryService`（descendantIds 展开子分类、requireEnabled 校验启用状态、列表树）、`CategoryFlatVO`。
**为什么**：M2 的文章必须挂在分类下，且"停用分类不能发文章"是验收点；M3 没交付，用伪 M3 提供分类能力，M2 通过 Service 接口调用（不跨模块 import mapper）。
**卡点**：递归展开子分类时死循环（父子 id 相同的数据），在递归入口加 visited 集合去重后正常。

## D9 · 2026-11-09 · 文章数据层：实体、Mapper 与联查 SQL

**做了什么**：实现 content 模块的 `CmsArticle` 实体（含 @Version 乐观锁、@TableLogic 逻辑删除）、`ArticleStatusEnum`（草稿/待审核/已发布/已下架）、`CmsArticleMapper` 与 XML（联查分类名、作者名的 selectArticleVO）。
**为什么**：文章列表要展示分类名和作者名，单表查询不够；联查 SQL 放 XML 便于维护，也是后面消除 N+1 的基础。
**卡点**：XML 放在 `src/main/java` 下时打包不进 target/classes，运行期报 "Invalid bound statement"；把 mapper XML 移到 `src/main/resources/mapper` 后正常。

## D10 · 2026-11-10 · 接口层：DTO 与 Controller

**做了什么**：实现 `ArticleSaveDTO`/`ArticleUpdateDTO`/`ArticleQueryDTO`（带参数校验注解）、`ArticleVO`、`ArticleController`（增删改查、提交审核、审核通过/驳回、下架、分页列表、工作台统计接口骨架）、`ArticleService` 接口。
**为什么**：请求参数和响应结构固定下来，前端与验收脚本按 DTO/VO 对接；Controller 只做参数接收和权限注解。
**卡点**：DTO 校验注解用了 `@NotBlank` 但没加 spring-boot-starter-validation 依赖，校验不生效；补依赖后 400 参数错误能正确返回。

## D11 · 2026-11-11 · 状态机与乐观锁：核心业务落地

**做了什么**：实现 `ArticleServiceImpl`：创建/编辑/删除/提交审核/审核通过/驳回/下架全流程，状态机严格校验（草稿→待审核→已发布→已下架，非法流转返回 40901）；编辑带乐观锁（version 冲突返回 40900）；删除账号守卫接 countByAuthor。
**为什么**：状态流转和并发控制是 M2 的核心验收点；用 MyBatis-Plus 乐观锁插件 + 显式状态判断实现。
**卡点**：编辑时忘了带 version 字段，update 不触发乐观锁，两个并发编辑都成功；把 DTO 的 version 传进 update 实体后，40900 正常返回。

