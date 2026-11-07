package com.ylf.cms.security.service;

import com.ylf.cms.common.LoginUser;

/**
 * 用户服务（M1 对外契约）
 * 其他模块（M2/M3/M4）只能调用本接口与DTO/VO，禁止访问M1的mapper/entity
 */
public interface UserService {

    /**
     * 系统账号总数（M2工作台统计使用）
     */
    long countAll();

    /**
     * 根据用户ID加载登录态（M1登录拦截器内部使用）
     */
    LoginUser loadLoginUser(Long userId);
}
