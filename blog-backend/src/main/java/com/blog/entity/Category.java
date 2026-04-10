package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 文章分类实体，对应数据库 categories 表
 *
 * @author blog
 * @since 1.0.0
 */
@Data
@TableName("categories")
public class Category {

    /** 分类 ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 分类名称 */
    private String name;

    /** 分类 URL slug，唯一，用于 SEO 友好的 URL */
    private String slug;

    /** 分类描述 */
    private String description;

    /** 排序序号，数字越小越靠前 */
    private Integer sortOrder = 0;
}
