package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data @Entity @Table(name = "menus")
public class Menu {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(unique = true, length = 100)
    private String code;  // nullable for pure nav menus
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuType type;
    @Column(length = 200)
    private String path;
    @Column(length = 200)
    private String component;
    @Column(length = 50)
    private String icon;
    private Long parentId;
    @Column(nullable = false)
    private Integer sortOrder = 0;
    @Column(nullable = false)
    private Integer status = 1;

    public enum MenuType { MENU, BUTTON, API }
}
