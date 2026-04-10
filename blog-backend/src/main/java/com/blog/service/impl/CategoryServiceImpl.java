package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.exception.NotFoundException;
import com.blog.dto.category.CategoryRequest;
import com.blog.entity.Category;
import com.blog.mapper.CategoryMapper;
import com.blog.service.CategoryService;
import com.github.slugify.Slugify;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 文章分类服务实现
 *
 * @author blog
 * @since 1.0.0
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    private final Slugify slugify = Slugify.builder().build();

    @Override
    public List<Category> listAll() {
        return this.list(Wrappers.<Category>lambdaQuery().orderByAsc(Category::getSortOrder));
    }

    @Override
    public Category create(CategoryRequest req) {
        String slug = resolveSlug(req.getSlug(), req.getName());
        if (this.count(Wrappers.<Category>lambdaQuery().eq(Category::getSlug, slug)) > 0) {
            throw new IllegalArgumentException("分类 slug 已存在: " + slug);
        }
        Category c = new Category();
        c.setName(req.getName());
        c.setSlug(slug);
        c.setDescription(req.getDescription());
        c.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        this.save(c);
        return c;
    }

    @Override
    public Category update(Long id, CategoryRequest req) {
        Category c = this.getById(id);
        if (c == null) throw new NotFoundException("分类不存在");
        c.setName(req.getName());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        if (req.getSortOrder() != null) c.setSortOrder(req.getSortOrder());
        this.updateById(c);
        return c;
    }

    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    /**
     * 根据提供的 slug 或分类名称生成 URL slug
     */
    private String resolveSlug(String slug, String name) {
        return (slug != null && !slug.isBlank()) ? slug : slugify.slugify(name);
    }
}
