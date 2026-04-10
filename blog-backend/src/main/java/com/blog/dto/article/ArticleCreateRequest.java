package com.blog.dto.article;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class ArticleCreateRequest {
    @NotBlank(message = "标题不能为空") @Size(max = 200) private String title;
    @Size(max = 200) private String slug;
    @Size(max = 500) private String summary;
    @NotBlank(message = "内容不能为空") private String content;
    private String coverImage;
    private String status = "DRAFT";
    private Long categoryId;
    private List<String> tagNames;
    private Boolean isTop = false;
    private Boolean allowComment = true;
}
