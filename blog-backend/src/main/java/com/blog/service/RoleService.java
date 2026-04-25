package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.role.RoleDTO;
import com.blog.dto.role.RoleVO;
import com.blog.entity.Role;
import java.util.List;

public interface RoleService extends IService<Role> {
    List<Role> listAll();
    List<RoleVO> listAllVO();
    RoleVO createRole(RoleDTO dto);
    RoleVO updateRole(Long id, RoleDTO dto);
    void deleteRole(Long id);
}
