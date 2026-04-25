package com.blog.dto.user;

import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class UserUpdateDTO {
    @Size(max = 50)
    private String nickname;

    private List<Long> roleIds = new ArrayList<>();
}
