package com.blog.dto.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TagRequest {
    @NotBlank(message = "标签名不能为空") @Size(max = 30) private String name;
    @Size(max = 30) private String slug;
}
