package com.ylf.cms.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 修改文章入参（M2）
 * 必须携带 id 与 version，乐观锁并发控制依赖 version
 */
@Data
public class ArticleUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "文章ID不能为空")
    private Long id;

    /** 乐观锁版本号，取自查询详情返回的version */
    @NotNull(message = "version不能为空")
    private Integer version;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "正文不能为空")
    private String content;

    @NotNull(message = "分类不能为空")
    private Long categoryId;
}
