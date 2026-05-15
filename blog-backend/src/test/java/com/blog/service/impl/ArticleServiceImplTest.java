package com.blog.service.impl;

import com.blog.entity.Article;
import com.blog.mapper.ArticleMapper;
import com.blog.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class ArticleServiceImplTest {

    @Autowired ArticleService articleService;
    @Autowired ArticleMapper articleMapper;

    @Test
    void batchDelete_withValidIds_shouldDeleteAll() {
        Long id1 = createTestArticle("批删测试 1");
        Long id2 = createTestArticle("批删测试 2");
        Long id3 = createTestArticle("批删测试 3");

        int deleted = articleService.batchDelete(Arrays.asList(id1, id2, id3));

        assertEquals(3, deleted);
        assertNull(articleMapper.selectById(id1));
        assertNull(articleMapper.selectById(id2));
        assertNull(articleMapper.selectById(id3));
    }

    @Test
    void batchDelete_withNonexistentIds_shouldReturnZero() {
        int deleted = articleService.batchDelete(Arrays.asList(999_999_999L, 999_999_998L));
        assertEquals(0, deleted);
    }

    @Test
    void batchDelete_withMixedIds_shouldDeleteExistingOnly() {
        Long realId = createTestArticle("混合 id 测试");

        int deleted = articleService.batchDelete(Arrays.asList(realId, 999_999_999L));

        assertEquals(1, deleted);
        assertNull(articleMapper.selectById(realId));
    }

    private Long createTestArticle(String title) {
        Article a = new Article();
        a.setTitle(title);
        a.setSlug(title.replace(" ", "-").toLowerCase() + "-" + System.nanoTime());
        a.setStatus(Article.ArticleStatus.DRAFT);   // <-- 枚举！不是 String
        a.setContent("test body");
        a.setAuthorId(1L);                          // admin from data.sql
        articleMapper.insert(a);
        return a.getId();
    }
}
