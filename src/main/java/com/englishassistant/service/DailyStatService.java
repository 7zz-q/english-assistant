package com.englishassistant.service;

public interface DailyStatService {
    void incrementReview(Long userId);
    void incrementEssay(Long userId);
}
