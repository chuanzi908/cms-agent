package com.ylf.cms.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ylf.cms.content.dto.ArticleQueryDTO;
import com.ylf.cms.content.entity.CmsArticle;
import com.ylf.cms.content.vo.ArticleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章Mapper（M2 内部，禁止被其他模块import）
 */
@Mapper
public interface CmsArticleMapper extends BaseMapper<CmsArticle> {

    /**
     * 单条详情联查（带作者昵称、分类名称）
     */
    ArticleVO selectVOById(@Param("id") Long id);

    /**
     * 分页数据查询（一次联查带出作者昵称、分类名称，杜绝N+1）
     */
    List<ArticleVO> selectPageVO(Page<ArticleVO> page, @Param("query") ArticleQueryDTO query);

    /**
     * 分页总数统计
     */
    Long selectPageVO_mpCount(@Param("query") ArticleQueryDTO query);
}
