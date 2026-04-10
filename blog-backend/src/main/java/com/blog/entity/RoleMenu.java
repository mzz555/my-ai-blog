package com.blog.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色-菜单关联实体（role_menus 表）
 *
 * @author blog
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("role_menus")
public class RoleMenu {

    /** 角色 ID */
    private Long roleId;

    /** 菜单 ID */
    private Long menuId;
}
