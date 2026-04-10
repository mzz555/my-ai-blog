package com.blog.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一 API 响应包装类
 * <p>所有 REST 接口均返回此格式，前端通过 {@code code} 字段判断成功（200）或失败（4xx/5xx）。</p>
 *
 * @param <T> 响应数据类型
 * @author blog
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T> {

    /** HTTP 业务状态码，200 = 成功 */
    private final int code;

    /** 错误消息，成功时为 null */
    private final String message;

    /** 响应数据，失败时为 null */
    private final T data;

    /**
     * 构造成功响应（含数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return code=200 的成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, null, data);
    }

    /**
     * 构造成功响应（无数据，用于操作类接口）
     *
     * @param <T> 数据类型（通常为 Void）
     * @return code=200 的成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(200, null, null);
    }

    /**
     * 构造错误响应
     *
     * @param code    HTTP 业务状态码（如 400、404、500）
     * @param message 错误描述
     * @param <T>     数据类型（Void）
     * @return 错误响应
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
