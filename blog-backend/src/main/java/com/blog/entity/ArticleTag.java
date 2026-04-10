package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文章-标签关联实体（article_tags 表）
 *
 * @author blog
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("article_tags")
public class ArticleTag {

    /** 文章 ID */
    private Long articleId;

    /** 标签 ID */
    private Long tagId;
}
