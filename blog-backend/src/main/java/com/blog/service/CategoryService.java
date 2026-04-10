package com.blog.service;

import com.blog.dto.category.CategoryRequest;
import com.blog.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> listAll();
    Category create(CategoryRequest request);
    Category update(Long id, CategoryRequest request);
    void delete(Long id);
}
