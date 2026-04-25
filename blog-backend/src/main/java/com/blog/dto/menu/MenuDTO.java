package com.blog.dto.menu;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MenuDTO {
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50)
    private String name;

    @Size(max = 200)
    private String path;

    @Size(max = 100)
    private String icon;

    private Integer sort = 0;

    private Boolean visible = true;

    private Long parentId;
}
