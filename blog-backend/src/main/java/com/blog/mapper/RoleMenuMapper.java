package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.RoleMenu;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-菜单关联数据访问层
 *
 * @author blog
 * @since 1.0.0
 */
@Mapper
public interface RoleMenuMapper extends BaseMapper<RoleMenu> {
}
