# CMS 接口文档（M2 内容模块）

> 基础地址：`http://127.0.0.1:8080`
> 认证：除登录外，所有接口请求头携带 `Authorization: Bearer {token}`（token 即登录返回的 userId）
> 返回格式：`{ "code": 0, "message": "操作成功", "data": ... }`

## 错误码说明（M2 相关）

| 错误码 | 含义 | 触发场景 |
|---|---|---|
| 0 | 成功 | - |
| 40000 | 参数校验失败 | 标题/正文/分类为空等 |
| 40100 | 未登录或会话失效 | 未携带 token |
| 40300 | 无操作权限 | 权限切面拒绝 |
| 40401 | 文章不存在或已删除 | 操作不存在的文章 |
| 40900 | 数据已被他人修改 | 乐观锁并发冲突（验收用例2） |
| 40901 | 当前状态不允许该操作 | 草稿直接审核通过等（验收用例1） |
| 40902 | 分类不存在或已停用 | 选择停用分类建稿 |
| 40903 | 该账号下存在稿件 | 删除有稿件的账号（验收用例3，M1侧抛出） |

---

## 1. 登录（M1 契约版）

`POST /api/login`

```json
{ "username": "admin", "password": "admin123" }
```

响应：

```json
{ "code": 0, "message": "操作成功", "data": { "token": "1" } }
```

---

## 2. 新增文章

`POST /api/article` （权限：article:create）

```json
{
  "title": "测试文章001",
  "content": "文章正文",
  "categoryId": 1
}
```

新建默认状态为草稿(0)。

## 3. 修改文章

`PUT /api/article` （权限：article:update）

```json
{
  "id": 1,
  "version": 1,
  "title": "新标题",
  "content": "新正文",
  "categoryId": 1
}
```

- 必须携带 `version`（取自查询详情的返回）
- 版本不一致返回 **40900**
- 已发布/已下架文章不可编辑，返回 **40901**

## 4. 删除文章（逻辑删除）

`DELETE /api/article/{id}` （权限：article:delete）

## 5. 文章详情

`GET /api/article/{id}`

## 6. 分页列表

`GET /api/article/page?pageNum=1&pageSize=10&title=&status=&categoryId=&authorId=`

- `categoryId` 自动展开为包含子分类
- 返回结构：`{ total, records: [...] }`，每条含 authorName、categoryName（一次联查，无N+1）

## 7. 提交审核

`POST /api/article/{id}/submitAudit` （权限：article:audit）— 草稿 → 待审核

## 8. 审核通过

`POST /api/article/{id}/passAudit` （权限：article:audit）— 待审核 → 已发布
> 草稿状态直接调用返回 **40901**（验收用例1）

## 9. 审核驳回

`POST /api/article/{id}/rejectAudit` （权限：article:audit）— 待审核 → 草稿

## 10. 下架

`POST /api/article/{id}/offline` （权限：article:offline）— 已发布 → 已下架

## 11. 内容工作台

`GET /api/content/dashboard`

返回：稿件总数、草稿数、待审核数、已发布数、已下架数、账号总数（M1）、分类总数（M3）

---

## 验收用例复现步骤

### 用例1：草稿直接审核通过 → 40901
1. `POST /api/article` 新建草稿
2. 直接 `POST /api/article/{id}/passAudit`
3. 预期返回 `code: 40901`

### 用例2：并发编辑冲突 → 40900
1. `GET /api/article/{id}` 拿到 `version`
2. 连续两次 `PUT /api/article` 传相同 `version`
3. 第二次返回 `code: 40900`

### 用例3：删除有稿件的账号 → 40903（M1-M2 跨模块契约）
1. 用 zhangssan(2) 建一篇文章
2. 调用 M1 删除账号接口（契约版在 `com.ylf.cms.security.controller.UserAdminController`）
3. 预期返回 `code: 40903`

### 用例4：分页 SQL 无 N+1
1. 观察控制台 SQL 日志
2. 调用 `GET /api/article/page`
3. 预期只出现 2 条 SQL：`selectPageVO_mpCount`（COUNT）+ `selectPageVO`（LIMIT）
