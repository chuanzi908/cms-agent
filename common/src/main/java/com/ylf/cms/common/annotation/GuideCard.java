package com.ylf.cms.common.annotation;

import java.lang.annotation.*;

/**
 * 工作台卡片标记注解（公共层，M1维护）
 * M4智能体/运维模块通过该注解识别并绑定工作台卡片
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GuideCard {

    /** 卡片标题 */
    String title() default "";

    /** 卡片编码 */
    String code() default "";
}
