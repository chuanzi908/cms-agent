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

