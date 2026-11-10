package com.ylf.cms.content.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询条件（M2）
 * 前端传 categoryId，Service 内部通过 M3 CategoryService.descendantIds 展开为 categoryIds（含子分类）
 */
@Data
public class ArticleQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int pageNum = 1;

    private int pageSize = 10;

    /** 标题模糊搜索 */
    private String title;

    /** 状态：0-草稿 1-待审核 2-已发布 3-已下架 */
    private Integer status;

    /** 分类ID（含子分类） */
    private Long categoryId;

    /** 作者ID */
    private Long authorId;

    /** 内部使用：展开后的分类ID集合（含子分类） */
    private List<Long> categoryIds;
}
