package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 菜单/权限数据访问层
 *
 * @author blog
 * @since 1.0.0
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 查询用户关联的所有菜单（通过 user_roles → role_menus → menus 联查）
     *
     * @param userId 用户 ID
     * @return 菜单列表（去重）
     */
    @Select("SELECT DISTINCT m.* FROM menus m " +
            "JOIN role_menus rm ON m.id = rm.menu_id " +
            "JOIN user_roles ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} " +
            "ORDER BY m.sort_order ASC")
    List<Menu> selectByUserId(@Param("userId") Long userId);
}
