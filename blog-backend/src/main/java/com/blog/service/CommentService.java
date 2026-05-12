package com.blog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.common.PageResult;
import com.blog.dto.comment.*;
import com.blog.entity.Comment;
import com.blog.entity.Comment.CommentStatus;
import java.util.List;

/**
 * 评论服务接口
 * <p>提供评论提交、审核、删除及列表查询功能。</p>
 *
 * @author blog
 * @since 1.0.0
 */
public interface CommentService extends IService<Comment> {

    /**
     * 提交评论（登录用户或游客均可）
     *
     * @param articleId 目标文章 ID
     * @param request   评论内容请求体
     * @param username  当前登录用户名，游客为 null
     */
    void create(Long articleId, CommentCreateRequest request, String username);

    /**
     * 查询文章的已审核评论（树形结构）
     *
     * @param articleId 文章 ID
     * @return 顶级评论列表，子评论嵌套在 children 字段中
     */
    List<CommentResponse> listApprovedByArticle(Long articleId);

    /**
     * 分页查询所有评论（管理端）
     *
     * @param page   页码，从 1 开始
     * @param size   每页数量
     * @param status 状态过滤，null 时返回全部
     * @return 分页结果
     */
    PageResult<CommentResponse> listAllForAdmin(int page, int size, String status);

    /**
     * 更新评论审核状态
     *
     * @param id     评论 ID
     * @param status 新状态（APPROVED 或 REJECTED）
     */
    void updateStatus(Long id, CommentStatus status);

    /**
     * 删除评论
     *
     * @param id 评论 ID
     */
    void delete(Long id);

    /**
     * 批量删除评论。
     *
     * @param ids 评论 ID 列表
     * @return 实际删除的行数
     */
    int batchDelete(java.util.List<Long> ids);
}
