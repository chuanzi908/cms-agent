package com.ylf.cms.content.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 新增文章入参（M2）
 */
@Data
public class ArticleSaveDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "正文不能为空")
    private String content;

    @NotNull(message = "分类不能为空")
    private Long categoryId;
}
