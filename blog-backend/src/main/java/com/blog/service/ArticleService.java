package com.blog.service;

import com.blog.common.PageResult;
import com.blog.dto.article.*;
import com.blog.entity.Article;

public interface ArticleService {
    Article create(ArticleCreateRequest request, String authorUsername);
    Article update(Long id, ArticleUpdateRequest request);
    void delete(Long id);
    void togglePublish(Long id);
    ArticleDetailResponse getBySlug(String slug);
    PageResult<ArticleListResponse> listPublished(int page, int size, Long categoryId, String tagSlug);
    PageResult<ArticleListResponse> listAll(int page, int size);
}
