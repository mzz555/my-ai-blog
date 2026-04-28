package com.blog.dto.auth;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateDTO {
    @Size(max = 500, message = "简介不能超过 500 字")
    private String bio;

    @Size(max = 500, message = "头像地址过长")
    private String avatar;
}
