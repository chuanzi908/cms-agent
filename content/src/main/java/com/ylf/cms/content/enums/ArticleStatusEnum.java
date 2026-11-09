package com.ylf.cms.content.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文章状态枚举（M2）
 * 流转规则：
 *   草稿(0) --提交审核--> 待审核(1) --审核通过--> 已发布(2) --下架--> 已下架(3)
 *   待审核(1) --审核驳回--> 草稿(0)
 *   禁止：草稿直接审核通过、已发布直接退回草稿
 */
@Getter
@AllArgsConstructor
public enum ArticleStatusEnum {

    DRAFT(0, "草稿"),
    PENDING(1, "待审核"),
    PUBLISHED(2, "已发布"),
    OFFLINE(3, "已下架");

    private final int code;
    private final String desc;

    public static ArticleStatusEnum of(Integer code) {
        if (code == null) {
            return null;
        }
        for (ArticleStatusEnum e : values()) {
            if (e.code == code) {
                return e;
            }
        }
        return null;
    }
}
