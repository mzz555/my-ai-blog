package com.blog.dto.auth;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    private List<String> roles;
    private List<String> permissions;
    private List<MenuNode> menus;

    @Data
    @Builder
    public static class MenuNode {
        private Long id;
        private String name;
        private String code;
        private String type;
        private String path;
        private String component;
        private String icon;
        private Long parentId;
        private Integer sortOrder;
    }
}
