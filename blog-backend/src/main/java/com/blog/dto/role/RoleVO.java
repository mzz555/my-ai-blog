package com.blog.dto.role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class RoleVO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer status;
    private List<PermissionItem> permissions;

    @Data
    @AllArgsConstructor
    public static class PermissionItem {
        private Long id;
        private String code;
    }
}
