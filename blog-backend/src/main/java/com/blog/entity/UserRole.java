package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户-角色关联实体（user_roles 表）
 *
 * @author blog
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_roles")
public class UserRole {

    /** 用户 ID */
    private Long userId;

    /** 角色 ID */
    private Long roleId;
}
