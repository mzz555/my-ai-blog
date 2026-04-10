package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.category.CategoryRequest;
import com.blog.entity.Category;
import java.util.List;

/**
 * 文章分类服务接口
 *
 * @author blog
 * @since 1.0.0
 */
public interface CategoryService extends IService<Category> {

    /**
     * 查询所有分类列表（按 sortOrder 升序）
     *
     * @return 分类列表
     */
    List<Category> listAll();

    /**
     * 创建新分类
     *
     * @param request 分类创建请求
     * @return 创建后的分类实体
     */
    Category create(CategoryRequest request);

    /**
     * 更新分类信息
     *
     * @param id      分类 ID
     * @param request 分类更新请求
     * @return 更新后的分类实体
     */
    Category update(Long id, CategoryRequest request);

    /**
     * 删除分类
     *
     * @param id 分类 ID
     */
    void delete(Long id);
}
