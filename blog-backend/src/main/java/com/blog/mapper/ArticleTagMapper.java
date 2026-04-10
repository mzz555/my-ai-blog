package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.ArticleTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 文章-标签关联数据访问层
 *
 * @author blog
 * @since 1.0.0
 */
@Mapper
public interface ArticleTagMapper extends BaseMapper<ArticleTag> {

    /**
     * 删除文章的所有标签关联（用于更新文章标签时先清空再重建）
     *
     * @param articleId 文章 ID
     */
    @Delete("DELETE FROM article_tags WHERE article_id = #{articleId}")
    void deleteByArticleId(@Param("articleId") Long articleId);
}
