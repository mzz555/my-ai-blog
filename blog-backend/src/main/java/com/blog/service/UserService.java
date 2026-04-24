package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.common.PageResult;
import com.blog.entity.User;

public interface UserService extends IService<User> {
    PageResult<User> listUsers(int page, int size, String keyword);
    void updateStatus(Long id, int status);
}
