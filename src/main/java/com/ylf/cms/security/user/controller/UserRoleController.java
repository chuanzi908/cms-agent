package com.ylf.cms.security.user.controller;

import com.ylf.cms.common.annotation.RequirePermission;
import com.ylf.cms.common.constant.Perm;
import com.ylf.cms.common.result.Result;
import com.ylf.cms.security.user.dto.AssignRolesRequest;
import com.ylf.cms.security.user.entity.SysRole;
import com.ylf.cms.security.user.service.UserRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户-角色分配。
 *
 * 与 {@link RoleController}（角色本身的增删改、角色授权限）分开：
 * 那边改的是"角色能干什么"，这里改的是"谁是这个角色"，判权口径也不同——
 * 读放行 user:manage 或 role:manage，写只认 user:manage。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRoleController {

    private final UserRoleService userRoleService;

    @GetMapping("/{id}/roles")
    @RequirePermission({Perm.USER_MANAGE, Perm.ROLE_MANAGE})
    public Result<List<SysRole>> rolesOf(@PathVariable Long id) {
        return Result.ok(userRoleService.rolesOf(id));
    }

    @PutMapping("/{id}/roles")
    @RequirePermission(Perm.USER_MANAGE)
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody AssignRolesRequest request) {
        userRoleService.assignRoles(id, request.getRoleIds());
        return Result.ok();
    }
}
