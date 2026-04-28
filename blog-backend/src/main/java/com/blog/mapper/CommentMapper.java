package com.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.blog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;
import java.util.Map;

/**
 * 评论数据访问层
 *
 * @author blog
 * @since 1.0.0
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 查询文章的已审核评论，按创建时间升序（用于构建评论树）
     *
     * @param articleId 文章 ID
     * @param status    状态字符串（如 "APPROVED"）
     * @return 评论列表
     */
    @Select("SELECT * FROM comments WHERE article_id = #{articleId} AND status = #{status} " +
            "ORDER BY created_at ASC")
    List<Comment> selectApprovedByArticleId(@Param("articleId") Long articleId,
                                            @Param("status") String status);

    /**
     * 按状态统计评论数量（用于仪表盘概览）
     *
     * @param status 状态字符串（如 "PENDING"）
     * @return 该状态的评论总数
     */
    @Select("SELECT COUNT(*) FROM comments WHERE status = #{status}")
    long countByStatus(@Param("status") String status);

    @Select("SELECT DATE(created_at) AS date, COUNT(*) AS count " +
            "FROM comments " +
            "WHERE created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(created_at) ORDER BY date ASC")
    List<Map<String, Object>> countByDayLast7();
}
