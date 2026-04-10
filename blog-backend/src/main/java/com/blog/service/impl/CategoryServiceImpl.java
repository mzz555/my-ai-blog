package com.blog.service.impl;

import com.blog.dto.category.CategoryRequest;
import com.blog.entity.Category;
import com.blog.repository.CategoryRepository;
import com.blog.service.CategoryService;
import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final Slugify slugify = Slugify.builder().build();

    @Override
    public List<Category> listAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category create(CategoryRequest req) {
        String slug = resolveSlug(req.getSlug(), req.getName());
        if (categoryRepository.existsBySlug(slug))
            throw new IllegalArgumentException("分类 slug 已存在: " + slug);
        Category c = new Category();
        c.setName(req.getName());
        c.setSlug(slug);
        c.setDescription(req.getDescription());
        c.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        return categoryRepository.save(c);
    }

    @Override
    public Category update(Long id, CategoryRequest req) {
        Category c = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("分类不存在"));
        c.setName(req.getName());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        if (req.getSortOrder() != null) c.setSortOrder(req.getSortOrder());
        return categoryRepository.save(c);
    }

    @Override
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    private String resolveSlug(String slug, String name) {
        return (slug != null && !slug.isBlank()) ? slug : slugify.slugify(name);
    }
}
