package com.blog.dto.article;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ArticleDetailResponse {
    private Long id;
    private String title;
    private String slug;
    private String summary;
    private String content;
    private String coverImage;
    private Integer viewCount;
    private Boolean isTop;
    private Boolean allowComment;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private String categoryName;
    private Long categoryId;
    private List<String> tagNames;
    private String authorName;
    private String authorAvatar;
}
