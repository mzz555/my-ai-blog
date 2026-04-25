package com.blog.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class RoleDTO {
    @NotBlank(message = "角色名不能为空")
    @Size(max = 50)
    private String name;

    @Size(max = 200)
    private String description;

    private List<String> permissionCodes = new ArrayList<>();
}
