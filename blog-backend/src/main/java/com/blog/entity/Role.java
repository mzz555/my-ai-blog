package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.List;

/**
 * 角色实体，对应数据库 roles 表
 * <p>用于 RBAC 权限体系中的角色定义，每个角色可关联多个菜单/权限节点。</p>
 *
 * @author blog
 * @since 1.0.0
 */
@Data
@TableName("roles")
public class Role {

    /** 角色 ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色名称，如"管理员"、"普通用户" */
    private String name;

    /** 角色唯一编码，如 ADMIN、USER */
    private String code;

    /** 角色描述 */
    private String description;

    /** 状态：1=启用，0=禁用 */
    private Integer status = 1;

    /** 排序序号 */
    private Integer sortOrder = 0;

    /** 角色关联的菜单列表，非数据库字段，由 Service 层通过 role_menus 表手动填充 */
    @TableField(exist = false)
    private List<Menu> menus;
}
