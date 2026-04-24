package com.blog.controller;

import com.blog.common.PageResult;
import com.blog.common.Result;
import com.blog.entity.User;
import com.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAuthority('user:list')")
    public Result<PageResult<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return Result.success(userService.listUsers(page, size, keyword));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('user:list')")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam int status) {
        userService.updateStatus(id, status);
        return Result.success();
    }
}
