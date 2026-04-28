package com.blog.service;

import java.util.Map;

public interface StatsService {
    void incrementViewCount(Long articleId);
    void syncViewCountsToDb();
    Map<String, Object> getOverview();
    Map<String, Object> getTrend();
}
