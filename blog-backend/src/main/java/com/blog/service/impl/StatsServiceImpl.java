package com.blog.service.impl;

import com.blog.repository.*;
import com.blog.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ArticleRepository articleRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

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
                articleRepository.incrementViewCount(articleId, count);
                redisTemplate.delete(key);
            }
        });
    }

    @Override
    public Map<String, Object> getOverview() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalArticles", articleRepository.count());
        stats.put("totalComments", commentRepository.count());
        stats.put("totalUsers", userRepository.count());
        return stats;
    }
}
