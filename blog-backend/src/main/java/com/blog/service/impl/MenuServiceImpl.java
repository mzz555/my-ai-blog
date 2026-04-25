package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.exception.NotFoundException;
import com.blog.dto.menu.MenuDTO;
import com.blog.dto.menu.MenuVO;
import com.blog.entity.Menu;
import com.blog.mapper.MenuMapper;
import com.blog.service.MenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl extends ServiceImpl<MenuMapper, Menu> implements MenuService {

    @Override
    public List<MenuVO> listTree() {
        List<Menu> all = this.list(
            Wrappers.<Menu>lambdaQuery().orderByAsc(Menu::getSortOrder));
        return buildTree(all, null);
    }

    private List<MenuVO> buildTree(List<Menu> all, Long parentId) {
        return all.stream()
            .filter(m -> Objects.equals(m.getParentId(), parentId))
            .map(m -> MenuVO.builder()
                .id(m.getId())
                .name(m.getName())
                .path(m.getPath())
                .icon(m.getIcon())
                .sort(m.getSortOrder())
                .visible(m.getStatus() != null && m.getStatus() == 1)
                .parentId(m.getParentId())
                .children(buildTree(all, m.getId()))
                .build())
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MenuVO create(MenuDTO dto) {
        Menu menu = toEntity(dto);
        this.save(menu);
        return toVO(menu);
    }

    @Override
    @Transactional
    public MenuVO update(Long id, MenuDTO dto) {
        Menu menu = this.getById(id);
        if (menu == null) throw new NotFoundException("菜单不存在");
        menu.setName(dto.getName());
        menu.setPath(dto.getPath());
        menu.setIcon(dto.getIcon());
        menu.setSortOrder(dto.getSort() != null ? dto.getSort() : 0);
        menu.setStatus(Boolean.TRUE.equals(dto.getVisible()) ? 1 : 0);
        menu.setParentId(dto.getParentId());
        this.updateById(menu);
        return toVO(menu);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        deleteRecursive(id);
    }

    private void deleteRecursive(Long id) {
        List<Menu> children = this.list(
            Wrappers.<Menu>lambdaQuery().eq(Menu::getParentId, id));
        children.forEach(c -> deleteRecursive(c.getId()));
        this.removeById(id);
    }

    private Menu toEntity(MenuDTO dto) {
        Menu menu = new Menu();
        menu.setName(dto.getName());
        menu.setPath(dto.getPath());
        menu.setIcon(dto.getIcon());
        menu.setSortOrder(dto.getSort() != null ? dto.getSort() : 0);
        menu.setStatus(Boolean.TRUE.equals(dto.getVisible()) ? 1 : 0);
        menu.setParentId(dto.getParentId());
        menu.setType(Menu.MenuType.MENU);
        return menu;
    }

    private MenuVO toVO(Menu menu) {
        return MenuVO.builder()
            .id(menu.getId())
            .name(menu.getName())
            .path(menu.getPath())
            .icon(menu.getIcon())
            .sort(menu.getSortOrder())
            .visible(menu.getStatus() != null && menu.getStatus() == 1)
            .parentId(menu.getParentId())
            .children(Collections.emptyList())
            .build();
    }
}
