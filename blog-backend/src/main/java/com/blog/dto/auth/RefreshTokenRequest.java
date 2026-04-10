package com.blog.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 Token 请求体
 *
 * @author blog
 * @since 1.0.0
 */
@Data
public class RefreshTokenRequest {

    /** Refresh Token，不能为空 */
    @NotBlank(message = "refreshToken 不能为空")
    private String refreshToken;
}
