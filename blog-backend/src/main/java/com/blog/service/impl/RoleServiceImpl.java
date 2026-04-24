package com.blog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.entity.Role;
import com.blog.mapper.RoleMapper;
import com.blog.service.RoleService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    public List<Role> listAll() {
        return this.list();
    }
}
