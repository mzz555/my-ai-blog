package com.blog.dto.menu;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class MenuVO {
    private Long id;
    private String name;
    private String path;
    private String icon;
    private Integer sort;
    private Boolean visible;
    private Long parentId;
    private List<MenuVO> children;
}
