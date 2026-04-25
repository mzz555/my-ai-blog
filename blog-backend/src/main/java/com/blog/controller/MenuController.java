package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.menu.MenuDTO;
import com.blog.dto.menu.MenuVO;
import com.blog.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    @PreAuthorize("hasAuthority('menu:manage')")
    public Result<List<MenuVO>> list() {
        return Result.success(menuService.listTree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('menu:manage')")
    public Result<MenuVO> create(@Valid @RequestBody MenuDTO dto) {
        return Result.success(menuService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('menu:manage')")
    public Result<MenuVO> update(@PathVariable Long id, @Valid @RequestBody MenuDTO dto) {
        return Result.success(menuService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('menu:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        menuService.delete(id);
        return Result.success();
    }
}
