package com.ylf.cms.content.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章VO（M2 对外数据契约，供M3前台等模块使用，不含内部字段）
 */
@Data
public class ArticleVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String title;

    private String content;

    private Long authorId;

    /** 作者昵称（联查 sys_user 得出） */
    private String authorName;

    private Long categoryId;

    /** 分类名称（联查 cms_category 得出） */
    private String categoryName;

    /** 状态：0-草稿 1-待审核 2-已发布 3-已下架 */
    private Integer status;

    /** 乐观锁版本号 */
    private Integer version;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
