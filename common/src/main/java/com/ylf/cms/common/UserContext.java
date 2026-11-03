package com.ylf.cms.common;

/**
 * 登录用户上下文（公共层，M1维护）
 * 由M1的登录拦截器写入，各模块只读使用，禁止修改
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser loginUser) {
        HOLDER.set(loginUser);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    /** 获取当前登录用户ID，未登录返回 null（接口已由登录拦截器保护） */
    public static Long getUserId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.getUserId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
