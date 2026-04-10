package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Set;

@Data @Entity @Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    @Column(nullable = false)
    private String password;
    private String avatar;
    @Column(columnDefinition = "TEXT")
    private String bio;
    @Column(nullable = false)
    private Integer status = 1;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @PrePersist
    void prePersist() { createdAt = updatedAt = LocalDateTime.now(); }
    @PreUpdate
    void preUpdate() { updatedAt = LocalDateTime.now(); }
}
