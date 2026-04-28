package com.blog.dto.comment;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentResponse {
    private Long id;
    private String content;
    private String nickname;
    private String avatar;
    private Long parentId;
    private String parentNickname;
    private String status;
    private LocalDateTime createdAt;
    private Long articleId;
    private String articleTitle;
    private String articleSlug;
    private List<CommentResponse> children;
}
