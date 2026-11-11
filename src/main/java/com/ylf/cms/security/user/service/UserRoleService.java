package com.ylf.cms.security.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ylf.cms.common.exception.BizException;
import com.ylf.cms.common.result.ResultCode;
import com.ylf.cms.security.user.entity.SysRole;
import com.ylf.cms.security.user.mapper.SysRoleMapper;
import com.ylf.cms.security.user.mapper.SysUserMapper;
import com.ylf.cms.security.user.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户-角色关系。
 *
 * 与角色授权一样是"全量替换"：提交的是这个人现在的角色集合，先删后插。
 * 关联表没有逻辑删除，解绑就是真删；历史归属留给 sys_log。
 *
 * 校验放在删除之前：合法的 id 检查不过就直接抛，绝不先把人身上的角色清空再报错——
 * 那种"报错但数据已经变了"的接口，前端不会重试，用户会莫名其妙失去全部权限。
 */
@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final SysUserRoleMapper userRoleMapper;

    /** 某人当前的角色。只返回启用中的角色：停用角色已不生效，列出来只会让前端误会。 */
    public List<SysRole> rolesOf(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIds(userId);
        if (roleIds == null || roleIds.isEmpty()) {
            return Collections.emptyList();
        }
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getId, roleIds)
                .eq(SysRole::getStatus, 1)
                .orderByAsc(SysRole::getSort)
                .orderByAsc(SysRole::getId));
    }

    /**
     * 全量替换某人的角色。
     *
     * 重复 id 去重、null 丢弃；不存在的角色 id 或用户 id 直接拒绝，且不改动任何数据。
     */
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        if (userMapper.selectById(userId) == null) {
            throw new BizException(ResultCode.NOT_FOUND, "用户不存在");
        }

        Set<Long> wanted = new LinkedHashSet<>();
        if (roleIds != null) {
            for (Long roleId : roleIds) {
                if (roleId != null) {
                    wanted.add(roleId);
                }
            }
        }
        if (!wanted.isEmpty()) {
            Long exists = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>()
                    .in(SysRole::getId, wanted));
            if (exists == null || exists.intValue() != wanted.size()) {
                throw new BizException(ResultCode.BAD_REQUEST, "存在无效的角色 id");
            }
        }

        userRoleMapper.deleteByUserId(userId);
        for (Long roleId : wanted) {
            userRoleMapper.insert(userId, roleId);
        }
    }
}
