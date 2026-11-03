package com.ylf.cms.common.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解（公共层，M1维护）
 * 标注在Controller方法上，由M1权限切面拦截校验
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequirePermission {

    /** 所需权限编码，如 "article:create" */
    String value();
}
