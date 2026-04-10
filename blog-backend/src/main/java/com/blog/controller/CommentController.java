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

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/api/articles/{articleId}/comments")
    public Result<List<CommentResponse>> list(@PathVariable Long articleId) {
        return Result.success(commentService.listApprovedByArticle(articleId));
    }

    @PostMapping("/api/articles/{articleId}/comments")
    public Result<Void> create(@PathVariable Long articleId,
                               @Valid @RequestBody CommentCreateRequest req,
                               @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : null;
        commentService.create(articleId, req, username);
        return Result.success();
    }

    @GetMapping("/api/comments/admin")
    @PreAuthorize("hasAuthority('comment:list')")
    public Result<PageResult<CommentResponse>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(commentService.listAllForAdmin(page, size));
    }

    @PutMapping("/api/comments/{id}/status")
    @PreAuthorize("hasAuthority('comment:approve')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam CommentStatus status) {
        commentService.updateStatus(id, status);
        return Result.success();
    }

    @DeleteMapping("/api/comments/{id}")
    @PreAuthorize("hasAuthority('comment:delete')")
    public Result<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.success();
    }
}
