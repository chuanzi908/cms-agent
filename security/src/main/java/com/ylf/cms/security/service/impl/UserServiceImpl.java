package com.ylf.cms.security.service.impl;

import com.ylf.cms.common.LoginUser;
import com.ylf.cms.security.entity.SysUser;
import com.ylf.cms.security.mapper.SysUserMapper;
import com.ylf.cms.security.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现（M1 契约版）
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final SysUserMapper sysUserMapper;

    @Override
    public LoginUser loadLoginUser(Long userId) {
        if (userId == null) {
            return null;
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        // 临时实现：以角色码充当权限码（D6 发现失真后改为真实联查 sys_role/sys_permission）
        List<String> roles = new ArrayList<>();
        roles.add("user");
        if (userId == 1L) {
            roles.add("ADMIN");
        }
        return new LoginUser(user.getId(), user.getUsername(), user.getNickname(), roles, new ArrayList<>(roles));
    }
}
