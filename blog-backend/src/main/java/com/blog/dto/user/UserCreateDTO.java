package com.blog.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * 后台创建用户的入参 DTO
 *
 * @author blog
 * @since 1.0.0
 */
@Data
public class UserCreateDTO {

    @NotBlank
    @Size(min = 3, max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "用户名只能包含字母、数字、下划线、连字符")
    private String username;

    @NotBlank
    @Email
    @Size(max = 100)
    private String email;

    @NotBlank
    @Size(min = 6, max = 50)
    private String password;

    @Size(max = 50)
    private String nickname;

    private List<Long> roleIds = new ArrayList<>();

    private Integer status;
}
