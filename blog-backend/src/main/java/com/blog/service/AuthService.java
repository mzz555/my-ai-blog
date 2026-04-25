package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.dto.auth.*;
import com.blog.dto.auth.ProfileUpdateDTO;
import com.blog.entity.User;

/**
 * 认证服务接口
 * <p>提供用户登录、注册、Token 刷新及当前用户信息查询功能。</p>
 *
 * @author blog
 * @since 1.0.0
 */
public interface AuthService extends IService<User> {

    /**
     * 用户登录
     *
     * @param request 登录请求（用户名 + 密码）
     * @return 包含 accessToken 和 refreshToken 的响应
     */
    AuthResponse login(LoginRequest request);

    /**
     * 用户注册，自动分配 USER 角色
     *
     * @param request 注册请求（用户名 + 邮箱 + 密码）
     */
    void register(RegisterRequest request);

    /**
     * 查询当前登录用户的详细信息（含角色、权限、菜单）
     *
     * @param username 当前用户名（从 JWT 中解析）
     * @return 用户信息 DTO
     */
    UserInfoResponse getCurrentUser(String username);

    /**
     * 刷新 Access Token
     *
     * @param refreshToken 有效的 Refresh Token
     * @return 包含新 accessToken 的响应
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * 更新当前用户个人资料
     *
     * @param username 当前用户名（从 JWT 中解析）
     * @param dto      更新内容
     * @return 更新后的用户信息 DTO
     */
    UserInfoResponse updateProfile(String username, ProfileUpdateDTO dto);
}
