package com.englishassistant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.englishassistant.entity.DailyStat;
import com.englishassistant.mapper.DailyStatMapper;
import com.englishassistant.service.DailyStatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * Daily stats service — increment calls are @Async fire-and-forget.
 *
 * Interview talking points:
 * - @Async + @EnableAsync (on AsyncConfig) = Spring AOP proxy intercepts call,
 *   submits to ThreadPoolTaskExecutor instead of blocking caller thread.
 * - Why async here? Stats updates are non-critical — user doesn't need to wait.
 * - @Async requires calling from a different bean (Spring proxy limitation).
 */
@Service
public class DailyStatServiceImpl implements DailyStatService {

    private static final Logger log = LoggerFactory.getLogger(DailyStatServiceImpl.class);

    private final DailyStatMapper dailyStatMapper;

    public DailyStatServiceImpl(DailyStatMapper dailyStatMapper) {
        this.dailyStatMapper = dailyStatMapper;
    }

    @Override
    @Async("aiTaskExecutor")
    public void incrementReview(Long userId) {
        upsert(userId, "words_reviewed");
        log.debug("Async: incremented review for user {}", userId);
    }

    @Override
    @Async("aiTaskExecutor")
    public void incrementEssay(Long userId) {
        upsert(userId, "essays_written");
        log.debug("Async: incremented essay for user {}", userId);
    }

    private void upsert(Long userId, String field) {
        LocalDate today = LocalDate.now();
        DailyStat stat = dailyStatMapper.selectOne(
            new LambdaQueryWrapper<DailyStat>()
                .eq(DailyStat::getUserId, userId)
                .eq(DailyStat::getDate, today));

        if (stat == null) {
            stat = new DailyStat();
            stat.setUserId(userId);
            stat.setDate(today);
            stat.setWordsLearned(0);
            stat.setWordsReviewed(0);
            stat.setEssaysWritten(0);
            stat.setMinutesSpent(0);
            dailyStatMapper.insert(stat);
        }

        if ("words_reviewed".equals(field)) {
            stat.setWordsReviewed(
                (stat.getWordsReviewed() != null ? stat.getWordsReviewed() : 0) + 1);
        } else if ("essays_written".equals(field)) {
            stat.setEssaysWritten(
                (stat.getEssaysWritten() != null ? stat.getEssaysWritten() : 0) + 1);
        }

        dailyStatMapper.updateById(stat);
    }
}
