package com.ylf.cms.content.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ylf.cms.common.Result;
import com.ylf.cms.common.annotation.RequirePermission;
import com.ylf.cms.content.dto.ArticleQueryDTO;
import com.ylf.cms.content.dto.ArticleSaveDTO;
import com.ylf.cms.content.dto.ArticleUpdateDTO;
import com.ylf.cms.content.service.ArticleService;
import com.ylf.cms.content.vo.ArticleVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 文章接口（M2）
 */
@RestController
@RequestMapping("/api/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /** 新增文章（默认草稿） */
    @PostMapping
    @RequirePermission("article:create")
    public Result<ArticleVO> create(@Valid @RequestBody ArticleSaveDTO dto) {
        return Result.success(articleService.create(dto));
    }

    /** 修改文章（携带version，并发冲突返回40900） */
    @PutMapping
    @RequirePermission("article:update")
    public Result<ArticleVO> update(@Valid @RequestBody ArticleUpdateDTO dto) {
        return Result.success(articleService.update(dto));
    }

    /** 逻辑删除文章 */
    @DeleteMapping("/{id}")
    @RequirePermission("article:delete")
    public Result<Void> delete(@PathVariable("id") Long id) {
        articleService.delete(id);
        return Result.success();
    }

    /** 文章详情 */
    @GetMapping("/{id}")
    public Result<ArticleVO> detail(@PathVariable("id") Long id) {
        return Result.success(articleService.getById(id));
    }

    /** 分页列表（title/status/categoryId/authorId 筛选） */
    @GetMapping("/page")
    public Result<Page<ArticleVO>> page(ArticleQueryDTO query) {
        return Result.success(articleService.page(query));
    }

    /** 提交审核：草稿 -> 待审核 */
    @PostMapping("/{id}/submitAudit")
    @RequirePermission("article:audit")
    public Result<Void> submitAudit(@PathVariable("id") Long id) {
        articleService.submitAudit(id);
        return Result.success();
    }

    /** 审核通过：待审核 -> 已发布（草稿直接审核返回40901） */
    @PostMapping("/{id}/passAudit")
    @RequirePermission("article:audit")
    public Result<Void> passAudit(@PathVariable("id") Long id) {
        articleService.passAudit(id);
        return Result.success();
    }

    /** 审核驳回：待审核 -> 草稿 */
    @PostMapping("/{id}/rejectAudit")
    @RequirePermission("article:audit")
    public Result<Void> rejectAudit(@PathVariable("id") Long id) {
        articleService.rejectAudit(id);
        return Result.success();
    }

    /** 下架：已发布 -> 已下架 */
    @PostMapping("/{id}/offline")
    @RequirePermission("article:offline")
    public Result<Void> offline(@PathVariable("id") Long id) {
        articleService.offline(id);
        return Result.success();
    }
}
