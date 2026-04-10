package com.blog.repository;

import com.blog.entity.Article;
import com.blog.entity.Article.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findBySlugAndStatus(String slug, ArticleStatus status);
    Page<Article> findByStatus(ArticleStatus status, Pageable pageable);
    Page<Article> findByStatusAndCategoryId(ArticleStatus status, Long categoryId, Pageable pageable);

    @Query("SELECT a FROM Article a JOIN a.tags t WHERE a.status = 'PUBLISHED' AND t.slug = :tagSlug")
    Page<Article> findPublishedByTagSlug(String tagSlug, Pageable pageable);

    @Modifying
    @Query("UPDATE Article a SET a.viewCount = a.viewCount + :count WHERE a.id = :id")
    void incrementViewCount(Long id, int count);
}
