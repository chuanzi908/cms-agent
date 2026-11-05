package com.ylf.cms.security.interceptor;

import com.ylf.cms.common.BusinessException;
import com.ylf.cms.common.LoginUser;
import com.ylf.cms.common.ResultCode;
import com.ylf.cms.common.UserContext;
import com.ylf.cms.security.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 登录拦截器（M1 契约版）
 * 约定：请求头 Authorization: Bearer {userId}，解析后写入 UserContext
 * 正式M1实现应替换为JWT/Redis会话解析，契约保持不变
 */
@Component
@RequiredArgsConstructor
public class LoginInterceptor implements HandlerInterceptor {

    private final UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        if (!StringUtils.hasText(auth) || !auth.startsWith("Bearer ")) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        String token = auth.substring(7).trim();
        Long userId;
        try {
            userId = Long.parseLong(token);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        LoginUser loginUser = userService.loadLoginUser(userId);
        if (loginUser == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        UserContext.set(loginUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}
