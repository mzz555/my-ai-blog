package com.blog.service.impl;

import com.blog.dto.auth.*;
import com.blog.entity.*;
import com.blog.repository.*;
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

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return AuthResponse.builder()
                .accessToken(jwtUtil.generateAccessToken(user.getId(), user.getUsername()))
                .refreshToken(jwtUtil.generateRefreshToken(user.getId(), user.getUsername()))
                .tokenType("Bearer")
                .build();
    }

    @Override
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername()))
            throw new IllegalArgumentException("用户名已存在");
        if (userRepository.existsByEmail(request.getEmail()))
            throw new IllegalArgumentException("邮箱已被注册");

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role userRole = roleRepository.findByCode("USER")
                .orElseThrow(() -> new IllegalStateException("USER 角色不存在，请检查初始化数据"));
        user.setRoles(Set.of(userRole));
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUser(String username) {
        User user = userRepository.findByUsernameWithRolesAndMenus(username)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        List<String> roles = user.getRoles().stream().map(Role::getCode).toList();
        List<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getMenus().stream())
                .map(Menu::getCode)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        List<UserInfoResponse.MenuNode> menus = user.getRoles().stream()
                .flatMap(r -> r.getMenus().stream())
                .filter(m -> m.getStatus() == 1)
                .distinct()
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
                .bio(user.getBio()).roles(roles)
                .permissions(permissions).menus(menus)
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
