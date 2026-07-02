package com.englishassistant.service;

import java.util.Map;

public interface StatsService {
    Map<String, Object> getOverview(Long userId);
    Object getDailyStats(Long userId);
    Map<String, Object> getStreak(Long userId);
    Map<String, Object> getStatsByLevel(Long userId);
}
