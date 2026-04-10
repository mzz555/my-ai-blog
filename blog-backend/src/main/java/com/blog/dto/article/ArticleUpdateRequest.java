package com.blog.dto.article;

import lombok.Data;
import java.util.List;

@Data
public class ArticleUpdateRequest {
    private String title;
    private String summary;
    private String content;
    private String coverImage;
    private Long categoryId;
    private List<String> tagNames;
    private Boolean isTop;
    private Boolean allowComment;
}
