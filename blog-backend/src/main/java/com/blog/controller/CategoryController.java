package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.category.CategoryRequest;
import com.blog.entity.Category;
import com.blog.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.listAll());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('category:manage')")
    public Result<Category> create(@Valid @RequestBody CategoryRequest req) {
        return Result.success(categoryService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('category:manage')")
    public Result<Category> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        return Result.success(categoryService.update(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
