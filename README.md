# cms-m2 — CMS 内容模块（M2）

《企业级 CMS》课程项目 **M2 内容模块**（`com.ylf.cms.content`）。15 天推进：计划见 `docs/计划.md`，每日进度与卡点见 `docs/ai-log.md`。

## 当前进度（D1）

- 多模块工程骨架：父 pom + 7 个模块（common/config/security/catalog/content/ops/startup）
- 启动入口 `CmsApplication`、`application.yml`
- 今日验证：`mvn -B -pl startup -am package` 通过

## 规划（15 天）

公共层（结果/异常/登录态契约）→ 数据层 → 伪 M1 登录/权限/用户管理 → 伪 M3 分类 → M2 文章（状态机/乐观锁/分页/逻辑删除/工作台）→ 测试验收 → 文档收尾。详见 `docs/计划.md`。
