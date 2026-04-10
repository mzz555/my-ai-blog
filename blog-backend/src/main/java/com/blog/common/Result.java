package com.blog.common;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Result<T> {
    private final int code;
    private final String message;
    private final T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(200, null, data);
    }

    public static <T> Result<T> success() {
        return new Result<>(200, null, null);
    }

    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
}
