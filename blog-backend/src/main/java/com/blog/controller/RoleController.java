package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.role.RoleDTO;
import com.blog.dto.role.RoleVO;
import com.blog.entity.Role;
import com.blog.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<List<RoleVO>> list() {
        return Result.success(roleService.listAllVO());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<RoleVO> create(@Valid @RequestBody RoleDTO dto) {
        return Result.success(roleService.createRole(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<RoleVO> update(@PathVariable Long id, @Valid @RequestBody RoleDTO dto) {
        return Result.success(roleService.updateRole(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('role:manage')")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }
}
