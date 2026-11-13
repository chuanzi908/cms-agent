package com.ylf.cms.content.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 工作台统计VO（M2）
 * 跨模块：用户总数来自M1 UserService.countAll，分类总数来自M3 CategoryService.listFlat
 */
@Data
public class DashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 稿件总数 */
    private Long articleTotal;

    /** 草稿数 */
    private Long draftCount;

    /** 待审核数 */
    private Long pendingCount;

    /** 已发布数 */
    private Long publishedCount;

    /** 已下架数 */
    private Long offlineCount;

    /** 系统账号总数（M1） */
    private Long userCount;

    /** 分类总数（M3） */
    private Long categoryCount;
}
