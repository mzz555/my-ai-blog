package com.blog.service.impl;

import com.blog.mapper.*;
import com.blog.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

/**
 * 统计服务实现
 * <p>管理文章浏览量：先写入 Redis，再定期（每 5 分钟）批量同步到数据库，
 * 避免高频写库导致的性能问题。</p>
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

    @Override
    @Scheduled(fixedDelay = 300_000)
    @Transactional
    public void syncViewCountsToDb() {
        Set<String> keys = redisTemplate.keys(VIEW_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return;
        keys.forEach(key -> {
            Object val = redisTemplate.opsForValue().get(key);
            if (val != null) {
                Long articleId = Long.parseLong(key.replace(VIEW_KEY_PREFIX, ""));
                int count = Integer.parseInt(val.toString());
                articleMapper.incrementViewCount(articleId, count);
                redisTemplate.delete(key);
            }
        });
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalArticles", articleMapper.selectCount(null));
        stats.put("totalComments", commentMapper.selectCount(null));
        stats.put("totalUsers", userMapper.selectCount(null));
        return stats;
    }
}
