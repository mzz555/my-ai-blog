package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.util.List;

/**
 * 菜单/权限实体，对应数据库 menus 表
 * <p>用于 RBAC 权限体系，支持三种类型：导航菜单（MENU）、按钮权限（BUTTON）、API 权限（API）。</p>
 *
 * @author blog
 * @since 1.0.0
 */
@Data
@TableName("menus")
public class Menu {

    /** 菜单 ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 菜单名称 */
    private String name;

    /** 权限唯一编码，如 article:create，纯导航菜单可为 null */
    private String code;

    /** 菜单类型：MENU=导航菜单，BUTTON=按钮权限，API=接口权限 */
    private MenuType type;

    /** 前端路由路径 */
    private String path;

    /** 前端组件路径 */
    private String component;

    /** 菜单图标 */
    private String icon;

    /** 父级菜单 ID，顶级菜单为 null */
    private Long parentId;

    /** 排序序号 */
    private Integer sortOrder = 0;

    /** 状态：1=启用，0=禁用 */
    private Integer status = 1;

    /** 子菜单列表（非数据库字段，用于树形结构） */
    @TableField(exist = false)
    private List<Menu> children;

    /**
     * 菜单类型枚举
     */
    public enum MenuType {
        /** 导航菜单 */
        MENU,
        /** 按钮权限 */
        BUTTON,
        /** API 接口权限 */
        API
    }
}
