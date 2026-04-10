package com.blog.service;

import com.blog.common.PageResult;
import com.blog.dto.comment.*;
import com.blog.entity.Comment.CommentStatus;
import java.util.List;

public interface CommentService {
    void create(Long articleId, CommentCreateRequest request, String username);
    List<CommentResponse> listApprovedByArticle(Long articleId);
    PageResult<CommentResponse> listAllForAdmin(int page, int size);
    void updateStatus(Long id, CommentStatus status);
    void delete(Long id);
}
