package com.blog.common.exception;

/**
 * 资源不存在异常（HTTP 404）
 * <p>当请求的文章、用户、分类等资源在数据库中不存在时抛出。</p>
 *
 * @author blog
 * @since 1.0.0
 */
public class NotFoundException extends BusinessException {

    /**
     * 构造资源不存在异常
     *
     * @param message 资源描述，如"文章不存在"、"用户不存在"
     */
    public NotFoundException(String message) {
        super(404, message);
    }
}
