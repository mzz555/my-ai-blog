package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data @Entity @Table(name = "articles")
public class Article {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 200)
    private String title;
    @Column(nullable = false, unique = true, length = 200)
    private String slug;
    @Column(length = 500)
    private String summary;
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;
    @Column(length = 255)
    private String coverImage;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ArticleStatus status = ArticleStatus.DRAFT;
    @Column(nullable = false)
    private Integer viewCount = 0;
    @Column(nullable = false)
    private Boolean isTop = false;
    @Column(nullable = false)
    private Boolean allowComment = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "article_tags",
        joinColumns = @JoinColumn(name = "article_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags;

    private LocalDateTime publishedAt;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist void prePersist() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate  void preUpdate()  { updatedAt = LocalDateTime.now(); }

    public enum ArticleStatus { DRAFT, PUBLISHED }
}
