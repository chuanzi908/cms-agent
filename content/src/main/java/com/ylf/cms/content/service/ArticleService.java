package com.ylf.cms.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ylf.cms.content.dto.ArticleQueryDTO;
import com.ylf.cms.content.dto.ArticleSaveDTO;
import com.ylf.cms.content.dto.ArticleUpdateDTO;
import com.ylf.cms.content.vo.ArticleVO;
import com.ylf.cms.content.vo.DashboardVO;

/**
 * 文章服务（M2 对外契约）
 * 其他模块（M1删除账号守卫、M3前台）只能调用本接口与VO，禁止访问M2的mapper/entity
 */
public interface ArticleService {

    /** 新增文章（默认草稿状态） */
    ArticleVO create(ArticleSaveDTO dto);

    /** 修改文章（乐观锁，版本冲突抛40900） */
    ArticleVO update(ArticleUpdateDTO dto);

    /** 逻辑删除文章 */
    void delete(Long id);

    /** 文章详情 */
    ArticleVO getById(Long id);

    /** 分页条件查询（无N+1，一次联查带出作者昵称/分类名称） */
    Page<ArticleVO> page(ArticleQueryDTO query);

    /** 提交审核：草稿 -> 待审核 */
    void submitAudit(Long id);

    /** 审核通过：待审核 -> 已发布（草稿直接审核通过抛40901） */
    void passAudit(Long id);

    /** 审核驳回：待审核 -> 草稿 */
    void rejectAudit(Long id);

    /** 下架：已发布 -> 已下架 */
    void offline(Long id);

    /**
     * 统计某作者名下未删除的文章数（M1删除账号守卫调用，count>0时M1抛40903）
     */
    long countByAuthor(Long authorId);

    /** 工作台统计（跨模块调用M1/M3） */
    DashboardVO dashboard();
}
