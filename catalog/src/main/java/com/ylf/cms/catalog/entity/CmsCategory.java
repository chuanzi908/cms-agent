package com.ylf.cms.catalog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文章分类（M3 内部实体，禁止被其他模块import）
 */
@Data
@TableName("cms_category")
public class CmsCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 父分类ID，0 表示一级分类 */
    private Long parentId;

    /** 0-启用 1-停用 */
    private Integer status;

    private Integer sort;

    @TableLogic
    private Integer deleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
