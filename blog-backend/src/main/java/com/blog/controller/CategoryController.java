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

/**
 * 文章分类控制器
 * <p>分类列表公开查询，增删改需要 category:manage 权限。</p>
 *
 * @author blog
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 查询所有分类列表（前台 + 管理端共用）
     * <p>GET /api/categories</p>
     *
     * @return 分类列表，按 sortOrder 升序
     */
    @GetMapping
    public Result<List<Category>> list() {
        return Result.success(categoryService.listAll());
    }

    /**
     * 创建新分类，需要 category:manage 权限
     * <p>POST /api/categories</p>
     *
     * @param req 分类创建请求体
     * @return 创建成功的分类实体
     */
    @PostMapping
    @PreAuthorize("hasAuthority('category:manage')")
    public Result<Category> create(@Valid @RequestBody CategoryRequest req) {
        return Result.success(categoryService.create(req));
    }

    /**
     * 更新分类信息，需要 category:manage 权限
     * <p>PUT /api/categories/{id}</p>
     *
     * @param id  分类 ID
     * @param req 分类更新请求体
     * @return 更新后的分类实体
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('category:manage')")
    public Result<Category> update(@PathVariable Long id, @Valid @RequestBody CategoryRequest req) {
        return Result.success(categoryService.update(id, req));
    }

    /**
     * 删除分类，需要 category:manage 权限
     * <p>DELETE /api/categories/{id}</p>
     *
     * @param id 分类 ID
     * @return 操作成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('category:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }
}
