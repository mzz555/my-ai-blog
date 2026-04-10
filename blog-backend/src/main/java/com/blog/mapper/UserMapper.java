package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

/**
 * 用户数据访问层
 * <p>继承 {@link BaseMapper} 获得通用 CRUD，额外提供权限查询方法。</p>
 *
 * @author blog
 * @since 1.0.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 查询用户拥有的所有权限码（通过 user_roles → role_menus → menus 联查）
     *
     * @param userId 用户 ID
     * @return 权限码列表，如 ["article:create", "article:delete"]
     */
    @Select("SELECT m.code FROM menus m " +
            "JOIN role_menus rm ON m.id = rm.menu_id " +
            "JOIN user_roles ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.code IS NOT NULL AND m.code != ''")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);
}
