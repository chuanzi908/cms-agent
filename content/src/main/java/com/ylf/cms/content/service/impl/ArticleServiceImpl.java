package com.ylf.cms.content.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ylf.cms.catalog.service.CategoryService;
import com.ylf.cms.common.ArticleAuthorCounter;
import com.ylf.cms.common.BusinessException;
import com.ylf.cms.common.ResultCode;
import com.ylf.cms.common.UserContext;
import com.ylf.cms.content.dto.ArticleQueryDTO;
import com.ylf.cms.content.dto.ArticleSaveDTO;
import com.ylf.cms.content.dto.ArticleUpdateDTO;
import com.ylf.cms.content.entity.CmsArticle;
import com.ylf.cms.content.enums.ArticleStatusEnum;
import com.ylf.cms.content.mapper.CmsArticleMapper;
import com.ylf.cms.content.service.ArticleService;
import com.ylf.cms.content.vo.ArticleVO;
import com.ylf.cms.content.vo.DashboardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 文章服务实现（M2）
 *
 * 跨模块边界（ModuleBoundaryTest强制）：
 *   - 只调用 M3 CategoryService 的 Service 接口
 *   - 禁止 import 其他模块的 mapper/entity/controller/config
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService, ArticleAuthorCounter {

    private final CmsArticleMapper articleMapper;
    private final CategoryService categoryService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO create(ArticleSaveDTO dto) {
        // 校验分类存在且启用（调用M3 Service，符合模块边界）
        categoryService.requireEnabled(dto.getCategoryId());

        Long authorId = UserContext.getUserId();
        if (authorId == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED);
        }
        CmsArticle article = new CmsArticle();
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setCategoryId(dto.getCategoryId());
        article.setAuthorId(authorId);
        article.setStatus(ArticleStatusEnum.DRAFT.getCode());
        article.setVersion(1);
        articleMapper.insert(article);
        return getById(article.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleVO update(ArticleUpdateDTO dto) {
        CmsArticle article = getExistingArticle(dto.getId());
        // 仅草稿、待审核可编辑
        int status = article.getStatus();
        if (status != ArticleStatusEnum.DRAFT.getCode() && status != ArticleStatusEnum.PENDING.getCode()) {
            throw new BusinessException(ResultCode.ILLEGAL_STATE, "当前状态不允许编辑");
        }
        categoryService.requireEnabled(dto.getCategoryId());

        CmsArticle update = new CmsArticle();
        update.setId(dto.getId());
        update.setTitle(dto.getTitle());
        update.setContent(dto.getContent());
        update.setCategoryId(dto.getCategoryId());
        update.setVersion(dto.getVersion());
        // MyBatis-Plus 乐观锁：UPDATE ... SET version = version + 1 WHERE id = ? AND version = ?
        int rows = articleMapper.updateById(update);
        if (rows == 0) {
            // 验收用例2：并发编辑冲突，返回40900
            throw new BusinessException(ResultCode.CONCURRENT_CONFLICT);
        }
        return getById(dto.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getExistingArticle(id);
        articleMapper.deleteById(id);
    }

    @Override
    public ArticleVO getById(Long id) {
        ArticleVO vo = articleMapper.selectVOById(id);
        if (vo == null) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        return vo;
    }

    @Override
    public Page<ArticleVO> page(ArticleQueryDTO query) {
        // 分类筛选：调用M3展开子分类ID（含自身）
        if (query.getCategoryId() != null) {
            List<Long> ids = categoryService.descendantIds(query.getCategoryId());
            query.setCategoryIds(ids);
        }
        Page<ArticleVO> page = new Page<>(query.getPageNum(), query.getPageSize());
        // 验收用例4：分页仅两条SQL（selectPageVO_mpCount 统计 + selectPageVO 数据），无N+1
        Long total = articleMapper.selectPageVO_mpCount(query);
        List<ArticleVO> records = articleMapper.selectPageVO(page, query);
        page.setTotal(total == null ? 0 : total);
        page.setRecords(records);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAudit(Long id) {
        CmsArticle article = getExistingArticle(id);
        if (article.getStatus() != ArticleStatusEnum.DRAFT.getCode()) {
            throw new BusinessException(ResultCode.ILLEGAL_STATE, "仅草稿状态可提交审核");
        }
        changeStatus(article, ArticleStatusEnum.PENDING);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void passAudit(Long id) {
        CmsArticle article = getExistingArticle(id);
        // 验收用例1：草稿直接审核通过 -> 40901
        if (article.getStatus() != ArticleStatusEnum.PENDING.getCode()) {
            throw new BusinessException(ResultCode.ILLEGAL_STATE, "仅待审核状态可通过审核");
        }
        changeStatus(article, ArticleStatusEnum.PUBLISHED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectAudit(Long id) {
        CmsArticle article = getExistingArticle(id);
        if (article.getStatus() != ArticleStatusEnum.PENDING.getCode()) {
            throw new BusinessException(ResultCode.ILLEGAL_STATE, "仅待审核状态可驳回");
        }
        changeStatus(article, ArticleStatusEnum.DRAFT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offline(Long id) {
        CmsArticle article = getExistingArticle(id);
        if (article.getStatus() != ArticleStatusEnum.PUBLISHED.getCode()) {
            throw new BusinessException(ResultCode.ILLEGAL_STATE, "仅已发布状态可下架");
        }
        changeStatus(article, ArticleStatusEnum.OFFLINE);
    }

    @Override
    public long countByAuthor(Long authorId) {
        if (authorId == null) {
            return 0L;
        }
        return articleMapper.selectCount(Wrappers.<CmsArticle>lambdaQuery()
                .eq(CmsArticle::getAuthorId, authorId)
                .eq(CmsArticle::getDeleted, 0));
    }

    /** ArticleAuthorCounter 接口实现（供M1删除账号守卫调用，逻辑同 countByAuthor） */
    @Override
    public long countArticlesByAuthor(Long authorId) {
        return countByAuthor(authorId);
    }

    @Override
    public DashboardVO dashboard() {
        DashboardVO vo = new DashboardVO();
        vo.setArticleTotal(countByStatus(null));
        vo.setDraftCount(countByStatus(ArticleStatusEnum.DRAFT.getCode()));
        vo.setPendingCount(countByStatus(ArticleStatusEnum.PENDING.getCode()));
        vo.setPublishedCount(countByStatus(ArticleStatusEnum.PUBLISHED.getCode()));
        vo.setOfflineCount(countByStatus(ArticleStatusEnum.OFFLINE.getCode()));
        return vo;
    }

    // ========== 内部方法 ==========

    private CmsArticle getExistingArticle(Long id) {
        CmsArticle article = articleMapper.selectById(id);
        if (article == null || Objects.equals(article.getDeleted(), 1)) {
            throw new BusinessException(ResultCode.ARTICLE_NOT_FOUND);
        }
        return article;
    }

    private void changeStatus(CmsArticle article, ArticleStatusEnum target) {
        CmsArticle update = new CmsArticle();
        update.setId(article.getId());
        update.setStatus(target.getCode());
        articleMapper.updateById(update);
    }

    private long countByStatus(Integer status) {
        return articleMapper.selectCount(Wrappers.<CmsArticle>lambdaQuery()
                .eq(CmsArticle::getDeleted, 0)
                .eq(status != null, CmsArticle::getStatus, status));
    }
}
