package com.blog.service.impl;

import com.blog.dto.user.UserCreateDTO;
import com.blog.entity.User;
import com.blog.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceImplTest {

    @Autowired UserService userService;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void createUser_success_returnsUserWithoutPasswordAndAssignsRoles() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("testuser_" + System.currentTimeMillis());
        dto.setEmail("test_" + System.currentTimeMillis() + "@example.com");
        dto.setPassword("secret123");
        dto.setNickname("测试用户");
        dto.setRoleIds(List.of(2L));  // USER 角色（V2 种子数据）
        dto.setStatus(1);

        User created = userService.createUser(dto);

        assertNotNull(created.getId());
        assertEquals(dto.getUsername(), created.getUsername());
        assertEquals("测试用户", created.getNickname());
        assertNull(created.getPassword(), "返回值的 password 字段必须置空");

        // 直接查 DB 校验密码已加密
        User fromDb = userService.getById(created.getId());
        assertNotNull(fromDb.getPassword());
        assertNotEquals("secret123", fromDb.getPassword(), "密码必须已加密");
        assertTrue(passwordEncoder.matches("secret123", fromDb.getPassword()));
    }

    @Test
    void createUser_duplicateUsername_throwsIllegalArgument() {
        // 先建一个
        String username = "dup_" + System.currentTimeMillis();
        UserCreateDTO first = new UserCreateDTO();
        first.setUsername(username);
        first.setEmail("first_" + System.currentTimeMillis() + "@example.com");
        first.setPassword("secret123");
        userService.createUser(first);

        // 再建同名
        UserCreateDTO dup = new UserCreateDTO();
        dup.setUsername(username);
        dup.setEmail("second_" + System.currentTimeMillis() + "@example.com");
        dup.setPassword("secret123");

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(dup)
        );
        assertTrue(ex.getMessage().contains("用户名"));
    }

    @Test
    void createUser_duplicateEmail_throwsIllegalArgument() {
        String email = "dupemail_" + System.currentTimeMillis() + "@example.com";
        UserCreateDTO first = new UserCreateDTO();
        first.setUsername("u1_" + System.currentTimeMillis());
        first.setEmail(email);
        first.setPassword("secret123");
        userService.createUser(first);

        UserCreateDTO dup = new UserCreateDTO();
        dup.setUsername("u2_" + System.currentTimeMillis());
        dup.setEmail(email);
        dup.setPassword("secret123");

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(dup)
        );
        assertTrue(ex.getMessage().contains("邮箱"));
    }
}
