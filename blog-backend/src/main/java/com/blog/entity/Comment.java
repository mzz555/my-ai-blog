package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data @Entity @Table(name = "comments")
public class Comment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;  // null = guest

    private Long parentId;
    @Column(length = 50)
    private String nickname;
    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommentStatus status = CommentStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }

    public enum CommentStatus { PENDING, APPROVED, REJECTED }
}
