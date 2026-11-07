package com.ylf.cms.security.controller;

import com.ylf.cms.common.ArticleAuthorCounter;
import com.ylf.cms.common.BusinessException;
import com.ylf.cms.common.Result;
import com.ylf.cms.common.ResultCode;
import com.ylf.cms.common.annotation.RequirePermission;
import com.ylf.cms.security.entity.SysUser;
import com.ylf.cms.security.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口（M1 契约版）
 *
 * 验收用例3：删除账号时通过 common 层 ArticleAuthorCounter（由M2实现）
 * 校验账号下是否有稿件，有则返回 40903，实现 M1-M2 跨模块契约且不产生模块循环依赖。
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserAdminController {

    private final SysUserMapper sysUserMapper;

    /** M2 通过 common 接口提供的文章计数能力（容器中唯一实现为 M2 的 ArticleServiceImpl） */
    private final ObjectProvider<ArticleAuthorCounter> articleAuthorCounterProvider;

    /** 删除账号（守卫：账号下有稿件则拒绝） */
    @DeleteMapping("/{id}")
    @RequirePermission("user:delete")
    public Result<Void> deleteUser(@PathVariable("id") Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            return Result.error(ResultCode.ACCOUNT_NOT_FOUND);
        }
        ArticleAuthorCounter counter = articleAuthorCounterProvider.getIfAvailable();
        if (counter != null && counter.countArticlesByAuthor(id) > 0) {
            // 验收用例3：账号下有稿件，返回40903
            throw new BusinessException(ResultCode.ACCOUNT_HAS_ARTICLES);
        }
        sysUserMapper.deleteById(id);
        return Result.success();
    }
}
