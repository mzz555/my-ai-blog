package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文章实体，对应数据库 articles 表
 * <p>author、category、tags 为非数据库字段，查询后由 Service 层手动填充。</p>
 *
 * @author blog
 * @since 1.0.0
 */
@Data
@TableName("articles")
public class Article {

    /** 文章 ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 文章标题，长度最多 200 */
    private String title;

    /** URL slug，全局唯一，用于 SEO 友好的 URL */
    private String slug;

    /** 文章摘要，长度最多 500 */
    private String summary;

    /** 文章正文，Markdown 格式 */
    private String content;

    /** 封面图 URL */
    private String coverImage;

    /** 发布状态：DRAFT=草稿，PUBLISHED=已发布 */
    private ArticleStatus status = ArticleStatus.DRAFT;

    /** 浏览量，由 Redis 定期同步 */
    private Integer viewCount = 0;

    /** 是否置顶 */
    private Boolean isTop = false;

    /** 是否允许评论 */
    private Boolean allowComment = true;

    /** 作者用户 ID（FK: users.id） */
    private Long authorId;

    /** 所属分类 ID（FK: categories.id），可为 null */
    private Long categoryId;

    /** 发布时间，状态变为 PUBLISHED 时设置 */
    private LocalDateTime publishedAt;

    /** 创建时间，INSERT 时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 最后更新时间，INSERT 和 UPDATE 时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 文章作者，非数据库字段，由 Service 层通过 authorId 查询填充 */
    @TableField(exist = false)
    private User author;

    /** 文章分类，非数据库字段，由 Service 层通过 categoryId 查询填充 */
    @TableField(exist = false)
    private Category category;

    /** 文章标签列表，非数据库字段，由 Service 层通过 article_tags 表查询填充 */
    @TableField(exist = false)
    private List<Tag> tags;

    /**
     * 文章发布状态枚举
     */
    public enum ArticleStatus {
        /** 草稿 */
        DRAFT,
        /** 已发布 */
        PUBLISHED
    }
}
