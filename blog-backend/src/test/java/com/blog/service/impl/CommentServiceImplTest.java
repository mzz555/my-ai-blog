package com.blog.service.impl;

import com.blog.entity.Comment;
import com.blog.mapper.CommentMapper;
import com.blog.service.CommentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class CommentServiceImplTest {

    @Autowired CommentService commentService;
    @Autowired CommentMapper commentMapper;

    @Test
    void batchDelete_withValidIds_shouldDeleteAll() {
        Long id1 = createTestComment();
        Long id2 = createTestComment();

        int deleted = commentService.batchDelete(Arrays.asList(id1, id2));

        assertEquals(2, deleted);
        assertNull(commentMapper.selectById(id1));
        assertNull(commentMapper.selectById(id2));
    }

    @Test
    void batchDelete_withNonexistentIds_shouldReturnZero() {
        int deleted = commentService.batchDelete(Arrays.asList(999_999_999L));
        assertEquals(0, deleted);
    }

    @Test
    void batchUpdateStatus_toApproved_shouldUpdateAll() {
        Long id1 = createTestComment();
        Long id2 = createTestComment();

        int updated = commentService.batchUpdateStatus(
            Arrays.asList(id1, id2),
            Comment.CommentStatus.APPROVED
        );

        assertEquals(2, updated);
        assertEquals(Comment.CommentStatus.APPROVED, commentMapper.selectById(id1).getStatus());
        assertEquals(Comment.CommentStatus.APPROVED, commentMapper.selectById(id2).getStatus());
    }

    @Test
    void batchUpdateStatus_toRejected_shouldUpdateAll() {
        Long id1 = createTestComment();

        int updated = commentService.batchUpdateStatus(
            Arrays.asList(id1),
            Comment.CommentStatus.REJECTED
        );

        assertEquals(1, updated);
        assertEquals(Comment.CommentStatus.REJECTED, commentMapper.selectById(id1).getStatus());
    }

    private Long createTestComment() {
        Comment c = new Comment();
        c.setArticleId(1L);
        c.setNickname("test_" + System.nanoTime());
        c.setContent("batch test");
        c.setStatus(Comment.CommentStatus.PENDING);
        commentMapper.insert(c);
        return c.getId();
    }
}
