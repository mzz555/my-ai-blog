package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 文章标签实体，对应数据库 tags 表
 *
 * @author blog
 * @since 1.0.0
 */
@Data
@TableName("tags")
public class Tag {

    /** 标签 ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名称，唯一，长度最多 30 */
    private String name;

    /** 标签 URL slug，唯一，用于 SEO 友好的 URL */
    private String slug;
}
