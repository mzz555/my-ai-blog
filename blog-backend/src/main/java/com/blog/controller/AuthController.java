package com.blog.controller;

import com.blog.common.Result;
import com.blog.dto.auth.*;
import com.blog.dto.auth.ProfileUpdateDTO;
import com.blog.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>提供用户登录、注册、Token 刷新及当前用户信息查询接口。</p>
 *
 * @author blog
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * <p>POST /api/auth/login</p>
     *
     * @param request 登录凭证（用户名 + 密码）
     * @return 包含 accessToken 和 refreshToken 的响应
     */
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(authService.login(request));
    }

    /**
     * 用户注册
     * <p>POST /api/auth/register</p>
     *
     * @param request 注册信息（用户名 + 邮箱 + 密码）
     * @return 注册成功响应
     */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.success();
    }

    /**
     * 获取当前登录用户信息（含角色、权限、菜单）
     * <p>GET /api/auth/me（需 Bearer Token）</p>
     *
     * @param userDetails Spring Security 注入的当前用户
     * @return 用户详细信息
     */
    @GetMapping("/me")
    public Result<UserInfoResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(authService.getCurrentUser(userDetails.getUsername()));
    }

    /**
     * 使用 Refresh Token 获取新的 Access Token
     * <p>POST /api/auth/refresh（Token 通过请求体传递，避免 URL 日志泄露）</p>
     *
     * @param request 包含 refreshToken 的请求体
     * @return 包含新 accessToken 的响应
     */
    @PostMapping("/refresh")
    public Result<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return Result.success(authService.refreshToken(request.getRefreshToken()));
    }

    @PatchMapping("/profile")
    public Result<UserInfoResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateDTO dto) {
        return Result.success(authService.updateProfile(userDetails.getUsername(), dto));
    }
}
