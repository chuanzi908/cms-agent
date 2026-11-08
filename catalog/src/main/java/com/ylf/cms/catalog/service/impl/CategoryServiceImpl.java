package com.ylf.cms.catalog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ylf.cms.catalog.entity.CmsCategory;
import com.ylf.cms.catalog.mapper.CmsCategoryMapper;
import com.ylf.cms.catalog.service.CategoryService;
import com.ylf.cms.catalog.vo.CategoryFlatVO;
import com.ylf.cms.common.BusinessException;
import com.ylf.cms.common.ResultCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 分类服务实现（M3 契约版）
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CmsCategoryMapper categoryMapper;

    @Override
    public List<CategoryFlatVO> listFlat() {
        List<CmsCategory> list = categoryMapper.selectList(Wrappers.<CmsCategory>lambdaQuery()
                .eq(CmsCategory::getDeleted, 0)
                .orderByAsc(CmsCategory::getSort));
        return list.stream().map(c -> {
            CategoryFlatVO vo = new CategoryFlatVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setParentId(c.getParentId());
            vo.setStatus(c.getStatus());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void requireEnabled(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException(ResultCode.CATEGORY_DISABLED);
        }
        CmsCategory category = categoryMapper.selectById(categoryId);
        if (category == null || category.getDeleted() == 1) {
            throw new BusinessException(ResultCode.CATEGORY_NOT_FOUND);
        }
        if (Objects.equals(category.getStatus(), 1)) {
            throw new BusinessException(ResultCode.CATEGORY_DISABLED);
        }
    }

    @Override
    public List<Long> descendantIds(Long categoryId) {
        CmsCategory root = categoryMapper.selectById(categoryId);
        if (root == null || root.getDeleted() == 1) {
            return Collections.emptyList();
        }
        List<CmsCategory> all = categoryMapper.selectList(Wrappers.<CmsCategory>lambdaQuery()
                .eq(CmsCategory::getDeleted, 0));
        List<Long> result = new ArrayList<>();
        collect(root.getId(), all, result);
        return result;
    }

    private void collect(Long parentId, List<CmsCategory> all, List<Long> result) {
        result.add(parentId);
        for (CmsCategory c : all) {
            if (Objects.equals(c.getParentId(), parentId)) {
                collect(c.getId(), all, result);
            }
        }
    }
}
