package com.blog.controller;

import com.blog.common.Result;
import com.blog.entity.Role;
import com.blog.service.RoleService;
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
    public Result<List<Role>> list() {
        return Result.success(roleService.listAll());
    }
}
