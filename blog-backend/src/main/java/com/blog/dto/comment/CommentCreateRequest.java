package com.blog.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentCreateRequest {
    @NotBlank(message = "评论内容不能为空") @Size(max = 2000) private String content;
    private Long parentId;
    private String nickname;
    private String email;
}
