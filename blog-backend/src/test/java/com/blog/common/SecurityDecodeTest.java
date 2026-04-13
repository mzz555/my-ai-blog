package com.blog.common;


import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SecurityDecodeTest {

        @Test
        public void testNoOpPassword() {
            // 明文密码（不加密）
            NoOpPasswordEncoder encoder = (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
            String rawPassword = "123456";
            String encoded = encoder.encode(rawPassword);

            System.out.println("NoOp encoded password: " + encoded);
            assertEquals(rawPassword, encoded);
        }

        @Test
        public void testBCryptPassword() {
            // BCrypt 加密密码
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String rawPassword = "123456";
            String encoded = encoder.encode(rawPassword);

            System.out.println("BCrypt encoded password: " + encoded);

            // 匹配密码
            boolean matches = encoder.matches(rawPassword, encoded);
            System.out.println("BCrypt matches: " + matches);
            assertTrue(matches);
        }

        @Test
        public void testBasicAuthDecode() {
            // 模拟 Basic Auth header
            String basicAuthHeader = "YWRtaW46cGFzc3dvcmQ="; // admin:password
            byte[] decodedBytes = Base64.getDecoder().decode(basicAuthHeader);
            String decoded = new String(decodedBytes, StandardCharsets.UTF_8);

            System.out.println("Decoded Basic Auth: " + decoded);
            assertEquals("admin:password", decoded);
        }

}
