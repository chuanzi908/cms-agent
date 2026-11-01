# AI 使用日志

按天记录：做了什么、为什么这么做、卡在哪、怎么解决的。

## D1 · 2026-11-01 · 仓库骨架

**做了什么**：建仓库、`pom.xml`、`CmsApplication`、`application.yml`、README 与本计划。

**为什么单独成仓**：智能体是"横切"模块——它要读全站的卡片、权限码和统计数字。放在主仓里时，`@GuideCard` 如果写在 `ops` 包内，M1–M3 的每个 Controller 都要 import 智能体模块，等于被观测者依赖观测者。所以注解放在 `common`，`ops` 只读不写。这个边界决定了本仓库的依赖面：`common` 必带，`security` 必带（要权限码判断"这个用户能不能看这张卡"），`catalog`、`content` 各带一个 Service（建议里的分类与文章数字）。

**卡点**：`ops` 单独拎出来编译不过——它 import 了 `LoginUser`、`RoleService`、`CategoryService`、`ArticleService`。核对了一遍依赖：`common` 27 处、`security` 7 处、`catalog`/`content` 各 1 处。结论是"M4 独立仓库"实际等于"整个后端"（除前端页面外），这决定了 15 天的切法：先上公共层，最后才轮到智能体本身。

**安全**：原项目的 `application.yml` 里写着数据库明文口令。本仓库改成 `${CMS_DB_USER}` / `${CMS_DB_PASSWORD}` 占位，`.env` 与 `*.log` 进 `.gitignore`。判据是"仓库里搜不到口令"：拿原口令去 `git grep`，必须 0 命中（这条能失败——把口令原样贴回来就会命中）。
