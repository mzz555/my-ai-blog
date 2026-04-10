package com.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 评论实体，对应数据库 comments 表
 * <p>支持游客评论（userId 为 null，使用 nickname/email 字段）和登录用户评论。</p>
 *
 * @author blog
 * @since 1.0.0
 */
@Data
@TableName("comments")
public class Comment {

    /** 评论 ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 评论内容 */
    private String content;

    /** 所属文章 ID（FK: articles.id） */
    private Long articleId;

    /** 评论用户 ID（FK: users.id），游客评论时为 null */
    private Long userId;

    /** 父评论 ID，用于楼中楼，顶级评论为 null */
    private Long parentId;

    /** 游客昵称，登录用户评论时无效 */
    private String nickname;

    /** 游客邮箱，登录用户评论时无效 */
    private String email;

    /** 审核状态：PENDING=待审核，APPROVED=已通过，REJECTED=已拒绝 */
    private CommentStatus status = CommentStatus.PENDING;

    /** 评论时间，INSERT 时自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 评论用户，非数据库字段，由 Service 层通过 userId 查询填充 */
    @TableField(exist = false)
    private User user;

    /**
     * 评论审核状态枚举
     */
    public enum CommentStatus {
        /** 待审核 */
        PENDING,
        /** 已通过 */
        APPROVED,
        /** 已拒绝 */
        REJECTED
    }
}
