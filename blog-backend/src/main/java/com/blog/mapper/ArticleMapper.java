package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

/**
 * 文章数据访问层
 *
 * @author blog
 * @since 1.0.0
 */
@Mapper
public interface ArticleMapper extends BaseMapper<Article> {

    /**
     * 根据 slug 和状态查询文章
     *
     * @param slug   文章 URL slug
     * @param status 状态字符串（如 "PUBLISHED"）
     * @return 匹配的文章，不存在时返回 null
     */
    @Select("SELECT * FROM articles WHERE slug = #{slug} AND status = #{status}")
    Article selectBySlugAndStatus(@Param("slug") String slug, @Param("status") String status);

    /**
     * 查询含有指定标签的所有已发布文章的 ID（用于按标签分页）
     *
     * @param tagSlug 标签 slug
     * @return 文章 ID 列表
     */
    @Select("SELECT a.id FROM articles a " +
            "JOIN article_tags at ON a.id = at.article_id " +
            "JOIN tags t ON at.tag_id = t.id " +
            "WHERE a.status = 'PUBLISHED' AND t.slug = #{tagSlug}")
    List<Long> selectIdsByTagSlug(@Param("tagSlug") String tagSlug);

    /**
     * 原子地增加文章浏览量（用于 Redis 定期同步）
     *
     * @param id    文章 ID
     * @param count 需要增加的浏览量
     */
    @Update("UPDATE articles SET view_count = view_count + #{count} WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id, @Param("count") int count);

    /**
     * 按状态统计文章数量（用于仪表盘概览）
     *
     * @param status 状态字符串（如 "PUBLISHED" 或 "DRAFT"）
     * @return 该状态的文章总数
     */
    @Select("SELECT COUNT(*) FROM articles WHERE status = #{status}")
    long countByStatus(@Param("status") String status);

    @Select("SELECT * FROM articles WHERE status = 'PUBLISHED' " +
            "AND (title LIKE CONCAT('%',#{keyword},'%') OR summary LIKE CONCAT('%',#{keyword},'%')) " +
            "ORDER BY is_top DESC, published_at DESC LIMIT #{offset}, #{size}")
    List<Article> searchPublished(@Param("keyword") String keyword,
                                  @Param("offset") int offset,
                                  @Param("size") int size);

    @Select("SELECT COUNT(*) FROM articles WHERE status = 'PUBLISHED' " +
            "AND (title LIKE CONCAT('%',#{keyword},'%') OR summary LIKE CONCAT('%',#{keyword},'%'))")
    long countSearchPublished(@Param("keyword") String keyword);

    @Update("UPDATE articles SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);
}
