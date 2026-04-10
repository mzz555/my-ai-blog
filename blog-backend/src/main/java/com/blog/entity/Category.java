package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "categories")
public class Category {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false, unique = true, length = 50)
    private String slug;
    @Column(length = 200)
    private String description;
    @Column(nullable = false)
    private Integer sortOrder = 0;
}
