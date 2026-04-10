package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "tags")
public class Tag {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 30)
    private String name;
    @Column(nullable = false, unique = true, length = 30)
    private String slug;
}
