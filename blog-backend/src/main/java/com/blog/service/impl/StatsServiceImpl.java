package com.blog.service.impl;

import com.blog.entity.Article.ArticleStatus;
import com.blog.entity.Comment.CommentStatus;
import com.blog.mapper.*;
import com.blog.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 统计服务实现
 * <p>管理文章浏览量：先写入 Redis，再定期（每 5 分钟）批量同步到数据库，
 * 避免高频写库导致的性能问题。同步使用 SCAN 替代 KEYS，防止 Redis 阻塞。</p>
 *
 * @author blog
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    private static final String VIEW_KEY_PREFIX = "article:view:";

    @Override
    public void incrementViewCount(Long articleId) {
        redisTemplate.opsForValue().increment(VIEW_KEY_PREFIX + articleId);
    }

    /**
     * 每 5 分钟将 Redis 中累积的浏览量批量同步到数据库。
     * 使用 SCAN 迭代替代 KEYS，避免在大 key 数量时阻塞 Redis。
     */
    @Override
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void syncViewCountsToDb() {
        ScanOptions options = ScanOptions.scanOptions()
                .match(VIEW_KEY_PREFIX + "*")
                .count(100)
                .build();

        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            while (cursor.hasNext()) {
                String key = cursor.next();
                Object val = redisTemplate.opsForValue().get(key);
                if (val == null) continue;
                Long articleId = Long.parseLong(key.replace(VIEW_KEY_PREFIX, ""));
                int count = Integer.parseInt(val.toString());
                articleMapper.incrementViewCount(articleId, count);
                redisTemplate.delete(key);
            }
        }
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalArticles",    articleMapper.selectCount(null));
        stats.put("publishedArticles", articleMapper.countByStatus(ArticleStatus.PUBLISHED.name()));
        stats.put("draftArticles",     articleMapper.countByStatus(ArticleStatus.DRAFT.name()));
        stats.put("pendingComments",   commentMapper.countByStatus(CommentStatus.PENDING.name()));
        stats.put("totalComments",     commentMapper.selectCount(null));
        stats.put("totalUsers",        userMapper.selectCount(null));
        return stats;
    }
}
