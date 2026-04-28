package com.blog.dto.article;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleListResponse {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String coverImage;
    private Integer viewCount;
    private Boolean isTop;
    private LocalDateTime publishedAt;
    private Long categoryId;
    private String categoryName;
    private List<String> tagNames;
    private String authorName;
    private String status;
}
