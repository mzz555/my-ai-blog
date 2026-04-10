package com.blog.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Spring Security 用户详情服务实现
 * <p>在认证过程中根据用户名加载用户信息，并通过 {@link UserMapper#selectPermissionsByUserId}
 * 查询该用户的所有权限码（如 article:create）作为 GrantedAuthority。</p>
 *
 * @author blog
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserMapper userMapper;

    /**
     * 根据用户名加载 Spring Security UserDetails
     *
     * @param username 用户名
     * @return Spring Security UserDetails 对象（含权限码）
     * @throws UsernameNotFoundException 用户不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.selectOne(
                Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
        if (user == null) throw new UsernameNotFoundException("用户不存在: " + username);

        List<String> permissions = userMapper.selectPermissionsByUserId(user.getId());
        Set<SimpleGrantedAuthority> authorities = permissions.stream()
                .filter(code -> code != null && !code.isBlank())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toSet());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(user.getStatus() == 0)
                .credentialsExpired(false)
                .build();
    }
}
