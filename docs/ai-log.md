# AI 使用日志

按天记录：做了什么、为什么这么做、卡在哪里、怎么解决的。

## D1 · 2026-11-01 · 仓库骨架

**做了什么**：搭建多模块 Maven 工程骨架，创建父 pom 与 common/config/security/catalog/content/ops/startup 七个模块，各模块只留空包与占位类；写了初版 README 与 15 天计划表；配置 .gitignore 与 application.yml（端口 8080、MySQL 连接串）。
**为什么**：M2 是内容模块，但要能独立运行和验收，必须把公共层、伪 M1（登录）、伪 M3（分类）都作为模块放进来，形成完整可启动的工程；startup 只做装配和启动，保证模块边界清晰。
**卡点**：第一次写多模块 pom 时子模块互相依赖顺序搞错，`cms-content` 依赖 `cms-catalog` 但 catalog 还没建，Maven 编译直接失败；解决办法是先建全模块骨架再填内容。

