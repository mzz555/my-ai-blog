package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.PageResult;
import com.blog.common.exception.NotFoundException;
import com.blog.dto.user.UserCreateDTO;
import com.blog.dto.user.UserUpdateDTO;
import com.blog.entity.Role;
import com.blog.entity.User;
import com.blog.entity.UserRole;
import com.blog.mapper.RoleMapper;
import com.blog.mapper.UserMapper;
import com.blog.mapper.UserRoleMapper;
import com.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public PageResult<User> listUsers(int page, int size, String keyword) {
        var wrapper = Wrappers.<User>lambdaQuery()
                .like(keyword != null && !keyword.isBlank(), User::getUsername, keyword)
                .orderByDesc(User::getCreatedAt);
        Page<User> pageData = this.page(new Page<>(page, size), wrapper);
        pageData.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.of(pageData.getRecords(), pageData.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, int status) {
        User user = this.getById(id);
        if (user == null) throw new NotFoundException("用户不存在");
        user.setStatus(status);
        this.updateById(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, UserUpdateDTO dto) {
        User user = this.getById(id);
        if (user == null) throw new NotFoundException("用户不存在");
        if (dto.getNickname() != null) user.setNickname(dto.getNickname());
        this.updateById(user);
        userRoleMapper.deleteByUserId(id);
        if (dto.getRoleIds() != null) {
            dto.getRoleIds().forEach(roleId -> userRoleMapper.insert(new UserRole(id, roleId)));
        }
        List<Role> roles = roleMapper.selectList(
            Wrappers.<Role>lambdaQuery().inSql(Role::getId,
                "SELECT role_id FROM user_roles WHERE user_id = " + id));
        user.setRoles(roles);
        return user;
    }

    @Override
    @Transactional
    public User createUser(UserCreateDTO dto) {
        if (this.count(Wrappers.<User>lambdaQuery().eq(User::getUsername, dto.getUsername())) > 0) {
            throw new IllegalArgumentException("用户名已存在");
        }
        if (this.count(Wrappers.<User>lambdaQuery().eq(User::getEmail, dto.getEmail())) > 0) {
            throw new IllegalArgumentException("邮箱已被注册");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(
            dto.getNickname() == null || dto.getNickname().isBlank()
                ? dto.getUsername()
                : dto.getNickname()
        );
        user.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        this.save(user);

        if (dto.getRoleIds() != null && !dto.getRoleIds().isEmpty()) {
            for (Long roleId : dto.getRoleIds()) {
                userRoleMapper.insert(new UserRole(user.getId(), roleId));
            }
        }

        user.setPassword(null);
        return user;
    }
}
