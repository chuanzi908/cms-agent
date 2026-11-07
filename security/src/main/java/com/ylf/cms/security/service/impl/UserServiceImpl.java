package com.ylf.cms.security.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ylf.cms.common.LoginUser;
import com.ylf.cms.security.entity.SysUser;
import com.ylf.cms.security.mapper.SysUserMapper;
import com.ylf.cms.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 用户服务实现（M1 契约版）
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;

    @Override
    public long countAll() {
        return sysUserMapper.selectCount(Wrappers.<SysUser>lambdaQuery().eq(SysUser::getDeleted, 0));
    }

    @Override
    public LoginUser loadLoginUser(Long userId) {
        if (userId == null) {
            return null;
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        // 真实查询角色与权限编码（伪M1也走同一张库）
        List<String> roles = sysUserMapper.findRoleCodes(userId);
        List<String> permissions = sysUserMapper.findPermissionCodes(userId);
        if (roles == null) {
            roles = Collections.emptyList();
        }
        if (permissions == null) {
            permissions = Collections.emptyList();
        }
        return new LoginUser(user.getId(), user.getUsername(), user.getNickname(), roles, permissions);
    }
}
