package com.blog.controller;

import com.blog.common.*;
import com.blog.dto.article.*;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    public Result<PageResult<ArticleListResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tagSlug) {
        return Result.success(articleService.listPublished(page, size, categoryId, tagSlug));
    }

    @GetMapping("/admin/list")
    @PreAuthorize("hasAuthority('article:list')")
    public Result<PageResult<ArticleListResponse>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(articleService.listAll(page, size));
    }

    @GetMapping("/{slug}")
    public Result<ArticleDetailResponse> detail(@PathVariable String slug) {
        return Result.success(articleService.getBySlug(slug));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('article:create')")
    public Result<Article> create(@Valid @RequestBody ArticleCreateRequest req,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(articleService.create(req, userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('article:update')")
    public Result<Article> update(@PathVariable Long id, @RequestBody ArticleUpdateRequest req) {
        return Result.success(articleService.update(id, req));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('article:publish')")
    public Result<Void> publish(@PathVariable Long id) {
        articleService.togglePublish(id);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('article:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }
}
