package com.blog.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "分类名不能为空") @Size(max = 50) private String name;
    @Size(max = 50) private String slug;
    @Size(max = 200) private String description;
    private Integer sortOrder = 0;
}
