package com.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.common.PageResult;
import com.blog.common.exception.NotFoundException;
import com.blog.dto.comment.*;
import com.blog.entity.*;
import com.blog.entity.Comment.CommentStatus;
import com.blog.mapper.*;
import com.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论服务实现
 *
 * @author blog
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public void create(Long articleId, CommentCreateRequest req, String username) {
        Article article = articleMapper.selectById(articleId);
        if (article == null) throw new NotFoundException("文章不存在");
        if (!article.getAllowComment()) throw new IllegalArgumentException("该文章已关闭评论");

        Comment c = new Comment();
        c.setContent(req.getContent());
        c.setArticleId(articleId);
        c.setParentId(req.getParentId());

        if (username != null) {
            User user = userMapper.selectOne(
                    Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
            if (user != null) c.setUserId(user.getId());
        } else {
            c.setNickname(req.getNickname());
            c.setEmail(req.getEmail());
        }
        this.save(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> listApprovedByArticle(Long articleId) {
        List<Comment> comments = this.baseMapper.selectApprovedByArticleId(
                articleId, CommentStatus.APPROVED.name());
        fillUsers(comments);
        return buildTree(comments);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CommentResponse> listAllForAdmin(int page, int size) {
        Page<Comment> pageData = this.page(
                new Page<>(page, size),
                Wrappers.<Comment>lambdaQuery().orderByDesc(Comment::getCreatedAt)
        );
        fillUsers(pageData.getRecords());
        List<CommentResponse> list = pageData.getRecords().stream()
                .map(this::toResponse)
                .toList();
        return PageResult.of(list, pageData.getTotal(), page, size);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, CommentStatus status) {
        Comment c = this.getById(id);
        if (c == null) throw new NotFoundException("评论不存在");
        c.setStatus(status);
        this.updateById(c);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        this.removeById(id);
    }

    /**
     * 批量填充评论的用户信息，避免 N+1 查询
     *
     * @param comments 评论列表
     */
    private void fillUsers(List<Comment> comments) {
        List<Long> userIds = comments.stream()
                .map(Comment::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (userIds.isEmpty()) return;
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        comments.forEach(c -> c.setUser(userMap.get(c.getUserId())));
    }

    /**
     * 将评论列表构建为树形结构（楼中楼）
     *
     * @param all 已按时间升序排列的评论列表
     * @return 顶级评论列表，子评论嵌套在 children 字段中
     */
    private List<CommentResponse> buildTree(List<Comment> all) {
        Map<Long, CommentResponse> map = new LinkedHashMap<>();
        all.forEach(c -> map.put(c.getId(), toResponse(c)));
        List<CommentResponse> roots = new ArrayList<>();
        map.values().forEach(r -> {
            if (r.getParentId() == null) {
                roots.add(r);
            } else if (map.containsKey(r.getParentId())) {
                CommentResponse parent = map.get(r.getParentId());
                if (parent.getChildren() == null) parent.setChildren(new ArrayList<>());
                parent.getChildren().add(r);
            }
        });
        return roots;
    }

    /**
     * 将评论实体转换为响应 DTO
     */
    private CommentResponse toResponse(Comment c) {
        CommentResponse r = new CommentResponse();
        r.setId(c.getId());
        r.setContent(c.getContent());
        r.setParentId(c.getParentId());
        r.setStatus(c.getStatus().name());
        r.setCreatedAt(c.getCreatedAt());
        if (c.getUser() != null) {
            r.setNickname(c.getUser().getUsername());
            r.setAvatar(c.getUser().getAvatar());
        } else {
            r.setNickname(c.getNickname());
        }
        return r;
    }
}
