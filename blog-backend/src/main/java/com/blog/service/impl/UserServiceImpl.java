package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.PageResult;
import com.blog.common.exception.NotFoundException;
import com.blog.entity.User;
import com.blog.mapper.UserMapper;
import com.blog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

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
}
