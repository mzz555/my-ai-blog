package com.blog.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Data @Entity @Table(name = "roles")
public class Role {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 50)
    private String name;
    @Column(nullable = false, unique = true, length = 50)
    private String code;
    @Column(length = 200)
    private String description;
    @Column(nullable = false)
    private Integer status = 1;
    @Column(nullable = false)
    private Integer sortOrder = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "role_menus",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "menu_id"))
    private Set<Menu> menus;
}
