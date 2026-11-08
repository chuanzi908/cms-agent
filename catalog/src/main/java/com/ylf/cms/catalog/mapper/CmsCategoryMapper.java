package com.ylf.cms.catalog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ylf.cms.catalog.entity.CmsCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分类Mapper（M3 内部，禁止被其他模块import）
 */
@Mapper
public interface CmsCategoryMapper extends BaseMapper<CmsCategory> {
}
