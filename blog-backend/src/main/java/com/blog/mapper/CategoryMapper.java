package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章分类数据访问层
 *
 * @author blog
 * @since 1.0.0
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
