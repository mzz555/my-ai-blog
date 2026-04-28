package com.blog.controller;

import com.blog.common.Result;
import com.blog.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @GetMapping("/overview")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> overview() {
        return Result.success(statsService.getOverview());
    }

    @GetMapping("/trend")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> trend() {
        return Result.success(statsService.getTrend());
    }

    @PostMapping("/articles/{id}/view")
    public Result<Void> recordView(@PathVariable Long id) {
        statsService.incrementViewCount(id);
        return Result.success();
    }
}
