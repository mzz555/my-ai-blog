package com.blog.service.impl;

import com.blog.common.PageResult;
import com.blog.dto.comment.*;
import com.blog.entity.*;
import com.blog.entity.Comment.CommentStatus;
import com.blog.repository.*;
import com.blog.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void create(Long articleId, CommentCreateRequest req, String username) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        if (!article.getAllowComment()) throw new IllegalArgumentException("该文章已关闭评论");

        Comment c = new Comment();
        c.setContent(req.getContent());
        c.setArticle(article);
        c.setParentId(req.getParentId());

        if (username != null) {
            userRepository.findByUsername(username).ifPresent(c::setUser);
        } else {
            c.setNickname(req.getNickname());
            c.setEmail(req.getEmail());
        }
        commentRepository.save(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentResponse> listApprovedByArticle(Long articleId) {
        List<Comment> comments = commentRepository
                .findByArticleIdAndStatusOrderByCreatedAtAsc(articleId, CommentStatus.APPROVED);
        return buildTree(comments);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<CommentResponse> listAllForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").descending());
        Page<Comment> pageData = commentRepository.findAll(pageable);
        List<CommentResponse> list = pageData.getContent().stream().map(this::toResponse).toList();
        return PageResult.of(list, pageData.getTotalElements(), page, size);
    }

    @Override
    @Transactional
    public void updateStatus(Long id, CommentStatus status) {
        Comment c = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));
        c.setStatus(status);
        commentRepository.save(c);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        commentRepository.deleteById(id);
    }

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
