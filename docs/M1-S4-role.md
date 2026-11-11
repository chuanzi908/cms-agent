# M1-S4 · 角色与授权：判据落位 + 用户角色分配

S4 交付的 13 个文件里，多数与 main 上已有的实现重复，原样放入会撞车（见下表）。
本次只补 main 真正缺的一块：**用户-角色分配**（谁是这个角色），并补两条判据测试。

## 判据对照

| S4 的判据 | main 的实现 | 状态 |
|---|---|---|
| 用户没有任何角色 → 空集合，不抛异常 | 本次新增 `UserRoleService#rolesOf` 返回 `Collections.emptyList()` | 本次补齐 |
| 角色码与权限码分别从三张关联表聚合，不拿反 | main 的 `SysUserMapper.selectRoleCodes` / `selectPermCodes` 用两条 SQL 分别取（`role_code` 与 `perm_code`），语义等价 | 已有 |
| 删角色时先清 `sys_role_permission` 再逻辑删除角色 | main `RoleService#delete`：先查是否仍被用户占用（40903），再 `deleteById` + `deleteByRoleId` | 已有 |
| 角色授权是全量替换（先删后插、id 去重） | main `RoleService#assignPermissions`：校验权限点存在后 `deleteByRoleId` + `insertIgnore`，并对 ADMIN 强制保留 `role:manage` | 已有 |
| 用户角色分配是全量替换 | **本次补齐** `UserRoleService#assignRoles` | 本次补齐 |

## 为什么没放 S4 的 RoleService / RoleController / DbAuthorityLoader

| S4 文件 | 处理 | 原因 |
|---|---|---|
| `security/role/RoleService.java`、`impl/RoleServiceImpl.java` | 未纳入 | main 的 `security.user.service.RoleService` 已覆盖（还多了 ADMIN 保护、被占用时 40903、`listPermissions`、`permissionNames` 给智能体用） |
| `security/role/controller/RoleController.java` | 未纳入 | 它与 main 的 `RoleController` 都映射 `GET/POST /api/roles`、`PUT /api/roles/{id}/permissions`、`DELETE /api/roles/{id}`，同时存在会 `Ambiguous mapping` 启动失败；main 版本还带 `@RequirePermission` 与 `@GuideCard` |
| `security/role/UserRoleService.java`、`impl/UserRoleServiceImpl.java` | **适配后纳入** | main 缺这块能力；改为 main 的风格（实体构造用注解 SQL 的 `SysUserRoleMapper`，不新建 `SysUserRole` 的 BaseMapper） |
| `security/auth/impl/DbAuthorityLoader.java` | 未纳入 | 它实现的是 S3 的 `AuthorityLoader` 接口，main 没有该接口（main 的 `AuthService#loadLoginUser` 直接用 `selectRoleCodes`/`selectPermCodes` 聚合）；且它调用了 `SysPermission#getStatus()`，main 的 `sys_permission` 表没有 `status` 列，编译不过 |
| `security/role/dto/*` | 部分纳入 | `AssignRolesRequest` 纳入；`RoleRequest` / `AssignPermissionsRequest` 与 main 的 `RoleSaveRequest` / `PermissionAssignRequest` 重复 |
| 测试 4 个 | 2 个改写 | `UserRoleServiceTest` → 并入 `UserRoleBoundaryTest`；`RoleServiceTest` / `RoleControllerTest` / `DbAuthorityLoaderTest` 针对的是被跳过的实现，main 已有 `RolePermissionBoundaryTest` 覆盖同等判据 |

## 本次新增

| 文件 | 说明 |
|---|---|
| `security/user/service/UserRoleService.java` | `rolesOf`（只含启用角色）、`assignRoles`（全量替换，先校验后删） |
| `security/user/dto/AssignRolesRequest.java` | `roleIds` 必填；传空数组表示收掉全部角色 |
| `security/user/controller/UserRoleController.java` | `GET /api/users/{id}/roles`（`user:manage` 或 `role:manage`）、`PUT /api/users/{id}/roles`（`user:manage`） |
| `src/test/.../UserRoleBoundaryTest.java` | 4 条判据 |

两处刻意的取舍：

- **先校验后删**：角色 id 或用户 id 非法时直接抛错，绝不清空已有角色。否则接口报错了，人身上的角色却已经被删掉，前端不会重试，用户会莫名其妙失去全部权限。
- **只列启用角色**：停用角色本就不参与授权，列出来只会让前端以为还能用。

## 怎么验

```bash
mvn -B test -Dtest=UserRoleBoundaryTest   # 4 条
mvn -B test                               # 全量
```

红验证：

- 把 `assignRoles` 的校验挪到 `deleteByUserId` 之后 → `invalidRoleDoesNotTouchData` 失败
  （`deleteByUserId` 被调用了，"报错但数据已改"）；
- 把 `LinkedHashSet` 换成 `ArrayList`（即去掉去重）→ `duplicatesInsertedOnce` 报错
  （`insert` 被多调了一次，`times(2)` 不成立）。

两条都实测过，改回后 `Tests run: 4, Failures: 0`。

## 手工复验（需要库）

1. `admin` 登录，`GET /api/users/2/roles` 看到 `AUTHOR`；
2. `PUT /api/users/2/roles`，body `{"roleIds":[2,3]}` → 200；
3. 再 `GET` 同路径 → `AUTHOR` + `AUDITOR`；
4. `PUT` body `{"roleIds":[99]}` → 400 `40000 存在无效的角色 id`，且再 `GET` 仍是上一步的两个角色（没被清空）。
