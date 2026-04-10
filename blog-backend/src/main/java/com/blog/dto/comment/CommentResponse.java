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
    private String status;
    private LocalDateTime createdAt;
    private List<CommentResponse> children;
}
