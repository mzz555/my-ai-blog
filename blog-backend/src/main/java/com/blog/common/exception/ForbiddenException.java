package com.blog.common.exception;

/**
 * 无权操作异常（HTTP 403）
 * <p>当用户尝试操作其无权访问的资源时抛出，
 * 通常用于业务层的权限二次校验（非 Spring Security 注解校验场景）。</p>
 *
 * @author blog
 * @since 1.0.0
 */
public class ForbiddenException extends BusinessException {

    /**
     * 构造无权操作异常，消息固定为"没有权限"
     */
    public ForbiddenException() {
        super(403, "没有权限");
    }

    /**
     * 构造带自定义消息的无权操作异常
     *
     * @param message 权限错误描述
     */
    public ForbiddenException(String message) {
        super(403, message);
    }
}
