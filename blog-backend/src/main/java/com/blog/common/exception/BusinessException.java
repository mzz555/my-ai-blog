package com.blog.common.exception;

/**
 * 业务异常基类
 * <p>所有可预期的业务错误（参数校验失败、资源不存在等）应抛出此类或其子类，
 * 由 {@link com.blog.common.GlobalExceptionHandler} 统一捕获并返回给前端。</p>
 *
 * @author blog
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    /** HTTP 状态码 */
    private final int code;

    /**
     * 构造业务异常
     *
     * @param code    HTTP 状态码（如 400、404、403）
     * @param message 错误描述，将直接返回给前端
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取 HTTP 状态码
     *
     * @return HTTP 状态码
     */
    public int getCode() {
        return code;
    }
}
