package com.blog.dto.tag;

import lombok.Data;

@Data
public class TagVO {
    private Long id;
    private String name;
    private String slug;
    private Long articleCount;
}
