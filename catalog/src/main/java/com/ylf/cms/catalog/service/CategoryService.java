package com.ylf.cms.catalog.service;

import com.ylf.cms.catalog.vo.CategoryFlatVO;

import java.util.List;

/**
 * 分类服务（M3 对外契约）
 * M2 只能调用本接口与 CategoryFlatVO，禁止访问M3的mapper/entity/controller
 */
public interface CategoryService {

    /**
     * 全部分类（扁平），M2工作台统计分类总数使用
     */
    List<CategoryFlatVO> listFlat();

    /**
     * 校验分类存在且启用，否则抛异常（M2新建文章时调用）
     */
    void requireEnabled(Long categoryId);

    /**
     * 获取分类及其所有子分类ID（含自身），M2按分类筛选文章时调用
     */
    List<Long> descendantIds(Long categoryId);
}
