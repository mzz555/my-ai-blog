package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.dto.auth.*;
import com.blog.entity.*;
import com.blog.mapper.*;
import com.blog.security.JwtUtil;
import com.blog.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 认证服务实现
 *
 * @author blog
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl extends ServiceImpl<UserMapper, User> implements AuthService {

    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final UserRoleMapper userRoleMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        User user = this.getOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername()));
        if (user == null) throw new IllegalArgumentException("用户不存在");
        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(user.getId(), user.getUsername()))
                .refreshToken(jwtUtil.generateRefreshToken(user.getId(), user.getUsername()))
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (this.count(Wrappers.<User>lambdaQuery().eq(User::getUsername, request.getUsername())) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (this.count(Wrappers.<User>lambdaQuery().eq(User::getEmail, request.getEmail())) > 0) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        this.save(user);

        // 分配 USER 角色
        Role userRole = roleMapper.selectOne(
                Wrappers.<Role>lambdaQuery().eq(Role::getCode, "USER"));
        if (userRole == null) throw new IllegalStateException("USER 角色不存在，请检查初始化数据");
        userRoleMapper.insert(new UserRole(user.getId(), userRole.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser(String username) {
        User user = this.getOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null) throw new IllegalArgumentException("用户不存在");

        List<Role> roles = roleMapper.selectByUserId(user.getId());
        List<Menu> menus = menuMapper.selectByUserId(user.getId());

        List<String> roleCodes = roles.stream().map(Role::getCode).toList();
        List<String> permissions = menus.stream()
                .map(Menu::getCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<UserInfoResponse.MenuNode> menuNodes = menus.stream()
                .filter(m -> m.getStatus() == 1)
                .map(m -> UserInfoResponse.MenuNode.builder()
                        .id(m.getId()).name(m.getName()).code(m.getCode())
                        .type(m.getType().name()).path(m.getPath())
                        .component(m.getComponent()).icon(m.getIcon())
                        .parentId(m.getParentId()).sortOrder(m.getSortOrder())
                        .build())
                .collect(Collectors.toList());

        return UserInfoResponse.builder()
                .id(user.getId()).username(user.getUsername())
                .email(user.getEmail()).avatar(user.getAvatar())
                .bio(user.getBio()).roles(roleCodes)
                .permissions(permissions).menus(menuNodes)
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken))
            throw new IllegalArgumentException("refresh token 无效或已过期");
        String username = jwtUtil.getUsername(refreshToken);
        Long userId = jwtUtil.getUserId(refreshToken);
        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(userId, username))
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .build();
    }
}
