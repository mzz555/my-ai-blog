package com.blog.controller;

import com.blog.common.*;
import com.blog.dto.comment.*;
import com.blog.entity.Comment.CommentStatus;
import com.blog.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 评论控制器
 * <p>支持游客和登录用户提交评论；管理端支持审核和删除。</p>
 *
 * @author blog
 * @since 1.0.0
 */
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 查询文章的已审核评论列表（树形结构）
     * <p>GET /api/articles/{articleId}/comments</p>
     *
     * @param articleId 文章 ID
     * @return 顶级评论列表，子评论嵌套在 children 中
     */
    @GetMapping("/api/articles/{articleId}/comments")
    public Result<List<CommentResponse>> list(@PathVariable Long articleId) {
        return Result.success(commentService.listApprovedByArticle(articleId));
    }

    /**
     * 提交评论（登录用户或游客均可）
     * <p>POST /api/articles/{articleId}/comments</p>
     *
     * @param articleId   目标文章 ID
     * @param req         评论内容（游客需提供 nickname）
     * @param userDetails 当前登录用户，未登录时为 null
     * @return 操作成功响应
     */
    @PostMapping("/api/articles/{articleId}/comments")
    public Result<Void> create(@PathVariable Long articleId,
                               @Valid @RequestBody CommentCreateRequest req,
                               @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        commentService.create(articleId, req, username);
        return Result.success();
    }

    /**
     * 分页查询所有评论（管理端），需要 comment:list 权限
     * <p>GET /api/comments/admin?page=1&size=10</p>
     *
     * @param page 页码，默认 1
     * @param size 每页数量，默认 10
     * @return 评论分页列表
     */
    @GetMapping("/api/comments/admin")
    @PreAuthorize("hasAuthority('comment:list')")
    public Result<PageResult<CommentResponse>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.listAllForAdmin(page, size));
    }

    /**
     * 更新评论审核状态，需要 comment:approve 权限
     * <p>PUT /api/comments/{id}/status?status=APPROVED</p>
     *
     * @param id     评论 ID
     * @param status 目标状态（APPROVED 或 REJECTED）
     * @return 操作成功响应
     */
    @PutMapping("/api/comments/{id}/status")
    @PreAuthorize("hasAuthority('comment:approve')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam CommentStatus status) {
        commentService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除评论，需要 comment:delete 权限
     * <p>DELETE /api/comments/{id}</p>
     *
     * @param id 评论 ID
     * @return 操作成功响应
     */
    @DeleteMapping("/api/comments/{id}")
    @PreAuthorize("hasAuthority('comment:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success();
    }
}
