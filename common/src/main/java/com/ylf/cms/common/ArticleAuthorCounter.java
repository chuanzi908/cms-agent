package com.ylf.cms.common;

/**
 * 内容使用量查询接口（公共层，M1维护）
 *
 * 作用：M1 删除账号前守卫校验账号下是否有稿件，避免 security 与 content 模块产生循环依赖。
 * M2 的 ArticleService 实现本接口，M1 通过 Spring 容器按类型获取实现。
 */
public interface ArticleAuthorCounter {

    /**
     * 统计某作者名下未删除的文章数
     *
     * @param authorId 作者用户ID
     * @return 文章数
     */
    long countArticlesByAuthor(Long authorId);
}
