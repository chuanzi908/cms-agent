package com.ylf.cms.catalog.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 分类扁平VO（M3 对外数据契约，M2可使用）
 */
@Data
public class CategoryFlatVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String name;

    private Long parentId;

    /** 0-启用 1-停用 */
    private Integer status;
}
