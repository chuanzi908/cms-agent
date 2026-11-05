package com.ylf.cms.security.controller;

import com.ylf.cms.common.Result;
import com.ylf.cms.security.entity.SysUser;
import com.ylf.cms.security.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 登录接口（M1 契约版，仅用于本地联调）
 * 正式M1实现应替换为JWT签发，此处返回 userId 作为 Bearer token
 */
@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class AuthController {

    private final SysUserMapper sysUserMapper;

    @PostMapping
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        SysUser user = sysUserMapper.selectOne(Wrappers.<SysUser>lambdaQuery()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0));
        if (user == null || !user.getPassword().equals(password)) {
            return Result.error(40102, "账号或密码错误");
        }
        return Result.success(Map.of("token", String.valueOf(user.getId())));
    }
}
