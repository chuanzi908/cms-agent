package com.ylf.cms.content.controller;

import com.ylf.cms.common.Result;
import com.ylf.cms.common.annotation.GuideCard;
import com.ylf.cms.content.service.ArticleService;
import com.ylf.cms.content.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内容工作台（M2）
 * 统计数据跨模块：用户总数(M1)、分类总数(M3)、稿件各状态数(M2)
 */
@RestController
@RequestMapping("/api/content")
@RequiredArgsConstructor
public class DashboardController {

    private final ArticleService articleService;

    @GetMapping("/dashboard")
    @GuideCard(title = "内容工作台", code = "content-dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(articleService.dashboard());
    }
}
