package com.ylf.cms.content.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章实体（M2 内部，禁止被其他模块import）
 */
@Data
@TableName("cms_article")
public class CmsArticle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String content;

    /** 作者用户ID（关联 sys_user.id） */
    private Long authorId;

    /** 分类ID（关联 cms_category.id） */
    private Long categoryId;

    /** 状态：0-草稿 1-待审核 2-已发布 3-已下架 */
    private Integer status;

    /** 乐观锁版本号（并发编辑控制） */
    @Version
    private Integer version;

    /** 逻辑删除标记：0-未删除 1-已删除 */
    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
