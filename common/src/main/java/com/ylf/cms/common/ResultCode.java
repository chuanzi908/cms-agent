package com.ylf.cms.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码（公共层，M1维护）
 *
 * 约定：40xxx 通用/参数，401xx 认证，403xx 权限，404xx 不存在，409xx 业务冲突，50xxx 系统错误
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(0, "操作成功"),

    // 通用
    PARAM_ERROR(40000, "参数校验失败"),
    UNAUTHORIZED(40100, "未登录或会话已失效"),
    FORBIDDEN(40300, "无操作权限"),
    NOT_FOUND(40400, "资源不存在"),
    SYSTEM_ERROR(50000, "系统繁忙，请稍后重试"),

    // M1 账号与权限
    ACCOUNT_NOT_FOUND(40101, "账号不存在"),
    ACCOUNT_PASSWORD_ERROR(40102, "账号或密码错误"),
    ACCOUNT_DISABLED(40301, "账号已被禁用"),
    PERMISSION_DENIED(40302, "权限不足，拒绝访问"),
    CANNOT_LOCK_SELF(40303, "不能锁定/删除当前登录账号自身"),

    // M2 内容模块
    CONCURRENT_CONFLICT(40900, "数据已被他人修改，请刷新后重试"),
    ILLEGAL_STATE(40901, "当前状态不允许该操作"),
    ARTICLE_NOT_FOUND(40401, "文章不存在或已删除"),
    ACCOUNT_HAS_ARTICLES(40903, "该账号下存在稿件，无法删除"),

    // M3 分类模块
    CATEGORY_NOT_FOUND(40402, "分类不存在"),
    CATEGORY_DISABLED(40902, "分类不存在或已停用"),
    CATEGORY_DEPTH_EXCEEDED(40904, "分类层级最多支持三级");

    private final int code;
    private final String message;
}
