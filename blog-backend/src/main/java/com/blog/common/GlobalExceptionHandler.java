package com.blog.common;

import com.blog.common.exception.BusinessException;
import com.blog.common.exception.ForbiddenException;
import com.blog.common.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>统一捕获并格式化各类异常，返回标准的 {@link Result} 响应体，
 * 避免在 Controller 层散落 try-catch 代码。</p>
 *
 * @author blog
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理请求参数校验失败（@Valid 注解触发），返回 400
     *
     * @param ex 参数校验异常，包含所有字段的错误信息
     * @return 包含第一个字段错误描述的响应
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidation(MethodArgumentNotValidException ex) {
        FieldError field = ex.getBindingResult().getFieldErrors().get(0);
        return Result.error(400, field.getField() + ": " + field.getDefaultMessage());
    }

    /**
     * 处理资源不存在异常，返回 404
     *
     * @param ex 资源不存在异常
     * @return 404 响应
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNotFound(NotFoundException ex) {
        return Result.error(404, ex.getMessage());
    }

    /**
     * 处理业务层禁止操作异常，返回 403
     *
     * @param ex 禁止操作异常
     * @return 403 响应
     */
    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleForbidden(ForbiddenException ex) {
        return Result.error(403, ex.getMessage());
    }

    /**
     * 处理通用业务异常（除 404、403 之外），默认返回 400
     *
     * @param ex 业务异常
     * @return 包含业务错误码和消息的响应
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusiness(BusinessException ex) {
        return Result.error(ex.getCode(), ex.getMessage());
    }

    /**
     * 处理非法参数异常，返回 400
     *
     * @param ex 非法参数异常
     * @return 400 响应
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return Result.error(400, ex.getMessage());
    }

    /**
     * 处理 Spring Security 权限拒绝异常，返回 403
     *
     * @param ex Spring Security 抛出的权限拒绝异常
     * @return 403 响应
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDenied(AccessDeniedException ex) {
        return Result.error(403, "没有权限");
    }

    /**
     * 兜底处理所有未捕获的异常，返回 500
     *
     * @param ex 未处理的异常
     * @return 500 响应
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception ex) {
        log.error("未处理异常", ex);
        return Result.error(500, "服务器内部错误");
    }
}
