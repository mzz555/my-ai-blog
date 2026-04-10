package com.blog.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void generateAndValidateAccessToken() {
        String token = jwtUtil.generateAccessToken(1L, "testuser");
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(1L, jwtUtil.getUserId(token));
        assertEquals("testuser", jwtUtil.getUsername(token));
    }

    @Test
    void expiredTokenShouldBeInvalid() {
        String token = jwtUtil.generateTokenWithExpiry(1L, "testuser", -1000L);
        assertFalse(jwtUtil.validateToken(token));
    }
}
