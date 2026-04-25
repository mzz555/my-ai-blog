package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户实体，对应数据库 users 表
 *
 * @author blog
 * @since 1.0.0
 */
@Data
@TableName("users")
public class User {

    /** 用户 ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名，唯一，长度最多 50 */
    private String username;

    /** 邮箱，唯一，长度最多 100 */
    private String email;

    /** BCrypt 加密后的密码 */
    private String password;

    /** 头像 URL */
    private String avatar;

    /** 个人简介 */
    private String bio;

    /** 用户昵称 */
    private String nickname;

    /** 账号状态：1=正常，0=禁用 */
    private Integer status = 1;

    /** 创建时间，INSERT 时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 最后更新时间，INSERT 和 UPDATE 时自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /** 用户角色列表，非数据库字段，由 Service 层通过 user_roles 表手动填充 */
    @TableField(exist = false)
    private List<Role> roles;
}
