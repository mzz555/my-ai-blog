package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.common.PageResult;
import com.blog.dto.article.*;
import com.blog.entity.Article;

/**
 * 文章服务接口
 * <p>提供文章的创建、编辑、发布、查询等业务功能。</p>
 *
 * @author blog
 * @since 1.0.0
 */
public interface ArticleService extends IService<Article> {

    /**
     * 创建新文章
     *
     * @param request        文章创建请求体
     * @param authorUsername 当前登录用户名
     * @return 保存后的文章实体
     */
    Article create(ArticleCreateRequest request, String authorUsername);

    /**
     * 更新文章内容
     *
     * @param id      文章 ID
     * @param request 文章更新请求体
     * @return 更新后的文章实体
     */
    Article update(Long id, ArticleUpdateRequest request);

    /**
     * 删除文章（同时清除关联标签数据）
     *
     * @param id 文章 ID
     */
    void delete(Long id);

    /**
     * 切换文章发布状态（草稿 ↔ 已发布）
     *
     * @param id 文章 ID
     */
    void togglePublish(Long id);

    /**
     * 根据 slug 查询已发布文章详情
     *
     * @param slug 文章 URL slug
     * @return 文章详情 DTO
     */
    ArticleDetailResponse getBySlug(String slug);

    /**
     * 分页查询已发布文章（支持按分类和标签过滤）
     *
     * @param page       页码，从 1 开始
     * @param size       每页数量
     * @param categoryId 分类 ID，为 null 时不过滤
     * @param tagSlug    标签 slug，为 null 时不过滤
     * @return 分页结果
     */
    PageResult<ArticleListResponse> listPublished(int page, int size, Long categoryId, String tagSlug);

    /**
     * 分页查询所有文章（管理端）
     *
     * @param page 页码，从 1 开始
     * @param size 每页数量
     * @return 分页结果
     */
    PageResult<ArticleListResponse> listAll(int page, int size);

    PageResult<ArticleListResponse> search(String keyword, int page, int size);

    int like(Long id, String clientIp);
}
