package com.blog.service;

import com.blog.dto.auth.*;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    void register(RegisterRequest request);
    UserInfoResponse getCurrentUser(String username);
    AuthResponse refreshToken(String refreshToken);
}
