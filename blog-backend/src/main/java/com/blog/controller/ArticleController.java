package com.blog.controller;

import com.blog.common.*;
import com.blog.dto.article.*;
import com.blog.entity.Article;
import com.blog.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 文章控制器
 * <p>提供文章的增删改查、发布状态切换等接口，
 * 前台接口无需认证，管理端接口需要相应权限码。</p>
 *
 * @author blog
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * 分页查询已发布文章（前台）
     * <p>GET /api/articles?page=1&size=10&categoryId=1&tagSlug=java</p>
     *
     * @param page       页码，默认 1
     * @param size       每页数量，默认 10
     * @param categoryId 按分类过滤，可选
     * @param tagSlug    按标签 slug 过滤，可选
     * @return 分页文章列表
     */
    @GetMapping
    public Result<PageResult<ArticleListResponse>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String tagSlug) {
        return Result.success(articleService.listPublished(page, size, categoryId, tagSlug));
    }

    /**
     * 分页查询所有文章（管理端），需要 article:list 权限
     * <p>GET /api/articles/admin/list?page=1&size=10</p>
     *
     * @param page 页码，默认 1
     * @param size 每页数量，默认 10
     * @return 所有文章分页列表（含草稿）
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasAuthority('article:list')")
    public Result<PageResult<ArticleListResponse>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long tagId) {
        return Result.success(articleService.listAll(page, size, keyword, status, categoryId, tagId));
    }

    /**
     * 根据 slug 查询文章详情（前台）
     * <p>GET /api/articles/{slug}</p>
     *
     * @param slug 文章 URL slug
     * @return 文章详情（含内容、作者、标签等）
     */
    @GetMapping("/{slug}")
    public Result<ArticleDetailResponse> detail(@PathVariable String slug) {
        return Result.success(articleService.getBySlug(slug));
    }

    /**
     * 获取上一篇/下一篇文章
     * <p>GET /api/articles/{slug}/neighbors</p>
     *
     * @param slug 文章 URL slug
     * @return 邻篇导航信息（prev/next）
     */
    @GetMapping("/{slug}/neighbors")
    public Result<ArticleNeighborsResponse> neighbors(@PathVariable String slug) {
        return Result.success(articleService.getNeighbors(slug));
    }

    /**
     * 根据 ID 查询文章（管理端编辑用）
     * GET /api/articles/admin/{id}
     */
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasAuthority('article:update')")
    public Result<ArticleDetailResponse> adminDetail(@PathVariable Long id) {
        return Result.success(articleService.getByIdForAdmin(id));
    }

    /**
     * 创建文章，需要 article:create 权限
     * <p>POST /api/articles</p>
     *
     * @param req         文章创建请求体
     * @param userDetails 当前登录用户（自动注入）
     * @return 创建成功的文章实体
     */
    @PostMapping
    @PreAuthorize("hasAuthority('article:create')")
    public Result<Article> create(@Valid @RequestBody ArticleCreateRequest req,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(articleService.create(req, userDetails.getUsername()));
    }

    /**
     * 更新文章，需要 article:update 权限
     * <p>PUT /api/articles/{id}</p>
     *
     * @param id  文章 ID
     * @param req 文章更新请求体（字段可选）
     * @return 更新后的文章实体
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('article:update')")
    public Result<Article> update(@PathVariable Long id, @RequestBody ArticleUpdateRequest req) {
        return Result.success(articleService.update(id, req));
    }

    /**
     * 切换文章发布状态，需要 article:publish 权限
     * <p>PUT /api/articles/{id}/publish</p>
     *
     * @param id 文章 ID
     * @return 操作成功响应
     */
    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('article:publish')")
    public Result<Void> publish(@PathVariable Long id) {
        articleService.togglePublish(id);
        return Result.success();
    }

    /**
     * 删除文章，需要 article:delete 权限
     * <p>DELETE /api/articles/{id}</p>
     *
     * @param id 文章 ID
     * @return 操作成功响应
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('article:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return Result.success();
    }

    /**
     * 批量删除文章。
     */
    @PostMapping("/batch-delete")
    @PreAuthorize("hasAuthority('article:delete')")
    public Result<java.util.Map<String, Integer>> batchDelete(
            @Valid @RequestBody com.blog.dto.common.BatchIdsDTO dto) {
        int deleted = articleService.batchDelete(dto.getIds());
        return Result.success(java.util.Map.of("deleted", deleted));
    }

    @GetMapping("/search")
    public Result<PageResult<ArticleListResponse>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(articleService.search(q.trim(), page, size));
    }

    @PostMapping("/{id}/like")
    public Result<Integer> like(@PathVariable Long id, HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
        return Result.success(articleService.like(id, ip));
    }
}
