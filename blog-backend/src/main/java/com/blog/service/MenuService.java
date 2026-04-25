package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.menu.MenuDTO;
import com.blog.dto.menu.MenuVO;
import com.blog.entity.Menu;
import java.util.List;

public interface MenuService extends IService<Menu> {
    List<MenuVO> listTree();
    MenuVO create(MenuDTO dto);
    MenuVO update(Long id, MenuDTO dto);
    void delete(Long id);
}
