package com.ylf.cms.security.aspect;

import com.ylf.cms.common.BusinessException;
import com.ylf.cms.common.LoginUser;
import com.ylf.cms.common.ResultCode;
import com.ylf.cms.common.UserContext;
import com.ylf.cms.common.annotation.RequirePermission;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限校验切面（M1 契约版）
 * 校验 @RequirePermission 注解声明的权限编码；admin 角色直接放行
 */
@Aspect
@Component
public class PermissionAspect {

    @Before("@annotation(requirePermission)")
    public void checkPermission(RequirePermission requirePermission) {
        LoginUser user = UserContext.get();
        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        List<String> roles = user.getRoles();
        if (roles != null && roles.contains("admin")) {
            return;
        }
        List<String> permissions = user.getPermissions();
        if (permissions != null && permissions.contains(requirePermission.value())) {
            return;
        }
        throw new BusinessException(ResultCode.PERMISSION_DENIED);
    }
}
