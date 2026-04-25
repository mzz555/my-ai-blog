package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.exception.NotFoundException;
import com.blog.dto.role.RoleDTO;
import com.blog.dto.role.RoleVO;
import com.blog.entity.Menu;
import com.blog.entity.Role;
import com.blog.entity.RoleMenu;
import com.blog.mapper.MenuMapper;
import com.blog.mapper.RoleMapper;
import com.blog.mapper.RoleMenuMapper;
import com.blog.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Autowired
    private MenuMapper menuMapper;

    @Autowired
    private RoleMenuMapper roleMenuMapper;

    @Override
    public List<Role> listAll() {
        return this.list();
    }

    @Override
    public List<RoleVO> listAllVO() {
        return this.list().stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RoleVO createRole(RoleDTO dto) {
        Role role = new Role();
        role.setName(dto.getName());
        role.setCode(dto.getName().toUpperCase().replace(" ", "_"));
        role.setDescription(dto.getDescription());
        this.save(role);
        assignPermissions(role.getId(), dto.getPermissionCodes());
        return toVO(role);
    }

    @Override
    @Transactional
    public RoleVO updateRole(Long id, RoleDTO dto) {
        Role role = this.getById(id);
        if (role == null) throw new NotFoundException("角色不存在");
        role.setName(dto.getName());
        role.setDescription(dto.getDescription());
        this.updateById(role);
        assignPermissions(id, dto.getPermissionCodes());
        return toVO(role);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        roleMenuMapper.delete(Wrappers.<RoleMenu>lambdaQuery().eq(RoleMenu::getRoleId, id));
        this.removeById(id);
    }

    private void assignPermissions(Long roleId, List<String> codes) {
        roleMenuMapper.delete(Wrappers.<RoleMenu>lambdaQuery().eq(RoleMenu::getRoleId, roleId));
        if (codes == null || codes.isEmpty()) return;
        List<Menu> menus = menuMapper.selectList(
            Wrappers.<Menu>lambdaQuery().in(Menu::getCode, codes));
        menus.forEach(m -> roleMenuMapper.insert(new RoleMenu(roleId, m.getId())));
    }

    private RoleVO toVO(Role role) {
        List<Menu> menus = menuMapper.selectList(
            Wrappers.<Menu>lambdaQuery()
                .inSql(Menu::getId,
                    "SELECT menu_id FROM role_menus WHERE role_id = " + role.getId()));
        List<RoleVO.PermissionItem> permissions = menus.stream()
            .filter(m -> m.getCode() != null)
            .map(m -> new RoleVO.PermissionItem(m.getId(), m.getCode()))
            .collect(Collectors.toList());
        return RoleVO.builder()
            .id(role.getId())
            .name(role.getName())
            .code(role.getCode())
            .description(role.getDescription())
            .status(role.getStatus())
            .permissions(permissions)
            .build();
    }
}
